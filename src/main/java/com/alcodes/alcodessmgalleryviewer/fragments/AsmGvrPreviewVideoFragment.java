package com.alcodes.alcodessmgalleryviewer.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;

import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewVideoBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class AsmGvrPreviewVideoFragment extends Fragment {
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource";
    private static final String ARG_MEDIA_CONFIG = "ARG_MEDIA_CONFIG";

    private NavController mNavController;
    private AsmGvrFragmentPreviewVideoBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;
    private AnimationDrawable mAnimationDrawable;
    private Uri mViewPagerUri;

    public AsmGvrPreviewVideoFragment() {
    }

    public static AsmGvrPreviewVideoFragment newInstance(AsmGvrMediaConfig mMediaConfig) {
        Bundle args = new Bundle();

        args.putInt("ARG_INT_PAGER_POSITION", mMediaConfig.getPosition());
        args.putString("ARG_STRING_PAGER_FILEURL", mMediaConfig.getUri());
        args.putBoolean("ARG_String_IsInternetSource", mMediaConfig.getFromInternetSource());

        AsmGvrPreviewVideoFragment fragment = new AsmGvrPreviewVideoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewVideoBinding.inflate(inflater, container, false);

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
        mViewPagerUri = Uri.parse(requireArguments().getString(ARG_String_FILEURL));

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
                        Timber.e("d;;Video fragment: page has been selected at: %s", mViewPagerPosition);
                        if(mMainSharedViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1){
                            mDataBinding.previewVideoView.seekTo(mMainSharedViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                        }
                    } else {
                        Timber.e("d;;Video fragment: page has been de-selected at: %s", mViewPagerPosition);
                        if(mDataBinding.previewVideoView.isPlaying()){
                            mMainSharedViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                        }
                    }
                }
            }
        });

        startVideoPlayer(mViewPagerUri);

    }

    public Boolean startVideoPlayer(Uri uri){
        Boolean noErrorFlag = true;
        String fileType = "";
        // Initialize VideoView with custom play & pause listener
        mDataBinding.previewVideoView.setForeground(null);
        mDataBinding.previewVideoView.setForeground(requireActivity().getDrawable(R.drawable.asm_gvr_loading_animation));
        mDataBinding.previewVideoView.setForegroundGravity(Gravity.CENTER);
        if(mAnimationDrawable == null){
            mAnimationDrawable = (AnimationDrawable) mDataBinding.previewVideoView.getForeground();
        }
        mAnimationDrawable.start();
        // Initialize VideoView with custom play & pause listener

        //Assigning URI to Video View and Anchoring Media Controller to Video View
        if(uri != null){
            try{
                fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase());
                fileType = fileType.substring(0, fileType.lastIndexOf("/"));
            } catch (Exception e) {
                e.printStackTrace();
                noErrorFlag = false;
            }
            if(noErrorFlag){
                if(fileType.equals("video")) {
                    MediaController mMediaController = new MediaController(requireActivity());
                    mMediaController.setAnchorView(mDataBinding.previewVideoView);
                    mDataBinding.previewVideoView.setMediaController(mMediaController);
                    mDataBinding.previewVideoView.setVideoURI(uri);
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
        //Assigning URI to Video View and Anchoring Media Controller to Video View

        //Setting Listener for Video View on preapred, finish, play and pause
        mDataBinding.previewVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mAnimationDrawable.stop();
                mDataBinding.previewVideoView.setForeground(null);
                if(mMainSharedViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1){
                    mDataBinding.previewVideoView.seekTo(mMainSharedViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                }
            }
        });
        mDataBinding.previewVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAnimationDrawable.stop();
                mDataBinding.previewVideoView.setForeground(null);
            }
        });

        return true;
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
        if(mDataBinding.previewVideoView.isPlaying()){
            mMainSharedViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
        }
    }

}
