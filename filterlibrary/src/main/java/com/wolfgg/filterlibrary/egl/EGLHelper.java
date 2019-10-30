package com.wolfgg.filterlibrary.egl;

import android.opengl.EGL14;
import android.view.Surface;

import com.wolfgg.filterlibrary.BuildConfig;
import com.wolfgg.filterlibrary.utils.LogHelper;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * 创建EGL环境，EGL是Opengl和本地窗口交互的桥梁
 */

public class EGLHelper {

    private static final String TAG = "EGLHelper";

    private EGL10 mEGL10;
    private EGLDisplay mEGLDisplay;
    private EGLContext mEGLContext;
    private EGLSurface mEGLSurface;

    /**
     * @param surface    本地显示窗口
     * @param eglContext 用来共享eglContext来实现共享纹理实现各种滤镜效果
     */
    public void initEGL(Surface surface, EGLContext eglContext) {

        // 1.获取EGL对象
        mEGL10 = (EGL10) EGLContext.getEGL();

        // 2.获取本地显示设备
        mEGLDisplay = mEGL10.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }

        // 3.初始化本地显示设备
        int[] version = new int[2];
        if (!mEGL10.eglInitialize(mEGLDisplay, version)) {
            throw new RuntimeException("eglInitialize failed");
        }

        // 4.获取config对象
        int[] attributes = new int[]{
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 8,
                EGL10.EGL_STENCIL_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE
        };

        int[] num_config = new int[1];
        if (!mEGL10.eglChooseConfig(mEGLDisplay, attributes, null, 1, num_config)) {
            throw new RuntimeException("eglChooseConfig failed");
        }

        int numConfigs = num_config[0];
        if (numConfigs <= 0) {
            throw new IllegalArgumentException("no configs match configSpec");
        }
        // 5.真正获取
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!mEGL10.eglChooseConfig(mEGLDisplay, attributes, configs, numConfigs, num_config)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }

        // 6.获取EGLContext  如果不为空直接使用传入的，否则使用系统的进行初始化
        int[] attrib_list = new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };

        if (eglContext != null) {
            mEGLContext = mEGL10.eglCreateContext(mEGLDisplay, configs[0], mEGLContext, attrib_list);
        } else {
            mEGLContext = mEGL10.eglCreateContext(mEGLDisplay, configs[0], EGL10.EGL_NO_CONTEXT, attrib_list);
        }

        // 7.获取surface
        mEGLSurface = mEGL10.eglCreateWindowSurface(mEGLDisplay, configs[0], surface, null);

        // 8.进行绑定
        if (!mEGL10.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent fail");
        }
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "create egl environment success");
        }
    }

    public boolean swapBuffers() {
        if (mEGL10 != null) {
            return mEGL10.eglSwapBuffers(mEGLDisplay, mEGLSurface);
        } else {
            throw new RuntimeException("egl is null");
        }
    }

    /**
     * 用于共享EGLContext
     * @return
     */
    public EGLContext getEGLContext() {
        return mEGLContext;
    }

    public void destroy() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "destroy egl");
        }
       if (mEGL10 != null) {
           if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
               mEGL10.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
           }
           if (mEGLDisplay != EGL10.EGL_NO_DISPLAY && mEGLSurface != EGL10.EGL_NO_SURFACE) {
               mEGL10.eglDestroySurface(mEGLDisplay, mEGLSurface);
               mEGLSurface = EGL10.EGL_NO_SURFACE;
           }
           if (mEGLDisplay != EGL10.EGL_NO_DISPLAY && mEGLContext != EGL10.EGL_NO_CONTEXT) {
               mEGL10.eglDestroyContext(mEGLDisplay, mEGLContext);
               mEGLContext = EGL10.EGL_NO_CONTEXT;
           }
           if (mEGLDisplay != EGL10.EGL_NO_DISPLAY) {
               mEGL10.eglTerminate(mEGLDisplay);
               mEGLDisplay = EGL10.EGL_NO_DISPLAY;
           }
           mEGL10 = null;
       }
    }


}
