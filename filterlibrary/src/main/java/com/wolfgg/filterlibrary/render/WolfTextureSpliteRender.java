package com.wolfgg.filterlibrary.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.wolfgg.filterlibrary.BuildConfig;
import com.wolfgg.filterlibrary.R;
import com.wolfgg.filterlibrary.base.BaseRender;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.ShaderUtils;
import com.wolfgg.filterlibrary.utils.UiUtils;
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
 * <p>
 * FBO 最后要将绘制到fbo的纹理绘制到一个窗口才能显示
 */

public class WolfTextureSpliteRender implements WolfEGLSurfaceView.WolfGLRender {

    private static final String TAG = "WolfTextureSpliteRender";

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
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private int mProgram; // 程序

    private int vPosition; // 顶点坐标
    private int fPosition; // 纹理坐标
    private int sampler; // 纹理
    private int umatrix; // 举证

    private float[] matrix = new float[16];

    private int vboId;
    private int fboId;

    private int imgTextureId;

    private int textureId; // 这个用来将所有存储所有的信息，这个纹理存有所有的效果

    private BaseRender mFBORender;

    private int width;
    private int height;

    private OnRenderCreateListener mOnRenderCreateListener;

    public WolfTextureSpliteRender(Context context) {
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

    public void setFBORender(BaseRender FBORender) {
        mFBORender = FBORender;
    }

    public void setOnRenderCreateListener(OnRenderCreateListener onRenderCreateListener) {
        mOnRenderCreateListener = onRenderCreateListener;
    }

    @Override
    public void onSurfaceCreated() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "onSurfaceCreate");
        }
        mFBORender.onCreate();
        String vertexSource = ShaderUtils.getRawResource(mContext, R.raw.vertex_shader_m);
        String fragmentSource = ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);
        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);

        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");
        sampler = GLES20.glGetUniformLocation(mProgram, "sTexture");
        umatrix = GLES20.glGetUniformLocation(mProgram, "u_Matrix");

        createVBO();

        createFBO();

        imgTextureId = ShaderUtils.loadTexture(mContext, R.drawable.miao);

        if (mOnRenderCreateListener != null) {
            mOnRenderCreateListener.onCreate(textureId);
        }

    }

    @Override
    public void onSurfaceChange(int width, int height) {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "onSurfaceChange");
        }
//        GLES20.glViewport(0, 0, width, height);

        this.width = width;
        this.height = height;

        width = UiUtils.getScreenWidthPixels(mContext);
        height = UiUtils.getScreenHeightPixels(mContext);

        if (width > height) {// 这里应该使用图片和视频的比例进行处理
            Matrix.orthoM(matrix, 0, -width / ((height / 400f) * 400f), width / ((height / 400f) * 400f), -1f, 1f, -1f, 1f);
        } else {
            Matrix.orthoM(matrix, 0, -1, 1, -height / ((width / 400f) * 400f), height / ((width / 400f) * 400f), -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame() {
        if (BuildConfig.DEBUG) {
            LogHelper.i(TAG, "wolf texture render draw");
        }

        // 设置手机屏幕大小
        GLES20.glViewport(0, 0, UiUtils.getScreenWidthPixels(mContext), UiUtils.getScreenHeightPixels(mContext));

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 进行绘制
        GLES20.glUseProgram(mProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTextureId);

        // 使用矩阵
        GLES20.glUniformMatrix4fv(umatrix, 1, false, matrix, 0);

        drawUserVBO();

        //使用绘制三角形的方式进行绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glViewport(0, 0, width, height);// 在还原到原来的视觉大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        mFBORender.onDraw(textureId);
    }

    private void drawNormal() {
        // 然后进行绘制
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, mFragmentFloatBuffer);
    }

    /**
     * 创建FBO
     */
    private void createFBO() { // 需要将所有的数据绑定一个纹理上
        int[] fbos = new int[1];
        GLES20.glGenFramebuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        // 创建一个纹理
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, UiUtils.getScreenWidthPixels(mContext), UiUtils.getScreenHeightPixels(mContext), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 绑定到fbo 将数据
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            if (BuildConfig.DEBUG) {
                LogHelper.i(TAG, "fbo wrong");
            }
        } else {
            if (BuildConfig.DEBUG) {
                LogHelper.i(TAG, "fbo success");
            }
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

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

    public interface OnRenderCreateListener {
        void onCreate(int textureId);
    }


}
