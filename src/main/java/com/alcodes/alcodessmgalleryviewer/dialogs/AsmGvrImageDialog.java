package com.alcodes.alcodessmgalleryviewer.dialogs;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alcodes.alcodessmgalleryviewer.databinding.AsmGvrDialogImageOptionBinding;
import com.alcodes.alcodessmgalleryviewer.databinding.bindingcallbacks.AsmGvrDialogImageOptionCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import java.io.IOException;

public class AsmGvrImageDialog extends DialogFragment implements AsmGvrDialogImageOptionCallback {
    public static final String TAG = AsmGvrImageDialog.class.getSimpleName();

    private static final String ARG_STRING_IMAGE_URL = "ARG_STRING_IMAGE_URL";

    private String mImageUrl;

    private AsmGvrDialogImageOptionBinding mDataBinding;

    public AsmGvrImageDialog() {}

    public static AsmGvrImageDialog newInstance(String imageUrl){
        Bundle args = new Bundle();

        args.putString(ARG_STRING_IMAGE_URL, imageUrl);

        AsmGvrImageDialog imageDialog = new AsmGvrImageDialog();
        imageDialog.setArguments(args);

        return imageDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        mImageUrl = "";

        if(args != null){
            mImageUrl = args.getString(ARG_STRING_IMAGE_URL, "");
        }

        mDataBinding = AsmGvrDialogImageOptionBinding.inflate(requireActivity().getLayoutInflater());
        mDataBinding.setBindingCallback(this);

        MaterialDialog.Builder builder = new MaterialDialog.Builder(requireActivity());

        builder.customView(mDataBinding.getRoot(), true);

        setCancelable(true);

        return builder.build();
    }


    @Override
    public void onBtnOpenImageFileOnWebBrowserClick() {
        //Open Browser
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mImageUrl));
        requireActivity().startActivity(browserIntent);

        //Close the dialog
        this.dismiss();
    }

    @Override
    public void onBtnSetWallpaperClick() {
        final Context currentContext = requireContext();

        //To get bitmap and set it as wallpaper with Wallpaper Manager
        Glide.with(currentContext).asBitmap().load(Uri.parse(mImageUrl)).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                try {
                    WallpaperManager.getInstance(currentContext).setBitmap(resource);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }).submit();

        //Close the dialog
        this.dismiss();
    }
}
