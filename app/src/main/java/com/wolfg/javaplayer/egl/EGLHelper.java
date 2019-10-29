package com.wolfg.javaplayer.egl;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;

/**
 * 搭建egl环境
 */

public class EGLHelper {

    private static final String TAG = "EGLHelper";

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLConfig mEGLConfig = null;


    public int init(Surface surface) {

        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.i(TAG, "eglGetDisplay create error");
            return -1;
        }

        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            Log.i(TAG, "eglInitialize error");
            return -1;
        }

        int[] config_attrib_list = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 8,
                EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] eglConfigs = new EGLConfig[1];
        int[] numConfig = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, config_attrib_list, 0, eglConfigs, 0, eglConfigs.length, numConfig, 0)) {
            Log.i(TAG, "eglChooseConfig error");
            return -1;
        }
        mEGLConfig = eglConfigs[0];

        int[] attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attrib_list, 0);
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
            Log.i(TAG, "eglCreateContext error");
            return -1;
        }

        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttribs, 0);
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            Log.i(TAG, "eglCreateWindowSurface error");
            return -1;
        }

        if (!EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext)) {
            Log.i(TAG, "eglMakeCurrent error");
            return -1;
        }

        Log.i(TAG, "SUCCESS");
        return 0;
    }

    public int swapBuffers() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY && mEGLSurface != EGL14.EGL_NO_SURFACE) {
            if (EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
                return 0;
            }
        }
        return -1;
    }

    public void destory() {
        Log.i(TAG, "destory egl");
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        }
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY && mEGLSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
            mEGLSurface = EGL14.EGL_NO_SURFACE;
        }
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY && mEGLContext != EGL14.EGL_NO_CONTEXT) {
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            mEGLContext = EGL14.EGL_NO_CONTEXT;
        }
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglTerminate(mEGLDisplay);
            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        }
    }

}
