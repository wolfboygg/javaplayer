package com.wolfgg.cameralibrary.video;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
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
    private MediaMuxer mMediaMuxer;


    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private int mRenderMode = RENDERMODE_CONTINUOUSLY;


    private WolfEGLSurfaceView.WolfGLRender mWolfGLRender;

    private OnMediaInfoListener mOnMediaInfoListener;

    private WolfEGLMediaThread mWolfEGLMediaThread;
    private VideoEncodecThread mVideoEncodecThread;


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

    public void initEncodec(EGLContext eglContext, String savePath, String mimeType, int width, int height) {
        LogHelper.i(TAG, "EGLContext is = " +eglContext);
        this.mWidth = width;
        mHeight = height;
        mEGLContext = eglContext;
        initMeidaEncodec(savePath, mimeType, width, height);
    }

    private void initMeidaEncodec(String savePath, String mimeType, int width, int height) {
        try {
            mMediaMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            initVideoEncodec(mimeType, width, height);
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


    public void startRecode() {
        if (mSurface != null && mEGLContext != null) {
            mWolfEGLMediaThread = new WolfEGLMediaThread(new WeakReference<WolfBaseMediaEncoder>(this));
            mVideoEncodecThread = new VideoEncodecThread(new WeakReference<WolfBaseMediaEncoder>(this));

            mWolfEGLMediaThread.isCreate = true;
            mWolfEGLMediaThread.isChange = true;

            mWolfEGLMediaThread.start();
            mVideoEncodecThread.start();
        }
    }

    public void stopRecode() {
        if (mWolfEGLMediaThread != null && mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();// 先停止编码线程
            mWolfEGLMediaThread.onDestroy();
            mVideoEncodecThread = null;
            mWolfEGLMediaThread = null;
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
                    mMediaMuxer.start();
                } else {
                    while (outputBufferIndex >= 0) { // 可能有很多的数据轨道
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

    public interface OnMediaInfoListener {
        void onMediaTime(int time);
    }


}
