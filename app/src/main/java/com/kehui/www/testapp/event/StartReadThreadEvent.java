package com.kehui.www.testapp.event;

/**
 * @author jwj
 * @date 2018/07/20
 */
public class StartReadThreadEvent {
    /**
     * 通知主线程开启读取数据线程
     */
    public String device;
    public StartReadThreadEvent(String device) {
        this.device = device;
    }

}
