package com.wolfg.javaplayer.view.scroller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.ggwolf.audioplayer.utils.LogHelper;

/**
 * 测试view的一下主要方法属性等等
 * <p>
 * 如果父view就是整个屏幕， 那么getLeft()==getRawX-getX  getLeft是view的左边到父view的距离
 *
 *
 *  view滑动的问题
 *  1.layout
 *  2.offsetLeftAndRight() / offsetTopAndBottom()
 *  3.LayoutParams
 *  4.动画
 *  5.scrollTo /scrollBy
 *  6.Scroller
 *
 *
 */

public class CustomerView extends View {

    private static final String TAG = "CustomerView";

    private int lastX = 0;
    private int lastY = 0;

    private Scroller mScroller;

    public CustomerView(Context context) {
        this(context, null);
    }

    public CustomerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CustomerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        mScroller = new Scroller(context);
    }

    private void init() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction();

        int x = (int) event.getX(); // 记录手指落下的位置
        int y = (int) event.getY();
        LogHelper.i(TAG, "this.getX():" + this.getX());  // this.getX() == getLeft()
        LogHelper.i(TAG, "this.getY():" + this.getY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 获取对应的getX, getY， getRawX， getRawY的值
                LogHelper.i(TAG, "getX:" + event.getX());
                LogHelper.i(TAG, "getY:" + event.getY());
                LogHelper.i(TAG, "getRawX:" + event.getRawX());
                LogHelper.i(TAG, "getRawY:" + event.getRawY());

                LogHelper.i(TAG, "getRawX()-getX():" + (event.getRawX() - event.getX()));

                lastX = x;
                lastY = y;


                break;

            case MotionEvent.ACTION_MOVE:

                int offsetX = x - lastX;
                int offsetY = y - lastY;

                // 然后进行移动
//                layout(getLeft() + offsetX, getTop() + offsetY, getRight() + offsetX, getBottom() + offsetY);

                // offsetLeftAndRight()
//                offsetLeftAndRight(offsetX);
//                offsetTopAndBottom(offsetY);

                ((View) getParent()).scrollBy(-offsetX, -offsetY);
//                ((View) getParent()).scrollTo(-lastX, -lastY);// 这就是我们说的需要从头开始进行滚动
                // 所以拖拽需要使用scrollBy进行操作。如果需要一次性滚动就使用scrollTo， 直接滚动到某个点
                lastX = x;
                lastY = y;


                break;

            case MotionEvent.ACTION_UP:
                // 让当前view滚动到原来的位置
                View viewGroup = (View) getParent();
                mScroller.startScroll(viewGroup.getScrollX(), viewGroup.getScrollY(),
                        -viewGroup.getScrollX(), -viewGroup.getScrollY());
                // 它返回的是当前View视图左上角坐标与View视图初始位置x轴方向上的距离。
                break;


        }
        // 如果想要获取到真正的left的值需要在onMeasure中进行获取，否则是获取不到的
        LogHelper.i(TAG, "getLeft():" + this.getLeft());
        LogHelper.i(TAG, "getRight():" + this.getRight());
        LogHelper.i(TAG, "getRight()-getLeft():" + (this.getRight() - getLeft()));
        LogHelper.i(TAG, "view width:" + (this.getRight() - getLeft()) / 3);// 这里获取到的是dp值，大概等于200dp 这里获取到是px值。
        return true;
    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        // 需要判断是否滚动完成
        if (mScroller.computeScrollOffset()) { // 表示可以滚动
            // 然后进行滚动
            ((View)getParent()).scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        }
        invalidate();
    }
}
