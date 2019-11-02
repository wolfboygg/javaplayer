package com.wolfgg.filterlibrary.texture;

import android.content.Context;
import android.util.AttributeSet;

import com.wolfgg.filterlibrary.render.WolfMutiRender;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

public class WolfMutiSurfaceView extends WolfEGLSurfaceView {

    private WolfMutiRender mWolfMutiRender;

    public WolfMutiSurfaceView(Context context) {
        this(context, null);
    }

    public WolfMutiSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfMutiSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWolfMutiRender = new WolfMutiRender(context);
        setRender(mWolfMutiRender);
    }

    public void setTextureId(int textureId) {
        mWolfMutiRender.setTextureId(textureId);
    }

}
