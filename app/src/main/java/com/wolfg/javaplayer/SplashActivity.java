package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ggwolf.ffmpeglibrary.FFmpegActivity;
import com.ggwolf.ffmpeglibrary.demo.CDemoActivity;

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
                startActivity(new Intent(this, FFmpegActivity.class));
                break;
            case R.id.c_thread:
                startActivity(new Intent(this, CDemoActivity.class));
                break;
        }
    }
}
