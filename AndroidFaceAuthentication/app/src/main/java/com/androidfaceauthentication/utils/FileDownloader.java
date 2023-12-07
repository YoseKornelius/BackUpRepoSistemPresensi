package com.androidfaceauthentication.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.androidfaceauthentication.App;

import java.util.Objects;

/*
file downloader helper
* */
public class FileDownloader {
    public static void downloadFile(String download, Context context) {
        String DownloadUrl = download;
        DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
        request1.setDescription("Sample File");   //appears the same in Notification bar while downloading
//        request1.setTitle("File1.mp3");

        request1.setVisibleInDownloadsUi(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner();
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        }
//        request1.setDestinationInExternalFilesDir(context, "/File", "Question1.mp3");
        request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "CinemaHD.png");

        DownloadManager manager1 = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Objects.requireNonNull(manager1).enqueue(request1);
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            Log.d("FileDownloader ", "downloadFile: ");
        }
    }
}
