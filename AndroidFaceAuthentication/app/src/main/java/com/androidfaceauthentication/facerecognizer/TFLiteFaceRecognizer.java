package com.androidfaceauthentication.facerecognizer;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;
import android.util.Pair;

import com.androidfaceauthentication.utils.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

/**
 * this class used to Determines if the person in one image appears in one of the images of the database.
 * this will do face recognition and do similarity check on by comparing face embeddings
 */
public class TFLiteFaceRecognizer {
    private static final Logger LOGGER = new Logger();

    private static final int OUTPUT_SIZE = 192;

    // Only return this many results.
    private static final int NUM_DETECTIONS = 1;

    // Float model
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    // Number of threads in the java app
    private static final int NUM_THREADS = 4;
    private boolean isModelQuantized;
    // Config values.
    private int inputSize;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
    // contains the location of detected boxes
    private float[][][] outputLocations;
    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the classes of detected boxes
    private float[][] outputClasses;
    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
    // contains the scores of detected boxes
    private float[][] outputScores;
    // numDetections: array of shape [Batchsize]
    // contains the number of detected boxes
    private float[] numDetections;

    public static final int T_API_INPUT_SIZE = 112;
    public static final boolean TF_API_IS_QUANTIZED = false;
    public static final String TF_API_MODEL_FILE = "mobile_face_net.tflite";
    public static final String TF_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    private static final int BATCH_SIZE = 1;

    private static final int PIXEL_SIZE = 3;
//    private ByteBuffer imgData;

    private InterpreterApi tfLite;

    // Face Mask Detector Output
    private float[][] output;

    private final HashMap<String, Recognition> registered = new HashMap<>();

    private static TFLiteFaceRecognizer instance = null;

    private Interpreter.Options intepreterOption;

    private TFLiteFaceRecognizer() {
    }


    public void register(String name, Recognition rec) {
        registered.put(name, rec);
    }

