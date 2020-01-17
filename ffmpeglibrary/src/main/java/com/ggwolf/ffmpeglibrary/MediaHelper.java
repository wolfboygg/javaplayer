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

}
