package com.androidfaceauthentication;

import android.app.Application;

public class App extends Application {
    private static App singleton;

    public static App getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
