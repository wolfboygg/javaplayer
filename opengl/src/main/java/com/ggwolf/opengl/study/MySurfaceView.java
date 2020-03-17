package com.ggwolf.opengl.study;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.ggwolf.opengl.study.MyNativeRender.SAMPLE_TYPE;

public class MySurfaceView extends GLSurfaceView {

    private MyGLRender mGLRender;

    public MySurfaceView(Context context) {
        this(context, null);
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    {
        setEGLContextClientVersion(2);
        mGLRender = new MyGLRender();
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setRenderer(mGLRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY); // 手动进行刷新 调用requestRender()方法的时候进行刷新

    }

    public MyGLRender getGLRender() {
        return mGLRender;
    }


    public static class MyGLRender implements GLSurfaceView.Renderer {

        private MyNativeRender mNativeRender;
        private int mSampleType;

        public MyGLRender() {
            mNativeRender = new MyNativeRender();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mNativeRender.native_OnSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mNativeRender.native_OnSurfaceChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            gl.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
            mNativeRender.native_OnDrawFrame();
        }

        // 需要记录一下我们设置的参数
        public void init() {
            mNativeRender.native_Init();
        }

        public void unInit() {
            mNativeRender.native_UnInit();
        }

        public void setParamsInt(int paramType, int value0, int value1) {
            if (paramType == SAMPLE_TYPE) {
                mSampleType = value0;
            }
            mNativeRender.native_SetParamsInt(paramType, value0, value1);
        }

        public void setImageDataWithIndex(int index, int format, int width, int height, byte[] bytes) {
            mNativeRender.native_SetImageDataWithIndex(index, format, width, height, bytes);
        }

        public int getSampleType() {
            return mSampleType;
        }

        public void updateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY) {
            mNativeRender.native_UpdateTransformMatrix(rotateX, rotateY, scaleX, scaleY);
        }

    }


}
