package com.wolfg.javaplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.wolfgg.filterlibrary.render.WolfTextureRender;
import com.wolfgg.filterlibrary.texture.WolfGLTextureView;
import com.wolfgg.filterlibrary.texture.WolfMutiSurfaceView;

public class TextureActivity extends AppCompatActivity {

    private WolfGLTextureView mWolfGLTextureView;
    private LinearLayout mLyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture);
        mWolfGLTextureView = findViewById(R.id.texture_view);
        mLyContent = findViewById(R.id.rl_content);
        mWolfGLTextureView.getWolfTextureRender().setOnRenderCreateListener(new WolfTextureRender.OnRenderCreateListener() {
            @Override
            public void onCreate(final int textureId) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       WolfMutiSurfaceView wolfMutiSurfaceView = new WolfMutiSurfaceView(TextureActivity.this);
                       wolfMutiSurfaceView.setTextureId(textureId);
                       wolfMutiSurfaceView.setSurfaceAndEglContext(null, mWolfGLTextureView.getEGLContext());
                       mLyContent.addView(wolfMutiSurfaceView);
                   }
               });
            }
        });
    }
}
