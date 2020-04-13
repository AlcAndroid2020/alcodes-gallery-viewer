package com.alcodes.alcodessmgalleryviewer.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.adapters.AsmGvrMainViewPagerAdapter;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentMainBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.util.ArrayList;
import java.util.List;


public class AsmGvrMainFragment extends Fragment {

    private NavController mNavController;
    private AsmGvrFragmentMainBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrMainViewPagerAdapter mAdapter;
    private ViewPager2.OnPageChangeCallback mViewPager2OnPageChangeCallback;

    private ActionBar mActionBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Init data binding;
        mDataBinding = AsmGvrFragmentMainBinding.inflate(inflater, container, false);

        mActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        mActionBar.setShowHideAnimationEnabled(true);

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
        Uri uri = null;
        List<String> UriList = new ArrayList<String>();

        //testing uri by using uri from file picker
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            //get single file
            if (bundle.getString("uri") != null)
                uri = Uri.parse(bundle.getString("uri"));

            //get multiple file
            if (bundle.getStringArrayList("urilist") != null)
                UriList = bundle.getStringArrayList("urilist");
        }


        // Init view model.
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        initIsNetworkConnectedListener();
        mMainSharedViewModel.setInternetStatusData(isConnected());

        // Init adapter data.
        List<String> data = new ArrayList<>();
        data.add("https://www.w3.org/TR/PNG/iso_8859-1.txt");
        data.add("https://i.pinimg.com/236x/64/84/6d/64846daa5a346126ef31c3f1fcbc4703--winter-wallpapers-wallpapers-ipad.jpg");
        data.add("https://upload.wikimedia.org/wikipedia/commons/3/38/Tampa_FL_Sulphur_Springs_Tower_tall_pano01.jpg");
        data.add("https://www.appears-itn.eu/wp-content/uploads/2018/07/long-300x86.jpg");
        data.add("https://images.wallpaperscraft.com/image/snow_snowflake_winter_form_pattern_49405_240x320.jpg");
        data.add("https://media.giphy.com/media/Pm4ZMaevvoGhXlm714/giphy.gif");
        data.add("https://upload.wikimedia.org/wikipedia/commons/thumb/2/2c/Rotating_earth_%28large%29.gif/300px-Rotating_earth_%28large%29.gif");
        data.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3");
        data.add("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        data.add("https://files.eric.ed.gov/fulltext/ED573583.pdf");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4");
        data.add("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4" );

        // Init Local File Uri

        //single file
        if (uri != null) {
            data.add(String.valueOf(uri));
        }

        // Multiple File
        if (UriList != null) {
            for(int i=0;i<UriList.size();i++){
                data.add(UriList.get(i));

            }
        }

        // Init adapter and view pager.
        mAdapter = new AsmGvrMainViewPagerAdapter(this, data);

        mViewPager2OnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Set fragment number in to menu bar
                mActionBar.setTitle((position + 1) + "/" + data.size());

                //get internet status from shared view model

                mMainSharedViewModel.setViewPagerCurrentPagePosition(position);
            }
        };

        mDataBinding.viewPager.setAdapter(mAdapter);

        mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
            @Override
            public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                if (!internetStatusData.internetStatus) {

                }
            }
        });
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

    public boolean isConnected(){
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());

        return activeNetwork != null && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public void initIsNetworkConnectedListener(){
        final ConnectivityManager connectivityManager = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();

        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(
                    builder.build(),
                    new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(@NonNull Network network) {
                            mMainSharedViewModel.setInternetStatusData(true);
                        }

                        @Override
                        public void onLost(@NonNull Network network) {
                            mMainSharedViewModel.setInternetStatusData(false);
                        }
                    }
            );
        }
    }
}
