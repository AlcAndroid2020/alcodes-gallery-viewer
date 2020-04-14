package com.alcodes.alcodessmgalleryviewer.fragments;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewUnknownfileBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.UnknownFileCallback;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.io.File;
import java.io.FileNotFoundException;
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
    private Uri dirpath;
    private DownloadManager mgr = null;
    private long downloadID;
    private static final int PERMISSION_STORGE_CODE = 1000;
    public File file;
    public String fileName = "";
    public Uri uri = null;
    private DocumentFile fileuri;

    private ActionBar mActionBar;


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

        mActionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();

        return mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //hide and show menu bar
        mDataBinding.previewUnknownFileRoot.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if(mActionBar.isShowing()){
                        mActionBar.hide();
                    }else{
                        mActionBar.show();
                    }
                    return super.onSingleTapUp(e);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }

        });

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

                try {
                    copyFileToSafFolder(getContext(), fileuri.getUri(), fileName);
                    deleteOriginalFile(getContext(), fileuri.getUri(), fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void deleteOriginalFile(Context context, Uri uri, String fileName) {

    }


    @Override
    public void onResume() {
        super.onResume();
        Timber.e("d;;Child fragment at: %s entering onResume", mViewPagerPosition);


    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.e("d;;Child fragment at: %s entering onPause", mViewPagerPosition);

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
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mViewPagerURL));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setType("application/pdf");
            startActivity(Intent.createChooser(shareIntent, "Share..."));
        }

    }

    public Uri copyFileToSafFolder(Context context, Uri src, String destFileName) throws FileNotFoundException {
        //String filePath=src.getPath();
        InputStream inputStream = context.getContentResolver().openInputStream(src);
        String docId = DocumentsContract.getTreeDocumentId(dirpath);
        Uri dirUri = DocumentsContract.buildDocumentUriUsingTree(dirpath, docId);

        Uri destUri;

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
            Toast.makeText(getContext().getApplicationContext(), "File Import Complete", Toast.LENGTH_LONG).show();
            return destUri;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void startDownload() {
        fileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        file = new File(requireContext().getExternalCacheDir(), fileName);
        fileuri = DocumentFile.fromFile(file);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mViewPagerURL))
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager = (DownloadManager) requireContext().getSystemService(DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in

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
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(intent);
    }

    @Override
    public void onDownloadButtonClicked() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, 42);


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 42) {
                if (null != data) {

                    dirpath = data.getData();

                    startDownload();

                }

            }

        }
    }


}
