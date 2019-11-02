package com.wolfgg.filterlibrary.render;

import android.content.Context;
import android.opengl.GLES20;

import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.ShaderUtils;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class WolfMutiRender implements WolfEGLSurfaceView.WolfGLRender {

    private final static String TAG = "WolfMutiRender";

    private Context mContext;

    private float[] vertex = new float[]{
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    private float[] fragment = new float[]{
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    private FloatBuffer mVertexData = null;
    private FloatBuffer mFragmentData = null;

    private int mProgram;
    private int vPosition;
    private int fPosition;
    private int sampler;

    // 共享纹理不用进行处理textureID
    private int textureId;

    private int vboId;

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public WolfMutiRender(Context context) {
        mContext = context;
        mVertexData = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex);
        mFragmentData = ByteBuffer.allocateDirect(fragment.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragment);
        mVertexData.position(0);
        mFragmentData.position(0);
    }

    @Override
    public void onSurfaceCreated() {
        LogHelper.i(TAG, "onSurfaceCreated");

        String vertexSource = ShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");

        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4 + fragment.length * 4, null, GLES20.GL_STREAM_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertex.length * 4, mVertexData);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, fragment.length * 4, mFragmentData);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    @Override
    public void onSurfaceChange(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame() {
        // 进行绘制
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertex.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);


    }
}
