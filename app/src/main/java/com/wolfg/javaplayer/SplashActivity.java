package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ggwolf.audioplayer.AudioPlayerActivity;
import com.ggwolf.imagedeal.ImageCopyActivty;
import com.ggwolf.opengl.ES3SampleActivity;
import com.wolfg.javaplayer.view.CustomerActivity;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initPermission();
    }

    private void initPermission() {
        int result = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == -1) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ffmpeg_btn:
//                startActivity(new Intent(this, FFmpegActivity.class));
                break;
            case R.id.c_thread:
//                startActivity(new Intent(this, CDemoActivity.class));
                break;
            case R.id.audio_player:
                startActivity(new Intent(this, AudioPlayerActivity.class));
                break;
            case R.id.customer_view:
                startActivity(new Intent(this, CustomerActivity.class));
                break;
            case R.id.opengl3_sample:
                startActivity(new Intent(this, ES3SampleActivity.class));
                break;
            case R.id.imagedeal_sample:
                startActivity(new Intent(this, ImageCopyActivty.class));
                break;
        }
    }
}
