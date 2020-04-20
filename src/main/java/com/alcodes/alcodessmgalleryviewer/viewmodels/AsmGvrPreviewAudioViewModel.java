package com.alcodes.alcodessmgalleryviewer.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import wseemann.media.FFmpegMediaMetadataRetriever;

public class AsmGvrPreviewAudioViewModel extends AndroidViewModel {
    private final MutableLiveData<Integer> mViewPagerPositionLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<List<AudioViewModel>> mViewPagerVideoViewLiveData = new MutableLiveData<>();
    private FFmpegMediaMetadataRetriever mFFmpegMMR;
    private Boolean isDetailShown=false;
    public AsmGvrPreviewAudioViewModel(@NonNull Application application) {
        super(application);
    }

    //get current audio position/progress
    public AsmGvrPreviewAudioViewModel.AudioViewModel getViewPagerVideoViewCurrentPlayingPosition(int mViewPagerPosition) {
        Boolean isNoMatchRecords = true;
        AsmGvrPreviewAudioViewModel.AudioViewModel AudioViewModel = new AsmGvrPreviewAudioViewModel.AudioViewModel();
        if (mViewPagerVideoViewLiveData.getValue() != null && mViewPagerVideoViewLiveData.getValue().size() != 0) {
            for (AsmGvrPreviewAudioViewModel.AudioViewModel records : mViewPagerVideoViewLiveData.getValue()) {
                if (records.viewPagerPosition == mViewPagerPosition) {
                    AudioViewModel = records;
                    isNoMatchRecords = false;
                    break;
                }
            }
            if (isNoMatchRecords) {
                AudioViewModel.currentPlayingPosition = -1;
                AudioViewModel.viewPagerPosition = -1;
            }

        } else {
            //Values -1 for null value store in Live Data
            AudioViewModel.currentPlayingPosition = -1;
            AudioViewModel.viewPagerPosition = -1;
        }

        return AudioViewModel;
    }

    //set  audio progress/position
    public void setViewPagerVideoViewLiveData(int viewPagerPosition, int currentPlayingPosition) {
        Boolean isPresentRecord = false;
        List<AudioViewModel> audioViewModels;
        AudioViewModel audioViewModel = new AudioViewModel();
        audioViewModel.viewPagerPosition = viewPagerPosition;
        audioViewModel.currentPlayingPosition = currentPlayingPosition;

        if (mViewPagerVideoViewLiveData.getValue() == null) {
            audioViewModels = new ArrayList<>();
            audioViewModels.add(audioViewModel);
        } else {
            audioViewModels = mViewPagerVideoViewLiveData.getValue();
            for (int i = 0; i < audioViewModels.size(); i++) {
                if (audioViewModels.get(i).viewPagerPosition == viewPagerPosition) {
                    isPresentRecord = true;
                    audioViewModels.get(i).currentPlayingPosition = currentPlayingPosition;
                    break;
                }
            }
            if (!isPresentRecord) {
                audioViewModels.add(audioViewModel);
            }
        }

        mViewPagerVideoViewLiveData.setValue(audioViewModels);
    }

    public FFmpegMediaMetadataRetriever getFFmpegMMR() {
        return mFFmpegMMR;
    }

    public void setFFmpegMMR() {
        mFFmpegMMR = new FFmpegMediaMetadataRetriever();
    }

    public Boolean getDetailShown() {
        return isDetailShown;
    }

    public void setDetailShown(Boolean detailShown) {
        isDetailShown = detailShown;
    }

    public class AudioViewModel {
        public int viewPagerPosition;
        public int currentPlayingPosition;
    }
}
