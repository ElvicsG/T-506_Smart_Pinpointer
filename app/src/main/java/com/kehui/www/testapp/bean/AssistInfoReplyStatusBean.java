package com.kehui.www.testapp.bean;

import java.util.List;

/**
 * Created by jwj on 2018/5/8.
 */

public class AssistInfoReplyStatusBean {
    /**
     * Code : 1
     * Message : 操作成功
     * data : [{"InfoID":"6baa8ca4-371c-4871-8a9b-33d8f7ecd788","ReplyStatus":"1","ReplyContent":"未检测到故障嘻嘻,请继续监测"}]
     * action : GetInfoReplyStatus
     */

    public String Code;
    public String Message;
    public String action;
    public List<DataBean> data;

    public static class DataBean {
        /**
         * InfoID : 6baa8ca4-371c-4871-8a9b-33d8f7ecd788
         * ReplyStatus : 1
         * ReplyContent : 未检测到故障嘻嘻,请继续监测
         */

        public String InfoID;
        public String ReplyStatus;
        public String ReplyContent;
    }
}
