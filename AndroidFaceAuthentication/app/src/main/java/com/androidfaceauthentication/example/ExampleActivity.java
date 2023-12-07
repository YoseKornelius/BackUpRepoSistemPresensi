package com.androidfaceauthentication.example;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.androidfaceauthentication.R;
import com.androidfaceauthentication.base.GraphicOverlay;
import com.androidfaceauthentication.base.VisionImageProcessor;
import com.androidfaceauthentication.facedetector.FaceDetectorProcessor;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ExampleActivity extends AppCompatActivity {

    private static final String TAG = "ExampleActivity";
    private static final int PERMISSION_REQUESTS = 1;

    private PreviewView previewView;
    private VisionImageProcessor imageProcessor;
    private GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camerax_live_preview_activity);
        previewView = findViewById(R.id.preview_view);
        imageProcessor = new FaceDetectorProcessor(this, "example");
        graphicOverlay = findViewById(R.id.graphic_overlay);

        if (previewView == null) {
            Log.d(TAG, "previewView is null");
        }
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }
        if (allPermissionsGranted()) {
            startCamera();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {

        super.onSaveInstanceState(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

        Log.i(TAG, "Permission granted!");
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
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
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

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    boolean needUpdateGraphicOverlayImageSourceInfo = true;

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

                /*imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                        imageProxy -> {
                            if (needUpdateGraphicOverlayImageSourceInfo) {
//                                boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                                boolean isImageFlipped = true;
                                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                                if (rotationDegrees == 0 || rotationDegrees == 180) {
                                    graphicOverlay.setImageSourceInfo(
                                            imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                                } else {
                                    graphicOverlay.setImageSourceInfo(
                                            imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                                }
                                needUpdateGraphicOverlayImageSourceInfo = false;
                            }

                            try {
                                imageProcessor.processImageProxy(imageProxy, graphicOverlay);
                            } catch (MlKitException e) {
                                Log.e(TAG, "Failed to process image. Error: " + e.getLocalizedMessage());
                                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                );*/
                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(),
                        new LuminosityAnalyzer()
                );


                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));

    }
}
