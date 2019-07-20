package com.kehui.www.testapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.kehui.www.testapp.R;

/**
 * @author yangle
 * @date 2016/11/29
 */
public class TempControlView extends View {

    // 控件宽
    private int width;
    // 控件高
    private int height;
    // 刻度盘半径
    private int dialRadius;
    // 圆弧半径
    private int arcRadius;
    // 刻度高
    private int scaleHeight = dp2px(5);
    // 刻度盘画笔
    private Paint dialPaint;
    // 圆弧画笔
    private Paint arcPaint;
    // 标题画笔
    private Paint titlePaint;
    //增益标识画笔
    private Paint tempFlagPaint;
    // 旋转按钮画笔
    private Paint buttonPaint;
    // 增益显示画笔
    private Paint tempPaint;
    // 文本提示
    private String title = "";
    // 增益
    private int temperature = 21;
    // 最低增益
    private int minTemp = 0;
    // 最高增益
    private int maxTemp = 100;
    // 1格代表增益1度
    private float angleRate = 0.2f;
    // 每格的角度
    private float angleOne = (float) 270 / (100) / angleRate;
    // 按钮图片
    private Bitmap buttonImage = BitmapFactory.decodeResource(getResources(),
            R.drawable.btn_rotate_3);
    // 按钮图片阴影
//    private Bitmap buttonImageShadow = BitmapFactory.decodeResource(getResources(),
//            R.mipmap.btn_rotate_shadow);
    // 抗锯齿
    private PaintFlagsDrawFilter paintFlagsDrawFilter;
    // 增益改变监听
    private OnTempChangeListener onTempChangeListener;
    // 控件点击监听
    private OnClickListener onClickListener;

    // 以下为旋转按钮相关

    // 当前按钮旋转的角度
    private float rotateAngle;
    // 当前的角度
    private float currentAngle;

    public TempControlView(Context context) {
        this(context, null);
    }

