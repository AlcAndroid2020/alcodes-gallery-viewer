package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;
import android.widget.Toast;

import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.AsmGvrImageCallback;
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewVideoBinding;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrStateBroadcastingVideoView;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrStateBroadcastingVideoViewModel;
import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class AsmGvrPreviewVideoFragment extends Fragment{
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource";

    private NavController mNavController;
    private AsmGvrFragmentPreviewVideoBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrStateBroadcastingVideoViewModel mStateBroadcastingVideoViewModel;
    private int mViewPagerPosition;
    private Uri mViewPagerUri;
    private ActionBar mActionBar;
    private MediaController mMediaController;

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

        mActionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();
        return mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
    }

    @SuppressLint("ClickableViewAccessibility")
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

        mStateBroadcastingVideoViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrStateBroadcastingVideoViewModel.class);
        // Init view model.

        //Observed Internet Status, if internet not present video is not played and no internet img will be shown (For URL only for now)
        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {

            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                if(internetStatusData.internetStatus){
                    startVideoPlayer(mViewPagerUri);
                }else{
                    if(mDataBinding.previewVideoView.isPlaying()){
                        mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                    }
                    mDataBinding.previewVideoView.setVisibility(View.GONE);
                    mDataBinding.previewVideoImageLoading.setZ(1);
                    mDataBinding.previewVideoView.setZ(0);
                    mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                    Glide.with(requireActivity())
                            .load(R.drawable.asm_gvr_no_wifi)
                            .into(mDataBinding.previewVideoImageLoading);
                }
            }
        });
        //Observed Internet Status, if internet not present video is not played and no internet img will be shown (For URL only for now)

        //Observed page selected and check if played history is present, if present then resume video
        mMainSharedViewModel.getViewPagerPositionLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    if (integer == mViewPagerPosition) {
                        if(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1){
                            mDataBinding.previewVideoView.seekTo(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                        }
                    } else {
                        if(mDataBinding.previewVideoView.isPlaying()){
                            mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                        }
                    }
                }
            }
        });
        //Observed page selected and check if played history is present, if present then resume video

        mDataBinding.previewVideoRoot.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if(mActionBar.isShowing()){
                        mActionBar.hide();
                    }else{
                        mActionBar.show();
                    }
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return super.onSingleTapUp(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }

        });

        startVideoPlayer(mViewPagerUri);
    }

    private Boolean startVideoPlayer(Uri uri){
        Boolean noErrorFlag = true;
        String fileType = "";

        mMediaController = new MediaController(requireActivity());
        // Initialize VideoView with loading bar when video is loading for playing
        mDataBinding.previewVideoView.setZ(0);
        mDataBinding.previewVideoImageLoading.setZ(1);
        mDataBinding.previewVideoView.setVisibility(View.VISIBLE);
        Glide.with(this)
                .asGif()
                .load(R.raw.loading)
                .into(mDataBinding.previewVideoImageLoading);
        // Initialize VideoView with loading bar when video is loading for playing

        //Assigning URI to Video View
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
                    mDataBinding.previewVideoView.setMediaController(mMediaController);
                    mMediaController.setAnchorView(mDataBinding.previewVideoView);
                    mDataBinding.previewVideoView.setVideoURI(uri);
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
        //Assigning URI to Video View

        //Setting Listener for Video View on prepared, finish, play and pause
        mDataBinding.previewVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                //Set video playing visible, set video info image view invisible
                mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                mDataBinding.previewVideoView.setVisibility(View.VISIBLE);

                //Set video playing visible, set video info image view invisible

                //Start video and check if there is records video playing, resume the video
                mp.start();
                if(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1){
                    mDataBinding.previewVideoView.seekTo(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                }
                //Start video and check if there is records video playing, resume the video
            }
        });

        mDataBinding.previewVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    //Hide video loading/buffering img
                    case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                        mDataBinding.previewVideoImageLoading.setZ(0);
                        mDataBinding.previewVideoView.setZ(1);
                        mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                        return true;
                    }
                    //Hide video loading/buffering img
                    //Show video loading/buffering img
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                        mDataBinding.previewVideoImageLoading.setZ(1);
                        mDataBinding.previewVideoView.setZ(0);
                        mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                        Glide.with(requireActivity())
                                .asGif()
                                .load(R.raw.loading)
                                .into(mDataBinding.previewVideoImageLoading);
                        return true;
                    }
                    //Show video loading/buffering img
                }
                return false;
            }
        });

        mDataBinding.previewVideoView.setPlayPauseListener(new AsmGvrStateBroadcastingVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {
                //Add Play img to show video image aside from media controller
                mDataBinding.previewVideoImageLoading.setZ(1);
                mDataBinding.previewVideoView.setZ(0);
                mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                Glide.with(requireActivity())
                        .load(R.drawable.play)
                        .into(mDataBinding.previewVideoImageLoading);
                //Add Play img to show video image aside from media controller
                //Check if video is playing to not accidentally hide image view, then delay 1.5 seconds to remove play img for video viewing experience
                if(mDataBinding.previewVideoView.isPlaying()){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDataBinding.previewVideoImageLoading.setZ(0);
                            mDataBinding.previewVideoView.setZ(1);
                            mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                        }
                    }, 1500);
                }
                //Check if video is playing to not accidentally hide image view, then delay 1.5 seconds to remove play img for video viewing experience
            }

            @Override
            public void onPause() {
                //Add Pause img to show video image aside from media controller
                mDataBinding.previewVideoImageLoading.setZ(1);
                mDataBinding.previewVideoView.setZ(0);
                mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                Glide.with(requireActivity())
                        .load(R.drawable.pause)
                        .into(mDataBinding.previewVideoImageLoading);
                //Add Pause img to show video image aside from media controller
            }
        });

        mDataBinding.previewVideoImageLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Only play video because the image view will not be present because of Z-Index hiding, so no use to pause
                mDataBinding.previewVideoView.start();
                //Only play video because the image view will not be present because of Z-Index hiding, so no use to pause
            }
        });

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        //Store in ViewModel the video playing position
        if(mDataBinding.previewVideoView.isPlaying()){
            mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
        }
        //Store in ViewModel the video playing position
    }
}
