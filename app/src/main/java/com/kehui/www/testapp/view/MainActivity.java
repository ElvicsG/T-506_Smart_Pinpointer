package com.kehui.www.testapp.view;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
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
import com.kehui.www.testapp.adpter.MyChartAdapterBase;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.base.BaseActivity;
import com.kehui.www.testapp.event.ResultOfRelevantEvent;
import com.kehui.www.testapp.event.SendCommandNotRespondEvent;
import com.kehui.www.testapp.event.ResultOfSvmEvent;
import com.kehui.www.testapp.event.SendCommandFinishEvent;
import com.kehui.www.testapp.event.UiHandleEvent;
import com.kehui.www.testapp.ui.CustomCircleView;
import com.kehui.www.testapp.ui.PercentLinearLayout;
import com.kehui.www.testapp.ui.SparkView.SparkView;
import com.kehui.www.testapp.ui.TempControlView;
import com.kehui.www.testapp.ui.WaterWaveView;
import com.kehui.www.testapp.util.Utils;
import com.kehui.www.testapp.util.SoundUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Gong
 * @date 2019/07/17
 */
public class MainActivity extends BaseActivity {

    /**
     * 浮动层   //GC20190215 界面无缝切换
     */
    @BindView(R.id.mainUI)
    LinearLayout llMainUI;
    @BindView(R.id.userUI)
    LinearLayout llUserUI;

    /**
     * 专家界面布局
     */
    //GC20191217
    @BindView(R.id.il_silence)
    TextView ilSilence;
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
    SeekBar seekBarMagnetic;
    @BindView(R.id.linechart_cichang)
    SparkView lineChartMagnetic;
    @BindView(R.id.seekbar_shengyin)
    SeekBar seekBarVoice;
    @BindView(R.id.linechart_shengyin)
    SparkView lineChartVoice;
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
    TextView tvMagneticValue;
    @BindView(R.id.tv_shengyin_value)
    TextView tvVoiceValue;
    @BindView(R.id.iv_synchronize_status)
    ImageView ivSynchronizeStatus;
    @BindView(R.id.tv_yan_shi)
    TextView tvYanShi;
    @BindView(R.id.tv_position)
    TextView tvPosition;

    /**
     * 用户界面布局
     */
    //GC20191217
    @BindView(R.id.il_silence_u)
    TextView ilSilenceU;
    @BindView(R.id.magnetic_field_gain_control_u)
    TempControlView magneticFieldGainControlU;
    @BindView(R.id.iv_magnetic_field_gain_u)
    ImageView ivMagneticGainU;
    @BindView(R.id.voice_gain_control_u)
    TempControlView voiceGainControlU;
    @BindView(R.id.iv_voice_gain_u)
    ImageView ivVoiceGainU;
    @BindView(R.id.tv_notice_u)
    TextView tvNoticeU;
    @BindView(R.id.iv_position_u)
    ImageView ivPositionU;
    @BindView(R.id.iv_position_right)
    ImageView ivPositionRight;
    @BindView(R.id.iv_position_left)
    ImageView ivPositionLeft;
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

    /**
     * 专家界面
     */
    public static MainActivity instance;
    private int cursorPosition;

