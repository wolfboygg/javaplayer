package com.wolfg.javaplayer.opengl;

import android.graphics.Bitmap;

public abstract class BaseOpengl {

    protected int surface_width;
    protected int surface_height;

    String vertex;
    String fragment;

    float[] vertexs = {
            1, -1,
            1, 1,
            -1, -1,
            -1, 1
    };

    float[] fragments = {
            1, 1,
            1, 0,
            0, 1,
            0, 0
    };


    float[] matrix = new float[16];

    int program;

    abstract void onCreate();

    abstract void onChange(int width, int height);

    abstract void onDraw();

    abstract void setMatrix(int width, int height);

    abstract void setBitmap(Bitmap bitmap);

    protected void setYUV(byte[] y, byte[] u, byte[] v, int width, int height) {

    }

    abstract void onDestroy();
}
