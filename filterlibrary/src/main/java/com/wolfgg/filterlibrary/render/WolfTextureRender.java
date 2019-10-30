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
 * <p>
 * <p>
 * 添加vbo功能和fbo功能
 * VBO
 * 1. 创建VBO得到vboID
 * 2. 根据id绑定vbo
 * 3. 分配vbo需要的缓存大小
 * 4. 为vbo设置顶点数据的值
 * 5. 解绑vbo
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

    private int vboId;

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

        createVBO();

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

        drawUserVBO();

        //使用绘制三角形的方式进行绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void drawNormal() {
        // 然后进行绘制
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, mFragmentFloatBuffer);
    }


    /**
     * 创建vbo
     */
    private void createVBO() {
        // 1.创建vbo
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];
        // 2. 绑定vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        // 3. 分配大小
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4 + fragment.length * 4, null, GLES20.GL_STREAM_DRAW);
        // 4.设置数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertex.length * 4, mVertexFloatBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, fragment.length * 4, mFragmentFloatBuffer);
        // 5. 解除绑定
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void drawUserVBO() {
        // 绑定vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        // 设置数据
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0); // 第二个参数表示的去几个点每次
        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertex.length * 4);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }


}