    public TempControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TempControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dialPaint = new Paint();
        dialPaint.setAntiAlias(true);
        dialPaint.setStrokeWidth(dp2px(1));
        dialPaint.setStyle(Paint.Style.STROKE);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.parseColor(arcColor));//#a03225
        arcPaint.setStrokeWidth(dp2px(1));
        arcPaint.setStyle(Paint.Style.STROKE);

        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setTextSize(sp2px(10));
        titlePaint.setColor(Color.parseColor("#FFFFFF"));
        titlePaint.setStyle(Paint.Style.STROKE);

        tempFlagPaint = new Paint();
        tempFlagPaint.setAntiAlias(true);
        tempFlagPaint.setTextSize(sp2px(10));
        tempFlagPaint.setColor(Color.parseColor(valueColor));//d0210e
        tempFlagPaint.setStyle(Paint.Style.STROKE);

        buttonPaint = new Paint();
        tempFlagPaint.setAntiAlias(true);
        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        tempPaint = new Paint();
        tempPaint.setAntiAlias(true);
        tempPaint.setTextSize(sp2px(10));
        tempPaint.setColor(Color.parseColor(currentValueColor));//#a03225
        tempPaint.setStyle(Paint.Style.STROKE);
    }


    private String arcColor = "#000000";

    //弧度颜色
    public void setArcColor(String color) {
        this.arcColor = color;
        arcPaint.reset();
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.parseColor(arcColor));//#a03225
        arcPaint.setStrokeWidth(dp2px(1));
        arcPaint.setStyle(Paint.Style.STROKE);

        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 控件宽、高
        width = height = Math.min(h, w);
        // 刻度盘半径
        dialRadius = width / 2 - dp2px(20);
        // 圆弧半径
        arcRadius = dialRadius - dp2px(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawScale(canvas);
        drawArc(canvas);
        drawText(canvas);
        drawButton(canvas);
        drawTemp(canvas);
    }

    /**
     * 绘制刻度盘
     *
     * @param canvas 画布
     */
    private void drawScale(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        // 逆时针旋转135-2度
        canvas.rotate(-135);
        dialPaint.setColor(Color.parseColor(dialColor1));//#a03225
        for (int i = 0; i < (21); i++) {
            canvas.drawLine(0, -dialRadius, 0, -dialRadius + scaleHeight, dialPaint);
            canvas.rotate(angleOne);
        }

        canvas.rotate(90);
        dialPaint.setColor(Color.parseColor(dialColor2));//#01eeff
        for (int i = 0; i < (temperature - minTemp) * angleRate; i++) {
            canvas.drawLine(0, -dialRadius, 0, -dialRadius + scaleHeight, dialPaint);
            canvas.rotate(angleOne);
        }
        canvas.restore();
    }

    /**
     * 绘制旋转按钮
     *
     * @param canvas 画布
     */
    private void drawButton(Canvas canvas) {
        // 按钮宽高
        int buttonWidth = buttonImage.getWidth();
        int buttonHeight = buttonImage.getHeight();

        Matrix matrix = new Matrix();
        // 设置按钮位置，移动到控件中心
        matrix.setTranslate((width - buttonWidth) / 2, (height - buttonHeight) / 2);
        // 设置旋转角度，旋转中心为控件中心，当前也是按钮中心
        matrix.postRotate(45 + rotateAngle, width / 2, height / 2);

        //设置抗锯齿
        canvas.setDrawFilter(paintFlagsDrawFilter);
        canvas.drawBitmap(buttonImage, matrix, buttonPaint);
    }

    private String dialColor1 = "#000000";
    private String dialColor2 = "#000000";
    private String valueColor = "#000000";

    //未选中刻度颜色
    public void setDialColor1(String color) {
        this.dialColor1 = color;
    }

    //选中刻度颜色
    public void setDialColor2(String color) {
        this.dialColor2 = color;
    }

    //最大最小值颜色
    public void setValueColor(String color) {
        this.valueColor = color;
        tempFlagPaint.reset();
        tempFlagPaint = new Paint();
        tempFlagPaint.setAntiAlias(true);
        tempFlagPaint.setTextSize(sp2px(10));
        tempFlagPaint.setColor(Color.parseColor(valueColor));//d0210e
        tempFlagPaint.setStyle(Paint.Style.STROKE);

    }

    /**
     * 绘制刻度盘下的圆弧
     *
     * @param canvas 画布
     */
    private void drawArc(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(135);
        RectF rectF = new RectF(-arcRadius, -arcRadius, arcRadius, arcRadius);
        canvas.drawArc(rectF, 0, 270, false, arcPaint);
        canvas.restore();
    }

    /**
     * 绘制标题与增益标识
     *
     * @param canvas 画布
     */
    private void drawText(Canvas canvas) {
        canvas.save();

        // 绘制标题
        float titleWidth = titlePaint.measureText(title);
        canvas.drawText(title, (width - titleWidth) / 2, dialRadius * 2 + dp2px(20), titlePaint);

        // 绘制最小增益标识
        // 最小增益如果小于10，显示为0x
        String minTempFlag = minTemp < 10 ? minTemp + "" : minTemp + "";
        float tempFlagWidth = titlePaint.measureText(maxTemp + "");
        canvas.rotate(55, width / 2, height / 2);
        canvas.drawText(minTempFlag, (width - tempFlagWidth) / 2 + dp2px(10), height - dp2px(5), tempFlagPaint);

        // 绘制最大增益标识
        canvas.rotate(-105, width / 2, height / 2);
        canvas.drawText(maxTemp + "", (width - tempFlagWidth) / 2, height - dp2px(5), tempFlagPaint);
        canvas.restore();
    }

    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * 绘制增益
     *
     * @param canvas 画布
     */
    private void drawTemp(Canvas canvas) {
        canvas.save();
        canvas.translate(getWidth() / 2, getHeight() / 2);

        float tempWidth = tempPaint.measureText(temperature + "");
        float tempHeight = (tempPaint.ascent() + tempPaint.descent()) / 2;
        canvas.drawText(temperature + "", -tempWidth / 2, -tempHeight, tempPaint);
        canvas.restore();
    }

    public String currentValueColor = "#000000";

    //当前设置值颜色
    public void setCurrentValueColor(String color) {
        this.currentValueColor = color;
        tempPaint.reset();
        tempPaint = new Paint();
        tempPaint.setAntiAlias(true);
        tempPaint.setTextSize(sp2px(18));
        tempPaint.setColor(Color.parseColor(currentValueColor));//#a03225
        tempPaint.setStyle(Paint.Style.STROKE);
    }

    private boolean isDown;
    private boolean isMove;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDown = true;
                float downX = event.getX();
                float downY = event.getY();
                currentAngle = calcAngle(downX, downY);
                break;

            case MotionEvent.ACTION_MOVE:
                isMove = true;
                float targetX;
                float targetY;
                downX = targetX = event.getX();
                downY = targetY = event.getY();
                float angle = calcAngle(targetX, targetY);

                // 滑过的角度增量
                float angleIncreased = angle - currentAngle;

                // 防止越界
                if (angleIncreased < -270) {
                    angleIncreased = angleIncreased + 360;
                } else if (angleIncreased > 270) {
                    angleIncreased = angleIncreased - 360;
                }

                IncreaseAngle(angleIncreased);
                currentAngle = angle;
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (isDown) {
                    if (isMove) {
                        // 纠正指针位置
                        rotateAngle = (float) ((temperature - minTemp) * angleRate * angleOne);
                        invalidate();
                        // 回调增益改变监听
                        if (onTempChangeListener != null) {
                            onTempChangeListener.change(temperature);
                        }
                        isMove = false;
                    } else {
                        // 点击事件
                        if (onClickListener != null) {
                            onClickListener.onClick(temperature);
                        }
                    }
                    isDown = false;
                }
                break;
            }
        }
        return true;
    }

    /**
     * 以按钮圆心为坐标圆点，建立坐标系，求出(targetX, targetY)坐标与x轴的夹角
     *
     * @param targetX x坐标
     * @param targetY y坐标
     * @return (targetX, targetY)坐标与x轴的夹角
     */
    private float calcAngle(float targetX, float targetY) {
        float x = targetX - width / 2;
        float y = targetY - height / 2;
        double radian;

        if (x != 0) {
            float tan = Math.abs(y / x);
            if (x > 0) {
                if (y >= 0) {
                    radian = Math.atan(tan);
                } else {
                    radian = 2 * Math.PI - Math.atan(tan);
                }
            } else {
                if (y >= 0) {
                    radian = Math.PI - Math.atan(tan);
                } else {
                    radian = Math.PI + Math.atan(tan);
                }
            }
        } else {
            if (y > 0) {
                radian = Math.PI / 2;
            } else {
                radian = -Math.PI / 2;
            }
        }
        return (float) ((radian * 180) / Math.PI);
    }

    /**
     * 增加旋转角度
     *
     * @param angle 增加的角度
     */
    private void IncreaseAngle(float angle) {
        rotateAngle += angle;
        if (rotateAngle < 0) {
            rotateAngle = 0;
        } else if (rotateAngle > 270) {
            rotateAngle = 270;
        }
        // 加上0.5是为了取整时四舍五入
        temperature = (int) ((rotateAngle / angleOne) / angleRate + 0.5) + minTemp;
    }

    /**
     * 设置几格代表1度，默认4格
     *
     * @param angleRate 几格代表1度
     */
    public void setAngleRate(int angleRate) {
        this.angleRate = angleRate;
    }

    /**
     * 设置增益
     *
     * @param temp 设置的增益
     */
    public void setTemp(int temp) {
        setTemp(minTemp, maxTemp, temp);
    }

    /**
     * 设置增益
     *
     * @param minTemp 最小增益
     * @param maxTemp 最大增益
     * @param temp    设置的增益
     */
    public void setTemp(int minTemp, int maxTemp, int temp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        if (temp < minTemp) {
            this.temperature = minTemp;
        } else {
            this.temperature = temp;
        }
        // 计算每格的角度
        angleOne = (float) 270 / (maxTemp - minTemp) / angleRate;
        // 计算旋转角度
        rotateAngle = (float) ((temp - minTemp) * angleRate * angleOne);

        invalidate();
    }

    /**
     * 设置增益改变监听
     *
     * @param onTempChangeListener 监听接口
     */
    public void setOnTempChangeListener(OnTempChangeListener onTempChangeListener) {
        this.onTempChangeListener = onTempChangeListener;
    }

    /**
     * 设置点击监听
     *
     * @param onClickListener 点击回调接口
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 增益改变监听接口
     */
    public interface OnTempChangeListener {
        /**
         * 回调方法
         *
         * @param temp 增益
         */
        void change(int temp);
    }

    /**
     * 点击回调接口
     */
    public interface OnClickListener {
        /**
         * 点击回调方法
         *
         * @param temp 增益
         */
        void onClick(int temp);
    }

    public int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
}
