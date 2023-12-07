package com.androidfaceauthentication.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.base.GraphicOverlay;
import com.androidfaceauthentication.base.VisionImageProcessor;
import com.androidfaceauthentication.faceantispoofing.AntispoofLog;
import com.androidfaceauthentication.faceantispoofing.TFLiteFaceSpoofingDetector;
import com.androidfaceauthentication.facedetector.FaceDetectorProcessor;
import com.androidfaceauthentication.facedetector.FaceGraphic;
import com.androidfaceauthentication.facerecognizer.Recognition;
import com.androidfaceauthentication.facerecognizer.TFLiteFaceRecognizer;
import com.androidfaceauthentication.network.APIInterface;
import com.androidfaceauthentication.network.RetrofitClient;
import com.androidfaceauthentication.utils.BitmapUtils;
import com.androidfaceauthentication.utils.PreferenceUtils;
import com.androidfaceauthentication.view.pojo.PresensiMahasiswaRequest;
import com.androidfaceauthentication.viewmodel.CameraXViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FaceVerificationActivity extends AppCompatActivity {
    private static final String TAG = "FaceVerificationActivity";
    private int lensFacing = CameraSelector.LENS_FACING_FRONT;
    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    private CameraSelector cameraSelector;
    private static final int PERMISSION_REQUESTS = 1;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private Preview previewUseCase;

    @Nullable
    private VisionImageProcessor imageProcessor;

    private TFLiteFaceRecognizer faceRecoginzer;

    private TFLiteFaceSpoofingDetector faceSpoofingDetector;

    private ImageCapture imageCapture;

    //    private boolean needUpdateGraphicOverlayImageSourceInfo;
    boolean flipX = false;

    APIInterface apiInterface;
    String idjadwal, nim, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        setContentView(R.layout.activity_face_verification);
        Intent recieveIntent = getIntent();
        idjadwal = recieveIntent.getStringExtra("idjadwal");
        nim = recieveIntent.getStringExtra("nim");
        email = recieveIntent.getStringExtra("email");
        previewView = findViewById(R.id.preview_view);
        apiInterface = RetrofitClient.getClient().create(APIInterface.class);
        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        try {
            faceSpoofingDetector = TFLiteFaceSpoofingDetector.getInstance(this.getAssets());
            faceRecoginzer = TFLiteFaceRecognizer.getInstance(this.getAssets());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ToggleButton facingSwitch = findViewById(R.id.facing_switch);
        facingSwitch.setOnCheckedChangeListener(this::onCheckedChanged);
        Button btnTakeImage = findViewById(R.id.btn_take_image);
        btnTakeImage.setOnClickListener(this::onTakeImageClicked);

        new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(CameraXViewModel.class).getProcessCameraProvider().observe(this, provider -> {
            cameraProvider = provider;
            if (allPermissionsGranted()) {
                bindAllCameraUseCases();
            }
        });

        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindAllCameraUseCases();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (cameraProvider == null) {
            return;
        }
        int newLensFacing = lensFacing == CameraSelector.LENS_FACING_FRONT ? CameraSelector.LENS_FACING_BACK : CameraSelector.LENS_FACING_FRONT;
        CameraSelector newCameraSelector = new CameraSelector.Builder().requireLensFacing(newLensFacing).build();
        try {
            if (cameraProvider.hasCamera(newCameraSelector)) {
                Log.d(TAG, "Set facing to " + newLensFacing);
                lensFacing = newLensFacing;
                if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    flipX = true;
                } else {
                    flipX = false;
                }
                cameraSelector = newCameraSelector;
                bindAllCameraUseCases();
                return;
            }
        } catch (CameraInfoUnavailableException e) {
            // Falls through
        }
        Toast.makeText(getApplicationContext(), "This device does not have lens with facing: " + newLensFacing, Toast.LENGTH_SHORT).show();
    }

    private void onTakeImageClicked(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            imageCapture.takePicture(getMainExecutor(), new ImageCapture.OnImageCapturedCallback() {
                public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {

                    @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
                    InputImage inputImage = null;
                    Bitmap sourceImageBitmap = BitmapUtils.convertJPEGImageProxyJPEGToBitmap(imageProxy);
                    int imageRotation = imageProxy.getImageInfo().getRotationDegrees();
                    if (image != null) {
                        inputImage = InputImage.fromBitmap(sourceImageBitmap, imageRotation);
                    }

                    Log.d(TAG, "image format  " + image.getFormat());
                    FaceDetectorProcessor faceDetectorProcessor = (FaceDetectorProcessor) imageProcessor;

                    faceDetectorProcessor.detectInImage(inputImage).addOnSuccessListener(faces -> {
                        if (faces.size() != 0) {
                            Face face = faces.get(0); //Get first face from detected faces

                            //Adjust orientation of Face
                            Bitmap frameBitmap = BitmapUtils.rotateBitmap(sourceImageBitmap, imageRotation, false, false);

                            //Get bounding box of face
                            RectF boundingBox = new RectF(face.getBoundingBox());

                            //Crop out bounding box from whole Bitmap(image)
                            Bitmap cropped_face = BitmapUtils.getCropBitmapByCPU(frameBitmap, boundingBox);

                            if (flipX)
                                cropped_face = BitmapUtils.rotateBitmap(cropped_face, 0, flipX, false);
                            //Scale the acquired Face to TFLiteFaceRecognizer.T_API_INPUT_SIZE*TFLiteFaceRecognizer.T_API_INPUT_SIZE which is required input for model
                            Bitmap recognizeScaled = BitmapUtils.getResizedBitmap(cropped_face, TFLiteFaceRecognizer.T_API_INPUT_SIZE, TFLiteFaceRecognizer.T_API_INPUT_SIZE);
                            //Bitmap spoofScaled = BitmapUtils.getResizedBitmap(cropped_face, TFLiteFaceSpoofingDetector.TF_INPUT_SIZE, TFLiteFaceSpoofingDetector.TF_INPUT_SIZE);
                            faceVerificationDialog(recognizeScaled);
                        }

                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "on failure faceDetectorProcessor: ", e.getCause());
                    }).addOnCompleteListener(task -> {
                        imageProxy.close();
                    });
                }

                public void onError(@NonNull final ImageCaptureException exception) {
                }

            });
        }
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider.unbindAll();
            bindPreviewUseCase(cameraProvider);
            bindAnalysisUseCase(cameraProvider);
        }
    }

    private void bindPreviewUseCase(ProcessCameraProvider cameraProvider) {
        if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
            return;
        }
        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);
        if (targetResolution != null) {
            builder.setTargetResolution(targetResolution);
        }
        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, previewUseCase);
    }

    private void bindAnalysisUseCase(ProcessCameraProvider cameraProvider) {

        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        try {
            Log.i(TAG, "Using Face Detector Processor");
            imageProcessor = new FaceDetectorProcessor(this, "");

        } catch (Exception e) {
            Log.e(TAG, "Can not create FACE_DETECTION image processor", e);
            Toast.makeText(getApplicationContext(), "Can not create image processor: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        ImageAnalysis.Builder imageAnalysisBuilder = new ImageAnalysis.Builder();
        ImageCapture.Builder imageCaptBuilder = new ImageCapture.Builder();
        Size targetResolution = PreferenceUtils.getCameraXTargetResolution(this, lensFacing);

        if (targetResolution != null) {
            imageAnalysisBuilder.setTargetResolution(targetResolution);
            imageCaptBuilder.setTargetResolution(targetResolution);
        }

        if (imageCapture != null) {
            cameraProvider.unbind(imageCapture);
        }
        imageCapture = imageCaptBuilder.setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();

        cameraProvider.bindToLifecycle(/* lifecycleOwner= */ this, cameraSelector, imageCapture);
    }

    private void faceVerificationDialog(Bitmap scaled) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final View view = getLayoutInflater().inflate(R.layout.dialog_add_face, null);
        builder.setView(view);
        ImageView image = view.findViewById(R.id.captured_face);
        image.setImageBitmap(scaled);
        builder.setTitle("Confirm Face");

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (faceSpoofingDetector != null) {
                //check if  spoof
                boolean spoofRecognitions = faceSpoofingDetector.isNotSpoofFace(scaled);
                if (spoofRecognitions) {
                    Log.w(TAG, "face anti spoof detector: face is real OK to go! ");
                    boolean recognizeFace = faceRecoginzer.isFaceRecognized(scaled);
                    if (recognizeFace) {
                        Log.w(TAG, "face recognizer : face recognized!");
                        Toast.makeText(this, "Wajah dikenali", Toast.LENGTH_SHORT).show();

                        PresensiMahasiswaRequest request = new PresensiMahasiswaRequest(idjadwal, nim);
                        Call<Boolean> call = apiInterface.sendPresensimhsRequest(request);
                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if (response.isSuccessful()) {
                                    Log.v(TAG, "ini ngecek code response " + response.code());
                                }
                                Boolean apiResponse = response.body();
                                if (apiResponse) {
                                    Log.v(TAG, "hasil api true " + apiResponse);
                                    Intent intent = new Intent(getApplicationContext(), JadwalActivity.class);
                                    intent.putExtra("idjadwal", idjadwal);
                                    intent.putExtra("email", email);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                Log.v(TAG, "ini on failure " + t.getMessage());
                            }
                        });
                    } else {
                        Log.e(TAG, "face recognize detector: terdeteksi wajah orang lain ");
                        Toast.makeText(this, "Wajah tidak dikenali", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "face anti spoof detector: spoof face detected! ");
                    Toast.makeText(this, "Wajah palsu terdeteksi!", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
        });
        builder.show();
    }

    /*
     * check all permission
     * */
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            bindAllCameraUseCases();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
}