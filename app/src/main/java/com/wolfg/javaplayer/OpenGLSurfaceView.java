package com.wolfg.javaplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wolfg.javaplayer.opengl.OpenGLController;

public class OpenGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private OpenGLController mOpenGLController;
    private OnSurfaceListener mOnSurfaceListener;

    public void setOnSurfaceListener(OnSurfaceListener onSurfaceListener) {
        mOnSurfaceListener = onSurfaceListener;
    }

    public void setOpenGLController(OpenGLController openGLController) {
        mOpenGLController = openGLController;
    }

    public OpenGLSurfaceView(Context context) {
        this(context, null);
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public OpenGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        getHolder().addCallback(this);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mOpenGLController != null) {
            mOpenGLController.onCreateSurface(holder.getSurface());
            if (mOnSurfaceListener != null) {
                mOnSurfaceListener.init();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mOpenGLController != null) {
            mOpenGLController.onChangeSurface(width, height);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mOpenGLController != null) {
            mOpenGLController.onDestroySurface();
        }
    }

    public interface OnSurfaceListener {
        void init();
    }


}
