package com.kehui.www.testapp.event;

/**
 * @author jwj
 * @date 2018/06/09
 */
public class OperationGuideEvent {
    /**
     * 判断是故障点
     */
    public boolean isFault;
    public OperationGuideEvent(boolean isFault) {
        this.isFault = isFault;
    }

}
