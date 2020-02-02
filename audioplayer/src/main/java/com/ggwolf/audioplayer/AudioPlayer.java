package com.ggwolf.audioplayer;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.text.TextUtils;

import com.ggwolf.audioplayer.audiobean.MuteEnum;
import com.ggwolf.audioplayer.listener.OnCompleteListener;
import com.ggwolf.audioplayer.listener.OnErrorListener;
import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.listener.OnPauseResumeListener;
import com.ggwolf.audioplayer.listener.OnPcmDbListener;
import com.ggwolf.audioplayer.listener.OnTimeInfoListener;
import com.ggwolf.audioplayer.utils.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 添加录制功能，使用系统自带的MediaCodec
 */

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

    private boolean initmediacodec = false;
    private int mSampleRate = 0;

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
        stopRecord();
        duration = -1;
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
        if (percent >= 0 && percent <= 100) {
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

    public void startRecord(File outFile) {
        if (!initmediacodec) {
            mSampleRate = n_samplerate();
            LogHelper.i(TAG, "mSampleRate is " + mSampleRate);
            if (mSampleRate > 0) {
                initmediacodec = true;
                initMediaCode(mSampleRate, outFile);
                n_startstoprecord(true);
                LogHelper.i(TAG, "开始录制");
            }
        }
    }


    public void stopRecord() {
        if (initmediacodec) {
            n_startstoprecord(false);
            releaseMediaCodec();
            LogHelper.i(TAG, "结束录制");
        }
    }

    public void pauseRecord() {
        n_startstoprecord(false);
        LogHelper.i(TAG, "暂停录制");
    }

    public void resumeRecord() {
        n_startstoprecord(true);
        LogHelper.i(TAG, "继续录制");
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

    private native int n_samplerate();

    private native void n_startstoprecord(boolean start);

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
     *
     * @param load
     */
    public void onCallLoad(boolean load) {
        if (onLoadListener != null) {
            onLoadListener.onLoad(load);
        }
    }

    /**
     * jni层来回调处理当前的时间信息
     *
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
     *
     * @param code
     * @param msg
     */
    public void onCallErrorInfo(int code, String msg) {
        LogHelper.i("guo", "code,....msg" + code);
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
     *
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
     *
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


    private MediaFormat encoderFormat = null;
    private MediaCodec encoder = null;
    private FileOutputStream outputStream = null;
    private MediaCodec.BufferInfo info = null;
    private int perpcmSize = 0;
    private byte[] outByteBuffer = null;
    private int aacSampleRate = 4; // 添加ADTS头信息使用

    public void initMediaCode(int sampleRate, File outFile) {
        try {
            aacSampleRate = getADTSSampleRate(sampleRate);
            encoderFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, 2);
            encoderFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            encoderFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            encoderFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 5);
            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            info = new MediaCodec.BufferInfo();

            if (encoder == null) {
                LogHelper.i(TAG, "create encoder is wrong");
                return;
            }
            encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            // 创建输出文件
            outputStream = new FileOutputStream(outFile);
            encoder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * jni层解码pcm数据进行播放的调用编码的java层方法
     *
     * @param size
     * @param buffer
     */
    public void encodecPcmToAcc(int size, byte[] buffer) {
        if (buffer != null && encoder != null) {
            int inputBufferIndex = encoder.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                // 将数据塞到编码器
                ByteBuffer byteBuffer = encoder.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();
                LogHelper.i(TAG, "buffer size is " + buffer.length + "-->size:" + size);
                byteBuffer.put(buffer);
                encoder.queueInputBuffer(inputBufferIndex, 0, size, 0, 0);
            }

            // 进行编码
            int index = encoder.dequeueOutputBuffer(info, 0);
            while (index >= 0) {

                try {
                    // 加上头信息
                    perpcmSize = info.size + 7;
                    outByteBuffer = new byte[perpcmSize];

                    ByteBuffer byteBuffer = encoder.getOutputBuffer(index);
                    byteBuffer.position(info.offset);
                    byteBuffer.limit(info.offset + info.size);

                    // 添加头信息
                    addADtsHeader(outByteBuffer, perpcmSize, aacSampleRate);

                    // 进行数据拷贝
                    byteBuffer.get(outByteBuffer, 7, info.size);
                    byteBuffer.position(info.offset);

                    // 写入文件
                    outputStream.write(outByteBuffer, 0, perpcmSize);

                    encoder.releaseOutputBuffer(index, false);
                    index = encoder.dequeueOutputBuffer(info, 0);
                    outByteBuffer = null;
                    LogHelper.i(TAG, "编码中......");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void addADtsHeader(byte[] packet, int packetLen, int samplerate) {
        int profile = 2; // AAC LC
        int freqIdx = samplerate; // samplerate
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF; // 0xFFF(12bit) 这里只取了8位，所以还差4位放到下一个里面
        packet[1] = (byte) 0xF9; // 第一个t位放F
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private int getADTSSampleRate(int samplerate) {
        int rate = 4;
        switch (samplerate) {
            case 96000:
                rate = 0;
                break;
            case 88200:
                rate = 1;
                break;
            case 64000:
                rate = 2;
                break;
            case 48000:
                rate = 3;
                break;
            case 44100:
                rate = 4;
                break;
            case 32000:
                rate = 5;
                break;
            case 24000:
                rate = 6;
                break;
            case 22050:
                rate = 7;
                break;
            case 16000:
                rate = 8;
                break;
            case 12000:
                rate = 9;
                break;
            case 11025:
                rate = 10;
                break;
            case 8000:
                rate = 11;
                break;
            case 7350:
                rate = 12;
                break;
        }
        return rate;
    }

    /**
     * 释放资源
     */
    private void releaseMediaCodec() {
        if (encoder == null) {
            return;
        }

        try {
            outputStream.close();
            outputStream = null;

            encoder.stop();
            encoder.release();
            encoder = null;
            encoderFormat = null;
            info = null;
            initmediacodec = false;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {

                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                outputStream = null;
            }
        }

    }


}
