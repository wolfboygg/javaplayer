package com.wolfg.javaplayer.opengl;

import android.graphics.Bitmap;
import android.view.Surface;

import com.wolfg.javaplayer.egl.EGLThread;

public class OpenGLController implements EGLThread.OnRenderListener {

    private EGLThread mEGLThread;
    private BaseOpengl mBaseOpengl;

    private Bitmap mBitmap;

    public void onCreateSurface(Surface surface) {
        mEGLThread = new EGLThread();
        mEGLThread.setRenderType(EGLThread.OPENGL_RENDER_HANDLER);
        mEGLThread.setContext(this);
        mEGLThread.setOnRenderListener(this);

        mBaseOpengl = new FilterYUV();
        mEGLThread.onSurfaceCreate(surface, mEGLThread);
    }

    public void onChangeSurface(int width, int height) {
        if (mEGLThread != null) {
            if (mBaseOpengl != null) {
                mBaseOpengl.surface_width = width;
                mBaseOpengl.surface_height = height;
            }
            mEGLThread.onSurfaceChange(width, height);
        }
    }

    public void onChangeFilter() {
        if (mEGLThread != null) {
            mEGLThread.onSurfaceChangeFilter();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mBaseOpengl.setBitmap(bitmap);
        mEGLThread.notifyRender();
    }


    public void onDestroySurface() {
        if (mEGLThread != null) {
            mEGLThread.destroy();
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }


    @Override
    public void callbackOnCreate(Object ctx) {
        OpenGLController openGLController = (OpenGLController) ctx;
        if (openGLController != null) {
            if (openGLController.mBaseOpengl != null) {
                openGLController.mBaseOpengl.onCreate();
            }
        }
    }

    @Override
    public void callbackOnChange(Object ctx, int width, int height) {
        OpenGLController openGLController = (OpenGLController) ctx;
        if (openGLController != null) {
            if (openGLController.mBaseOpengl != null) {
                openGLController.mBaseOpengl.onChange(width, height);
            }
        }
    }

    @Override
    public void callbackOnDraw(Object ctx) {
        OpenGLController openGLController = (OpenGLController) ctx;
        if (openGLController != null) {
            if (openGLController.mBaseOpengl != null) {
                openGLController.mBaseOpengl.onDraw();
            }
        }
    }

    @Override
    public void callbackOnChangeFilter(Object ctx, int width, int height) {
        // 进行切换滤镜
        OpenGLController openGLController = (OpenGLController) ctx;
        if (openGLController !=  null) {
            if (openGLController.mBaseOpengl != null) {
                openGLController.mBaseOpengl.onDestroy();
                openGLController.mBaseOpengl = null;
            }
            openGLController.mBaseOpengl = new FilterTwo();
            openGLController.mBaseOpengl.onCreate();
            openGLController.mBaseOpengl.onChange(width, height);
            openGLController.mBaseOpengl.setBitmap(mBitmap);
            openGLController.mEGLThread.notifyRender();
        }
    }

    @Override
    public void callbackOnDestroy(Object ctx) {
        OpenGLController openGLController = (OpenGLController) ctx;
        openGLController.mBaseOpengl.onDestroy();
    }


    public void setYUVData(byte[] y, byte[] u, byte[] v, int width, int height) {
        if (mBaseOpengl != null) {
            mBaseOpengl.setYUV(y, u, v, width, height);
        }
        if (mEGLThread != null) {
            mEGLThread.notifyRender();
        }
    }



}
