package com.alcodes.alcodessmgalleryviewer.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrFragmentMainBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AsmGvrMainFragment extends Fragment {

    private AsmGvrFragmentMainBinding mDataBinding;

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
    }
}
