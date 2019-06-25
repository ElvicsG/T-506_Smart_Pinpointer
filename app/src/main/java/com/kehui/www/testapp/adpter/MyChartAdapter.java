package com.kehui.www.testapp.adpter;

import com.kehui.www.testapp.ui.SparkView.SparkAdapter;

/*
 * Created by 29062 on 2017/2/17.
 */

public class MyChartAdapter extends SparkAdapter {

    private int[] mTempArray;
    private int[] mCompareArray;

    public void setmCompareArray(int[] mCompareArray) {
        this.mCompareArray = mCompareArray;
    }

    public void setmTempArray(int[] mTempArray) {
        this.mTempArray = mTempArray;
    }


    public void setShowCompareLine(boolean showCompareLine) {
        isShowCompareLine = showCompareLine;
    }


    private boolean isShowCompareLine;
    private int splitNum;
    private boolean isShowSplitLine;

    public MyChartAdapter(int[] mTempArray, int[] mCompareArray, boolean
            isShowCompareLine, int splitNum, boolean isShowSplitLine) {
        this.mTempArray = mTempArray;
        this.mCompareArray = mCompareArray;
        this.isShowCompareLine = isShowCompareLine;
        this.splitNum = splitNum;
        this.isShowSplitLine = isShowSplitLine;
    }

    @Override
    public int getCount() {
        return 400;
    }

    @Override
    public Object getItem(int index) {
        return index;
    }

    @Override
    public float getY(int index) {
        return mTempArray[index];
    }

    @Override
    public float getX(int index) {
        return super.getX(index);
    }


    public boolean isShowCompareLine() {
        return isShowCompareLine;
    }

    @Override
    public float getY1(int index) {
        return mCompareArray[index];
    }

    @Override
    public boolean getCompare() {
        return isShowCompareLine;
    }
}
