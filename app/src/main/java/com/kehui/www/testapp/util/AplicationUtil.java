package com.kehui.www.testapp.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 应用工具类
 * 不好意思application拼错了
 * Created by 王延超 on 2016/10/25.
 */

public class AplicationUtil {

//   public static BluetoothSocket _socket;  //GC20180000 蓝牙连接建立过程

    //做一个吐司
    public static void makeToast(Context context, String str) {
        Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }

}
