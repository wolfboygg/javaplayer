package com.wolfgg.cameralibrary.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.wolfgg.filterlibrary.utils.UiUtils;

import java.util.List;

/**
 * 相机的处理类
 */

public class WolfCamera {
    private Camera mCamera;

    private SurfaceTexture mSurfaceTexture;

    private int width;
    private int height;

    private boolean isFocusing = false;

    public WolfCamera(Context context) {
        this.width = UiUtils.getScreenWidthPixels(context);
        this.height = UiUtils.getScreenHeightPixels(context);
    }

    public void initCamera(SurfaceTexture surfaceTexture, int cameraId) {
        this.mSurfaceTexture = surfaceTexture;
        setCameraParm(cameraId);
    }

    private void setCameraParm(int cameraId) {
        try {
            mCamera = Camera.open(cameraId);
            mCamera.setPreviewTexture(mSurfaceTexture);
            Camera.Parameters parameters = mCamera.getParameters();

            parameters.setFlashMode("off");// 设置闪光灯
            parameters.setPreviewFormat(ImageFormat.NV21); // 设置相机格式

            Camera.Size size = getFitSize(parameters.getSupportedPictureSizes());
            parameters.setPictureSize(size.width, size.height);

            size = getFitSize(parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(size.width, size.height);

            // 添加自动对焦功能
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }

            mCamera.setParameters(parameters);
            mCamera.startPreview();

            // 进行对焦
            isFocusing = false;
            startAutoFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void startAutoFocus() {
        if (mCamera != null && !isFocusing) {
            mCamera.cancelAutoFocus();
            isFocusing = true;
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    isFocusing = false;
                }
            });
        }
    }


    public void stopPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void changeCamera(int cameraId) { // 首先先关闭原来的预览，然后在重新开启
        if (mCamera != null) {
            mCamera.stopPreview();
        }
        setCameraParm(cameraId);
    }


    private Camera.Size getFitSize(List<Camera.Size> sizes) {
        if (width < height) {// 交换宽和高
            int temp = width;
            width = height;
            height = temp;
        }

        for (Camera.Size size : sizes) {
            if (size.width * 1.0f / size.height == 1.0f * width / height) {
                return size;
            }
        }
        return sizes.get(0); // 如果没有取到完全相同的，就应该使用比例最接近的。但这里没有处理
    }


}
