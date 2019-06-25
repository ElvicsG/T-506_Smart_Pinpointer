package com.kehui.www.testapp.event;

/**
 * Created by jwj on 2018/6/9.
 */

public class OperationGuideEvent {
    public boolean isMalfunction;//是故障点
    public OperationGuideEvent(boolean isMalfunction) {
        this.isMalfunction = isMalfunction;
    }

}
