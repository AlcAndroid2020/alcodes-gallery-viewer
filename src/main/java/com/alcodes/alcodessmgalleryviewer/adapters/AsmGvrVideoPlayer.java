package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.MediaController;
import com.alcodes.alcodessmgalleryviewer.entities.AsmGvrStateBroadcastingVideoView;

import com.alcodes.alcodessmgalleryviewer.R;

public class AsmGvrVideoPlayer {
    private Boolean noErrorFlag = true;
    private String fileType = "";
    private Uri uri;

    public AsmGvrVideoPlayer(){
        this.noErrorFlag = true;
        this.fileType = "";
    }

    public LinearLayout startVideoPlayer(Context context, Uri uri){
    // Initialize Linear Layout for Centered Display
        //Check for existing Linear Layout (Optimization)
        LinearLayout mll = new LinearLayout(context);
        mll.setOrientation(LinearLayout.VERTICAL);
        mll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llParam.gravity = Gravity.CENTER;
        mll.setLayoutParams(llParam);
        //Check for existing Linear Layout (Optimization)

    // Initialize VideoView with custom play & pause listener
        //Check for existing Video View (Optimization)
        AsmGvrStateBroadcastingVideoView mStateBroadcastingVideoView = new AsmGvrStateBroadcastingVideoView(context);
        //Add Video View into Linear Layout
        mll.addView(mStateBroadcastingVideoView, 0);
            //Add Video View into Linear Layout
        //Check for existing Video View (Optimization)

        mStateBroadcastingVideoView.setForeground(null);
        mStateBroadcastingVideoView.setForeground(context.getDrawable(R.drawable.asm_gvr_loading_animation));
        mStateBroadcastingVideoView.setForegroundGravity(Gravity.CENTER);
        AnimationDrawable mAnimationDrawable = (AnimationDrawable) mStateBroadcastingVideoView.getForeground();
        mAnimationDrawable.start();
        // Initialize VideoView with custom play & pause listener
    // Initialize Linear Layout for Centered Display

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
                   MediaController mMediaController = new MediaController(context);
                    mMediaController.setAnchorView(mStateBroadcastingVideoView);
                    mStateBroadcastingVideoView.setMediaController(mMediaController);
                    mStateBroadcastingVideoView.setVideoURI(uri);
                }
            }else{
                return null;
            }
        }else{
            return null;
        }
    //Assigning URI to Video View and Anchoring Media Controller to Video View

    //Setting Listener for Video View on preapred, finish, play and pause
        mStateBroadcastingVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                mAnimationDrawable.stop();
                mStateBroadcastingVideoView.setForeground(null);
            }
        });
        mStateBroadcastingVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mAnimationDrawable.stop();
                mStateBroadcastingVideoView.setForeground(null);
            }
        });
        mStateBroadcastingVideoView.setPlayPauseListener(new AsmGvrStateBroadcastingVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {

            }

            @Override
            public void onPause() {

            }
        });

        return mll;
    }
}
