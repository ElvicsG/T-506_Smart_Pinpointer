package com.kehui.www.testapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by jwj on 2018/6/6.
 * 用户页面自定义圆
 */

public class CustomCircleView extends View {
    private Paint mPaint;
    private Context mContext;
    private int screenWidth;
    private int screenHeight;
    private int radial = 50;
    private String circleColor;

    private float mViewCenterX;
    private float mViewCenterY;

    private int paintWidth;

    public CustomCircleView(Context context) {
        super(context);
        init();
    }

    public CustomCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        mPaint = new Paint();
        //设置画笔为抗锯齿
        mPaint.setAntiAlias(true);
        //设置颜色
        mPaint.setColor(Color.RED);
        //画笔样式分
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(20);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewCenterX = getWidth() / 2;
        mViewCenterY = getHeight() / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(mViewCenterX, mViewCenterY, radial, mPaint);
    }

    public void setCircleColor(String color) {
        this.circleColor = color;
    }

    public void setPaintWidth(int paintWidth) {
        this.paintWidth = paintWidth;
    }

    public void updateView(String circleColor, int paintWidth,int radial) {
        setCircleColor(circleColor);
        setPaintWidth(paintWidth);
        mPaint.reset();
        //设置画笔为抗锯齿
        mPaint.setAntiAlias(true);
        //设置颜色
        mPaint.setColor(Color.parseColor(circleColor));
        //画笔样式分
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(paintWidth);
        this.radial=radial;
        invalidate();

    }
}
