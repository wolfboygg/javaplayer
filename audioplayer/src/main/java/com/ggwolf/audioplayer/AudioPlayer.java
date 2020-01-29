package com.ggwolf.audioplayer;

import android.text.TextUtils;

import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.listener.OnPauseResumeListener;
import com.ggwolf.audioplayer.utils.LogHelper;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private OnPreparedListener listener;
    private OnLoadListener onLoadListener;
    private OnPauseResumeListener onPauseResumeListener;

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
        // 这里开始就是加载状态
        onCallLoad(true);
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

    public void pause() {
        n_pause();
        if (onPauseResumeListener != null) {
            onPauseResumeListener.onPause(true);
        }
    }

    public void resume() {
        n_resume();
        if (onPauseResumeListener != null) {
            onPauseResumeListener.onPause(false);
        }
    }

    /**
     * 打开解码器就完成
     */
    private native void n_prepared(String path);

    private native void n_start();

    // 暂停
    private native void n_pause();

    // 播放
    private native void n_resume();

    /**
     * jni层需要调用这个方法通知准备好了
     */
    public void onCallPrepared() {
        if (listener != null) {
            listener.onPrepard();
        }
    }

    /**
     * jni层通过判断当前解码队列中是否有数据来判断当前播放器的状态
     * @param load
     */
    public void onCallLoad(boolean load) {
        if (onLoadListener != null) {
            onLoadListener.onLoad(load);
        }
    }


    public void setListener(OnPreparedListener listener) {
        this.listener = listener;
    }

    /**
     * 加载状态的回调
     * @param onLoadListener
     */
    public void setOnLoadListener(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
    }

    public void setOnPauseResumeListener(OnPauseResumeListener onPauseResumeListener) {
        this.onPauseResumeListener = onPauseResumeListener;
    }

    public interface OnPreparedListener {
        void onPrepard();
    }


}
