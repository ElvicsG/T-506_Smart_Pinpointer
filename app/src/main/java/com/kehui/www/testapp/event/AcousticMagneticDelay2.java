package com.kehui.www.testapp.event;

/**
 * @author Gong
 * @date 2018/11/16
 */
public class AcousticMagneticDelay2 {
    public double delayValue;
    public boolean isRelated;
    /**
     * 虚光标位置传递  //GC20190218
     */
    public int position;
    public AcousticMagneticDelay2(double delayValue,boolean isRelated,int position) {
        this.delayValue = delayValue;
        this.isRelated = isRelated;
        this.position = position;
    }

}
