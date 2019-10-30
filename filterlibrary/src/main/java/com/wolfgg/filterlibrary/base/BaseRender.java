package com.wolfgg.filterlibrary.base;

import android.content.Context;

import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

import java.nio.FloatBuffer;

/**
 * 做一个基类的render，做一些抽取功能
 */

public class BaseRender implements WolfEGLSurfaceView.WolfGLRender {

    protected Context mContext;

    protected FloatBuffer mVertexFloatBuffer;
    protected FloatBuffer mFragmentFloatBuffer;

    private float[] vertex = {
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
    };

    private float[] fragment = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private int mProgram; // 程序

    private int vPosition; // 顶点坐标
    private int fPosition; // 纹理坐标
    private int sampler; // 纹理

    public BaseRender(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated() {

    }

    @Override
    public void onSurfaceChange(int width, int height) {

    }

    @Override
    public void onDrawFrame() {

    }


}
