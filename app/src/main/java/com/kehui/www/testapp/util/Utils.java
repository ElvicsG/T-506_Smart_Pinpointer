package com.kehui.www.testapp.util;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jwj
 * @date 2018/04/10
 */
public class Utils {

    /**
     * @param b 字节数组
     * @return  字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        String hex;
        for (int i = 0; i < b.length; i++) {
            hex = Integer.toHexString(b[i] & 0xff);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            result.append(hex.toUpperCase()).append(" ");
        }
        return result.toString();
    }

    /**
     * //G?
     * @param str   需要保存的字符串
     */
    public static void saveFile(String str) {
        String filePath = null;
        boolean hasSdCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSdCard) {
            // SD卡根目录的hello.text
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "hello.txt";
        } else {
            // 系统下载缓存根目录的hello.text
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + "hello.txt";
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
            Log.e("打印-保存文件", "保存成功");
        } catch (Exception e) {
            Log.e("打印-保存文件", "保存失败");
            e.printStackTrace();
        }

    }

    public static void showToast(final Context activity, final String word) {
        final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static boolean isNetVisible(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                // 当前所连接的网络可用
                return info.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
//        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        TelephonyManager mTelephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//// 检查网络连接，如果无网络可用，就不需要进行连网操作等
//        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
//        if (info == null ||
//                !mConnectivity.getBackgroundDataSetting()) {
//            return false;
//        }
////判断网络连接类型，只有在3G或wifi里进行一些数据更新。
//        int netType = info.getType();
//        int netSubtype = info.getSubtype();
//        if (netType == ConnectivityManager.TYPE_WIFI) {
//            return info.isConnected();
//        } else if (netType == ConnectivityManager.TYPE_MOBILE
//                && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
//                && !mTelephony.isNetworkRoaming()) {
//            return info.isConnected();
//        } else {
//            return false;
//        }
    }

    /**
     * @param is 流对象
     * @return 流转换成字符串 返回null代表异常
     */
    public static String streamToString(InputStream is) {
        // 1、在读取的过程中，将读取的内容存储到缓存中，然后一次性的转换成字符串返回
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // 2、 读流操作，读到没有为止（循环）
        byte[] buffer = new byte[1024];
        // 3、记录读取内容的临界值(临时变量)
        int temp = -1;
        try {
            while ((temp = is.read(buffer)) != -1) {
                bos.write(buffer, 0, temp);
            }
            // 返回读取的数据
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                bos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 判断某activity是否处于栈顶
     *
     * @return true在栈顶 false不在栈顶
     */
    public static boolean isActivityTop(Class cls, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String name = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        return name.equals(cls.getName());
    }

    /**
     * @return  当前时间
     */
    public static String formatTimeStamp(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(time);
    }

    /**
     * @param timeString    字符串
     * @return  转时间戳
     */
    public static long getTime(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        long l = 0;
        try {
            d = sdf.parse(timeString);
            l = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return l;
    }

    /**
     * @param context   运行环境、场景
     * @return  获取设备序列号
     */
    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * dp转换成px
     */
    public static int dp2px(Context context,float dpValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    /**
     * px转换成dp
     */
    public static int px2dp(Context context,float pxValue){
        float scale=context.getResources().getDisplayMetrics().density;
        return (int)(pxValue/scale+0.5f);
    }

}
