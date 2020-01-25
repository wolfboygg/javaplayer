package com.ggwolf.audioplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

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
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.audio_start) {
            AudioPlayer.getInstance().prepared();
        }
    }
}
