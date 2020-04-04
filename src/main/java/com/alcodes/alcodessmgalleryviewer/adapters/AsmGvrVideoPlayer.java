package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.MediaController;

import com.alcodes.alcodessmgalleryviewer.R;

public class AsmGvrVideoPlayer {
    private Boolean noErrorFlag = true;
    private String fileType = "";

    public AsmGvrVideoPlayer(){
        this.noErrorFlag = true;
        this.fileType = "";
    }

    public View startVideoPlayer(Context context, Uri uri){
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        final AsmGvrStateBroadcastingVideoView videoView = new AsmGvrStateBroadcastingVideoView(context);
        ll.addView(videoView, 0);
        videoView.setForeground(null);
        videoView.setForeground(context.getDrawable(R.drawable.asm_gvr_loading_animation));
        videoView.setForegroundGravity(Gravity.CENTER);
        final AnimationDrawable animationDrawable = (AnimationDrawable) videoView.getForeground();
        animationDrawable.start();
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
                    MediaController mediaController = new MediaController(context);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                    videoView.setVideoURI(uri);
                }
            }else{
                return null;
            }
        }else{
            return null;
        }

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                animationDrawable.stop();
                videoView.setForeground(null);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                animationDrawable.stop();
                videoView.setForeground(null);
            }
        });
        videoView.setPlayPauseListener(new AsmGvrStateBroadcastingVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {

            }

            @Override
            public void onPause() {

            }
        });

        return ll;
    }
}
