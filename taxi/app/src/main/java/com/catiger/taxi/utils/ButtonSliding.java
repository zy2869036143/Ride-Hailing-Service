package com.catiger.taxi.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;

import com.catiger.taxi.R;

/**
 * 滑动进入
 *
 * @author chen
 */
public class ButtonSliding extends androidx.appcompat.widget.AppCompatTextView {
    /**
     * 文本颜色画笔
     */
    private Paint paintText;
    /**
     * 背景弧画笔
     */
    private Paint paintRect;
    /**
     * 按钮画笔
     */
    private Paint paintCircle;
    private Context context;
    /**
     * 背景弧颜色
     */
    private int boundColor = Color.RED;
    /**
     * 文本弧颜色
     */
    private int mTextColor = Color.BLACK;

    private Bitmap bitmap;
    /**
     * 按钮背景
     */
    private int drawableId;

    private boolean isColorSrc = true;

    public ButtonSliding(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ButtonSliding);
        boundColor = typedArray.getColor(R.styleable.ButtonSliding_boundColor,boundColor);
        mTextColor = typedArray.getColor(R.styleable.ButtonSliding_boundColor,mTextColor);
        drawableId = typedArray.getResourceId(R.styleable.ButtonSliding_btnSrc, 0);
        isColorSrc = typedArray.getBoolean(R.styleable.ButtonSliding_typeColor, isColorSrc);
        if (isColorSrc) {

        } else {
            if(drawableId == 0)
                throw new IllegalArgumentException("资源设置null");
            bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        }


