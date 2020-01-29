package com.ggwolf.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.ggwolf.audioplayer.listener.OnLoadListener;
import com.ggwolf.audioplayer.utils.LogHelper;

/**
 * 使用ffmpeg解码视频文件，然后使用opensl进行播放
 */
public class AudioPlayerActivity extends AppCompatActivity {
    private static final String TAG = "AudioPlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);
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
