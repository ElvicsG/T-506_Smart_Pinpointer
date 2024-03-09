package com.kehui.www.testapp.base;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.adpter.MyChartAdapterBase;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.bean.PackageBean;
import com.kehui.www.testapp.event.ResultOfRelevantEvent;
import com.kehui.www.testapp.event.SendCommandNotRespondEvent;
import com.kehui.www.testapp.event.ResultOfSvmEvent;
import com.kehui.www.testapp.event.SendCommandFinishEvent;
import com.kehui.www.testapp.event.RestartGetStreamEvent;
import com.kehui.www.testapp.event.UiHandleEvent;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.ui.CustomDeviceListDialog;
import com.kehui.www.testapp.util.SoundUtils;
import com.kehui.www.testapp.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

/**
 * 专家模式和用户模式的基类
 * @author Gong
 * @date 2019/07/17
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    /**
     * 声音播放部分
     */
    public AudioManager audioManager;
    public AudioTrack mAudioTrack;
    public SoundUtils soundSystem;
    public int streamVolumeNow;
    public boolean isSilence;
    public boolean isExit;
    /**
     * 蓝牙相关部分
     */
    private BluetoothSocket bluetoothSocket;
    private BluetoothSocket reconnectSocket = null;
    /**
     * 获得本设备的蓝牙适配器实例      返回值：如果设备具备蓝牙功能，返回BluetoothAdapter 实例；否则，返回null对象
     */
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private boolean needReconnect;
    /**
     * 下发蓝牙数据（控制命令或MAC地址）
     */
    public byte[] sendCommand;
    public long[] crcTable;
    public boolean hasSentInitCommand;
    public boolean hasSentCommand;
    public byte[] address;
    public int[] sendAddress = new int[3];
    /**
     * 接收蓝牙数据
     */
    public InputStream inputStream;
    public int[] stream;
    public int streamLength;
    public int emptyCount;
    public int[] blueStream;
    public int blueStreamLen;
    public boolean hasGotStream;
    public boolean doingStream;
    public int[] streamLeft;
    public int leftLen;
    public boolean hasLeft;
    /**
     * 解码packageBean需要的解析常量数组
     */
    public int[] indexTable = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
    public int[] stepSizeTable = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371,
            408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493,
            10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767,
    };
    /**
     * 是否开始获取声音包的标志
     */
    public boolean isVoicePack;
    public boolean isVoicePackStart; //GC20231020
    public int triggeredPosition;
    public int voicePackCount;
    public int voicePackCount2;     //GC20231020
    public int voicePackCount3;     //GC20231023
    public int[] voiceDraw; //GT20240308
    public int[] compareDraw;
    public int[] magneticArray;
    public int[] magneticArray2;    //GC20240222    //水平线圈磁场数据缓存数组
    public int[] magTemp1;    //GT20240228 用于调试，竖直线圈磁场数据临时缓存数组
    public int[] magTemp2;    //GT20240228 用于调试，水平线圈磁场数据临时缓存数组
    public int[] magneticDraw;
    public boolean isDraw;
    public boolean isAmplify;   //是否数字放大处理状态    //GC20231019
    public boolean isPlay;      //是否播放的状态    //GC20231023

    /**
     * 声音特征训练
     */
