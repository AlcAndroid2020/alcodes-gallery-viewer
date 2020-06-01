package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.alcodes.alcodessmgalleryviewer.R;

import java.io.File;


public class AsmGvrMainSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mViewPagerPositionLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> mColorSelectedLiveData=new MutableLiveData<>();
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

    public LiveData<Integer> getColorSelectedLiveData() {
        return mColorSelectedLiveData;
    }
    public void setmColorSelectedLiveData(int color) {
        mColorSelectedLiveData.setValue(color);
    }

    public void setInternetStatusData(boolean status) {
        InternetStatusData dataHolder = new InternetStatusData();
        dataHolder.internetStatus = status;
        if (status) {
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_internet_access);
        } else{
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_no_internet_access);
        }

        if(mInternetStatusData.getValue() == null){
            mInternetStatusData.setValue(dataHolder);
        }else{
            mInternetStatusData.postValue(dataHolder);
        }
    }

    public LiveData<InternetStatusData> getInternetStatusDataLiveData() {
        return mInternetStatusData;
    }

}

