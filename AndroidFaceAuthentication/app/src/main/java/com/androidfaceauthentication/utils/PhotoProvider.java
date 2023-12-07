package com.androidfaceauthentication.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * Project: MobileFaceNetAndroid
 * Package: com.mobilefacenetandroid.util
 * <p>
 * User: dendy
 * Date: 10/05/2021
 * Time: 13:37
 * <p>
 * Description : pthoto provider to solve SecurityException requiring a valid ContentProvider on Android 8
 */
public class PhotoProvider extends ContentProvider {
    public static final String CONTENT_PROVIDER_AUTHORITY = "com.mobilefacenetandroid.env.PhotoProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    public static Uri getPhotoUri(File file) {
        Uri outputUri = Uri.fromFile(file);
        Uri.Builder builder = new Uri.Builder()
                .authority(CONTENT_PROVIDER_AUTHORITY)
                .scheme("file")
                .path(outputUri.getPath())
                .query(outputUri.getQuery())
                .fragment(outputUri.getFragment());

        return builder.build();
    }

    public static Uri getPhotoUri(String filepath) {
        Uri outputUri = Uri.fromFile(new File(filepath));
        Uri.Builder builder = new Uri.Builder()
                .authority(CONTENT_PROVIDER_AUTHORITY)
                .scheme("file")
                .path(outputUri.getPath())
                .query(outputUri.getQuery())
                .fragment(outputUri.getFragment());

        return builder.build();
    }
}
