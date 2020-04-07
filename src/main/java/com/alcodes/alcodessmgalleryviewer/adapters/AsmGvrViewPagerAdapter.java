package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.alcodes.alcodessmgalleryviewer.R;

public class AsmGvrViewPagerAdapter extends PagerAdapter {

    private Context context;
    private String[] urls;
    AsmGvrAudioPlayer audioPlayer;
    private ImageView errorImageView;

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

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        String fileType;
        View viewDisplay = null;
        Uri uri;
        errorImageView = new ImageView(context);
        errorImageView.setScaleType(ImageView.ScaleType.CENTER);
        errorImageView.setImageResource(R.drawable.asm_gvr_ic_error_outline_black_128dp);
        errorImageView.setLayoutParams(new LinearLayout.LayoutParams(
                200, 200));

        // Check url is empty or not
        if (urls.length!=0){
            uri = Uri.parse(urls[position]);
            fileType = checkUrlAndUriType(uri);

            //check fileType is null or not
            if(fileType != null){
                if(fileType.equals("video")) {
                    AsmGvrVideoPlayer videoPlayer = new AsmGvrVideoPlayer();
                    viewDisplay = videoPlayer.startVideoPlayer(context, uri);

                    //set error icon to viewDisplay when is null
                    if(viewDisplay==null){
                        viewDisplay = errorImageView;
                    }
                    container.addView(viewDisplay);
                }else if(fileType.equals("image")) {
                    viewDisplay = new AsmGvrTouchImageView(context, uri);

                    if(viewDisplay==null){
                        viewDisplay = errorImageView;
                    }
                    container.addView(viewDisplay);
                }else if(fileType.equals("audio")) {
                  audioPlayer = new AsmGvrAudioPlayer();
                    viewDisplay = audioPlayer.initize(context, uri);

                    if(viewDisplay==null){
                        viewDisplay = errorImageView;
                    }
                    container.addView(viewDisplay);
                    if(duration!=0)
                        audioPlayer.setProgress(duration);

                }else{
                    AsmGvrOpenUnknownFile openUnknownFile = new AsmGvrOpenUnknownFile();
                    viewDisplay = openUnknownFile.startOpenUnknownFile(context, uri);
                    ;
                    if(viewDisplay==null){
                        viewDisplay = errorImageView;
                    }
                    container.addView(viewDisplay);
                }
            }else{
                errorImageView.setImageResource(R.drawable.asm_gvr_ic_error_outline_black_128dp);
                viewDisplay = errorImageView;

                container.addView(viewDisplay);
            }
        }
        return viewDisplay;
    }

    public String checkUrlAndUriType (Uri uri){
        ContentResolver cR = context.getContentResolver();
        String fileType;
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

    public int  getprogress(){
       return audioPlayer.getProgress();
    }

    int duration;
    public void setProgress(int d) {
   duration=d;
    }
}
