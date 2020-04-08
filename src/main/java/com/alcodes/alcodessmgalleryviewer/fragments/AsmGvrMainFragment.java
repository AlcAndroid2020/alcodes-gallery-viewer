package com.alcodes.alcodessmgalleryviewer.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrMainViewPagerAdapter;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentMainBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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
    private String appsName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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

        //Save internet status to shared view model
        mMainSharedViewModel.setInternetStatusData(isConnected());

        //get internet status from shared view model
        Toast.makeText(getActivity(), mMainSharedViewModel.getInternetStatusDataLiveData().getValue().statusMessage, Toast.LENGTH_LONG).show();

        // Init adapter data.

        List<String> data = new ArrayList<>();
        data.add("https://i.pinimg.com/236x/64/84/6d/64846daa5a346126ef31c3f1fcbc4703--winter-wallpapers-wallpapers-ipad.jpg");
        data.add("https://images.wallpaperscraft.com/image/snow_snowflake_winter_form_pattern_49405_240x320.jpg");
        data.add("https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Rotating_earth_%28large%29.gif/300px-Rotating_earth_%28large%29.gif");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        data.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
        data.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        data.add("https://files.eric.ed.gov/fulltext/ED573583.pdf");

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

    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }
}
