package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alcodes.alcodessmgalleryviewer.R;

public class AsmGvrAudioPlayer {
    private Boolean noErrorFlag = true;
    private String fileType = "";
    private AnimationDrawable animationMusicDrawable;
    private AsmGvrStateBroadcastingVideoView mediaPlayer;
    Context mContext;
    private int duration;
    int newduration;
    Boolean isRotate = false;

    public AnimationDrawable getAnimationMusicDrawable() {
        return animationMusicDrawable;
    }

    public void setAnimationMusicDrawable(AnimationDrawable animationMusicDrawable) {
        this.animationMusicDrawable = animationMusicDrawable;
    }

    public View initize(final Context getContext, Uri uri) {

        final Context context = getContext;
        mContext = getContext;
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);
        mediaPlayer = new AsmGvrStateBroadcastingVideoView(context);
        ll.addView(mediaPlayer, 0);
        mediaPlayer.setForeground(null);
        mediaPlayer.setForeground(context.getDrawable(R.drawable.asm_gvr_loading_animation));
        mediaPlayer.setForegroundGravity(Gravity.CENTER);
        final AnimationDrawable animationDrawable = (AnimationDrawable) mediaPlayer.getForeground();
        animationDrawable.start();
        if (uri != null) {

            try {
                fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase());
                fileType = fileType.substring(0, fileType.lastIndexOf("/"));
            } catch (Exception e) {
                e.printStackTrace();
                noErrorFlag = false;
            }

            if (noErrorFlag) {
                if (fileType.startsWith("audio")) {

                    MediaController mediaController2 = new MediaController(context);
                    //  mediaPlayer.setBackgroundColor(Color.BLUE); set some image/icon
                    mediaController2.setAnchorView(mediaPlayer);
                    mediaPlayer.setMediaController(mediaController2);

                    //set videoview
                    mediaPlayer.setVideoURI(uri);

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaPlayer.setForeground(null);
                            mediaPlayer.setForeground(context.getDrawable(R.drawable.asm_gvr_music_animation));
                            final AnimationDrawable animationMusicDrawable = (AnimationDrawable) mediaPlayer.getForeground();
                            mediaPlayer.setForegroundGravity(Gravity.CENTER);
                            setAnimationMusicDrawable(animationMusicDrawable);

                            if (newduration != 0) {
                                mediaPlayer.seekTo(newduration);
                                newduration = 0;
                            }


                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            if (getAnimationMusicDrawable() != null)
                                mediaPlayer.setForeground(null);
                        }
                    });
                    mediaPlayer.setPlayPauseListener(new AsmGvrStateBroadcastingVideoView.PlayPauseListener() {
                        @Override
                        public void onPlay() {
                            getAnimationMusicDrawable().start();
                        }

                        @Override
                        public void onPause() {
                            if (getAnimationMusicDrawable() != null)
                                getAnimationMusicDrawable().stop();

                        }

                    });


                    return ll;
                } else
                    return null;
            } else
                return null;
        } else
            return null;
    }



    public int getProgress() {
        Toast.makeText(mContext,"get"+mediaPlayer.getCurrentPosition(),Toast.LENGTH_SHORT).show();

        return mediaPlayer.getCurrentPosition();

    }


    public void setProgress(int d) {
        newduration = d;
    }
}
