package com.kehui.www.testapp.event;

/**
 * Created by 34238 on 2018/11/16 0016.
 */

public class AcousticMagneticDelay2 {
    public double delayValue;
    public boolean isRelated;
    public int position;    //GC20190218 虚光标位置传递
    public AcousticMagneticDelay2(double delayValue,boolean isRelated,int position) {
        this.delayValue = delayValue;
        this.isRelated = isRelated;
        this.position = position;
    }

}
