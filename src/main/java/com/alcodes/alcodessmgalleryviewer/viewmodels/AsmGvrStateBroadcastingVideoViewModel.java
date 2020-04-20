package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alcodes.alcodessmgalleryviewer.R;
import com.alcodes.alcodessmgalleryviewer.views.AsmGvrCircularProgressBar;
import com.danikula.videocache.HttpProxyCacheServer;

import java.util.ArrayList;
import java.util.List;

public class AsmGvrStateBroadcastingVideoViewModel extends AndroidViewModel {
    private final MutableLiveData<List<VideoViewModel>> mViewPagerVideoViewLiveData = new MutableLiveData<>();
    private HttpProxyCacheServer httpProxyCacheServer;
    private AsmGvrCircularProgressBar mCircularProgressBar;

    public AsmGvrStateBroadcastingVideoViewModel(@NonNull Application application) {
        super(application);
    }

    public void initHttpProxyCacheServer(Context context){
        httpProxyCacheServer = new HttpProxyCacheServer.Builder(context)
                .maxCacheSize(1024 * 1024 * 1024)
                .build();
    }

    public HttpProxyCacheServer getHttpProxyCacheServer() {
        return httpProxyCacheServer;
    }

    public AsmGvrCircularProgressBar getCircularProgressBar() {
        return mCircularProgressBar;
    }

    public void setCircularProgressBar(Context context) {
        mCircularProgressBar = new AsmGvrCircularProgressBar(context);
    }

    public VideoViewModel getViewPagerVideoViewCurrentPlayingPosition(int mViewPagerPosition) {
        Boolean isNoMatchRecords = true;
        VideoViewModel videoViewModel = new VideoViewModel();
        if(mViewPagerVideoViewLiveData.getValue() != null && mViewPagerVideoViewLiveData.getValue().size() != 0){
            // Check for existing records for video played halfway
            for(VideoViewModel records : mViewPagerVideoViewLiveData.getValue()){
                if(records.viewPagerPosition == mViewPagerPosition){
                    videoViewModel = records;
                    isNoMatchRecords = false;
                    break;
                }
            }
            // Check for existing records for video played halfway
            // Not records found
            if(isNoMatchRecords){
                videoViewModel.currentPlayingPosition = -1;
                videoViewModel.viewPagerPosition = -1;
            }
            // Not records found
        }else{
            //Values -1 for null value store in Live Data
            videoViewModel.currentPlayingPosition = -1;
            videoViewModel.viewPagerPosition = -1;
        }

        return videoViewModel;
    }

    public void setViewPagerVideoViewLiveData(int viewPagerPosition, int currentPlayingPosition) {
        //Declare variables for Live Data usage
        Boolean isPresentRecord = false;
        List<VideoViewModel> videoViewModels;
        VideoViewModel videoViewModel = new VideoViewModel();
        videoViewModel.viewPagerPosition = viewPagerPosition;
        videoViewModel.currentPlayingPosition = currentPlayingPosition;
        //Declare variables for Live Data usage

        //If Live Data is empty
        if(mViewPagerVideoViewLiveData.getValue() == null){
            videoViewModels = new ArrayList<>();
            videoViewModels.add(videoViewModel);
        }//If Live Data is empty
        //IF Live Data is not empty
        else{
            videoViewModels = mViewPagerVideoViewLiveData.getValue();
            // If present records then update the current video playing position
            for(int i=0; i < videoViewModels.size();i++){
                if(videoViewModels.get(i).viewPagerPosition == viewPagerPosition){
                    isPresentRecord = true;
                    videoViewModels.get(i).currentPlayingPosition = currentPlayingPosition;
                    break;
                }
            }
            // If present records then update the current video playing position
            // If not present then add new record
            if(!isPresentRecord){
                videoViewModels.add(videoViewModel);
            }
            // If not present then add new record
        }
        //IF Live Data is not empty

        mViewPagerVideoViewLiveData.setValue(videoViewModels);
    }

    public class VideoViewModel{
        public int viewPagerPosition;
        public int currentPlayingPosition;
    }
}