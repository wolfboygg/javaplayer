package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import com.wolfgg.cameralibrary.camera.WolfCameraView;
import com.wolfgg.cameralibrary.video.WolfBaseMediaEncoder;
import com.wolfgg.cameralibrary.video.WolfMediaEncodec;
import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.utils.UiUtils;

public class VideoActivity extends AppCompatActivity implements WolfBaseMediaEncoder.OnMediaInfoListener {

    public static final String TAG = "VideoActivity";

    private WolfCameraView mWolfCameraView;
    private Button mButton;

    private WolfMediaEncodec mWolfMediaEncodec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mWolfCameraView = findViewById(R.id.cameraview);
        mButton = findViewById(R.id.btn_record);
    }

    public void record(View view) {
        if (mWolfMediaEncodec == null) {
            LogHelper.i(TAG, "textureId is = " + mWolfCameraView.getTextureId());
            mWolfMediaEncodec = new WolfMediaEncodec(this, mWolfCameraView.getTextureId());
            mWolfMediaEncodec.setOnMediaInfoListener(this);
            mWolfMediaEncodec.initEncodec(mWolfCameraView.getEGLContext(),
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/wl_live_pusher.mp4",
                    MediaFormat.MIMETYPE_VIDEO_AVC, UiUtils.getScreenWidthPixels(this), UiUtils.getScreenHeightPixels(this)
            );
            mWolfMediaEncodec.startRecode();
            mButton.setText("正在录制");
        } else {
            mWolfMediaEncodec.stopRecode();
            mButton.setText("开始录制");
            mWolfMediaEncodec = null;
        }
    }

    @Override
    public void onMediaTime(int time) {
        LogHelper.i(TAG, "time is = " + time);
    }
}
