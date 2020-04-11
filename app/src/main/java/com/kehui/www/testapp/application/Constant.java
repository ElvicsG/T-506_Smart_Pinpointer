package com.kehui.www.testapp.application;

/**
 * @author jwj
 * @date 2018/4/11
 */
public class Constant {

    public static int DEFAULT_TIMEOUT = 20;

    /**
     * 是否截取上传数据的标志
     */
    public static boolean isStartInterception;
    public static StringBuffer sbData = new StringBuffer();
    public static int PageSize = 20;
    public static int voiceGain = 21;
    public static int magneticFieldGain = 21;

    /**
     *  滤波模式    * 全通 0 * 低通 1 * 带通 2 * 高通 3
     */
    public static int filterType = 0;
    public static String DeviceId = "";

    /**
     * 硬件断开后，当前显示的声音、磁场、滤波模式参数  //GC20190407 蓝牙重连功能优化
     */
    public static byte[] CurrentVoiceParam;
    public static byte[] CurrentMagParam;
    public static byte[] CurrentFilterParam;

    public static Boolean BluetoothState;

}
