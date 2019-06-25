package com.kehui.www.testapp.view;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.base.BaseActivity;
import com.kehui.www.testapp.event.AcousticMagneticDelay2;
import com.kehui.www.testapp.event.HandleReceiveDataEvent;
import com.kehui.www.testapp.event.OperationGuideEvent;
import com.kehui.www.testapp.event.SendDataFinishEvent;
import com.kehui.www.testapp.event.UINoticeEvent;
import com.kehui.www.testapp.ui.PercentLinearLayout;
import com.kehui.www.testapp.ui.TempControlView;
import com.kehui.www.testapp.ui.WaterWaveView;
import com.kehui.www.testapp.util.PrefUtils;
import com.kehui.www.testapp.util.ShowProgressDialog;
import com.kehui.www.testapp.util.Utils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 用户模式页面
 */
public class UserMainActivity extends BaseActivity {
    @BindView(R.id.magnetic_field_gain_control)
    TempControlView magneticFieldGainControl;
    @BindView(R.id.iv_magnetic_field_gain)
    ImageView ivMagneticFieldGain;
    @BindView(R.id.voice_gain_control)
    TempControlView voiceGainControl;
    @BindView(R.id.iv_voice_gain)
    ImageView ivVoiceGain;
    @BindView(R.id.tv_notice)
    TextView tvNotice;
    @BindView(R.id.iv_position)
    ImageView ivPosition;
    @BindView(R.id.ll_voice)
    PercentLinearLayout llVoice;
    @BindView(R.id.ll_filter)
    PercentLinearLayout llFilter;
    @BindView(R.id.ll_assist)
    PercentLinearLayout llAssist;
    @BindView(R.id.ll_settings)
    PercentLinearLayout llSettings;
    @BindView(R.id.iv_mode)
    ImageView ivMode;
    @BindView(R.id.tv_mode)
    TextView tvMode;
    @BindView(R.id.ll_mode)
    LinearLayout llMode;
    @BindView(R.id.iv_silence)
    ImageView ivSilence;
    @BindView(R.id.rl_wave)
    RelativeLayout rlWave;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.ccv_first)
    CustomCircleView ccvFirst;
    @BindView(R.id.ccv_second)
    CustomCircleView ccvSecond;
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.tv_last_delay)
    TextView tvLastDelay;
    @BindView(R.id.tv_current_delay)
    TextView tvCurrentDelay;
    @BindView(R.id.tv_min_delay_value)
    TextView tvMinDelayValue;
    @BindView(R.id.ll_min_delay)
    LinearLayout llMinDelay;

    public static UserMainActivity instance;
    private ValueAnimator valueAnimator;    //GN 动画绘制1
    private int[] scoreText = {R.drawable.ic_wait_empty, R.drawable.ic_wait_1, R.drawable.ic_wait_2, R.drawable.ic_wait_3};
    private WaterWaveView v;    //水波纹动画
    private ViewGroup.MarginLayoutParams layoutParams;  //GN 探头位置
    private ValueAnimator valueAnimator2;   //GN 动画绘制1
    private double lastDelayValue = -1; //上次的声磁延时值
    private double minDelayValue = 43.625;  //GC20181115 历史最小延时值（最大349*0.125=43.625ms）
    private Dialog dialog;
    public int currentPosition;     //GN 当前增益进度条的位置
    private boolean firstFind = true;   //GC20181119
    private int positionState = -1;  //GC20181119 故障圈的大小状态
    private int isRelatedCount = 0; //GC20181119 相关次数计数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        ButterKnife.bind(this);
//        EventBus.getDefault().register(this);
        instance = this;
        initView();
//        showProgressDialog();

    }
    //GN 初始化
    private void initView() {
        checkVoice();   //GC20181117 系统音量监听
        //GN 动画绘制1（正在测试中）
        ccvFirst.setVisibility(View.GONE);
        ccvSecond.setVisibility(View.GONE);
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(0, 4).setDuration(1000);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int i = (int) animation.getAnimatedValue();
                    ivScan.setImageResource(scoreText[i % scoreText.length]);
                }
            });
        }
        valueAnimator.start();
        v = new WaterWaveView(UserMainActivity.this);
        v.setFillWaveSourceShapeRadius(10);
        rlWave.addView(v);
        //设置探头位置
        layoutParams = new ViewGroup.MarginLayoutParams(ivPosition.getLayoutParams());
        //设置增益显示
