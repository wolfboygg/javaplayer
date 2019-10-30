package com.wolfgg.filterlibrary.render;

import android.content.Context;
import android.opengl.GLES20;

import com.wolfgg.filterlibrary.BuildConfig;
import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.ShaderUtils;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 用来绘制纹理
 * 绘制纹理需要进行加载shader，创建顶点和纹理坐标
 * <p>
 * 先实现功能，最后在进行结构的抽取
 */

public class WolfTextureRender implements WolfEGLSurfaceView.WolfGLRender {

    private static final String TAG = "WolfTextureRender";

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

    private int imgTextureId;

    public WolfTextureRender(Context context) {
        mContext = context;
        mVertexFloatBuffer = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex);
        mFragmentFloatBuffer = ByteBuffer.allocateDirect(fragment.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragment);
        mVertexFloatBuffer.position(0);
        mFragmentFloatBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "onSurfaceCreate");
        }
        String vertexSource = ShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");

        imgTextureId = ShaderUtils.loadTexture(mContext, R.drawable.androids);

    }

    @Override
    public void onSurfaceChange(int width, int height) {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "onSurfaceChange");
        }
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "wolf texture render draw");
        }
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 进行绘制
        GLES20.glUseProgram(mProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);

        // 然后进行绘制
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, mFragmentFloatBuffer);

        //使用绘制三角形的方式进行绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);


    }
}
