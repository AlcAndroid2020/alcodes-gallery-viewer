package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;
import android.util.Log;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alcodes.alcodessmgalleryviewer.R;
import java.util.ArrayList;
import java.util.List;

public class AsmGvrMainSharedViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> mViewPagerPositionLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<VideoViewModel>> mViewPagerVideoViewLiveData = new MutableLiveData<>();

    public static class InternetStatusData{
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
        if(status){
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_internet_access);
        }else
            dataHolder.statusMessage = getApplication().getResources().getString(R.string.asm_gvr_msg_no_internet_access);
        mInternetStatusData.setValue(dataHolder);
    }

    public LiveData<InternetStatusData> getInternetStatusDataLiveData() {
        return mInternetStatusData;
    }




    public VideoViewModel getViewPagerVideoViewCurrentPlayingPosition(int mViewPagerPosition) {
        Boolean isNoMatchRecords = true;
        VideoViewModel videoViewModel = new VideoViewModel();
        if(mViewPagerVideoViewLiveData.getValue() != null && mViewPagerVideoViewLiveData.getValue().size() != 0){
            for(VideoViewModel records : mViewPagerVideoViewLiveData.getValue()){
                if(records.viewPagerPosition == mViewPagerPosition){
                    videoViewModel = records;
                    isNoMatchRecords = false;
                    break;
                }
            }
            if(isNoMatchRecords){
                videoViewModel.currentPlayingPosition = -1;
                videoViewModel.viewPagerPosition = -1;
            }

        }else{
            //Values -1 for null value store in Live Data
            videoViewModel.currentPlayingPosition = -1;
            videoViewModel.viewPagerPosition = -1;
        }

        return videoViewModel;
    }

    public void setViewPagerVideoViewLiveData(int viewPagerPosition, int currentPlayingPosition) {
        Boolean isPresentRecord = false;
        List<VideoViewModel> videoViewModels;
        VideoViewModel videoViewModel = new VideoViewModel();
        videoViewModel.viewPagerPosition = viewPagerPosition;
        videoViewModel.currentPlayingPosition = currentPlayingPosition;

        if(mViewPagerVideoViewLiveData.getValue() == null){
            videoViewModels = new ArrayList<>();
            videoViewModels.add(videoViewModel);
        }else{
            videoViewModels = mViewPagerVideoViewLiveData.getValue();
            for(int i=0; i < videoViewModels.size();i++){
                if(videoViewModels.get(i).viewPagerPosition == viewPagerPosition){
                    isPresentRecord = true;
                    videoViewModels.get(i).currentPlayingPosition = currentPlayingPosition;
                    break;
                }
            }
            if(!isPresentRecord){
                videoViewModels.add(videoViewModel);
            }
        }

        mViewPagerVideoViewLiveData.setValue(videoViewModels);
    }

    public class VideoViewModel{
        public int viewPagerPosition;
        public int currentPlayingPosition;
    }
}


