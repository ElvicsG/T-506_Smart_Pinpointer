package com.kehui.www.testapp.view;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.util.PrefUtils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.URLs;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.ui.HorizontalProgressDialog;
import com.kehui.www.testapp.util.ShowProgressDialog;
import com.kehui.www.testapp.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity {
    @BindView(R.id.tv_app_name)
    TextView tvAppName;

    public static SplashActivity instance;
    private CustomDialog customDialog;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //根据上次的语言设置，重新设置语言
//        MyApplication.getInstances().switchLanguage(PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "ch"));
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        instance = this;
        //检测版本更新
        initUpdateApk();
        TextView tvVersion = (TextView) this.findViewById(R.id.tv_version2);
        tvVersion.setText(this.getResources().getString(R.string.version_code) + getVerCode(this));
        //设置字体
        Typeface type = Typeface.createFromAsset(tvAppName.getContext().getAssets(), "founderBlack.ttf");
        tvAppName.setTypeface(type);
        Constant.DeviceId = Utils.getAndroidId(SplashActivity.this);
        Log.e("设备序列号", Constant.DeviceId);//8ffd20e5d086759d  // cf390c486799c36e
        //GC20181116 模式切换时无需点击操作
        if (PrefUtils.getString(SplashActivity.this, AppConfig.CLICK_MODE, "click_mode").equals("clicked")) {
            Utils.showToast(SplashActivity.this, getString(R.string.connecting_waite));
            Intent intent = new Intent(this, SeekDeviceActivity.class);
            startActivity(intent);
            finish();
        }

    }

    //显示等待的弹窗
    public void showProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = ShowProgressDialog.createLoadingDialog(SplashActivity.this);
        dialog.show();
    }

    public void clickSplash(View view) {
        Utils.showToast(SplashActivity.this, getString(R.string.connecting_waite));
        Intent intent = new Intent(this, SeekDeviceActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
        }
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    public String getVerCode(Context context) {
        String verCode = "";
        try {
            //注意："com.example.try_downloadfile_progress"对应AndroidManifest.xml里的package="……"部分
            verCode = context.getPackageManager().getPackageInfo(
                    "com.kehui.www.testapp", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("msg", e.getMessage());
        }
        return verCode;
    }

    private int mLocalVersionCode;//本地的版本号
    private boolean isCheckOver = true;

    /**
     * 获取数据方法
     */
    public void initUpdateApk() {
        // 1，应用版本名称
//        tv_version_name.setText("版本名称:" + getVersionName());
        // 2、检测（本地版本）是否有更新，，如果有更新，提示用户下载(member)
        mLocalVersionCode = getVersionCode();
        // 3、获取服务器版本号（客户端发请求，服务端给响应，（json,xml））
        // http://www.oxxx.com/update74.json?key=value 返回200，请求成功，流的方式将数据都取下来
        // json中内容包含：
        // 新版本应用描述
        // 新版本版本号(本地版本比大小)
        // 新版本版本名称
        // 新版本apk下载地址
        if (isCheckOver == true) {
            checkVersion();
        }
        //有状态开关时的操作
  /*      if (SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE, false)) {
            checkVersion();
        } else {
            // 直接进入应用程序主界面
            // mHandler.sendMessageDelayed(msg, 4000);
            // 在发送消息4秒后去处理。ENTER_HOME状态码指向的消息
            mHandler.sendEmptyMessageDelayed(ENTER_HOME, 2000);
        }*/
    }

    /**
     * 返回版本号
     *
     * @return 非0代表获取成功
     */
    private int getVersionCode() {
        // 1，包管理者对象packageManager
        PackageManager pm = SplashActivity.this.getPackageManager();
        // 2,从包的管理者对象中，获取指定的包名的基本信息（版本名称，版本号），传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(SplashActivity.this.getPackageName(), 0);
            // 3,获取版本名称
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String mVersionDos;
    public static String mDownloadUrl;
    public boolean isToast = false;

    /**
     * 检测版本号
     */
    private void checkVersion() {
        isCheckOver = false;
        if (isToast) {
            Utils.showToast(SplashActivity.this, getString(R.string.checking_update));
        }
        new Thread() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();//当前时间戳
                // 发送请求获取数据，参数则为请求的json的链接地址
                // http://localhost:8080/update74.json仅限于模拟器访问tomcat
                try {
                    // 1、封装url地址
                    URL url = new URL(URLs.AppUrl + URLs.AppPort + "/updatePad.json");

                    // 2、开启一个链接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // 3、设置常见请求参数(请求头)
                    connection.setRequestMethod("GET");// 请求方法
                    // 请求超时
                    connection.setConnectTimeout(60000);

                    // 读取超时
                    connection.setReadTimeout(60000);

                    // 4、获取请求响应码
                    if (connection.getResponseCode() == 200) {
                        // 5、以流的形式，将数据获取下来d
                        InputStream is = connection.getInputStream();
                        // 6、将流转换成字符串,封装类StreamUtil
                        String json = Utils.streamToString(is);
                        // 7、json解析
                        JSONObject jsonObject = new JSONObject(json);

                        String versionName = jsonObject.getString("versionName");
                        mVersionDos = jsonObject.getString("versionDos");
                        String versionCode = jsonObject.getString("versionCode");
                        mDownloadUrl = jsonObject.getString("downloadUrl");
                        // 8、比对版本号（服务器版本号>本地版本号，提示用户更新）
                        if (mLocalVersionCode < Integer.parseInt(versionCode)) {
                            // 提示用户更新，弹出对话框（UI），消息机制
                            msg.what = UPDATE_VERSION;
                        } else {
                            // 进入应用程序界面
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                } finally {
                    // 指定睡眠时间，请求网络的时常超过四秒不做处理
                    // 请求网络时长 小于3秒，强制让其睡眠满四秒
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime < 2000) {
                        try {
                            Thread.sleep(2000 - (endTime - startTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    /**
     * 更新新版本的状态码
     */
    protected static final int UPDATE_VERSION = 100;
    /**
     * 进入应用程序主界面的状态码
     */
    protected static final int ENTER_HOME = 101;
    /**
     * URL出错的状态码
     */
    protected static final int URL_ERROR = 102;
    protected static final int IO_ERROR = 103;
    protected static final int JSON_ERROR = 104;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    // 弹出对话框，提示用户更新

                    if (SettingActivity.instance != null) {
                        SettingActivity.instance.showUpdateDialog();
                    } else {

                        if (!isFinishing())
                            showUpdateDialog();
                    }

                    isCheckOver = true;
                    break;
                case ENTER_HOME:
                    if (isToast) {
                        Utils.showToast(SplashActivity.this, getString(R.string.latest_version));
                    }
                    isCheckOver = true;
                    break;
                case URL_ERROR:
                    Log.e("update", "URL异常");
                    isCheckOver = true;
                    break;
                case IO_ERROR:
                    Log.e("update", "IO读取异常");
                    isCheckOver = true;
                    break;
                case JSON_ERROR:
                    Log.e("update", "JSON解析异常");
                    isCheckOver = true;
                    break;
                default:
                    break;
            }
        }
    };

    public static boolean isCancel = true; // 用来判断是否点击了取消


    /**
     * 弹出对话框，提示用户更新
     */
    protected void showUpdateDialog() {
        customDialog = new CustomDialog(SplashActivity.this);
        customDialog.show();

        customDialog.setHintText(getString(R.string.version_update));
        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancel = true;
                // 下载apk，，apk链接地址，downloadUrl
                DownloadApk(SplashActivity.this);
                customDialog.dismiss();
            }
        });
        customDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
    }

    public static HorizontalProgressDialog mpdialog;

    public static void DownloadApk(final Context context) {
        // apk下载链接地址，放置apk的所在路径
        // 1，判断sd卡是否可用，是否挂载上
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 2,获取sd卡路径，File.separator相当于”/“
            String path = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + "kehuiPadApp.apk";
            // 3,发送请求，获取apk，并放置在指定目录
            HttpUtils httpUtils = new HttpUtils();
            // 4，发送请求，传递参数（下载地址，下载应用放置位置）
            httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    // 下载成功
                    Utils.showToast(context, context.getString(R.string.download_success));
                    mpdialog.cancel();
                    File file = responseInfo.result;
                    if (isCancel == true) {
                        // 提示用户安装
                        installApk(file, context);
                    }
                }

                @Override
                public void onFailure(com.lidroid.xutils.exception.HttpException e, String s) {
                    Utils.showToast(context, context.getString(R.string.download_failure));
                }

                // 刚开始下载的方法
                @Override
                public void onStart() {
                    mpdialog = new HorizontalProgressDialog(context);
                    mpdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); //设置横向进度条风格
//                    mpdialog.setTitle("正在下载请稍后...");                       //设置标题
//                    mpdialog.setIcon(R.drawable.logo_iv);             //设置图标

                    mpdialog.setOnOkAndCancelListener(new HorizontalProgressDialog.OnOkAndCancelListener() {
                        @Override
                        public void onCancel(View v) {
                            mpdialog.dismiss();
                            mpdialog.setProgress(0);
                            isCancel = false;
                        }
                    });

                    super.onStart();
                }

                // 下载过程中的方法（下载apk的总大小，当前的下载位置，是否正在下载）
                @Override
                public void onLoading(long total, long current, final boolean isUploading) {
                    super.onLoading(total, current, isUploading);

                    if (isCancel == true) {
                        //保留两位小数
                        DecimalFormat df = new DecimalFormat("#.##");
                        String format = df.format(total * 1.0f / 1024 / 1024);
                        Log.e("download", df.format(total * 1.0f / 1024 / 1024) + "      aa");
                        mpdialog.setMessage(context.getString(R.string.this_update) + format + context.getString(R.string.wait_downloading));               //设置内容
                        mpdialog.setMax((int) total);
                        mpdialog.setProgress((int) current);
                        mpdialog.setIndeterminate(false);              //设置进度条是否可以不明确
                        mpdialog.setCancelable(false);                  //设置进度条是否可以取消
                        mpdialog.show();
                    }
                }
            });

        } else {
            Utils.showToast(context, context.getString(R.string.download_failure));
        }
    }

    /**
     * 安装对应apk
     *
     * @param file 提示用户安装
     */
    public static void installApk(File file, Context context) {
        // 系统应用界面，源码，安装apk入口
        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //android4.0以后需要添加这行代码
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory("android.intent.category.DEFAULT");
        // 文件作为数据源
        // intent.setData(Uri.fromFile(file));
        // 设置安装的类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);


//        startActivityForResult(intent, 0);
    }
}
