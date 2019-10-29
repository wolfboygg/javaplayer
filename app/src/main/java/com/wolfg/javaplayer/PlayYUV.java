package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.wolfg.javaplayer.opengl.OpenGLController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlayYUV extends AppCompatActivity {

    private OpenGLSurfaceView mOpenGLSurfaceView;
    private OpenGLController mOpenGLController;

    private boolean isExit = true;
    private FileInputStream fis = null;

    private String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/output.yuv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_yuv);
        mOpenGLSurfaceView = findViewById(R.id.surface);
        mOpenGLController = new OpenGLController();
        mOpenGLSurfaceView.setOpenGLController(mOpenGLController);
    }

    public void play(View view) {
        if (isExit) {
            isExit = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int width = 368;
                    int height = 640;
                    try {
                        fis = new FileInputStream(new File(path));
                        byte[] y = new byte[width * height];
                        byte[] u = new byte[width * height / 4];
                        byte[] v = new byte[width * height / 4];
                        while (true) {
                            if (isExit) {
                                break;
                            }
                            int ysize = fis.read(y);
                            int usize = fis.read(u);
                            int vsize = fis.read(v);
                            if (ysize > 0 && usize > 0 && vsize > 0) {
                                mOpenGLController.setYUVData(y, u, v, width, height);
                                Thread.sleep(40);
                            } else {
                                isExit = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                }
            }).start();
        }
    }

    public void stop(View view) {
        isExit = true;
    }
}
