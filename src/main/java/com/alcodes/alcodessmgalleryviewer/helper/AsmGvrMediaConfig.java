package com.alcodes.alcodessmgalleryviewer.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.Serializable;

public class AsmGvrMediaConfig {
    private int position;
    private String uri;
    private String fileType;
    private Boolean fromInternetSource;
    private ContentResolver cR;

    public AsmGvrMediaConfig() {
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Boolean getFromInternetSource() {
        return fromInternetSource;
    }

    public void setFromInternetSource(Boolean fromInternetSource) {
        this.fromInternetSource = fromInternetSource;
    }

    public String checkUrlAndUriType (Context context, Uri uri){
        Boolean isOnline = false;
        if(cR == null){
            cR = context.getContentResolver();
        }
        if(uri.getScheme().equals("http") | uri.getScheme().equals("https")){
            isOnline = true;
            try{
                fileType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(uri)).toLowerCase());
                fileType = fileType.substring(0, fileType.lastIndexOf("/"));
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else{
            isOnline = false;
            fileType = cR.getType(uri).substring(0, cR.getType(uri).lastIndexOf("/"));
        }
        if(isOnline){
            fileType += "/online";
        }else{
            fileType += "/offline";
        }

        return fileType;
    }
}
