package com.wolfgg.cameralibrary.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMetadata;
import android.media.MediaMuxer;
import android.view.Surface;

import com.wolfgg.filterlibrary.egl.EGLHelper;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLContext;

/**
 * 创建一个渲染的线程和编码的线程，两个联系的桥梁就是surface，编码和渲染是一个surface就可以进行处理
 */
public abstract class WolfBaseMediaEncoder {

    private static final String TAG = "WolfBaseMediaEncoder";

    private Surface mSurface;
    private EGLContext mEGLContext;

    private int mWidth;
    private int mHeight;


    private MediaCodec mVideoEncodec;
    private MediaFormat mVideoFormat;
    private MediaCodec.BufferInfo mVideoBufferinfo;

    // 声音录制功能
    private MediaCodec mAudioEncodec;
    private MediaFormat mAudioFormat;
    private MediaCodec.BufferInfo mAudioBufferInfo;

    private long audioPts = 0; // 这个pts需要手动计算，传入编码器
    private int sampleRate;

    private MediaMuxer mMediaMuxer;
    private boolean encodecStart;
    private boolean audioExit;
    private boolean videoExit;


    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;


    private WolfEGLSurfaceView.WolfGLRender mWolfGLRender;

    private OnMediaInfoListener mOnMediaInfoListener;

    private WolfEGLMediaThread mWolfEGLMediaThread;
    private VideoEncodecThread mVideoEncodecThread;
    private AudioEncodecThread mAudioEncodecThread;


    public WolfBaseMediaEncoder(Context context) {

    }

    public void setRender(WolfEGLSurfaceView.WolfGLRender wolfGLRender) {
        this.mWolfGLRender = wolfGLRender;
    }

