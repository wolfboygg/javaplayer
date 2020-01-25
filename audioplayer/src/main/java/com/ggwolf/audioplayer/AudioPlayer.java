package com.ggwolf.audioplayer;

import android.text.TextUtils;

import com.ggwolf.audioplayer.utils.LogHelper;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private OnPreparedListener listener;

    /**
     * 播放资源的地址
     */
    private String mSource;

    static {
        System.loadLibrary("audio");
    }

    private static AudioPlayer sInstance;

    public static AudioPlayer getInstance() {
        if (sInstance == null) {
            synchronized (AudioPlayer.class) {
                if (sInstance == null) {
                    sInstance = new AudioPlayer();
                }
            }
        }
        return sInstance;
    }

    private AudioPlayer() {

    }

    public void setSource(String mSource) {
        this.mSource = mSource;
    }

    public void prepared() {
        if (TextUtils.isEmpty(mSource)) {
            LogHelper.i(TAG, "播放资源地址没有设置");
            return;
        }
        new Thread(() -> {
            n_prepared(mSource);
        }).start();
    }


    public void start() {
        if (TextUtils.isEmpty(mSource)) {
            LogHelper.i(TAG, "播放资源地址没有设置");
            return;
        }
        new Thread(() -> {
            n_start();
        }).start();
    }


    /**
     * 打开解码器就完成
     */
    public native void n_prepared(String path);

    public native void n_start();

    /**
     * jni层需要调用这个方法通知准备好了
     */
    public void onCallPrepared() {
        if (listener != null) {
            listener.onPrepard();
        }
    }

    public void setListener(OnPreparedListener listener) {
        this.listener = listener;
    }

    public interface OnPreparedListener {
        void onPrepard();
    }


}