//    private double[] readFeature = new double[2000];  //4*500
    private double[] readFeature = new double[6000];    //特征向量缓存数组增大    //GC20231120
    private boolean svmTrainThread;
    private svm_model model;
    /**
     * 声音智能识别
     */
    public int maxVoice;
    public int maxVoicePlay;
    public int maxMagnetic;
    public int maxMagnetic2; //水平线圈磁场数据的最大值  //GC20240226
    private int[] tempVoice;
    private int[] tempVoiceTrigger;         //用于找极小值    //GC20231022
    private int[] tempVoiceBeforeTrigger;   //用于找极小值    //GC20231022
    private int[] tempResults = new int[400];       //GC20231020
    private int[] tempResultsMax = new int[400];    //GC20231020
    private int[] svmData;
    private int[] autoLocate;
    private double[] feature;
    private double[] normalization;
    private double[] normalization2;
    public int svmPredictCount;
    public boolean firstFind;
    private double p;
    public int isRelatedCount;
    private double timeDelay;
    private int position;
    public boolean isDrawCircle;
    /**
     * sparkView布局部分
     */
    public MyChartAdapterBase myChartAdapterVoice;
    public MyChartAdapterBase myChartAdapterMagnetic;
    public boolean isClickMem;
    public boolean isCom;
    /**
     * 声音和磁场增益控制
     */
    public int seekBarType;
    public int[] seekBarVoiceInt;
    public int[] seekBarMagneticInt;
    /**
     * 自定义滤波方式的对话框
     */
    private CustomDialog customDialog;
    public int clickTongNum;
    public int currentFilter;
    public double filter;
    /**
     * 自定义扫描蓝牙设备的对话框
     */
    private CustomDeviceListDialog customDeviceListDialog;
    /**
     * 触摸静音功能
     */
    public int clickTime;
    public boolean mute;
    /**
     * 磁场增益相关
     */
    public int gainState;           //增益大小的状态    //GC20240226
    public boolean suitableGain;    //增益是否合适的判断  //GC20240226
    public int gainM;   //磁场增益阶数    //GC20240227
    public boolean isChangeMag; //GT20240228 用于调试，磁场数据显示来源判断
    public boolean isTooSmall;  //GC20240304 增益过小状态记录
    public boolean isRightOld;  //GC20240304 上一次触发方向记录
    public double magIntensity = 0; //GC20240304 上一次磁场强度记录
    /**
     * 全局的handler对象用来执行UI更新
     */
    public static final int SEND_SUCCESS    = 1;
    public static final int SEND_ERROR      = 2;
    public static final int POSITION_RIGHT  = 4;
    public static final int POSITION_LEFT   = 5;
    public static final int LIGHT_UP        = 6;
    public static final int TRIGGERED       = 7;
    public static final int WHAT_REFRESH    = 8;
    public static final int LINK_LOST       = 9;
    public static final int LINK_CONNECT    = 10;
    public static final int MUTE_STATE      = 11;
    public static final int NO_MUTE_STATE   = 12;
    public static final int HEADPHONES_CONNECT      = 13;
    public static final int HEADPHONES_DISCONNECT   = 14;
    public static final int HEADPHONES_DISCONNECT_STATE   = 15;

    @SuppressLint("HandlerLeak")
    public Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EventBus.getDefault().post(new UiHandleEvent(msg.what));
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //GC20200519 hideBottomUIMenu();
        initData();
        setAudioTrack();
        getFeatureData();
        //启动训练声音特征的线程
        svmTrain.start();
        getCrcTable();
        //获取蓝牙数据流
        startGetStream();
        //启动处理蓝牙数据流的线程
        doStream.start();
        //注册使用EventBus
        EventBus.getDefault().register(this);
    }

    /**
     * 所需数据初始化
     */
    private void initData() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        sendCommand = new byte[7];
        //缓存的蓝牙数据
        stream = new int[1024000];
        streamLength = 0;
        //需要处理的蓝牙数据
        blueStream = new int[1024000];
        blueStreamLen = 0;
        //将蓝牙数据分包后的剩余数据
        streamLeft = new int[59];
        leftLen = 0;
        //触发时刻数据点所在的位置
        triggeredPosition = 0;
        voicePackCount = 0;
        voicePackCount2 = 0;    //GC20231020
        voicePackCount3 = 0;    //GC20231023
        voiceDraw = new int[400];
        magneticArray = new int[400];
        magneticArray2 = new int[400];  //GC20240222    //初始化
        for (int i = 0; i < 400; i++) {
            magneticArray2[i] = 2048;  //初始化为2048  避免影响自动增益 //GT20240309
        }
        magTemp1 = new int[400];  //GT20240228 用于调试，初始化
        magTemp2 = new int[400];  //GT20240228 用于调试，初始化
        magneticDraw = new int[400];
        compareDraw = new int[400];
        isDraw = true;
        isAmplify = false;  //是否数字放大处理状态    //GC20231019
        isPlay = false;     //是否播放的状态           //GC20231023
        tempVoice = new int[900];
        tempVoiceTrigger = new int[100];        //用于找极小值    //GC20231022
        tempVoiceBeforeTrigger = new int[100];  //用于找极小值    //GC20231022
        svmData = new int[800];
        autoLocate = new int[800];
        feature = new double[4];
        //归一化的声音数据
        normalization = new double[800];
        normalization2 = new double[800];
        //连续预测为故障声的次数
        svmPredictCount = 0;
        //svm是否第一次发现故障声
        firstFind = true;
        p = 0;
        //连续相关的次数
        isRelatedCount = 0;
        //初始化seekBar控制情况
        seekBarType = 0;
        seekBarVoiceInt = new int[]{22, 22};
        seekBarMagneticInt = new int[]{22, 22};
        //滤波模式默认为全通
        clickTongNum = 0;
        //GC20191221 触摸静音功能
        int clickTime = 0;
        gainState = 0;   //GC20240226   增益大小初始状态为0

    }

    /**
     * 设置音频播放工具
     */
    public void setAudioTrack() {
        //内部的音频缓冲区的大小
        int minBufferSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);  //GC20230601
        //音频设置——指定流的类型、音频数据的采样频率、输出声道、音频数据块、bufferSizeInBytes、模式类型
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,  8000,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,   //ENCODING_PCM_8BIT    ENCODING_PCM_16BIT   ENCODING_PCM_FLOAT
                minBufferSize, AudioTrack.MODE_STREAM);
        // 播放模式有MODE_STATIC和MODE_STREAM两种分类：
        // STREAM方式表示由用户通过write方式把数据一次一次得写到audioTrack中，
        // 这种方式的缺点就是JAVA层和Native层不断地交换数据，效率损失较大；
        // 而STATIC方式表示是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audioTrack，
        // 后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
        // 这种方法对于铃声等体积较小的文件比较合适。
        mAudioTrack.play();
    }

    /**
     * 从assets文件夹中获取声音特征数据     //GC20180504 注意读取文本文件的编码格式为ANSI
     */
    private void getFeatureData() {
//        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "feature.txt");
//        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "01.txt");    //GC20231120    新特征值添加
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "02.txt");    //GC20231206    新特征值添加
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        try {
            //读取bis流中的下一个字节
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");
            for (int i = 0; i < split.length; i++) {
                try {
                    readFeature[i] = Double.parseDouble(split[i]);
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 训练声音特征的线程    //GC20180504
     */
    Thread svmTrain = new Thread(new Runnable() {
        @Override
        public void run() {
            if (!svmTrainThread) {
                voiceSvmTrain(readFeature);
                svmTrainThread = true;
            }
        }
    });

    /**
     * 训练生成model
     * @param readFeature   读取的声音特征
     */
    private void voiceSvmTrain(double[] readFeature) {  //支持向量机参数以及训练调整位置   //GC20231026
        svm_problem sp = new svm_problem();
//        svm_node[][] x = new svm_node[500][4];  //将特征值载入，共500组
//        svm_node[][] x = new svm_node[554][4];  //将特征值载入，共554组   //GC20231120    新特征值添加
        svm_node[][] x = new svm_node[609][4];  //将特征值载入，共609组   //GC20231206    新特征值添加
        int k = 0;
//        for (int i = 0; i < 500; i++) {
//        for (int i = 0; i < 554; i++) { //GC20231120    新特征值添加
        for (int i = 0; i < 609; i++) { //GC20231206    新特征值添加
            for (int j = 0; j < 4; j++, k++) {
                x[i][j] = new svm_node();
                x[i][j].index = j + 1;
                x[i][j].value = readFeature[k];
            }
        }
//        double[] labels = new double[500];
//        for (int i = 0; i < 500; i++) {   //前223是故障声，后面不是故障声
//            if (i <= 222) {
//        double[] labels = new double[554];  //GC20231120    新特征值添加
//        for (int i = 0; i < 554; i++) { //前277组特征值是故障声，后面不是故障声
//            if (i <= 276) {
        double[] labels = new double[609];  //GC20231206    新特征值添加
        for (int i = 0; i < 609; i++) { //前332组特征值是故障声，后面不是故障声
            if (i <= 331) {
                labels[i] = 1;
            } else {
                labels[i] = -1;
            }
        }
        //训练的数据
        sp.x = x;
        //训练数据的类别
        sp.y = labels;
        //训练数据的个数
//        sp.l = 500;
//        sp.l = 554; //GC20231120    新特征值添加
        sp.l = 609; //GC20231206    新特征值添加

        svm_parameter prm = new svm_parameter();
        //SVM的类型——分类问题(包括C-SVC、n-SVC)、回归问题(包括e-SVR、n-SVR)以及分布估计(one-class-SVM )
        prm.svm_type = svm_parameter.C_SVC;
        //核函数——LINEAR：线性核函数、POLY:多项式核函数、RBF:径向机核函数、SIGMOID: 神经元的非线性作用函数核函数
        prm.kernel_type = svm_parameter.RBF;
        //prm.degree = 3; //for poly (默认3)
        //prm.coef0 = 0;  //for poly/sigmoid (默认0)
        //for poly/rbf/sigmoid (默认1/k) 默认大小 7.9
        prm.gamma = 0.5;
        //训练所需的内存 inMB
        prm.cache_size = 1024;
        //stopping criteria 设置允许的终止判据(默认0.001)
        prm.eps = 1e-3;
        //for C_SVC, EPSILON_SVR and NU_SVR 惩罚因子(损失函数)(默认1) 0.1 全部   10 270
        prm.C = 1000;
        //prm.nr_weight = 0;           //for C_SVC 设置第几类的参数C为weight*C(C-SVC中的C)
        //prm.weight_label = null;    //for C_SVC
        //prm.weight = null;           //for C_SVC
        //prm.nu = 0.5;            //for NU_SVC, ONE_CLASS, and NU_SVR(默认0.5)表示防止过拟合，容忍误差的程度，可以通过调节这个会改变训练出来超平面的位置
        //prm.p = 0.1;             //for EPSILON_SVR (默认0.1)
        //prm.shrinking = 1;      //是否使用启发式，0或1(默认1)
        //prm.probability = 0;
        model = svm.svm_train(sp, prm); //生成模型  //GC20231108
    }

    /**
     * 从assets文件夹中获取crctable的数据
     */
    public void getCrcTable() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "crctable.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;
        try {
            //读取bis流中的下一个字节
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();
            String[] split = s.split("\\s+");
            crcTable = new long[256];
            for (int i = 0; i < split.length; i++) {
                crcTable[i] = Long.parseLong(split[i], 16);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取蓝牙数据流
     */
    private void startGetStream() {
        try {
            bluetoothSocket = MyApplication.getInstances().getBluetoothSocket();
            if (bluetoothSocket != null) {
                //通过蓝牙socket获得输入流
                inputStream = bluetoothSocket.getInputStream();
            }
        } catch (IOException e) {
            Toast.makeText(this, getResources().getString(R.string.Can_not_get_input_stream_via_Bluetooth_socket), Toast.LENGTH_SHORT).show();
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            e.printStackTrace();
        }
        if (!hasGotStream) {
            //重连时下发当前的参数命令  //GC20190407 蓝牙重连功能优化
            if(Constant.CurrentVoiceParam != null) {
                sendCommand(Constant.CurrentVoiceParam);
            }
            if(Constant.CurrentFilterParam != null) {
                sendCommand(Constant.CurrentFilterParam);
            }
            if(Constant.CurrentMagParam != null) {
                sendCommand(Constant.CurrentMagParam);
            }
            //当前连接状态为连接
            Constant.BluetoothState = true;
            //启动缓存获取蓝牙数据的线程
            new Thread(getStream).start();
            hasGotStream = true;
        }

    }

    /**
     * 缓存获取蓝牙数据流的线程
     */
    Runnable getStream = new Runnable() {
        @Override
        public void run() {
            int len;
            int lastLen = 127;
            //存放每个输入流的字节数组
            byte[] buffer = new byte[1024000];
            while (true) {
                try {
                    do {
                        if (inputStream == null) {
                            Log.e("打印-inputStream", "null");
                            return;
                        }
                        len = inputStream.read(buffer, 0, buffer.length);
//                        Log.e("stream", "len: " + len + "  时间：" + System.currentTimeMillis());     //GT20180321 每个输入流的的长度
                        //GC20200114    重新连接时——输入流先是一个长度很长的数据，然后紧跟着许多连续的长度为127的数据
                        if (len > 700) {
                            Log.e(TAG, "处理掉无效空数据>700" + " len:" + len);
//                            return; //小米重连操作    //GC20220727
                        } else {
                            if ((len == 127) && (lastLen == 127)) {
                                emptyCount++;
                            } else {
                                emptyCount = 0;
                            }
                            byte[] tempBuffer = new byte[len];
                            for (int i = 0, j = streamLength; i < len; i++, j++) {
                                if (Constant.isStartInterception) {
                                    tempBuffer[i] = buffer[i];
                                }
                                //将传过来的字节数组转变为int数组
                                stream[j] = buffer[i] & 0xff;
                            }
                            //截取上传数据    //jwj20180411
                            if (Constant.isStartInterception) {
                                Constant.sbData.append(Utils.bytes2HexString(tempBuffer));
                            }
                            streamLength += len;
//                            Log.e("streamLength", "streamLength:" + streamLength);  //GT20180321 要处理的蓝牙数据的长度
                            //GC20200114    连续3次len长度127，清空
                            if (emptyCount == 3) {
                                //缓存的数据清零
                                streamLength = 0;
                                emptyCount = 1;
                                Log.e(TAG, "处理掉无效空数据 " + " lastLen:" + lastLen + " len:" + len);
                            }
                            lastLen = len;

                        }
                        //在不处理数据时缓存数个输入流
                        //GC20200104 if (streamCount >= 5 && !doingStream) {
                        if (streamLength >= 590 && !doingStream) {
                            if (hasSentInitCommand) {
                                System.arraycopy(stream, 0, blueStream, 0, streamLength);
                                blueStreamLen = streamLength;
                                doingStream = true;

                            } else {
                                //缓存数据之前先发送初始化控制命令
                                sendMagneticInitCommand();
                                handle.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendVoiceInitCommand();
                                    }
                                }, 1000);
                                hasSentInitCommand = true;
                            }
                            //缓存的数据清零
                            streamLength = 0;
                        }
                        //短时间没有数据才跳出进行显示
                    } while (inputStream.available() != 0);

                } catch (IOException e) {
                    try {
                        inputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    inputStream = null;
                    //提示框连接断开提示 //GC20190407 蓝牙重连功能优化
                    Message message = new Message();
                    message.what = LINK_LOST;
                    handle.sendMessage(message);
                    //当前连接状态为断开
                    Constant.BluetoothState = false;
                    //重置变量
                    stream = null;
                    stream = new int[1024000];
                    streamLength = 0;
                    blueStream = null;
                    blueStream = new int[1024000];
                    blueStreamLen = 0;
                    doingStream = false;
                    streamLeft = null;
                    streamLeft = new int[59];
                    leftLen = 0;
                    hasLeft = false;
                    //启动重连线程，是否需要单独线程控制，需要观察
                    needReconnect = false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!needReconnect) {
                                reconnect();
                            }
                        }
                    }).start();
                    return;
                }
            }
        }
    };

    /**
     * 丢失连接（硬件断开）后，尝试重新连接 //GC20190407
     */
    public void reconnect() {
        //读取设置数据
        SharedPreferences shareData = getSharedPreferences("Add", 0);
        String address = shareData.getString(String.valueOf(1), null);
        //通过MAC地址获取蓝牙设备
        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        //用服务号得到socket
        try {
            reconnectSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            MyApplication.getInstances().setBluetoothSocket(reconnectSocket);
            MyApplication.getInstances().setBluetoothDevice(bluetoothDevice);
            MyApplication.getInstances().setBluetoothAdapter(bluetoothAdapter);

        } catch (IOException e) {
//            Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();
        }
        //连接socket
        try {
            if (reconnectSocket != null) {
                reconnectSocket.connect();
                needReconnect = true;
                EventBus.getDefault().post(new RestartGetStreamEvent(bluetoothDevice.getName()));
                Log.e(TAG, "尝试连接成功");
            }

        } catch (IOException e) {
            try {
                reconnectSocket.close();
                reconnectSocket = null;
                Log.e(TAG, "尝试连接走到异常");
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @param event 蓝牙重新连接成功后的事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RestartGetStreamEvent event) {
        //提示框提示语更新    //GC20190613
        Message message = new Message();
        message.what = LINK_CONNECT;
        handle.sendMessage(message);
        Toast.makeText(this, getResources().getString(R.string.connect) + " " + event.device + " " + getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
        hasGotStream = false;
        startGetStream();
    }

    /**
     * 处理蓝牙数据的线程
     */
    Thread doStream = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (doingStream) {
                    doStream(blueStream, blueStreamLen);
                    doingStream = false;
                }
            }
        }
    });

    /*APP接收的蓝牙数据流——
    ①数据包结构：一包59个字节
    (1)	S/M：声音、磁场数据的选择
            0x53：声音
            0x54：声音静音状态
            0x4d：磁场
    (2)	Mark：数据的标记位
    当数据为声音数据时:
        位7：是否在此包数据触发
            0：没有触发
            1：触发
        位6~0：触发时的数据点所在位置（0~99）
    当数据为磁场数据时：
        位7：判断探头在电缆哪一侧
		    0：左
		    1：右
        位1~0：为磁场数据的顺序
            00
            01
            10
            11
    (3)	Index：解码数据index
    (4)	Predsample：解码数据（由两个字节组成，第4个字节为高8位，第5个字节为低8位）
    (5)	Date：声音编码数据
    (6)	Crc：循环冗余校验码（由末尾4个字节组成）

    ②控制命令——设备端响应：一组7个字节
    Device：设备地址，T-506为0x60  十进制96
    Function：对应功能码
    Respond：响应值，命令是否响应
        0：未响应
 	    1：已响应
 	Crc：占4个字节*/

    /**
     * @param temp 需要处理的蓝牙数据
     * @param tempLength   数据长度
     */
    private void doStream(int[] temp, int tempLength) {
        int i = 0;
        //处理过的数据长度
        int dataNum = 0;
        int[] receivedBean = new int[59];
        int[] receivedCommand = new int[7];
        //处理过后有剩余数据
        if (hasLeft) {
            for (int j = leftLen, k = 0; j < 59; j++, k++) {
                //合并剩余数据
                streamLeft[j] = temp[k];
            }
            for (int i1 = 0; i1 < 59; i1++) {
                //找数据头（0x53：声音  0x4d：磁场  0x60：DEVICE  0x54：声音静音状态） //GC20191220
                if (streamLeft[i1] == 83 || streamLeft[i1] == 77 || streamLeft[i1] == 96 || streamLeft[i1] == 84 || streamLeft[i1] == 78) { //GC20240222    78=0x4e：数据截取时水平磁场数据头添加
                    for (int i2 = 0, j = i1; i2 < 59; i2++, j++) {
                        if (j >= 59) {
                            receivedBean[i2] = temp[i + j - leftLen];
                        } else {
                            receivedBean[i2] = streamLeft[j];
                        }
                        boolean isCrc = doTempCrc(receivedBean);
                        //判断是数据包数据
                        if (isCrc) {
                            doTempBean(receivedBean);
                            i1 += 58;
                        } else {
                            if (streamLeft[i1] == 96) {
                                for (int i3 = 0, k = i1; i3 < 7; i3++, k++) {
                                    if (k >= 59) {
                                        receivedCommand[i3] = temp[i + k - leftLen];
                                    } else {
                                        receivedCommand[i3] = streamLeft[k];
                                    }
                                }
                                boolean isCrc2 = doTempCrc2(receivedCommand);
                                //判断是控制命令的反馈
                                if (isCrc2) {
                                    hasSentCommand = false;
                                    i1 += 6;
                                    //第三个字节Respond：0，命令未响应;1，命令已响应      //蓝牙耳机状态反馈：2连接；3断开  //GC20210714
                                    if (receivedCommand[2] == 0) {
                                        //Respond：响应值为0，命令未响应
//                                        handle.sendEmptyMessage(SEND_ERROR);  //GT20240301 屏蔽测试效果
                                        EventBus.getDefault().post(new SendCommandNotRespondEvent());   //GT20240301
                                        seekBarType = 0;
                                    } else if (receivedCommand[2] == 1) {
//                                        if (receivedCommand[1] == sendCommand[1]) {   //GC20210707    命令发送错误BUG改进试验
                                            //接収的命令内容与要发送的命令内容一致
                                            seekBarType = 0;
                                            handle.sendEmptyMessage(SEND_SUCCESS);
//                                        }
                                    } else if (receivedCommand[2] == 2) {
                                        handle.sendEmptyMessage(HEADPHONES_CONNECT);
                                    } else if (receivedCommand[2] == 3) {
                                        handle.sendEmptyMessage(HEADPHONES_DISCONNECT);
                                    }
                                }
                            }
                        }
                    }}
            }
            hasLeft = false;
        }
        //开始遍历处理
        for (; i < tempLength - 59; i++) {
            //找数据头（0x53：声音  0x4d：磁场  0x60：DEVICE  0x54：声音静音状态） //GC20191220
            if (temp[i] == 83 || temp[i] == 77 || temp[i] == 96 || temp[i] == 84 || temp[i] == 78) { //GC20240222    78=0x4e：数据截取时水平磁场数据头添加
                //截取数据包长度的数据
                for (int j = i, k = 0; j < (i + 59); j++, k++) {
                    receivedBean[k] = temp[j];
                }
                boolean isCrc = doTempCrc(receivedBean);
                //判断是数据包数据
                if (isCrc) {
                    doTempBean(receivedBean);
                    i += 58;
                } else {
                    if (temp[i] == 96) {
                        //截取控制命令长度的数据
                        for (int j = i, k = 0; j < (i + 7); j++, k++) {
                            receivedCommand[k] = temp[j];
                        }
                        boolean isCrc2 = doTempCrc2(receivedCommand);
                        //判断是控制命令的反馈
                        if (isCrc2) {
                            hasSentCommand = false;
                            i += 6;
                            //第三个字节Respond：0，命令未响应;1，命令已响应      //蓝牙耳机状态反馈：2连接；3断开  //GC20210714
                            if (receivedCommand[2] == 0) {
//                                handle.sendEmptyMessage(SEND_ERROR);  //GT20240301 屏蔽测试效果
                                EventBus.getDefault().post(new SendCommandNotRespondEvent());   //GT20240301
                                seekBarType = 0;
                            } else if (receivedCommand[2] == 1) {
//                                if (receivedCommand[1] == sendCommand[1]) {   //GC20210707    命令发送错误BUG改进试验
                                    //接収的命令内容与要发送的命令内容一致
                                    seekBarType = 0;
                                    handle.sendEmptyMessage(SEND_SUCCESS);
//                                }
                            }else if (receivedCommand[2] == 2) {
                                handle.sendEmptyMessage(HEADPHONES_CONNECT);
                            } else if (receivedCommand[2] == 3) {
                                handle.sendEmptyMessage(HEADPHONES_DISCONNECT);
                            }
                        }
                    }
                }
            }
            dataNum = i;
        }

        if (dataNum == tempLength) {
            hasLeft = false;
        } else {
            //把剩下的数据存到临时数组中 同时设置有剩余的数组
            for (int j = dataNum + 1, k = 0; j < tempLength; j++, k++) {
                streamLeft[k] = temp[j];
            }
            leftLen = tempLength - i;
            hasLeft = true;
        }

    }

    /**
     * @param tempCrc   接収的数据包
     * @return  CRC校验的结果
     */
    public boolean doTempCrc(int[] tempCrc) {
        int[] ints = new int[55];
        //crc校验返回回来进行比对的4个字符
        int[] ints2 = new int[4];
        for (int i1 = 0, j = 0; i1 < 55; i1++, j++) {
            ints[j] = tempCrc[i1];
        }
        for (int i1 = 55, j = 0; i1 < 59; i1++, j++) {
            ints2[j] = tempCrc[i1];
        }
        return isCrc(ints, ints2);
    }

    /**
     * @param tempCrc   接收的控制命令
     * @return  CRC校验的结果
     */
    public boolean doTempCrc2(int[] tempCrc) {
        int[] ints = new int[3];
        //crc校验返回回来进行比对的4个字符
        int[] ints2 = new int[4];
        for (int i1 = 0, j = 0; i1 < 3; i1++, j++) {
            ints[j] = tempCrc[i1];
        }
        for (int i1 = 3, j = 0; i1 < 7; i1++, j++) {
            ints2[j] = tempCrc[i1];
        }
        return isCrc(ints, ints2);
    }

    /**
     * @param ints  数据内容
     * @param ints2 crc码
     * @return  判断Crc校验的结果
     */
    public boolean isCrc(int[] ints, int[] ints2) {
        long l = getCrc(ints);
        long ll = (long) (ints2[0] * Math.pow(2, 24) + ints2[1] * Math.pow(2, 16) + ints2[2] *
                Math.pow(2, 8) + ints2[3]);
        return l == ll;
    }

    /**
     * @param tempBean  对59个字节数据进行bean对象的处理
     */
    int[] resultsMag;   //GN20240305 蓝牙数据无ADOCM编码，用于查看磁场数据
    private void doTempBean(int[] tempBean) {
        PackageBean packageBean = new PackageBean();
        packageBean.setSM(tempBean[0]);
        packageBean.setMark(tempBean[1]);
        packageBean.setIndex(tempBean[2]);
        packageBean.setPredsample(new int[]{tempBean[3], tempBean[4]});
        int[] date = new int[50];
        for (int i1 = 5, j = 0; i1 < 55; i1++, j++) {
            date[j] = tempBean[i1];
        }
        resultsMag = new int[25];
        for (int i = 0; i < 25; i++) {
            resultsMag[i] = date[2*i] * 256 + date[2*i+1];   //GN20240305 50个点的8位数据转为25个点的16位数据
        }
        packageBean.setDate(date);
        doPackageBean(packageBean);
    }

    /**
     * 解码packageBean并对其进行相应的操作
     */
    public boolean isRight = false;
    public boolean isSingle = false;
    public int state = 1;
    public double amplify = 1;    //放大倍数  //GC20231019
    public void doPackageBean(PackageBean packageBean) {
        int[] results = decodeData(packageBean);    //GN20240305 原来的数据需要先解码
        //1、是声音数据——SM: "83" = 0x53（正常状态）；"84" = 0x54（静音状态）
        if (packageBean.getSM() == 83 || packageBean.getSM() == 84) {
            int mark = packageBean.getMark();
            //判断声音状态并显示    //GC20191220
            if (packageBean.getSM() == 84) {
//                Log.i(TAG, "静音状态");
                handle.sendEmptyMessage(MUTE_STATE);
            } else {
                handle.sendEmptyMessage(NO_MUTE_STATE);
            }
            //放大处理方式①：每一包声音数据直接放大   //GC20231019
            /*if (isAmplify) {
                int[] tempResults = new int[100];
                int[] tempResultsMax = new int[100];
                int maxTempResults = 0;
                for (int i = 0; i < 100; i++) {
                    //找到声音信号去直流分量后的最大值maxTempResults   //GC20231019
                    tempResults[i] = results[i];
                    tempResults[i] = tempResults[i] - 2048; //去直流分量
                    if (tempResults[i] > 2047) {            //去坏点
                        tempResults[i] = 2047;
                    } else if (tempResults[i] < -2048) {
                        tempResults[i] = -2048;
                    }
                    tempResultsMax[i] = Math.abs(tempResults[i]);   //取绝对值
                    if (tempResultsMax[i] > maxTempResults) {
                        maxTempResults = tempResultsMax[i];
                    }
                }
                amplify = (double) (2048 / maxTempResults);    //根据最大值算放大倍数
                Log.e("声音播放放大1", "最大值maxTempResults: " + maxTempResults + " 放大倍数m: " + amplify);
                for (int i = 0; i < 100; i++) {
                    tempResults[i] = (int) (amplify * tempResults[i]);
                    results[i] = tempResults[i] + 2048;         //得到放大后的原始数据
                }
            }*/
            //截取需要绘制的声音数据
            if (binaryStartsWithOne(mark)) {
                //Mark最高位是1——代表仪器在这一包声音数据内触发
//                Message msg = new Message();
//                msg.what = LIGHT_UP;  //GC20220510
//                handle.sendMessage(msg);
                /*//找触发后第一包的极小值点  //GC20231022
                for (int i = 0; i < 100; i++) {
                    tempVoiceTrigger[i] = results[i];
                }
                findExtremePoint(); */
                //放大处理方式③：触发后的几个包才放大——过程1   //GC20231021
                if (isAmplify) {
                    //触发这一包声音数据的放大处理   //GC20231021
                    int[] tempResults = new int[100];
                    int[] tempResultsMax = new int[100];
                    int maxTempResults = 0;
                    for (int i = 0; i < 100; i++) {
                        //找到声音信号去直流分量后的最大值maxTempResults
                        tempResults[i] = results[i];
                        tempResults[i] = tempResults[i] - 2048; //去直流分量
                        if (tempResults[i] > 2047) {            //去坏点
                            tempResults[i] = 2047;
                        } else if (tempResults[i] < -2048) {
                            tempResults[i] = -2048;
                        }
                        tempResultsMax[i] = Math.abs(tempResults[i]);   //取绝对值
                        if (tempResultsMax[i] > maxTempResults) {
                            maxTempResults = tempResultsMax[i];
                        }
                    }
                    amplify = (double) (2048 / maxTempResults);    //根据最大值算放大倍数
                    Log.e("声音播放放大3", "触发那一包maxTempResults: " + maxTempResults + " 放大倍数m: " + amplify);
                    for (int i = 0; i < 100; i++) {
                        tempResults[i] = (int) (amplify * tempResults[i]);
                        results[i] = tempResults[i] + 2048;         //得到放大后的原始数据
                    }
                }
//                isPlay = true;  //GC20231023
                isVoicePack = true;
                //Mark后7位，找到触发时刻声音数据点所在的位置
                triggeredPosition = getMarkLastSeven(mark);
                //凑足触发时刻前100个点的声音数据   //GC20180412 截取声音数据步骤2
                for (int i = 0, j = triggeredPosition; j < 100; i++, j++) {
                    //提取缓存数据中有效的部分
                    tempVoice[i] = tempVoice[j];
                }
                for (int i = 100 - triggeredPosition, j = 0; j < 100; i++, j++) {
                    //将当前这一包的数据按位置放入缓存数组
                    tempVoice[i] = results[j];
                }
                //截取声音包的个数
                voicePackCount++;
                //放大处理方式②：触发后的几个包合并后再放大——过程1  //GC20231020
                /*isVoicePackStart = true;
                for (int i = 0; i < 100; i++) {
                    tempResults[i] = results[i];    //缓存触发后的第1包 //GC20231020
                }*/
                voicePackCount2++;  //GC20231020  //计算处理到第几包  //GC20231021
            } else {
                if (isVoicePack) {
                    //放大处理方式③：触发后的几个包才放大——过程2   //GC20231021
                    if (isAmplify) {
                        //触发后续包声音数据的放大处理   //GC20231021
                        int[] tempResults = new int[100];
                        int[] tempResultsMax = new int[100];
                        int maxTempResults = 0;
                        for (int i = 0; i < 100; i++) {
                            //找到声音信号去直流分量后的最大值maxTempResults
                            tempResults[i] = results[i];
                            tempResults[i] = tempResults[i] - 2048; //去直流分量
                            if (tempResults[i] > 2047) {            //去坏点
                                tempResults[i] = 2047;
                            } else if (tempResults[i] < -2048) {
                                tempResults[i] = -2048;
                            }
                            tempResultsMax[i] = Math.abs(tempResults[i]);   //取绝对值
                            if (tempResultsMax[i] > maxTempResults) {
                                maxTempResults = tempResultsMax[i];
                            }
                        }
//                        amplify = (double) (2048 / maxTempResults);    //根据最大值算放大倍数
//                        Log.e("声音播放放大3", "触发后续包maxTempResults: " + maxTempResults + "放大倍数m: " + amplify);
                        //上面屏蔽后统一用第一包的放大倍数
                        //放大触发后的几个包
                        for (int i = 0; i < 100; i++) {
                            tempResults[i] = (int) (amplify * tempResults[i]);
                            results[i] = tempResults[i] + 2048;         //得到放大后的原始数据
                        }
                        Log.e("声音播放放大3", "触发后续包maxTempResults: " + maxTempResults);
                        //改为只放大前3包，用于观察分界处
//                        if (voicePackCount2 < 3) {
//                            for (int i = 0; i < 100; i++) {
//                                tempResults[i] = (int) (amplify * tempResults[i]);
//                                results[i] = tempResults[i] + 2048;         //得到放大后的原始数据
//                            }
//                            voicePackCount2++;  //GC20231021
//                        }
//                        Log.e("声音播放放大3", "voicePackCount2: " + voicePackCount2 + "voicePackCount: " + voicePackCount);
                    }
                    //放大处理方式②：触发后的几个包合并后再放大——过程2（4包）  //GC20231020
                    /*if (voicePackCount2 < 4) {
                        //这是需要放大的声音后续的包，拼4包 //GC20231020
                        for (int i = 100 * voicePackCount2, j = 0; j < 100; i++, j++) {
                            tempResults[i] = results[j];
                        }
                        voicePackCount2++;
                    } else {
                        isVoicePackStart = false;   //缓存需要放大的声音包完毕，拼4包 //GC20231020
                        voicePackCount2 = 0;
                    }*/
                    //已经开始截取声音包
                    if (voicePackCount <= 7) {
                        //凑足触发时刻后700个点的声音数据   //GC20180412 截取声音数据步骤3
                        for (int i = 100 - triggeredPosition + voicePackCount * 100, j = 0; j < 100; i++, j++) {
                            tempVoice[i] = results[j];  //缓存声音数据    //GC20231118
                        }
                        /*//放大处理方式②：触发后的几个包合并后再放大——过程2（8包）  //GC20231020
                        //这是需要放大的声音后续的包，拼8包 //GC20231020
                        for (int i = 100 * voicePackCount, j = 0; j < 100; i++, j++) {
                            tempResults[i] = results[j];
                        }*/
                        voicePackCount++;
                    } else {
                        isVoicePack = false;
                        voicePackCount = 0;
//                        isVoicePackStart = false;   //缓存需要放大的声音包完毕，拼8包 //GC20231020
                        voicePackCount2 = 0;    //GC20231020    //计算处理到第几包后清零 //GC20231021
//                        isPlay = false;  //GC20231023
                    }
                } else {
                    //未开始截取声音包
                    for (int i = 0, j = 0; j < 100; i++, j++) {
                        //先缓存1包数据，用于凑足触发时刻前100个点的声音数据   //GC20180412 截取声音数据步骤1
                        tempVoice[i] = results[j];  //缓存声音数据    //GC20231118
//                        tempVoiceBeforeTrigger[i] = results[j];   //用于找极小值    //GC20231022
                    }
                }
            }
            //GC20231023
            if (voicePackCount3 < 10) {
//                int[] tempResults = new int[100];
//                int[] tempResultsMax = new int[100];
//                int maxTempResults = 0;
//                for (int i = 0; i < 100; i++) {
//                    //找到声音信号去直流分量后的最大值maxTempResults   //GC20231019
//                    tempResults[i] = results[i];
//                    tempResults[i] = tempResults[i] - 2048; //去直流分量
//                    if (tempResults[i] > 2047) {            //去坏点
//                        tempResults[i] = 2047;
//                    } else if (tempResults[i] < -2048) {
//                        tempResults[i] = -2048;
//                    }
//                    tempResultsMax[i] = Math.abs(tempResults[i]);   //取绝对值
//                    if (tempResultsMax[i] > maxTempResults) {
//                        maxTempResults = tempResultsMax[i];
//                    }
//                }
//                amplify = (double) (2048 / maxTempResults);    //根据最大值算放大倍数
//                Log.e("声音播放放大0", "最大值maxTempResults: " +
//                        maxTempResults + " 放大倍数m: " + amplify + " 计数voicePackCount3: " + voicePackCount3);
//                for (int i = 0; i < 100; i++) {
//                    tempResults[i] = (int) (amplify * tempResults[i]);
//                    results[i] = tempResults[i] + 2048;         //得到放大后的原始数据
//                }
                for (int i = 0; i < 100; i++) {
                    results[i] = (int) (1.5 * results[i]);
                }
//                Log.e("声音播放放大0", " 计数voicePackCount3: " + voicePackCount3);
                voicePackCount3++;
                isPlay = true;
            } else {
                voicePackCount3 = 0;
                isPlay = false;
//                Log.e("声音播放放大0", " 计数voicePackCount3: " + voicePackCount3);
            }
            //1、声音数据解码之后立刻播放
            playSound(results);
            //2、只播放触发那一段  //GC20231023
//            if (isAmplify) {
//                if (isPlay) {
//                    playSound(results);
//                }
//            }
            //3、触发后的几个包合并后再放大——播放   //GC20231020
            /*if (isAmplify) {    //放大时声音数据需要处理
                if (!isVoicePackStart) {    //凑足声音包了    //GC20231020
                    int maxTempResults = 0;
                    for (int i = 0; i < 400; i++) {
                        //找到声音信号去直流分量后的最大值maxTempResults
                        tempResults[i] = tempResults[i] - 2048; //去直流分量
                        if (tempResults[i] > 2047) {            //去坏点
                            tempResults[i] = 2047;
                        } else if (tempResults[i] < -2048) {
                            tempResults[i] = -2048;
                        }
                        tempResultsMax[i] = Math.abs(tempResults[i]);   //取绝对值
                        if (tempResultsMax[i] > maxTempResults) {
                            maxTempResults = tempResultsMax[i];
                        }
                    }
                    amplify = (double) (2047 / maxTempResults);    //根据最大值算放大倍数
                    Log.e("声音播放放大2", "最大值maxTempResults: " + maxTempResults + " 放大倍数m: " + amplify);
                    for (int i = 0; i < 400; i++) {
                        tempResults[i] = (int) (amplify * tempResults[i]);    //放大对应的倍数
                        tempResults[i] = tempResults[i] + 2048;         //还原数据准备播放
//                        tempVoice[i] = tempResults[i];  //绘制波形也改变
                    }
                    for (int i = 0; i < 4; i++) {   //逐包播放
                        //将tempResults从100 * i开始复制100个数到results数组
                        System.arraycopy(tempResults, 100 * i, results, 0, 100);
                        playSound(results);
                    }
                }
                else {  //不需要处理的数据包，直接播放
                    playSound(results);
                }
            } else {
                playSound(results); //不放大时声音数据解码之后立刻播放
            }*/
        }
        //2、是磁场数据——SM: "77" = 0x4d  "78" = 0x4e
        else if (packageBean.getSM() == 77 || packageBean.getSM() == 78) {  //GC20240222    78=0x4e：数据包解码时水平磁场数据头添加
            if (packageBean.getSM() == 78) {    //"78" = 0x4e：是水平线圈的磁场数据  //GC20240222 APP兼容旧底板程序的写法
                if (packageBean.getMark() >= 128) {
                    //Mark: "128"= 10000000 位7是1：两个磁场线圈产生的信号方向不同，相对电缆位置是“右” //GC20171205
                    //令位7=0
                    packageBean.setMark(packageBean.getMark() - 128);
                }
                //按顺序拼接磁场数据（从0到3，共4包，1包有100个点数据）
                /*for (int i = 0, j = packageBean.getMark() * 100; i < 100; i++, j++) {
                    magneticArray2[j] = results[i];  //缓存磁场数据  //GC20231118
                }*/ //GN20240305 现在不用解码直接拿来用
                //按顺序拼接磁场数据（从0到3，共4包，1包有50个点数据,解码出25个有效数据）
                for (int i = 0, j = packageBean.getMark() * 25; i < 25; i++, j++) {
                    magneticArray2[j] = resultsMag[i];  //缓存磁场数据前100个点数据
                }
                if (packageBean.getMark() == 3){
                    for (int i = 0, j = 100; i < 300; i++, j++) {
                        magneticArray2[j] = 2048;  //后300个点赋值为2048  避免影响自动增益 //GT20240309
                    }
                }
            }
            else {
                //优化根据“标记位”数值来判断主机探头相对电缆位置    //GC20211229
                /*if (packageBean.getMark() >= 192) {
                    //Mark: "192"= 11000000 位7和位6是1：有一个磁场线圈产生的信号为0 位置根据磁场信号数值和记忆的方向来判断 //GC20171205
                    if (isDraw) {
                        isSingle = true;
                    }
                    //令位7=0
                    packageBean.setMark(packageBean.getMark() - 192);
                } else */   //GC20240225    ①不用了
                if (packageBean.getMark() >= 128) {
                    //Mark: "128"= 10000000 位7是1：两个磁场线圈产生的信号方向不同，相对电缆位置是“右” //GC20171205    //GN20240227 (同相左，异向右)
                    if (isDraw) {
                        //非“暂停”状态下才刷新位置显示    //GC20200409
//                        handle.sendEmptyMessage(POSITION_RIGHT);  //GN20240227 主板程序上传的“左右”结果需要再判断下（有时电缆过远，判断结果不对）
//                        isSingle = false; //GC20240225    ①不用了
                        isRight = true;
                    }
                    //令位7=0
                    packageBean.setMark(packageBean.getMark() - 128);
                } else {
                    if (isDraw) {
//                        handle.sendEmptyMessage(POSITION_LEFT);   //GN20240227
//                        isSingle = false; //GC20240225    ①不用了
                        isRight = false;
                    }
                }
                //按顺序拼接磁场数据（从0到3，共4包，1包有100个点数据）
                /*for (int i = 0, j = packageBean.getMark() * 100; i < 100; i++, j++) {
                    magneticArray[j] = results[i];  //缓存磁场数据  //GC20231118
                }*/ //GN20240305 现在不用解码直接拿来用
                //按顺序拼接磁场数据（从0到3，共4包，1包有50个点数据,解码出25个有效数据）
                for (int i = 0, j = packageBean.getMark() * 25; i < 25; i++, j++) {
                    magneticArray[j] = resultsMag[i];  //缓存磁场数据前100个点数据
                }
                if (packageBean.getMark() == 3){
                    for (int i = 0, j = 100; i < 300; i++, j++) {
                        magneticArray[j] = 2048;  //后300个点赋值为2048  避免影响自动增益 //GT20240309
                    }
                }
                //磁场信号第一包来到 //GC20220510 触发灯亮起时机更改
                if (packageBean.getMark() == 0) {
                    //“同步指示” 灯变红
                    Message msg = new Message();
                    msg.what = LIGHT_UP;
                    handle.sendMessage(msg);
                }
                //4个磁场包拼接完成，开始画主界面波形
                if (packageBean.getMark() == 3) {
                    //“同步指示” 灯变灰
//                Message msg = new Message();
//                msg.what = TRIGGERED; //GC20220510    //GC20240225    ②不用了
//                handle.sendMessage(msg);
                    if (isDraw) {
                        //得到绘制的磁场数组magneticDraw
                        for (int i = 0; i < 400; i++) {
                            if (!isChangeMag) {
                                magneticDraw[i] = magneticArray[i]; //magneticDraw绘制的磁场数据    //GC20231118
                            } else {
                                magneticDraw[i] = magneticArray2[i]; //GT20240228 用于调试，magneticDraw绘制水平磁场数据
                            }
                            if (i >= 50) {  //GT20240307 增益大小判断去除前50个点
                                //找到磁场信号幅值最大值maxMagnetic用于用户界面画进度条高度   //GC20181113
                                magTemp1[i] = magneticArray[i] - 2048; //找maxMagnetic  //GC20231118
                                if (magTemp1[i] > 2047) {
                                    magTemp1[i] = 2047;
                                } else if (magTemp1[i] < -2048) {
                                    magTemp1[i] = -2048;
                                }
                                magTemp1[i] = Math.abs(magTemp1[i]);
                                if (magTemp1[i] > maxMagnetic) {
                                    maxMagnetic = magTemp1[i];
                                }
                                magTemp2[i] = magneticArray2[i] - 2048; //找水平线圈最大值maxMagnetic2  //GC20240226
                                if (magTemp2[i] > 2047) {
                                    magTemp2[i] = 2047;
                                } else if (magTemp2[i] < -2048) {
                                    magTemp2[i] = -2048;
                                }
                                magTemp2[i] = Math.abs(magTemp2[i]);
                                if (magTemp2[i] > maxMagnetic2) {
                                    maxMagnetic2 = magTemp2[i];
                                }
                            }
                        }
                        Log.e(TAG, "增益判断" + " /maxMagnetic=" + maxMagnetic + " /maxMagnetic2=" + maxMagnetic2);
                        autoGain(); //GC20240226
                        if ((maxMagnetic < 102) | (maxMagnetic2 < 102)) {   //GN20240227    线圈增益过小，认为其方向判断无效——如果最大值小于 2048*5%=102
                            isTooSmall = true;  //增益过小状态记录  //GC20240304
                            //判断无效不改变之前的结果  //GC20240304 之前的结果是isRightOld
                            if (isRightOld) {
                                handle.sendEmptyMessage(POSITION_RIGHT);
                            } else {
                                handle.sendEmptyMessage(POSITION_LEFT);
                            }
                        } else {    //线圈增益不过小，判断的“左右”结果才采纳   //GN20240227
                            isTooSmall = false;  //增益过小状态记录  //GC20240304
                            if (isRight) {
                                handle.sendEmptyMessage(POSITION_RIGHT);  //GN20240227
                                isRightOld = true;  //GC20240304
                            } else {
                                handle.sendEmptyMessage(POSITION_LEFT);  //GN20240227
                                isRightOld = false; //GC20240304
                            }
                        }
                        /*//根据第5个点数值判断    //GC20211229
                        if (isSingle) {
                            //单个线圈有信号
                            if ( magneticDraw[5] > 2048) {
                                if (state == 1) {
                                    handle.sendEmptyMessage(POSITION_RIGHT);
                                } else if (state == 2){
                                    handle.sendEmptyMessage(POSITION_LEFT);
                                }
                            } else {
                                if (state == 3) {
                                    handle.sendEmptyMessage(POSITION_RIGHT);
                                } else if (state == 4){
                                    handle.sendEmptyMessage(POSITION_LEFT);
                                }
                            }
                        } else {
                            //可正常判断方向时记录状态
                            if ( magneticDraw[5] > 2048) {
                                if (isRight) {
                                    state = 1;
                                } else {
                                    state = 2;
                                }
                            } else {
                                if (isRight) {
                                    state = 3;
                                } else {
                                    state = 4;
                                }
                            }
                        }*/   //GC20240225    ①不用了
                        //截取需要绘制的声音数组voiceDraw      //GC20180428 加上触发前50个点的数据   //GC20200103 改回触发时刻
                        System.arraycopy(tempVoice, 100, voiceDraw, 0, 400);    //voiceDraw绘制的声音 //GC20231118
                        //用于声音自动识别定位的数组
                        for (int i = 0; i < 800; i++) {
                            //用于计算特征值的声音数据  //GC20180412    //GC20231118
                            svmData[i] = tempVoice[i];
                            //用于自动定位的声音数据   //GC20181201    //GC20231118
                            autoLocate[i] = tempVoice[i];
                        }
                        //获取声音特征
                        obtainFeature();
                        //根据声音特征预测
                        if (svmTrainThread) {
                            voiceSvmPredict(feature);
                        }
                        Log.e("声音播放", "触发后的isAmplify状态:  " + isAmplify );   //GC20231019
//                    Log.e("声音播放", "maxVoicePlay:  " + maxVoicePlay ); //GT20200402 查看播放的音量数据
                    }
                    Message message = new Message();
                    message.what = WHAT_REFRESH;
                    handle.sendMessage(message);
                }
            }
        }

    }

    /**
     * 1判断增益，①和②的关系是或，有1路大都无法判断强度大小了    //GC20240226
     */
    private void autoGain() {
        //①竖直线圈磁场数据的增益判断    //GC20240226
        if (maxMagnetic <= 164) {
            //判断增益过小——如果最大值小于 2048*8%
            gainState = 2;  //增益过小
            Log.e(TAG, "增益判断" + " /竖直线圈增益过小，maxMagnetic=" + maxMagnetic);
        }
        if (maxMagnetic >= 1884) {
            //判断增益过小——如果最大值小于 2048*92%
            gainState = 1;  //增益过大
            Log.e(TAG, "增益判断" + " /竖直线圈增益过大，maxMagnetic=" + maxMagnetic);
        }
        //②水平线圈磁场数据的增益判断    //GC20240226
        if (maxMagnetic2 <= 164) {
            //判断增益过小——如果最大值小于 2048*15%
            gainState = 2;  //增益过小
            Log.e(TAG, "增益判断" + " /水平线圈增益过小，maxMagnetic2=" + maxMagnetic2);
        }
        if (maxMagnetic2 >= 1884) {
            //判断增益过小——如果最大值小于 2048*85%
            gainState = 1;  //增益过大
            Log.e(TAG, "增益判断" + " /水平线圈增益过大，maxMagnetic2=" + maxMagnetic2);
        }

    }

    /**
     * 找最小极小值和第二极小值 //GC20231022
     */
    public int min = 4096;
    public int minSecond = 4096;
    private void findExtremePoint() {
        /*//求触发之前那一包数据后50个点的均值
        double ave = 0;
        for (int i = 50; i < 100; i++) {
            ave += tempVoiceBeforeTrigger[i];
        }
        ave = ave / 50;
        //方差
        double var = 0;
        for (int i = 50; i < 100; i++) {
            var += (tempVoiceBeforeTrigger[i] - ave) * (tempVoiceBeforeTrigger[i] - ave);
        }
        var = var / 50;
        //标准差
        double sta = Math.sqrt(var);
        if (sta < 1) {
            sta = 1;
        }*/

        //极小值点
        int minNum = 0;
        int[] minData = new int[100];
        int[] minDataPos = new int[100];
        min = 4096;
        minSecond = 4096;
        int minPos = minDataPos[0];
        int minPosSecond = minDataPos[0];
        /*//去头去尾
        for (int i = 1; i < 99; i++) {
            if ( tempVoiceTrigger[i] < (ave - sta * 5) ){
                if ((tempVoiceTrigger[i] < tempVoiceTrigger[i - 1]) && (tempVoiceTrigger[i] <= tempVoiceTrigger[i + 1])) {
                    //只找极小值点
                    minData[minNum] = tempVoiceTrigger[i];  //极小值数值
                    minDataPos[minNum] = i;                 //极小值位置
                    minNum++;
                }
            }
        }*/
        for (int i = 3; i < 97; i++) {
            if ((tempVoiceTrigger[i] < tempVoiceTrigger[i - 1]) && (tempVoiceTrigger[i] <= tempVoiceTrigger[i + 1])) {
                if (tempVoiceTrigger[i - 1] <= tempVoiceTrigger[i - 2]) {
                    if (tempVoiceTrigger[i - 2] <= tempVoiceTrigger[i - 3]) {
                        minData[minNum] = tempVoiceTrigger[i];  //极小值数值
                        minDataPos[minNum] = i;                 //极小值位置
                        minNum++;
                    }
                }
            }
        }
        //找最小极小值和第二极小值
        if (minNum == 0) {
            Log.e("声音播放", "没有极小值");
        } else if (minNum == 1){
            min = minData[1];
            minPos = minDataPos[1];
            Log.e("声音播放", "极小值只有一个 /min = " + min);
        } else {
            for (int k = 0; k < minNum; k++) {
                if (minData[k] <= min) {
                    min = minData[k];       //最小极小值数值
                    minPos = minDataPos[k]; //最小极小值位置
                }
            }
            for (int k = 0; k < minNum; k++) {
                if ((minData[k] < minSecond) & (minData[k] > min)) {
                    minSecond = minData[k];       //第二极小值数值
                    minPosSecond = minDataPos[k]; //第二极小值位置
                }
            }
            Log.e("声音播放", "极小值 /min = " + min + "第二极小值 /minSecond = " + minSecond);
        }

    }

    /**
     * 解码数据
     */
    public int[] decodeData(PackageBean bean1) {
        int index = bean1.getIndex();
        int[] predsample = bean1.getPredsample();
        int predsample1 = predsample[0];
        int predsample2 = predsample[1];
        int pred = predsample1 * 256 + predsample2; //数据解码的原始位置 //GC20231118
        int[] date = bean1.getDate();
        /* for (int i : date) {
            Log.e("FILE", i+"");
        }*/
        int[] dateArray = new int[100];
        int count = 0;
        for (int da : date) {
            dateArray[count] = da >> 4;
            dateArray[count + 1] = da - dateArray[count] * 16;
            count += 2;
        }
        return decodeDataSecond(index, pred, dateArray);
    }

    /**
     * 二次解码
     */
    public int[] decodeDataSecond(int index, int pred, int[] dateArray) {
        int prevsample = pred;
        int previndex = index;
        int PREDSAMPLE;
        int INDEX;
        int[] result = new int[dateArray.length];
        for (int i = 0; i < dateArray.length; i++) {
            PREDSAMPLE = prevsample;
            INDEX = previndex;
            int step = stepSizeTable[INDEX];
            int code = dateArray[i];
            int diffq = step / 8;
            if ((code & 4) == 4) {
                diffq = diffq + step;
            }
            if ((code & 2) == 2) {
                diffq = diffq + step / 2;
            }
            if ((code & 1) == 1) {
                diffq = diffq + step / 4;
            }
            if ((code & 8) == 8) {
                PREDSAMPLE = PREDSAMPLE - diffq;
            } else {
                PREDSAMPLE = PREDSAMPLE + diffq;
            }

            if (PREDSAMPLE > 5000) {    //解码数据有大于5000的   //GC20231205
                Constant.isDataOver = true; //GC20231205
            } else {
                Constant.isDataOver = false;
            }

            if (PREDSAMPLE > 4095) {    //数据二次解码的原始位置 //GC20231118
                PREDSAMPLE = 4095;
            }
            if (PREDSAMPLE < 0) {
                PREDSAMPLE = 0;
            }
            //Log.e("FILE", code+"");
            INDEX = INDEX + indexTable[code];

            if (INDEX < 0) {
                INDEX = 0;
            }
            if (INDEX > 88) {
                INDEX = 88;
            }
            prevsample = PREDSAMPLE;
            previndex = INDEX;
            result[i] = prevsample;
        }
        return result;
    }

    /**
     * @param tByte 需要判断的字节数组
     * @return  判断二进制的最高位是否是1
     */
    public boolean binaryStartsWithOne(int tByte) {
        String tString = Integer.toBinaryString((tByte & 0xFF) + 0x100).substring(1);
        return tString.startsWith("1");
    }

    /**
     * 获取mark的后7位
     */
    public Integer getMarkLastSeven(int mark) {
        String tString = Integer.toBinaryString((mark & 0xFF) + 0x100).substring(1);
        String substring = tString.substring(1);
        return Integer.valueOf(substring, 2);
    }

    /**
     * @param results   声音数据转化和播放    //GT20200309
     */
    public void playSound(int[] results) {
        byte[] pcm16 = new byte[results.length * 2];    //采样位数转化为16位    //一、ENCODING_PCM_16BIT 成功播放
        byte[] pcm8 = new byte[results.length];         //采样位数转化为8位     //二、ENCODING_PCM_8BIT 成功播放
        byte[] pcm24 = new byte[results.length * 3];
        byte[] bytes = new byte[results.length * 4];
        float[] floats = new float[results.length * 3];
        float[] floats2 = new float[results.length];                            //三、ENCODING_PCM_FLOAT 成功播放   AudioFormat格式音频
        int max = 0;
        //原始采样位数为12位，改变位数最终转换成字节去播放  //GC20230601
        for (int i = 0; i < results.length; i++) {
            //转化为16位  变为short后是2个字节 表数范围-2^15 - 2^15 -1 (-32768到32767)
            short sh = (short) ((results[i] - 2048) * 16);  //  * 2^4
            //方法③
            floats2[i] = sh / 32768;
            byte[] bytes1 = shortToByte(sh);
            pcm16[i * 2] = bytes1[0];       //低8位
            pcm16[i * 2 + 1] = bytes1[1];   //高8位
            //GT20200402
            int sh1 = Math.abs(sh);
            if (sh1 > max) {
                max = sh1;
                maxVoicePlay = max;
            }
            //转化为8位 变为byte后是1个字节 表数范围-128 - 127
            pcm8[i] = (byte) ((results[i]) / 16);           //  / 2^4
            /*//转化为24位  变为int是4个字节 表数范围-2^31 - 2^31-1
            int in = (results[i] - 2048) * 16 * 256;  //2^12
            byte[] bytes2 = intToByte(in);
            bytes[i * 4] = bytes2[0];
            bytes[i * 4 + 1] = bytes2[1];
            bytes[i * 4 + 2] = bytes2[2];
            bytes[i * 4 + 3] = bytes2[3];
            //24位PCM数据 有效数据只占3个字节
            pcm24[i * 3] = bytes2[0];
            pcm24[i * 3 + 1] = bytes2[1];
            pcm24[i * 3 + 2] = bytes2[2];*/
        }
        /*//方法①
        for(int i = 0,j = 0; j < pcm24.length; i++){
            //24bit pcm转成浮点数    float 3.4*10^-38 ~3.4*10^38
            float n = ((pcm24[j++] | (pcm24[j++]<<8) | (pcm24[j++]<<16))<<8)>>8;
            n = n/16777216/2;
            floats[i] = n;
        }*/
        //方法②
//        floats = bytesToFloats(pcm8, pcm8.length,false);
        if (!isExit) {
            mAudioTrack.write(pcm16, 0, pcm16.length);  //GC20230601    pcm16   pcm8
//            mAudioTrack.write(floats2, 0, floats2.length, AudioTrack.WRITE_BLOCKING);    //WRITE_NON_BLOCKING
        }
    }

    /**
     * short转byte
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();
            // 向右移8位
            temp = temp >> 8;
        }
        return b;
    }

    /**
     * int转byte   //GT20200309
     */
    public static byte[] intToByte(int number) {
        int temp = number;
        byte[] b = new byte[4];
        //for (int i = 0; i < b.length; i++) {
        for (int i = 0; i < 4; i++) {
            b[i] = Integer.valueOf(temp & 0xff).byteValue();
            // 向右移8位
            temp = temp >> 8;
        }
        return b;
    }

    // 将byte[] 数组转换成float[]数组
    public static float[] bytesToFloats(byte[] bytes,int len,boolean isBe) {
        if(bytes==null){
            return null;
        }
        float[] floats = new float[len/4];
        // 大端序
        if (isBe) {
            ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asFloatBuffer().get(floats);
        } else {
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(floats);
        }
        return floats;
    }

    private byte[] convertTo16Bit(float[] data) {
        short[] shortValue = new short[data.length];
        byte[] byte16bit = new byte[shortValue.length * 2];
        for (int i = 0; i < data.length; i++) {
            shortValue[i] = (short) (32768 * data[i]);
        }
        byte16bit = toByteArray(shortValue);
        return byte16bit;
    }

    private byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) (src[i]);
            dest[i * 2 + 1] = (byte) (src[i] >> 8);
        }
        return dest;
    }

    /**
     * 字节转换为浮点
     *
     * @param b 字节（至少4个字节）
     * @param index 开始位置
     * @return
     */
    public static float byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }


    /**
     * 获取当前声音数据的特征值    //GC20180412
     */
    private void obtainFeature() {
        //0、声音数据归一化
        int max = 0;
        for (int i = 0; i < 800; i++) {
            svmData[i] = svmData[i] - 2048; //找maxVoice  //GC20231118
            if (svmData[i] > 2047) {
                svmData[i] = 2047;
            } else if (svmData[i] < -2048) {
                svmData[i] = -2048;
            }
            svmData[i] = Math.abs(svmData[i]);
            if (svmData[i] > max) {
                max = svmData[i];
                //找找到声音信号幅值最大值maxVoice用于用户界面画进度条高度 //GC20181113
                maxVoice = max;
            }
        }
        for (int i = 0; i < 800; i++) {
            //强制转换类型，int运算得double
            normalization[i] = svmData[i] / (max * 1.0);
        }
        //1、获取短时能量分布处理 （短时步长为50）   //特征获取  //GC20231108
        double[] mED = new double[751];
        for (int i = 0; i < 751; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + normalization[i + j] * normalization[i + j];
            }
            mED[i] = mtemp / 50;
        }
        //2、获取短时过零率 //阈值0.5 //特征获取  //GC20231108
        double threshold = 0.5;
        double[] mZCR = new double[750];
        for (int i = 0; i < 750; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + Math.abs(Math.signum(normalization[i + j + 1] - threshold) - Math.signum(normalization[i + j] - threshold))
                        + Math.abs(Math.signum(normalization[i + j + 1] + threshold) - Math.signum(normalization[i + j] + threshold));
            }   //符号函数 1 0 -1
            mZCR[i] = mtemp / 2;
        }
        //3、根据短时能量分布得到：脉冲宽度、脉冲高宽比、位置特征值
        int isStart = 0;    //是否是脉冲宽度的起始
        int Nu = 1;         //数据序数
        int widthFirst = 0;
        int widthLast = 0;
        double max2 = 0.0;
        //求能量分布数组的均值
        double sum = 0.0;
        double meanValue;
        for (int i = 0; i < 751; i++) {
            sum += mED[i];
        }
        meanValue = sum / 751;
        for (int i = 0; i < 751; i++) {
            //找超过均值的最小序数和最大序数
            if (mED[i] > meanValue) {
                if (isStart == 0) {
                    widthFirst = Nu;
                    isStart = 1;
                } else {
                    widthLast = Nu;
                }
            }
            Nu++;
            //找短时能量分布数组的最大值
            if (mED[i] > max2) {
                max2 = mED[i];
            }
        }
        //①脉冲宽度观测值----大于均值的元素中，最小序数元素与最大序数元素序数的差值
        feature[0] = widthLast - widthFirst;
        //②脉冲高宽比观测值----短时能量分布数组的最大值除以脉冲宽度得到高宽比
        feature[1] = max2 / (widthLast - widthFirst);
        //③脉冲位置取整观测值
        feature[2] = Math.round((widthLast + widthFirst) / 2);
        //4、根据短时过零率得到过零率特征值（可以用于反映信号的频率信息）
        //求短时过零率数组的均值
        double sum2 = 0.0;
        double meanValue2;
