package com.ggwolf.audioplayer;

import android.text.TextUtils;

import com.ggwolf.audioplayer.listener.OnErrorListener;
import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.listener.OnPauseResumeListener;
import com.ggwolf.audioplayer.listener.OnTimeInfoListener;
import com.ggwolf.audioplayer.utils.LogHelper;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private AudioTimeInfoBean audioTimeInfoBean;

    private OnPreparedListener listener;
    private OnLoadListener onLoadListener;
    private OnPauseResumeListener onPauseResumeListener;
    private OnTimeInfoListener onTimeInfoListener;
    private OnErrorListener onErrorListener;

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

    public void stop() {
        new Thread(() -> n_stop()).start();
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

    // 停止播放
    private native void n_stop();

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

    /**
     * jni层来回调处理当前的时间信息
     * @param currentTime
     * @param totalTime
     */
    public void onCallTimeInfo(int currentTime, int totalTime) {
        if (onTimeInfoListener != null) {
            if (audioTimeInfoBean == null) {
                audioTimeInfoBean = new AudioTimeInfoBean();
            }
            audioTimeInfoBean.setCurrentTime(currentTime);
            audioTimeInfoBean.setTotalTime(totalTime);
            onTimeInfoListener.onTimeInfo(audioTimeInfoBean);
        }
    }

    /**
     * 将c++层的错误信息调用返回到应用层
     * @param code
     * @param msg
     */
    public void onCallErrorInfo(int code, String msg) {
        LogHelper.i("guo","code,....msg" + code);
        stop();// 开一个自线程在释放资源
        if (onErrorListener != null) {
            onErrorListener.onError(code, msg);
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

    public void setOnTimeInfoListener(OnTimeInfoListener onTimeInfoListener) {
        this.onTimeInfoListener = onTimeInfoListener;
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public interface OnPreparedListener {
        void onPrepard();
    }


}
