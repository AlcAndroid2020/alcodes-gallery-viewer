package com.alcodes.alcodessmgalleryviewer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewAudioFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewImageFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewUnknowFileFragment;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrPreviewVideoFragment;

import java.util.List;

public class AsmGvrMainViewPagerAdapter extends FragmentStateAdapter {

    private final List<String> mData;

    public AsmGvrMainViewPagerAdapter(@NonNull Fragment fragment, List<String> data) {
        super(fragment);

        mData = data;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String data = mData.get(position);

        if ("video".equals(data)) {
            return AsmGvrPreviewVideoFragment.newInstance(position);
        } else if ("audio".equals(data)) {
            return AsmGvrPreviewAudioFragment.newInstance(position);
        }else if ("file".equals(data)) {
            return AsmGvrPreviewUnknowFileFragment.newInstance(position);
        }

        // Default return image preview.
        return AsmGvrPreviewImageFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
