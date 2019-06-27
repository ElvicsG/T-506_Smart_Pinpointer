package com.kehui.www.testapp.view;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.adpter.MyChartAdapter;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.base.BaseActivity;
import com.kehui.www.testapp.event.AcousticMagneticDelay2;
import com.kehui.www.testapp.event.AcousticMagneticDelayEvent;
import com.kehui.www.testapp.event.HandleReceiveDataEvent;
import com.kehui.www.testapp.event.OperationGuideEvent;
import com.kehui.www.testapp.event.SendDataFinishEvent;
import com.kehui.www.testapp.event.UINoticeEvent;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.ui.PercentLinearLayout;
import com.kehui.www.testapp.ui.SparkView.SparkView;
import com.kehui.www.testapp.ui.TempControlView;
import com.kehui.www.testapp.ui.WaterWaveView;
import com.kehui.www.testapp.util.PrefUtils;
import com.kehui.www.testapp.util.ShowProgressDialog;
import com.kehui.www.testapp.util.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * 界面无缝切换思路：
 * 将用户界面和专家界面用布局封装，放到一个activity中，
 * 点击模式切换时，控制布局的隐藏和显示。
 * */
/**
 * 专家模式页面
 */
public class MainActivity extends BaseActivity {

    //浮动层   //GC2.01.005 界面无缝切换
    @BindView(R.id.mainUI)
    LinearLayout llMainUI;
    @BindView(R.id.userUI)
    LinearLayout llUserUI;

    //用户界面所有控件
    @BindView(R.id.magnetic_field_gain_control_u)
    TempControlView magneticFieldGainControlU;
    @BindView(R.id.iv_magnetic_field_gain_u)
    ImageView ivMagneticFieldGainU;
    @BindView(R.id.voice_gain_control_u)
    TempControlView voiceGainControlU;
    @BindView(R.id.iv_voice_gain_u)
    ImageView ivVoiceGainU;
    @BindView(R.id.tv_notice_u)
    TextView tvNoticeU;
    @BindView(R.id.iv_position_u)
    ImageView ivPositionU;
    @BindView(R.id.ll_voice_u)
    PercentLinearLayout llVoiceU;
    @BindView(R.id.ll_filter_u)
    PercentLinearLayout llFilterU;
    @BindView(R.id.ll_assist_u)
    PercentLinearLayout llAssistU;
    @BindView(R.id.ll_settings_u)
    PercentLinearLayout llSettingsU;
    @BindView(R.id.iv_mode_u)
    ImageView ivModeU;
    @BindView(R.id.tv_mode_u)
    TextView tvModeU;
    @BindView(R.id.ll_mode_u)
    LinearLayout llModeU;
    @BindView(R.id.iv_silence_u)
    ImageView ivSilenceU;
    @BindView(R.id.rl_wave_u)
    RelativeLayout rlWaveU;
    @BindView(R.id.tv_scan_u)
    TextView tvScanU;
    @BindView(R.id.ccv_first_u)
    CustomCircleView ccvFirstU;
    @BindView(R.id.ccv_second_u)
    CustomCircleView ccvSecondU;
    @BindView(R.id.iv_scan_u)
    ImageView ivScanU;
    @BindView(R.id.tv_last_delay_u)
    TextView tvLastDelayU;
    @BindView(R.id.tv_current_delay_u)
    TextView tvCurrentDelayU;
    @BindView(R.id.tv_min_delay_value_u)
    TextView tvMinDelayValueU;
    @BindView(R.id.ll_min_delay_u)
    LinearLayout llMinDelayU;


    //专家界面控件
    @BindView(R.id.ll_silence)
    LinearLayout llSilence;
    @BindView(R.id.ll_pause)
    LinearLayout llPause;
    @BindView(R.id.ll_memory)
    LinearLayout llMemory;
    @BindView(R.id.ll_compare)
    LinearLayout llCompare;
    @BindView(R.id.ll_assist)
    LinearLayout llAssist;
    @BindView(R.id.ll_settings)
    LinearLayout llSettings;
    @BindView(R.id.iv_mode)
    ImageView ivMode;
    @BindView(R.id.tv_mode)
    TextView tvMode;
    @BindView(R.id.ll_mode)
    LinearLayout llMode;
    @BindView(R.id.ll_right)
    PercentLinearLayout llRight;
    @BindView(R.id.tv_notice)
    TextView tvNotice;
    @BindView(R.id.seekbar_cichang)
    SeekBar seekbarCichang;
    @BindView(R.id.linechart_cichang)
    SparkView linechartCichang;
    @BindView(R.id.seekbar_shengyin)
    SeekBar seekbarShengyin;
    @BindView(R.id.linechart_shengyin)
    SparkView linechartShengyin;
    @BindView(R.id.rl_left)
    LinearLayout rlLeft;
    @BindView(R.id.ll_filter)
    LinearLayout llFilter;
    @BindView(R.id.iv_silence)
    ImageView ivSilence;
    @BindView(R.id.tv_play)
    TextView tvPlay;
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.tv_cichang_value)
    TextView tvCichangValue;
    @BindView(R.id.tv_shengyin_value)
    TextView tvShengyinValue;
    @BindView(R.id.iv_synchronize_status)
    ImageView ivSynchronizeStatus;
    @BindView(R.id.tv_yan_shi)
    TextView tvYanShi;
    @BindView(R.id.tv_position)
    TextView tvPosition;


    //用户界面变量
    //public static UserMainActivity instance;
    private ValueAnimator valueAnimator;    //GN 动画绘制1
    private int[] scoreText = {R.drawable.ic_wait_empty, R.drawable.ic_wait_1, R.drawable.ic_wait_2, R.drawable.ic_wait_3};
    private WaterWaveView v;    //水波纹动画
    private ViewGroup.MarginLayoutParams layoutParams;  //GN 探头位置
    private ValueAnimator valueAnimator2;   //GN 动画绘制2
    //private double lastDelayValue = -1; //上次的声磁延时值
    private double minDelayValue = 43.625;  //GC20181115 历史最小延时值（最大349*0.125=43.625ms）
    //private Dialog dialog;
    public int currentPosition;     //GN 当前增益进度条的位置
    private boolean firstFind = true;   //GC20181119
    //private int positionState = -1;  //GC20181119 故障圈的大小状态
    //private int isRelatedCount = 0; //GC20181119 相关次数计数

    //专家界面变量
    public static MainActivity instance;
    private CustomDialog customDialog;
    private Dialog dialog;
    private double lastDelayValue = -1;         //上次的声磁延时值
    private int lastPosition = 50;  //GC20190218
    private int positionState = -1;  //故障点状态远离还是接近
    private int isRelatedCount = 0; //GC20181119 相关次数计数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持亮屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        //绑定控件
        ButterKnife.bind(this);
