package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;
import android.util.Log;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alcodes.alcodessmgalleryviewer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AsmGvrMainSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mViewPagerPositionLiveData = new MutableLiveData<>(0);

    public static class InternetStatusData {
        public boolean internetStatus;
        public String statusMessage;
    }

    private MutableLiveData<InternetStatusData> mInternetStatusData = new MutableLiveData<>();


    public AsmGvrMainSharedViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getViewPagerPositionLiveData() {
        return mViewPagerPositionLiveData;
    }

    public void setViewPagerCurrentPagePosition(int position) {
        mViewPagerPositionLiveData.setValue(position);
    }

    public void setInternetStatusData(boolean status) {
        InternetStatusData dataHolder = new InternetStatusData();
        dataHolder.internetStatus = status;
        if (status) {
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_internet_access);
        } else
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_no_internet_access);
        mInternetStatusData.setValue(dataHolder);
    }

    public LiveData<InternetStatusData> getInternetStatusDataLiveData() {
        return mInternetStatusData;
    }
    private File downloadedFile;

    public File getDowloadProgress(){

        return downloadedFile;
    }
    public void setDownloadPogress(File progress){
        downloadedFile=progress;
    }



    public class VideoViewModel {
        public int viewPagerPosition;
        public int currentPlayingPosition;

    }
}