        typedArray.recycle();
        init();
    }

    /**
     * 初始化画笔
     */
    private void init() {
        paintText = new Paint();
        paintText.setDither(true);
        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(getTextSize());
        paintText.setColor(mTextColor);

        paintRect = new Paint();
        paintRect.setDither(true);
        paintRect.setAntiAlias(true);
        paintRect.setColor(boundColor);
        paintRect.setStyle(Paint.Style.STROKE);
        paintRect.setStrokeWidth(pxToDp(2));

        paintCircle = new Paint();
        paintCircle.setDither(true);
        paintCircle.setAntiAlias(true);
        paintCircle.setStyle(Paint.Style.FILL);
        paintCircle.setStrokeWidth(pxToDp(2));
    }

    /**
     * 测量处理,默认情况
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽高的模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //指定宽高的大小
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //如果宽高设置为wrap_content时，刚默认为300
        if (widthMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.AT_MOST) {
            height = 200;
            width = height * 4;
        }

        //如果宽高不一致时，则以高为标准
        if (width != height) {
            width = height * 4;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
        drawText(canvas);
//        drawButton(canvas);
        if (isColorSrc) {
            drawButton(canvas);
        } else {
            drawBitmap(canvas);
        }
    }

    /**
     * 按钮X(left)与Y(top)的值
     */
    private int btnBitmapX;
    private int btnBitmapY;
    /**
     * 按钮X(right)与Y(bottom)的值
     */
    private int btnBitmapX2;
    private int btnBitmapY2;

    /**
     * 绘制图片的按钮
     * @param canvas
     */
    private void drawBitmap(Canvas canvas) {

        RectF rectF = new RectF(btnBitmapX, btnBitmapY, btnBitmapX2, btnBitmapY2);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        canvas.drawBitmap(bitmap, rect, rectF, paintCircle);
    }

    /**
     * 缩放图片
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public Bitmap getNewBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        // 获得图片的宽高.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return newBitmap;
    }


    /**
     * 创建新的圆角图片
     * @param bitmap
     * @param px
     * @return
     */
    public  Bitmap makeRoundCorner(Bitmap bitmap, int px)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, px, px, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /**
     * 绘制按钮
     *
     * @param canvas
     */
    private void drawButton(Canvas canvas) {
        canvas.drawCircle(btnX, btnY, btnRadus, paintCircle);
    }

    /**
     * 绘制背景弧
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {


        RectF rectF = new RectF(x - x, y - y, x + x, y + y);

        canvas.drawRoundRect(rectF, 100, 100, paintRect);
    }

    private float pxToDp(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    /**
     * 按钮半径
     */
    private int btnRadus;

    /**
     * 绘制文本
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        String text = (String) getText();
        //获取文本的宽高
        Rect rect = new Rect();
        paintText.getTextBounds(text.trim(), 0, text.trim().length(), rect);
        int dx = getWidth() / 2 - rect.width() / 2 + btnRadus;
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        //基线
        float baseline = getHeight() / 2 + (fontMetrics.top - fontMetrics.bottom) / 2 - fontMetrics.top;
        //绘制文本
        canvas.drawText(text, dx, baseline, paintText);
    }

    /**
     * 中心点
     */
    private int y;
    private int x;
    /**
     * 按钮中心点
     */
    private int btnX;
    private int btnY;
    /**
     * 按钮与背景弧的间隙
     */
    private int btnBound = 10;

    /**
     * 背景的图片
     */
    private int bitmapRadus;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //数值太多选择在测量时初始化，在构造函数时有些数值拿不到，draw刷新时又会被重新赋值，所以选择在此初始化
        //初始化中心点
        x = (right - left) / 2;
        y = (bottom - top) / 2;
        //初始化中心点
        btnRadus = (y + y) / 2 - btnBound;
        //按钮的X,Y
        btnX = btnRadus + btnBound;
        btnY = y;

        //按钮图片的X,Y
        btnBitmapX = btnBound;
        btnBitmapY = btnBound;

        btnBitmapX2 = btnRadus + btnRadus + btnBound;
        btnBitmapY2 = btnRadus + btnRadus + btnBound;
        //按钮图片与弧的间隙
        bitmapRadus = btnBitmapX2 - btnBitmapX;
        if(!isColorSrc){
            bitmap = getNewBitmap(bitmap, btnRadus + btnRadus, btnRadus + btnRadus);
            bitmap = makeRoundCorner(bitmap,100);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int X = (int)event.getX();
        switch (event.getAction()) {
            //手指移动时
            case MotionEvent.ACTION_MOVE:

                if(isColorSrc){
                    btnX = (X) ;

                    //到达右端一定距离时调用接口
                    if (btnX >= x + x - btnBound - btnRadus - 5) {
                        buttonSildingEvent.onOver();
                    }
                    //如果超出控件范围则不调用刷新
                    if (btnX >= x + x - btnBound - btnRadus || btnX <= btnRadus + btnBound)
                        break;
                    //如果在控件范围则不调用刷新
                    if (btnX <= x + x - btnRadus - btnBound && btnX >= btnRadus + btnBound)
                        postInvalidate();
                }else {
                    btnBitmapX2 = (X);
                    //同上
                    if (btnBitmapX2 >= x + x  || btnBitmapX2 <= btnRadus + btnBound) {
                        break;
                    }
                    //同上

                    if (btnBitmapX2 >= x + x - btnBound - btnRadus - 5) {
                        buttonSildingEvent.onOver();
                    }
                    //同上

                    if (btnBitmapX2 <= x + x - btnRadus - btnBound && btnBitmapX2 >= btnRadus + btnBound)
                        postInvalidate();
                }

                break;

                //手指抬起时
            case MotionEvent.ACTION_UP:
                if(isColorSrc){
                    btnX = (X);
                    //如果超出控件边界，则给定默认值
                    if (btnX > x + x - btnRadus)
                        btnX = x + x - btnRadus;
                    //如果超出控件边界，则给定默认值
                    if (btnX < btnRadus + btnBound)
                        btnX = btnRadus + btnBound;
                    reset(btnX);

                }else {
                    btnBitmapX2 = (X);
                    btnBitmapX = (X) - bitmapRadus;
                    //同上
                    if (btnBitmapX2 > x + x - btnRadus)
                        btnBitmapX2 = x + x - btnRadus;
                    //同上
                    if (btnBitmapX2 < btnRadus + btnBound)
                        btnBitmapX2 = btnRadus + btnBound;

                    reset(btnBitmapX2);

                }

                break;
        }
        return true;
    }

    /**
     * 松手回弹
     */
    private void reset(int start) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, btnRadus + btnBound);
        valueAnimator.setDuration(1000);
        valueAnimator.setInterpolator(new BounceInterpolator());
        valueAnimator.start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(isColorSrc){
                    //X的值，Y不用管
                    btnX = (int) animation.getAnimatedValue();

                }else {
                    //图片是由Rect决定绘制图片的位置，所以要赋值X与X2
                    btnBitmapX2 = (int) animation.getAnimatedValue();
                    btnBitmapX = (int) animation.getAnimatedValue() - bitmapRadus;

                    if(btnBitmapX <= 0){
                        btnBitmapX = btnBound;
                        btnBitmapX2 = btnRadus + btnRadus + btnBound;
                    }
                }
                //刷新
                invalidate();
            }
        });
    }

    private ButtonSildingEvent buttonSildingEvent;

    /**
     * 设置完成的监听
     * @param buttonSilding
     */
    public void setOnListener(ButtonSildingEvent buttonSilding) {
        this.buttonSildingEvent = buttonSilding;
    }

    /**
     * 按钮滑动到右端的接口
     */
    public interface ButtonSildingEvent {
        void onOver();
    }
}

