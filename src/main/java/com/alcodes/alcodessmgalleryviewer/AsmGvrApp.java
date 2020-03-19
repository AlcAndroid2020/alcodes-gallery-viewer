package com.alcodes.alcodessmgalleryviewer;

import android.app.Application;

import timber.log.Timber;

public class AsmGvrApp {

    public static final String TAG = AsmGvrApp.class.getSimpleName();

    public static void init(Application application) {
        if (BuildConfig.DEBUG) {
            if (Timber.treeCount() > 0) {
                // Timber is already init-ed.
                Timber.tag(TAG).i("Timber: debug tree already init from main module.");
            } else {
                // No debug tree plant yet.
                Timber.plant(new Timber.DebugTree());

                Timber.tag(TAG).i("Timber: debug tree init successfully.");
            }
        }
    }
}
