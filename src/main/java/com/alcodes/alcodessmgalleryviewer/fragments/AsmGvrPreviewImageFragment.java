package com.alcodes.alcodessmgalleryviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentPreviewImageBinding;
import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import timber.log.Timber;

public class AsmGvrPreviewImageFragment extends Fragment {

    private static final String ARG_INT_PAGER_POSITION = "ARG_INT_PAGER_POSITION";

    private NavController mNavController;
    private AsmGvrFragmentPreviewImageBinding mDataBinding;
    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int mViewPagerPosition;

    public AsmGvrPreviewImageFragment() {
    }

    public static AsmGvrPreviewImageFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt(ARG_INT_PAGER_POSITION, position);

        AsmGvrPreviewImageFragment fragment = new AsmGvrPreviewImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBinding = AsmGvrFragmentPreviewImageBinding.inflate(inflater, container, false);

        return mDataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init navigation component.
        mNavController = Navigation.findNavController(requireParentFragment().requireView());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Extract arguments.
        mViewPagerPosition = requireArguments().getInt(ARG_INT_PAGER_POSITION);

        mDataBinding.textViewDemo.setText(String.format(Locale.ENGLISH, "Position: %d", mViewPagerPosition));

        // Init view model.
        mMainSharedViewModel = new ViewModelProvider(
                mNavController.getBackStackEntry(R.id.asm_gvr_nav_main),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())
        ).get(AsmGvrMainSharedViewModel.class);

        mMainSharedViewModel.getViewPagerPositionLiveData().observe(getViewLifecycleOwner(), new Observer<Integer>() {

            @Override
            public void onChanged(Integer integer) {
                if (integer != null) {
                    if (integer == mViewPagerPosition) {
                        // TODO this page has been selected.
                        Timber.e("d;;Image fragment: page has been selected at: %s", mViewPagerPosition);
                    } else {
                        // TODO this page has been de-selected.
                        Timber.e("d;;Image fragment: page has been de-selected at: %s", mViewPagerPosition);
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Timber.e("d;;Child fragment at: %s entering onResume", mViewPagerPosition);
    }

    @Override
    public void onPause() {
        super.onPause();

        Timber.e("d;;Child fragment at: %s entering onPause", mViewPagerPosition);
    }
}
