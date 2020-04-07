package com.alcodes.alcodessmgalleryviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrViewPagerAdapter;

public class AsmGvrViewPagerFragment extends Fragment {

    public static final String TAG = AsmGvrViewPagerFragment.class.getSimpleName();

    private String[] imageUrls = new String[] {
            "https://i.pinimg.com/236x/64/84/6d/64846daa5a346126ef31c3f1fcbc4703--winter-wallpapers-wallpapers-ipad.jpg",
            "https://images.wallpaperscraft.com/image/snow_snowflake_winter_form_pattern_49405_240x320.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Rotating_earth_%28large%29.gif/300px-Rotating_earth_%28large%29.gif",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "https://files.eric.ed.gov/fulltext/ED573583.pdf"
    };

    public AsmGvrViewPagerFragment() {
    }

    public static AsmGvrViewPagerFragment newInstance() {
        return new AsmGvrViewPagerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.asm_gvr_fragment_view_pager, container, false);

        final ViewPager viewPager = view.findViewById(R.id.view_pager);
        final AsmGvrViewPagerAdapter adapter = new AsmGvrViewPagerAdapter(getContext(), imageUrls);
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Toast.makeText(getContext(), "viewpager position " + viewPager.getChildCount(), Toast.LENGTH_SHORT).show();
                adapter.resetBackForwardPagerView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
