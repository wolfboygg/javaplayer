package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wolfgg.cameralibrary.camera.WolfCameraView;

public class CameraActivity extends AppCompatActivity {

    private WolfCameraView mWolfCameraView;

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


}
