package com.ggwolf.opengl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ggwolf.opengl.study.MySurfaceView;

public class ES3SampleActivity extends AppCompatActivity {


    private MySurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_es3_sample);
        mGLSurfaceView = findViewById(R.id.my_gl_surface_view);
        mGLSurfaceView.getGLRender().init();
        init();
    }

    private void init() {

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGLSurfaceView.getGLRender().unInit();
    }
}
