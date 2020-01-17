package com.ggwolf.ffmpeglibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ggwolf.ffmpeglibrary.view.PlayView;

public class FFmpegActivity extends AppCompatActivity {
    private static final String TAG = "FFmpegActivity";

    private TextView mTvVideoInfo;

    private Button mBtnStart;
    private PlayView mPlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);
        mTvVideoInfo = findViewById(R.id.tv_video_info);
        findViewById(R.id.get_protocol_btn).setOnClickListener(this::onClick);
        findViewById(R.id.get_format_btn).setOnClickListener(this::onClick);
        findViewById(R.id.get_codec_btn).setOnClickListener(this::onClick);
        findViewById(R.id.get_filter_btn).setOnClickListener(this::onClick);
        findViewById(R.id.pcm_start).setOnClickListener(this::onClick);

        mBtnStart = findViewById(R.id.start_btn);
        mBtnStart.setOnClickListener(this::onClick);
        mPlayView = findViewById(R.id.play_view);
    }


    public void onClick(View view) {
        int id = view.getId();
        String info = null;
        if (id == R.id.get_protocol_btn) {
            info = MediaHelper.getInstance().urlprotocolinfo();
        } else if (id == R.id.get_format_btn) {
            info = MediaHelper.getInstance().avformatinfo();
        } else if (id == R.id.get_codec_btn) {
            info = MediaHelper.getInstance().avcodecinfo();
        } else if (id == R.id.get_filter_btn) {
            info = MediaHelper.getInstance().avfilterinfo();
        } else if (id == R.id.start_btn) {
            // test only pressed once
            new Thread(mPlayView).start();
            return;
        } else if (id == R.id.pcm_start) {
            MediaHelper.getInstance().playPcm();
            return;
        }

        Log.i(TAG, "info is :" + info);
        if (!TextUtils.isEmpty(info)) {
            mTvVideoInfo.setText(info);
        }
    }




}
