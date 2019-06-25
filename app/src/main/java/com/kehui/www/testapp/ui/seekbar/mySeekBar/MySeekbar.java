package com.kehui.www.testapp.ui.seekbar.mySeekBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.ui.seekbar.widgets.CrystalSeekbar;

/**
 * Created by 29062 on 2016/12/29.
 */


public class MySeekbar extends CrystalSeekbar {


    public MySeekbar(Context context) {
        super(context);
    }

    public MySeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public float getCornerRadius(TypedArray typedArray) {
        return super.getCornerRadius(typedArray);
    }

    @Override
    public float getMinValue(TypedArray typedArray) {
        return 0f;
    }

    @Override
    public float getMaxValue(TypedArray typedArray) {
        return 32f;
    }

    @Override
    public float getMinStartValue(TypedArray typedArray) {
        return 22f;
    }

    @Override
    public float getSteps(TypedArray typedArray) {
        return super.getSteps(typedArray);
    }

    @Override
    public int getBarColor(TypedArray typedArray) {
        return Color.parseColor("#A0E3F7");
    }

    @Override
    public int getBarHighlightColor(TypedArray typedArray) {
        return Color.BLACK;
    }

    @Override
    public int getLeftThumbColor(TypedArray typedArray) {
        return Color.parseColor("#058EB7");
    }

    @Override
    public int getLeftThumbColorPressed(TypedArray typedArray) {
        return Color.parseColor("#046887");
    }

    @Override
    public Drawable getLeftDrawable(TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.CrystalRangeSeekbar_left_thumb_image);
    }

    @Override
    public Drawable getLeftDrawablePressed(TypedArray typedArray) {
        return typedArray.getDrawable(R.styleable.CrystalRangeSeekbar_left_thumb_image_pressed);
    }

    @Override
    public int getDataType(TypedArray typedArray) {
        return super.getDataType(typedArray);
    }

}

