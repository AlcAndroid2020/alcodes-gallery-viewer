package com.alcodes.alcodessmgalleryviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrMainViewPagerAdapter;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentMainBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

public class AsmGvrMainFragment extends Fragment {

    private NavController mNavController;
    private AsmGvrFragmentMainBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrMainViewPagerAdapter mAdapter;
    private ViewPager2.OnPageChangeCallback mViewPager2OnPageChangeCallback;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Init view model.
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        // Init adapter data.
        List<String> data = new ArrayList<>();
        /*data.add("image");
        data.add("image");
        data.add("video");
        data.add("audio");
        data.add("video");
        data.add("image");
        data.add("audio");
        data.add("image");*/
        data = Arrays.asList(Urls);

        // Init adapter and view pager.
        mAdapter = new AsmGvrMainViewPagerAdapter(this, data);

        mViewPager2OnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                mMainSharedViewModel.setViewPagerCurrentPagePosition(position);
            }
        };

        mDataBinding.viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        mDataBinding.viewPager.registerOnPageChangeCallback(mViewPager2OnPageChangeCallback);
    }

    @Override
    public void onPause() {
        super.onPause();

        mDataBinding.viewPager.unregisterOnPageChangeCallback(mViewPager2OnPageChangeCallback);

    }
}
