package com.alcodes.alcodessmgalleryviewer.views;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.alcodes.alcodessmgalleryviewer.viewmodels.AsmGvrMainSharedViewModel;


public class AsmGvrCircularProgressBar extends CircularProgressDrawable {

    private AsmGvrMainSharedViewModel mMainSharedViewModel;
    private int[] mRgbColor = {
            Color.WHITE,
            Color.CYAN,
            Color.BLUE,
            Color.BLACK
    };

    public AsmGvrCircularProgressBar(@NonNull Context context) {
        super(context);
        //Set width of the Line/Stroke
        setStrokeWidth(10f);

        //Set how big is the progress bar
        setCenterRadius(60f);

        //Set Progress Bar Color
        setColorSchemeColors(mRgbColor);

        //Start it
        start();
    }
}
