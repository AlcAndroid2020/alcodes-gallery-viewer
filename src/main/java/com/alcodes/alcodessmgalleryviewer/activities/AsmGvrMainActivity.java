package com.alcodes.alcodessmgalleryviewer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrActivityMainBinding;

public class AsmGvrMainActivity extends AppCompatActivity {

    private AsmGvrActivityMainBinding mDataBinding;
    private NavController mNavController;
    public static final String EXTRA_INTEGER_SELECTED_THEME = "EXTRA_INTEGER_SELECTED_THEME";
    public static final String EXTRA_STRING_GET_DEFAULT_THEME = "EXTRA_STRING_GET_DEFAULT_THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            //Get File From Previous Main Module Fragment
            if(bundle.getInt(EXTRA_INTEGER_SELECTED_THEME) != -1){
                if (bundle.getInt(EXTRA_INTEGER_SELECTED_THEME) == 1){
                    setTheme(R.style.asm_gvr_apps_theme_semi_transparent);
                }else if (bundle.getInt(EXTRA_INTEGER_SELECTED_THEME) == 2){
                    setTheme(R.style.asm_gvr_apps_theme_transparent);
                }else {
                    setTheme(bundle.getInt(EXTRA_STRING_GET_DEFAULT_THEME));
                }
            }

        }


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

