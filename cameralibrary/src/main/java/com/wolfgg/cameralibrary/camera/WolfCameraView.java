package com.wolfgg.cameralibrary.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

/**
 * 一个用来显示相机预览的界面
 */
public class WolfCameraView extends WolfEGLSurfaceView {

    private WolfCameraRender mWolfCameraRender;

    private WolfCamera mWolfCamera;

    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    public WolfCameraView(Context context) {
        this(context, null);
    }

    public WolfCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置render
        mWolfCameraRender = new WolfCameraRender(context);
        mWolfCamera = new WolfCamera(context);
        setRender(mWolfCameraRender);
        setRenderMode(WolfEGLSurfaceView.RENDERMODE_CONTINUOUSLY); // 连续刷新
        mWolfCameraRender.setOnSurfaceCreateListener(new WolfCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId) {
                // 设置给相机
                mWolfCamera.initCamera(surfaceTexture, cameraId);
            }
        });
    }

    public void onDestroy() {
        if (mWolfCamera != null) {
            mWolfCamera.stopPreview();
        }
    }

}
