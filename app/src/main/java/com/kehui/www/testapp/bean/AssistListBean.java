package com.kehui.www.testapp.bean;

import java.util.List;

/**
 * Created by jwj on 2018/4/16.
 */

public class AssistListBean {

    public String Code;
    public String Message;
    public String action;
    public List<DataBean> data;

    public static class DataBean {

        public String InfoID;
        public String InfoTime;
        public String InfoUName;
        public String InfoAddress;
        public String InfoLength;
        public String InfoLineType;
        public String InfoFaultType;
        public String InfoFaultLength;
        public String InfoMemo;//备注
        public String InfoDevID;
        public String ReplyContent;
        public String ReplyStatus;
        public String ReplyTime;
        public String ReplyUser;
        public String InfoCiChang;//35s数据
        public String InfoCiCangVol; //磁场增益
        public String InfoShengYin;
        public String InfoShengYinVol;//声音增益
        public String InfoLvBo;//滤波
        public String InfoYuYan;//语言
        public String InTime;//上传时间
    }
}
