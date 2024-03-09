package com.kehui.www.testapp.event;

/**
 * @author Gong
 * @date 2018/11/16
 */
public class ResultOfRelevantEvent {

    public double timeDelay;
    public boolean isRelated;
    /**
     * 虚光标位置传递  //GC20190218
     */
    public int cursorPosition;
    public double p;    //GC20231130
    public ResultOfRelevantEvent(double timeDelay, boolean isRelated, int cursorPosition, double p) {
        this.timeDelay = timeDelay;
        this.isRelated = isRelated;
        this.cursorPosition = cursorPosition;
        this.p = p; //GC20231130
    }

}
