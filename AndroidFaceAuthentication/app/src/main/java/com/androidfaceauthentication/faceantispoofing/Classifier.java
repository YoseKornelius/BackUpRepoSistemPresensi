package com.androidfaceauthentication.faceantispoofing;

import android.graphics.Bitmap;
import com.androidfaceauthentication.facerecognizer.Recognition;

import java.util.List;

public interface Classifier {

    List<Recognition> recognizeImage(Bitmap bitmap);

    void close();
}
