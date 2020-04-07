package com.alcodes.alcodessmgalleryviewer.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewAudioFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewImageFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewVideoFragment;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AsmGvrMainViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> mData;
    private String fileType;
    private ContentResolver cR;

    public AsmGvrMainViewPagerAdapter(@NonNull Fragment fragment, List<String> data) {
        super(fragment);
        cR = fragment.getContext().getContentResolver();
        mData = data;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String data = mData.get(position);
        Uri uri = Uri.parse(data);
        fileType = checkUrlAndUriType(uri);

        if(fileType != null){
            if(fileType.equals("video")) {
                return AsmGvrPreviewVideoFragment.newInstance(position);
            }else if(fileType.equals("image")) {
                return AsmGvrPreviewImageFragment.newInstance(position);
            }else if(fileType.equals("audio")) {
                return AsmGvrPreviewAudioFragment.newInstance(position);
            }else{
                return AsmGvrPreviewAudioFragment.newInstance(position);
            }
        }

        // Default return image preview.
        return AsmGvrPreviewImageFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public String checkUrlAndUriType (Uri uri){
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
            try{
            fileType = cR.getType(uri).substring(0, cR.getType(uri).lastIndexOf("/"));
            return fileType;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