    /**
     * Memory-map the model file in Assets.
     */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)
            throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public static TFLiteFaceRecognizer getInstance() throws IOException {
        if (instance != null) {
            return instance;
        } else {
            throw new IOException("instance is null. Please load the tf lite first");
        }
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     */
    public static TFLiteFaceRecognizer getInstance(
            final AssetManager assetManager)
            throws IOException {

        if (instance != null) {
            return instance;
        } else {
            instance = new TFLiteFaceRecognizer();

            String actualFilename = TF_API_LABELS_FILE.split("file:///android_asset/")[1];
            InputStream labelsInput = assetManager.open(actualFilename);
            BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput));
            String line;
            while ((line = br.readLine()) != null) {
                LOGGER.w(line);
                instance.labels.add(line);
            }
            br.close();

            instance.inputSize = T_API_INPUT_SIZE;

            instance.isModelQuantized = TF_API_IS_QUANTIZED;
            // Pre-allocate buffers.
            int numBytesPerChannel;
            if (instance.isModelQuantized) {
                numBytesPerChannel = 1; // Quantized
            } else {
                numBytesPerChannel = 4; // Floating point
            }
//            instance.imgData = ByteBuffer.allocateDirect(BATCH_SIZE * instance.inputSize * instance.inputSize
//                    * PIXEL_SIZE * numBytesPerChannel);
//            instance.imgData.order(ByteOrder.nativeOrder());
            instance.intValues = new int[instance.inputSize * instance.inputSize];

            instance.outputLocations = new float[1][NUM_DETECTIONS][4];
            instance.outputClasses = new float[1][NUM_DETECTIONS];
            instance.outputScores = new float[1][NUM_DETECTIONS];
            instance.numDetections = new float[1];

            instance.intepreterOption = new Interpreter.Options();
            CompatibilityList gpuCompatList = new CompatibilityList();

            if (gpuCompatList.isDelegateSupportedOnThisDevice()) {
                // if the device has a supported GPU, add the GPU delegate
                GpuDelegate.Options delegateOptions = gpuCompatList.getBestOptionsForThisDevice();
                GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
                instance.intepreterOption.addDelegate(gpuDelegate);
            } else {
                LOGGER.i(" GPU is not supported, run on " + NUM_THREADS + " threads");
                instance.intepreterOption.setNumThreads(NUM_THREADS);
            }

            try {
                instance.tfLite = new Interpreter(loadModelFile(assetManager, TF_API_MODEL_FILE),
                        instance.intepreterOption);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return instance;
        }
    }

    // looks for the nearest embeddings in the dataset (using L2 norm)
    // this triplet loss function from FaceNet are implemented
    //https://vincentblog.xyz/posts/an-overview-of-face-recognition
    // and retrurns the pair <id, distance>
    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> ret = null;
        for (Map.Entry<String, Recognition> entry : registered.entrySet()) {
            final String name = entry.getKey();
            final float[] knownEmb = entry.getValue().getEmbeddings();

            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                //Means the l2 distance or norm between the anchor embedding and positive embedding.
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            //l2 distance is calculated as the square root of the sum of the squared vector values.
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(name, distance);
            }
            Log.v("hasil distance", String.valueOf(distance));
        }
        return ret;
    }

    //convert bitmap to byteBUffer based on model tensor output
    //remember that we would need 4 bytes for each value if our datatype is float
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        if (instance.isModelQuantized) {
            byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        } else {
            byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        //Also, there's a separate function to add float values to the byte buffer.
        //Replace byteBuffer.put with byteBuffer.putFloat.
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];
                if (instance.isModelQuantized) {
                    byteBuffer.put((byte) ((val >> 16) & 0xFF));
                    byteBuffer.put((byte) ((val >> 8) & 0xFF));
                    byteBuffer.put((byte) (val & 0xFF));
                } else {
                    byteBuffer.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    byteBuffer.putFloat((((val) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }
        return byteBuffer;
    }

    public float[][] getEmbeddingsFromImage(Bitmap bitmap) {
        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        Trace.endSection(); // preprocessBitmap
        // Copy the input data into TensorFlow.
        Object[] inputArray = {byteBuffer};
        // Here outputMap is changed to fit the Face Mask detector
        Map<Integer, Object> outputMap = new HashMap<>();
        //current embeedings
        float[][] embeedings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeedings);
        // Run the inference call.
        Trace.beginSection("Run the inference call.");
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
        Trace.endSection();
        return embeedings;
    }

    /*
    here process to recognize images using tflite model done
    1 . it will check if the model are optimized (quntized) or not. if yes the we convert the
     input value to INT
    2. the we feed the input, proceess it using TF and get it embeddings
    3. we calculate the embedding using triplet loss implemented using L2 Norm function
    4. we return back the result as Array List
    * */
    public Recognition recognizeImage(Bitmap bitmap, boolean saveEmbedings) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        float[][] embeedings = getEmbeddingsFromImage(bitmap);
        float distance = Float.MAX_VALUE;
        String id = "0";
        String label = "unknown";

        //check if there is registered face exist
        if (registered.size() > 0) {
            //compare the distance of face registered embeddings vs detected face embeddings
            final Pair<String, Float> nearest = findNearest(embeedings[0]);
            if (nearest != null) {
                final String name = nearest.first;
                label = name;
                distance = nearest.second;
                LOGGER.i("nearest: " + name + " - distance: " + distance);
            }
        }
        final Recognition recognition = new Recognition(
                id,
                label,
                distance,
                new RectF());

//        if (saveEmbedings) {
//            recognition.setEmbeddings(embeedings);
//        }

//        recognitions.add(rec);

        Trace.endSection();
        //return the result
        return recognition;
    }

    //check if face recognized by the system or not
    //recognition done by compare the input face embedding with db face embeddings
    //if distance biger than threshold, then the face are unrecognized
//    https://medium.com/gravel-engineering/recognizing-face-in-android-using-deep-neural-network-tensorflow-lite-be980efea656
    public boolean isFaceRecognized(Bitmap bitmap) {
        float[][] embeedings = getEmbeddingsFromImage(bitmap);
        float similarityThreshold = 0.9f;
        float distance;
        final Pair<String, Float> nearest = findNearest(embeedings[0]);
        if (nearest != null) {
            distance = nearest.second;
            LOGGER.i("embeding Distance: " + distance);
            if (distance <= similarityThreshold) {
                return true;
            }
        }
        return false;
    }
}
