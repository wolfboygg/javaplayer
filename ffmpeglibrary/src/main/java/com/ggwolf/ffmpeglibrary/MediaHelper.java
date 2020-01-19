package com.ggwolf.ffmpeglibrary;

import android.view.Surface;

/**
 * native层ffmpeg的操作类
 */
public class MediaHelper {

    private static MediaHelper sInstance = null;

    public static MediaHelper getInstance() {
        if (sInstance == null) {
            synchronized (MediaHelper.class) {
                if (sInstance == null) {
                    sInstance = new MediaHelper();
                }
            }
        }
        return sInstance;
    }

    private MediaHelper() {

    }

    static {
        System.loadLibrary("myffmpeg");
    }


    /**
     * 获取当前播放视频的协议的信息
     * @return
     */
    public native String urlprotocolinfo();

    /**
     * 获取视频编码格式的信息
     * @return
     */
    public native String avformatinfo();

    /**
     * 获取视频编解码信息
     * @return
     */
    public native String avcodecinfo();

    /**
     * 获取特效信息
     * @return
     */
    public native String avfilterinfo();

    public native void openVideo(String path, Surface surface);

    public native void playPcm();

    /**
     * Test C++ Thread
     */
    public native void normalThread();

    /**
     * 实现一个生产者消费者模型
     */
    public native void mutexThread();

    public native void cCallJavaMethod();

    /**
     * 我们在c++ 层封装好对自线程和主线程的调用方式，然后进行切换线程即可调用
     * 其实主要的区别就是JNIEnv的对象问题是与线程绑定的
     * @param code
     * @param msg
     */
    public void onError(int code, String msg) {
        if (mListenner != null) {
            mListenner.onError(code, msg);
        }
    }

    public OnErrorListener mListenner;

    public void setmListenner(OnErrorListener mListenner) {
        this.mListenner = mListenner;
    }

    public interface OnErrorListener {
        void onError(int code, String msg);
    }




}
