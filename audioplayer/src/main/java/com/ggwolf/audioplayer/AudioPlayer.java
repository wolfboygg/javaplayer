package com.ggwolf.audioplayer;

import android.text.TextUtils;

import com.ggwolf.audioplayer.audiobean.MuteEnum;
import com.ggwolf.audioplayer.listener.OnCompleteListener;
import com.ggwolf.audioplayer.listener.OnErrorListener;
import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.listener.OnPauseResumeListener;
import com.ggwolf.audioplayer.listener.OnPcmDbListener;
import com.ggwolf.audioplayer.listener.OnTimeInfoListener;
import com.ggwolf.audioplayer.utils.LogHelper;

public class AudioPlayer {
    private static final String TAG = "AudioPlayer";

    private AudioTimeInfoBean audioTimeInfoBean;

    private int duration = -1;

    private OnPreparedListener listener;
    private OnLoadListener onLoadListener;
    private OnPauseResumeListener onPauseResumeListener;
    private OnTimeInfoListener onTimeInfoListener;
    private OnErrorListener onErrorListener;
    private OnCompleteListener onCompleteListener;
    private OnPcmDbListener onPcmDbListener;

    /**
     * 播放资源的地址
     */
    private String mSource;

    private boolean isPlayNext = false;
    private int mVolumePercent = 100;

    private MuteEnum mMuteEnum = null;

    private float mPitch = 1.0f;
    private float mSpeed = 1.0f;

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
            setVolume(mVolumePercent);
            setMute(mMuteEnum);
            n_start();
            setPitch(mPitch);
            setSpeed(mSpeed);
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
        audioTimeInfoBean = null;
        new Thread(() -> n_stop()).start();
    }

    public void seek(int secds) {
        n_seek(secds);
    }

    public void playNext(String url) {
        isPlayNext = true;
        mSource = url;
        stop();
    }

    public int getDuration() {
        if (duration == -1) {
            duration = n_duration();
        }
        return duration;
    }

    public void setVolume(int percent) {
        if (percent >= 0 && percent <=100) {
            mVolumePercent = percent;
            n_volume(percent);
        }
    }

    public int getVolume() {
        return mVolumePercent;
    }

    public void setMute(MuteEnum muteEnum) {
        mMuteEnum = muteEnum;
        n_mute(muteEnum.getValue());
    }

    public void setPitch(float pitch) {
        this.mPitch = pitch;
        n_pitch(pitch);
    }

    public void setSpeed(float speed) {
        this.mSpeed = speed;
        n_speed(speed);
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

    private native void n_seek(int secds);

    private native int n_duration();

    private native void n_volume(int precent);

    private native void n_mute(int mute);

    private native void n_pitch(float pitch);

    private native void n_speed(float speed);

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

    /**
     * 播放完成的回调
     */
    public void onCallComplete() {
        stop();
        if (onCompleteListener != null) {
            onCompleteListener.onComplete();
        }
    }

    public void onCallStop() {
        // 在这里判断是否播放下个url
        if (isPlayNext) {
            isPlayNext = false;
            prepared();
        }
    }

    /**
     * 计算当前播放pcm的分贝值
     * @param db
     */
    public void onCallPcmDB(int db) {
        if (onPcmDbListener != null) {
            onPcmDbListener.onPcmDb(db);
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

    public void setOnCompleteListener(OnCompleteListener onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    public void setOnPcmDbListener(OnPcmDbListener onPcmDbListener) {
        this.onPcmDbListener = onPcmDbListener;
    }

    public interface OnPreparedListener {
        void onPrepard();
    }


}
