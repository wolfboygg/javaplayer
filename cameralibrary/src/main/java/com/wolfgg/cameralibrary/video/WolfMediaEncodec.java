package com.wolfgg.cameralibrary.video;

import android.content.Context;

public class WolfMediaEncodec extends WolfBaseMediaEncoder {

    private WolfEncodecRender mWolfEncodecRender;

    public WolfMediaEncodec(Context context, int textureId) {
        super(context);
        mWolfEncodecRender = new WolfEncodecRender(context, textureId);
        setRender(mWolfEncodecRender);
        setRenderMode(WolfBaseMediaEncoder.RENDERMODE_CONTINUOUSLY);
    }
}
