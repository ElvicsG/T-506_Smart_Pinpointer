package com.kehui.www.testapp.view;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.XXXX.dao.db.AssistanceDataInfoDao;
import com.google.gson.Gson;
import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.application.URLs;
import com.kehui.www.testapp.bean.AssistListBean;
import com.kehui.www.testapp.bean.RequestBean;
import com.kehui.www.testapp.database.AssistanceDataInfo;
import com.kehui.www.testapp.retrofit.APIService;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.util.DES3Utils;
import com.kehui.www.testapp.util.PrefUtils;
import com.kehui.www.testapp.util.Utils;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 发起协助页面
 */
public class InitiateAssistanceActivity extends BaseActivity {

    @BindView(R.id.tv_test_time_value)
    TextView tvTestTimeValue;
    @BindView(R.id.et_test_name)
    EditText etTestName;
    @BindView(R.id.et_test_position)
    EditText etTestPosition;
    @BindView(R.id.et_cable_length)
    EditText etCableLength;
    @BindView(R.id.et_cable_type)
    EditText etCableType;
    @BindView(R.id.et_fault_type)
    TextView etFaultType;
    @BindView(R.id.et_fault_length)
    EditText etFaultLength;
    @BindView(R.id.et_short_note)
    EditText etShortNote;
    @BindView(R.id.pb_data_collection)
    ProgressBar pbDataCollection;
    @BindView(R.id.tv_data_collection_progress)
    TextView tvDataCollectionProgress;
    @BindView(R.id.btn_commit)
    Button btnCommit;
    @BindView(R.id.btn_back)
    Button btnBack;

    private AssistanceDataInfoDao dao;
    private LocationManager lm;
    private static final String TAG = "打印-位置";
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private static final int PERMISSON_REQUESTCODE = 0;
    private static final int SETTING_REQUESTCODE = 1;
    private static boolean isFinish;


    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;
    private int counter;
    private String dataCollection;
    private String isReport;
    private CustomDialog customDialog;
    private long currentTimeStamp;
    private int resultCode = 0;
    private PopupWindow faultTypeWindow;
    private View faultTypeView;//回复内容弹窗view

