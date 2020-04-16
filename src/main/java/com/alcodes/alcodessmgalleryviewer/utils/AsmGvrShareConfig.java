package com.alcodes.alcodessmgalleryviewer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

public class AsmGvrShareConfig {

    public AsmGvrShareConfig() {
    }

    public void shareWith(Context context, Uri uri) {

        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            Intent shareIntent = new Intent();
            shareIntent.setType("text/html");
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "This is the URL I'm sharing.");
            shareIntent.putExtra(Intent.EXTRA_TEXT, uri.toString());
            context.startActivity(Intent.createChooser(shareIntent, "Share With..."));

        } else {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "This is the file I'm sharing.");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            context.startActivity(Intent.createChooser(shareIntent, "Share With..."));
        }
    }
}