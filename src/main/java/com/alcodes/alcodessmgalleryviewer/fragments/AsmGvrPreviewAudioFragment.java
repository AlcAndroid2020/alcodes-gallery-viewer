package com.alcodes.alcodessmgalleryviewer.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.MediaConfig;
import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewAudioBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.util.Locale;

import timber.log.Timber;

public class AsmGvrPreviewAudioFragment extends Fragment {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_PAGER_FILEURL = "ARG_STRING_PAGER_FILEURL";

    private NavController mNavController;
    private VideoView videoView;
    private AsmGvrFragmentPreviewAudioBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;

    public AsmGvrPreviewAudioFragment() {
    }

    public static AsmGvrPreviewAudioFragment newInstance(MediaConfig position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position.getPosition());
        args.putString(ARG_String_PAGER_FILEURL, position.getUri());
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
        mViewPagerURL = requireArguments().getString(ARG_String_PAGER_FILEURL);
        mDataBinding.textViewDemo.setText(String.format(Locale.ENGLISH, "Position: %d %s", mViewPagerPosition, mViewPagerURL));

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
        loadmusic();
        if (savedInstanceState != null) {
            //last stoped progress
           progress=savedInstanceState.getInt("audioProgress");


        }

    }

    private void loadmusic() {
        //initiz video view/load music
        MediaController mediaController = new MediaController(getContext());
        videoView = mDataBinding.AudioPlayer;
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(Uri.parse(mViewPagerURL));
        videoView.setForeground(getContext().getDrawable(R.drawable.muisicon));

        if(progress!=0)
            videoView.seekTo(progress);


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

    }

    int progress;

    @Override
    public void onResume() {
        super.onResume();
        videoView.seekTo(progress);
        videoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(videoView.isPlaying())
        progress = videoView.getCurrentPosition();
        String time=createTimeLabel(progress);
        Toast.makeText(getContext(),"Pause at"+time,Toast.LENGTH_SHORT).show();
        videoView.pause();
    }
    private String createTimeLabel(int time) {
        String timelabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timelabel = min + ":";
        if (sec < 10)
            timelabel += "0";
        timelabel += sec;
        return timelabel;
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //for recover audio when rotation
       outState.putInt("audioProgress",progress);

    }

}
