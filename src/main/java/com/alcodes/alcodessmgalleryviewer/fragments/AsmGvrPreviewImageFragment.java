package com.alcodes.alcodessmgalleryviewer.fragments;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewImageBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.AsmGvrImageCallback;
import com.alcodes.alcodessmgalleryviewer.gsonmodels.AsmGvrMediaConfigModel;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrDownloadConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrShareConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrTouchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.IOException;

public class AsmGvrPreviewImageFragment extends Fragment implements AsmGvrImageCallback {
    private static final String ARG_JSON_STRING_MEDIACONFIG_MODEL = "ARG_JSON_STRING_MEDIACONFIG_MODEL";
    private static final String OUTSTATE_IMAGE_DETAIL_IS_SHOWN = "OUTSTATE_IMAGE_DETAIL_IS_SHOWN";
    private static final int OPEN_DIRECTORY_REQUEST_CODE = 50;

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
        mediaConfigModel.fileName = mediaConfig.getFileName();

        args.putString(ARG_JSON_STRING_MEDIACONFIG_MODEL, new Gson().toJson(mediaConfigModel));

        AsmGvrPreviewImageFragment fragment = new AsmGvrPreviewImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewImageBinding.inflate(inflater, container, false);

        mActionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();

        setHasOptionsMenu(true);

        return mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.asm_gvr_image_menu, menu);
        boolean setVisible;

        if(mMediaConfig.fromInternetSource){
            setVisible = true;
        }else{
            setVisible = false;
        }

        menu.findItem(R.id.menu_item_open_image_on_browser).setVisible(setVisible);
        menu.findItem(R.id.menu_save_image).setVisible(setVisible);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Any rotation happens will close/open the detail panel when its original is close/open state
        outState.putBoolean(OUTSTATE_IMAGE_DETAIL_IS_SHOWN, mDataBinding.touchImageViewPreviewImage.getIsDetailShown());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.menu_item_open_image_on_browser) {
            // Open image on Browser is Pressed
            openImageOnWebBrowser();
        }else if(itemId == R.id.menu_item_set_as_wallpaper) {
            //Set image as wallpaper is Pressed
            setImageAsWallPaper();
        }else if(itemId == R.id.menu_save_image){
            //Open SAF to select directory and save the images
            saveImageIntoDevice();
        }else if(itemId == R.id.menu_share_image){
            //Share images
            shareImageToOthers();
        }else if(itemId == R.id.menu_details){
            //Show Details
            onSlideImageDetailUp(mDataBinding.touchImageViewPreviewImage);
        }

        return super.onOptionsItemSelected(item);
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

        //Init Saved instance State
        if(savedInstanceState != null){
            if(savedInstanceState.getBoolean(OUTSTATE_IMAGE_DETAIL_IS_SHOWN)){
                //Image Detail is Shown, then after rotate should be shown
                mDataBinding.touchImageViewPreviewImage.setIsDetailShown(savedInstanceState.getBoolean(OUTSTATE_IMAGE_DETAIL_IS_SHOWN));
                mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.VISIBLE);
            }else{
                //Image Detail is not Shown, then after rotate should not be shown
                mDataBinding.touchImageViewPreviewImage.setIsDetailShown(savedInstanceState.getBoolean(OUTSTATE_IMAGE_DETAIL_IS_SHOWN));
                mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.INVISIBLE);
            }
        }

        mMainSharedViewModel.getViewPagerPositionLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    if (integer == mViewPagerPosition) {
                    } else {
                        //Before leaving this fragment, Reset the scale and position it at Center
                        mDataBinding.touchImageViewPreviewImage.resetIamgeToCenter();
                    }
                }
            }
        });
        //get selected color
        mMainSharedViewModel.getColorSelectedLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if(integer!=null){
                    mDataBinding.touchImageViewPreviewImage.setBackgroundColor(ContextCompat.getColor(getActivity(),  integer));

                }
            }
        });

        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                // Init image
                mDataBinding.touchImageViewPreviewImage.initImageView(getContext(),
                        Uri.parse(mMediaConfig.uri), internetStatusData.internetStatus,
                        AsmGvrPreviewImageFragment.this);
            }
        });

        //Init View
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == OPEN_DIRECTORY_REQUEST_CODE){
                if(data != null){
                    new AsmGvrDownloadConfig().startDownload(requireContext(), mMediaConfig.uri, data.getData());
                }
            }
        }
    }

    @Override
    public void onTouchShowHideActionBar() {
        if(mActionBar.isShowing()){
            mActionBar.hide();
        }else{
            mActionBar.show();
        }
    }

    @Override
    public void onSlideImageDetailUp(AsmGvrTouchImageView imageView) {
        mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.VISIBLE);
        TranslateAnimation animation = new TranslateAnimation(
                0,                 // fromXDelta
                0,                   // toXDelta
                mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.getHeight(),            // fromYDelta
                0);// toYDelta
        animation.setDuration(500);
        animation.setFillAfter(true);
        mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setAnimation(animation);

        imageView.setIsDetailShown(true);
    }

    @Override
    public void onSlideImageDetailDown(AsmGvrTouchImageView imageView) {
        mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.INVISIBLE);
        TranslateAnimation animation = new TranslateAnimation(
                0,                 // fromXDelta
                0,                   // toXDelta
                0,            // fromYDelta
                mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.getHeight()); // toYDelta
        animation.setDuration(500);
        animation.setFillAfter(true);
        mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setAnimation(animation);

        imageView.setIsDetailShown(false);
    }

    private void initView(){
        String fileLocation;
        String fileName = mMediaConfig.fileName;
        String fileType;

        try{
            if(mMediaConfig.fromInternetSource){
                fileLocation = mMediaConfig.uri;
                fileType = mDataBinding.touchImageViewPreviewImage.getImageFileExtensionURL(Uri.parse(mMediaConfig.uri));
            }else{
                fileLocation = Uri.parse(mMediaConfig.uri).getPath();
                fileType = mDataBinding.touchImageViewPreviewImage.getImageFileExtensionURI(Uri.parse(mMediaConfig.uri));
            }

            mDataBinding.includedPanelFileDetails.relativelayoutLocation.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.relativelayoutName.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.relativelayoutFileType.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewFileLocation.setText(fileLocation);
            mDataBinding.includedPanelFileDetails.textViewFileName.setText(fileName);
            mDataBinding.includedPanelFileDetails.textViewFileType.setText(mMediaConfig.fileType+"/"+fileType);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void openImageOnWebBrowser(){
        //Open Browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mMediaConfig.uri));
        requireActivity().startActivity(browserIntent);
    }

    private void setImageAsWallPaper(){
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(requireContext());

        //To get bitmap and set it as wallpaper with Wallpaper Manager
        Glide.with(requireContext()).asBitmap().load(Uri.parse(mMediaConfig.uri)).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                try {
                    //Set Wallpaper
                    wallpaperManager.setBitmap(resource);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }).submit();
    }

    private void saveImageIntoDevice(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, OPEN_DIRECTORY_REQUEST_CODE);
    }

    private void shareImageToOthers(){
        new AsmGvrShareConfig().shareWith(requireContext(), Uri.parse(mMediaConfig.uri));
    }
}
