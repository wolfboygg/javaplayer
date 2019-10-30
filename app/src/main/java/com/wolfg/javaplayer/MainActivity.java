package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import com.wolfg.javaplayer.opengl.OpenGLController;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {

    private OpenGLSurfaceView mOpenGLSurfaceView;
    private OpenGLController mOpenGLController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOpenGLSurfaceView = findViewById(R.id.surface);
        mOpenGLController = new OpenGLController();
        mOpenGLSurfaceView.setOpenGLController(mOpenGLController);
        mOpenGLSurfaceView.setOnSurfaceListener(new OpenGLSurfaceView.OnSurfaceListener() {
            @Override
            public void init() {
                mOpenGLController.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.mingren));
            }
        });
    }

    public void changefilter(View view) {
        mOpenGLController.onChangeFilter();
    }

    public void play(View view) {
        startActivity(new Intent(this, PlayYUV.class));
    }
}
