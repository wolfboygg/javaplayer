package com.wolfgg.cameralibrary.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wolfgg.cameralibrary.R;
import com.wolfgg.filterlibrary.utils.LogHelper;

/**
 * 一个封装好对相机ui的操作
 */

public class WolfCameraSurfaceView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = "WolfCameraSurfaceView";

    private WolfCameraView mWolfCameraView;
    private ImageView ivFocus;
    private ImageView mCameraChange;

    private double mTargetAspect = -1.0;


    public WolfCameraSurfaceView(Context context) {
        this(context, null);
    }

    public WolfCameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public WolfCameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = View.inflate(getContext(), R.layout.layout_camera, null);
        mWolfCameraView = view.findViewById(R.id.wolfcameraview);
        ivFocus = view.findViewById(R.id.iv_focus);
        mCameraChange = view.findViewById(R.id.camera_change);
        mCameraChange.setOnClickListener(this);
        removeAllViews();
        addView(view);
    }

    public void previewAngle() {
        mWolfCameraView.previewAngle(getContext());
    }

    public void onDestroy() {
        mWolfCameraView.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogHelper.i(TAG, "onTouchEvent..");
        if (event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_DOWN) {
            startAutoFocus(event.getX(), event.getY());
        }
        return true;
    }

    private void startAutoFocus(float x, float y) {
        if (mWolfCameraView == null) {
            return;
        }
        LogHelper.i(TAG, "startAutoFocus x = " + x + "--> y = " + y);
        if (x != -1 && y != -1) { // 这里显示一个对焦动画
            ivFocus.setTranslationX(x - (ivFocus.getWidth()) / 2);
            ivFocus.setTranslationY(y - (ivFocus.getWidth()) / 2);
            ivFocus.clearAnimation();

            // 执行动画
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivFocus, "scaleX", 1.75f, 1.0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivFocus, "scaleY", 1.75f, 1.0f);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleX).with(scaleY);
            set.setDuration(500);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    ivFocus.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ivFocus.setVisibility(View.GONE);
                }
            });
            set.start();
        }
        mWolfCameraView.startAutoFocus();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTargetAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            int horizPadding = getPaddingLeft() + getPaddingRight();
            int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = mTargetAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) < 0.01) {
            } else {
                if (aspectDiff > 0) {
                    initialHeight = (int) (initialWidth / mTargetAspect);
                } else {
                    initialWidth = (int) (initialHeight * mTargetAspect);
                }
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onClick(View v) {
    }
}
