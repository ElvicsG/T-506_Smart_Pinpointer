package com.kehui.www.testapp.view;

import android.app.Dialog;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.kehui.www.testapp.util.PrefUtils;
import com.kehui.www.testapp.util.ShowProgressDialog;

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
 * 专家模式页面
 */
public class MainActivity extends BaseActivity {
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

    public static MainActivity instance;
    private CustomDialog customDialog;
    private Dialog dialog;
    private double lastDelayValue = -1;         //上次的声磁延时值
    private int positionState = -1;  //故障点状态远离还是接近
    private int isRelatedCount = 0; //GC20181119 相关次数计数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager
                .LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
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

    }
    //设置seekBar的回掉S
    private void setSeekBar() {
        //磁场seekbar数值改变执行的回掉方法
        seekbarCichang.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCichangValue.setText(progress + "%");
                Constant.magneticFieldGain = progress;
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

        seekbarShengyin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvShengyinValue.setText(progress + "%");
                Constant.voiceGain = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekbarShengyin.setEnabled(false);
                shengyinSeekbarInts[0] = shengyinSeekbarInts[1];
                shengyinSeekbarInts[1] = seekbarShengyin.getProgress();
                Log.e("打印-设置cichang", seekbarShengyin.getProgress() + "");
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
                    //seekbarCichang.setProgress(s2b(cichangSeekbarInts[0]));   //GC20180609 修改下发时赋值的是1
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
        }
        if (event.status == WHAT_POSITION_LEFT) {
            tvPosition.setText(getResources().getString(R.string.left));
        }
        if (event.status == WHAT_LIGHT) {
            ivSynchronizeStatus.setImageResource(R.drawable.ic_synchronize_status_33);
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
        }

    }
    //信息提示框内容
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(OperationGuideEvent event) {
        if (event.isMalfunction) {
            tvNotice.setText(getString(R.string.message_notice_7));
        } else {
            tvNotice.setText(getString(R.string.message_notice_6));
        }
    }
    //GC20181119 信息框提示2
    @Subscribe(threadMode = ThreadMode.MAIN)    //GC20181201 去掉波形界面自动定位和提示
    public void onEventMainThread(AcousticMagneticDelay2 event) {
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
    @Subscribe(threadMode = ThreadMode.MAIN)    //GC20181201 去掉波形界面自动定位和提示
    public void onEventMainThread(AcousticMagneticDelayEvent event) {
        /*linechartShengyin.setScrubLine3(event.position);
        tvYanShi.setText((event.delayValue) + "ms");*/
    }

    @OnClick({R.id.ll_silence, R.id.ll_pause, R.id.ll_memory, R.id.ll_compare, R.id.ll_filter, R.id.ll_assist, R.id.ll_settings, R.id.ll_mode})
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
        Intent intent = new Intent();
        PrefUtils.setString(MainActivity.this, AppConfig.CURRENT_MODE, "user");
        PrefUtils.setString(MainActivity.this, AppConfig.CLICK_MODE, "clicked");   //GC20181116
        intent.setAction("restartapp");
//        intent.putExtra("type", 1);
//        intent.putExtra("name", "user");
        sendBroadcast(intent);
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
//GC20180609 发送控制命令失败调整
//GC20181115 改进“已发现故障”提示
//GC20181201 去掉波形界面自动定位和提示