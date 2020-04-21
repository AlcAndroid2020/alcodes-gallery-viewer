package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewAudioBinding;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrDownloadConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrOpenWithConfig;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrShareConfig;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrPreviewAudioViewModel;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrCircularProgressBar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.CacheListener;
import com.vincan.medialoader.DownloadManager;
import com.vincan.medialoader.MediaLoader;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class AsmGvrPreviewAudioFragment extends Fragment implements CacheListener {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";
    private static final String ARG_String_FILEURL = "ARG_STRING_PAGER_FILEURL";
    private static final String ARG_STRING_FILE_TYPE = "ARG_STRING_FILE_TYPE";
    private static final String ARG_STRING_FILE_NAME = "ARG_STRING_FILE_NAME";
    private static final String ARG_String_IsInternetSource = "ARG_String_IsInternetSource ";

    private NavController mNavController;
    private AsmGvrFragmentPreviewAudioBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrPreviewAudioViewModel mPreviewAudioViewModel;
    private int mViewPagerPosition;
    private String mViewPagerURL;
    private Boolean mInternetSource;
    private ActionBar mActionBar;
    private AsmGvrDownloadConfig mGvrDownloadConfig;
    private AsmGvrShareConfig mGvrShareConfig;
    private AsmGvrOpenWithConfig mGvrOpenWithConfig;
    private Boolean IsSlideUp = false;
    private FFmpegMediaMetadataRetriever mFFmpegMMR;
    private String ProxyUrl = null;
    private String mFileType;
    private String mFileName;
    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_MAX_OFF_PATH = 120;


    public AsmGvrPreviewAudioFragment() {
    }

    public static AsmGvrPreviewAudioFragment newInstance(AsmGvrMediaConfig position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position.getPosition());
        args.putString(ARG_String_FILEURL, position.getUri());
        args.putString(ARG_String_IsInternetSource, position.getFromInternetSource().toString());
        args.putString(ARG_STRING_FILE_TYPE, position.getFileType());
        args.putString(ARG_STRING_FILE_NAME, position.getFileName());
        AsmGvrPreviewAudioFragment fragment = new AsmGvrPreviewAudioFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewAudioBinding.inflate(inflater, container, false);

        mActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        setHasOptionsMenu(true);

        return mDataBinding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.asm_gvr_audio_menu, menu);
        if (!mInternetSource) {
            //for local file hide download option
            menu.findItem(R.id.audio_menu_download).setVisible(false);
        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.audio_menu_open_with) {
            //open audio with other app
            mGvrOpenWithConfig.openWith(getContext(), Uri.parse(mViewPagerURL));

        } else if (itemId == R.id.audio_menu_download) {
            //open file picker to select folder to download
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, 42);
        } else if (itemId == R.id.audio_menu_share) {
            //share audio
            mGvrShareConfig.shareWith(getContext(), Uri.parse(mViewPagerURL));
        } else if (itemId == R.id.audio_menu_details) {
            //show audio file details

            //check if details is shown
            if (!IsSlideUp) {
                //show details
                onSlideAudioDetailUp(true);
                IsSlideUp = true;
            } else {
                //hide details
                onSlideAudioDetailUp(false);
                IsSlideUp = false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSlideAudioDetailUp(Boolean isSlidedUP) {

        //slide up
        if (isSlidedUP) {

            mDataBinding.rootAudioDetails.setVisibility(View.VISIBLE);
            TranslateAnimation animation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                   // toXDelta
                    mDataBinding.rootAudioDetails.getHeight(),            // fromYDelta
                    0);// toYDelta
            animation.setDuration(500);
            animation.setFillAfter(true);
            mDataBinding.rootAudioDetails.setAnimation(animation);
        } else {
            //slide down / close details
            mDataBinding.rootAudioDetails.setVisibility(View.INVISIBLE);
            TranslateAnimation animation = new TranslateAnimation(
                    0,                 // fromXDelta
                    0,                   // toXDelta
                    0,            // fromYDelta
                    mDataBinding.rootAudioDetails.getHeight()); // toYDelta
            animation.setDuration(500);
            animation.setFillAfter(true);
            mDataBinding.rootAudioDetails.setAnimation(animation);
        }

    }


    private void showdetails() {
        // show audio file details

        //for local file
        if (!mInternetSource) {

            Uri uri = Uri.parse(mViewPagerURL);
            DocumentFile df = DocumentFile.fromSingleUri(getContext(), uri);
            mDataBinding.audioViewFileName.setText(mFileName);
            mDataBinding.audioViewFiletype.setText(df.getType());
            String size = "";
            //format size


            size = createFileSizeLabel(df.length());
            mDataBinding.audioViewFileSize.setText(size);

            mDataBinding.audioViewDuration.setText("Duration: " + createTimeLabel(mDataBinding.AudioPlayer.getDuration()));
            //format date
            Date d = new Date(df.lastModified());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String newDate = formatter.format(d);
            mDataBinding.audioDate.setText(newDate);
            mDataBinding.audioViewPath.setText(uri.getPath());

        } else {
            //for  url file

            mFFmpegMMR.setDataSource(ProxyUrl);

            mDataBinding.audioViewFileName.setText(mFileName);
            mDataBinding.audioViewDuration.setText("Duration: " + createTimeLabel(mDataBinding.AudioPlayer.getDuration()));
            String extension = ProxyUrl.substring(ProxyUrl.lastIndexOf(".") + 1);
            mDataBinding.audioViewFiletype.setText("audio / " + extension);
            mDataBinding.audioViewPath.setText(ProxyUrl);
            String mSize = createFileSizeLabel(Long.valueOf(mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_FILESIZE)));
            mDataBinding.audioViewFileSize.setText(mSize);

            mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME);
            if (mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME) != null)
                mDataBinding.audioDate.setText(String.format("Date Created: %s", mFFmpegMMR.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_CREATION_TIME)));

            else
                mDataBinding.audioDateRoot.setVisibility(View.GONE);

        }

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
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


    private String createFileSizeLabel(long mSize) {
        String size;
        if (mSize > 0) {

            //convert bytes to mb size
            DecimalFormat format = new DecimalFormat("#.##");
            long MiB = 1024 * 1024;
            long KiB = 1024;

            double length = Double.parseDouble(String.valueOf(mSize));

          /*  if (length > KiB) {
                size = format.format(length / KiB) + " KB";
            }
            if (length > MiB) {
                size = format.format(length / MiB) + " MB";
            } else
                size = format.format(length) + " B";
*/
            size = format.format(length / MiB) + " MB";

        } else
            size = "0 MB";

        return size;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mGvrDownloadConfig == null)
            mGvrDownloadConfig = new AsmGvrDownloadConfig();

        if (mGvrShareConfig == null)
            mGvrShareConfig = new AsmGvrShareConfig();

        if (mGvrOpenWithConfig == null)
            mGvrOpenWithConfig = new AsmGvrOpenWithConfig();

        if (savedInstanceState != null) {

            if (savedInstanceState.getBoolean("IsDetailShown")) {
                onSlideAudioDetailUp(true);
                IsSlideUp = true;
            }
        }
        // Extract arguments.
        mViewPagerPosition = requireArguments().getInt(ARG_INT_PAGER_POSITION);
        mViewPagerURL = requireArguments().getString(ARG_String_FILEURL);
        mInternetSource = checkFileType(mViewPagerURL);
        mFileType = requireArguments().getString(ARG_STRING_FILE_TYPE);
        mFileName = requireArguments().getString(ARG_STRING_FILE_NAME);

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


        //for get details for url file
        if (mFFmpegMMR == null) {
            if (mPreviewAudioViewModel.getFFmpegMMR() == null) {
                mPreviewAudioViewModel.setFFmpegMMR();
                mFFmpegMMR = mPreviewAudioViewModel.getFFmpegMMR();
            } else {
                mFFmpegMMR = mPreviewAudioViewModel.getFFmpegMMR();
            }
        }


        //get selected color
        mMainSharedViewModel.getColorSelectedLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    mDataBinding.previewAudioRoot.setBackgroundColor(ContextCompat.getColor(getActivity(), integer));

                }
            }
        });

        //resume if screen rotation
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
                        }else if(!mDataBinding.AudioPlayer.isPlaying() & mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition >= -1){
                            mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());
                        }
                    }
                }
            }
        });


        //hide and show menu bar
        mDataBinding.previewAudioRoot.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(requireActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (mActionBar.isShowing()) {
                        mActionBar.hide();
                    } else {
                        mActionBar.show();
                    }
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return super.onSingleTapUp(e);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


                    if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH) {
                        //Swipe Left or Right will not take any action
                        return false;
                    }

                    if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE) {
                        // Swipe Up
                        onSlideAudioDetailUp(true);
                    } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE) {
                        // Swipe Down
                        onSlideAudioDetailUp(false);

                    }

                    return super.onFling(e1, e2, velocityX, velocityY);
                }

            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }


        });


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


        //initiz video view/load music
        mDataBinding.AudioPlayer.setZ(0);
        mDataBinding.loadgif.setZ(1);
        Glide.with(this)
                .load(new AsmGvrCircularProgressBar(requireContext()))
                .into(mDataBinding.loadgif);

        //initiz video view/load music
        mediaController.setAnchorView(mDataBinding.AudioPlayer);
        mDataBinding.AudioPlayer.setMediaController(mediaController);


        if (IsOnlineAudio) {
            //cache audio
            String proxyUrl = MediaLoader.getInstance(getContext()).getProxyUrl(uri.toString());
            mDataBinding.AudioPlayer.setVideoURI(Uri.parse(proxyUrl));
            ProxyUrl = proxyUrl;
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
                //set details
                showdetails();

                //get progress if screen rotated and slide to other page
                if (mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition != -1) {
                    mDataBinding.AudioPlayer.seekTo(mPreviewAudioViewModel.getViewPagerVideoViewCurrentPlayingPosition(mViewPagerPosition).currentPlayingPosition);
                }

            }
        });

    }


    private void showErrorMsg() {

        mDataBinding.AudioPlayer.setVisibility(View.GONE);
        Glide.with(requireContext())
                .load(R.drawable.asm_gvr_no_wifi)
                .apply(new RequestOptions().override(256, 256))
                .centerInside()
                .into(mDataBinding.loadgif);

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
        if (mDataBinding.AudioPlayer.isPlaying())
            mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());
        else if(!mDataBinding.AudioPlayer.isPlaying() && mDataBinding.AudioPlayer.getCurrentPosition() > 0){
            mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("IsDetailShown", IsSlideUp);
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            //Start download using Download utils class
            if (requestCode == 42) {
                if (data != null) {
                    mGvrDownloadConfig.startDownload(requireActivity(), mViewPagerURL, data.getData());
                }
            }
        }
    }
}
