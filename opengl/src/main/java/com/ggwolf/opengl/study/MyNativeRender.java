package com.ggwolf.opengl.study;

public class MyNativeRender {

    // 这里定义我们的OpenGL学习的效果demo

    public static final int SAMPLE_TYPE = 200;


    public static final int SAMPLE_TYPE_TRIANGLE = SAMPLE_TYPE;


    static {
        System.loadLibrary("studyrender");
    }

    /**
     * 定义我们对native层的操作
     */

    public native void native_Init();

    public native void native_UnInit();

    public native void native_SetParamsInt(int paramType, int value0, int value1);

    public native void native_UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY);

    public native void native_SetImageData(int format, int width, int height, byte[] bytes);

    public native void native_SetImageDataWithIndex(int index, int format, int width, int height, byte[] bytes);

    public native void native_OnSurfaceCreated();

    public native void native_OnSurfaceChanged(int width, int height);

    public native void native_OnDrawFrame();

}
