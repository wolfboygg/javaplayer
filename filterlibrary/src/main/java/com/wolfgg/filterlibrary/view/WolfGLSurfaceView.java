package com.wolfgg.filterlibrary.view;

import android.content.Context;
import android.util.AttributeSet;

import com.wolfgg.filterlibrary.render.WolfRender;

/**
 * 单纯的测试用来
 */

public class WolfGLSurfaceView extends WolfEGLSurfaceView {
    public WolfGLSurfaceView(Context context) {
        this(context, null);
    }

    public WolfGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRender(new WolfRender());
        setRenderMode(WolfEGLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
