package com.ggwolf.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.SeekBar;
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

    private SeekBar mSeekBar;

    private int position;
    private boolean isSeek = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
        mTimeInfo = findViewById(R.id.time_info);
        mSeekBar = findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (AudioPlayer.getInstance().getDuration() > 0 && isSeek)  {
                    position = progress * AudioPlayer.getInstance().getDuration() / 100;
//                    AudioPlayer.getInstance().seek(position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeek = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogHelper.i("guo","11111111");
                AudioPlayer.getInstance().seek(position);
                isSeek = false;
            }
        });
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
        AudioPlayer.getInstance().setOnPauseResumeListener(pause -> {
            if (pause) {
                LogHelper.i(TAG, "audio play state is pause");
            } else {
                LogHelper.i(TAG, "audio paly state is resume");
            }
        });
        AudioPlayer.getInstance().setOnTimeInfoListener(timeInfo -> {
            // 显示信息 这里是在自线程回调回来的，需要到主线程进行处理
//            LogHelper.i(TAG, "timeInfo.toString():" + timeInfo.toString());
            runOnUiThread(() -> {
                if (!isSeek) {
                    mTimeInfo.setText(TimeUtils.secdsToDateFormat(timeInfo.getCurrentTime(), timeInfo.getTotalTime()) + "/"
                            + TimeUtils.secdsToDateFormat(timeInfo.getTotalTime(), timeInfo.getTotalTime()));

                    // 设置到seekbar上
                    mSeekBar.setProgress(timeInfo.getCurrentTime() * 100 / timeInfo.getTotalTime());
                }

            });
        });

        AudioPlayer.getInstance().setOnErrorListener((code, msg) -> {
            LogHelper.i(TAG, "code :" + code + "-->msg:" + msg);
        });

        AudioPlayer.getInstance().setOnCompleteListener(() -> {
            LogHelper.i(TAG, "播放完成");
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
        } else if (id == R.id.audio_stop) {
            AudioPlayer.getInstance().stop();
        } else if (id == R.id.audio_seek) {
            AudioPlayer.getInstance().seek(300);
        } else if (id == R.id.audio_next) {
            AudioPlayer.getInstance().playNext(Environment.getExternalStorageDirectory().getAbsolutePath() + "/next.mp3");
        }
    }
}
