package com.alcodes.alcodessmgalleryviewer.fragments;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

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
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrStateBroadcastingVideoView;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import timber.log.Timber;

public class AsmGvrPreviewAudioFragment extends Fragment {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource ";

    private NavController mNavController;
    private VideoView videoView;
    private AsmGvrFragmentPreviewAudioBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;
    private AnimationDrawable mAnimationDrawable;

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
                        Timber.e("d;;Audio fragment: page has been selected at: %s", mViewPagerPosition);
                    } else {
                        // TODO this page has been de-selected.
                        Timber.e("d;;Audio fragment: page has been de-selected at: %s", mViewPagerPosition);
                    }
                }
            }
        });
        loadmusic(Uri.parse(mViewPagerURL));
    }

    private void loadmusic(Uri uri) {

        //determine the audio is from online/local



        //initiz video view/load music
        MediaController mediaController = new MediaController(getContext());
        videoView = mDataBinding.AudioPlayer;

        videoView.setForeground(getContext().getDrawable(R.drawable.asm_gvr_loading_animation));
        if (mAnimationDrawable == null) {
            mAnimationDrawable = (AnimationDrawable) videoView.getForeground();
        }
        mAnimationDrawable.start();

        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);

        if (  mMainSharedViewModel.getAudioProgress()!= 0)
            //videoView.seekTo(progress);
        videoView.seekTo(mMainSharedViewModel.getAudioProgress());

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mAnimationDrawable.stop();
                videoView.setForeground(getContext().getDrawable(R.drawable.muisicon));

                mp.start();
            }
        });


    }


    //to recover progress when screen rotate/rotete back
    int progress;

    @Override
    public void onResume() {
        super.onResume();
     /*   if (progress != 0)
            videoView.seekTo(progress);


*/
        videoView.seekTo(mMainSharedViewModel.getAudioProgress());
        videoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
    /*    if (videoView != null)
            if (videoView.isPlaying())
                progress = videoView.getCurrentPosition();
        */
    mMainSharedViewModel.setAudioPogress(videoView.getCurrentPosition());

        videoView.pause();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //for recover audio when rotation
      //  outState.putInt("audioProgress", progress);

    }


}
