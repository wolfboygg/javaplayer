package com.wolfgg.cameralibrary.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;

import com.wolfgg.filterlibrary.utils.LogHelper;
import com.wolfgg.filterlibrary.view.WolfEGLSurfaceView;

/**
 * 一个用来显示相机预览的界面
 */
public class WolfCameraView extends WolfEGLSurfaceView {

    private static final String TAG = "WolfCameraView";

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
        initRender(context);
    }

    private void initRender(Context context) {
        mWolfCameraRender = new WolfCameraRender(context);
        mWolfCamera = new WolfCamera(context);
        setRender(mWolfCameraRender);
        previewAngle(context);
        setRenderMode(WolfEGLSurfaceView.RENDERMODE_CONTINUOUSLY); // 连续刷新
        mWolfCameraRender.setOnSurfaceCreateListener(new WolfCameraRender.OnSurfaceCreateListener() {
            @Override
            public void onSurfaceCreate(SurfaceTexture surfaceTexture, int textureId) {
                // 设置给相机
                mWolfCamera.initCamera(surfaceTexture, cameraId);
            }
        });
    }

    // 设置预览的角度
    public void previewAngle(Context context) {
        // 通过获取当前activity的角度来进行角度变化
        int angle = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        LogHelper.i(TAG, "activity angle is = " + angle);
        // 先将矩阵还原
        mWolfCameraRender.resetMatrix();
        switch (angle) {
            case Surface.ROTATION_0:
                LogHelper.i(TAG, "angle is 0");
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mWolfCameraRender.setAngle(90, 0, 0, 1); //
                    mWolfCameraRender.setAngle(180, 1, 0, 0);// x轴进行旋转是进行上下颠倒 镜像
                } else {
                    mWolfCameraRender.setAngle(90, 0, 0, 1); //
                }
                break;
            case Surface.ROTATION_90:
                LogHelper.i(TAG, "angle is 90");
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mWolfCameraRender.setAngle(180, 0, 0, 1);
                    mWolfCameraRender.setAngle(180, 0, 1, 0);

                } else {
                    mWolfCameraRender.setAngle(90f, 0, 0, 1);
                }
                break;
            case Surface.ROTATION_180:
                LogHelper.i(TAG, "angle is 180");
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mWolfCameraRender.setAngle(90f, 0f, 0f, 1f);
                    mWolfCameraRender.setAngle(180, 0f, 1f, 0f);
                } else {
                    mWolfCameraRender.setAngle(-90, 0f, 0f, 1f);
                }
                break;
            case Surface.ROTATION_270:
                LogHelper.i(TAG, "angle is 270");
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mWolfCameraRender.setAngle(180f, 0.0f, 1f, 0f);
                } else {
                    mWolfCameraRender.setAngle(0f, 0f, 0f, 1f);
                }
                break;

        }
    }


    public void onDestroy() {
        if (mWolfCamera != null) {
            mWolfCamera.stopPreview();
        }
    }

    public void startAutoFocus() {
        //后置摄像头才有对焦功能
        if (mWolfCamera != null && cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return;
        }
        mWolfCamera.startAutoFocus();
    }

    public void changeCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mWolfCamera.changeCamera(cameraId);
    }
}
