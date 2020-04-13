package com.alcodes.alcodessmgalleryviewer.adapters;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewAudioFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewImageFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewUnknowFileFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewVideoFragment;

import java.util.List;
import com.alcodes.alcodessmgalleryviewer.utils.AsmGvrMediaConfig;

import org.jetbrains.annotations.NotNull;

public class AsmGvrMainViewPagerAdapter extends FragmentStateAdapter {
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

        String fileType = mMediaConfig.checkUrlAndUriType(fragment.getActivity(), Uri.parse(data));
        Boolean isOnline;
        if(fileType.endsWith("online")){
            isOnline = true;
        }else{
            isOnline = false;
        }

        if(!fileType.startsWith("image") && !fileType.startsWith("video") && !fileType.startsWith("audio")){
            mMediaConfig.setFileType("unknown");
        }else{
            mMediaConfig.setFileType(fileType.substring(0, fileType.lastIndexOf("/")));
        }
        mMediaConfig.setFromInternetSource(isOnline);
        mMediaConfig.setPosition(position);
        mMediaConfig.setUri(data);

        if(fileType.startsWith("image")){
            return AsmGvrPreviewImageFragment.newInstance(mMediaConfig);
        }else if(fileType.startsWith("audio")){
            return AsmGvrPreviewAudioFragment.newInstance(mMediaConfig);
        }else if(fileType.startsWith("video")){
            return AsmGvrPreviewVideoFragment.newInstance(mMediaConfig);
        }else{
            return AsmGvrPreviewUnknowFileFragment.newInstance(mMediaConfig);
        }

        // Default return image preview.
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

}