    /**
     * 用户界面
     */
    private ValueAnimator valueAnimator;
    private int[] scoreText = {R.drawable.ic_wait_empty, R.drawable.ic_wait_1, R.drawable.ic_wait_2, R.drawable.ic_wait_3};
    private WaterWaveView v;
    private ViewGroup.MarginLayoutParams layoutParams;
    private int gainPosition;
    private double lastDelayValue = -1;
    private double currentDelayValue = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持亮屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        instance = this;
        initView();
        setSeekBar();
        getVoiceData();
        getMagneticData();
        setChartListener();
        //读取提示音 //GC20190422
        soundSystem = new SoundUtils(this);
    }

    /**
     * 初始化试图
     */
    private void initView() {
        //专家界面
        seekBarMagnetic.setMax(100);
        seekBarVoice.setMax(100);
        seekBarMagnetic.setProgress(70);
        seekBarVoice.setProgress(70);
        tvMagneticValue.setText(70 + "%");
        tvVoiceValue.setText(70 + "%");
        checkVoice();

        //用户界面  //GC20190215 界面无缝切换
        ccvFirstU.setVisibility(View.GONE);
        ccvSecondU.setVisibility(View.GONE);
        //去掉最小延时值显示    //GC20190717
        llMinDelayU.setVisibility(View.GONE);
        //画动画1——波纹  正在测试中   ...
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

        /*//GT //GC20190717
        //去动画1——波纹  正在测试中   ...
        rlWaveU.removeView(v);
        tvScanU.setVisibility(View.GONE);
        ivScanU.setVisibility(View.GONE);
        //画动画2
        //颜色改动之前——灰色"#555555"  "黄色#e1de04"
        ccvFirstU.updateView("#00ffde", 8, 19);
        //GC20190724
        ccvFirstU.setVisibility(View.VISIBLE);
        tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
        tvCurrentDelayU.setText(getString(R.string.current) + currentDelayValue + "ms");*/

        //设置探头位置
        //GC20191011 layoutParams = new ViewGroup.MarginLayoutParams(ivPositionU.getLayoutParams());
        //设置磁场增益显示
        magneticFieldGainControlU.setArcColor("#a03225");
        magneticFieldGainControlU.setDialColor1("#a03225");
        magneticFieldGainControlU.setDialColor2("#01eeff");
        magneticFieldGainControlU.setValueColor("#d0210e");
        magneticFieldGainControlU.setCurrentValueColor("#a03225");
        magneticFieldGainControlU.setTitle(getString(R.string.gain));
        magneticFieldGainControlU.setTemp(0, 100, 70);
        magneticFieldGainControlU.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {
                seekBarMagnetic.setProgress(temp);
                tvMagneticValue.setText(temp + "%");
                Constant.magneticFieldGain = temp;
                magneticFieldGainControlU.setEnabled(false);
                MainActivity.this.seekBarMagneticInt[0] = MainActivity.this.seekBarMagneticInt[1];
                MainActivity.this.seekBarMagneticInt[1] = temp;

                seekBarType = 1;
                int[] ints = {96, 0, 128 + b2s(temp)};
                long l = getCommandCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuilder ss = new StringBuilder();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32);
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
                //GC20190407 蓝牙重连功能优化
                Constant.CurrentMagParam = request;
                sendCommand(request);

            }
        });
        //设置声音增益显示
        voiceGainControlU.setArcColor("#026b02");
        voiceGainControlU.setDialColor1("#026b02");
        voiceGainControlU.setDialColor2("#01eeff");
        voiceGainControlU.setValueColor("#00ec03");
        voiceGainControlU.setCurrentValueColor("#026b02");
        voiceGainControlU.setTitle(getString(R.string.gain));
        voiceGainControlU.setTemp(0, 100, 70);
        voiceGainControlU.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {
                seekBarVoice.setProgress(temp);
                tvVoiceValue.setText(temp + "%");
                Constant.voiceGain = temp;
                voiceGainControlU.setEnabled(false);
                MainActivity.this.seekBarVoiceInt[0] = MainActivity.this.seekBarVoiceInt[1];
                MainActivity.this.seekBarVoiceInt[1] = temp;

                seekBarType = 2;
                int[] ints = {96, 0, b2s(temp)};
                long l = getCommandCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuilder ss = new StringBuilder();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32);
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
                //GC20190407 蓝牙重连功能优化
                Constant.CurrentVoiceParam = request;
                sendCommand(request);

            }
        });

    }

    /**
     * 初始化seekBar设置
     */
    private void setSeekBar() {
        //磁场seekBar数值改变执行的回掉方法
        seekBarMagnetic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvMagneticValue.setText(progress + "%");
                Constant.magneticFieldGain = progress;
                //GC20190215 界面无缝切换
                magneticFieldGainControlU.setTemp(0, 100, progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MainActivity.this.seekBarMagnetic.setEnabled(false);
                MainActivity.this.seekBarMagneticInt[0] = MainActivity.this.seekBarMagneticInt[1];
                MainActivity.this.seekBarMagneticInt[1] = seekBar.getProgress();

                seekBarType = 1;
                int[] ints = {96, 0, 128 + b2s(seekBar.getProgress())};
                long l = getCommandCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuilder ss = new StringBuilder();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32);
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
                //GC20190407 蓝牙重连功能优化
                Constant.CurrentMagParam = request;
                sendCommand(request);
            }

        });

        //声音seekBar数值改变执行的回掉方法
        seekBarVoice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVoiceValue.setText(progress + "%");
                Constant.voiceGain = progress;
                voiceGainControlU.setTemp(0, 100, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBarVoice.setEnabled(false);
                MainActivity.this.seekBarVoiceInt[0] = MainActivity.this.seekBarVoiceInt[1];
                MainActivity.this.seekBarVoiceInt[1] = seekBarVoice.getProgress();
                //Log.e("打印-设置shengyin", seekBarVoiceInt.getProgress() + "");
                seekBarType = 2;
                int[] ints = {96, 0, b2s(seekBarVoice.getProgress())};
                long l = getCommandCrcByte(ints);
                String s = Long.toBinaryString((int) l);
                StringBuilder ss = new StringBuilder();
                if (s.length() <= 32) {
                    for (int i = 0; i < (32 - s.length()); i++) {
                        ss.append("0");
                    }
                    s = ss.toString() + s;
                } else {
                    s = s.substring(s.length() - 32);
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
                //GC20190407 蓝牙重连功能优化
                Constant.CurrentVoiceParam = request;
                sendCommand(request);
            }

        });

        //GC20191022 初始化滤波方式为带通
        Constant.filterType = currentFilter;
        clickDaitong();
    }

    /**
     * 初始化磁场数据
     */
    private void getMagneticData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "cichang.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取bis流中的下一个字节
        int c;
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");

            for (int i = 0; i < split.length; i++) {
                magneticDraw[i] = 0;
            }
            myChartAdapterMagnetic = new MyChartAdapterBase(magneticDraw, null,
                    false, 0, false);
            lineChartMagnetic.setAdapter(myChartAdapterMagnetic);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 声音数据初始化
     */
    private void getVoiceData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "shengyin.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取bis流中的下一个字节
        int c;
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");
            for (int i = 0; i < split.length; i++) {
                voiceDraw[i] = 0;
            }
            myChartAdapterVoice = new MyChartAdapterBase(voiceDraw, null,
                    false, 0, false);
            lineChartVoice.setAdapter(myChartAdapterVoice);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 监听声音光标位置
     */
    private void setChartListener() {
        lineChartVoice.setScrubListener(new SparkView.OnScrubListener() {
            @Override
            public void onScrubbed(Object value) {
                if ((int) value >= 50) {
                    tvYanShi.setText((((int) value - 50) * 0.125) + "ms");
                    Log.e("VALUE","" + value); //数值从0到399
                } else {
                    tvYanShi.setText(0 + "ms");
                }
            }
        });
    }

    /**
     * @param event 发送控制命令事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendCommandFinishEvent event) {
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (seekBarType == 1) {
                    seekBarMagnetic.setProgress(MainActivity.this.seekBarMagneticInt[1]);
                    //seekBarMagneticInt.setProgress(s2b(seekBarMagneticInt[0]));   //GC20170609 修改增益显示方式（百分比或实际值）
                    MainActivity.this.seekBarMagneticInt[1] = MainActivity.this.seekBarMagneticInt[0];
                } else if (seekBarType == 2) {
                    seekBarVoice.setProgress(MainActivity.this.seekBarVoiceInt[1]);
                    //seekBarVoiceInt.setProgress(s2b(seekBarVoiceInt[0])); //同理 磁场
                    MainActivity.this.seekBarVoiceInt[1] = MainActivity.this.seekBarVoiceInt[0];
                }
                seekBarType = 0;
                llFilter.setClickable(true);
                seekBarMagnetic.setEnabled(true);
                seekBarVoice.setEnabled(true);
                //GC20190215 界面无缝切换
                llFilterU.setClickable(true);
                voiceGainControlU.setEnabled(true);
                magneticFieldGainControlU.setEnabled(true);
                hasSentCommand = false;
            }
        }, 500);
    }

    //接收控制命令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendCommandNotRespondEvent event) {
        if (seekBarType == 1) {
            seekBarMagnetic.setProgress(this.seekBarMagneticInt[0]);
            this.seekBarMagneticInt[1] = this.seekBarMagneticInt[0];
        } else if (seekBarType == 2) {
            seekBarVoice.setProgress(this.seekBarVoiceInt[0]);
            this.seekBarVoiceInt[1] = this.seekBarVoiceInt[0];
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UiHandleEvent event) {
        //GC20191217
        if (event.status == MUTE_STATE) {
            //GT 触摸
            //sendTouchOffCommand();
            mute = true;
            ivSilence.setImageResource(R.drawable.ic_close_voice);
            ivSilenceU.setImageResource(R.drawable.ic_close_voice);
            //字体图标变红
            ilSilence.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red3));
            ilSilenceU.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.red3));
            ivSilence.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red3));
            ivSilenceU.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.red3));
        }
        if (event.status == NO_MUTE_STATE) {
            mute = false;
            //字体图标变蓝
            ilSilence.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ilSilenceU.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ivSilence.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ivSilenceU.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            checkVoice();
        }
        if (event.status == SEND_SUCCESS) {
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    llFilter.setClickable(true);
                    //GC20190215 界面无缝切换
                    llFilterU.setClickable(true);
                }
            }, 500);
        }
        if (event.status == SEND_ERROR) {
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    llFilter.setClickable(true);
                    seekBarMagnetic.setEnabled(true);
                    seekBarVoice.setEnabled(true);
                    //GC20190215 界面无缝切换
                    llFilterU.setClickable(true);
                    voiceGainControlU.setEnabled(true);
                    magneticFieldGainControlU.setEnabled(true);

                }
            }, 500);
            Toast.makeText(MainActivity.this, getResources().getString(R.string
                    .The_sending_data_failed_and_was_being_resent), Toast.LENGTH_SHORT).show();
        }
        if (event.status == POSITION_RIGHT) {
            tvPosition.setText(getResources().getString(R.string.right));
            //GC20190215 界面无缝切换
            /*layoutParams.setMargins(Utils.dp2px(MainActivity.this, 100), Utils.dp2px(MainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPositionU.setLayoutParams(params);*/
            //设置探头位置 //GC20191011
            ivPositionRight.setVisibility(View.VISIBLE);
            ivPositionLeft.setVisibility(View.INVISIBLE);
        }
        if (event.status == POSITION_LEFT) {
            tvPosition.setText(getResources().getString(R.string.left));
            //GC20190215 界面无缝切换
            /*layoutParams.setMargins(Utils.dp2px(MainActivity.this, 40), Utils.dp2px(MainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPositionU.setLayoutParams(params);*/
            //设置探头位置 //GC20191011
            ivPositionRight.setVisibility(View.INVISIBLE);
            ivPositionLeft.setVisibility(View.VISIBLE);
        }

        if (event.status == LIGHT_UP) {
            ivSynchronizeStatus.setImageResource(R.drawable.light_red);
            //提示框词条和左右位置闪烁一次效果  //GC20190307
            tvNotice.setText("");
            tvPosition.setText("");
            tvNoticeU.setText("");
            //GC20190724
            if (isDrawCircle) {
                ccvFirstU.setVisibility(View.INVISIBLE);
                tvCurrentDelayU.setText("");
            }
        }
        if (event.status == TRIGGERED) {
            ivSynchronizeStatus.setImageResource(R.drawable.light_gray);
        }
        if (event.status == WHAT_REFRESH) {
            if (isDraw) {
                myChartAdapterVoice.setmTempArray(voiceDraw);
                myChartAdapterVoice.setShowCompareLine(isCom);
                if (isCom) {
                    myChartAdapterVoice.setmCompareArray(compareDraw);
                }
                myChartAdapterVoice.notifyDataSetChanged();
                myChartAdapterMagnetic.setmTempArray(magneticDraw);
                myChartAdapterMagnetic.notifyDataSetChanged();

            }
            //上下语句顺序调整，否则影响进度条回落功能  //GC20181113
            handleGainView(maxVoice, ivVoiceGainU, 1);
            handleGainView(maxMagnetic, ivMagneticGainU, 0);
        }
        //GC20190407
        if (event.status == LINK_LOST) {
            Utils.showToast(this, getResources().getString(R.string.Link_Lost_Reconnect));
            tvNotice.setText(getResources().getString(R.string.Link_Lost_Reconnect));
            tvNoticeU.setText(getResources().getString(R.string.Link_Lost_Reconnect));
        }
        //GC20190613
        if (event.status == LINK_RECONNECT) {
            tvNotice.setText(getString(R.string.message_notice_5));
            tvNoticeU.setText(getString(R.string.message_notice_5));
        }

    }

    /**
     * 用户界面磁场或声音的进度条处理    //GC20181113
     *
     * @param maxValue  数据最大值
     * @param imageView 布局选择
     * @param type  控制哪个进度条
     */
    private void handleGainView(int maxValue, ImageView imageView, final int type) {
        //根据最大值计算进度条高度
        double a = maxValue / 2048.00;
        int b = (int) (a * 100);
        if (b >= 0 && b < 10) {
            gainPosition = 0;
        } else if (b >= 10 && b < 20) {
            gainPosition = 1;
        } else if (b >= 20 && b < 30) {
            gainPosition = 2;
        } else if (b >= 30 && b < 40) {
            gainPosition = 3;
        } else if (b >= 40 && b < 50) {
            gainPosition = 4;
        } else if (b >= 50 && b < 60) {
            gainPosition = 5;
        } else if (b >= 60 && b < 70) {
            gainPosition = 6;
        } else if (b >= 70 && b < 80) {
            gainPosition = 7;
        } else if (b >= 80 && b < 90) {
            gainPosition = 8;
        } else if (b >= 90 && b < 100) {
            gainPosition = 9;
        } else if (b >= 100) {
            gainPosition = 10;
        }
        //判断控制哪个进度条
        if (type == 0) {
            changeMagneticGainView(imageView, gainPosition);
            maxMagnetic = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = gainPosition - 1; i >= 0; i--) {
                        SystemClock.sleep(200);
                        Message message = new Message();
                        message.what = 1;
                        message.obj = i;
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        } else if (type == 1) {
            changeVoiceGainView(ivVoiceGainU, gainPosition);
            maxVoice = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = gainPosition - 1; i >= 0; i--) {
                        SystemClock.sleep(200);
                        Message message = new Message();
                        message.what = 2;
                        message.obj = i;
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        }

    }

    /**
     * 增益进度条回落处理
     */
    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int position = (int) msg.obj;
            if (msg.what == 1) {
                changeMagneticGainView(ivMagneticGainU, position);
            } else if (msg.what == 2) {
                changeVoiceGainView(ivVoiceGainU, position);
            }
            return false;
        }
    });

    public void changeMagneticGainView(ImageView imageView, int position) {
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
            default:
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
            default:
                break;
        }

    }


    /**
     * @param event 支持向量机识别声音的结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ResultOfSvmEvent event) {
        if (event.isFault) {
            //从svm认为不是故障声到是故障声，需要再测试一次用来做相关 //GC20181119
            if (firstFind) {
                //不是故障，判断“磁场触发”
                tvNotice.setText(getString(R.string.triggered));
                tvNoticeU.setText(getString(R.string.triggered));
                //延时值显示
                if(lastDelayValue > 0){
                    tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
                }else{
                    tvLastDelayU.setText("");
                }
                tvCurrentDelayU.setText("");
                firstFind = false;
            }

        } else {
            //不是故障，判断“磁场触发”
            tvNotice.setText(getString(R.string.triggered));
            tvNoticeU.setText(getString(R.string.triggered));
            //延时值显示
            tvCurrentDelayU.setText("");
            if(lastDelayValue > 0){
                tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
            }else{
                tvLastDelayU.setText("");
            }
            //去动画2
            ccvFirstU.setVisibility(View.GONE);
            ccvSecondU.setVisibility(View.GONE);
            //GC20190724
            isDrawCircle = false;
            //画动画1——波纹  正在测试中   ...
            try {
                rlWaveU.addView(v);
            }catch (Exception ignored){
            }
            tvScanU.setVisibility(View.VISIBLE);
            ivScanU.setVisibility(View.VISIBLE);
            firstFind = true;
            //延时值显示逻辑bug修改  //GC20190720
            isRelatedCount = 0;
        }
    }

    /**
     * @param event 相关的结果   //GC20181119
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ResultOfRelevantEvent event) {
        if (event.isRelated) {
            /*//播放提示音 //GC20190422
            soundSystem.play(soundSystem.SONAR);*/
            //播放提示音改进 //GC20191024
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundSystem.play(soundSystem.SONAR);
                }
            }, 2000);

            //是故障，判断“已发现故障”
            tvNotice.setText(getString(R.string.message_notice_7));
            tvNoticeU.setText(getString(R.string.message_notice_7));

            if (isRelatedCount == 0) {
                //第一次相关 延时值显示
                tvCurrentDelayU.setText(getString(R.string.current) + event.timeDelay + "ms");
                if(lastDelayValue > 0){
                    tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
                }else{
                    tvLastDelayU.setText("");
                }
                //专家界面延时值显示和光标定位 //GC20190218
                tvYanShi.setText(event.timeDelay + "ms");
                lineChartVoice.setScrubLine3(event.cursorPosition);
                //保存故障结果
                lastDelayValue = event.timeDelay;
                cursorPosition = event.cursorPosition;

            } else if (isRelatedCount > 0) {
                //继续相关  延时值显示   //GC20190717
                tvCurrentDelayU.setText(getString(R.string.current) + lastDelayValue + "ms");
                tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
                //专家界面延时值显示和光标定位 //GC20190218
                tvYanShi.setText(lastDelayValue + "ms");
                lineChartVoice.setScrubLine3(cursorPosition);
            }
            //刻度圆圈动画2绘制 //GC20190625
            currentDelayValue = lastDelayValue;
            drawCircle();
            isRelatedCount++;

        }else{
            isRelatedCount = 0;
            //不是故障，判断“磁场触发”
            tvNotice.setText(getString(R.string.triggered));
            tvNoticeU.setText(getString(R.string.triggered));
            //去动画2
            ccvFirstU.setVisibility(View.GONE);
            ccvSecondU.setVisibility(View.GONE);
            //GC20190724
            isDrawCircle = false;
            //画动画1——波纹  正在测试中   ...
            try {
                rlWaveU.addView(v);
            }catch (Exception ignored){
            }
            tvScanU.setVisibility(View.VISIBLE);
            ivScanU.setVisibility(View.VISIBLE);
            //延时值显示
            tvCurrentDelayU.setText("");
            if(lastDelayValue > 0){
                tvLastDelayU.setText(getString(R.string.last) + lastDelayValue + "ms");
            }else{
                tvLastDelayU.setText("");
            }
        }

    }

    /**
     * 根据延时值大小画圆圈   //GC20190625
     */
    public void drawCircle() {
        //去动画1——波纹  正在测试中   ...
        rlWaveU.removeView(v);
        tvScanU.setVisibility(View.GONE);
        ivScanU.setVisibility(View.GONE);
        //画动画2
        if ( (currentDelayValue > 0) && (currentDelayValue <= 1) ){
            //颜色改动之前——灰色"#555555"  "黄色#e1de04"  //GC20190717
            ccvFirstU.updateView("#00ffde", 8, 5);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 2) ){
            ccvFirstU.updateView("#00ffde", 8, 19);
        }else if ( (currentDelayValue > 2) && (currentDelayValue <= 3) ){
            ccvFirstU.updateView("#00ffde", 8, 33);
        }else if ( (currentDelayValue > 3) && (currentDelayValue <= 4) ){
            ccvFirstU.updateView("#00ffde", 8, 47);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 5) ){
            ccvFirstU.updateView("#00ffde", 8, 61);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 6) ){
            ccvFirstU.updateView("#00ffde", 8, 75);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 7) ){
            ccvFirstU.updateView("#00ffde", 8, 89);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 8) ){
            ccvFirstU.updateView("#00ffde", 8, 103);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 9) ){
            ccvFirstU.updateView("#00ffde", 8, 117);
        }else if ( (currentDelayValue > 1) && (currentDelayValue <= 10) ){
            ccvFirstU.updateView("#00ffde", 8, 131);
        }else if (currentDelayValue > 10){
            ccvFirstU.updateView("#00ffde", 8, 145);
        }
        //GC20190724
        isDrawCircle = true;
        ccvFirstU.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.ll_silence, R.id.ll_pause, R.id.ll_memory, R.id.ll_compare, R.id.ll_filter, R.id.ll_assist, R.id.ll_settings,
            R.id.ll_mode, R.id.ll_voice_u, R.id.ll_filter_u, R.id.ll_assist_u, R.id.ll_settings_u, R.id.ll_mode_u})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_silence:
                clickSilence();
                break;
            case R.id.ll_pause:
                clickPause();
                break;
            case R.id.ll_memory:
                clickMemory();
                llMemory.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.bg_expert_btn_select));
                llMemory.setClickable(false);
                handle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        llMemory.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_btn_expert));
                        llMemory.setClickable(true);
                    }
                }, 400);
                break;
            case R.id.ll_compare:
                clickCompare();
                if (!isCom) {
                    llCompare.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_btn_expert));
                } else {
                    llCompare.setBackground(ContextCompat.getDrawable(MainActivity.this,R.drawable.bg_expert_btn_select));
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
                clickAssist();
                break;
            case R.id.ll_settings_u:
                clickSetting();
                break;
            case R.id.ll_mode_u:
                llUserUI.setVisibility(View.INVISIBLE);
                llUserUI.setEnabled(false);
                llVoiceU.setEnabled(false);
                llAssistU.setEnabled(false);
                llSettingsU.setEnabled(false);
                llFilterU.setEnabled(false);
                llMainUI.setVisibility(View.VISIBLE);
                llMainUI.setEnabled(true);
                break;
            default:
                break;
        }
    }
    //点击静音
    public void clickSilence() {
        //GC20191217 仪器静音状态下按键命令处理
        if (mute){
            //GC20191221
            clickTime++;
            Log.e("VALUE","" + clickTime);
            if(clickTime > 3) {
                sendTouchOffCommand();
                clickTime = 0;
                Log.e("VALUE","end" + clickTime);
            } else {
                sendResetCommand();
            }
            mute = false;
            //字体图标变蓝
            ilSilence.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ilSilenceU.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ivSilence.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            ivSilenceU.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue3));
            if (isSilence) {
                streamVolumeNow = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
                ivSilence.setImageResource(R.drawable.ic_close_voice);
                ivSilenceU.setImageResource(R.drawable.ic_close_voice);
            } else {
                ivSilence.setImageResource(R.drawable.ic_open_voice);
                ivSilenceU.setImageResource(R.drawable.ic_open_voice);
            }
        } else {
            int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (isSilence) {
                if (streamVolumeNow == 0) {
                    streamVolumeNow = streamMaxVolume / 2;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolumeNow, AudioManager.FLAG_PLAY_SOUND);
                ivSilence.setImageResource(R.drawable.ic_open_voice);
                ivSilenceU.setImageResource(R.drawable.ic_open_voice);
            } else {
                streamVolumeNow = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
                ivSilence.setImageResource(R.drawable.ic_close_voice);
                ivSilenceU.setImageResource(R.drawable.ic_close_voice);
            }
            isSilence = !isSilence;
        }
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
    //点击协助
    public void clickAssist() {
        Intent intent = new Intent(this, AssistListActivity.class);
        startActivity(intent);
    }
    //点击设置
    public void clickSetting() {
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }

    /**
     * 界面无缝切换思路：
     * 将用户界面和专家界面用布局封装，放到一个activity中，
     * 点击模式切换时，控制布局的隐藏和显示。
     */
    public void clickMode() {
        llUserUI.setVisibility(View.VISIBLE);
        llUserUI.setEnabled(true);
        llVoiceU.setEnabled(true);
        llAssistU.setEnabled(true);
        llSettingsU.setEnabled(true);
        llFilterU.setEnabled(true);

        llMainUI.setEnabled(false);
        llMainUI.setVisibility(View.INVISIBLE);

    }

    /**
     * @param keyCode   物理按键内容
     * @param event 点击事件
     * @return  无按键返回
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            isExit = true;
            //关闭并释放资源
            mAudioTrack.release();
            finish();
            //按键返回bug  //GC20190407 蓝牙重连功能优化
            System.exit(0);
            MyApplication.getInstances().getBluetoothAdapter().disable();
            return true;

        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handle.postDelayed(new Runnable() {
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
            handle.postDelayed(new Runnable() {
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

        }
        return false;

    }

    public void checkVoice() {
        //GC20191217 非仪器静音状态下才去检测音量
        if (!mute) {
            int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (streamVolume <= 0) {
                isSilence = true;
                ivSilence.setImageResource(R.drawable.ic_close_voice);
                ivSilenceU.setImageResource(R.drawable.ic_close_voice);
            } else {
                isSilence = false;
                ivSilence.setImageResource(R.drawable.ic_open_voice);
                ivSilenceU.setImageResource(R.drawable.ic_open_voice);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            instance = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

}
