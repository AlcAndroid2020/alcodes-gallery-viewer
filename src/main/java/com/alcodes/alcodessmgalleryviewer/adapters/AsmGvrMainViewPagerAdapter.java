package com.alcodes.alcodessmgalleryviewer.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.net.Uri;
import com.alcodes.alcodessmgalleryviewer.helper.AsmGvrMediaConfig;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewAudioFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewImageFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewUnknowFileFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewVideoFragment;

import java.util.List;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

public class AsmGvrMainViewPagerAdapter extends FragmentStateAdapter {
    //private final List<MediaConfig> mData;
    private final List<String> mData;
    private final AsmGvrMediaConfig mMediaConfig;
    private Fragment fragment;

    public AsmGvrMainViewPagerAdapter(@NonNull Fragment fragment,List<String> data) {
        super(fragment);
        this.fragment = fragment;
        mData = data;
        mMediaConfig = new AsmGvrMediaConfig();
    }

    @Override
    @NotNull
    public Fragment createFragment(int position) {
        String data = mData.get(position);
        String fileType = mMediaConfig.checkUrlAndUriType(fragment.getContext(), Uri.parse(data));
        Boolean isOnline;
        if(fileType.substring(fileType.lastIndexOf("/"), fileType.length()).equals("online")){
            isOnline = true;
        }else{
            isOnline = false;
        }

        boolean image = fileType.substring(0, fileType.lastIndexOf("/")).equals("image");
        boolean audio = fileType.substring(0, fileType.lastIndexOf("/")).equals("audio");
        boolean video = fileType.substring(0, fileType.lastIndexOf("/")).equals("video");

        if(!image || !audio || !video){
            mMediaConfig.setFileType("unknown");
        }else{
            mMediaConfig.setFileType(fileType.substring(0, fileType.lastIndexOf("/")));
        }
        mMediaConfig.setFromInternetSource(isOnline);
        mMediaConfig.setPosition(position);
        mMediaConfig.setUri(data);

        if(image){
            // TODO For other module please change the parameter for the new instance
            return AsmGvrPreviewImageFragment.newInstance(position);
        }else if(audio){
            // TODO For other module please change the parameter for the new instance
            return AsmGvrPreviewAudioFragment.newInstance(mMediaConfig);
        }else if(video){
            return AsmGvrPreviewVideoFragment.newInstance(mMediaConfig);
        }else{
            // TODO For other module please change the parameter for the new instance
            return AsmGvrPreviewUnknowFileFragment.newInstance(position);
        }

        // Default return image preview.
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
