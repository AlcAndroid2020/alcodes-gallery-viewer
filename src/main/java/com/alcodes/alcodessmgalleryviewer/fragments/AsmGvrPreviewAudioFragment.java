package com.alcodes.alcodessmgalleryviewer.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewAudioBinding;
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrPreviewAudioViewModel;
import com.bumptech.glide.Glide;
import com.danikula.videocache.CacheListener;
import com.vincan.medialoader.DownloadManager;
import com.vincan.medialoader.MediaLoader;

import java.io.File;

public class AsmGvrPreviewAudioFragment extends Fragment implements CacheListener {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource ";

    private NavController mNavController;
    private AsmGvrFragmentPreviewAudioBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrPreviewAudioViewModel mPreviewAudioViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;
    private Boolean mInternetSource;

    public AsmGvrPreviewAudioFragment() {
    }

    public static AsmGvrPreviewAudioFragment newInstance(AsmGvrMediaConfig position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position.getPosition());
        args.putString(ARG_String_FILEURL, position.getUri());
        args.putString(ARG_String_IsInternetSource, position.getFromInternetSource().toString());
        AsmGvrPreviewAudioFragment fragment = new AsmGvrPreviewAudioFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewAudioBinding.inflate(inflater, container, false);

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
        mViewPagerURL = requireArguments().getString(ARG_String_FILEURL);
        // mInternetSource = Boolean.valueOf(requireArguments().getString(ARG_String_IsInternetSource));
        mInternetSource = checkFileType(mViewPagerURL);

        // Init view model.

        //ShareviewModel
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).
                get(AsmGvrMainSharedViewModel.class);

        //AudioViewModel
        mPreviewAudioViewModel = new ViewModelProvider(mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())).
                get(AsmGvrPreviewAudioViewModel.class);


        mMainSharedViewModel.getViewPagerPositionLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    if (integer == mViewPagerPosition) {
                        // TODO this page has been selected.
                        if (mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1) {
                            mDataBinding.AudioPlayer.seekTo(mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                        }
                    } else {
                        // TODO this page has been de-selected.
                        if (mDataBinding.AudioPlayer.isPlaying()) {
                            mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());
                        }
                    }
                }
            }
        });
/*
        //Check Internet State
        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {

            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                if (internetStatusData.internetStatus) {

                    //Internet Connected


                    //for Online file (url)
                    if (mInternetSource) {
                        loadmusic(Uri.parse(mViewPagerURL), true);
                    } else {
                        loadmusic(Uri.parse(mViewPagerURL), false);
                    }

                } else {

                    //No Internet

                    //for Online file (url)
                    if (mInternetSource) {
                        if (DownloadManager.getInstance(getContext()).isCached(mViewPagerURL))
                            loadmusic(Uri.parse(mViewPagerURL), true);
                        else
                            showErrorMsg();

                    } else {
                        //for offline file (uri)
                        loadmusic(Uri.parse(mViewPagerURL), false);
                    }

                }
            }
        });
*/
        //able to recove progress for online video onky
        loadmusic(Uri.parse(mViewPagerURL), true);

    }

    private Boolean checkFileType(String mViewPagerURL) {

        if (mViewPagerURL.substring(0, 5).equals("https"))
            return true;
        else if (mViewPagerURL.substring(0, 4).equals("http"))
            return true;

        else
            return false;
    }


    private void loadmusic(Uri uri, Boolean IsOnlineAudio) {

        //initiz video view/song player

        MediaController mediaController = new MediaController(getContext());

        //loading dialog
        mDataBinding.AudioPlayer.setZ(0);
        mDataBinding.loadgif.setZ(1);
        Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(mDataBinding.loadgif);


        //initiz video view/load music

        mediaController.setAnchorView(mDataBinding.AudioPlayer);
        mDataBinding.AudioPlayer.setMediaController(mediaController);


        if (IsOnlineAudio) {
            //cache audio
            String proxyUrl = MediaLoader.getInstance(getContext()).getProxyUrl(uri.toString());
            mDataBinding.AudioPlayer.setVideoURI(Uri.parse(proxyUrl));
            //video path

        } else {
            //for local audio/offline audio
            mDataBinding.AudioPlayer.setVideoURI(uri);

        }
        //when player ready to play
        mDataBinding.AudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                //close progress bar
                mDataBinding.loadgif.setVisibility(View.GONE);

                //set music icon
                mDataBinding.AudioPlayer.setForeground(getContext().getDrawable(R.drawable.asm_gvr_music_icon));

                //get progress if screen rotated and slide to other page
                if (mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1) {
                    mDataBinding.AudioPlayer.seekTo(mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                }

            }
        });
    }


    private void showErrorMsg() {

        mDataBinding.AudioPlayer.setVisibility(View.GONE);
        mDataBinding.loadgif.setImageResource(R.drawable.asm_gvr_no_internet);

    }

    //to recover progress when screen rotate/rotete back
    @Override
    public void onResume() {
        super.onResume();
        mDataBinding.AudioPlayer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDataBinding.AudioPlayer.isPlaying()) {
            mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());
        }
    }


    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }
}
