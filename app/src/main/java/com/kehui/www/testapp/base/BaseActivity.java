package com.kehui.www.testapp.base;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
import com.kehui.www.testapp.event.StartReadThreadEvent;
import com.kehui.www.testapp.event.UiHandleEvent;
import com.kehui.www.testapp.ui.CustomDialog;
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
import java.util.Random;
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

    /**
     * 声音播放部分
     */
    public AudioManager audioManager;
    public AudioTrack mAudioTrack;

    /**
     * 蓝牙相关部分
     */
    BluetoothDevice device = null;
    BluetoothSocket socket = null;
    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    public BluetoothSocket blueSocket;
    private boolean needReconnect;

    /**
     * 发送蓝牙数据
     */
    public byte[] sendCommand;
    public long[] crcTable;
    public boolean hasSentInitCommand;
    public boolean hasSentCommand;

    /**
     * 接收蓝牙数据
     */
    public InputStream inputStream;
    public int[] stream;
    public int streamLength;
    public int streamCount;
    public int[] blueStream;
    public int blueStreamLen;
    public boolean hasGotStream;
    public boolean handleStream;
    public int[] streamLeft;
    public int leftLen;
    public boolean hasLeft;

    public int seekBarType;
    public int[] seekBarVoiceInt;
    public int[] seekBarMagneticInt;
    public boolean isExit;     //是否退出软件的标志
    public boolean mShengyinFlag;   //是否开始获取声音包的标志
    public int mShengyinMarkNum;    //GN 触发时刻数据点所在的位置
    public int mShengyinCount;      //GN 触发后获取声音包的个数
    public int[] mShengyinArray;
    public int[] voiceDraw;
    public int[] compareDraw;
    public int[] magneticArray;
    public int[] magneticDraw;
    public boolean isDraw;

    /**
     * 解密需要的解析常量数组
     */
    public int[] indexTable = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
    public int[] stepSizeTable = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31,
            34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190,
            209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963,
            1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660,
            4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635,
            13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};

    /**
     * View布局
     */
    public MyChartAdapterBase myChartAdapterVoice;
    public MyChartAdapterBase myChartAdapterMagnetic;
    public int streamVolumeNow;
    public boolean isSilence;
    public boolean isClickMem;
    public boolean isCom;

    /**
     * 自定义对话框   *低通1 *带通2 *高通3 *全通0
     */
    private CustomDialog customDialog;
    public int clickTongNum;
    public int currentFilter;

    /**
     * 用户界面增益进度条显示  //GC20181113
     */
    public int maxVoice;
    public int maxMagnetic;

    /**
     * 声音特征训练
     */
    private double[] readFeature = new double[2000];
    private boolean svmTrainThread;
    private svm_model model;

    /**
     * 声音智能识别
     */
    private int[] tempVoice;
    private int[] svmData;
    private int[] autoLocate;
    private double[] feature;
    private double[] normalization;
    private double[] normalization2;

    /**
     * 相关部分   //GC20181119
     */
    public int svmPredictCount;
    public int isRelatedCount;
    public boolean firstFind;
    private double p = 0;
    private int cursorPosition;
    private double timeDelay;

    /**
     *  提示音功能添加 //GC20190422
     */
    public SoundUtils soundSystem;

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
    public static final int LINK_RECONNECT  = 10;
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
        initData();
        setAudioTrack();
        getFeaturexData();
        //启动训练声音特征的线程
        svmTrain.start();
        getCrcTable();
        //获取蓝牙数据
        startThread();
        handleBlueStream.start();   //GN 处理蓝牙数据的线程
        EventBus.getDefault().register(this);

    }

    /**
     * 初始化
     */
    private void initData() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        blueSocket = MyApplication.getInstances().get_socket();

        sendCommand = new byte[7];
        //缓存的蓝牙数据
        stream = new int[1024000];
        streamLength = 0;
        //缓存数据包含的输入流个数
        streamCount = 0;
        //需要处理的蓝牙数据
        blueStream = new int[1024000];
        blueStreamLen = 0;
        //将蓝牙数据分包后的剩余数据
        streamLeft = new int[59];
        leftLen = 0;

        //seekBar控制情况 磁场（1）声音（2）
        seekBarType = 0;
        seekBarVoiceInt = new int[]{22, 22};
        seekBarMagneticInt = new int[]{22, 22};
        isExit = false;
        mShengyinFlag = false;
        mShengyinMarkNum = 0;
        mShengyinCount = 0;
        mShengyinArray = new int[500];
        new Random();
        isDraw = true;
        voiceDraw = new int[400];
        magneticArray = new int[400];
        magneticDraw = new int[400];
        compareDraw = new int[400];
        isClickMem = false;
        isCom = false;
        //初始化滤波方式为全通
        clickTongNum = 0;

        tempVoice = new int[900];
        svmData = new int[800];
        autoLocate = new int[800];
        feature = new double[4];
        //归一化的声音数据
        normalization = new double[800];
        normalization2 = new double[800];

        //连续预测为故障声的次数
        svmPredictCount = 0;
        //连续相关的次数
        isRelatedCount = 0;
        firstFind = true;

    }

    /**
     * 设置音频播放工具
     */
    public void setAudioTrack() {
        int minBufferSize = AudioTrack.getMinBufferSize(8000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        //Log.e("Size", "minBufferSize:" + minBufferSize);    //GT20171129  内部的音频缓冲区的大小 输出结果1392
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, //GN当前应用使用的哪一种音频管理策略
                // STREAM_ALARM：警告声
                // STREAM_MUSCI：音乐声，例如music等
                // STREAM_RING：铃声
                // STREAM_SYSTEM：系统声音
                // STREAM_VOCIE_CALL：电话声音
                8000,// 设置音频数据的采样率
                AudioFormat.CHANNEL_OUT_MONO,   //GN单通道
                AudioFormat.ENCODING_PCM_16BIT, //GN数据位宽
                //minBufferSize * 6, AudioTrack.MODE_STREAM);   //GC20171129 减少350ms左右延时
                minBufferSize / 6, AudioTrack.MODE_STREAM);
        //GN手动计算一帧“音频帧”（Frame）的大小（12） int size = 采样率 x 位宽 x 采样时间 x 通道数
        //GN播放模式
        // AudioTrack中有MODE_STATIC和MODE_STREAM两种分类。
        // STREAM方式表示由用户通过write方式把数据一次一次得写到audiotrack中。
        // 这种方式的缺点就是JAVA层和Native层不断地交换数据，效率损失较大。
        // 而STATIC方式表示是一开始创建的时候，就把音频数据放到一个固定的buffer，然后直接传给audiotrack，
        // 后续就不用一次次得write了。AudioTrack会自己播放这个buffer中的数据。
        // 这种方法对于铃声等体积较小的文件比较合适。
        mAudioTrack.play();

    }

    /**
     * 从assets文件夹中获取声音特征的数据 //GC20180504 注意读取文本文件的编码格式为ANSI
     */
    private void getFeaturexData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "feature.txt");
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
    private void voiceSvmTrain(double[] readFeature) {
        svm_problem sp = new svm_problem();
        svm_node[][] x = new svm_node[500][4];
        int k = 0;
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 4; j++, k++) {
                x[i][j] = new svm_node();
                x[i][j].index = j + 1;
                x[i][j].value = readFeature[k];
            }
        }
        double[] labels = new double[500];
        for (int i = 0; i < 500; i++) {
            if (i <= 222) {
                labels[i] = 1;
            } else {
                labels[i] = -1;
            }
        }
        sp.x = x;       //训练数据
        sp.y = labels;  //训练数据的类别
        sp.l = 500;     //训练数据的个数
        svm_parameter prm = new svm_parameter();
        prm.svm_type = svm_parameter.C_SVC;     //GN SVM的类型
        /*GN 分类问题(包括C-SVC、n-SVC)、回归问题(包括e-SVR、n-SVR)以及分布估计(one-class-SVM )*/
        prm.kernel_type = svm_parameter.RBF;     //GN 核函数
        /*GN LINEAR：线性核函数、POLY:多项式核函数、RBF:径向机核函数、SIGMOID: 神经元的非线性作用函数核函数*/
        //prm.degree = 3; //for poly (默认3)
        //prm.coef0 = 0;  //for poly/sigmoid (默认0)
        prm.gamma = 0.5;  //for poly/rbf/sigmoid (默认1/k) 默认大小 7.9
        prm.cache_size = 1024;      //训练所需的内存 inMB
        prm.eps = 1e-3;          //stopping criteria 设置允许的终止判据(默认0.001)
        prm.C = 1000;          //for C_SVC, EPSILON_SVR and NU_SVR 惩罚因子(损失函数)(默认1) 0.1 全部   10 270
        //prm.nr_weight = 0;           //for C_SVC 设置第几类的参数C为weight*C(C-SVC中的C)
        //prm.weight_label = null;    //for C_SVC
        //prm.weight = null;           //for C_SVC
        //prm.nu = 0.5;            //for NU_SVC, ONE_CLASS, and NU_SVR(默认0.5)表示防止过拟合，容忍误差的程度，可以通过调节这个会改变训练出来超平面的位置
        //prm.p = 0.1;             //for EPSILON_SVR (默认0.1)
        //prm.shrinking = 1;      //是否使用启发式，0或1(默认1)
        //prm.probability = 0;
        model = svm.svm_train(sp, prm);

    }

    /**
     * 从assets文件夹中获取crctable的数据
     */
    public void getCrcTable() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "crctable.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int c;
        //读取bis流中的下一个字节
        try {
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
     * @param event 蓝牙重连事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StartReadThreadEvent event) {
        //GC20190613
        Message message = new Message();
        message.what = LINK_RECONNECT;
        handle.sendMessage(message);
        Toast.makeText(this, getResources().getString(R.string.connect) + " " + event.device + " " + getResources().getString(R.string.success),
                Toast.LENGTH_SHORT).show();
        hasGotStream = false;
        startThread();

    }

    /**
     * 获取蓝牙数据
     */
    private void startThread() {
        try {
            //进入演示模式后打开仪器依旧可以正常连接
            blueSocket = MyApplication.getInstances().get_socket();
            if (blueSocket != null) {
                //通过蓝牙socket获得输入流
                inputStream = blueSocket.getInputStream();
            }
        } catch (IOException e) {
            Toast.makeText(this, getResources().getString(R.string
                    .Can_not_get_input_stream_via_Bluetooth_socket), Toast.LENGTH_SHORT).show();
            handle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            e.printStackTrace();
        }
        if (!hasGotStream) {
            //重连时下发当前的参数命令  //GC2.01.006 蓝牙重连功能优化
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
            //启动获取蓝牙数据的线程
            new Thread(getStream).start();
            hasGotStream = true;
        }

    }

    /**
     * 获取蓝牙数据的线程
     */
    Runnable getStream = new Runnable() {
        @Override
        public void run() {
            int len;
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
                        //Log.e("stream", "len:" + len + "时间" + System.currentTimeMillis());     //GT20180321 每个输入流的的长度
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
                        streamCount++;
                        //在不处理数据时缓存数个输入流    //GC20171129
                        if (streamCount >= 5 && !handleStream) {
                            if (hasSentInitCommand) {
                                /*//超过590滤除
                                if (streamLength > 590) {
                                    streamLength = 590;
                                }*/     //GC20190627 触发灯闪烁bug原因
                                if (streamLength >= 0) {
                                    System.arraycopy(stream, 0, blueStream, 0, streamLength);
                                }
                                blueStreamLen = streamLength;
                                //Log.e("stream", "lenSum:" + blueStreamLen);  //GT20180321 要处理的蓝牙数据的长度
                                handleStream = true;

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
                            streamCount = 0;
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

                    //当前连接状态为断开 //GC2.01.006 蓝牙重连功能优化
                    Constant.BluetoothState = false;
                    //重置变量
                    stream = null;
                    stream = new int[1024000];
                    streamLength = 0;
                    streamCount = 0;
                    blueStream = null;
                    blueStream = new int[1024000];
                    blueStreamLen = 0;
                    handleStream = false;
                    streamLeft = null;
                    streamLeft = new int[59];
                    leftLen = 0;
                    hasLeft = false;

                    //GC20190407 启动重连
                    needReconnect = false;
                    Message message = new Message();
                    message.what = LINK_LOST;
                    handle.sendMessage(message);
                    //启动重连线程，是否需要单独线程控制，需要观察
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!needReconnect) {
                                //Log.e("蓝牙测试", "connectThread线程，尝试连接");
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
     * 尝试重新连接蓝牙
     */
    public void reconnect() {
        //读取设置数据
        SharedPreferences sharedata1 = getSharedPreferences("Add", 0);
        String address = sharedata1.getString(String.valueOf(1), null);
        //得到蓝牙设备句柄
        device = bluetooth.getRemoteDevice(address);
        //用服务号得到socket
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            MyApplication.getInstances().set_socket(socket);
            MyApplication.getInstances().set_device(device);
            MyApplication.getInstances().set_bluetooth(bluetooth);

        } catch (IOException e) {
//            Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();
        }
        //连接socket
        try {
            //GC2.01.007
            if (socket != null) {
                socket.connect();
                needReconnect = true;
                EventBus.getDefault().post(new StartReadThreadEvent(device.getName()));
            }

        } catch (IOException e) {
            try {
                socket.close();
                socket = null;
                //Log.e("蓝牙测试", "connectThread线程，走到异常");
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }

    /*蓝牙控制命令——客户端发送：共7个字节
    Device：设备地址，T-506为0x60  十进制96
    Function：功能码，实现声音、磁场增益的调整和声音通带的选择
        0：声音/磁场增益调整
        1：声音通道选择低通
        2：声音通道选择带通
        3：声音通道选择高通
        4：声音通道选择全通
    Control：控制声音和磁场的增益  （字节转换为位）（增益共有32阶：0~31）
        位7：声音/磁场的选择
            0：声音
            1：磁场
        位6~0：调整后的阶数
    Crc：占4个字节*/
    /*蓝牙控制命令——设备端响应：共7个字节
    第3个字节Respond：响应值，命令是否响应
        0：未响应
 	    1：已响应*/

    /**
     * 发送声音初始化控制命令
     */
    public void sendVoiceInitCommand() {
        //设备地址：“96”= 0x60；声音/磁场增益调整：0；控制增益：声音0、阶数22
        int[] ints = {96, 0, 22};
        long l = getCommandCrcByte(ints);

        String s = Long.toBinaryString((int) l);
        StringBuilder ss = new StringBuilder();
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
        sendCommand(request);
    }

    /**
     * 发送磁场初始化控制命令
     */
    public void sendMagneticInitCommand() {
        //设备地址：96”= 0x60；声音/磁场增益调整：0；控制增益：声音“128”= 二进制1000 0000、阶数22。
        int[] ints = {96, 0, 128 + 22};
        long l = getCommandCrcByte(ints);

        String s = Long.toBinaryString((int) l);
        StringBuilder ss = new StringBuilder();
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
        sendCommand(request);
    }

    //对发送的控制命令进行CRC校验
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
            if (blueSocket == null) {
                Toast.makeText(this, getResources().getString(R.string.Bluetooth_is_not_connected),
                        Toast.LENGTH_SHORT).show();
            }
            try {
                //蓝牙输出流
                OutputStream os = blueSocket.getOutputStream();
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
     * 处理蓝牙数据的线程
     */
    Thread handleBlueStream = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (handleStream) {
                    doBlueStream(blueStream, blueStreamLen);
                    handleStream = false;
                }
            }
        }

    });

    /**
     * @param temp 需要处理的蓝牙数据    //G?
     * @param tempLength   数据长度
     */
    private void doBlueStream(int[] temp, int tempLength) {
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
                //找数据头（0x53：声音  0x4d：磁场  0x60：T-506）
                if (streamLeft[i1] == 83 || streamLeft[i1] == 77 || streamLeft[i1] == 96) {
                    for (int i2 = 0, j = i1; i2 < 59; i2++, j++) {
                        if (j >= 59) {
                            receivedBean[i2] = temp[i + j - leftLen];
                        } else {
                            //截取数据包
                            receivedBean[i2] = streamLeft[j];
                        }
                        boolean isCrc = doTempCrc(receivedBean);
                        if (isCrc) {
                            //判断为数据包数据
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
                                if (isCrc2) {
                                    //判断为控制命令
                                    hasSentCommand = false;
                                    i1 += 6;
                                    if (receivedCommand[2] != 1) {
                                        //Respond：响应值为0，命令未响应
                                        handle.sendEmptyMessage(SEND_ERROR);
                                        EventBus.getDefault().post(new SendCommandNotRespondEvent());
                                        seekBarType = 0;
                                    } else if (receivedCommand[1] == sendCommand[1]) {
                                        //接収的命令内容与要发送的命令内容一致
                                        seekBarType = 0;
                                        handle.sendEmptyMessage(SEND_SUCCESS);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            hasLeft = false;
        }
        //开始遍历
        for (; i < tempLength - 59; i++) {
            if (temp[i] == 83 || temp[i] == 77 || temp[i] == 96) {
                for (int j = i, k = 0; j < (i + 59); j++, k++) {
                    //截取数据包
                    receivedBean[k] = temp[j];
                }
                boolean isCrc = doTempCrc(receivedBean);
                if (isCrc) {
                    //判断为数据包数据
                    doTempBean(receivedBean);
                    i += 58;
                } else {
                    if (temp[i] == 96) {
                        for (int j = i, k = 0; j < (i + 7); j++, k++) {
                            //截取控制命令
                            receivedCommand[k] = temp[j];
                        }
                        boolean isCrc2 = doTempCrc2(receivedCommand);
                        if (isCrc2) {
                            //判断为控制命令
                            hasSentCommand = false;
                            if (receivedCommand[2] != 1) {
                                //Respond：响应值，命令未响应
                                handle.sendEmptyMessage(SEND_ERROR);
                                EventBus.getDefault().post(new SendCommandNotRespondEvent());
                                seekBarType = 0;
                            } else if (receivedCommand[1] == sendCommand[1]) {
                                //接収的命令内容与要发送的命令内容一致
                                seekBarType = 0;
                                handle.sendEmptyMessage(SEND_SUCCESS);
                            }
                            i += 6;
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

    /*数据包结构：
    (1)	S/M：声音、磁场数据的选择
            0x53：声音
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
    (4)	Predsample：解码数据（由两个字节组成，字节4为高8位，字节5为低8位）
    (5)	Date：声音编码数据
    (6)	Crc：循环冗余校验码*/
    /**
     * @param tempBean  对59个数据进行bean对象的处理
     */
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
        packageBean.setDate(date);
        doPackageBean(packageBean);
    }

    /**
     * @param packageBean   解析packageBean并对其进行相应的操作
     */
    public void doPackageBean(PackageBean packageBean) {
//        Log.e("FILE", "packageBean:" + packageBean.toString());
        int[] results = decodeData(packageBean);
        //SM: “83”=0x53 代表声音    “77”= 0x4d 代表磁场
        if (packageBean.getSM() == 83) {
            int mark = packageBean.getMark();
            //Mark最高位是1，代表仪器在这一包内触发，组成放电声音的第一个包的有效数据
            if (binaryStartsWithOne(mark)) {
                //触发灯变红
                Message msg = new Message();
                msg.what = LIGHT_UP;
                handle.sendMessage(msg);
                //开始截取声音包
                mShengyinFlag = true;
                //Mark后7位，触发时刻数据点所在的位置
                mShengyinMarkNum = getMarkLastSeven(mark);
                for (int i = 0, j = mShengyinMarkNum; j < 100; i++, j++) {
                    mShengyinArray[i] = results[j];
                    //GC20180412 凑足触发时刻之前100点的数据1
                    tempVoice[i] = tempVoice[j];
                }
                //GC20180412 凑足触发时刻之前100点的数据2
                for (int i = 100 - mShengyinMarkNum, j = 0; j < 100; i++, j++) {
                    tempVoice[i] = results[j];
                }
                //获取声音包的个数
                mShengyinCount++;
            } else {
                if (mShengyinFlag) {
                    //已经开始获取声音包
                    if (mShengyinCount <= 7) {
                        //声音识别需要9个声音包 4
                        for (int i = 100 + mShengyinCount * 100 - mShengyinMarkNum , j = 0; j < 100; i++, j++) {
                            tempVoice[i] = results[j];
                        }
                        if (mShengyinCount <= 4) {
                            //画波形需要5个声音包
                            for (int i = mShengyinCount * 100 - mShengyinMarkNum, j = 0; j < 100; i++, j++) {
                                mShengyinArray[i] = results[j];
                            }
                        }
                        mShengyinCount++;
                    } else {
                        mShengyinFlag = false;
                        mShengyinCount = 0;
                    }
                } else {
                    //不获取声音包
                    for (int i = 0, j = 0; j < 100; i++, j++) {
                        //GC2018412 先缓存一包，用于找出声音识别的前100个点
                        tempVoice[i] = results[j];
                    }
                }
            }
            playSound(results);

        } else if (packageBean.getSM() == 77) {
            //探头相对电缆位置的判断   Mark "128"= 10000000 代表位7是1 //GC20171205
            if (packageBean.getMark() >= 128) {
                handle.sendEmptyMessage(POSITION_RIGHT);
                //令位7为0
                packageBean.setMark(packageBean.getMark() - 128);
            } else {
                handle.sendEmptyMessage(POSITION_LEFT);
            }
            //按照顺序（00 01 10 11）将磁场数据拼接起来，1包含有100个数据点
            for (int i = 0, j = packageBean.getMark() * 100; i < 100; i++, j++) {
                magneticArray[j] = results[i];
            }
            //4个磁场包拼接完成，开始画主界面波形
            if (packageBean.getMark() == 3) {
                //“同步指示” 灯变灰
                Message msg = new Message();
                msg.what = TRIGGERED;
                handle.sendMessage(msg);
                if (isDraw) {
                    for (int i = 0; i < 400; i++) {
                        magneticDraw[i] = magneticArray[i];
                        //找寻磁场信号幅值最大点用于用户界面画进度条高度   //GC20181113
                        magneticArray[i] = magneticArray[i] - 2048;
                        if (magneticArray[i] > 2047) {
                            magneticArray[i] = 2047;
                        } else if (magneticArray[i] < -2048) {
                            magneticArray[i] = -2048;
                        }
                        magneticArray[i] = Math.abs(magneticArray[i]);
                        if (magneticArray[i] > maxMagnetic) {
                            maxMagnetic = magneticArray[i];
                        }
                    }
                    //GC20180428 声音波形加上触发前50个点的数据   //voiceDraw[i] = mShengyinArray[i];
                    System.arraycopy(tempVoice, 50, voiceDraw, 0, 400);
                    for (int i = 0; i < 800; i++) {
                        //用于计算特征值的声音数据  //GC20180412
                        svmData[i] = tempVoice[i];
                        //用于自动定位的声音数据   //GC20181201
                        autoLocate[i] = tempVoice[i];
                    }
                    //获取声音特征
                    obtainFeaturex();
                    //根据声音特征预测
                    if (svmTrainThread) {
                        voiceSvmPredict(feature);
                    }
                }
                Message message = new Message();
                message.what = WHAT_REFRESH;
                handle.sendMessage(message);
            }
        }

    }

    //解密数据
    public int[] decodeData(PackageBean bean1) {
        int index = bean1.getIndex();
        int[] predsample = bean1.getPredsample();
        int predsample1 = predsample[0];
        int predsample2 = predsample[1];
        int pred = predsample1 * 256 + predsample2;
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

    //二次解密
    public int[] decodeDataSecond(int index, int pred, int[] dateArray) {
        int prevsample = pred;
        int previndex = index;
        int PREDSAMPLE = 0;
        int INDEX = 0;
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

            if (PREDSAMPLE > 4095) {
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
     * @return  判断二进制的最高位是否是1   //G?
     */
    public boolean binaryStartsWithOne(int tByte) {
        String tString = Integer.toBinaryString((tByte & 0xFF) + 0x100).substring(1);
        return tString.startsWith("1");
    }

    //获取mark的后7位
    public Integer getMarkLastSeven(int mark) {
        String tString = Integer.toBinaryString((mark & 0xFF) + 0x100).substring(1);
        String substring = tString.substring(1, tString.length());
        return Integer.valueOf(substring, 2);
    }

    /**
     * @param results   声音数据
     */
    public void playSound(int[] results) {
        byte[] bytes = new byte[results.length * 2];
        for (int i = 0; i < results.length; i++) {
            short sh = (short) ((results[i] - 2048) * 16);
            byte[] bytes1 = shortToByte(sh);
            //Log.e("FILE", "byte.length:  " + bytes1.length);
            bytes[i * 2] = bytes1[0];
            bytes[i * 2 + 1] = bytes1[1];
        }
        if (!isExit) {
            mAudioTrack.write(bytes, 0, bytes.length);
        }
//        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
//        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
//        String str = formatter.format(curDate);
//        Log.e("TAG4playSound", str);    //GT20171129
    }

    //short转byte
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = new Integer(temp & 0xff).byteValue();
            temp = temp >> 8; // 向右移8位
        }
        return b;

    }

    /**
     * 获取当前声音数据的特征值    //GC20180412
     */
    private void obtainFeaturex() {
        //声音数据归一化
        int max = 0;
        for (int i = 0; i < 800; i++) {
            svmData[i] = svmData[i] - 2048;
            if (svmData[i] > 2047) {
                svmData[i] = 2047;
            } else if (svmData[i] < -2048) {
                svmData[i] = -2048;
            }
            svmData[i] = Math.abs(svmData[i]);
            if (svmData[i] > max) {
                max = svmData[i];
                //找寻声音信号幅值最大点用于用户界面画进度条高度 //GC20181113
                maxVoice = max;
            }
        }
        for (int i = 0; i < 800; i++) {
            //强制转换类型，int运算得double
            normalization[i] = svmData[i] / (max * 1.0);
        }
        //短时能量分布处理  （短时步长为50）
        double[] mED = new double[751];
        for (int i = 0; i < 751; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + normalization[i + j] * normalization[i + j];
            }
            mED[i] = mtemp / 50;
        }
        //短时过零率 //阈值
        double threshold = 0.5;
        double[] mZCR = new double[750];
        for (int i = 0; i < 750; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + Math.abs(Math.signum(normalization[i + j + 1] - threshold) - Math.signum(normalization[i + j] - threshold))
                        + Math.abs(Math.signum(normalization[i + j + 1] + threshold) - Math.signum(normalization[i + j] + threshold));
            }
            mZCR[i] = mtemp / 2;
        }
        //短时能量分布脉冲的宽度、高宽比、位置特征值
        //是否是脉冲宽度的起始
        int isStart = 0;
        //数据序数
        int Nu = 1;
        int widthFirst = 0;
        int widthLast = 0;
        double max2 = 0.0;
        //求均值
        double sum = 0.0;
        double meanValue;
        for (int i = 0; i < 751; i++) {
            sum += mED[i];
        }
        meanValue = sum / 751;
        for (int i = 0; i < 751; i++) {
            if (mED[i] > meanValue) {
                if (isStart == 0) {
                    widthFirst = Nu;
                    isStart = 1;
                } else {
                    widthLast = Nu;
                }
            }
            Nu++;
            if (mED[i] > max2) {
                max2 = mED[i];
            }
        }
        //脉冲宽度（大于均值的元素中，最小序数元素与最大序数元素序数的差值）
        feature[0] = widthLast - widthFirst;
        //脉冲高宽比观测值
        feature[1] = max2 / (widthLast - widthFirst);
        //脉冲位置取整观测值
        feature[2] = Math.round((widthLast + widthFirst) / 2);
        //短时过零率特征值
        //求均值
        double sum2 = 0.0;
        double meanValue2;
        for (int i = 0; i < 751; i++) {
            sum2 += mED[i];
        }
        meanValue2 = sum2 / 750;
        double temp = 0.0;
        for (int i = 0; i < 750; i++) {
            if (mZCR[i] > meanValue2) {
                temp = temp + mZCR[i];
            }
        }
        //数组中大于均值的元素和为过零率特征值
        feature[3] = temp;
        //特征值归一化（参考已采数据特征的最大最小值进行归一化）   //GC20180707
        feature[0] = Math.abs(feature[0] - 39) / (750 - 39);
        feature[1] = Math.abs(feature[1] - 7.6523e-5) / (0.0047 - 7.6523e-5);
        feature[2] = Math.abs(feature[2] - 105) / (732 - 105);
        feature[3] = Math.abs(feature[3] - 7) / (4871 - 7);

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
            EventBus.getDefault().post(new ResultOfSvmEvent(true));
            //连续两次判断为故障声开始进行相关
            if(svmPredictCount > 0){
                //相关判断
                related();
                if(p > 0.9){
                    //相关之后再光标定位、计算延时值
                    autoLocate();
                    //相关——判断是故障,虚光标位置和声磁延时值传递   //GC20190218
                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,true, cursorPosition));
                }else{
                    //不相关——判断不是故障
                    EventBus.getDefault().post(new ResultOfRelevantEvent(timeDelay,false, cursorPosition));
                }
            }
            //保留上次预测结果为是的声音数据用做相关计算 //GC20181119
            System.arraycopy(normalization, 0, normalization2, 0, 800);
            svmPredictCount++;

        } else {
            //判断不是故障
            EventBus.getDefault().post(new ResultOfSvmEvent(false));
            svmPredictCount = 0;
            //延时值显示逻辑bug修改  //GC20190720
            isRelatedCount = 0;
        }

    }

    /**
     * 计算相关系数       //GC20181119
     */
    private void related() {
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
        /*String related = String.valueOf(p);
        Log.e("related", related);*/
    }

    /**
     * 得到自动定位的光标位置和相应的声磁延时值
     */
    private void autoLocate() {
        //求触发时刻前50个点的均值    //20190720
        double ave = 0;
        for (int i = 50; i < 100; i++) {
            ave += autoLocate[i];
        }
        ave = ave / 50;
        //方差
        double var = 0;
        for (int i = 50; i < 100; i++) {
            var += (autoLocate[i] - ave) * (autoLocate[i] - ave);
        }
        var = var / 50;
        //标准差
        double sta = Math.sqrt(var);

        //从触发时刻（第101个点i=100）之后，找出越出置信边界的第一个极值点（屏幕波形显示触发时刻前50个点和后349个点）
        int n = 0;
        //去头去尾
        for (int i = 101; i < 449; i++) {
            //置信边界  //GC20181201 只求极小值
            if ( autoLocate[i] < (ave - sta * 5) ){
                if ((autoLocate[i] < autoLocate[i - 1]) && (autoLocate[i] <= autoLocate[i + 1])) {
                    //极小值点
                    n = i;
                }
            }
            if ( n > 0 ) {
                break;
            }
        }
        if (n > 0) {
            cursorPosition = n - 50;
            timeDelay = (cursorPosition - 50) * 0.125;
            Log.e("test",cursorPosition + "点数");
            Log.e("test",timeDelay + "ms");
        }

    }

    //点击记忆按钮执行的方法
    public void clickMemory() {
        isClickMem = true;
        System.arraycopy(voiceDraw, 0, compareDraw, 0, 400);
    }

    //点击比较按钮执行的方法
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
     * 滤波方式选择， *低通1 *带通2 *高通3 *全通0
     * @param llView    滤波对话框
     */
    protected void showFilterDialog(final LinearLayout llView) {
        customDialog = new CustomDialog(BaseActivity.this);
        customDialog.show();
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

        customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (currentFilter) {
                    case 0:
                        Constant.filterType = currentFilter;
                        clickQuantong();
                        break;
                    case 1://低通
                        Constant.filterType = currentFilter;
                        clickDitong();
                        break;
                    case 2://带通
                        Constant.filterType = currentFilter;
                        clickDaitong();
                        break;
                    case 3://高通
                        Constant.filterType = currentFilter;
                        clickGaotong();
                        break;
                }
                customDialog.dismiss();

            }
        });
        customDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llView.setClickable(true);
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

    //发送低通控制命令
    public void clickDitong() {
        clickTongNum = 1;
        int[] ints = {96, 1, 0};    //GN （控制命令前三个字节的十进制数值）  设备地址：96；低通滤波功能：1；控制增益：无

        long l = getCommandCrcByte(ints);
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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendCommand(request);

    }

    //发送带通控制命令
    public void clickDaitong() {
        clickTongNum = 2;
        int[] ints = {96, 2, 0};

        long l = getCommandCrcByte(ints);
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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendCommand(request);

    }

    //发送高通控制命令
    public void clickGaotong() {
        clickTongNum = 3;
        int[] ints = {96, 3, 0};

        long l = getCommandCrcByte(ints);
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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendCommand(request);

    }

    //发送全通控制命令
    public void clickQuantong() {
        clickTongNum = 0;
        int[] ints = {96, 4, 0};

        long l = getCommandCrcByte(ints);
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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendCommand(request);

    }

    //GN 增益数值和百分比的转化 100转32
    public int b2s(int b) {
        int s = 0;
        float v = (float) b / 100.0f;
        float v1 = v * 32;
        s = (int) v1;
        return s;
    }

    //32转100
    public int s2b(int s) {
        int b = 0;
        float v = (float) s / 32.0f;
        float v1 = v * 100;
        b = (int) v1;
        return b;
    }

    @Override
    protected void onDestroy() {
        try {
            isExit = true;
            // 关闭并释放资源
            mAudioTrack.release();
            try {
                blueSocket.close();
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
//GT20171129 内部的音频缓冲区的大小 输出结果1392
//GT20180321 蓝牙输入流解读

/*更改记录*/
//GC20170609    修改增益显示方式（百分比或实际值）
//GC20171129    修改蓝牙输入流的处理，缩短声音播放的延时问题    //G?
//GC20171205    添加探头相对电缆位置的左右判断的功能
//GC20180412    获取需要处理的声音数据并计算其特征值
//GC20180428    画声音波形的位置改变，提前50个点
//GC20180504    训练声音特征生成model //G?是否不用线程
//GC20180707    声音特征值归一化计算修改
//GC20181113    增益进度条高度显示优化
//GC20181118    接收控制命令未响应处理
//GC20181119 添加相关判断
//GC20181201 自动定位算法优化


//GC20190123 智能识别提示优化
//GC20190216 红色虚光标绘制情况修改
//GC20190218 专家界面延时值显示和相关结果一致起来（通过相关后计时自动定位数值不一致也不刷新延时值）
//GC20190307 词条和延时跳动效果
//GC20190407 硬件关闭重连功能添加
//GC20190422 "发现故障"提示音添加
//GC20190613 重连主提示框提示
//GC20190625 用户界面发现故障UI提示更改 （去掉上次延时值，显示刻度圆圈大小）
//GC20190627 触发灯闪烁bug原因
//GC20190717 用户界面布局修改
//GC20190720    延时值显示逻辑bug修改

//版本变动
//GC2.01.005 界面无缝切换
//GC2.01.006 蓝牙重连功能优化
//GC2.01.007 提示音改进和蓝牙重连提示优化
//GC2.01.008 稳定无bug版本
//GC2.01.009 用户界面布局修改