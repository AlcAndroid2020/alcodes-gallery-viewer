package com.alcodes.alcodessmgalleryviewer.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
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

public class AsmGvrPreviewAudioFragment extends Fragment {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource ";

    private NavController mNavController;
    private AsmGvrFragmentPreviewAudioBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrPreviewAudioViewModel mPreviewAudioViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;
    private AnimationDrawable mAnimationDrawable;
    private Boolean mIsInternetConnected;

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
        mInternetSource = Boolean.valueOf(requireArguments().getString(ARG_String_IsInternetSource));


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
        //Check Internet State
        mIsInternetConnected = mMainSharedViewModel.getInternetStatusDataLiveData().getValue().internetStatus;

        //load music player

        //determine if audio is from url
        if (mInternetSource == true) {
            if (mIsInternetConnected == true)
                loadmusic(Uri.parse(mViewPagerURL));
                //downloadOffline(mViewPagerURL);

            else
                showErrorMsg();
        } else {
            //for local audio/offline audio
            loadmusic(Uri.parse(mViewPagerURL));
        }

    }

    private void downloadOffline(String link) {

        //still study
    }



    private void loadmusic(Uri uri) {

        //initiz video view/song player
        MediaController mediaController = new MediaController(getContext());

        //loading dialog

        //initiz video view/load music
        mediaController.setAnchorView(mDataBinding.AudioPlayer);
        mDataBinding.AudioPlayer.setMediaController(mediaController);
        mDataBinding.AudioPlayer.setVideoURI(uri);

        //when player ready to play
        mDataBinding.AudioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                //close progress bar
                mDataBinding.indeterminateBar.setVisibility(View.GONE);

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
        new AlertDialog.Builder(getContext())
                .setTitle("No Internet Connection")
                .setMessage("Internet connection is required to play audio")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
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

}
