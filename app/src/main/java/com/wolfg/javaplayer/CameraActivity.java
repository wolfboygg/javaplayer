package com.wolfg.javaplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.os.Bundle;

import com.wolfgg.cameralibrary.camera.WolfCameraSurfaceView;
import com.wolfgg.cameralibrary.camera.WolfCameraView;

public class CameraActivity extends AppCompatActivity {

    private WolfCameraSurfaceView mWolfCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mWolfCameraView = findViewById(R.id.wolfcameraview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWolfCameraView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWolfCameraView.previewAngle();
    }
}