//        for (int i = 0; i < 751; i++) {
//            sum2 += mED[i];
//        }
        //之前错误使用mED[i]   //GC20231115
        for (int i = 0; i < 750; i++) {
            sum2 += mZCR[i];
        }
        meanValue2 = sum2 / 750;
        double temp = 0.0;
        for (int i = 0; i < 750; i++) {
            if (mZCR[i] > meanValue2) {
                temp = temp + mZCR[i];
            }
        }
        //④过零率特征值----数组中大于均值的元素和为过零率特征值
        feature[3] = temp;
        //5、特征值归一化（参考已采数据特征的最大最小值进行归一化）   //GC20180707  //可以优化写法?
        //%500组（750/39 0.0047/7.6523e-5 732/105 4871/7）
        feature[0] = Math.abs(feature[0] - 39) / (750 - 39);
//        feature[1] = Math.abs(feature[1] - 7.6523e-5) / (0.0047 - 7.6523e-5);
        feature[2] = Math.abs(feature[2] - 105) / (732 - 105);
//        feature[3] = Math.abs(feature[3] - 7) / (4871 - 7); //选取的数字特征   //GC20231108

        //%554组（750/39 0.0053/7.6523e-5 732/105 5301/7）（加入001/020/008/013）  //GC20231120    新特征值添加 极值有变化
        feature[1] = Math.abs(feature[1] - 7.6523e-5) / (0.0053 - 7.6523e-5);
