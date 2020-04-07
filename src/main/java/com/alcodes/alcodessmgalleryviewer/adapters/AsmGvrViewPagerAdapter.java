package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;
import com.alcodes.alcodessmgalleryviewer.entities.AsmGvrStateBroadcastingVideoView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.alcodes.alcodessmgalleryviewer.R;

import org.jetbrains.annotations.NotNull;

public class AsmGvrViewPagerAdapter extends PagerAdapter {
    private Context context;
    private String[] urls;
    private ImageView errorImageView;
    private ViewGroup container;
    private AsmGvrVideoPlayer mVideoPlayer;
    private AsmGvrAudioPlayer mAudioPlayer;
    private AsmGvrOpenUnknownFile mOpenUnknownFile;
    private View viewDisplay;
    private Uri uri;
    private String fileType;
    private ContentResolver cR;

    public AsmGvrViewPagerAdapter(Context context, String[] urls) {
        this.context = context;
        this.urls = urls;
    }

    @Override
    public int getCount() {
        return urls.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NotNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        viewDisplay = null;
        if(errorImageView == null){
            errorImageView = new ImageView(context);
            errorImageView.setScaleType(ImageView.ScaleType.CENTER);
            errorImageView.setImageResource(R.drawable.asm_gvr_ic_error_outline_black_128dp);
            errorImageView.setLayoutParams(new LinearLayout.LayoutParams(
                    200, 200));
        }

        // Check url is empty or not
        if (urls.length!=0){
            uri = Uri.parse(urls[position]);
            fileType = checkUrlAndUriType(uri);
            //check fileType is null or not
            if(fileType != null){
                if(fileType.equals("video")) {
                    if(mVideoPlayer == null){
                        mVideoPlayer = new AsmGvrVideoPlayer();
                    }
                    viewDisplay = mVideoPlayer.startVideoPlayer(context, uri);
                }else if(fileType.equals("image")) {
                    viewDisplay = new AsmGvrTouchImageView(context, uri);
                }else if(fileType.equals("audio")) {
                    if(mAudioPlayer == null){
                        mAudioPlayer = new AsmGvrAudioPlayer();
                    }
                    viewDisplay = mAudioPlayer.initializeAudioPlayer(context, uri);
                }else{
                    if(mOpenUnknownFile == null){
                        mOpenUnknownFile = new AsmGvrOpenUnknownFile();
                    }
                    viewDisplay = mOpenUnknownFile.startOpenUnknownFile(context, uri);
                }
                if(viewDisplay==null){
                    viewDisplay = errorImageView;
                }
            }else{
                errorImageView.setImageResource(R.drawable.asm_gvr_ic_error_outline_black_128dp);
                viewDisplay = errorImageView;
            };
            container.addView(viewDisplay);
        }
        this.container = container;

        return viewDisplay;
    }

    public void resetBackForwardPagerView(){
        //Loop through container's ViewGroup item to find Video View to pause and reset MediaController
        for(int i=0;i < container.getChildCount();i++){
            if(container.getChildAt(i) instanceof LinearLayout){
                if(((LinearLayout) container.getChildAt(i)).getChildAt(0) instanceof AsmGvrStateBroadcastingVideoView){
                    if(((AsmGvrStateBroadcastingVideoView) ((LinearLayout) container.getChildAt(i)).getChildAt(0)).isPlaying()){
                        ((AsmGvrStateBroadcastingVideoView) ((LinearLayout) container.getChildAt(i)).getChildAt(0)).pause();
                        MediaController mediaController = new MediaController(context);
                        mediaController.setAnchorView(((VideoView) ((LinearLayout) container.getChildAt(i)).getChildAt(0)));
                        ((AsmGvrStateBroadcastingVideoView) ((LinearLayout) container.getChildAt(i)).getChildAt(0)).setMediaController(mediaController);
                    }
                }
            }
        }
        //Loop through container's ViewGroup item to find Video View to pause and reset MediaController
    }

    public String checkUrlAndUriType (Uri uri){
        if(cR == null){
            cR = context.getContentResolver();
        }
        if(uri.getScheme().equals("http") | uri.getScheme().equals("https")){
            try{
                fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase());
                fileType = fileType.substring(0, fileType.lastIndexOf("/"));
                return fileType;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else{
            fileType = cR.getType(uri).substring(0, cR.getType(uri).lastIndexOf("/"));
            return fileType;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

}