    public int screenWidth;
    public int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initiate_assistance);
        ButterKnife.bind(this);
        dao = MyApplication.getInstances().getDaoSession().getAssistanceDataInfoDao();
        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
        initData();
        initView();
        initFaultTypePopupWindow();

        Constant.sbData = new StringBuffer();
        Constant.isStartInterception = true;
        isFinish = false;
        //截取35s数据
        startInterceptionThread.start();

    }

    private void initView() {
        tvTestTimeValue.setText(Utils.formatTimeStamp(currentTimeStamp));

        etFaultType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFaultTypeDialog();
            }
        });
    }

    private void initData() {
        currentTimeStamp = System.currentTimeMillis();
//        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        // 判断GPS是否正常启动
//        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
//            // 返回开启GPS导航设置界面
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            startActivityForResult(intent, 0);
//            return;
//        }
//
//        // 为获取地理位置信息时设置查询条件
//        String bestProvider = lm.getBestProvider(getCriteria(), true);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//        Location location = lm.getLastKnownLocation(bestProvider);
//        updateView(location);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if (location != null) {
            Log.e(TAG, "设备位置信息\n\n经度：");
            Log.e(TAG, String.valueOf(location.getLongitude()));
            Log.e(TAG, "\n纬度：");
            Log.e(TAG, String.valueOf(location.getLatitude()));
        } else {
            // 清空EditText对象
            Log.e(TAG, "清空");
        }
    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    @OnClick({R.id.btn_commit, R.id.btn_back, R.id.tv_test_time_value})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_commit:
                btnCommit.setEnabled(false);
                insertData();
                break;
            case R.id.btn_back:
                showBackDialog();
                break;
            case R.id.tv_test_time_value:
                showDatePickerDialog(InitiateAssistanceActivity.this, 2, tvTestTimeValue, Calendar.getInstance());
                break;
        }
    }

    Thread startInterceptionThread = new Thread(new Runnable() {
        @Override
        public void run() {
            int i = 0;
            Log.e("打印-线程", "线程开始");
            for (i = 0; i < 35; i++) {
                try {
                    //设置进度值
                    counter = i * 3;
                    //睡眠1000毫秒
                    Thread.sleep(1000);
                    handler.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (i == 34) {
                    handler.sendEmptyMessage(0);
                }
                //GC2.01.006 蓝牙重连功能优化
                if (Constant.BluetoothState == false) {
                    handler.sendEmptyMessage(2);
                    return;
                }

            }


        }
    });

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Constant.isStartInterception = false;
                isFinish = true;
                counter = 100;

                tvDataCollectionProgress.setText(counter + "%");
                pbDataCollection.setProgress(counter);

                Log.e("打印字符串", Constant.sbData.toString());
            } else if (msg.what == 1) {

                if (counter > 100) {
                    counter = 99;
                }
                pbDataCollection.setProgress(counter);
                tvDataCollectionProgress.setText(counter + "%");
            }else if (msg.what == 2) {  //GC2.01.006 蓝牙重连功能优化
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Utils.showToast(InitiateAssistanceActivity.this,getResources().getString(R.string
                        .Link_Lost_Reconnect));
                finish();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 日期选择
     *
     * @param activity
     * @param themeResId
     * @param tv
     * @param calendar
     */
    public static void showDatePickerDialog(final Activity activity, int themeResId, final TextView tv, Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            // 绑定监听器(How the parent is notified that the date is set.)
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                tv.setText(year + activity.getString(R.string.year) + (monthOfYear + 1) + activity.getString(R.string.month) + dayOfMonth + activity.getString(R.string.day));

            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void insertData() {
        long testTime = currentTimeStamp;
        String testName = etTestName.getText().toString().trim();
        String testPosition = etTestPosition.getText().toString().trim();
        String cableLength = etCableLength.getText().toString().trim();
        String cableType = etCableType.getText().toString().trim();
        String faultType = etFaultType.getText().toString().trim();
        String faultLength = etFaultLength.getText().toString().trim();
        String shortNote = etShortNote.getText().toString().trim();
        String infoId = java.util.UUID.randomUUID().toString();
        String language = PrefUtils.getString(InitiateAssistanceActivity.this, AppConfig.CURRENT_LANGUAGE, "ch");
        if (TextUtils.isEmpty(testName)) {
            Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.test_name_is_empty));
            btnCommit.setEnabled(true);
            return;
        } else if (!isFinish) {
            Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.test_data_time_no_finish));
            btnCommit.setEnabled(true);
            return;
        }
