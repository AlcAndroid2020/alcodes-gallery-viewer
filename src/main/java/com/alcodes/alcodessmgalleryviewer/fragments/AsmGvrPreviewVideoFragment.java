package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewVideoBinding;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrDownloadConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrFileDetailsHelper;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrOpenWithConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrShareConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrStateBroadcastingVideoViewModel;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrCircularProgressBar;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrStateBroadcastingVideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class AsmGvrPreviewVideoFragment extends Fragment{
    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_STRING_FILE_PATH = "ARG_STRING_FILE_PATH";
    private static final String ARG_STRING_IS_INTERNET_SOURCE = "ARG_STRING_IS_INTERNET_SOURCE";
    private static final String ARG_STRING_FILE_TYPE = "ARG_STRING_FILE_TYPE";
    private static final String ARG_STRING_FILE_NAME = "ARG_STRING_FILE_NAME";
    private static final String OUTSTATE_VIDEO_DETAIL_IS_SHOWN = "OUTSTATE_VIDEO_DETAIL_IS_SHOWN";
    private static final int CHOOSE_DOWNLOAD_FOLDER_REQUEST_CODE = 41;
    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_MAX_OFF_PATH = 120;

    private NavController mNavController;
    private AsmGvrFragmentPreviewVideoBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrStateBroadcastingVideoViewModel mStateBroadcastingVideoViewModel;
    private int mViewPagerPosition;
    private Boolean mIsInternetSource;
    private String mFileType;
    private String mFileName;
    private Uri mViewPagerUri;
    private ActionBar mActionBar;
    private MediaController mMediaController;
    private HttpProxyCacheServer mHttpProxyCacheServer;
    private String mProxyURL = "";
    private AsmGvrDownloadConfig mDownloadConfig;
    private AsmGvrShareConfig mShareConfig;
    private AsmGvrOpenWithConfig mOpenWithConfig;
    private FFmpegMediaMetadataRetriever mFFmpegMMR;
    private Boolean videoErrorFlag = false;
    private Boolean isDetailsShowing = false;
    private AsmGvrCircularProgressBar mCircularProgressBar;
    private Uri mVideoUri;
    private AsmGvrFileDetailsHelper mFileDetailsHelper;

    public AsmGvrPreviewVideoFragment() {
    }

    public static AsmGvrPreviewVideoFragment newInstance(AsmGvrMediaConfig mMediaConfig) {
        Bundle args = new Bundle();

        args.putInt("ARG_INT_PAGER_POSITION", mMediaConfig.getPosition());
        args.putString("ARG_STRING_FILE_PATH", mMediaConfig.getUri());
        args.putBoolean("ARG_STRING_IS_INTERNET_SOURCE", mMediaConfig.getFromInternetSource());
        args.putString("ARG_STRING_FILE_TYPE", mMediaConfig.getFileType());
        args.putString("ARG_STRING_FILE_NAME", mMediaConfig.getFileName());

        AsmGvrPreviewVideoFragment fragment = new AsmGvrPreviewVideoFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewVideoBinding.inflate(inflater, container, false);

        mActionBar = ((AppCompatActivity)requireActivity()).getSupportActionBar();

        setHasOptionsMenu(true);
        return mDataBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.asm_gvr_video_fragment_menu, menu);
        if(!mIsInternetSource){
            menu.getItem(2).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Refresh Video
        if(item.getItemId() == R.id.video_fragment_menu_refresh){
            if(mDataBinding.previewVideoView.isPlaying()){
                mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
            }else if(!mDataBinding.previewVideoView.isPlaying() & mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition >= -1){
                mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
            }
            startVideoPlayer(mViewPagerUri);
        }
        //Refresh Video
        //Share Video
        else if (item.getItemId() == R.id.video_fragment_menu_share) {
            mShareConfig.shareWith(requireActivity(), mViewPagerUri);
        }//Share Video
        //Download video to selected directory
        else if(item.getItemId() == R.id.video_fragment_menu_download){
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, CHOOSE_DOWNLOAD_FOLDER_REQUEST_CODE);
        }//Download video to selected directory
        //Open video with other application
        else if(item.getItemId() == R.id.video_fragment_menu_open_with){
            mOpenWithConfig.openWith(requireActivity(), mViewPagerUri);
        }
        //Open video with other application
        else if(item.getItemId() == R.id.video_fragment_menu_details){
            if(!isDetailsShowing){
                initSlideVideoDetails(true);
                isDetailsShowing = true;
            }else{
                initSlideVideoDetails(false);
                isDetailsShowing = false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("IsDetailShown", isDetailsShowing);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.getBoolean("IsDetailShown")) {
                initSlideVideoDetails(true);
                isDetailsShowing = true;
            }
        }

        // Extract arguments.
        mViewPagerPosition = requireArguments().getInt(ARG_INT_PAGER_POSITION);
        mViewPagerUri = Uri.parse(requireArguments().getString(ARG_STRING_FILE_PATH));
        mIsInternetSource = requireArguments().getBoolean(ARG_STRING_IS_INTERNET_SOURCE);
        mFileType = requireArguments().getString(ARG_STRING_FILE_TYPE);
        mFileName = requireArguments().getString(ARG_STRING_FILE_NAME);
        // Extract arguments.

        // Init Internet Status & Video Caching Notifier
        mDataBinding.previewVideoNotifierRoot.setZ(3);
        mDataBinding.previewVideoNoInternet.setZ(3);
        mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setZ(3);
        Glide.with(requireActivity())
                .load(R.drawable.asm_gvr_no_wifi)
                .into(mDataBinding.previewVideoNoInternet);
        mDataBinding.previewVideoCache.setZ(3);
        Glide.with(requireActivity())
                .load(R.drawable.asm_gvr_save)
                .into(mDataBinding.previewVideoCache);
        if(mIsInternetSource){
            mDataBinding.previewVideoCache.setVisibility(View.GONE);
        }
        // Init Internet Status & Video Caching Notifier

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

        // Init HttpProxyCacheServer for VideoView & Circular Progress Bar, Action Bar Menu Features, FFmpegMediaMetaDataReceiver & MediaMetaDataReceiver, and FileDetailsHelper Initialization
        if(mStateBroadcastingVideoViewModel.getHttpProxyCacheServer() == null){
            mStateBroadcastingVideoViewModel.initHttpProxyCacheServer(requireActivity());
        }
        mHttpProxyCacheServer = mStateBroadcastingVideoViewModel.getHttpProxyCacheServer();

        if(mCircularProgressBar == null){
            if(mStateBroadcastingVideoViewModel.getCircularProgressBar() == null){
                mStateBroadcastingVideoViewModel.setCircularProgressBar(requireActivity());
                mCircularProgressBar = mStateBroadcastingVideoViewModel.getCircularProgressBar();
            }else{
                mCircularProgressBar = mStateBroadcastingVideoViewModel.getCircularProgressBar();
            }
        }
        if(mDownloadConfig == null){
            if(mStateBroadcastingVideoViewModel.getDownloadConfig() == null){
                mStateBroadcastingVideoViewModel.setDownloadConfig();
                mDownloadConfig = mStateBroadcastingVideoViewModel.getDownloadConfig();
            }else{
                mDownloadConfig = mStateBroadcastingVideoViewModel.getDownloadConfig();
            }
        }
        if(mShareConfig == null){
            if(mStateBroadcastingVideoViewModel.getShareConfig() == null){
                mStateBroadcastingVideoViewModel.setShareConfig();
                mShareConfig = mStateBroadcastingVideoViewModel.getShareConfig();
            }else{
                mShareConfig = mStateBroadcastingVideoViewModel.getShareConfig();
            }
        }
        if(mOpenWithConfig == null){
            if(mStateBroadcastingVideoViewModel.getOpenWithConfig() == null){
                mStateBroadcastingVideoViewModel.setOpenWithConfig();
                mOpenWithConfig = mStateBroadcastingVideoViewModel.getOpenWithConfig();
            }else{
                mOpenWithConfig = mStateBroadcastingVideoViewModel.getOpenWithConfig();
            }
        }
        if(mFFmpegMMR == null){
            if(mStateBroadcastingVideoViewModel.getFFmpegMMR() == null){
                mStateBroadcastingVideoViewModel.setFFmpegMMR();
                mFFmpegMMR = mStateBroadcastingVideoViewModel.getFFmpegMMR();
            }else{
                mFFmpegMMR = mStateBroadcastingVideoViewModel.getFFmpegMMR();
            }
        }
        if(mFileDetailsHelper == null){
            if(mStateBroadcastingVideoViewModel.getFileDetailsHelper() == null){
                mStateBroadcastingVideoViewModel.setFileDetailsHelper();
                mFileDetailsHelper = mStateBroadcastingVideoViewModel.getFileDetailsHelper();
            }else{
                mFileDetailsHelper = mStateBroadcastingVideoViewModel.getFileDetailsHelper();
            }
        }


        // Init HttpProxyCacheServer for VideoView & Circular Progress Bar, Action Bar Menu Features, FFmpegMediaMetaDataReceiver & MediaMetaDataReceiver, and FileDetailsHelper Initialization

        //Observed Internet Status, if internet not present video is not played and no internet img will be shown (For URL only for now)
        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                if(internetStatusData.internetStatus){
                    mDataBinding.previewVideoNoInternet.setVisibility(View.INVISIBLE);
//                    mDataBinding.previewVideoView.start();
                    if(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1){
                        mDataBinding.previewVideoView.seekTo(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                    }
                    videoErrorFlag = false;
                    startVideoPlayer(mViewPagerUri);
                }else{
                    if(mDataBinding.previewVideoView.isPlaying()){
                        mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                    }
                    else if(!mDataBinding.previewVideoView.isPlaying() & mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition >= -1){
                        mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                    }
                    mDataBinding.previewVideoNoInternet.setVisibility(View.VISIBLE);
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
                        else if(!mDataBinding.previewVideoView.isPlaying() & mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition >= -1){
                            mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
                        }
                    }
                }
            }
        });
        //Observed page selected and check if played history is present, if present then resume video

        // Hide and show menu bar & notifiers using double tap gesture
        mDataBinding.previewVideoRoot.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if(mActionBar.isShowing()){
                        mActionBar.hide();
                        mDataBinding.previewVideoNotifierRoot.setVisibility(View.INVISIBLE);
                    }else{
                        mActionBar.show();
                        mDataBinding.previewVideoNotifierRoot.setVisibility(View.VISIBLE);
                    }
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return super.onSingleTapUp(e);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH){
                        //Swipe Left or Right will not take any action
                        return false;
                    }

                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                        // Swipe Up
                        if(!isDetailsShowing){
                            initSlideVideoDetails(true);
                            isDetailsShowing = true;

                        }
                        mDataBinding.previewVideoView.setMediaController(null);
                    }else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                        // Swipe Down
                        mDataBinding.previewVideoView.setMediaController(mMediaController);
                        initSlideVideoDetails(false);
                        isDetailsShowing = false;
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                if(isDetailsShowing)
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                else
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }

        });

        // Hide and show menu bar & notifiers using double tap gesture

        startVideoPlayer(mViewPagerUri);
        initVideoDetails(mVideoUri);
    }

    private void initVideoDetails(Uri uri){
        try {
            //Set Data Source for FFmpeg to get datasource for details displaying
            if (mIsInternetSource) {
                mFFmpegMMR.setDataSource(uri.toString());
                //Extract metadata of file size
                mDataBinding.includedPanelFileDetails.relativelayoutFileSize.setVisibility(View.VISIBLE);
                mDataBinding.includedPanelFileDetails.textViewFileSize.setText(String.format("File Size: %s",
                        mFileDetailsHelper.fileSizeBytesConverter(Long.parseLong(mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE)), "MB")));
                //Extract metadata of file size
            } else {
                mFFmpegMMR.setDataSource(requireActivity(), uri);
                //Extract metadata of file size
                DocumentFile documentFile = DocumentFile.fromSingleUri(requireActivity(), uri);
                mDataBinding.includedPanelFileDetails.relativelayoutFileSize.setVisibility(View.VISIBLE);
                mDataBinding.includedPanelFileDetails.textViewFileSize.setText(String.format("File Size: %s", mFileDetailsHelper.fileSizeBytesConverter(documentFile.length(), "MB")));
                //Extract metadata of file size
            }
            //Set Data Source for FFmpeg to get datasource for details displaying

            //Extract Creation Date if available
            if (mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME) != null) {
                mDataBinding.includedPanelFileDetails.relativelayoutDateRoot.setVisibility(View.VISIBLE);
                mDataBinding.includedPanelFileDetails.textViewDate.setText(String.format("Date Created: %s", mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME)));
            } else {
                mDataBinding.includedPanelFileDetails.relativelayoutDateRoot.setVisibility(View.GONE);
            }
            //Extract Creation Date if available

            //Display URL/URI & filename for details displaying
            mDataBinding.includedPanelFileDetails.relativelayoutLocation.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.relativelayoutName.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewFileLocation.setText(String.format("Path: %s", uri.toString()));
            mDataBinding.includedPanelFileDetails.textViewFileName.setText(String.format("Name: %s", mFileName));
            //Display URL/URI & filename for details displaying

            //Extract video duration for details displaying
            mDataBinding.includedPanelFileDetails.relativelayoutDuration.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewDuration.setText(String.format("Duration: %s",
                    mFileDetailsHelper.createTimeLabel(Integer.parseInt(mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)))));
            //Extract video duration for details displaying

            //Extract Video Resolution for details displaying
            mDataBinding.includedPanelFileDetails.relativelayoutResolution.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewRes.setText(String.format("Resolution: %sX%s",
                    mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
                    mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
            //Extract Video Resolution for details displaying

            //Extract Video & Audio Codec for details displaying
            mDataBinding.includedPanelFileDetails.relativelayoutVideoCodec.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.relativelayoutAudioCodec.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewVideoCodec.setText(String.format("Video Codec: %s", mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC)));
            mDataBinding.includedPanelFileDetails.textViewAudioCodec.setText(String.format("Audio Codec: %s", mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_AUDIO_CODEC)));
            //Extract Video & Audio Codec for details displaying

            //Extract Video Frame Rate for details displaying
            mDataBinding.includedPanelFileDetails.relativelayoutFrameRate.setVisibility(View.VISIBLE);
            mDataBinding.includedPanelFileDetails.textViewFrameRate.setText(String.format("Frame per second: %s FPS", mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FRAMERATE)));
            //Extract Video Frame Rate for details displaying
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startVideoPlayer(Uri uri){
        mMediaController = new MediaController(requireActivity());
        // Initialize VideoView with loading bar when video is loading for playing
        mDataBinding.previewVideoView.setZ(0);
        mDataBinding.previewVideoView.setVisibility(View.VISIBLE);
        Glide.with(requireActivity())
                .asGif()
                .load(mCircularProgressBar)
                .into(mDataBinding.previewVideoImageLoading);
        // Initialize VideoView with loading bar when video is loading for playing

        //Assigning URI to Video View
        if(uri != null) {
            if (mFileType.equals("video")) {
                mDataBinding.previewVideoView.setMediaController(mMediaController);
                mMediaController.setAnchorView(mDataBinding.previewVideoView);
                if (mIsInternetSource) {
                    mProxyURL = mHttpProxyCacheServer.getProxyUrl(uri.toString(),true);
                    mDataBinding.previewVideoView.setVideoURI(Uri.parse(mProxyURL));

                    if(!mMainSharedViewModel.getInternetStatusDataLiveData().getValue().internetStatus && mIsInternetSource){
                        Glide.with(requireActivity())
                                .load(R.drawable.asm_gvr_unable_load)
                                .apply(new RequestOptions().override(256,256))
                                .centerInside()
                                .into(mDataBinding.previewVideoImageLoading);
                        mCircularProgressBar.stop();
                        videoErrorFlag = true;
                    }else{
                        videoErrorFlag = false;
                        mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                        mCircularProgressBar.stop();
                        if(mVideoUri == null){
                            mVideoUri = Uri.parse(mProxyURL);
                        }else{
                            if(mVideoUri != Uri.parse(mProxyURL)){
                                mVideoUri = Uri.parse(mProxyURL);
                            }
                        }
                    }
                } else {
                    mDataBinding.previewVideoView.setVideoURI(uri);
                    if(mVideoUri == null){
                        mVideoUri = uri;
                    }else{
                        if(mVideoUri != uri){
                            mVideoUri = uri;
                        }
                    }
                }
            }
            //Assigning URI to Video View

            //Setting Listener for Video View on prepared, finish, asm_gvr_play and pause
            if(!videoErrorFlag){
                mDataBinding.previewVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        //Set video playing visible, set video info image view invisible
                        mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                        mCircularProgressBar.stop();
                        mDataBinding.previewVideoView.setVisibility(View.VISIBLE);
                        //Set video playing visible, set video info image view invisible

                        //Start video and check if there is records video playing, resume the video
                        if (mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1) {
                            mDataBinding.previewVideoView.seekTo(mStateBroadcastingVideoViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                        }
                        //Start video and check if there is records video playing, resume the video
                        if (mIsInternetSource) {
                            if (mHttpProxyCacheServer.isCached(mViewPagerUri.toString())) {
                                mDataBinding.previewVideoCache.setVisibility(View.VISIBLE);
                            } else {
                                mDataBinding.previewVideoCache.setVisibility(View.GONE);
                            }
                        }
                    }
                });

                // Set On Complete video show video cache status
                mDataBinding.previewVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (mIsInternetSource) {
                            if (mHttpProxyCacheServer.isCached(mViewPagerUri.toString())) {
                                mDataBinding.previewVideoCache.setVisibility(View.VISIBLE);
                            } else {
                                mDataBinding.previewVideoCache.setVisibility(View.GONE);
                            }
                        }
                    }
                });
                // Set On Complete video show video cache status

                //Set Video View buffering listener to show loading bar
                mDataBinding.previewVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        switch (what) {
                            //Hide video loading/buffering img
                            case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                                mDataBinding.previewVideoImageLoading.setZ(0);
                                mDataBinding.previewVideoView.setZ(1);
                                mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                                mCircularProgressBar.stop();
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
                                        .load(mCircularProgressBar)
                                        .into(mDataBinding.previewVideoImageLoading);
                                mCircularProgressBar.start();
                                return true;
                            }
                            //Show video loading/buffering img
                        }
                        return false;
                    }
                });
                //Set Video View buffering listener to show loading bar

                //Set Video View Play & Pause Listener
                mDataBinding.previewVideoView.setPlayPauseListener(new AsmGvrStateBroadcastingVideoView.PlayPauseListener() {
                    @Override
                    public void onPlay() {
                        //Add Play img to show video image aside from media controller
                        mDataBinding.previewVideoImageLoading.setZ(1);
                        mDataBinding.previewVideoView.setZ(0);
                        mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                        mCircularProgressBar.stop();
                        Glide.with(requireActivity())
                                .load(R.drawable.asm_gvr_play)
                                .apply(new RequestOptions().override(256,256))
                                .centerInside()
                                .into(mDataBinding.previewVideoImageLoading);
                        //Add Play img to show video image aside from media controller
                        //Check if video is playing to not accidentally hide image view, then delay 1.5 seconds to remove asm_gvr_play img for video viewing experience
                        if (mDataBinding.previewVideoView.isPlaying()) {
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mDataBinding.previewVideoImageLoading.setZ(0);
                                    mDataBinding.previewVideoView.setZ(1);
                                    mDataBinding.previewVideoImageLoading.setVisibility(View.GONE);
                                    mCircularProgressBar.stop();
                                }
                            }, 1500);
                        }
                        //Check if video is playing to not accidentally hide image view, then delay 1.5 seconds to remove asm_gvr_play img for video viewing experience
                    }

                    @Override
                    public void onPause() {
                        //Add Pause img to show video image aside from media controller
                        mDataBinding.previewVideoImageLoading.setZ(1);
                        mDataBinding.previewVideoView.setZ(0);
                        mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                        Glide.with(requireActivity())
                                .load(R.drawable.asm_gvr_pause)
                                .apply(new RequestOptions().override(256,256))
                                .centerInside()
                                .into(mDataBinding.previewVideoImageLoading);
                        //Add Pause img to show video image aside from media controller
                    }
                });
                //Set Video View Play & Pause Listener

                //Set Video View on click listener for asm_gvr_play video alternative media controller
                mDataBinding.previewVideoImageLoading.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Only asm_gvr_play video because the image view will not be present because of Z-Index hiding, so no use to pause
                        mDataBinding.previewVideoView.start();
                        //Only asm_gvr_play video because the image view will not be present because of Z-Index hiding, so no use to pause
                    }
                });
                //Set Video View on click listener for asm_gvr_play video alternative media controller

                //Set Video view asm_gvr_play back error listener
                mDataBinding.previewVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        switch(what) {
                            case MediaPlayer.MEDIA_ERROR_IO:
                            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED: {
                                mDataBinding.previewVideoImageLoading.setVisibility(View.VISIBLE);
                                Glide.with(requireActivity())
                                        .load(R.drawable.asm_gvr_unable_load)
                                        .apply(new RequestOptions().override(256,256))
                                        .centerInside()
                                        .into(mDataBinding.previewVideoImageLoading);
                            }
                        }
                        return false;
                    }
                });
                //Set Video view asm_gvr_play back error listener
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        // Resume video here instead of onPrepared to avoid 2 page playing
        if(!videoErrorFlag){
            mDataBinding.previewVideoView.start();
        }
        // Resume video here instead of onPrepared to avoid 2 page playing
        // Reset media Controller stuck to pause
        if(mMediaController.isShowing()){
            mMediaController.hide();
            mMediaController.show();
        }
        // Reset media Controller stuck to pause
    }

    @Override
    public void onPause() {
        super.onPause();
        //Store in ViewModel the video playing position
        if(mDataBinding.previewVideoView.isPlaying()){
            mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
        }else if(!mDataBinding.previewVideoView.isPlaying() && mDataBinding.previewVideoView.getCurrentPosition() > 0){
            mStateBroadcastingVideoViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.previewVideoView.getCurrentPosition());
        }
        //Store in ViewModel the video playing position
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Start download using Download utils class
            if (requestCode == CHOOSE_DOWNLOAD_FOLDER_REQUEST_CODE) {
                if (data != null) {
                    mDownloadConfig.startDownload(requireActivity(), mViewPagerUri.toString(),data.getData());
                }
            }
            //Start download using Download utils class
        }
    }

    private void initSlideVideoDetails(Boolean isSlideUp){
        if(isSlideUp){
            mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.VISIBLE);
            TranslateAnimation animation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                   // toXDelta
                    mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.getHeight(),            // fromYDelta
                    0);// toYDelta
            animation.setDuration(500);
            animation.setFillAfter(true);
            mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setAnimation(animation);
        }else{
            mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                   // toXDelta
                    0,            // fromYDelta
                    mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.getHeight()); // toYDelta
            animation.setDuration(500);
            animation.setFillAfter(true);
            mDataBinding.includedPanelFileDetails.linearLayoutFileDetails.setAnimation(animation);
        }
    }
}
