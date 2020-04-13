package com.alcodes.alcodessmgalleryviewer.fragments;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewUnknownfileBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.UnknownFileCallback;
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AsmGvrPreviewUnknowFileFragment extends Fragment implements UnknownFileCallback {
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_PAGER_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private NavController mNavController;
    private AsmGvrFragmentPreviewUnknownfileBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;

    private DownloadManager mgr = null;
    private long downloadID;
    private static final int PERMISSION_STORGE_CODE = 1000;
    public File file;
    public File desFile;
    public String fileName = "";
    public Uri uri = null;
    public AsmGvrPreviewUnknowFileFragment() {
    }

    public static AsmGvrPreviewUnknowFileFragment newInstance(AsmGvrMediaConfig position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position.getPosition());
        args.putString(ARG_String_PAGER_FILEURL, position.getUri());


        AsmGvrPreviewUnknowFileFragment fragment = new AsmGvrPreviewUnknowFileFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewUnknownfileBinding.inflate(inflater, container, false);

        return mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Extract arguments.
        mViewPagerPosition = requireArguments().getInt(ARG_INT_PAGER_POSITION);
        mViewPagerURL = requireArguments().getString(ARG_String_PAGER_FILEURL);

        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        mgr = (DownloadManager) getContext().getSystemService(DOWNLOAD_SERVICE);
        getContext().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
         uri = Uri.parse(mViewPagerURL);

        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            mDataBinding.btnDownload.setVisibility(View.VISIBLE);
        } else {
            mDataBinding.btnDownload.setVisibility(View.INVISIBLE);
        }

        mDataBinding.setBindingCallback(this);
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);

            if (downloadID == id) {

                Toast.makeText(requireContext(), getResources().getString(R.string.DownloadComplete), Toast.LENGTH_SHORT).show();

//                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//                StrictMode.setVmPolicy(builder.build());
////                Uri path = FileProvider.getUriForFile(getActivity(), "com.alcodes.alcodessmgalleryviewer", file);
////                File a = new File(path.toString());//create path from uri
////                File curentPath = new File(a.getParent());
//                try (InputStream in = new FileInputStream(file)) {
//                    try (OutputStream out = new FileOutputStream(desFile)) {
//                        // Transfer bytes from in to out
//                        byte[] buf = new byte[1024];
//                        int len;
//                        while ((len = in.read(buf)) > 0) {
//                            out.write(buf, 0, len);
//                        }
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                handleDownloadedGPXFiles2(Uri.fromFile(desFile));
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Timber.e("d;;Child fragment at: %s entering onResume", mViewPagerPosition);

        if (mMainSharedViewModel.getDowloadProgress() == null) {
            file = null;
        } else {
            file = mMainSharedViewModel.getDowloadProgress();
//            startshare(file);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.e("d;;Child fragment at: %s entering onPause", mViewPagerPosition);
        mMainSharedViewModel.setDownloadPogress(file);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(onComplete);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    startDownload();
                }
            }
        }
    }

    @Override
    public void onShareButtonClicked() {

        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            Intent shareIntent = new Intent();
            shareIntent.setType("text/html");
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "This is the URL I'm sharing.");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mViewPagerURL);
            startActivity(Intent.createChooser(shareIntent, "Share..."));

        } else {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "This is the file I'm sharing.");
            shareIntent.putExtra(Intent.EXTRA_STREAM,Uri.parse(mViewPagerURL));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            startActivity(Intent.createChooser(shareIntent, "Share..."));
        }

    }

    private void startDownload() {
        fileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        file = new File(requireContext().getExternalCacheDir(), fileName);

       /*
       Create a DownloadManager.Request with all the information necessary to start the download
        */
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mViewPagerURL))
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in


//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mViewPagerURL));
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
//        request.setTitle("Download");
//        request.setDescription("Downloading file...");
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + fileName);
//
//        DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//        manager.enqueue(request);


    }

    private void handleDownloadedGPXFiles2(Uri selectedFileUri) {

        String displayName = fileName;
        String fileExtension;
        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(selectedFileUri, null, null, null, null);
        try {

            File destDirectory = new File(getContext().getExternalCacheDir(), "Imported");
            File destFile = new File(destDirectory, displayName);
            FileOutputStream outStream = new FileOutputStream(destFile);
            InputStream in = getContext().getContentResolver().openInputStream(selectedFileUri);
            OutputStream out = outStream;
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
            Toast.makeText(getContext().getApplicationContext(), "File Import Complete", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext().getApplicationContext(), "File Import FAILED", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }


    }


    @Override
    public void onOpenWithButtonClicked() {
        String filename = "";
        uri = Uri.parse(mViewPagerURL);
        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            filename = uri.toString();
        } else {
            DocumentFile f = DocumentFile.fromSingleUri(getContext(), uri);
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
            intent.setDataAndType(uri, "application/vnd.ms-excel");           // Excel file
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

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    @Override
    public void onDownloadButtonClicked() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//                requestPermissions(permission, PERMISSION_STORGE_CODE);
//            } else {
//                startDownload();
//            }
//
//        } else {
//            startDownload();
//        }
//        startDownload();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        startActivityForResult(intent, 41);

    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 41) {
                if (null != data) {
                    Uri uri= data.getData();

                    desFile = new File(uri.toString());
                    desFile = new File(desFile.getParent());
                    startDownload();

                }

            }

        }
    }

}