//        EventBus.getDefault().register(this);
        instance = this;
        initView();
        setSeekBar();
        getShengyinData();  //初始化声音数据
        getCichangData();   //初始化磁场数据
        setChartListenner();
//        linechartShengyin.setStartPoint(50);
//        showProgressDialog();

    }
    //初始化试图
    private void initView() {
        seekbarCichang.setMax(100);
        seekbarShengyin.setMax(100);
        seekbarCichang.setProgress(70);
        seekbarShengyin.setProgress(70);
        tvCichangValue.setText(70 + "%");
        tvShengyinValue.setText(70 + "%");
        checkVoice();
        //streamVolumenow = 0;

        //用户界面  //GC2.01.005 界面无缝切换
        //GN 动画绘制1（正在测试中）
        ccvFirstU.setVisibility(View.GONE);
        ccvSecondU.setVisibility(View.GONE);
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(0, 4).setDuration(1000);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    ivScanU.setImageResource(scoreText[i % scoreText.length]);
                }
            });
        }
        valueAnimator.start();
        v = new WaterWaveView(MainActivity.this);
        v.setFillWaveSourceShapeRadius(10);
        rlWaveU.addView(v);
        //设置探头位置
        layoutParams = new ViewGroup.MarginLayoutParams(ivPositionU.getLayoutParams());
        //设置增益显示
//        magneticFieldGainControl.setAngleRate(0.2);
        magneticFieldGainControlU.setArcColor("#a03225");
        magneticFieldGainControlU.setDialColor1("#a03225");
        magneticFieldGainControlU.setDialColor2("#01eeff");
        magneticFieldGainControlU.setValueColor("#d0210e");
        magneticFieldGainControlU.setCurrentValueColor("#a03225");
        magneticFieldGainControlU.setTitle(getString(R.string.gain));
        magneticFieldGainControlU.setTemp(0, 100, 70);   //GC20181102 magneticFieldGainControl.setTemp(0, 100, 63);
        magneticFieldGainControlU.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {

                seekbarCichang.setProgress(temp);
                tvCichangValue.setText(temp + "%");
                Constant.magneticFieldGain = temp;
                magneticFieldGainControlU.setEnabled(false);
                cichangSeekbarInts[0] = cichangSeekbarInts[1];
                cichangSeekbarInts[1] = temp;

                seekbarType = 1;
                int[] ints = {96, 0, 128 + b2s(temp)};
                long l = getRequestCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuffer ss = new StringBuffer();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32, s.length());
                }
                String substring1 = s.substring(0, 8);
                String substring2 = s.substring(8, 16);
                String substring3 = s.substring(16, 24);
                String substring4 = s.substring(24, 32);
                Integer integer1 = Integer.valueOf(substring1, 2);
                Integer integer2 = Integer.valueOf(substring2, 2);
                Integer integer3 = Integer.valueOf(substring3, 2);
                Integer integer4 = Integer.valueOf(substring4, 2);

                byte[] request = new byte[7];
                request[0] = (byte) ints[0];
                request[1] = (byte) ints[1];
                request[2] = (byte) ints[2];
                request[3] = (byte) integer1.intValue();
                request[4] = (byte) integer2.intValue();
                request[5] = (byte) integer3.intValue();
                request[6] = (byte) integer4.intValue();
                sendString(request);

            }
        });
