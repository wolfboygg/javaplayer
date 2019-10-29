package com.wolfg.javaplayer.utils;


import android.opengl.GLES20;

public class ShaderUtils {
    public static int createProgram(String vertex, String fragment) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);

        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        return program;
    }

    public static int loadShader(int type, String shaderStr) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderStr);
        GLES20.glCompileShader(shader);
        return shader;
    }

}
