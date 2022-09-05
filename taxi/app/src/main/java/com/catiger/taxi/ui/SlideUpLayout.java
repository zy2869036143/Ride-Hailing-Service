package com.catiger.taxi.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class SlideUpLayout extends LinearLayout {

    /**
     * 变量
     */
    private View bar;
    private View content;
    private Scroller scroller;
    private int downY;

    /**
     * 构造
     */
    public SlideUpLayout(Context context) {
        this(context, null);
    }

    public SlideUpLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 尺寸
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scroller = new Scroller(getContext());
        bar = getChildAt(0);
        content = getChildAt(1);
    }

    /**
     * 位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //bar置底
        bar.layout(0, getMeasuredHeight() - bar.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight());
        //content隐藏
        content.layout(0, getMeasuredHeight(), getMeasuredWidth(), bar.getBottom() + content.getMeasuredHeight());
    }

    /**
     * 触控
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
                //记录位置
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE://移动
                //移动偏移
                int offsetY = (int) event.getY() - downY;
                //滚动位置
                int toScroll = getScrollY() - offsetY;
                //是否滚动位置超出限制
                if (toScroll < 0) {//最小限制
                    toScroll = 0;
                } else if (toScroll > content.getMeasuredHeight()) {//最大限制
                    toScroll = content.getMeasuredHeight();
                }
                //滚动
                scrollTo(0, toScroll);
                //记录位置
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP://松开
                //滚动偏移
                int offsetScroll = getScrollY();
                //是否滚动偏移超过实际高度的一半
                if (offsetScroll > content.getMeasuredHeight() / 2) {//显示
                    scroller.startScroll(getScrollX(), getScrollY(), 0, content.getMeasuredHeight() - offsetScroll, 500);
                } else {//隐藏
                    scroller.startScroll(getScrollX(), getScrollY(), 0, -offsetScroll, 500);
                }
                //刷新界面
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 滚动
     */
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {//滚动已完成
            //计算位置并滚动
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            //刷新界面
            invalidate();
        }
    }

}