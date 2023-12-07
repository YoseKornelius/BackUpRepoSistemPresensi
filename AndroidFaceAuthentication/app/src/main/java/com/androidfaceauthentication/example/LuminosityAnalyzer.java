package com.androidfaceauthentication.example;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

//analyzer demo usecase
public class LuminosityAnalyzer implements ImageAnalysis.Analyzer {
    private static final String TAG = "LuminosityAnalyzer";

    private byte[] byteBufferToByteArray(ByteBuffer byteBuffer){
        byte[] arr = new byte[byteBuffer.remaining()];
        byteBuffer.get(arr);
        return arr;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        ByteBuffer byteBuffers = image.getPlanes()[0].getBuffer();
        byte[] data = byteBufferToByteArray(byteBuffers);
//        data.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "run analysis");
        image.close();
    }
}
