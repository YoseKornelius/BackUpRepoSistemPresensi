package com.androidfaceauthentication.faceantispoofing;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.androidfaceauthentication.facerecognizer.Recognition;
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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.InterpreterApi;

import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;

/**
 * this class used to Determines if the frame image is spoof real or fake face
 * tensorflow code adopted from
 * https://github.com/amitshekhariitbhu/Android-TensorFlow-Lite-Example/
 */
public class TFLiteFaceSpoofingDetector implements Classifier {

    private String TAG= "TFLiteFaceSpoofingDetector";
    private static final Logger LOGGER = new Logger();
    private InterpreterApi tfLite;
    private static TFLiteFaceSpoofingDetector instance = null;
    private List<String> labels = new Vector<>();

    //quantizied float value
    private static final int NUM_THREADS = 4;

    //https://stackoverflow.com/questions/57963341/why-does-the-tensorflow-lite-example-use-image-mean-and-image-std-when-adding-pi
    private static final float IMAGE_MEAN = 127.5f;
    private static final float IMAGE_STD = 127.5f;
    public static final int TF_INPUT_SIZE = 224;
    //public static final int TF_INPUT_SIZE = 112;
    private static final int BATCH_SIZE = 1;
    private static final int PIXEL_SIZE = 3;
    public static final boolean TF_IS_QUANTIZED = false;
    public static final String TF_API_MODEL_FILE = "liveness224.tflite";
    //public static final String TF_API_MODEL_FILE = "liveness112.tflite";
    public static final String TF_API_LABELS_FILE = "spooflabel.txt";
    //    private static final int MAX_RESULTS = 1;
    private static final float THRESHOLD = 0.7f;

    // Config values.
    private int inputSize;

    private Interpreter.Options intepreterOption;

    //prevent object creation from another object
    private TFLiteFaceSpoofingDetector() {

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

    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }

    public static TFLiteFaceSpoofingDetector getInstance() throws IOException {
        if (instance != null) {
            return instance;
        } else {
            throw new IOException("instance is null. Please load the tf lite first");
        }
    }

    public static TFLiteFaceSpoofingDetector getInstance(AssetManager assetManager) throws IOException {
        if (instance == null) {
            instance = new TFLiteFaceSpoofingDetector();
            instance.labels = instance.loadLabelList(assetManager, TF_API_LABELS_FILE);

            instance.inputSize = TF_INPUT_SIZE;
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
            instance.tfLite = new Interpreter(loadModelFile(assetManager, TF_API_MODEL_FILE)
                    , instance.intepreterOption);
        }
        return instance;
    }

    @Override
    public void close() {
        tfLite.close();
        tfLite = null;
    }

    private Recognition getSortedResultByte(byte[][] labelProbArray) {
        float confidence = labelProbArray[0][0];
//        Log.i("TAG", "getSortedResultFloat: " + confidence);
        //if confidance < threshold, it is real face, else is spoof
        if (confidence < THRESHOLD) {
            return new Recognition("0",
                    labels.get(0),
                    confidence, TF_IS_QUANTIZED);

        } else {
            return new Recognition("1",
                    labels.get(1),
                    confidence, TF_IS_QUANTIZED);
        }
    }

    private Recognition getSortedResultFloat(float[][] labelProbArray) {
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        float confidence = labelProbArray[0][0];
        //Log.i("TAG", "getSortedResultFloat: " + confidence);
        //if confidance < threshold, it is real face, else is spoof
        if (confidence < THRESHOLD) {
            return new Recognition("0",
//                    labels.get(0) + ", confidence : " + confidence,
                    labels.get(0),
                    confidence, TF_IS_QUANTIZED);

        } else {
            return new Recognition("1",
                    labels.get(1),
                    confidence, TF_IS_QUANTIZED);
        }
    }

    //convert bitmap to byteBUffer based on model tensor output
    //remember that we would need 4 bytes for each value if our datatype is float
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer;
        if (TF_IS_QUANTIZED) {
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
                if (TF_IS_QUANTIZED) {
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

    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        final ArrayList<Recognition> recognitions = new ArrayList<>();
        if (TF_IS_QUANTIZED) {
            byte[][] result = new byte[1][1];
            tfLite.run(byteBuffer, result);
            recognitions.add(getSortedResultByte(result));
            return recognitions;
        } else {
            float[][] result = new float[1][1];
            tfLite.run(byteBuffer, result);
            recognitions.add(getSortedResultFloat(result));
            return recognitions;
        }
    }

    public boolean isNotSpoofFace(Bitmap bitmap) {
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(bitmap);
        Recognition recognition ;
        if (TF_IS_QUANTIZED) {
            byte[][] result = new byte[1][1];
            tfLite.run(byteBuffer, result);
            recognition = getSortedResultByte(result);
        } else {
            float[][] result = new float[1][1];
            tfLite.run(byteBuffer, result);
            recognition = getSortedResultFloat(result);
        }
        Log.d(TAG, "face spoof result = " + recognition.getTitle() + " , confidence = " + recognition.getDistance());
        return recognition.getDistance() < THRESHOLD;
    }
}
