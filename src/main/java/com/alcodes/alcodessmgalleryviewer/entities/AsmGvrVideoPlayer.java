package com.alcodes.alcodessmgalleryviewer.entities;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.MediaController;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.activities.AsmGvrMainActivity;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewVideoBinding;

public class AsmGvrVideoPlayer {
    private Boolean noErrorFlag = true;
    private String fileType = "";
    private AsmGvrStateBroadcastingVideoView mStateBroadcastingVideoView;
    private Uri uri;

    public AsmGvrVideoPlayer(){
        this.noErrorFlag = true;
        this.fileType = "";
    }

    public Boolean startVideoPlayer(Context context, Uri uri, AsmGvrFragmentPreviewVideoBinding mDataBinding){
    // Initialize VideoView with custom play & pause listener
        mDataBinding.previewVideoView.setForeground(null);
        mDataBinding.previewVideoView.setForeground(context.getDrawable(R.drawable.asm_gvr_loading_animation));
        mDataBinding.previewVideoView.setForegroundGravity(Gravity.CENTER);
        AnimationDrawable mAnimationDrawable = (AnimationDrawable) mDataBinding.previewVideoView.getForeground();
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
                    MediaController mMediaController = new MediaController(context);
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
                mp.start();
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
}