//        feature[3] = Math.abs(feature[3] - 7) / (5301 - 7);

        //%609组（750/39 0.0053/7.6523e-5 732/105 7063/7） //GC20231206    新特征值添加 极值有变化
        feature[3] = Math.abs(feature[3] - 7) / (7063 - 7);

    }

    /**
     * 使用生成的model去预测
     * @param featurex  获得的声音特征
     */
    private void voiceSvmPredict(double[] featurex) {
        svm_node[] test = new svm_node[]{new svm_node(), new svm_node(), new svm_node(), new svm_node()};
        test[0].index = 1;
        test[1].index = 2;
        test[2].index = 3;
        test[3].index = 4;
        test[0].value = featurex[0];
        test[1].value = featurex[1];
        test[2].value = featurex[2];
        test[3].value = featurex[3];

        //不带概率的分类测试
        double result = svm.svm_predict(model, test);
        if (result == 1.0) {
            //是故障点
            EventBus.getDefault().post(new ResultOfSvmEvent(true)); //1、SVM预测结果结果为是 //GC20231201
            //连续两次判断为故障声开始进行相关
            if(svmPredictCount > 0){
                //相关判断
                correlationCalculation();   //相关计算  //GC20231108
                if(p > 0.4){    //调整相关参数阈值，旧0.9    //GC20231204
                    //相关之后再光标定位、计算延时值
                    autoLocate();   //光标定位计算  //GC20231108
                    //相关——判断是故障,虚光标位置和声磁延时值传递   //GC20190218
                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,true, position, p));   //添加p //GC20231130
                }else{
                    //不相关——判断不是故障
//                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,false, position));
//                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,true, position, p)); //不相关也认为是，看是否有相关系数低的识别故障声 //GC20231130
                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,false, position, p)); //相关后无论结果如何显示相关系数  //GC20231204
                }
            }
            //保留上次预测结果为是的声音数据用做相关计算 //GC20181119
            System.arraycopy(normalization, 0, normalization2, 0, 800);     //相关计算  //GC20231108
            svmPredictCount++;

        } else {
            //判断不是故障
            EventBus.getDefault().post(new ResultOfSvmEvent(false));
            svmPredictCount = 0;
        }

    }

    /**
     * 计算相关系数       //GC20181119
     */
    private void correlationCalculation() {
        //分母1
        double sum1 = 0;
        for (int i = 0; i < 800; i++) {
            sum1 += normalization2[i];
        }
        double ave1 = sum1 / 800;
        double var1 = 0;
        for (int i = 0; i < 800; i++) {
            var1 += (normalization2[i] - ave1) * (normalization2[i] - ave1);
        }
        double sta1 = Math.sqrt(var1);
        //分母2
        double sum2 = 0;
        for (int i = 0; i < 800; i++) {
            sum2 += normalization[i];
        }
        double ave2 = sum2 / 800;
        double var2 = 0;
        for (int i = 0; i < 800; i++) {
            var2 += (normalization[i] - ave2) * (normalization[i] - ave2);
        }
        double sta2 = Math.sqrt(var2);
        //分子
        double sum = 0;
        for (int i = 0; i < 800; i++) {
            sum += (normalization2[i] - ave1) * (normalization[i] - ave2);
        }
        //相关系数
        p = sum / (sta1 * sta2);
        /*String correlationCalculation = String.valueOf(p);
        Log.e("correlationCalculation", correlationCalculation);*/
    }

    /**
     * 得到自动定位的光标位置和相应的声磁延时值
     */
    private void autoLocate() {
        //求触发时刻前50个点的均值    //20190720
        /*double ave = 0;
        for (int i = 50; i < 100; i++) {
            ave += autoLocate[i];
        }
        ave = ave / 50;
        //方差
        double var = 0;
        for (int i = 50; i < 100; i++) {
            var += (autoLocate[i] - ave) * (autoLocate[i] - ave);
        }
        var = var / 50;*/
        //求触发时刻前100个点的均值    //GC20231206
        double ave = 0;
        for (int i = 0; i < 100; i++) {
            ave += autoLocate[i];
        }
        ave = ave / 100;
        //方差
        double var = 0;
        for (int i = 0; i < 100; i++) {
            var += (autoLocate[i] - ave) * (autoLocate[i] - ave);
        }
        var = var / 100;
        //标准差
        double sta = Math.sqrt(var);
        //GC20190720
        if (sta < 1) {
            sta = 1;
        }

        //从触发时刻（第101个点i=100）之后，找出越出置信边界的第一个极值点（屏幕波形显示触发时刻后400个点）
        int m = 0;  //极大值位置 //GC20231206
        int n = 0;
        int index = 0;  //极值位置  //GC20231206
        //去头去尾
        for (int i = 101; i < 498; i++) {
            //置信边界  //GC20181201 只求极小值
            /*if ( autoLocate[i] < (ave - sta * 5) ){
                if ((autoLocate[i] < autoLocate[i - 1]) && (autoLocate[i] <= autoLocate[i + 1])) {
                    //极小值点
                    n = i;
                }
            }
            if ( n > 0 ) {
                //对于极小值，上坡到坡顶
                while (autoLocate[n] < autoLocate[n - 1]) { //定位算法修改  //GC20231206
                    n = n - 1;
                }
                break;
            }*/
            //置信边界  //定位算法修改  //GC20231206
            if ( autoLocate[i] > (ave + sta * 5) || autoLocate[i] < (ave - sta * 5) ){
                //有极大值点
                if ((autoLocate[i] > autoLocate[i - 1]) && (autoLocate[i] >= autoLocate[i + 1])) {
                    m = i;
                }
                //有极小值点
                if ((autoLocate[i] < autoLocate[i - 1]) && (autoLocate[i] <= autoLocate[i + 1])) {
                    n = i;
                }
            }
            if ( (m > 0) || ( n > 0 )){
                break;
            }
        }
        //根据极值点找到故障放电声波形起始点(定位光标所在位置)
        if ( m > 0 ) {
            //对于极大值，下坡到坡底   //GC20231206
            while (autoLocate[m] > autoLocate[m - 1]) {
                m = m - 1;
            }
            index = m;
            Log.e("test2", "m = " + m);
        }
        if ( n > 0 ) {
            //对于极小值，上坡到坡顶   //GC20231206
            while (autoLocate[n] < autoLocate[n - 1]) {
                n = n - 1;
            }
            index = n;
            Log.e("test2", "n = " + n);
        }
        if (index > 0) {
            position = index - 50;
            position = position - 50;   //GC20200313    自动算法光标定位国内习惯改进修正BUG
            timeDelay = position * 0.125;   //光标位置最终是在触发时刻后开始计算 //GC20231206
            Log.e("test", "点数 = " + position + " /ms = " + timeDelay);
        }

    }

    /*APP发送的蓝牙数据流
    ①控制命令——APP发送：共7个字节
    Device：设备地址，T-506为0x60  十进制96；
    Function：功能码，实现声音、磁场增益的调整和声音通带的选择
        0：声音/磁场增益调整
        1：声音通道选择低通
        2：声音通道选择带通
        3：声音通道选择高通
        4：声音通道选择全通
        5：触摸静音重置命令
        6：自动关机取消命令
        7：触摸静音打开命令
        8：触摸静音关闭命令
        9：音频蓝牙重置命令
    Control：控制声音和磁场的增益  （字节转换为位）（增益共有32阶：0~31）
        位7：声音/磁场的选择
            0：声音
            1：磁场
        位6~0：调整后的阶数
    Crc：占4个字节

    ②后续增加下发耳机MAC address——Device数据头0x61,Function和Control用来传数据*/

    /**
     * 1、下发低通控制命令     数字滤波100Hz-400Hz
     * 格式：①设备地址：96；②低通声音通道：1；③：无
     */
    public void clickDitong() {
        clickTongNum = 1;
        int[] ints = {96, 1, 0};

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
        //GC2.01.006 蓝牙重连功能优化
        Constant.CurrentFilterParam = request;
        sendCommand(request);

    }

    /**
     * 2、下发带通控制命令     数字滤波150Hz-600Hz
     */
    public void clickDaitong() {
        clickTongNum = 2;
        int[] ints = {96, 2, 0};

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
        //GC2.01.006 蓝牙重连功能优化
        Constant.CurrentFilterParam = request;
        sendCommand(request);

    }

    /**
     * 3、下发高通控制命令     数字滤波200Hz-1500Hz    （硬件改参数，实际效果10Hz-800Hz）
     */
    public void clickGaotong() {
        clickTongNum = 3;
        int[] ints = {96, 3, 0};

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
        //GC2.01.006 蓝牙重连功能优化
        Constant.CurrentFilterParam = request;
        sendCommand(request);

    }

    /**
     * 4、下发全通控制命令     数字滤波不处理曾经是100Hz-1500Hz  （硬件改参数，实际效果10Hz-800Hz）
     */
    public void clickQuantong() {
        clickTongNum = 0;
        int[] ints = {96, 4, 0};

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
        //GC2.01.006 蓝牙重连功能优化
        Constant.CurrentFilterParam = request;
        sendCommand(request);

    }

    /**
     * 5、下发触摸静音重置控制命令   //GC20191220
     */
    public void sendResetCommand() {
        //设备地址：“96”= 0x60；触摸重置控制命令：5,0
        int[] ints = {96, 5, 0};

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
        sendCommand(request);
    }

    /**
     * 6、下发自动关机取消命令（用于老化）   //GC20191221
     */
    public void sendAgingCommand() {
        //设备地址：“96”= 0x60；自动关机取消命令：6,0
        int[] ints = {96, 6, 0};

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
        sendCommand(request);
    }

    /**
     * 7、下发触摸静音打开命令   //GC20191221
     */
    public void sendTouchOnCommand() {
        //设备地址：“96”= 0x60；触摸静音打开命令：7,0
        int[] ints = {96, 7, 0};

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
        sendCommand(request);
    }

    /**
     * 8、下发触摸静音关闭命令   //GC20191221
     */
    public void sendTouchOffCommand() {
        //设备地址：“96”= 0x60；触摸静音关闭命令：8,0
        int[] ints = {96, 8, 0};
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
        sendCommand(request);
    }

    /**
     * 9、下发音频蓝牙重置控制命令    //GC20210630
     */
    public void sendReconnectCommand() {
        int[] ints = {96, 9, 0};

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
        sendCommand(request);

    }

    /**
     * 0、下发声音增益初始化命令
     */
    public void sendVoiceInitCommand() {
        //①设备地址：“96”= 0x60；②声音/磁场增益调整：0；③增益：阶数22
        int[] ints = {96, 0, 22};
        //计算CRC校验码
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
        //下发命令
        byte[] request = new byte[7];
        request[0] = (byte) ints[0];
        request[1] = (byte) ints[1];
        request[2] = (byte) ints[2];
        request[3] = (byte) integer1.intValue();
        request[4] = (byte) integer2.intValue();
        request[5] = (byte) integer3.intValue();
        request[6] = (byte) integer4.intValue();
        sendCommand(request);
    }

    /**
     * 01、下发声音增益命令  //GC20240227 （声音增益控制命令预留，还未启用）
     */
    public void sendVoiceCommand(int gainV) {
        //①设备地址：“96”= 0x60；②声音/磁场增益调整：0；③增益：声音“0”= 二进制0000 0000 + 阶数XX
        int[] ints = {96, 0, gainV};
        //计算CRC校验码
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
        //下发命令
        byte[] request = new byte[7];
        request[0] = (byte) ints[0];
        request[1] = (byte) ints[1];
        request[2] = (byte) ints[2];
        request[3] = (byte) integer1.intValue();
        request[4] = (byte) integer2.intValue();
        request[5] = (byte) integer3.intValue();
        request[6] = (byte) integer4.intValue();
        sendCommand(request);
    }

    /**
     * 0、下发磁场初始化控制命令93%
     */
    public void sendMagneticInitCommand() {
        //①设备地址：“96”= 0x60；②声音/磁场增益调整：0；③增益：磁场“128”= 二进制1000 0000 + 底层板子初始阶数22（70%）
//        int[] ints = {96, 0, 128 + 29}; //阶数为29（93%）    //GC20210730
        int[] ints = {96, 0, 128 + 27}; //阶数为27（93%/以29为最高换算）   //GC20220326
        gainM = 27; //GC20240227    磁场增益阶数的初始化值
        //计算CRC校验码
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
        //下发命令
        byte[] request = new byte[7];
        request[0] = (byte) ints[0];
        request[1] = (byte) ints[1];
        request[2] = (byte) ints[2];
        request[3] = (byte) integer1.intValue();
        request[4] = (byte) integer2.intValue();
        request[5] = (byte) integer3.intValue();
        request[6] = (byte) integer4.intValue();
        sendCommand(request);
    }

    /**
     * 01、下发磁场增益命令  //GC20240227
     */
    public void sendMagneticCommand(int gainM) {
        //①设备地址：“96”= 0x60；②声音/磁场增益调整：0；③增益：磁场“128”= 二进制1000 0000 + 底层板子初始阶数22（70%）
//        int[] ints = {96, 0, 128 + 29}; //阶数为29（93%）    //GC20210730
//        int[] ints = {96, 0, 128 + 27}; //阶数为27（93%/以29为最高换算）   //GC20220326
        int[] ints = {96, 0, 128 + gainM};     //GC20240227
        //计算CRC校验码
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
        //下发命令
        byte[] request = new byte[7];
        request[0] = (byte) ints[0];
        request[1] = (byte) ints[1];
        request[2] = (byte) ints[2];
        request[3] = (byte) integer1.intValue();
        request[4] = (byte) integer2.intValue();
        request[5] = (byte) integer3.intValue();
        request[6] = (byte) integer4.intValue();
        sendCommand(request);
    }

    /**
     * @param bytes 发送的控制命令
     * @return  发送控制命令的CRC码
     */
    public long getCommandCrcByte(int[] bytes) {
        return getCrc(bytes);
    }

    /**
     * @param ints  数据内容
     * @return  得到crc码
     */
    public long getCrc(int[] ints) {
        handle.postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, 2000);
        long nReg = Long.valueOf("4294967295");
        long integer = Long.valueOf("4294967295");
        for (int i = 0; i < ints.length; i++) {
            nReg = nReg ^ ints[i];
            for (int j = 0; j < 4; j++) {
                long a = nReg >> 24;
                long b = a & 255;
                long nTemp = crcTable[(int) b];
                nReg = (nReg << 8) & integer;
                nReg = nReg ^ nTemp;
            }
        }
        return nReg;
    }

    /**
     * @param command   设备控制命令
     */
    public void sendCommand(byte[] command) {
        if (!hasSentCommand) {
            System.arraycopy(command, 0, sendCommand, 0, command.length);
            if (bluetoothSocket == null) {
                Toast.makeText(this, getResources().getString(R.string.Bluetooth_is_not_connected), Toast.LENGTH_SHORT).show();
            }
            try {
                //蓝牙输出流
                OutputStream os = bluetoothSocket.getOutputStream();
                os.write(command);
                EventBus.getDefault().post(new SendCommandFinishEvent());
                hasSentCommand = true;
            } catch (IOException e) {
                //Toast.makeText(this, "发送失败" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(MainActivity.this, "还没有收到来自设备端的回复", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 下发MAC地址控制命令    //GC20210701
     */
    public void sendMAC(int[] ints) {
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
        sendCommand(request);

    }

    /**
     * 点击记忆按钮执行的方法
     */
    public void clickMemory() {
        isClickMem = true;
        System.arraycopy(voiceDraw, 0, compareDraw, 0, 400);
    }

    /**
     * 点击比较按钮执行的方法
     */
    public void clickCompare() {
        if (isClickMem) {
            isCom = !isCom;
        } else {
            Toast.makeText(this, getResources().getString(R.string
                    .You_have_no_memory_data_can_not_compare), Toast.LENGTH_SHORT).show();
        }
        myChartAdapterVoice.setmTempArray(voiceDraw);
        myChartAdapterVoice.setShowCompareLine(isCom);
        myChartAdapterVoice.setmCompareArray(compareDraw);
        myChartAdapterVoice.notifyDataSetChanged();
    }

    /**
     * 点击滤波按钮     方式选择 clickTongNum：低通=1；带通=2；高通=3；全通=0
     * @param llView    滤波对话框
     */
    protected void showFilterDialog(final LinearLayout llView) {
        customDialog = new CustomDialog(BaseActivity.this);
        customDialog.show();
        //对话框初始化
        switch (clickTongNum) {
            case 0:
                customDialog.clearFilter1();
                customDialog.rgFilter2.check(customDialog.rbQuanTong.getId());
                break;
            case 1:
                customDialog.clearFilter2();
                customDialog.rgFilter1.check(customDialog.rbDiTong.getId());
                break;
            case 2:
                customDialog.clearFilter2();
                customDialog.rgFilter1.check(customDialog.rbDaiTong.getId());
                break;
            case 3:
                customDialog.clearFilter1();
                customDialog.rgFilter2.check(customDialog.rbGaoTong.getId());
                break;
            default:
                break;
        }
        customDialog.setFilterVisible();
        customDialog.setTextGone();
        //监听滤波选项变化
        customDialog.setRadioGroup(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (customDialog.rbDiTong.getId() == checkedId) {
                    customDialog.clearFilter2();
                    customDialog.rgFilter1.check(customDialog.rbDiTong.getId());
                    currentFilter = 1;

                } else if (customDialog.rbDaiTong.getId() == checkedId) {
                    customDialog.clearFilter2();
                    customDialog.rgFilter1.check(customDialog.rbDaiTong.getId());
                    currentFilter = 2;

                } else if (customDialog.rbGaoTong.getId() == checkedId) {
                    customDialog.clearFilter1();
                    customDialog.rgFilter2.check(customDialog.rbGaoTong.getId());
                    currentFilter = 3;

                } else if (customDialog.rbQuanTong.getId() == checkedId) {
                    customDialog.clearFilter1();
                    customDialog.rgFilter2.check(customDialog.rbQuanTong.getId());
                    currentFilter = 0;

                }
            }
        });
        //点击按键执行的事件
        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentFilter) {
                    case 0:
                        Constant.filterType = currentFilter;
                        clickQuantong();
                        break;
                    case 1:
                        Constant.filterType = currentFilter;
                        clickDitong();
                        break;
                    case 2:
                        Constant.filterType = currentFilter;
                        clickDaitong();
                        break;
                    case 3:
                        Constant.filterType = currentFilter;
                        clickGaotong();
                        break;
                    default:
                        break;
                }
                customDialog.dismiss();

            }
        });
        customDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();

            }
        });
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                llView.setClickable(true);
            }
        });

    }

    /**
     * 点击耳机按钮
     * @param llView    耳机查找对话框     //GC20210712
     */
    protected void showHeadphonesDialog(final LinearLayout llView) {
        customDeviceListDialog = new CustomDeviceListDialog(BaseActivity.this);
        customDeviceListDialog.show();

        //注册接收查找到设备action接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(customDeviceListDialog.broadcastReceiver, filter);
        //注册查找结束action接收器
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(customDeviceListDialog.broadcastReceiver, filter);

        customDeviceListDialog.setLeftButton(getString(R.string.find_device), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDeviceListDialog.doDiscovery();

            }
        });
        customDeviceListDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDeviceListDialog.dismiss();
            }
        });
        customDeviceListDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //关闭服务查找
                if (bluetoothAdapter != null) {
                    bluetoothAdapter.cancelDiscovery();
                }
                if (customDeviceListDialog.getAddress) {
                    //将得到MAC地址发送给主板进行耳机连接   //GC20210714
                    sendAddress(customDeviceListDialog.headphonesAddress);
                    customDeviceListDialog.getAddress = false;
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.waiting_link), Toast.LENGTH_SHORT).show();
                    handle.sendEmptyMessage(HEADPHONES_DISCONNECT_STATE);
                }
                llView.setClickable(true);
            }
        });

    }

    /**
     * 下发耳机MAC地址    //GC20210706
     */
    public void sendAddress(String neededAddress) {
//        String neededAddress = "5CC6E918D15A";    读取的地址字符串“20:21:03:02:18:51”
        address = neededAddress.getBytes();
        //使用定时器将需要的MAC地址间隔开发送     发送MAC地址命令开头：0x61
        sendAddress[0] = 97;
        sendTimer.start();
    }

    /**
     * 倒计时处理    //GC20210706     //GT20210705
     */
    int count = 0;
    int i = 1;
    private CountDownTimer sendTimer = new CountDownTimer(1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            for (; i < 3; i++) {
                sendAddress[i] = address[count];
                count++;
            }
            if (i == 3) {
                i = 1;
                //字符串里面的“：”符号不要
                count++;
                sendMAC(sendAddress);
                sendTimer.start();
            }
            //地址字符串转为的BYTE长度为17，计数至18停止取消计数
            if (count == 18) {
                count = 0;
                sendTimer.cancel();
            }
        }

    };

    /**
     * 声音增益阶数和百分比的转化 100转32
     */
    public int b2s(int b) {
        int s;
        float v = (float) b / 100.0f;
        float v1 = v * 32;
        s = (int) v1;
        s = Math.round(v1);   //改为四舍五入，之前不准确  //GC20240227
        return s;
    }

    /**
     * 磁场增益阶数和百分比的转化 100转29
     */
    public int b2sM(int b) { //GC20220326
        int s;
        float v = (float) b / 100.0f;
        float v1 = v * 29;
//        s = (int) v1;
        s = Math.round(v1); //改为四舍五入，之前不准确  //GC20240227
        gainM = s;  //GC20240227    记录调整进度条后磁场增益下发的阶数
        return s;
    }

    /**
     * 29转100
     */
    public int s2bM(int s) {
        int b;
        float v = (float) s / 29.0f;
        float v1 = v * 100;
//        b = (int) v1;
        b = Math.round(v1); //改为四舍五入，之前不准确  //GC20240227    磁场阶数变化后转化为百分比
        return b;
    }

    /**
     * 32转100
     */
    public int s2b(int s) {
        int b;
        float v = (float) s / 32.0f;
        float v1 = v * 100;
        b = (int) v1;
        b = Math.round(v1); //改为四舍五入，之前不准确  //GC20240227
        return b;
    }

    /**
     * 隐藏虚拟按键，并且全屏  //GC20200519
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            isExit = true;
            // 关闭并释放资源
            mAudioTrack.release();
            try {
                bluetoothSocket.close();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();

    }

}

/*调试记录*/
//        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
//        Date curDate = new Date(System.currentTimeMillis());  //获取当前时间
//        String str = formatter.format(curDate);
//        Log.e("TAG4playSound", str);
//GT20180321 蓝牙输入流解读
//GT20200309    声音数据转化和播放
//GT20200402    查看播放的音量数据
//GC20230601    位数修改后播放效果测试
//GC20231019    放大处理方式①：每一包声音数据直接放大
//GC20231020    放大处理方式②：触发后的几个包合并后再放大------>结论：会有延时
//GC20231021    放大处理方式③：触发后的几个包才放大
//GC20231022    最小极小值和第二极小值显示
//GC20231024    UI屏蔽声音数字放大处理调试部分

