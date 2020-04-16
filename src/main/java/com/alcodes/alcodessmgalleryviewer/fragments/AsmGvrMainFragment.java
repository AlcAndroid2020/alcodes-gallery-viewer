package com.alcodes.alcodessmgalleryviewer.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

    public static final String EXTRA_STRING_ARRAY_FILE_URI = "EXTRA_STRING_ARRAY_FILE_URI";

    private NavController mNavController;
    private AsmGvrFragmentMainBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private AsmGvrMainViewPagerAdapter mAdapter;
    private ViewPager2.OnPageChangeCallback mViewPager2OnPageChangeCallback;
    private List<String> data;
    private int getThemeData = 0;
    private int color;
    private ActionBar mActionBar;
    public static final String EXTRA_INTEGER_SELECTED_THEME = "EXTRA_INTEGER_SELECTED_THEME";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        mActionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();

        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Init data binding;
        mDataBinding = AsmGvrFragmentMainBinding.inflate(inflater, container, false);

        return mDataBinding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        //Init data to prevent null pointer exception
        data = new ArrayList<>();


        //testing uri by using uri from file picker
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();


        if (bundle != null) {
            //Get File From Previous Main Module Fragment
            if (bundle.getStringArrayList(EXTRA_STRING_ARRAY_FILE_URI) != null) {
                data = bundle.getStringArrayList(EXTRA_STRING_ARRAY_FILE_URI);
                getThemeData = bundle.getInt(EXTRA_INTEGER_SELECTED_THEME);
            }

            //getcolor
            if (bundle != null)
                if (bundle.getInt("color") != 0)
                    color = bundle.getInt("color");
        }

        // Init view model.
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        mMainSharedViewModel.setInternetStatusData(isConnected());
        initIsNetworkConnectedListener();

        // Init adapter and view pager.
        mAdapter = new AsmGvrMainViewPagerAdapter(this, data);

        mViewPager2OnPageChangeCallback = new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Set fragment number in to menu bar
                if (getThemeData != 2)
                    mActionBar.setTitle((position + 1) + "/" + data.size());
                else
                    mActionBar.setTitle("");

                //get internet status from shared view model
                mMainSharedViewModel.getInternetStatusDataLiveData().observe(getViewLifecycleOwner(), new Observer<AsmGvrMainSharedViewModel.InternetStatusData>() {
                    @Override
                    public void onChanged(AsmGvrMainSharedViewModel.InternetStatusData internetStatusData) {
                        if (!internetStatusData.internetStatus) {
                            Toast.makeText(getActivity(), mMainSharedViewModel.getInternetStatusDataLiveData().getValue().statusMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


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

        //set color
        if (color != 0)
            mMainSharedViewModel.setmColorSelectedLiveData(color);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());

        return activeNetwork != null && activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public void initIsNetworkConnectedListener() {
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
