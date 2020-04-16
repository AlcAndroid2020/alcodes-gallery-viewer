package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewUnknownfileBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.UnknownFileCallback;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrDownloadConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrOpenWithConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrShareConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.io.File;

import static android.content.Context.DOWNLOAD_SERVICE;

public class AsmGvrPreviewUnknowFileFragment extends Fragment implements UnknownFileCallback {
    private final AsmGvrDownloadConfig mDownloadConfig;
    private final AsmGvrShareConfig mShareConfig;
    private final AsmGvrOpenWithConfig mOpenWithConfig;
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
    public File file;
    public String fileName = "";
    public Uri uri = null;
    private DocumentFile fileuri;

    private ActionBar mActionBar;


    public AsmGvrPreviewUnknowFileFragment() {
        mDownloadConfig = new AsmGvrDownloadConfig();
        mShareConfig = new AsmGvrShareConfig();
        mOpenWithConfig = new AsmGvrOpenWithConfig();
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

        mActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();

        return mDataBinding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //hide and show menu bar
        mDataBinding.previewUnknownFileRoot.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (mActionBar.isShowing()) {
                        mActionBar.hide();
                    } else {
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

        uri = Uri.parse(mViewPagerURL);

        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            mDataBinding.btnDownload.setVisibility(View.VISIBLE);
        } else {
            mDataBinding.btnDownload.setVisibility(View.INVISIBLE);
        }

        mDataBinding.setBindingCallback(this);

        //get selected color
        mMainSharedViewModel.getColorSelectedLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer!=null){
                    mDataBinding.previewUnknownFileRoot.setBackgroundColor(ContextCompat.getColor(getActivity(),  integer));

                }
            }
        });


    }


    @Override
    public void onShareButtonClicked() {
        mShareConfig.shareWith(getContext(),Uri.parse(mViewPagerURL));
    }

    @Override
    public void onOpenWithButtonClicked() {
        mOpenWithConfig.openWith(getContext(),Uri.parse(mViewPagerURL));
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
                    mDownloadConfig.startDownload(getContext(), mViewPagerURL,dirpath);
                }
            }
        }
    }


}
