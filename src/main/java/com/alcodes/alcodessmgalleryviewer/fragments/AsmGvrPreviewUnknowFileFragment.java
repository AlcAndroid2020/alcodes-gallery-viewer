package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.bumptech.glide.Glide;

public class AsmGvrPreviewUnknowFileFragment extends Fragment implements UnknownFileCallback {
    private final AsmGvrDownloadConfig mDownloadConfig;
    private final AsmGvrShareConfig mShareConfig;
    private final AsmGvrOpenWithConfig mOpenWithConfig;
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_PAGER_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private NavController mNavController;
    private AsmGvrFragmentPreviewUnknownfileBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private String mViewPagerURL;
    private Uri mDirpath;
    private String mFilename = "";
    private String mFiletype="";
    private Uri uri = null;
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

        mViewPagerURL = requireArguments().getString(ARG_String_PAGER_FILEURL);

        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);


        uri = Uri.parse(mViewPagerURL);
        String URLfileName = URLUtil.guessFileName(mViewPagerURL, null, MimeTypeMap.getFileExtensionFromUrl(mViewPagerURL));
        String realname = URLfileName.substring(URLfileName.indexOf(':') + 1 );
        if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
            mFilename = uri.toString();
            mDataBinding.btnDownload.setVisibility(View.VISIBLE);
            mDataBinding.FileNameView.setText(getResources().getString(R.string.FileName) +"Unknown");
            Glide.with(requireActivity())
                    .load(R.drawable.asm_gvr_no_wifi)
                    .into(mDataBinding.noInternet);
        } else {
            DocumentFile f = DocumentFile.fromSingleUri(getContext(), uri);
            mFilename = f.getName();
            mDataBinding.FileNameView.setText(getResources().getString(R.string.FileName) + realname);
            mDataBinding.btnDownload.setVisibility(View.INVISIBLE);
        }

        mDataBinding.FileTypeView.setText(getResources().getString(R.string.FileType) + checkFileType(mFilename));
        mDataBinding.setBindingCallback(this);

        //get selected color
        mMainSharedViewModel.getColorSelectedLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    mDataBinding.previewUnknownFileRoot.setBackgroundColor(ContextCompat.getColor(getActivity(), integer));

                }
            }
        });
        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                if(internetStatusData.internetStatus){
                    mDataBinding.noInternet.setVisibility(View.INVISIBLE);
//                    mDataBinding.btnDownload.setVisibility(View.VISIBLE);
                    if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
                        mDataBinding.btnDownload.setVisibility(View.VISIBLE);
                    } else {
                        mDataBinding.btnDownload.setVisibility(View.INVISIBLE);
                    }

                }else{

                    mDataBinding.noInternet.setVisibility(View.VISIBLE);
                    if (uri.getScheme().equals("http") | uri.getScheme().equals("https")) {
                        mDataBinding.btnDownload.setVisibility(View.INVISIBLE);
                    } else {
                        mDataBinding.btnDownload.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private String checkFileType(String filename) {
        if (filename.contains(".doc") || filename.contains(".docx")) {
            mFiletype = getResources().getString(R.string.Word);
        } else if (filename.contains(".pdf")) {
            mFiletype = getResources().getString(R.string.pdf);
        } else if (filename.contains(".ppt") || filename.contains(".pptx")) {
            mFiletype = getResources().getString(R.string.powerpoint);
        } else if (filename.contains(".xls") || filename.contains(".xlsx")) {
            mFiletype = getResources().getString(R.string.excel);
        } else if (filename.contains(".zip") || filename.contains(".rar")) {
            mFiletype = getResources().getString(R.string.zip);
        } else if (filename.contains(".rtf")) {
            mFiletype = getResources().getString(R.string.rtf);
        } else if (filename.contains(".wav") || filename.contains(".mp3") ||
                filename.contains(".m4a") || filename.contains(".flac") || filename.contains(".gsm")
                || filename.contains(".mkv") || filename.contains(".ogg") || filename.contains(".mid")
                || filename.contains(".mxmf") || filename.contains(".xmf") || filename.contains(".ota")
                || filename.contains(".imy")) {
            mFiletype = getResources().getString(R.string.audio);
        } else if (filename.contains(".gif")) {
            mFiletype = getResources().getString(R.string.gif);
        } else if (filename.contains(".jpg") || filename.contains(".jpeg") || filename.contains(".png")) {
            mFiletype = getResources().getString(R.string.image);
        } else if (filename.contains(".txt")) {
            mFiletype = getResources().getString(R.string.text);
        } else if (filename.contains(".3gp") || filename.contains(".mpg") || filename.contains(".mpeg")
                || filename.contains(".mpe") || filename.contains(".mp4") || filename.contains(".avi")
                || filename.contains(".webm")) {
            mFiletype = getResources().getString(R.string.video);
        } else {
            mFiletype = getResources().getString(R.string.unknown);
        }
        return mFiletype;
    }


    @Override
    public void onShareButtonClicked() {
        if(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase()) != null)
            mShareConfig.shareWith(getContext(), Uri.parse(mViewPagerURL));
        else
            Toast.makeText(getActivity(), "Invalid File Url.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOpenWithButtonClicked() {
        if(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase()) != null)
            mOpenWithConfig.openWith(getContext(), Uri.parse(mViewPagerURL));
        else
            Toast.makeText(getActivity(), "Invalid File Url.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDownloadButtonClicked() {


//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//        startActivityForResult(intent, 42);

        if(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase()) != null) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, 42);
        }
        else
            Toast.makeText(getActivity(), "Invalid File Url.", Toast.LENGTH_SHORT).show();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 42) {
                if (null != data) {
                    mDirpath = data.getData();
                    mDownloadConfig.startDownload(getContext(), mViewPagerURL, mDirpath);
                }
            }
        }
    }


}

