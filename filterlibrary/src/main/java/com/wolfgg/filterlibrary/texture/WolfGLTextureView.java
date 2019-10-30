package com.wolfgg.filterlibrary.texture;

import android.content.Context;
import android.util.AttributeSet;

import com.wolfgg.filterlibrary.render.WolfTextureRender;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

/**
 * 用来绘制纹理
 */

public class WolfGLTextureView extends WolfEGLSurfaceView {

    private WolfTextureRender mWolfTextureRender;

    public WolfGLTextureView(Context context) {
        this(context, null);
    }

    public WolfGLTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfGLTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWolfTextureRender = new WolfTextureRender(this.getContext());
        setWolfGLRender(mWolfTextureRender);
        setRenderMode(WolfEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


}
