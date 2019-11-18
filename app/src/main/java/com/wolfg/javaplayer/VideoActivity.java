package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.wolfgg.cameralibrary.camera.WolfCameraView;
import com.wolfgg.cameralibrary.video.WolfBaseMediaEncoder;
import com.wolfgg.cameralibrary.video.WolfMediaEncodec;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.UiUtils;
import com.ywl5320.libmusic.WlMusic;
import com.ywl5320.listener.OnCompleteListener;
import com.ywl5320.listener.OnPreparedListener;
import com.ywl5320.listener.OnShowPcmDataListener;

public class VideoActivity extends AppCompatActivity implements WolfBaseMediaEncoder.OnMediaInfoListener {

    public static final String TAG = "VideoActivity";

    private WolfCameraView mWolfCameraView;
    private Button mButton;

    private WlMusic mWlMusic;

    private WolfMediaEncodec mWolfMediaEncodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mWolfCameraView = findViewById(R.id.cameraview);
        mButton = findViewById(R.id.btn_record);

        mWlMusic = WlMusic.getInstance();
        mWlMusic.setCallBackPcmData(true);
        mWlMusic.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared() {
                mWlMusic.playCutAudio(1, 30);
            }
        });

        mWlMusic.setOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete() {
                if (mWolfMediaEncodec != null) {
                    mWolfMediaEncodec.stopRecode();
                    mWolfMediaEncodec = null;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButton.setText("开始录制");
                        }
                    });
                }
            }
        });

        mWlMusic.setOnShowPcmDataListener(new OnShowPcmDataListener() {
            @Override
            public void onPcmInfo(int samplerate, int bit, int channels) {
                LogHelper.d(TAG, "textureid is " + mWolfCameraView.getTextureId());
                mWolfMediaEncodec = new WolfMediaEncodec(VideoActivity.this, mWolfCameraView.getTextureId());
                mWolfMediaEncodec.setOnMediaInfoListener(VideoActivity.this);
                mWolfMediaEncodec.initEncodec(mWolfCameraView.getEGLContext(),
                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4",
                        UiUtils.getScreenWidthPixels(VideoActivity.this), UiUtils.getScreenHeightPixels(VideoActivity.this), samplerate, channels);
                mWolfMediaEncodec.startRecode();
            }

            @Override
            public void onPcmData(byte[] pcmdata, int size, long clock) {
                if (mWolfMediaEncodec != null) {
                    mWolfMediaEncodec.putPCMData(pcmdata, size);
                }
            }
        });
    }

    public void record(View view) {
        if (mWolfMediaEncodec == null) {
            LogHelper.i(TAG, "textureId is = " + mWolfCameraView.getTextureId());
            mWlMusic.setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/cd.mp3");// 这里不能直接传入aac格式的文件 wlMusic是有问题的
            mWlMusic.prePared();
            mButton.setText("正在录制");
        } else {
            mWolfMediaEncodec.stopRecode();
            mButton.setText("开始录制");
            mWolfMediaEncodec = null;
            mWlMusic.stop();
        }
    }

    @Override
    public void onMediaTime(int time) {
        LogHelper.i(TAG, "time is = " + time);
    }
}
