package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.documentfile.provider.DocumentFile;

public class AsmGvrOpenUnknownFile {

    private Context context;

    private Uri uri;


    public View startOpenUnknownFile(Context getContext, Uri getUri){
        context = getContext;
        uri = getUri;

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        final Button buttonView = new Button(context);
        buttonView.setText("Open With ...");
        ll.addView(buttonView, 0);

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = "";
                if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {

                    filename = uri.toString();
                } else {
                    DocumentFile f = DocumentFile.fromSingleUri(context, uri);
                    filename = f.getName();
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (filename.contains(".doc") || filename.contains(".docx")) {
                    // Word document
                    intent.setDataAndType(uri, "application/msword");
                } else if (filename.contains(".pdf")) {
                    // PDF file
                    intent.setDataAndType(uri, "application/pdf");
                } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
                    // Powerpoint file
                    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
                    // Excel file
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                } else if (filename.contains(".zip") || filename.contains(".rar")) {
                    // WAV audio file
                    intent.setDataAndType(uri, "application/x-wav");
                } else if (filename.contains(".rtf")) {
                    // RTF file
                    intent.setDataAndType(uri, "application/rtf");
                } else if (filename.contains(".wav") || filename.contains(".mp3")) {
                    // WAV audio file
                    intent.setDataAndType(uri, "audio/x-wav");
                } else if (filename.contains(".gif")) {
                    // GIF file
                    intent.setDataAndType(uri, "image/gif");
                } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
                    // JPG file
                    intent.setDataAndType(uri, "image/jpeg");
                } else if (filename.contains(".txt")) {
                    // Text file
                    intent.setDataAndType(uri, "text/plain");
                } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg") || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")) {
                    // Video files
                    intent.setDataAndType(uri, "video/*");
                } else {
                    //if you want you can also define the intent type for any other file
                    //additionally use else clause below, to manage other unknown extensions
                    //in this case, Android will show all applications installed on the device
                    //so you can choose which application to use
                    intent.setDataAndType(uri, "/");
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        return ll;
    }
}
