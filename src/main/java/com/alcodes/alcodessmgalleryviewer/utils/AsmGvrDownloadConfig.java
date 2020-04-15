package com.alcodes.alcodessmgalleryviewer.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AsmGvrDownloadConfig {
    private Uri dirpath;
    private DownloadManager mgr = null;
    private long downloadID;
    public File file;
    public String fileName = "";
    public Uri uri = null;
    private DocumentFile fileuri;
    private ContentResolver cR;
    private String mViewPagerURL;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public Uri getUri() {
        return uri;
    }

    public AsmGvrDownloadConfig() {
    }


    public void startDownload(Context context, String uri, Uri path) {
        mViewPagerURL = uri;
//        mViewPagerURL="http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        dirpath = path;
        fileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        file = new File(context.getExternalCacheDir(), fileName);
        fileuri = DocumentFile.fromFile(file);

//        DocumentFile documentFile = DocumentFile.fromTreeUri(context, dirpath);
//        DocumentFile[] files = documentFile.listFiles();
//        if (files != null && files.length > 0) {
//
//            for (DocumentFile file : files) {
//                Toast.makeText(context, file.getName(), Toast.LENGTH_SHORT).show();
//                if (file.getName()!=fileName){
//
//                    fileName="test";
//                }
//            }
//        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mViewPagerURL))
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in

        mgr = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);

            if (downloadID == id) {
                Uri movefileuri = null;
                //Move File to user selected file
                try {
                    movefileuri = copyFileToSafFolder(ctxt, fileuri.getUri(), dirpath, fileName);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //delete file, after move file complete
                if (movefileuri != null) {
                    Uri delfile = fileuri.getUri();
                    File fdelete = new File(delfile.getPath());
                    if (fdelete.delete())
                        Toast.makeText(ctxt, "ooooooooo", Toast.LENGTH_SHORT).show();
                }
            }


        }
    };

    public Uri copyFileToSafFolder(Context context, Uri src, Uri dirpath, String destFileName) throws FileNotFoundException {
        InputStream inputStream = context.getContentResolver().openInputStream(src);
        String docId = DocumentsContract.getTreeDocumentId(dirpath);
        Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(dirpath, docId);
        Uri a;
        Uri destUri = null;
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, dirpath);
        DocumentFile[] files = documentFile.listFiles();
        if (files != null && files.length > 0) {

            for (DocumentFile file : files) {
                Toast.makeText(context, file.getName(), Toast.LENGTH_SHORT).show();

                if (file.getUri() == destUri) {

                    fileName = "test";
                }
            }
        }


        try {
            //change to src
            destUri = DocumentsContract.createDocument(context.getContentResolver(), dirUri, "*/*", fileName);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = inputStream;

            os = context.getContentResolver().openOutputStream(destUri, "w");

            byte[] buffer = new byte[1024];

            int length;
            while ((length = is.read(buffer)) > 0)
                os.write(buffer, 0, length);

            is.close();
            os.flush();
            os.close();

            return destUri;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

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
        } else if (filename.contains(".wav") || filename.contains(".mp3")) {        // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (filename.contains(".gif")) {                                     // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
            intent.setDataAndType(uri, "image/jpeg");
        } else if (filename.contains(".txt")) {
            intent.setDataAndType(uri, "text/plain");
        } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg") || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")) {
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "/");
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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
