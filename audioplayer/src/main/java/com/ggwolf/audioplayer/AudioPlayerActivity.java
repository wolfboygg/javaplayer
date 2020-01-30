package com.ggwolf.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;

import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.listener.OnTimeInfoListener;
import com.ggwolf.audioplayer.utils.LogHelper;
import com.ggwolf.audioplayer.utils.TimeUtils;

/**
 * 使用ffmpeg解码视频文件，然后使用opensl进行播放
 */
public class AudioPlayerActivity extends AppCompatActivity {
    private static final String TAG = "AudioPlayerActivity";

    private TextView mTimeInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        mTimeInfo = findViewById(R.id.time_info);
        AudioPlayer.getInstance().setSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/out.mp3");
        AudioPlayer.getInstance().setListener(() -> {
            LogHelper.i(TAG, "audio prepared on sucesss");
            AudioPlayer.getInstance().start();
        });
        AudioPlayer.getInstance().setOnLoadListener(load -> {
            if (load) {
                LogHelper.i(TAG, "audio is loading.....");
            } else {
                LogHelper.i(TAG, "audio is playing.....");
            }
        });
        AudioPlayer.getInstance().setOnPauseResumeListener(pause-> {
            if (pause) {
                LogHelper.i(TAG, "audio play state is pause");
            } else {
                LogHelper.i(TAG, "audio paly state is resume");
            }
        });
        AudioPlayer.getInstance().setOnTimeInfoListener(timeInfo -> {
            // 显示信息 这里是在自线程回调回来的，需要到主线程进行处理
            LogHelper.i(TAG, "timeInfo.toString():" + timeInfo.toString());
            runOnUiThread(() -> {
                mTimeInfo.setText(TimeUtils.secdsToDateFormat(timeInfo.getCurrentTime(), timeInfo.getTotalTime()) + "/"
                 + TimeUtils.secdsToDateFormat(timeInfo.getTotalTime(), timeInfo.getTotalTime()));
            });
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.audio_start) {
            AudioPlayer.getInstance().prepared();
        } else if (id == R.id.audio_pause) {
            AudioPlayer.getInstance().pause();
        } else if (id == R.id.audio_resume) {
            AudioPlayer.getInstance().resume();
        }
    }
}
