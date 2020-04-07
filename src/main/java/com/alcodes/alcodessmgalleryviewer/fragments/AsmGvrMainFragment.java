package com.alcodes.alcodessmgalleryviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrViewPagerAdapter;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentMainBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class AsmGvrMainFragment extends Fragment {

    private AsmGvrFragmentMainBinding mDataBinding;

    private String[] Urls = new String[] {
            "https://i.pinimg.com/236x/64/84/6d/64846daa5a346126ef31c3f1fcbc4703--winter-wallpapers-wallpapers-ipad.jpg",
            "https://images.wallpaperscraft.com/image/snow_snowflake_winter_form_pattern_49405_240x320.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Rotating_earth_%28large%29.gif/300px-Rotating_earth_%28large%29.gif",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://files.eric.ed.gov/fulltext/ED573583.pdf"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Init data binding;
        mDataBinding = AsmGvrFragmentMainBinding.inflate(inflater, container, false);

        return mDataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ViewPager viewPager = mDataBinding.viewPager;
        final AsmGvrViewPagerAdapter adapter = new AsmGvrViewPagerAdapter(getContext(), Urls);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(getContext(), "viewpager position " + position, Toast.LENGTH_SHORT).show();
                adapter.checkPage(viewPager);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }
}
