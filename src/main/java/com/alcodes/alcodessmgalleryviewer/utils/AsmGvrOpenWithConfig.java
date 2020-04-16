package com.alcodes.alcodessmgalleryviewer.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

public class AsmGvrOpenWithConfig {
    public AsmGvrOpenWithConfig() {
    }


    public void openWith(Context context, Uri uri) {
        String filename = "";

        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            filename = uri.toString();
        } else {
            DocumentFile f = DocumentFile.fromSingleUri(context, uri);
            filename = f.getName();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (filename.contains(".doc") || filename.contains(".docx")) {
            intent.setDataAndType(uri, "application/msword");               // Word document
        } else if (filename.contains(".pdf")) {
            intent.setDataAndType(uri, "application/pdf");                   // PDF file
        } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");    // Powerpoint file
        } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
            intent.setDataAndType(uri, "application/vnd.ms-excel");         // Excel file
        } else if (filename.contains(".zip") || filename.contains(".rar")) {
            intent.setDataAndType(uri, "application/x-wav");                  // WAV audio file
        } else if (filename.contains(".rtf")) {                                     // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (filename.contains(".wav") || filename.contains(".mp3") ||
                filename.contains(".m4a") || filename.contains(".flac") || filename.contains(".gsm")
                || filename.contains(".mkv") || filename.contains(".ogg") || filename.contains(".mid")
                || filename.contains(".mxmf") || filename.contains(".xmf") || filename.contains(".ota")
                || filename.contains(".imy")) {        // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (filename.contains(".gif")) {                                     // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (filename.contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg")
                || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")
                || filename.contains(".webm")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "/");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
