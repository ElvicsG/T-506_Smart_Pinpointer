package com.kehui.www.testapp.event;

/**
 * @author jwj
 * @date 2018/07/20
 */
public class RestartGetStreamEvent {

    /**
     * 硬件重连时通知主线程开启读取蓝牙数据
     */
    public String device;
    public RestartGetStreamEvent(String device) {
        this.device = device;
    }

}
