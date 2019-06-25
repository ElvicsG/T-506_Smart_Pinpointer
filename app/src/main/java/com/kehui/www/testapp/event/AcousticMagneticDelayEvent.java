package com.kehui.www.testapp.event;

/**
 * Created by jwj on 2018/6/14.
 */

public class AcousticMagneticDelayEvent {
    public int position;
    public double delayValue;
    public AcousticMagneticDelayEvent(int position, double delayValue) {
        this.position = position;
        this.delayValue = delayValue;
    }

}
