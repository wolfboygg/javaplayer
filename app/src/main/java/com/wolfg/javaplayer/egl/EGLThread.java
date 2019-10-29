package com.wolfg.javaplayer.egl;

import android.util.Log;
import android.view.Surface;

public class EGLThread implements Runnable {

    private static final String TAG = "EGLThread";

    public static final int OPENGL_RENDER_AUTO = 1;
    public static final int OPENGL_RENDER_HANDLER = 2;

    private int renderType = OPENGL_RENDER_AUTO;

    private Thread mGLThread = null;
    private Surface mSurface;


    private boolean isCreate = false;
    private boolean isChange = false;
    private boolean isExit = false;
    private boolean isStart = false;
    private boolean isChangeFilter = false;

    private Object mLock = new Object();

    private OnRenderListener mOnRenderListener;

    int surface_width = 0;
    int surface_height = 0;

    private Object mContext;

    public void setContext(Object context) {
        mContext = context;
    }

    public void setOnRenderListener(OnRenderListener onRenderListener) {
        mOnRenderListener = onRenderListener;
    }

    public void onSurfaceCreate(Surface surface, EGLThread eglThread) {
        if (mGLThread == null) {
            isCreate = true;
            mSurface = surface;
            mGLThread = new Thread(eglThread);
            mGLThread.start();
        }

    }

    public void onSurfaceChange(int width, int height) {
        isChange = true;
        surface_width = width;
        surface_height = height;
        notifyRender();
    }

    public void onSurfaceChangeFilter() {
        isChangeFilter = true;
        notifyRender();
    }

    public void setRenderType(int type) {
        this.renderType = type;
    }

    public void notifyRender() {
        Log.i(TAG, "notifyRender");
        synchronized (mLock) {
            mLock.notifyAll();
        }
    }

    public void destroy() {
        isExit = true;
        notifyRender();
    }




    @Override
    public void run() {
        EGLHelper eglHelper = new EGLHelper();
        int code = eglHelper.init(mSurface);
        Log.i(TAG, "egl init code is = " + code);
        isExit = false;
        while (true) {
            if (isCreate) {
                Log.i(TAG, "eglThread call surfaceCreate");
                isCreate = false;
                mOnRenderListener.callbackOnCreate(mContext);
            }

            if (isChange) {
                Log.i(TAG, "eglThread call surfaceChange");
                isChange = false;
                isStart = true;
                mOnRenderListener.callbackOnChange(mContext, surface_width, surface_height);
            }

            Log.i(TAG, "draw");

            if (isChangeFilter) {
                isChangeFilter = false;
                mOnRenderListener.callbackOnChangeFilter(mContext, surface_width, surface_height);
            }

            if (isStart) {
                mOnRenderListener.callbackOnDraw(mContext);
                eglHelper.swapBuffers();
            }

            if (renderType == OPENGL_RENDER_AUTO) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    synchronized (mLock) {
                        mLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (isExit) {
                // 释放资源
                mOnRenderListener.callbackOnDestroy(mContext);
                eglHelper.destory();
                eglHelper = null;
                break;
            }


        }

    }


    public interface OnRenderListener {
        void callbackOnCreate(Object ctx);

        void callbackOnChange(Object ctx, int width, int height);

        void callbackOnDraw(Object ctx);

        void callbackOnChangeFilter(Object ctx, int width, int height);

        void callbackOnDestroy(Object ctx);
    }


}