//G?    增益下发 / 保存生成的model
//GT20210705    延时操作运用

/*更改记录*/
//GC20171205    添加探头相对电缆位置的左右方向判断显示
//GC20180412    截取800个点的声音数据并计算其特征值
//GC20180428    画声音波形的位置改变，提前50个点————后恢复//GC20200103
//GC20180504    训练声音特征生成model   //G?是否不用线程
//GC20180707    声音特征值归一化计算修改
//GC20181113    增益进度条高度显示优化
//GC20181119    添加相关判断
//GC20181201    声音自动定位算法优化

//GC2.01.005————界面无缝切换，光标绘制更改，添加硬件关闭重连功能
//GC20190215    界面无缝切换
//GC20190216    界面光标绘制情况修改
//GC20190218    专家界面延时值显示和相关结果一致起来（通过相关后计时自动定位数值不一致也不刷新延时值）
//GC20190307    提示框词条和左右位置闪烁一次效果
//GC2.01.006————硬件关闭重连功能优化
//GC20190407    硬件关闭重连功能添加和优化
//GC2.01.007————添加故障提示音和重连提示
//GC20190422    "发现故障"提示音添加
//GC20190613    蓝牙重新连接后，提示框提示语更新
//GC2.01.008————用户界面布局修改，稳定无bug版本
//GC20190625    用户界面发现故障UI提示更改 （动画改为根据延时值改变大小的圆圈）
//GC2.01.009————用户界面布局修改，故障判断逻辑优化
//GC20190717    用户界面布局修改（去掉最小延时值、改变当前、上次延时值显示位置等）
//GC20190720    延时值显示逻辑bug修改
//GC20190724    去掉圆圈动画，与词条跳动效果一致
//GC2.01.010————多平台平板适配
//GC20191011    设置用户界面探头的左右位置
//GC2.01.011————多平台平板适配
//GC20191220    触摸静音状态判断和显示
//GC20191221    命令预留
//GC2.02.012————各种使用反馈优化
//GC20200103    国内习惯适配（声音波形触发时刻前去掉、用户专家界面模式按钮更改）
//GC20200313    自动算法光标定位国内习惯改进修正BUG
//GC20200114    重连操作延时原因——设备与APP重新连接时有一部分无用的空数据，需要处理掉 ③最终处理
//GC20200409    “暂停”状态时“左右”位置不刷新
//GC20200410    1分钟无触发信号界面提示调整      //GC20200728 后续优化
//GC20200417    提示音改为“叮叮”，提示间隔拉长
//GC20200519    播放暂停效果改进 / （如有虚拟按键，屏蔽）尝试
//GC2.02.013————蓝牙耳机，浅色主题，初始化控制命令BUG
//GC20210630    下发音频蓝牙重置控制命令
//GC20210701    下发MAC地址控制命令
//GC20210703    浅色主题
//GC20210706    下发耳机MAC地址
//GC20210714    利用对话框查找需要的蓝牙耳机MAC地址，然后下发给主板操作  /  蓝牙耳机状态反馈
//GC20210830    暂停时点击“切换界面”按钮取消暂停，恢复至播放状态
//GC20210730    磁场增益缺省值改为93%（阶数29，增益阶数0-32）
//GC20211229    优化根据“标记位”数值来判断主机探头相对电缆位置

