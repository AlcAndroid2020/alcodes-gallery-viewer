package com.alcodes.alcodessmgalleryviewer.fragments;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewUnknownfileBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.UnknownFileCallback;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.tonyodev.fetch2.Fetch;

import java.util.Locale;

import timber.log.Timber;

public class AsmGvrPreviewUnknowFileFragment extends Fragment implements UnknownFileCallback {
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private NavController mNavController;
    private AsmGvrFragmentPreviewUnknownfileBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;
    private Button btnDownload;

    private Fetch fetch;
    private static final int PERMISSION_STORGE_CODE = 1000;


    public AsmGvrPreviewUnknowFileFragment() {
    }

    public static AsmGvrPreviewUnknowFileFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position);

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

        mDataBinding.textViewDemo.setText(String.format(Locale.ENGLISH, "Position: %d", mViewPagerPosition));

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startdownloading();
                }
            }
        }
    }
    @Override
    public void onShareButtonClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permission, PERMISSION_STORGE_CODE);
            } else {
                startdownloading();
            }

        } else {
            startdownloading();
        }
    }


    private void startdownloading() {
        String URL = "https://files.eric.ed.gov/fulltext/ED573583.pdf";

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(URL));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download");
        request.setDescription("Downloading file...");

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis());

        DownloadManager manager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
