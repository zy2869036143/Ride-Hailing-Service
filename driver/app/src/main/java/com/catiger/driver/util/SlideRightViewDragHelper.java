package com.catiger.driver.util;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.customview.widget.ViewDragHelper;

public class SlideRightViewDragHelper extends LinearLayout {
    private ViewDragHelper viewDragHelper;
    private View child;
    private Point childPosition = new Point();
    private Point childEndPosition = new Point();
    private OnReleasedListener onReleasedListener;
    private int oldX;
    private int screenWidth;//屏幕宽
    private int screenWidthto = 70;//当前滑动比例  大于这个值时滑动完成、小于时回弹

    public SlideRightViewDragHelper(Context context, AttributeSet attrs) {
        super(context, attrs);
        screenWidth = getResources().getDisplayMetrics().widthPixels;//获取屏幕宽
        //新建viewDragHelper ,viewGroup, 灵敏度，回调(子view的移动)
        viewDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return true;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                oldX = left;
                return Math.max(0, left);
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                //滑动比例大于70，视为滑动完成
                if ((oldX*100/screenWidth) > screenWidthto) {
                    viewDragHelper.settleCapturedViewAt(childPosition.x, childPosition.y); //不管是滑动成功还是失败，都必须反弹
                    invalidate();
                    	//滑动完成的回调
                    if (onReleasedListener != null){
                        onReleasedListener.onReleased();
                    }
                    //下面是不反弹的代码，根据自己的需求自行改动
                    
//                    viewDragHelper.settleCapturedViewAt(childEndPosition.x, childEndPosition.y);不反弹的代码
//                    invalidate(); //必须刷新,因为其内部使用的是mScroller.startScroll，所以别忘了需要invalidate()以及结合computeScroll方法一起。
					
				
                } else {
                    viewDragHelper.settleCapturedViewAt(childPosition.x, childPosition.y); //反弹
                    invalidate();
                }
                super.onViewReleased(releasedChild, xvel, yvel);
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        child = getChildAt(0);
    }

    @Override   //用viewDragHelper拦截-true
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override  //viewDragHelper拦截事件
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //定位一开始的坐标
        childPosition.x = child.getLeft();
        childPosition.y = child.getTop();
        //滑动成功后定位坐标
        childEndPosition.x = child.getRight();
        childEndPosition.y = child.getTop();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (viewDragHelper.continueSettling(true)) {
            invalidate();
        }
    }
    
	//调用此方法进行回调
    public void setOnReleasedListener(OnReleasedListener onReleasedListener) {
        this.onReleasedListener = onReleasedListener;
    }
	//滑动完成的回调接口
    public interface OnReleasedListener {
        void onReleased();
    }

}
