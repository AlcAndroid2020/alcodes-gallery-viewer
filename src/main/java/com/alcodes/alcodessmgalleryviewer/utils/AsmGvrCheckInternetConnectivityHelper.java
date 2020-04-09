package com.alcodes.alcodessmgalleryviewer.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

public class AsmGvrCheckInternetConnectivityHelper {
    public boolean isNetworkConnected(Activity activity){
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());

        return activeNetwork != null && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