    public void setRenderMode(int mRenderMode) {
        if (mWolfGLRender == null) {
            throw new RuntimeException("must set render before");
        }
        this.mRenderMode = mRenderMode;
    }

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        mOnMediaInfoListener = onMediaInfoListener;
    }

    public void initEncodec(EGLContext eglContext, String savePath, int width, int height, int sampleRate, int channelCount) {
        LogHelper.i(TAG, "EGLContext is = " + eglContext);
        this.mWidth = width;
        mHeight = height;
        mEGLContext = eglContext;
        initMeidaEncodec(savePath, width, height, sampleRate, channelCount);
    }

    private void initMeidaEncodec(String savePath, int width, int height, int sampleRate, int channelCount) {
        try {
            mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            initAudioEncodec(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initVideoEncodec(String mimeType, int width, int height) {
        try {
            mVideoBufferinfo = new MediaCodec.BufferInfo();
            mVideoFormat = MediaFormat.createVideoFormat(mimeType, width, height);
            mVideoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            mVideoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);
            mVideoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
            mVideoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 设置i帧的处理

            mVideoEncodec = MediaCodec.createEncoderByType(mimeType);
            mVideoEncodec.configure(mVideoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mSurface = mVideoEncodec.createInputSurface();
            LogHelper.i(TAG, "surface is " + mSurface);

        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mVideoFormat = null;
            mVideoBufferinfo = null;
        }
    }

    /**
     * 初始化音频编码器
     *
     * @param mimeType
     * @param sampleRate
     * @param channelCount
     */
    private void initAudioEncodec(String mimeType, int sampleRate, int channelCount) {
        try {
            this.sampleRate = sampleRate;
            mAudioBufferInfo = new MediaCodec.BufferInfo();
            mAudioFormat = MediaFormat.createAudioFormat(mimeType, sampleRate, channelCount);
            mAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);// 码率
            mAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mAudioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 4096);

            mAudioEncodec = MediaCodec.createEncoderByType(mimeType);
            mAudioEncodec.configure(mAudioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
            mAudioBufferInfo = null;
            mAudioFormat = null;
            mAudioEncodec = null;
        }
    }


    public void startRecode() {
        if (mSurface != null && mEGLContext != null) {

            audioPts = 0;
            audioExit = false;
            videoExit = false;
            encodecStart = false;

            mWolfEGLMediaThread = new WolfEGLMediaThread(new WeakReference<WolfBaseMediaEncoder>(this));
            mVideoEncodecThread = new VideoEncodecThread(new WeakReference<WolfBaseMediaEncoder>(this));
            mAudioEncodecThread = new AudioEncodecThread(new WeakReference<WolfBaseMediaEncoder>(this));

            mWolfEGLMediaThread.isCreate = true;
            mWolfEGLMediaThread.isChange = true;

            mWolfEGLMediaThread.start();
            mVideoEncodecThread.start();
            mAudioEncodecThread.start();
        }
    }

    public void stopRecode() {
        if (mWolfEGLMediaThread != null && mVideoEncodecThread != null && mAudioEncodecThread != null) {
            mVideoEncodecThread.exit();// 先停止编码线程
            mAudioEncodecThread.exit();
            mWolfEGLMediaThread.onDestroy();
            mVideoEncodecThread = null;
            mAudioEncodecThread = null;
            mWolfEGLMediaThread = null;
        }
    }

    public void putPCMData(byte[] buffer, int size) {
        // 这里需要向音频的编码器中进行塞入数据
        if (mAudioEncodecThread != null && !mAudioEncodecThread.isExit && buffer != null && size > 0) {
            int inputBufferIndex = mAudioEncodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mAudioEncodec.getInputBuffer(inputBufferIndex);
                inputBuffer.clear();
                inputBuffer.put(buffer);
                long pts = getAudioPts(size, sampleRate);
                mAudioEncodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
            }

        }
    }


    static class WolfEGLMediaThread extends Thread {

        private WeakReference<WolfBaseMediaEncoder> encoder;
        private EGLHelper mEGLHelper;
        private Object mObject;// 用来处理绘制的唤醒问题

        private boolean isExit = false;
        private boolean isCreate = false;
        private boolean isChange = false;
        private boolean isStart = false;

        public WolfEGLMediaThread(WeakReference<WolfBaseMediaEncoder> encoder) {
            this.encoder = encoder;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            isStart = false;
            mObject = new Object();
            mEGLHelper = new EGLHelper();
            mEGLHelper.initEGL(encoder.get().mSurface, encoder.get().mEGLContext);

            while (true) {
                if (isExit) {
                    release();
                    break;
                }

                if (isStart) {
                    if (encoder.get().mRenderMode == RENDERMODE_WHEN_DIRTY) {
                        synchronized (mObject) {
                            try {
                                mObject.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else if (encoder.get().mRenderMode == RENDERMODE_CONTINUOUSLY) {
                        try {
                            Thread.sleep(1000 / 60);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new RuntimeException("mRenderMode is wrong value");
                    }
                }

                onCreate();
                onChange(encoder.get().mWidth, encoder.get().mHeight);
                onDraw();
                isStart = true;
            }
        }

        private void onCreate() {
            // isCreate 在开始编码的时候进行复制
            if (isCreate && encoder.get().mWolfGLRender != null) {
                isCreate = false;
                encoder.get().mWolfGLRender.onSurfaceCreated();
            }
        }


        private void onChange(int width, int height) {
            if (isChange && encoder.get().mWolfGLRender != null) {
                isChange = false;
                encoder.get().mWolfGLRender.onSurfaceChange(width, height);
            }
        }

        private void onDraw() {
            if (encoder.get().mWolfGLRender != null && mEGLHelper != null) {
                encoder.get().mWolfGLRender.onDrawFrame();
                if (!isStart) {
                    encoder.get().mWolfGLRender.onDrawFrame();
                }
                // 交换数据
                mEGLHelper.swapBuffers();
            }
        }

        private void requetRender() {
            if (mObject != null) {
                synchronized (mObject) {
                    mObject.notifyAll();
                }
            }
        }

        public void onDestroy() {
            isExit = true;
            requetRender();
        }

        public void release() {
            if (mEGLHelper != null) {
                mEGLHelper.destroy();
                mEGLHelper = null;
                mObject = null;
                encoder = null;
            }
        }
    }


    static class VideoEncodecThread extends Thread {
        private WeakReference<WolfBaseMediaEncoder> encoder;

        private boolean isExit = false;

        private MediaCodec mMediaCodec;
        private MediaFormat mMediaFormat;
        private MediaCodec.BufferInfo mVideoBufferInfo;
        private MediaMuxer mMediaMuxer;

        private int mVideoTrackIndex;
        private long pts;

        public VideoEncodecThread(WeakReference<WolfBaseMediaEncoder> encoder) {
            this.encoder = encoder;
            mMediaCodec = encoder.get().mVideoEncodec;
            mMediaFormat = encoder.get().mVideoFormat;
            mVideoBufferInfo = encoder.get().mVideoBufferinfo;
            mMediaMuxer = encoder.get().mMediaMuxer;
        }

        @Override
        public void run() {
            super.run();
            // 进行数据编码
            pts = 0;
            mVideoTrackIndex = -1;
            isExit = false;
            mMediaCodec.start();
            while (true) {

                if (isExit) {
                    mMediaCodec.stop();
                    mMediaCodec.release();
                    mMediaCodec = null;

                    mMediaMuxer.stop();
                    mMediaMuxer.release();
                    mMediaMuxer = null;

                    LogHelper.i(TAG, "录制完成");
                    break;
                }

                int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mVideoBufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    mVideoTrackIndex = mMediaMuxer.addTrack(mMediaCodec.getOutputFormat());
                    if (encoder.get().mAudioEncodecThread.audioTrackIndex != -1) {
                        mMediaMuxer.start();
                        encoder.get().encodecStart = true;
                    }
                } else {
                    while (outputBufferIndex >= 0) { // 可能有很多的数据轨道
                        if (encoder.get().encodecStart) {
                            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffers()[outputBufferIndex];
//                        ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                            outputBuffer.position(mVideoBufferInfo.offset);
                            outputBuffer.limit(mVideoBufferInfo.offset + mVideoBufferInfo.size);

                            if (pts == 0) {
                                pts = mVideoBufferInfo.presentationTimeUs;// 等于当前的pts值，不要使用默认的，太大
                            }
                            mVideoBufferInfo.presentationTimeUs = mVideoBufferInfo.presentationTimeUs - pts;// 这样pts会是从0开始

                            // 开始进行编码写入文件
                            mMediaMuxer.writeSampleData(mVideoTrackIndex, outputBuffer, mVideoBufferInfo);
                            if (encoder.get().mOnMediaInfoListener != null) {
                                encoder.get().mOnMediaInfoListener.onMediaTime((int) (mVideoBufferInfo.presentationTimeUs / 1000000));
                            }
                        }
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mVideoBufferInfo, 0); // 开始下一个轨道处理

                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }

    }

    static class AudioEncodecThread extends Thread {
        private WeakReference<WolfBaseMediaEncoder> encoder;
        private boolean isExit;

        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo mAudioBufferInfo;
        private MediaMuxer mMediaMuxer;

        private int audioTrackIndex = -1;
        /**
         * pts 很重要，这个和音视频同步相关。
         */
        private long pts;

        public AudioEncodecThread(WeakReference<WolfBaseMediaEncoder> encoder) {
            this.encoder = encoder;
            this.audioEncodec = encoder.get().mAudioEncodec;
            this.mAudioBufferInfo = encoder.get().mAudioBufferInfo;
            this.mMediaMuxer = encoder.get().mMediaMuxer;
            audioTrackIndex = -1;
        }

        @Override
        public void run() {
            super.run();
            pts = 0;
            isExit = false;
            audioEncodec.start();
            while (true) {
                if (isExit) {
                    // 结束编码
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    encoder.get().audioExit = true; // 音频编码和视频编码要相互关联，同时进行退出
                    if (encoder.get().videoExit) {
                        mMediaMuxer.stop();
                        mMediaMuxer.release();
                        mMediaMuxer = null;
                    }
                    break;
                }
                // 然后就开始了编码
                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(mAudioBufferInfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // 设置编码混合器进行开始工作
                    if (mMediaMuxer != null) {
                        audioTrackIndex = mMediaMuxer.addTrack(audioEncodec.getOutputFormat());
                        if (encoder.get().mVideoEncodecThread.mVideoTrackIndex != -1) { // 视频已经准备好了
                            mMediaMuxer.start();
                            encoder.get().encodecStart = true;
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (encoder.get().encodecStart) { // 开始进行编码写入
                            ByteBuffer outputBuffer = audioEncodec.getOutputBuffer(outputBufferIndex);
                            outputBuffer.position(mAudioBufferInfo.offset);
                            outputBuffer.limit(mAudioBufferInfo.offset + mAudioBufferInfo.size);
                            if (pts == 0) {
                                pts = mAudioBufferInfo.presentationTimeUs;
                            }
                            mAudioBufferInfo.presentationTimeUs = mAudioBufferInfo.presentationTimeUs - pts;
                            mMediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, mAudioBufferInfo);
                        }
                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(mAudioBufferInfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }

    }

    private long getAudioPts(int size, int sampleRate) {
        // 大小 = 采样率 * 采样大小(16bit) * 声道数
        audioPts += (long) (1.0 * size / (sampleRate * 2 * 2) * 1000000.0);
        return audioPts;
    }


    public interface OnMediaInfoListener {
        void onMediaTime(int time);
    }


}
