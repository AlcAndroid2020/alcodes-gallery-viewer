package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AsmGvrMainSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mViewPagerPositionLiveData = new MutableLiveData<>(0);

    private boolean internetStatus = false;
    private int audioProgress;

    public AsmGvrMainSharedViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getViewPagerPositionLiveData() {
        return mViewPagerPositionLiveData;
    }

    public void setViewPagerCurrentPagePosition(int position) {
        mViewPagerPositionLiveData.setValue(position);
    }

    public void setInternetStatus(boolean status) {
        internetStatus = status;
        Log.e("test", String.valueOf(internetStatus));
    }

    public String getInternetStatusString() {
        if (internetStatus)
            return "Internet access";
        else
            return "No internet access.";
    }

    public boolean getInternetStatus() {
        return internetStatus;
    }


    public int getAudioProgress(){

        return audioProgress;
    }
    public void setAudioPogress(int progress){
         audioProgress=progress;
    }
}
