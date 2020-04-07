package com.alcodes.alcodessmgalleryviewer.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.fragments.AsmGvrViewPagerFragment;

public class AsmGvrViewPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asm_gvr_activity_view_pager);

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(AsmGvrViewPagerFragment.TAG) == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.framelayout_fragment_view_pager_holder,AsmGvrViewPagerFragment.newInstance(), AsmGvrViewPagerFragment.TAG)
                    .commit();
        }

    }
}
