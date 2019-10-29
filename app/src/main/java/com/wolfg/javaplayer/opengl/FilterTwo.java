package com.wolfg.javaplayer.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.wolfg.javaplayer.utils.MatrixUtils;
import com.wolfg.javaplayer.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FilterTwo extends BaseOpengl {

    private static final String TAG = "FilterTwo";
    private int vPosition;
    private int fPosition;
    private int sampler;
    private int u_matrix;

    private Bitmap mBitmap;

    private FloatBuffer vertexFloatBuffer;
    private FloatBuffer fragmentFloatBuffer;

    @Override
    void onCreate() {
        vertex = "attribute vec4 v_Position;\n"
                + "attribute vec2 f_Position;\n"
                + "varying vec2 ft_Position;\n"
                + "uniform mat4 u_Matrix;\n"
                + "void main() {\n"
                + "ft_Position = f_Position;\n"
                + "gl_Position = v_Position * u_Matrix;\n"
                + "}";

        fragment = "precision mediump float;\n"
                + "uniform sampler2D sTexture;\n"
                + "varying vec2 ft_Position;\n"
                + "void main() {\n"
                + "lowp vec4 textureColor = texture2D(sTexture, ft_Position);\n"
                + "float gray = textureColor.r * 0.2125 + textureColor.g * 0.7154 + textureColor.b * 0.0721;\n"
                + "gl_FragColor = vec4(gray, gray, gray, textureColor.w);\n"
                + "}";
        program = ShaderUtils.createProgram(vertex, fragment);

        vertexFloatBuffer = ByteBuffer.allocateDirect(vertexs.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexs);
        vertexFloatBuffer.position(0);

        fragmentFloatBuffer = ByteBuffer.allocateDirect(fragments.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(fragments);
        fragmentFloatBuffer.position(0);

        // 获取纹理和定点坐标
        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");
        sampler = GLES20.glGetUniformLocation(program, "sTexture");
    }

    @Override
    void onChange(int width, int height) {
        surface_width = width;
        surface_height = height;
        GLES20.glViewport(0, 0, width, height);
        setMatrix(width, height);
    }

    @Override
    void onDraw() {
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, loadTexture());

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentFloatBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    void setMatrix(int width, int height) {
        if (mBitmap == null) {
            return;
        }
        MatrixUtils.initMatrix(matrix);
        for (int i = 0; i < matrix.length; i++) {
            Log.i(TAG, "matrix[" + i + "]=" + matrix[i]);
        }
        float screen_r = width * 1.0f / height;
        float picture_r = mBitmap.getWidth() * 1.0f / mBitmap.getHeight();

        if (screen_r > picture_r) { // 缩放宽
            float r = width / (1.0f * height / mBitmap.getHeight() * mBitmap.getWidth());
            MatrixUtils.orthoM(matrix, -r, r, -1, 1);
//            Matrix.orthoM(matrix, 0, -r, r, -1, 1, -1, 1);
        } else { // 缩放高
            float r = height / (1.0f * width / mBitmap.getWidth() * mBitmap.getHeight());
            MatrixUtils.orthoM(matrix, -1, 1, -r, r);
//            Matrix.orthoM(matrix, 0, -1, 1, -r, r, -1, 1);
        }
        for (int i = 0; i < matrix.length; i++) {
            Log.i(TAG, "matrix[" + i + "]=" + matrix[i]);
        }
    }

    @Override
    void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (surface_width > 0 && surface_height > 0) {
            setMatrix(surface_width, surface_height);
        }
    }

    @Override
    void onDestroy() {
        GLES20.glDeleteProgram(program);
    }

    private int loadTexture() {
        int[] textures = new int[1];
        //创建和绑定纹理
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //激活第0个纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(sampler, 0);
        //设置环绕和过滤方式
        //环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        //过滤（纹理像素映射到坐标点）：（缩小、放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        if (mBitmap == null) {
            return 0;
        }
        //设置图片
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return textures[0];
    }


}
