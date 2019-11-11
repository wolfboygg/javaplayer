package com.wolfgg.cameralibrary.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.wolfgg.cameralibrary.R;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.ShaderUtils;
import com.wolfgg.filterlibrary.utils.UiUtils;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 相机进行渲染的render
 */
public class WolfCameraRender implements WolfEGLSurfaceView.WolfGLRender, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "WolfCameraRender";

    private Context mContext;

    private float[] vertex = {
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    private float[] fragment = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mFragmentBuffer;


    private int mProgram;

    private int vPosition;
    private int fPosition;
    private int sampler;

    private int vboId;
    private int fboId;

    private int fboTextureId;
    private int cameraTextureId;

    private SurfaceTexture mSurfaceTexture;

    // 用来绘制fbo的预览的位置的大小，防止变形
    private int screenWidth;
    private int screenHeight;

    private int width;
    private int height;

    private WolfCameraFBORender mWolfCameraFBORender;


    private OnSurfaceCreateListener mOnSurfaceCreateListener;

    public void setOnSurfaceCreateListener(OnSurfaceCreateListener onSurfaceCreateListener) {
        mOnSurfaceCreateListener = onSurfaceCreateListener;
    }

    public WolfCameraRender(Context context) {
        mContext = context;
        screenWidth = UiUtils.getScreenWidthPixels(context);
        screenHeight = UiUtils.getScreenHeightPixels(context);
        LogHelper.i(TAG, "screenWidth:" + screenWidth + "-->screenHeight:" + screenHeight);

        mWolfCameraFBORender = new WolfCameraFBORender(context);

        mVertexBuffer = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertex);
        mVertexBuffer.position(0);

        mFragmentBuffer = ByteBuffer.allocateDirect(fragment.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragment);
        mFragmentBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated() {

        mWolfCameraFBORender.onCreate();

        String vertexSource = ShaderUtils.getRawResource(mContext, R.raw.vertex_shader);
        String fragmentSource = ShaderUtils.getRawResource(mContext, R.raw.fragment_shader);

        mProgram = ShaderUtils.createProgram(vertexSource, fragmentSource);
        vPosition = GLES20.glGetAttribLocation(mProgram, "v_Position");
        fPosition = GLES20.glGetAttribLocation(mProgram, "f_Position");

        // vbo
        int[] vbos = new int[1];
        GLES20.glGenBuffers(1, vbos, 0);
        vboId = vbos[0];

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4 + fragment.length * 4, null, GLES20.GL_STREAM_DRAW);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertex.length * 4, mVertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertex.length * 4, fragment.length * 4, mFragmentBuffer);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //fbo
        int[] fbos = new int[1];
        GLES20.glGenBuffers(1, fbos, 0);
        fboId = fbos[0];
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTextureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, UiUtils.getScreenWidthPixels(mContext), UiUtils.getScreenHeightPixels(mContext), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
        // 检查一下是否绑定成功了
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogHelper.i(TAG, "fbo wrong");
        } else {
            LogHelper.i(TAG, "fbo success");
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // 绑定camera纹理
        int[] textureidsoes = new int[1];
        GLES20.glGenTextures(1, textureidsoes, 0);
        cameraTextureId = textureidsoes[0];
        LogHelper.i(TAG, "cameraTextureId is:" + cameraTextureId);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, cameraTextureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        mSurfaceTexture = new SurfaceTexture(cameraTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        // 然后通过回调出去
        if (mOnSurfaceCreateListener != null) {
            mOnSurfaceCreateListener.onSurfaceCreate(mSurfaceTexture, cameraTextureId);
        }

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);


    }

    @Override
    public void onSurfaceChange(int width, int height) {
        this.width = width;
        this.height = height;
//        GLES20.glViewport(0, 0, screenWidth, screenHeight);
    }

    @Override
    public void onDrawFrame() {
        // 首先要进行更新纹理
        mSurfaceTexture.updateTexImage();
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 开始绘制到fbo上
        GLES20.glUseProgram(mProgram);
        GLES20.glViewport(0, 0, screenWidth, screenHeight);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboId);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertex.length * 4);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        mWolfCameraFBORender.onChange(this.width, this.height);
        mWolfCameraFBORender.onDraw(fboTextureId);

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    public interface OnSurfaceCreateListener {
        void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId);
    }

}
