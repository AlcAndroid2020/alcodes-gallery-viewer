package com.alcodes.alcodessmgalleryviewer;

public class MediaConfig {
    int position;
    String Uri;
    String  fileType;
    boolean fromInternetSource;

    public MediaConfig(int position, String uri, String fileType, boolean fromInternetSource) {
        this.position = position;
        Uri = uri;
        this.fileType = fileType;
        this.fromInternetSource = fromInternetSource;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUri() {
        return Uri;
    }

    public void setUri(String uri) {
        Uri = uri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public boolean isFromInternetSource() {
        return fromInternetSource;
    }

    public void setFromInternetSource(boolean fromInternetSource) {
        this.fromInternetSource = fromInternetSource;
    }
}