/**
 * //                       _ooOoo_
 * //                      o8888888o
 * //                      88" . "88
 * //                      (| -_- |)
 * //                       O\ = /O
 * //                   ____/`---'\____
 * //                 .   ' \\| |// `.
 * //                  / \\||| : |||// \
 * //                / _||||| -:- |||||- \
 * //                  | | \\\ - /// | |
 * //                | \_| ''\---/'' | |
 * //                 \ .-\__ `-` ___/-. /
 * //              ______`. .' /--.--\ `. . __
 * //           ."" '< `.___\_<|>_/___.' >'"".
 * //          | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //            \ \ `-. \_ __\ /__ _/ .-` / /
 * //    ======`-.____`-.___\_____/___.-`____.-'======
 * //                       `=---='
 * //
 * //    .............................................
 * //             佛祖保佑             永无BUG
 * =====================================================
 */
//GC2.02.014————
//GC20220326    磁场线圈变大，增益最大阶数32和29变换的位置，目前29————————————版本调节①   2.02.014（阶数最高29）/ 2.02.013（阶数最高32）
//GC20220407    蓝牙耳机按钮隐藏位置———————————————————————————版本调节②      2.02.014
//GC20220510    触发灯闪烁效果优化，改正亮起后不变灰的BUG
//GC20220520    显示版本号
//GC2.02.015————
//GC20220620    seekBar更换
//GC20220708    改提示为“接近故障点”/去掉箭头波形显示区箭头
//GC20220709    长按设置“耳机”按钮调出

