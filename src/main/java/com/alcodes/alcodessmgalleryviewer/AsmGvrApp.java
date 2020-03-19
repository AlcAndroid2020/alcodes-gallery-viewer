package com.alcodes.alcodessmgalleryviewer;

import android.app.Application;

import timber.log.Timber;

public class AsmGvrApp {

    // TODO REF: https://medium.com/@deepakpk/how-to-add-a-git-android-library-project-as-a-sub-module-c713a653ab1f
    // TODO REF: https://developer.android.com/studio/projects/add-app-module
    // TODO REF: https://proandroiddev.com/your-android-libraries-should-not-ask-an-application-context-51986cc140d4
    // TODO REF: https://archie94.github.io/blogs/working-with-submodules-in-git-and-android-studio

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
