package com.wolfg.javaplayer.opengl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.wolfg.javaplayer.utils.MatrixUtils;
import com.wolfg.javaplayer.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class FilterYUV extends BaseOpengl {

    private static final String TAG = "FilterYUV";
    private int vPosition;
    private int fPosition;
    private int sampler_y;
    private int sampler_u;
    private int sampler_v;
    private int u_matrix;

    private FloatBuffer vertexFloatBuffer;
    private FloatBuffer fragmentFloatBuffer;

    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    private int[] textureId_yuv;

    private int yuv_width;
    private int yuv_height;


    @Override
    void onCreate() {
        vertex = "attribute vec4 v_Position;\n"
                + "attribute vec2 f_Position;\n"
                + "varying vec2 ft_Position;\n"
                + "uniform mat4 u_Matrix;\n"
                + "void main() {\n"
                + "    ft_Position = f_Position;\n"
                + "    gl_Position = v_Position * u_Matrix;\n"
                + "}";
        fragment = "precision mediump float;\n"
                + "varying vec2 ft_Position;\n"
                + "uniform sampler2D sampler_y;\n"
                + "uniform sampler2D sampler_u;\n"
                + "uniform sampler2D sampler_v;\n"
                + "void main() {\n"
                + "   float y,u,v;\n"
                + "   y = texture2D(sampler_y,ft_Position).r;\n"
                + "   u = texture2D(sampler_u,ft_Position).r - 0.5;\n"
                + "   v = texture2D(sampler_v,ft_Position).r - 0.5;\n"
                + "\n"
                + "   vec3 rgb;\n"
                + "   rgb.r = y + 1.403 * v;\n"
                + "   rgb.g = y - 0.344 * u - 0.714 * v;\n"
                + "   rgb.b = y + 1.770 * u;\n"
                + "\n"
                + "   gl_FragColor = vec4(rgb,1);\n"
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

        initRenderYUV();

    }

    private void initRenderYUV() {

        vPosition = GLES20.glGetAttribLocation(program, "v_Position");
        fPosition = GLES20.glGetAttribLocation(program, "f_Position");
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix");
        sampler_y = GLES20.glGetUniformLocation(program, "sampler_y");
        sampler_u = GLES20.glGetUniformLocation(program, "sampler_u");
        sampler_v = GLES20.glGetUniformLocation(program, "sampler_v");

        textureId_yuv = new int[3];

        GLES20.glGenTextures(3, textureId_yuv, 0);
        for (int i = 0; i < 3; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        }

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

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexFloatBuffer);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentFloatBuffer);

        if (yuv_width > 0 && yuv_height > 0) {

            if (y != null) {
                Log.i(TAG, "yuv render y....");
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv[0]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuv_width, yuv_height, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, y);
                GLES20.glUniform1i(sampler_y, 0);
                y.clear();
                y = null;
            }

            if (u != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv[1]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuv_width / 2, yuv_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, u);
                GLES20.glUniform1i(sampler_u, 1);
                u.clear();
                u = null;
            }

            if (v != null) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId_yuv[2]);
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, yuv_width / 2, yuv_height / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, v);
                GLES20.glUniform1i(sampler_v, 2);
                v.clear();
                v = null;
            }
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    void setMatrix(int width, int height) {
        MatrixUtils.initMatrix(matrix);
        for (int i = 0; i < matrix.length; i++) {
            Log.i(TAG, "matrix[" + i + "]=" + matrix[i]);
        }
        float screen_r = width * 1.0f / height;
        float picture_r = yuv_width * 1.0f / yuv_height;

        if (screen_r > picture_r) { // 缩放宽
            float r = width / (1.0f * height / yuv_height * yuv_width);
            MatrixUtils.orthoM(matrix, -r, r, -1, 1);
//            Matrix.orthoM(matrix, 0, -r, r, -1, 1, -1, 1);
        } else { // 缩放高
            float r = height / (1.0f * width / yuv_width * yuv_height);
            MatrixUtils.orthoM(matrix, -1, 1, -r, r);
//            Matrix.orthoM(matrix, 0, -1, 1, -r, r, -1, 1);
        }
        for (int i = 0; i < matrix.length; i++) {
            Log.i(TAG, "matrix[" + i + "]=" + matrix[i]);
        }

    }

    @Override
    void setBitmap(Bitmap bitmap) {

    }

    @Override
    protected void setYUV(byte[] y, byte[] u, byte[] v, int width, int height) {
        if (width > 0 && height > 0) {
            if (yuv_width != width && yuv_height != height) {
                yuv_width = width;
                yuv_height = height;
            }
            this.y = ByteBuffer.wrap(y);
            this.u = ByteBuffer.wrap(u);
            this.v = ByteBuffer.wrap(v);
            setMatrix(surface_width, surface_height);
        }
    }

    @Override
    void onDestroy() {
        GLES20.glDeleteTextures(3, textureId_yuv, 0);
        GLES20.glDeleteProgram(program);
    }
}
