package com.alcodes.alcodessmgalleryviewer.utils;

import java.text.DecimalFormat;

public class AsmGvrFileDetailsHelper {

    public AsmGvrFileDetailsHelper() {
    }

    public String fileSizeBytesConverter(long size, String convertTo) {
        if (size <= 0)
            return "0";

        int digitGroups = 0;
        if(convertTo.equals("MB")){
            digitGroups = 2;
        }else if(convertTo.equals("KB")) {
            digitGroups = 1;
        }

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + convertTo;
    }

    public String createTimeLabel(int time) {
        String timelabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timelabel = min + ":";
        if (sec < 10)
            timelabel += "0";
        timelabel += sec;
        return timelabel;
    }
}