//        else if (TextUtils.isEmpty(testPosition)) {
//            Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.test_position_is_empty));
//            return;
//        }
        RequestBean requestBean = new RequestBean();
        requestBean.InfoDevID = Constant.DeviceId;
        requestBean.InfoID = infoId;
        requestBean.InfoTime = Utils.formatTimeStamp(currentTimeStamp);
        requestBean.InfoUName = testName;
        requestBean.InfoAddress = testPosition;
        requestBean.InfoLength = cableLength;
        requestBean.InfoLineType = cableType;
        requestBean.InfoFaultType = faultType;
        requestBean.InfoFaultLength = faultLength;
        requestBean.InfoCiChang = Constant.sbData.toString();
        requestBean.InfoCiCangVol = Constant.magneticFieldGain + "";//磁场增益
        requestBean.InfoShengYinVol = Constant.voiceGain + "";//声音增益
        requestBean.InfoLvBo = Constant.filterType + "";//滤波模式
        requestBean.InfoYuYan = language;//语言类型
        requestBean.InfoMemo = shortNote;
        //0是未回复

        //0是未上报1是已上报
        if (!Utils.isNetVisible(InitiateAssistanceActivity.this)) {//无网
            if (queryData() < 20) {
                isReport = "0";
                resultCode = 1;
                AssistanceDataInfo info = new AssistanceDataInfo(null, infoId, testTime, testName, testPosition, cableLength, cableType, faultType, faultLength
                        , shortNote, Constant.sbData.toString(), isReport, "0", "", Constant.magneticFieldGain, Constant.voiceGain, Constant.filterType, language);
                dao.insert(info);
                setResult(resultCode);
                finish();
            } else {
                Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.over_20_notice));
            }
            Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.no_net_wait_uploader));
        } else {//有网
            isReport = "1";
            AssistanceDataInfo info = new AssistanceDataInfo(null, infoId, testTime, testName, testPosition, cableLength, cableType, faultType, faultLength
                    , shortNote, Constant.sbData.toString(), isReport, "0", "", Constant.magneticFieldGain, Constant.voiceGain, Constant.filterType, language);
            dao.insert(info);
            resultCode = 1;
            Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.uploading_waiting));
            uploadInfo(requestBean);
        }

    }

    private void uploadInfo(RequestBean requestBean) {
        final Gson gson = new Gson();
        String json = gson.toJson(requestBean);
        json = DES3Utils.encryptMode(MyApplication.keyBytes, json.getBytes());
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URLs.AppUrl + URLs.AppPort)
                .addConverterFactory(ScalarsConverterFactory.create()).client(client)
                .build();
        APIService service = retrofit.create(APIService.class);
        Call<String> call = service.api("UploadInfo", json);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    byte[] srcBytes = DES3Utils.decryptMode(MyApplication.keyBytes, response.body());
                    String result = new String(srcBytes);
                    AssistListBean responseBean = gson.fromJson(result, AssistListBean.class);
                    if (responseBean.Code.equals("1")) {
                        showSuccessDialog();
                        btnCommit.setEnabled(true);
                    } else {
                        Utils.showToast(InitiateAssistanceActivity.this, responseBean.Message);
                    }

                } catch (Exception e) {
                    Log.e("打印-请求报异常-检查代码", "AppInfoList");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Utils.showToast(InitiateAssistanceActivity.this, getString(R.string.check_net_retry));
            }
        });
    }

    /**
     * 弹出对话框，提示用户更新
     */
    protected void showSuccessDialog() {
        customDialog = new CustomDialog(InitiateAssistanceActivity.this);
        customDialog.setCanceledOnTouchOutside(false);

        customDialog.show();

        customDialog.setHintText(getString(R.string.upload_success));
        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(resultCode);
                customDialog.dismiss();
                finish();
            }
        });
        customDialog.setRightGone();
    }

    /**
     * 弹出对话框，提示用户更新
     */
    protected void showBackDialog() {
        customDialog = new CustomDialog(InitiateAssistanceActivity.this);
        customDialog.show();

        customDialog.setHintText(getString(R.string.confirm_cancel_assist));
        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                finish();
            }
        });
        customDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();

            }
        });
    }

    //查
    private int queryData() {
        return dao.queryBuilder().where(AssistanceDataInfoDao.Properties.ReportStatus.eq("0")).list().size();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showBackDialog();
        }
        return false;
    }

    private void initFaultTypePopupWindow() {
        faultTypeView = LayoutInflater.from(InitiateAssistanceActivity.this).inflate(R.layout.dialog_fault_type_layout, null);
        final TextView tvFaultType1 = (TextView) faultTypeView.findViewById(R.id.tv_fault_type_1);
        final TextView tvFaultType2 = (TextView) faultTypeView.findViewById(R.id.tv_fault_type_2);
        final TextView tvFaultType3 = (TextView) faultTypeView.findViewById(R.id.tv_fault_type_3);
        final TextView tvFaultType4 = (TextView) faultTypeView.findViewById(R.id.tv_fault_type_4);
        tvFaultType1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFaultType.setText(tvFaultType1.getText().toString());
                faultTypeWindow.dismiss();
            }
        });
        tvFaultType2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFaultType.setText(tvFaultType2.getText().toString());
                faultTypeWindow.dismiss();
            }
        });
        tvFaultType3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFaultType.setText(tvFaultType3.getText().toString());
                faultTypeWindow.dismiss();
            }
        });
        tvFaultType4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etFaultType.setText(tvFaultType4.getText().toString());
                faultTypeWindow.dismiss();
            }
        });
        faultTypeWindow = new PopupWindow(faultTypeView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        // 设置以下代码，即背景颜色还有外部点击事件的处理才可以点击外部消失,
        faultTypeWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        faultTypeWindow.setOutsideTouchable(true);
        faultTypeWindow.setWidth(screenWidth / 4 - Utils.dp2px(InitiateAssistanceActivity.this, 15));
    }

    private void showFaultTypeDialog() {
        faultTypeWindow.showAsDropDown(etFaultType);

    }

}
