package com.catiger.driver.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.catiger.driver.R;

/**
 * Created by Administrator on 2017.05.27.0027.
 */

public class MyToggleButton extends View {

private Bitmap bgBitmap;
private Bitmap slidebg;
private final int viewWidth;
private final int viewheight;
private float slidebgleft;
private final int slideWidth;
private final int slideMaxLeft;
//设置一个成员变量,用来判定开关的状态
private boolean toggleStste = false;
private boolean canChangeToggleState = false;

private onToggleStateChangedListener monToggleStateChangedListener = null;

//创建一个开关状态改变的监听,当状态改变时触发,否则不触发
public void setOnToggleStateChangedListener(onToggleStateChangedListener monToggleStateChangedListener) {
 this.monToggleStateChangedListener = monToggleStateChangedListener;
}


public MyToggleButton(Context context, AttributeSet attrs) {
 super(context, attrs);
 //设置按钮的背景和滑块资源
 setBackgroundAndSlideResource(R.drawable.button_red,R.drawable.slidelock_bg);
 //获取背景的高度和宽度
 viewWidth = bgBitmap.getWidth();
 viewheight = bgBitmap.getHeight();
 //背景的宽和高就是这个自定义按钮的宽和高
 //获取滑块的宽度
 slideWidth = slidebg.getWidth();
 //计算滑块的右边最大值
 slideMaxLeft = viewWidth - slideWidth;
}

//定义一个方法,用来显示按钮是开还是关
public void setToggleStste(boolean toggleStste) {
 this.toggleStste = toggleStste;
 if (toggleStste) {
  slidebgleft = slideMaxLeft;
 } else {
  slidebgleft = 0;
 }
 //重新绘制
 invalidate();
}

//设置按钮的背景和滑块资源
private void setBackgroundAndSlideResource(int toogle_background, int toogle_slidebg) {
 bgBitmap = BitmapFactory.decodeResource(getResources(), toogle_background);
 slidebg = BitmapFactory.decodeResource(getResources(), toogle_slidebg);
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//  super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 //调用setMeasuredDimension绘制按钮的区域
 setMeasuredDimension(viewWidth, viewheight);
}

@Override
protected void onDraw(Canvas canvas) {

 //重写drawBitmap,将控件的背景和滑块绘制到页面中
 canvas.drawBitmap(bgBitmap, 0, 0, null);
 drawSlide(canvas);

}

//通过控制slidebgleft,来控制滑块的位置
private void drawSlide(Canvas canvas) {
 //限制滑块的运行区间,防止滑块移动到界外
 if (slidebgleft < 0) {
  slidebgleft = 0;
 } else if (slidebgleft > slideMaxLeft) {
  slidebgleft = slideMaxLeft;
 }
 canvas.drawBitmap(slidebg, slidebgleft, 0, null);
 if (canChangeToggleState) {
  canChangeToggleState = false;
  //记录上一次开关的状态
  boolean lastToggleState = toggleStste;
  //根据当前滑块的位置更新开关的状态
  if (slidebgleft == 0) {
   toggleStste = false;
  } else {
   toggleStste = true;
  }

  //如果当前的状态与上一次状态不同时,才会触发监听事件
  if (lastToggleState != toggleStste && monToggleStateChangedListener != null) {
   monToggleStateChangedListener.onToggleStateChange(this, toggleStste);
  }
 }
}

//设置按钮的触摸事件
@Override
public boolean onTouchEvent(MotionEvent event) {
 switch (event.getAction()) {
  case MotionEvent.ACTION_DOWN:
   slidebgleft = event.getX() - slideWidth / 2;
   break;
  case MotionEvent.ACTION_MOVE:
   slidebgleft = event.getX() - slideWidth / 2;
   break;
  case MotionEvent.ACTION_UP:
   if (event.getX() > viewWidth / 2) {
    slidebgleft = slideMaxLeft;
   } else {
    slidebgleft = 0;
   }
   //只有当手机离开屏幕的是否才可以触发监听
   canChangeToggleState = true;
   break;
 }
 //重复不断地绘制
 invalidate();
 return true;
}

interface onToggleStateChangedListener {
 void onToggleStateChange(MyToggleButton button, boolean isToggleOn);
}
}