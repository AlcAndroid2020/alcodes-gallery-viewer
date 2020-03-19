package com.alcodes.alcodessmgalleryviewer.activities;

import android.os.Bundle;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrActivityMainBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class AsmGvrMainActivity extends AppCompatActivity {

    private AsmGvrActivityMainBinding mDataBinding;
    private NavController mNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init data binding.
        mDataBinding = AsmGvrActivityMainBinding.inflate(getLayoutInflater());

        setContentView(mDataBinding.getRoot());

        // Init navigation components.
        mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupActionBarWithNavController(this, mNavController);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return mNavController.navigateUp() || super.onSupportNavigateUp();
    }
}
