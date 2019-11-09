package com.wolfgg.filterlibrary.texture;

import android.content.Context;
import android.util.AttributeSet;

import com.wolfgg.filterlibrary.render.FBOGray;
import com.wolfgg.filterlibrary.render.FBOScale;
import com.wolfgg.filterlibrary.render.FBOSplitScreen2Render;
import com.wolfgg.filterlibrary.render.FBOSplitScreen4Render;
import com.wolfgg.filterlibrary.render.FBOSplitScreen9Render;
import com.wolfgg.filterlibrary.render.WolfTextureSpliteRender;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

public class WolfGLSpliteTextureView extends WolfEGLSurfaceView {

    private WolfTextureSpliteRender mWolfTextureSpliteRender;

    public WolfGLSpliteTextureView(Context context) {
        this(context, null);
    }

    public WolfGLSpliteTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfGLSpliteTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWolfTextureSpliteRender = new WolfTextureSpliteRender(context);
        mWolfTextureSpliteRender.setFBORender(new FBOGray(context));
        setRender(mWolfTextureSpliteRender);
        setRenderMode(WolfEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
