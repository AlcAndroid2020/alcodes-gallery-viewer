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

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Uri movefileuri = null;
    boolean resultOfComparison = false;

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
        if (checkDuplicate(context, path) == true) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("Download Warning");
            builder.setMessage("The File U Already Download, Are u Want To Downlod Again?");
            builder.setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Download(context, uri, path);
                }
            });
            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        } else {
            Download(context, uri, path);
        }
    }

    public boolean checkDuplicate(Context context, Uri path) {

        DocumentFile documentFile = DocumentFile.fromTreeUri(context, path);
        DocumentFile[] files = documentFile.listFiles();
        if (files != null && files.length > 0) {
            for (DocumentFile f : files) {
                if (f.getName().equals(fileName)) {
                    resultOfComparison = true;
                } else {
                    resultOfComparison = false;
                }
            }
        }
        return resultOfComparison;
    }

    public void Download(Context context, String uri, Uri path) {
        mViewPagerURL = uri;
        dirpath = path;
        fileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        file = new File(context.getExternalCacheDir(), fileName);
        fileuri = DocumentFile.fromFile(file);

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
                DocumentFile documentFile = DocumentFile.fromTreeUri(ctxt, dirpath);
                DocumentFile[] files = documentFile.listFiles();
                if (files != null && files.length > 0) {
                    for (DocumentFile file : files) {
                        boolean resultOfComparison = file.getName().equals(fileName);
                        if (resultOfComparison == true) {
                            int i = 1;
                            fileName = fileName.substring(0, fileName.lastIndexOf("."));
                            Matcher m = Pattern.compile("\\((.*?)\\)").matcher(fileName);
                            if (m.find()) {
                                ++i;
                                fileName = fileName.replaceAll("\\s*\\([^\\)]*\\)\\s*", "(" + i + ")" + ".pdf");

                            } else {
                                fileName = fileName + "(" + i + ")" + ".pdf";
                            }
                        }
                    }
                }
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
        Uri destUri = null;

        try {
            //change to src
            destUri = DocumentsContract.createDocument(context.getContentResolver(), dirUri, "*/*", destFileName);

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
