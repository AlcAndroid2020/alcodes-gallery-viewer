package com.alcodes.alcodessmgalleryviewer.utils;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

import androidx.documentfile.provider.DocumentFile;

import com.alcodes.alcodessmgalleryviewer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AsmGvrDownloadConfig {
    private Uri mDirpath;
    private DownloadManager mgr = null;
    private long mDownloadID;
    private File mFile;
    private String mFileName = "";
    private DocumentFile mFileuri;
    private String mViewPagerURL;
    private Uri mMovefileuri = null;

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    public AsmGvrDownloadConfig() {
    }

    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public void startDownload(Context context, String uri, Uri path) {

        mViewPagerURL = uri;
        mFileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        String randomString=getRandomString(5);
        String extension = mFileName.substring(mFileName.indexOf('.') + 1 );
        mFileName = randomString +"."+extension;
        mDirpath = path;
        mFile = new File(context.getExternalCacheDir(), mFileName);
        mFileuri = DocumentFile.fromFile(mFile);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mViewPagerURL))
                .setTitle(mFileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(mFile))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        mDownloadID = downloadManager.enqueue(request);// enqueue puts the download request in

        mgr = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }




    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (mDownloadID == id) {
                try {
                    mMovefileuri = copyFileToSafFolder(ctxt, mFileuri.getUri(), mDirpath, mFileName);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //delete file, after move file complete
                if (mMovefileuri != null) {
                    Uri delfile = mFileuri.getUri();

                    File fdelete = new File(delfile.getPath());

                    if (fdelete.delete()) {
                        AlertDialog alertDialog = new AlertDialog.Builder(ctxt)

                                .setTitle(ctxt.getString(R.string.DownloadComplete))
                                .setMessage(ctxt.getString(R.string.DownloadCompleteMessage))
                                .setPositiveButton(ctxt.getString(R.string.Okay), null)
                                .show();
                        ctxt.unregisterReceiver(onComplete);
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
            destUri = DocumentsContract.createDocument(context.getContentResolver(), dirUri, "/", destFileName);

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