package com.wolfgg.filterlibrary.render;

import android.opengl.GLES20;

import com.wolfgg.filterlibrary.BuildConfig;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

public class WolfRender implements WolfEGLSurfaceView.WolfGLRender {

    private static final String TAG = "WolfRender";

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "onDrawframe.......");
        }
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }
}
