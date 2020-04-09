package com.alcodes.alcodessmgalleryviewer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrTouchImageView;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewImageBinding;
import com.alcodes.alcodessmgalleryviewer.gsonmodels.AsmGvrMediaConfigModel;
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import timber.log.Timber;

public class AsmGvrPreviewImageFragment extends Fragment {

    //private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    //private static final String ARG_STRING_FILE_URI = "ARG_STRING_FILE_URI";
    private static final String ARG_JSON_STRING_MEDIACONFIG_MODEL = "ARG_JSON_STRING_MEDIACONFIG_MODEL";

    private NavController mNavController;
    private AsmGvrFragmentPreviewImageBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrMediaConfigModel mMediaConfig;
    private int mViewPagerPosition;

    public AsmGvrPreviewImageFragment() {
    }

    public static AsmGvrPreviewImageFragment newInstance(AsmGvrMediaConfig mediaConfig) {
        Bundle args = new Bundle();
        //args.putString(ARG_STRING_FILE_URI, mediaConfig.getUri());
        //args.putInt(ARG_INT_PAGER_POSITION, mediaConfig.getPosition());

        AsmGvrMediaConfigModel mediaConfigModel = new AsmGvrMediaConfigModel();
        mediaConfigModel.position = mediaConfig.getPosition();
        mediaConfigModel.fileType = mediaConfig.getFileType();
        mediaConfigModel.fromInternetSource = mediaConfig.getFromInternetSource();
        mediaConfigModel.uri = mediaConfig.getUri();

        args.putString(ARG_JSON_STRING_MEDIACONFIG_MODEL, new Gson().toJson(mediaConfigModel));

        AsmGvrPreviewImageFragment fragment = new AsmGvrPreviewImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewImageBinding.inflate(inflater, container, false);

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
        //mMediaConfig = new AsmGvrMediaConfig();
        //mMediaConfig.setUri(requireArguments().getString(ARG_STRING_FILE_URI));
        //mMediaConfig.setPosition(requireArguments().getInt(ARG_INT_PAGER_POSITION));

        mMediaConfig = new GsonBuilder().create().fromJson(requireArguments().getString(ARG_JSON_STRING_MEDIACONFIG_MODEL), AsmGvrMediaConfigModel.class);

        mViewPagerPosition = mMediaConfig.position;

        // Init image view
        mDataBinding.touchImageViewPreviewImage.initImageView(getContext(),Uri.parse(mMediaConfig.uri));

        // Init view model.
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        mMainSharedViewModel.getViewPagerPositionLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    if (integer == mViewPagerPosition) {
                        // TODO this page has been selected.
                        //Before entering this fragment, Reset the scale and position it at Center
                        mDataBinding.touchImageViewPreviewImage.resetIamgeToCenter();
                    } else {
                        // TODO this page has been de-selected.
                    }
                }
            }
        });

        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                Timber.e("CHECK: " + internetStatusData.statusMessage);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //Timber.e("d;;Child fragment at: %s entering onResume", mViewPagerPosition);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Timber.e("d;;Child fragment at: %s entering onPause", mViewPagerPosition);
    }
}