//GC2.02.016————
//GC20220727    小米重连BUG解决

//GC2.02.017————
//GC20220801    国外无协助功能、初始配对界面优化
//GC20220823    用户界面增益控制颜色修改

//GC20230531————————设备适配型号 平板设备为Q802 / 平板设备为Q86————————

/*————————智能算法改进————————*/
//GC20231108    SVM算法相关
//GC20231115    SVM算法错误及改进
//GC20231118    解码数据错误 超出0到4095 范围 12位AD 2^12  APP未修正解码错乱问题

//GC20231120    新特征值554组添加
//GC20231130    显示相关系数
//GC20231201    算法给出结果的逻辑（先SVM预测，后相关判断，第一次SVM预测是的时候先显示磁场触发）

//GC20231204    相关后无论结果如何显示相关系数 / 调整相关参数阈值，优化最终结果显示
//GC20231205    刷新波形时显示数据是否错乱显示——————————————————————————————（待修改）
//GC20231206    新特征值609组添加 / 光标定位重新计算
//GC20231209    延时显示逻辑优化 /  自动定位光标位置显示  //GC20231225
/*————————智能算法改进————————*/

//GC20231207    网络框架相关  服务器
//GC20231215    版本
//GC20231224    系统时间时间获取
//GT20240301    命令发送失败 屏蔽测试     旧尝试：//GC20210707    命令发送错误BUG改进试验
/*————————用户界面升级，底板程序有修改，兼容旧底板程序————————*/
//GC20240222    水平线圈磁场数据接收：截取和解码
//GC20240223    用户界面磁场进度条UI改为绿色进度条
//GC20240226    磁场增益大小判断处理
//GC20240227    磁场增益自动调整：下发磁场增益命令 / 增益百分比和阶数转化bug
//GN20240227    “左右”方向判断BUG修改   //GC20240225    不用了程序段整理
//GC20240228    长时间不触发磁场增益调到初始阶数27（93%）
//GT20240228    2路磁场线圈数据切换、最大值数据查看、强度值观察
//GC20240229    根据增益阶数和最大值判断强度
//GC20240304    添加位置显示
//GT20240304    磁场增益加减改为只有一档
//GT20240307    增益大小判断去除前50个点
/*————————用户界面升级，底板程序有修改，兼容旧底板程序————————*/

//GN20240306    屏蔽协助

//GT20240308    声音显示水平磁场数据
//GN20240305    磁场数据显不用解码ADPCM编码
//GT20240309    赋值为2048  避免影响自动增益