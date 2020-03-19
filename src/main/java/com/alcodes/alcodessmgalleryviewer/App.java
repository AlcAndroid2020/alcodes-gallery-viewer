package com.alcodes.alcodessmgalleryviewer;

import android.app.Application;

import com.alcodes.alcodessmgalleryviewer.BuildConfig;

import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
