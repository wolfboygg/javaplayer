package com.wolfgg.filterlibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wolfgg.filterlibrary.egl.EGLHelper;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 创建一个自己的GLSurfaceView,内部有一个线程用来进行绘制，可以设置线程的刷新模式等等
 * <p>
 * 将方法回调到render中进行绘制
 */

public class WolfEGLSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Surface mSurface;
    private EGLContext mEGLContext;

    private WolfGLRender mWolfGLRender;
    private WolfEGLThread mWolfEGLThread;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private int mRenderMode = RENDERMODE_CONTINUOUSLY;

    public WolfEGLSurfaceView(Context context) {
        this(context, null);
    }

    public WolfEGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfEGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        getHolder().addCallback(this);
    }


    public void setSurfaceAndEglContext(Surface surface, EGLContext eglContext) {
        this.mSurface = surface;
        this.mEGLContext = eglContext;
    }

    public void setRender(WolfGLRender wolfGLRender) {
        mWolfGLRender = wolfGLRender;
    }

    public void setRenderMode(int renderMode) {
        if (mWolfGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        mRenderMode = renderMode;
    }

    public void requestRender() {
        if (mWolfEGLThread != null) {
            mWolfEGLThread.requestRender();
        }
    }

    public EGLContext getEGLContext() {
        if (mWolfEGLThread != null) {
            return mWolfEGLThread.getEGLContext();
        }
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mSurface == null) {
            mSurface = holder.getSurface();
        }
        mWolfEGLThread = new WolfEGLThread(new WeakReference<WolfEGLSurfaceView>(this));
        mWolfEGLThread.isCreate = true;
        mWolfEGLThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWolfEGLThread.width = width;
        mWolfEGLThread.height = height;
        mWolfEGLThread.isChange = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mWolfEGLThread.onDestroy();
        mWolfEGLThread = null;
        mSurface = null;
        mEGLContext = null;
    }

    /**
     * 一个静态内部类，用来执行绘制操作
     */
    static class WolfEGLThread extends Thread {
        private WeakReference<WolfEGLSurfaceView> mWolfEGLSurfaceViewWeakReference;
        private EGLHelper mEGLHelper = null;
        private Object mObject = null;

        private boolean isStart = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isExit = false;

        private int width;
        private int height;


        public WolfEGLThread(WeakReference<WolfEGLSurfaceView> wolfEGLSurfaceViewWeakReference) {
            this.mWolfEGLSurfaceViewWeakReference = wolfEGLSurfaceViewWeakReference;
        }

        @Override
        public void run() {
            isExit = false;
            isStart = false;
            mObject = new Object();
            mEGLHelper = new EGLHelper();
            mEGLHelper.initEGL(mWolfEGLSurfaceViewWeakReference.get().mSurface, mWolfEGLSurfaceViewWeakReference.get().mEGLContext);
            // 开始进行绘制
            while (true) {
                if (isExit) {
                    release(); // 释放资源
                    break;
                }

                if (isStart) { // 用来进行判断是否可以进行绘制 刚开始的需要进行两次绘制才能渲染出颜色
                    // 这里进行处理是无限绘制还是手动进行绘制
                    if (mWolfEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_WHEN_DIRTY) { // 手动绘制模式
                        synchronized (mObject) {
                            try {
                                mObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (mWolfEGLSurfaceViewWeakReference.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        // 设置每秒绘制多少帧 1s 60fps
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }

                onCreate();
                onChange(width, height);
                onDraw(); // 持续进行绘制

                isStart = true;
            }
        }

        private void onCreate() {
            if (isCreate && mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender != null) {
                isCreate = false;
                mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender.onSurfaceCreated();
            }
        }

        private void onChange(int width, int height) {
            if (isChange && mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender != null) {
                isChange = true;
                mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender.onSurfaceChange(width, height);
            }
        }

        private void onDraw() {
            // 需要绘制两次才能显示出来
            if (mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender != null && mEGLHelper != null) {
                mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender.onDrawFrame();
                // 为了兼容5.0一下的设备
                if (!isStart) { // 第一次需要进行两次绘制
                    mWolfEGLSurfaceViewWeakReference.get().mWolfGLRender.onDrawFrame();
                }
                mEGLHelper.swapBuffers(); // 交换数据
            }
        }

        private void requestRender() {
            if (mObject != null) {
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        }

        public void onDestroy() {
            isExit = true;
            requestRender();
        }

        private void release() {
            if (mEGLHelper != null) {
                mEGLHelper.destroy();
                mEGLHelper = null;
                mObject = null;
                mWolfEGLSurfaceViewWeakReference = null;
            }
        }

        public EGLContext getEGLContext() {
            if (mEGLHelper != null) {
                return mEGLHelper.getEGLContext();
            }
            return null;
        }


    }


    public interface WolfGLRender {
        void onSurfaceCreated();

        void onSurfaceChange(int width, int height);

        void onDrawFrame();
    }


}
