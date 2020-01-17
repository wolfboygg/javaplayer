package com.ggwolf.ffmpeglibrary.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import com.ggwolf.ffmpeglibrary.MediaHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PlayView extends GLSurfaceView implements GLSurfaceView.Renderer, Runnable, SurfaceHolder.Callback {
    public PlayView(Context context) {
        this(context, null);
    }

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            setRenderer(this);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    @Override
    public void run() {
        String videoPath = Environment.getExternalStorageDirectory() + "/pcm.mp4";
        MediaHelper.getInstance().openVideo(videoPath, getHolder().getSurface());
    }
}

