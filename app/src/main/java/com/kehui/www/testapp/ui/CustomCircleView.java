package com.kehui.www.testapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * 用户页面自定义圆
 * @author jwj
 * @date 2018/06/06
 */
public class CustomCircleView extends View {

    private Paint paint;
    private int radial = 50;
    private float viewCenterX;
    private float viewCenterY;

    public CustomCircleView(Context context) {
        super(context);
        init();
    }

    public CustomCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        //设置画笔为抗锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(Color.RED);
        //画笔样式分
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewCenterX = getWidth() / 2;
        viewCenterY = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(viewCenterX, viewCenterY, radial, paint);
    }

    /**
     * 更新布局绘制
     * @param circleColor   圆圈颜色
     * @param paintWidth    圆圈绘制的宽度
     * @param radial    圆圈直径大小
     */
    public void updateView(String circleColor, int paintWidth, int radial) {
        paint.reset();
        //设置画笔为抗锯齿
        paint.setAntiAlias(true);
        //设置颜色
        paint.setColor(Color.parseColor(circleColor));
        //画笔样式分
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(paintWidth);
        this.radial = radial;
        invalidate();
    }

}