//        magneticFieldGainControl.setAngleRate(0.2);
        magneticFieldGainControl.setArcColor("#a03225");
        magneticFieldGainControl.setDialColor1("#a03225");
        magneticFieldGainControl.setDialColor2("#01eeff");
        magneticFieldGainControl.setValueColor("#d0210e");
        magneticFieldGainControl.setCurrentValueColor("#a03225");
        magneticFieldGainControl.setTitle(getString(R.string.gain));
        magneticFieldGainControl.setTemp(0, 100, 70);   //GC20181102 magneticFieldGainControl.setTemp(0, 100, 63);
        magneticFieldGainControl.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {
                Constant.magneticFieldGain = temp;
                magneticFieldGainControl.setEnabled(false);
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
        voiceGainControl.setArcColor("#026b02");    //弧度颜色
        voiceGainControl.setDialColor1("#026b02");  //未选中刻度颜色
        voiceGainControl.setDialColor2("#01eeff");  //选中刻度颜色
        voiceGainControl.setValueColor("#00ec03");  //最大最小值颜色
        voiceGainControl.setCurrentValueColor("#026b02");   //当前设置值颜色
        voiceGainControl.setTitle(getString(R.string.gain));
        voiceGainControl.setTemp(0, 100, 70);   //GC20181102 voiceGainControl.setTemp(0, 100, 45);
        voiceGainControl.setOnTempChangeListener(new TempControlView.OnTempChangeListener() {
            @Override
            public void change(int temp) {
                Constant.voiceGain = temp;
                voiceGainControl.setEnabled(false);
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

    //GN 发送控制命令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SendDataFinishEvent event) {
        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (seekbarType == 1) {
                    cichangSeekbarInts[1] = cichangSeekbarInts[0];
                } else if (seekbarType == 2) {
                    shengyinSeekbarInts[1] = shengyinSeekbarInts[0];
                }
                seekbarType = 0;
                llFilter.setClickable(true);
                voiceGainControl.setEnabled(true);
                magneticFieldGainControl.setEnabled(true);
                hasSendMessage = false;
            }
        }, 500);
    }
    //GC20181118 接收控制命令
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HandleReceiveDataEvent event) {
        if (seekbarType == 1) {
            cichangSeekbarInts[1] = cichangSeekbarInts[0];;
        } else if (seekbarType == 2) {
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
                }
            }, 500);
            Toast.makeText(UserMainActivity.this, getResources().getString(R.string
                    .The_sending_data_failed_and_was_being_resent), Toast.LENGTH_SHORT).show();
        }
        if (event.status == BLUETOOTH_DISCONNECTED) {
            toastDisconnected = true;
            if (!isExit) {
                new SweetAlertDialog(UserMainActivity.this, SweetAlertDialog.WARNING_TYPE)
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
            layoutParams.setMargins(Utils.dp2px(UserMainActivity.this, 100), Utils.dp2px(UserMainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPosition.setLayoutParams(params);
        }
        if (event.status == WHAT_POSITION_LEFT) {
            layoutParams.setMargins(Utils.dp2px(UserMainActivity.this, 40), Utils.dp2px(UserMainActivity.this, 50), 0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(layoutParams);
            ivPosition.setLayoutParams(params);
        }
        if (event.status == WHAT_REFRESH) {
            handleGainView(maxShengYin, ivVoiceGain, 1);    //GC20181113 上下语句顺序调整，否则影响进度条回落功能
            handleGainView(maxCiChang, ivMagneticFieldGain, 0);
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
            changeVoiceGainView(ivVoiceGain, currentPosition);
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
                changeMagneticFieldGainView(ivMagneticFieldGain, position);
            } else if (msg.what == 5) {
                changeVoiceGainView(ivVoiceGain, position);     //GC20181121 添加声音回落
            }
            return false;
        }
    });
    //信息提示框内容
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OperationGuideEvent event) {
        //GN 闪烁动画关闭
        if (valueAnimator2 != null) {
            valueAnimator2.end();
        }
        if (event.isMalfunction) {
            if (firstFind) {
                //GN 去动画1
                rlWave.removeView(v);               //GN 波纹
                tvScan.setVisibility(View.GONE);    //GN 正在测试中
                ivScan.setVisibility(View.GONE);    //GN ...
                //GN “已发现故障”中圈 状态2
                tvNotice.setText(getString(R.string.message_notice_7));
                if(lastDelayValue > 0){     //GN 有过相关后的声磁延时值
                    tvLastDelay.setText(lastDelayValue + "ms");
                }else{
                    tvLastDelay.setText("");
                }
                tvCurrentDelay.setText("");
                ccvSecond.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                ccvFirst.setVisibility(View.GONE);
                ccvSecond.setVisibility(View.VISIBLE);
                positionState = 3;
                firstFind = false;
            }

        } else {
            //GN 去动画2
            ccvFirst.setVisibility(View.GONE);
            ccvSecond.setVisibility(View.GONE);
            //GN “未发现故障”波纹 状态1
            tvNotice.setText(getString(R.string.message_notice_6));
            if(lastDelayValue > 0){     //GN 有过相关后的声磁延时值
                tvLastDelay.setText(lastDelayValue + "ms");
            }else{
                tvLastDelay.setText("");
            }
            tvCurrentDelay.setText("");
            //GN 显示“正在测试中”动画
            rlWave.addView(v);                  //GN 波纹
            tvScan.setVisibility(View.VISIBLE); //GN 正在测试中
            ivScan.setVisibility(View.VISIBLE); //GN ...

            firstFind = true;   //GC20181119
        }

    }
    //GC20181119 相关后进行效果显示，至少第二次连续判断为是故障点
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AcousticMagneticDelay2 event) {
        if (event.isRelated) {
            if (isRelatedCount == 0) {   //GN 第一次相关
                if(lastDelayValue < 0){
                    //GN 历史第一次发现故障 中圈闪烁 状态3
                    tvNotice.setText(getString(R.string.message_notice_7));
                    tvLastDelay.setText("");
                    tvCurrentDelay.setText(event.delayValue + "ms");
                    ccvFirst.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                    ccvSecond.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                    if (valueAnimator2 == null) {
                        valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                        valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int i = (int) animation.getAnimatedValue();
                                if (i == 0) {
                                    ccvSecond.setVisibility(View.VISIBLE);
                                    ccvFirst.setVisibility(View.GONE);
                                } else if (i == 1) {
                                    ccvSecond.setVisibility(View.GONE);
                                    ccvFirst.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    valueAnimator2.start();

                } else if (lastDelayValue > 0) {  //GN 有过相关后的声磁延时值，比较大小
                    if (lastDelayValue < (event.delayValue - 0.375)) {  //GC20181204 3个点的波动范围
                        //GN 远离故障点 大圈 状态4
                        tvNotice.setText(getString(R.string.message_notice_9));
                        tvLastDelay.setText(lastDelayValue + "ms");
                        tvCurrentDelay.setText(event.delayValue + "ms");
                        ccvFirst.updateView("#555555", 5, 40);      //灰色 细圈 小尺寸
                        ccvSecond.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                        ccvFirst.setVisibility(View.VISIBLE);
                        ccvSecond.setVisibility(View.VISIBLE);
                        positionState = 1;

                    } else if (lastDelayValue > (event.delayValue + 0.375)) {   //GC20181204 3个点的波动范围
                        //GN 接近故障点 小圈 状态6
                        tvNotice.setText(getString(R.string.message_notice_8));
                        tvLastDelay.setText(lastDelayValue + "ms");
                        tvCurrentDelay.setText(event.delayValue + "ms");
                        ccvFirst.updateView("#555555", 5, 138);     //灰色 细圈 大尺寸
                        ccvSecond.updateView("#e1de04", 20, 40);    //黄色 粗圈 小尺寸
                        ccvFirst.setVisibility(View.VISIBLE);
                        ccvSecond.setVisibility(View.VISIBLE);
                        positionState = 2;

                    } else if ( (lastDelayValue <= (event.delayValue + 0.375)) && (lastDelayValue >= (event.delayValue - 0.375)) ) {    //GC20181204 3个点的波动范围
                        tvNotice.setText(getString(R.string.message_notice_7));
                        tvLastDelay.setText(event.delayValue + "ms");   //GC20181122 经过相关为否或不是故障之后再次相关，且前后差值不大，更新为最新值
                        tvCurrentDelay.setText(event.delayValue + "ms");
                        if(positionState == 1) {            //GN  后续发现故障 大圈闪烁 状态5
                            ccvFirst.updateView("#555555", 20, 138);    //灰色 粗圈 大尺寸
                            ccvSecond.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                        }else if(positionState == 2) {      //GN  后续发现故障 小圈闪烁 状态7
                            ccvFirst.updateView("#555555", 20, 40);    //灰色 粗圈 小尺寸
                            ccvSecond.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
                        }else if(positionState == 3) {      //GN  后续发现故障 中圈闪烁 状态3
                            ccvFirst.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                            ccvSecond.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                        }
                        if (valueAnimator2 == null) {
                            valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                            valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                            valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int i = (int) animation.getAnimatedValue();
                                    if (i == 0) {
                                        ccvSecond.setVisibility(View.VISIBLE);
                                        ccvFirst.setVisibility(View.GONE);
                                    } else if (i == 1) {
                                        ccvSecond.setVisibility(View.GONE);
                                        ccvFirst.setVisibility(View.VISIBLE);
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
                llMinDelay.setVisibility(View.VISIBLE);
                tvMinDelayValue.setText(minDelayValue + "ms");  //GN 显示历史最小声磁延时值

            } else if (isRelatedCount > 0) {    //从第二次相关开始后继续相关
                tvNotice.setText(getString(R.string.message_notice_7));
                tvLastDelay.setText(lastDelayValue + "ms");
                tvCurrentDelay.setText(lastDelayValue + "ms");
                if(positionState == 1) {                //GN  后续发现故障 大圈闪烁 状态5
                    ccvFirst.updateView("#555555", 20, 138);    //灰色 粗圈 大尺寸
                    ccvSecond.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
                }else if(positionState == 2) {          //GN  后续发现故障 小圈闪烁 状态7
                    ccvFirst.updateView("#555555", 20, 40);    //灰色 粗圈 小尺寸
                    ccvSecond.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
                }else if(positionState == 3) {          //GN  后续发现故障 中圈闪烁 状态3
                    ccvFirst.updateView("#555555", 20, 89);    //灰色 粗圈 中尺寸
                    ccvSecond.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
                }
                if (valueAnimator2 == null) {
                    valueAnimator2 = ValueAnimator.ofInt(0, 2).setDuration(1000);
                    valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
                    valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int i = (int) animation.getAnimatedValue();
                            if (i == 0) {
                                ccvSecond.setVisibility(View.VISIBLE);
                                ccvFirst.setVisibility(View.GONE);
                            } else if (i == 1) {
                                ccvSecond.setVisibility(View.GONE);
                                ccvFirst.setVisibility(View.VISIBLE);
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
                tvLastDelay.setText(lastDelayValue + "ms");
            }else{
                tvLastDelay.setText("");
            }
            tvCurrentDelay.setText("");
            if(positionState == 1) {                        //GN  相关为否 大圈 状态8
                ccvSecond.updateView("#e1de04", 20, 138);   //黄色 粗圈 大尺寸
            }else if(positionState == 2) {                  //GN  相关为否 小圈 状态9
                ccvSecond.updateView("#e1de04", 20, 40);   //黄色 粗圈 小尺寸
            }else if(positionState == 3) {                  //GN  相关为否 中圈 状态10
                ccvSecond.updateView("#e1de04", 20, 89);   //黄色 粗圈 中尺寸
            }
            ccvFirst.setVisibility(View.GONE);
            ccvSecond.setVisibility(View.VISIBLE);

        }

    }

    @OnClick({R.id.ll_voice, R.id.ll_filter, R.id.ll_assist, R.id.ll_settings, R.id.ll_mode})
    public void onViewClicked(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.ll_voice:
                clickSilence();
                break;
            case R.id.ll_filter:
                clickFilter();
                break;
            case R.id.ll_assist:
                intent.setClass(UserMainActivity.this, AssistListActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_settings:
                intent.setClass(UserMainActivity.this, SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_mode:
                PrefUtils.setString(UserMainActivity.this, AppConfig.CURRENT_MODE, "expert");
                PrefUtils.setString(UserMainActivity.this, AppConfig.CLICK_MODE, "clicked");   //GC20181116
                intent.setAction("restartapp");
                sendBroadcast(intent);
//                finish();
                break;
        }
    }
    //点击静音按钮执行的方法
    public void clickSilence() {
        int streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
     /*   System.out.println("streamMaxVolume:" + streamMaxVolume);
        System.out.println("streamVolume:" + streamVolume);*/
        if (isSilence) {
            if (streamVolumenow == 0) {
                streamVolumenow = streamMaxVolume / 2;
            }
            //audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, streamVolumenow, AudioManager
                    .FLAG_PLAY_SOUND);
            ivSilence.setImageResource(R.drawable.ic_open_voice);
        } else {
            streamVolumenow = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            //audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager
                    .FLAG_PLAY_SOUND);
            ivSilence.setImageResource(R.drawable.ic_close_voice);
        }
        isSilence = !isSilence;
        //AplicationUtil.makeToast(this, "clickSilence");
    }
    //点击滤波
    private void clickFilter() {
        llFilter.setClickable(false);
        showFilterDialog(llFilter);

    }

    //GC20181117 静音按钮状态监听
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
            checkVoice();
            super.onKeyDown(keyCode, event);
            checkVoice();
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
    //显示等待的弹窗
    public void showProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = ShowProgressDialog.createLoadingDialog(UserMainActivity.this);
        dialog.show();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}

/*更改记录*/
//GC20181102 增益初始值修改
//GC20181113 增益进度条显示bug修改
//GC20181115 改进发现过声音故障时，再次未发现故障时的声磁延时动画效果
//GC20181116 模式切换时无需点击操作
//GC20181117 系统音量监听
//GC20181119 增加相关后提示效果逻辑判断重写
//GC20181121 添加声音回落
//GC20181122 现场测试bug修正
//GC20181204 3个点的波动范围