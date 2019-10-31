package com.wolfgg.filterlibrary.render;

import android.content.Context;
import android.opengl.GLES20;

import com.wolfgg.filterlibrary.BuildConfig;
import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class FBORender {

    private static final String TAG = "FBORender";

    protected Context mContext;

    protected FloatBuffer mVertexFloatBuffer;
    protected FloatBuffer mFragmentFloatBuffer;

    private float[] vertex = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    private float[] fragment = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    private int mProgram; // 程序

    private int vPosition; // 顶点坐标
    private int fPosition; // 纹理坐标
    private int sampler; // 纹理

    private int vboId;

    public FBORender(Context context) {
        this.mContext = context;
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

    public void onCreated() {
        String vertexSource = ShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");

        int [] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4 + fragment.length * 4, null, GLES20. GL_STATIC_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertex.length * 4, mVertexFloatBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, fragment.length * 4, mFragmentFloatBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    public void onChange(int width, int height) {

    }

    public void onDraw(int textureId) {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "fborender onDraw textureId = " + textureId);
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);

        // 进行绘制
        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        // 然后进行绘制
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertex.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


}
