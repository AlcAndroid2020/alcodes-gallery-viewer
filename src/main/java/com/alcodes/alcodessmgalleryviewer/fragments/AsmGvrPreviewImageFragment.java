package com.alcodes.alcodessmgalleryviewer.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewImageBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.AsmGvrImageCallback;
import com.alcodes.alcodessmgalleryviewer.gsonmodels.AsmGvrMediaConfigModel;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import timber.log.Timber;

public class AsmGvrPreviewImageFragment extends Fragment implements AsmGvrImageCallback {
    private static final String ARG_JSON_STRING_MEDIACONFIG_MODEL = "ARG_JSON_STRING_MEDIACONFIG_MODEL";

    private NavController mNavController;
    private AsmGvrFragmentPreviewImageBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrMediaConfigModel mMediaConfig;
    private int mViewPagerPosition;
    private ActionBar mActionBar;

    public AsmGvrPreviewImageFragment() {
    }

    public static AsmGvrPreviewImageFragment newInstance(AsmGvrMediaConfig mediaConfig) {
        Bundle args = new Bundle();

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

        mActionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();

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
        mMediaConfig = new GsonBuilder().create().fromJson(requireArguments().getString(ARG_JSON_STRING_MEDIACONFIG_MODEL), AsmGvrMediaConfigModel.class);

        mViewPagerPosition = mMediaConfig.position;

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
                    } else {
                        //Before leaving this fragment, Reset the scale and position it at Center
                        mDataBinding.touchImageViewPreviewImage.resetIamgeToCenter();
                        // TODO this page has been de-selected.
                    }
                }
            }
        });

        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                // Init image
                mDataBinding.touchImageViewPreviewImage.initImageView(getContext(),Uri.parse(mMediaConfig.uri),internetStatusData.internetStatus, AsmGvrPreviewImageFragment.this );;
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

    @Override
    public void onTouchShowHideActionBar() {
        if(mActionBar.isShowing()){
            mActionBar.hide();
        }else{
            mActionBar.show();
        }
    }
}