//        voiceGainControl.setAngleRate(1);
        voiceGainControlU.setArcColor("#026b02");    //弧度颜色
        voiceGainControlU.setDialColor1("#026b02");  //未选中刻度颜色
        voiceGainControlU.setDialColor2("#01eeff");  //选中刻度颜色
        voiceGainControlU.setValueColor("#00ec03");  //最大最小值颜色
        voiceGainControlU.setCurrentValueColor("#026b02");   //当前设置值颜色
        voiceGainControlU.setTitle(getString(R.string.gain));
        voiceGainControlU.setTemp(0, 100, 70);   //GC20181102 voiceGainControl.setTemp(0, 100, 45);
        voiceGainControlU.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {

                seekbarShengyin.setProgress(temp);
                tvShengyinValue.setText(temp + "%");
                Constant.voiceGain = temp;
                voiceGainControlU.setEnabled(false);
                shengyinSeekbarInts[0] = shengyinSeekbarInts[1];
                shengyinSeekbarInts[1] = temp;

                seekbarType = 2;
                int[] ints = {96, 0, b2s(temp)};
                long l = getRequestCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuffer ss = new StringBuffer();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32, s.length());
                }
                String substring1 = s.substring(0, 8);
                String substring2 = s.substring(8, 16);
                String substring3 = s.substring(16, 24);
                String substring4 = s.substring(24, 32);
                Integer integer1 = Integer.valueOf(substring1, 2);
                Integer integer2 = Integer.valueOf(substring2, 2);
                Integer integer3 = Integer.valueOf(substring3, 2);
                Integer integer4 = Integer.valueOf(substring4, 2);

                byte[] request = new byte[7];
                request[0] = (byte) ints[0];
                request[1] = (byte) ints[1];
                request[2] = (byte) ints[2];
                request[3] = (byte) integer1.intValue();
                request[4] = (byte) integer2.intValue();
                request[5] = (byte) integer3.intValue();
                request[6] = (byte) integer4.intValue();
                sendString(request);

            }
        });

    }
    //设置seekBar的回掉S
    private void setSeekBar() {
        //磁场seekbar数值改变执行的回掉方法
        seekbarCichang.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCichangValue.setText(progress + "%");
                Constant.magneticFieldGain = progress;
                //seekbarCichang.setProgress(progress);
                magneticFieldGainControlU.setTemp(0, 100, progress);    //GC2.01.005 界面无缝切换

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarCichang.setEnabled(false);
                cichangSeekbarInts[0] = cichangSeekbarInts[1];
                cichangSeekbarInts[1] = seekBar.getProgress();

                seekbarType = 1;
                int[] ints = {96, 0, 128 + b2s(seekBar.getProgress())};
                long l = getRequestCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuffer ss = new StringBuffer();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32, s.length());
                }
                String substring1 = s.substring(0, 8);
                String substring2 = s.substring(8, 16);
                String substring3 = s.substring(16, 24);
                String substring4 = s.substring(24, 32);
                Integer integer1 = Integer.valueOf(substring1, 2);
                Integer integer2 = Integer.valueOf(substring2, 2);
                Integer integer3 = Integer.valueOf(substring3, 2);
                Integer integer4 = Integer.valueOf(substring4, 2);

                byte[] request = new byte[7];
                request[0] = (byte) ints[0];
                request[1] = (byte) ints[1];
                request[2] = (byte) ints[2];
                request[3] = (byte) integer1.intValue();
                request[4] = (byte) integer2.intValue();
                request[5] = (byte) integer3.intValue();
                request[6] = (byte) integer4.intValue();
                sendString(request);
            }

        });
        //声音seekbar数值改变执行的回掉方法
        seekbarShengyin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvShengyinValue.setText(progress + "%");
                Constant.voiceGain = progress;
                voiceGainControlU.setTemp(0, 100, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarShengyin.setEnabled(false);
                shengyinSeekbarInts[0] = shengyinSeekbarInts[1];
                shengyinSeekbarInts[1] = seekbarShengyin.getProgress();
                //Log.e("打印-设置shengyin", seekbarShengyin.getProgress() + "");
                seekbarType = 2;
                int[] ints = {96, 0, b2s(seekbarShengyin.getProgress())};
                long l = getRequestCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuffer ss = new StringBuffer();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32, s.length());
                }
                String substring1 = s.substring(0, 8);
                String substring2 = s.substring(8, 16);
                String substring3 = s.substring(16, 24);
                String substring4 = s.substring(24, 32);
                Integer integer1 = Integer.valueOf(substring1, 2);
                Integer integer2 = Integer.valueOf(substring2, 2);
                Integer integer3 = Integer.valueOf(substring3, 2);
                Integer integer4 = Integer.valueOf(substring4, 2);

                byte[] request = new byte[7];
                request[0] = (byte) ints[0];
                request[1] = (byte) ints[1];
                request[2] = (byte) ints[2];
                request[3] = (byte) integer1.intValue();
                request[4] = (byte) integer2.intValue();
                request[5] = (byte) integer3.intValue();
                request[6] = (byte) integer4.intValue();
                sendString(request);

            }

        });

    }
    //获得初始化磁场数据
    private void getCichangData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "cichang.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c = 0;//读取bis流中的下一个字节
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
//            Log.e("FILE", s);
            String[] split = s.split("\\s+");
            //Log.e("FILE","splitSize:"+split.length);
            /*for (String s1 : split) {
                mTempCichangList.add(Integer.parseInt(s1));

            }*/
           /* for (int i = 0; i < split.length; i++) {
                mTempCichangArray[i] = Integer.parseInt(split[i]);
            }*/
            for (int i = 0; i < split.length; i++) {
                mTempCichangArray[i] = 0;
            }
            myChartAdapterCichang = new MyChartAdapter(mTempCichangArray, null,
                    false, 0, false);

            linechartCichang.setAdapter(myChartAdapterCichang);
            //refreshUi(false, 10);

           /* byte retArr[]=baos.toByteArray();
            for (byte b : retArr) {
                Log.e("FILE",""+b);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //获得初始化声音数据
    private void getShengyinData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" +
                "shengyin.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c = 0;//读取bis流中的下一个字节
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            //Log.e("FILE", s);
            String[] split = s.split("\\s+");
            //Log.e("FILE","splitSize:"+split.length);
            /* for (String s1 : split) {
                mTempShengyinList.add(Integer.parseInt(s1));

            }*/
            /*for (int i = 0; i < split.length; i++) {
                mTempShengyinArray[i] = Integer.parseInt(split[i]);
            }*/
            for (int i = 0; i < split.length; i++) {
                mTempShengyinArray[i] = 0;
            }


            /*for (Integer integer : mTempShengyinList) {
                Log.e("HEJIA", integer + "");

            }*/
            //Log.e("HEJIA", "size:             " + mTempShengyinList.size());

            //refreshUi(false, 10);
            myChartAdapterShengyin = new MyChartAdapter(mTempShengyinArray, null,
                    false, 0, false);

            linechartShengyin.setAdapter(myChartAdapterShengyin);

           /* byte retArr[]=baos.toByteArray();
            for (byte b : retArr) {
                Log.e("FILE",""+b);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    //监听声音光标位置
    private void setChartListenner() {
        linechartShengyin.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if ((int) value >= 50) {
                    tvYanShi.setText((((int) value - 50) * 0.125) + "ms");
                    //Log.e("VALUE","" + value); //GN 数值从0到399
                } else {
                    tvYanShi.setText(0 + "ms");
                    //Log.e("VALUE","" + value);
                }
            }
        });
    }

    //GN 发送控制命令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendDataFinishEvent event) {
        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (seekbarType == 1) {
                    seekbarCichang.setProgress(cichangSeekbarInts[1]);
                    //seekbarCichang.setProgress(s2b(cichangSeekbarInts[0]));   //GC20180609 修改增益显示方式（百分比或实际值）
                    cichangSeekbarInts[1] = cichangSeekbarInts[0];
                } else if (seekbarType == 2) {
                    seekbarShengyin.setProgress(shengyinSeekbarInts[1]);
                    //seekbarShengyin.setProgress(s2b(shengyinSeekbarInts[0])); //同理 磁场
                    shengyinSeekbarInts[1] = shengyinSeekbarInts[0];
                }
                seekbarType = 0;
                llFilter.setClickable(true);
                seekbarCichang.setEnabled(true);
                seekbarShengyin.setEnabled(true);
                voiceGainControlU.setEnabled(true);
                magneticFieldGainControlU.setEnabled(true);
                hasSendMessage = false;
            }
        }, 500);
    }
    //GN 接收控制命令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HandleReceiveDataEvent event) {
        if (seekbarType == 1) {
            seekbarCichang.setProgress(cichangSeekbarInts[0]);
            cichangSeekbarInts[1] = cichangSeekbarInts[0];;
        } else if (seekbarType == 2) {
            seekbarShengyin.setProgress(shengyinSeekbarInts[0]);
            shengyinSeekbarInts[1] = shengyinSeekbarInts[0];
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UINoticeEvent event) {
        if (event.status == SEND_SUCCESS) {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    llFilter.setClickable(true);
                }
            }, 500);
        }
        if (event.status == SEND_ERROR) {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    llFilter.setClickable(true);
                    seekbarCichang.setEnabled(true);
                    seekbarShengyin.setEnabled(true);

                }
            }, 500);
            Toast.makeText(MainActivity.this, getResources().getString(R.string
                    .The_sending_data_failed_and_was_being_resent), Toast.LENGTH_SHORT).show();
        }
        if (event.status == BLUETOOTH_DISCONNECTED) {
            toastDisconnected = true;
            if (!isExit) {
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getResources().getString(R.string.tishi))
                        .setContentText(getResources().getString(R.string
                                .Bluetooth_disconnected_please_reconnect))
                        /*.setCancelText("不，谢谢")*/
                        .setConfirmText(getResources().getString(R.string.Exit_application))
                        .showCancelButton(true)
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismiss();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                finish();
                                MyApplication.getInstances().get_bluetooth().disable();
                                sDialog.dismiss();
                            }
                        })
                        .show();
            }
        }
        if (event.status == WHAT_POSITION_Right) {
            tvPosition.setText(getResources().getString(R.string.right));
            layoutParams.setMargins(Utils.dp2px(MainActivity.this, 100), Utils.dp2px(MainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPositionU.setLayoutParams(params);    //GC2.01.005 界面无缝切换
        }
        if (event.status == WHAT_POSITION_LEFT) {
            tvPosition.setText(getResources().getString(R.string.left));
            layoutParams.setMargins(Utils.dp2px(MainActivity.this, 40), Utils.dp2px(MainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPositionU.setLayoutParams(params);
        }

        if (event.status == WHAT_LIGHT) {
            ivSynchronizeStatus.setImageResource(R.drawable.ic_synchronize_status_33);
            tvNotice.setText("");   //GC20190307 词条跳动效果
            tvNoticeU.setText("");
            tvPosition.setText("");
        }
        if (event.status == CICHANG_CHANGE_OVER) {
            ivSynchronizeStatus.setImageResource(R.drawable.ic_synchronize_status_22);
        }
        if (event.status == WHAT_REFRESH) {
            if (isDraw) {
                myChartAdapterShengyin.setmTempArray(mTempShengyinArray);
                myChartAdapterShengyin.setShowCompareLine(isCom);
                if (isCom) myChartAdapterShengyin.setmCompareArray(mCompareArray);
                myChartAdapterShengyin.notifyDataSetChanged();
                myChartAdapterCichang.setmTempArray(mTempCichangArray);
                myChartAdapterCichang.notifyDataSetChanged();

            }
            handleGainView(maxShengYin, ivVoiceGainU, 1);    //GC20181113 上下语句顺序调整，否则影响进度条回落功能
            handleGainView(maxCiChang, ivMagneticFieldGainU, 0);
        }

    }
    //GN 控制增益进度条
    private void handleGainView(int maxValue, ImageView imageView, final int type) {
        double a = maxValue / 2048.00;  //GC20181113 最大值重新计算
        int b = (int) (a * 100);
        if (b >= 0 && b < 10) {
            currentPosition = 0;
        } else if (b >= 10 && b < 20) {
            currentPosition = 1;
        } else if (b >= 20 && b < 30) {
            currentPosition = 2;
        } else if (b >= 30 && b < 40) {
            currentPosition = 3;
        } else if (b >= 40 && b < 50) {
            currentPosition = 4;
        } else if (b >= 50 && b < 60) {
            currentPosition = 5;
        } else if (b >= 60 && b < 70) {
            currentPosition = 6;
        } else if (b >= 70 && b < 80) {
            currentPosition = 7;
        } else if (b >= 80 && b < 90) {
            currentPosition = 8;
        } else if (b >= 90 && b < 100) {
            currentPosition = 9;
        } else if (b >= 100) {
            currentPosition = 10;
        }
        //GC20181113 判断结构修改
        if (type == 0) {
            changeMagneticFieldGainView(imageView, currentPosition);
            maxCiChang = 0;     //GC20181113 刷新之后归零
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = currentPosition - 1; i >= 0; i--) {
                        SystemClock.sleep(200);
                        Message message = new Message();
                        message.what = 4;
                        message.obj = i;
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        } else if (type == 1) {
            changeVoiceGainView(ivVoiceGainU, currentPosition);
            maxShengYin = 0;    //GC20181113 刷新之后归零
            //GC20181121 添加声音回落
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = currentPosition - 1; i >= 0; i--) {
                        SystemClock.sleep(200);
                        Message message = new Message();
                        message.what = 5;
                        message.obj = i;
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        }

    }
    public void changeMagneticFieldGainView(ImageView imageView, int position) {
        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_0);
                break;
            case 1:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_3);
                break;
            case 4:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_4);
                break;
            case 5:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_5);
                break;
            case 6:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_6);
                break;
            case 7:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_7);
                break;
            case 8:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_8);
                break;
            case 9:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_9);
                break;
            case 10:
                imageView.setImageResource(R.drawable.ic_magnetic_field_gain_10);
                break;
        }

    }
    public void changeVoiceGainView(ImageView imageView, int position) {
        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.ic_voice_gain_0);
                break;
            case 1:
                imageView.setImageResource(R.drawable.ic_voice_gain_1);
                break;
            case 2:
                imageView.setImageResource(R.drawable.ic_voice_gain_2);
                break;
            case 3:
                imageView.setImageResource(R.drawable.ic_voice_gain_3);
                break;
            case 4:
                imageView.setImageResource(R.drawable.ic_voice_gain_4);
                break;
            case 5:
                imageView.setImageResource(R.drawable.ic_voice_gain_5);
                break;
            case 6:
                imageView.setImageResource(R.drawable.ic_voice_gain_6);
                break;
            case 7:
                imageView.setImageResource(R.drawable.ic_voice_gain_7);
                break;
            case 8:
                imageView.setImageResource(R.drawable.ic_voice_gain_8);
                break;
            case 9:
                imageView.setImageResource(R.drawable.ic_voice_gain_9);
                break;
            case 10:
                imageView.setImageResource(R.drawable.ic_voice_gain_10);
                break;
        }

    }
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int position = (int) msg.obj;
            if (msg.what == 4) {
                changeMagneticFieldGainView(ivMagneticFieldGainU, position);
            } else if (msg.what == 5) {
                changeVoiceGainView(ivVoiceGainU, position);     //GC20181121 添加声音回落
            }
            return false;
        }
    });
    //GC20190123 智能算法识别声音的结果
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OperationGuideEvent event) {
        //GN 关闭闪烁动画 //GC20190123 每次智能识别都会先关闭一下
        if (valueAnimator2 != null) {
            valueAnimator2.end();
        }
        if (event.isMalfunction) {
            if (firstFind) {    //GN 第一次判断为故障声采取下列操作，后续不采取任何操作
                /*//GN “已发现故障”中圈 状态2
                tvNotice.setText(getString(R.string.message_notice_7));
                //GN 去动画1
                rlWaveU.removeView(v);               //GN 波纹
                tvScanU.setVisibility(View.GONE);    //GN 正在测试中
                ivScanU.setVisibility(View.GONE);    //GN ...
                //GN 显示动画2
                ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                ccvFirstU.setVisibility(View.GONE);
                ccvSecondU.setVisibility(View.VISIBLE);*/   //GC20190123 不作任何处理
                //延时值显示
                if(lastDelayValue > 0){
                    tvLastDelayU.setText(lastDelayValue + "ms");    //GN 有过相关后的声磁延时值
                }else{
                    tvLastDelayU.setText("");
                }
                tvCurrentDelayU.setText("");

                positionState = 3;
                firstFind = false;
            }

        } else {
           /* //GN “未发现故障”波纹 状态1
            tvNotice.setText(getString(R.string.message_notice_6));*/
            //GC20190123 相关为否的状态 判断“磁场触发”，显示动画1
            tvNotice.setText(getString(R.string.triggered));
            tvNoticeU.setText(getString(R.string.triggered));   //GC201901232
            //GN 去动画2
            ccvFirstU.setVisibility(View.GONE);
            ccvSecondU.setVisibility(View.GONE);
            //GN 显示动画1
            try {
                rlWaveU.addView(v);                  //GN 波纹
            }catch (Exception l_ex){}
            tvScanU.setVisibility(View.VISIBLE); //GN 正在测试中
            ivScanU.setVisibility(View.VISIBLE); //GN ...
            //延时值显示
            if(lastDelayValue > 0){
                tvLastDelayU.setText(lastDelayValue + "ms");    //GN 有过相关后的声磁延时值
            }else{
                tvLastDelayU.setText("");
            }
            tvCurrentDelayU.setText("");

            firstFind = true;   //GC20181119
        }

    }
    //GC20181119 信息框提示2
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AcousticMagneticDelay2 event) {
        if (event.isRelated) {
            //GC20190123 相关为是的状态，判断“已发现故障”，显示动画2
            tvNotice.setText(getString(R.string.message_notice_7));
            tvNoticeU.setText(getString(R.string.message_notice_7));    //GC201901232
            //GN 去动画1
            rlWaveU.removeView(v);               //GN 波纹
            tvScanU.setVisibility(View.GONE);    //GN 正在测试中
            ivScanU.setVisibility(View.GONE);    //GN ...
            //GN 显示动画2
            ccvFirstU.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
            ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
            if (valueAnimator2 == null) {
                valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int i = (int) animation.getAnimatedValue();
                        if (i == 0) {
                            ccvSecondU.setVisibility(View.VISIBLE);
                            ccvFirstU.setVisibility(View.GONE);
                        } else if (i == 1) {
                            ccvSecondU.setVisibility(View.GONE);
                            ccvFirstU.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            valueAnimator2.start();
            //GC20190123 延时值显示
            if (isRelatedCount == 0) {   //GN 第一次相关
                //GN 显示上次延时值
                if(lastDelayValue < 0){     //GN 历史记录第一次相关
                    tvLastDelayU.setText("");
                }else if (lastDelayValue > 0) {  //GN 有过相关后的声磁延时值，
                    tvLastDelayU.setText(lastDelayValue + "ms");
                }
                //GN 显示当前延时值
                tvCurrentDelayU.setText(event.delayValue + "ms");
                //GC20190218 专家界面延时值显示
                tvYanShi.setText(event.delayValue + "ms");
                linechartShengyin.setScrubLine3(event.position);
                //GN 保存到上次的声磁延时值
                lastDelayValue = event.delayValue;
                lastPosition = event.position;  //GC20190218
                //GN 保存历史最小声磁延时值
                if (event.delayValue < minDelayValue) {
                    minDelayValue = event.delayValue;
                }
                //GN 显示历史最小声磁延时值
                llMinDelayU.setVisibility(View.VISIBLE);
                tvMinDelayValueU.setText(minDelayValue + "ms");

            } else if (isRelatedCount > 0) {    //从第二次相关开始后继续相关
                tvLastDelayU.setText(lastDelayValue + "ms");
                tvCurrentDelayU.setText(lastDelayValue + "ms");
                //GC20190218 专家界面延时值显示
                tvYanShi.setText(lastDelayValue + "ms");
                linechartShengyin.setScrubLine3(lastPosition);

            }
            isRelatedCount++;

        }else{
            isRelatedCount = 0;
            //GC20190123 相关为否的状态 判断“磁场触发”，显示动画1
            tvNotice.setText(getString(R.string.triggered));
            tvNoticeU.setText(getString(R.string.triggered));   //GC201901232
            //GC20190123 去动画2
            ccvFirstU.setVisibility(View.GONE);
            ccvSecondU.setVisibility(View.GONE);
            //GC20190123 显示动画1
            try {
                rlWaveU.addView(v);                  //GN 波纹
            }catch (Exception l_ex){}
            tvScanU.setVisibility(View.VISIBLE); //GN 正在测试中
            ivScanU.setVisibility(View.VISIBLE); //GN ...
            //延时值显示
            if(lastDelayValue > 0){
                tvLastDelayU.setText(lastDelayValue + "ms");    //GN 有过相关后的声磁延时值
            }else{
                tvLastDelayU.setText("");
            }
            tvCurrentDelayU.setText("");

        }
        //GC20190213 去掉接近远离逻辑判断，简化
        /*if (event.isRelated) {
            if (isRelatedCount == 0) {   //GN 第一次相关
                if(lastDelayValue < 0){
                    //GN 历史第一次发现故障 中圈闪烁 状态3
                    tvNotice.setText(getString(R.string.message_notice_7));
                    tvLastDelayU.setText("");
                    tvCurrentDelayU.setText(event.delayValue + "ms");
                    ccvFirstU.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                    ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                    if (valueAnimator2 == null) {
                        valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                        valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int i = (int) animation.getAnimatedValue();
                                if (i == 0) {
                                    ccvSecondU.setVisibility(View.VISIBLE);
                                    ccvFirstU.setVisibility(View.GONE);
                                } else if (i == 1) {
                                    ccvSecondU.setVisibility(View.GONE);
                                    ccvFirstU.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    valueAnimator2.start();

                } else if (lastDelayValue > 0) {  //GN 有过相关后的声磁延时值，比较大小
                    if (lastDelayValue < (event.delayValue - 0.375)) {  //GC20181204 3个点的波动范围
                        //GN 远离故障点 大圈 状态4
                        tvNotice.setText(getString(R.string.message_notice_9));
                        tvLastDelayU.setText(lastDelayValue + "ms");
                        tvCurrentDelayU.setText(event.delayValue + "ms");
                        ccvFirstU.updateView("#555555", 5, 40);      //灰色 细圈 小尺寸
                        ccvSecondU.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                        ccvFirstU.setVisibility(View.VISIBLE);
                        ccvSecondU.setVisibility(View.VISIBLE);
                        positionState = 1;

                    } else if (lastDelayValue > (event.delayValue + 0.375)) {   //GC20181204 3个点的波动范围
                        //GN 接近故障点 小圈 状态6
                        tvNotice.setText(getString(R.string.message_notice_8));
                        tvLastDelayU.setText(lastDelayValue + "ms");
                        tvCurrentDelayU.setText(event.delayValue + "ms");
                        ccvFirstU.updateView("#555555", 5, 138);     //灰色 细圈 大尺寸
                        ccvSecondU.updateView("#e1de04", 20, 40);    //黄色 粗圈 小尺寸
                        ccvFirstU.setVisibility(View.VISIBLE);
                        ccvSecondU.setVisibility(View.VISIBLE);
                        positionState = 2;

                    } else if ( (lastDelayValue <= (event.delayValue + 0.375)) && (lastDelayValue >= (event.delayValue - 0.375)) ) {    //GC20181204 3个点的波动范围
                        tvNotice.setText(getString(R.string.message_notice_7));
                        tvLastDelayU.setText(event.delayValue + "ms");   //GC20181122 经过相关为否或不是故障之后再次相关，且前后差值不大，更新为最新值
                        tvCurrentDelayU.setText(event.delayValue + "ms");
                        if(positionState == 1) {            //GN  后续发现故障 大圈闪烁 状态5
                            ccvFirstU.updateView("#555555", 20, 138);    //灰色 粗圈 大尺寸
                            ccvSecondU.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                        }else if(positionState == 2) {      //GN  后续发现故障 小圈闪烁 状态7
                            ccvFirstU.updateView("#555555", 20, 40);    //灰色 粗圈 小尺寸
                            ccvSecondU.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
                        }else if(positionState == 3) {      //GN  后续发现故障 中圈闪烁 状态3
                            ccvFirstU.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                            ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                        }
                        if (valueAnimator2 == null) {
                            valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                            valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                            valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int i = (int) animation.getAnimatedValue();
                                    if (i == 0) {
                                        ccvSecondU.setVisibility(View.VISIBLE);
                                        ccvFirstU.setVisibility(View.GONE);
                                    } else if (i == 1) {
                                        ccvSecondU.setVisibility(View.GONE);
                                        ccvFirstU.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                        valueAnimator2.start();
                    }
                }
                //GN 只有第一次相关才刷新声磁延时值
                lastDelayValue = event.delayValue;      //GN 保存到上次的声磁延时值
                if (event.delayValue < minDelayValue) {
                    minDelayValue = event.delayValue;   //GN 保存历史最小声磁延时值
                }
                llMinDelayU.setVisibility(View.VISIBLE);
                tvMinDelayValueU.setText(minDelayValue + "ms");  //GN 显示历史最小声磁延时值

            } else if (isRelatedCount > 0) {    //从第二次相关开始后继续相关
                tvNotice.setText(getString(R.string.message_notice_7));
                tvLastDelayU.setText(lastDelayValue + "ms");
                tvCurrentDelayU.setText(lastDelayValue + "ms");
                if(positionState == 1) {                //GN  后续发现故障 大圈闪烁 状态5
                    ccvFirstU.updateView("#555555", 20, 138);    //灰色 粗圈 大尺寸
                    ccvSecondU.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                }else if(positionState == 2) {          //GN  后续发现故障 小圈闪烁 状态7
                    ccvFirstU.updateView("#555555", 20, 40);    //灰色 粗圈 小尺寸
                    ccvSecondU.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
                }else if(positionState == 3) {          //GN  后续发现故障 中圈闪烁 状态3
                    ccvFirstU.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                    ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                }
                if (valueAnimator2 == null) {
                    valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                    valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                    valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int i = (int) animation.getAnimatedValue();
                            if (i == 0) {
                                ccvSecondU.setVisibility(View.VISIBLE);
                                ccvFirstU.setVisibility(View.GONE);
                            } else if (i == 1) {
                                ccvSecondU.setVisibility(View.GONE);
                                ccvFirstU.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                valueAnimator2.start();

            }
            isRelatedCount++;

        }else{  //相关结果为否
            isRelatedCount = 0;
            //GN “已发现故障”状态
            tvNotice.setText(getString(R.string.message_notice_7));
            if(lastDelayValue > 0){     //GN 有过相关后的声磁延时值
                tvLastDelayU.setText(lastDelayValue + "ms");
            }else{
                tvLastDelayU.setText("");
            }
            tvCurrentDelayU.setText("");
            if(positionState == 1) {                        //GN  相关为否 大圈 状态8
                ccvSecondU.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
            }else if(positionState == 2) {                  //GN  相关为否 小圈 状态9
                ccvSecondU.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
            }else if(positionState == 3) {                  //GN  相关为否 中圈 状态10
                ccvSecondU.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
            }
            ccvFirstU.setVisibility(View.GONE);
            ccvSecondU.setVisibility(View.VISIBLE);

        }*/
        //GC20181201 去掉波形界面自动定位提示
        /*if (event.isRelated) {
            if (isRelatedCount == 0) {   //GN 第一次相关
                if(lastDelayValue < 0){ //历史第一次发现故障
                    tvNotice.setText(getString(R.string.message_notice_7));
                    //GN 只有第一次相关才刷新声磁延时值
                    lastDelayValue = event.delayValue;      //GN 保存到上次的声磁延时值
                } else if (lastDelayValue > 0) {  //GN 有过相关后的声磁延时值，比较大小
                    if (lastDelayValue < (event.delayValue - 0.625)) {
                        tvNotice.setText(getString(R.string.message_notice_9));
                        positionState = 1;
                        //GN 只有第一次相关才刷新声磁延时值
                        lastDelayValue = event.delayValue;      //GN 保存到上次的声磁延时值
                    } else if (lastDelayValue > (event.delayValue + 0.625)) {
                        tvNotice.setText(getString(R.string.message_notice_8));
                        positionState = 2;
                        //GN 只有第一次相关才刷新声磁延时值
                        lastDelayValue = event.delayValue;      //GN 保存到上次的声磁延时值
                    } else if ( (lastDelayValue <= (event.delayValue + 0.625)) && (lastDelayValue >= (event.delayValue - 0.625)) ) {
                        tvNotice.setText(getString(R.string.message_notice_7));

                    }
                }

            } else if (isRelatedCount > 0) {    //从第二次相关开始后继续相关
                tvNotice.setText(getString(R.string.message_notice_7));

            }
            isRelatedCount++;

        }else{  //相关结果为否
            isRelatedCount = 0;
            tvNotice.setText(getString(R.string.message_notice_7));

        }*/

    }
    //自动计算声音信号的光标位置和声磁延时值（仪器触发，发现是故障声音时）
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AcousticMagneticDelayEvent event) {
        //GC20181201 保留专家界面光标自动定位
        linechartShengyin.setScrubLine3(event.position);
        tvYanShi.setText((event.delayValue) + "ms");
    }

    @OnClick({R.id.ll_silence, R.id.ll_pause, R.id.ll_memory, R.id.ll_compare, R.id.ll_filter, R.id.ll_assist, R.id.ll_settings, R.id.ll_mode,R.id.ll_voice_u, R.id.ll_filter_u, R.id.ll_assist_u, R.id.ll_settings_u, R.id.ll_mode_u})
    public void onViewClicked(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_silence:
                clickSilence();
                break;
            case R.id.ll_pause:
                clickPause();
                break;
            case R.id.ll_memory:
                clickMemory();
                llMemory.setBackground(getResources().getDrawable(R.drawable.bg_expert_btn_select));
                llMemory.setClickable(false);
                mHandle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        llMemory.setBackground(getResources().getDrawable(R.drawable.ic_btn_expert));
                        llMemory.setClickable(true);
                    }
                }, 250);
                break;
            case R.id.ll_compare:
                clickCompare();
                if (!isCom) {
                    llCompare.setBackground(getResources().getDrawable(R.drawable.ic_btn_expert));
                } else {
                    llCompare.setBackground(getResources().getDrawable(R.drawable.bg_expert_btn_select));
                }
                break;
            case R.id.ll_filter:
                clickFilter();
                break;
            case R.id.ll_assist:
                clickAssist();
                break;
            case R.id.ll_settings:
                clickSetting();
                break;
            case R.id.ll_mode:
                clickMode();
                break;
            case R.id.ll_voice_u:
                clickSilence();
                break;
            case R.id.ll_filter_u:
                clickFilter();
                break;
            case R.id.ll_assist_u:
                intent.setClass(MainActivity.this, AssistListActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_settings_u:
                intent.setClass(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_mode_u:
                //GC201901231 模式切换更改
                /*PrefUtils.setString(MainActivity.this, AppConfig.CURRENT_MODE, "expert");
                PrefUtils.setString(MainActivity.this, AppConfig.CLICK_MODE, "clicked");   //GC20181116*/
                /*intent.setAction("restartapp");
                sendBroadcast(intent);*/
                llUserUI.setVisibility(View.INVISIBLE);
                llUserUI.setEnabled(false);
                llVoiceU.setEnabled(false);
                llAssistU.setEnabled(false);
                llSettingsU.setEnabled(false);
                llFilterU.setEnabled(false);
                llMainUI.setVisibility(View.VISIBLE);
                llMainUI.setEnabled(true);
//                finish();
                break;
        }
    }
    //点击静音按钮执行的方法
    public void clickSilence() {
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        /*System.out.println("streamMaxVolume:" + streamMaxVolume);
        System.out.println("streamVolume:" + streamVolume);*/
        if (isSilence) {
            if (streamVolumenow == 0) {
                streamVolumenow = streamMaxVolume / 2;
            }
            //audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolumenow,
                    AudioManager
                            .FLAG_PLAY_SOUND);
            ivSilence.setImageResource(R.drawable.ic_open_voice);
            ivSilenceU.setImageResource(R.drawable.ic_open_voice);
        } else {
            streamVolumenow = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager
                    .FLAG_PLAY_SOUND);
            ivSilence.setImageResource(R.drawable.ic_close_voice);
            ivSilenceU.setImageResource(R.drawable.ic_close_voice);
        }
        isSilence = !isSilence;
        //AplicationUtil.makeToast(this, "clickSilence");
    }
    //点击暂停
    public void clickPause() {
        //mMediaPlayer.stop();
        isDraw = !isDraw;
        if (isDraw) {
            ivPlay.setImageResource(R.drawable.ic_stop);
            tvPlay.setText(getString(R.string.pause));
        } else {
            ivPlay.setImageResource(R.drawable.ic_play);
            tvPlay.setText(getString(R.string.play));
        }
    }
    //点击滤波
    private void clickFilter() {
        showFilterDialog(llFilter);

    }
    //点击协助按钮执行的方法
    public void clickAssist() {
        Intent intent = new Intent(this, AssistListActivity.class);
        startActivity(intent);

    }
    //点击设置
    public void clickSetting() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);

    }
    //点击用户模式
    public void clickMode() {
//        Intent intent = new Intent();
        //GC201901231 模式切换更改PrefUtils.setString(MainActivity.this, AppConfig.CURRENT_MODE, "user");
        PrefUtils.setString(MainActivity.this, AppConfig.CLICK_MODE, "clicked");   //GC20181116
        llUserUI.setVisibility(View.VISIBLE);
        llUserUI.setEnabled(true);
        llVoiceU.setEnabled(true);
        llAssistU.setEnabled(true);
        llSettingsU.setEnabled(true);
        llFilterU.setEnabled(true);

        llMainUI.setEnabled(false);

        llMainUI.setVisibility(View.INVISIBLE);
//        intent.setAction("restartapp");
//        intent.putExtra("type", 1);
//        intent.putExtra("name", "user");
//        sendBroadcast(intent);
//        finish();

    }

    //GN 静音按钮状态监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isExit = true;
            mAudioTrack.release();// 关闭并释放资源
            finish();
            MyApplication.getInstances().get_bluetooth().disable();

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkVoice();
                        }
                    });
                }
            }, 500);
            super.onKeyDown(keyCode, event);

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkVoice();
                        }
                    });
                }
            }, 500);
            super.onKeyDown(keyCode, event);

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            super.onKeyDown(keyCode, event);
          /*  checkVoice();
            super.onKeyDown(keyCode, event);
            checkVoice();*/
        }
        return false;

    }
    public void checkVoice() {
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (streamVolume <= 0) {
            isSilence = true;
            ivSilence.setImageResource(R.drawable.ic_close_voice);
        } else {
            isSilence = false;
            ivSilence.setImageResource(R.drawable.ic_open_voice);
        }

    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    //显示等待的弹窗
    public void showProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = ShowProgressDialog.createLoadingDialog(MainActivity.this);
        dialog.show();
    }
    @Override
    protected void onDestroy() {
        try {
            instance = null;
//            if (mSocket != null) {
//                mSocket.close();
////                mInputStream.close();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();

    }

}

/*更改记录*/
//GC20180609 修改增益显示方式（百分比或实际值）
//GC20181115 改进“已发现故障”提示
//GC20181201 去掉波形界面自动定位和提示