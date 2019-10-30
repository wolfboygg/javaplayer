package com.wolfg.javaplayer.test;

import android.content.Context;
import android.opengl.GLES20;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wolfgg.filterlibrary.egl.EGLHelper;

public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private EGLHelper eglHelper = new EGLHelper();

    public TestSurfaceView(Context context) {
        this(context, null);
    }

    public TestSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TestSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, final int width, final int height) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                eglHelper.initEGL(holder.getSurface(), null);
                while (true) {
                    GLES20.glViewport(0, 0, width, height);
                    GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    eglHelper.swapBuffers();
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
