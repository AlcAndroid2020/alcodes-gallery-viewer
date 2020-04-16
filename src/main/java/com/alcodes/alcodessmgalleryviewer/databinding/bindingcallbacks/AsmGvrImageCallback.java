package com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks;


import com.alcodes.alcodessmgalleryviewer.views.AsmGvrTouchImageView;

public interface AsmGvrImageCallback {
    void onTouchShowHideActionBar();
    void onSlideImageDetailUp(AsmGvrTouchImageView imageView);
    void onSlideImageDetailDown(AsmGvrTouchImageView imageView);
}
