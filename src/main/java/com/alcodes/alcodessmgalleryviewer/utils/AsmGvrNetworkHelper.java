package com.alcodes.alcodessmgalleryviewer.helper;

import android.content.Context;
import android.net.Uri;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.ProxyCacheUtils;
import com.danikula.videocache.file.FileNameGenerator;

public class AsmGvrNetworkHelper {

    private static HttpProxyCacheServer mProxy;

    public static HttpProxyCacheServer getProxyInstance(Context context) {
        if (mProxy == null) {
            synchronized (AsmGvrNetworkHelper.class) {
                if (mProxy == null) {
                    AsmGvrNetworkHelper.mProxy = AsmGvrNetworkHelper.newProxy(context);
                }
            }
        }

        return mProxy;
    }

    private static HttpProxyCacheServer newProxy(Context context) {
        return new HttpProxyCacheServer.Builder(context.getApplicationContext())
                .maxCacheSize(1024 * 1024 * 1024)
                .fileNameGenerator(new FileNameGenerator() {
                    @Override
                    public String generate(String url) {
                        Uri uri = Uri.parse(url);
                        String videoId = uri.getQueryParameter("videoId");
                        return videoId + ".mp4";
                    }
                })
                .build();
    }

}
