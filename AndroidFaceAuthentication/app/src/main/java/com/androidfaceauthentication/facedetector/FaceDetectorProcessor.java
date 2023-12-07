/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidfaceauthentication.facedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.androidfaceauthentication.faceantispoofing.AntispoofLog;
import com.androidfaceauthentication.faceantispoofing.Classifier;
import com.androidfaceauthentication.faceantispoofing.TFLiteFaceSpoofingDetector;
import com.androidfaceauthentication.facerecognizer.Recognition;
import com.androidfaceauthentication.facerecognizer.TFLiteFaceRecognizer;
import com.androidfaceauthentication.utils.Logger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.androidfaceauthentication.base.GraphicOverlay;
import com.androidfaceauthentication.base.VisionProcessorBase;
import com.androidfaceauthentication.utils.PreferenceUtils;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Face Detector Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = new Logger();
    private static final String TAG = "FaceDetectorProcessor";

    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final float MAXIMUM_CONFIDENCE = 1.0f;

    private final FaceDetector faceDetector;

    private TFLiteFaceSpoofingDetector faceSpoofingDetector;

    //recognizion part
    private TFLiteFaceRecognizer faceRecoginzer;

    String testMode;
    String name;

    SensorManager sensorManager;
    Sensor luxSensor;

    private float luxValue;

    public FaceDetectorProcessor(Context context, @NonNull String testMode) {
        super(context);
        FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
        Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
        faceDetector = FaceDetection.getClient(faceDetectorOptions);

        //if test mode is empty string then ignore saving log on firebase
        this.testMode = testMode;
        if (!this.testMode.isEmpty()) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            name = user.getUid() + "_" + user.getDisplayName().replace(" ", "");
        }

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        luxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (luxSensor != null) {
            sensorManager.registerListener(listenLux, luxSensor, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            Toast.makeText(context, "Sensor cahaya tidak ditemukan", Toast.LENGTH_SHORT).show();
        }
        try {
            //init face recognition
            faceRecoginzer = TFLiteFaceRecognizer.getInstance(context.getAssets());

            //init face antispoof
            faceSpoofingDetector = TFLiteFaceSpoofingDetector.getInstance(context.getAssets());

        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast = Toast.makeText(context, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
//            finish();
        }
    }


    SensorEventListener listenLux = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            luxValue = sensorEvent.values[0];
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    public void stop() {
        super.stop();
        faceDetector.close();
        sensorManager.unregisterListener(listenLux, luxSensor);
    }

    @Override
    public Task<List<Face>> detectInImage(InputImage image) {
        return faceDetector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {

        // embeding di passing ke facedetetector pokoknya terload
        for (Face face : faces) {
            //in here, we will do the recognition process for all detected face.
            //we get detected face image

            //crop the image using face boundingbox
            Bitmap croppedImage = getCroppedImageFace(face, this.getProcessingImage(), TFLiteFaceRecognizer.T_API_INPUT_SIZE, TFLiteFaceRecognizer.T_API_INPUT_SIZE);
            Bitmap croppedImageSpoofDetector = getCroppedImageFace(face, this.getProcessingImage(), TFLiteFaceSpoofingDetector.TF_INPUT_SIZE, TFLiteFaceSpoofingDetector.TF_INPUT_SIZE);

            // spoof detection before doing face recognition
            Recognition recognition = null;
            if (faceSpoofingDetector != null) {
                List<Recognition> recognitions = faceSpoofingDetector.recognizeImage(croppedImageSpoofDetector);
                if (!recognitions.isEmpty()) {
                    Recognition recognizeFace = recognizeFace(croppedImage);
//                    recognizeFace.getTitle();
                    recognition = recognitions.get(0);
//                    Log.d(TAG, "onSuccess: ");
                    if (recognizeFace != null) {
                        recognition.setTitle(recognitions.get(0).getTitle() + " " + recognizeFace.getTitle());
                    }

                    graphicOverlay.add(new FaceGraphic(graphicOverlay, face, recognition));
                    //if test mode is empty string then ignore saving log on firebase
                    if (!this.testMode.isEmpty()) {
                        DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();

                        long tsLong = System.currentTimeMillis() / 1000;
                        AntispoofLog log = new AntispoofLog(tsLong, recognition.getTitle(), recognition.getDistance(), luxValue);

                        mdatabase.child("faceantispooflog").child(name).child(testMode).child(Long.toString(tsLong)).setValue(log);
                    }
                }
            }
        }
    }

    private Bitmap getCroppedImageFace(Face face, Bitmap processingImage, int width, int height) {
        final RectF boundingBox = new RectF(face.getBoundingBox());
        if (boundingBox == null) return null;

        // here the face is do cropping to inference input type
//        Bitmap faceBmp = Bitmap.createBitmap(API_INPUT_SIZE, API_INPUT_SIZE,
//                Bitmap.Config.ARGB_8888);
        Bitmap faceBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas cvFace = new Canvas(faceBmp);
//        LOGGER.i("FACE " + face.toString());

        //crop and scale bitmap to fit input inference size
        float sx = ((float) width) / boundingBox.width();
        float sy = ((float) height) / boundingBox.height();
        Matrix matrix = new Matrix();
        matrix.postTranslate(-boundingBox.left, -boundingBox.top);
        matrix.postScale(sx, sy);
        cvFace.drawBitmap(processingImage, matrix, null);

        return faceBmp;
    }

    //do face recognition process by normalize image data before identify it
    private Recognition onProcessFaceRecognize(Bitmap cropedImage, boolean saveEmbedings) {
        if (cropedImage == null) {
            return null;
        }

        Recognition recognized = faceRecoginzer.recognizeImage(cropedImage, saveEmbedings);
        recognized.setCrop(cropedImage);

        return recognized;
    }


    // fungsi RECOGNIZE????????
    public Recognition recognizeFace(Bitmap croppedImage) {
        /*float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
        switch (MODE) {
            case TF_OD_API:
                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                break;
        }*/

        Recognition recognition = onProcessFaceRecognize(croppedImage, false);
        if (recognition != null) {
            float conf = recognition.getDistance();
            //only give result when confidence meet the threshold
            if (conf < MAXIMUM_CONFIDENCE) {
                Integer color = Color.GREEN;
                Log.i(TAG, "onFaceDetected: " + recognition.getTitle());
                recognition.setColor(color);
                return recognition;
            }
        }
        return null;
    }

    private static void logExtrasForTesting(Face face) {
        if (face != null) {
            Log.v(MANUAL_TESTING_LOG, "face bounding box: " + face.getBoundingBox().flattenToString());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle X: " + face.getHeadEulerAngleX());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle Y: " + face.getHeadEulerAngleY());
            Log.v(MANUAL_TESTING_LOG, "face Euler Angle Z: " + face.getHeadEulerAngleZ());

            // All landmarks
            int[] landMarkTypes = new int[]{FaceLandmark.MOUTH_BOTTOM, FaceLandmark.MOUTH_RIGHT, FaceLandmark.MOUTH_LEFT, FaceLandmark.RIGHT_EYE, FaceLandmark.LEFT_EYE, FaceLandmark.RIGHT_EAR, FaceLandmark.LEFT_EAR, FaceLandmark.RIGHT_CHEEK, FaceLandmark.LEFT_CHEEK, FaceLandmark.NOSE_BASE};
            String[] landMarkTypesStrings = new String[]{"MOUTH_BOTTOM", "MOUTH_RIGHT", "MOUTH_LEFT", "RIGHT_EYE", "LEFT_EYE", "RIGHT_EAR", "LEFT_EAR", "RIGHT_CHEEK", "LEFT_CHEEK", "NOSE_BASE"};
            for (int i = 0; i < landMarkTypes.length; i++) {
                FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
                if (landmark == null) {
                    Log.v(MANUAL_TESTING_LOG, "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
                } else {
                    PointF landmarkPosition = landmark.getPosition();
                    String landmarkPositionStr = String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
                    Log.v(MANUAL_TESTING_LOG, "Position for face landmark: " + landMarkTypesStrings[i] + " is :" + landmarkPositionStr);
                }
            }
            Log.v(MANUAL_TESTING_LOG, "face left eye open probability: " + face.getLeftEyeOpenProbability());
            Log.v(MANUAL_TESTING_LOG, "face right eye open probability: " + face.getRightEyeOpenProbability());
            Log.v(MANUAL_TESTING_LOG, "face smiling probability: " + face.getSmilingProbability());
            Log.v(MANUAL_TESTING_LOG, "face tracking id: " + face.getTrackingId());
        }
    }

    /*
    used to recognize face from image
    * */
    public void recognizeFaceFromImage(Bitmap imageSource, String label) {
        //always assuming the image source rotation is portrait natural orientation
        //https://developer.android.com/training/camerax/orientation-rotation
        //image's counter-clockwise orientation degrees. Only 0, 90, 180, 270 android
        InputImage image = InputImage.fromBitmap(imageSource, 0);
        faceDetector.process(image).addOnSuccessListener(faces -> {
            LOGGER.i(faces.size() + " face detected: recognizing from images step starting");
            for (Face face : faces) {
                Bitmap croppedImage = getCroppedImageFace(face, imageSource, TFLiteFaceRecognizer.T_API_INPUT_SIZE, TFLiteFaceRecognizer.T_API_INPUT_SIZE);
                Recognition recognition = onProcessFaceRecognize(croppedImage, true);
                if (recognition != null) {
                    //directly register the face and its image
                    LOGGER.i("register " + label + " into identified face");
                    recognition.setTitle(label);
                    //faceRecoginzer.register(label, recognition);
                }
            }
        });
    }

    public TFLiteFaceRecognizer getFaceRecoginzer() {
        return faceRecoginzer;
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    public TFLiteFaceSpoofingDetector getFaceSpoofingDetector() {
        return faceSpoofingDetector;
    }
}
