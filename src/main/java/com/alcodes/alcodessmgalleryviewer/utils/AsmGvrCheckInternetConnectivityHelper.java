package com.alcodes.alcodessmgalleryviewer.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

import androidx.annotation.NonNull;

import timber.log.Timber;

public class AsmGvrCheckInternetConnectivityHelper {
    public boolean isNetworkConnected(Activity activity){
        ConnectivityManager cm =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());


        return activeNetwork != null && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public void initIsNetworkConnectedListener(Activity activity){
        final ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(
                    builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            Toast.makeText(activity, "Internet is on", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            Toast.makeText(activity, "Internet is off", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }
}
