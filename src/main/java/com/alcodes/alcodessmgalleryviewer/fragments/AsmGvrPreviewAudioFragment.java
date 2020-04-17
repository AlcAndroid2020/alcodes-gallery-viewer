package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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
import com.bumptech.glide.Glide;
import com.danikula.videocache.CacheListener;
import com.vincan.medialoader.DownloadManager;
import com.vincan.medialoader.MediaLoader;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private ActionBar mActionBar;
    private AsmGvrDownloadConfig mGvrDownloadConfig;
    private AsmGvrShareConfig mGvrShareConfig;
    private AsmGvrOpenWithConfig mGvrOpenWithConfig;

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
        } else {
            //for online hide file details
            menu.findItem(R.id.audio_menu_details).setVisible(false);

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

        }
        else if (itemId == R.id.audio_menu_details) {
            //show audio file details
            showdetails();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showdetails() {
        // show audio file details
        Uri uri = Uri.parse(mViewPagerURL);
        DocumentFile df = DocumentFile.fromSingleUri(getContext(), uri);
        String line1 = getResources().getString(R.string.filename) + ": " + df.getName() + "\n";
        String line2 = getResources().getString(R.string.filetype) + ": " + df.getType() + "\n";
        String size = "";
        //format size
        if (df.length() > 0) {

            //convert bytes to mb size
            DecimalFormat format = new DecimalFormat("#.##");
            long MiB = 1024 * 1024;
            long KiB = 1024;

            double length = Double.parseDouble(String.valueOf(df.length()));

            if (length > KiB) {
                size = format.format(length / KiB) + " KB";
            }
            if (length > MiB) {
                size = format.format(length / MiB) + " MB";
            } else
                size = format.format(length) + " B";

        } else
            size = "0 MB";

        String line3 =  getResources().getString(R.string.file_size)+": "+ size + "\n";

        //format date
        Date d = new Date(df.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String newDate = formatter.format(d);
        String line4 = getResources().getString(R.string.lastmodif) + ": " + newDate + "\n";
        String line5 = getResources().getString(R.string.path) + ": " + uri.getPath() + "\n";


        AlertDialog alertDialog = new AlertDialog.Builder(getContext())


                .setNegativeButton( getResources().getString(R.string.Okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setMessage(line1 + "\n" + line2 + "\n" + line3 + "\n" + line4 + "\n" + line5).show();


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
        if (mGvrDownloadConfig == null)
            mGvrDownloadConfig = new AsmGvrDownloadConfig();

        if (mGvrShareConfig == null)
            mGvrShareConfig = new AsmGvrShareConfig();

        if (mGvrOpenWithConfig == null)
            mGvrOpenWithConfig = new AsmGvrOpenWithConfig();

        // Extract arguments.
        mViewPagerPosition = requireArguments().getInt(ARG_INT_PAGER_POSITION);
        mViewPagerURL = requireArguments().getString(ARG_String_FILEURL);
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

        //loading dialog

        //initiz video view/load music
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
        mPreviewAudioViewModel.setViewPagerVideoViewLiveData(mViewPagerPosition, mDataBinding.AudioPlayer.getCurrentPosition());

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
