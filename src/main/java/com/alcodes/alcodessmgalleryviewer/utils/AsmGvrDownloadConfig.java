package com.alcodes.alcodessmgalleryviewer.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
                    if (fdelete.delete()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(ctxt)

                                .setTitle("Download Completed!")
                            .setMessage("Your file have downloaded!")
                                .setPositiveButton("Ok",null)
                                .show();
                    }
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


}
