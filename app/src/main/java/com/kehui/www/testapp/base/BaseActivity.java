package com.kehui.www.testapp.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
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
import com.kehui.www.testapp.adpter.MyChartAdapter;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.Constant;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.bean.PackageBean;
import com.kehui.www.testapp.event.AcousticMagneticDelayEvent;
import com.kehui.www.testapp.event.AcousticMagneticDelay2;
import com.kehui.www.testapp.event.HandleReceiveDataEvent;
import com.kehui.www.testapp.event.OperationGuideEvent;
import com.kehui.www.testapp.event.SendDataFinishEvent;
import com.kehui.www.testapp.event.StartReadThreadEvent;
import com.kehui.www.testapp.event.UINoticeEvent;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.util.AplicationUtil;
import com.kehui.www.testapp.util.PrefUtils;
import com.kehui.www.testapp.util.Utils;
import com.kehui.www.testapp.view.UserMainActivity;

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
 * 是专家模式和用户模式基类
 */
public class BaseActivity extends AppCompatActivity {
    //蓝牙相关
    BluetoothDevice _device = null; // 蓝牙设备
    BluetoothSocket _socket = null; // 蓝牙通信socket
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号
    //GN20190407 是否尝试连接
    public boolean isFlag;

    /*声音播放*/
    public AudioManager audioManager;
    public AudioTrack mAudioTrack;
    /*声音特征训练*/
    private double[] readFeature = new double[2000];
    private boolean svmTrainThread; //GN 训练声音特征线程是否启动的标志
    private svm_model model;
    /*获取蓝牙数据*/
    public long[] mCrcTable;
    public BluetoothSocket mSocket;
    public InputStream mInputStream;
    public boolean getBDataThread;    //GN 获取蓝牙数据线程是否启动的标志
    public int mTempLength = 0;         //GN 蓝牙数据的临时数组长度
    public int[] mTemp = new int[1024000]; //GN 蓝牙数据的临时数组
    public int mTempcount = 0;          //GN 蓝牙数据的临时数组内的输入流个数
    public boolean handleBDataThread;   //GN 蓝牙数据是否正在处理的标志
    public boolean sendCommand;         //GN 初始化控制命令是否已经发送的标志
    public byte[] tempRequest = new byte[7];
    public boolean hasSendMessage;      //GN 控制命令是否发送成功的标志
    public int mTempLength2 = 0;            //GN 进行处理的蓝牙数据数组长度
    public int[] mTemp2 = new int[1024000];   //GN 进行处理的蓝牙数据数组
    /*处理蓝牙数据*/
    public boolean hasLeft;     //GN 将蓝牙数据分包后，是否有剩余数据的标志
    public int hasLeftLen = 0;  //GN 将蓝牙数据分包后(每包59个字节)，剩余数据的数组长度
    public int[] mTempLeft = new int[59];    //GN 将蓝牙数据分包后有剩余数据时的临时数组
    public int seekbarType;     //GN 磁场（1）或声音（2）控制的选择
    public int[] shengyinSeekbarInts;
    public int[] cichangSeekbarInts;
    public int crcNum;
    public boolean toastDisconnected;  //蓝牙设备是否连接失败的标志
    public boolean isExit;     //是否退出软件的标志
    public boolean mShengyinFlag;   //是否开始获取声音包的标志
    public int mShengyinMarkNum;    //GN 触发时刻数据点所在的位置
    public int mShengyinCount;      //GN 触发后获取声音包的个数
    public int[] mShengyinArray;
    public boolean isDraw;     //是否可以画波形的标志
    public int[] mTempShengyinArray;
    public int[] mCompareShengyinArray;
    public int[] mCompareArray;
    public int[] mCichangArray;
    public int[] mTempCichangArray;
    /*声音智能识别*/
    private int[] mAIRecognitionArray = new int[900];   //GC20180412 需要预测的声音缓存数组
    private int[] mSVM = new int[800];                   //GC20180412 获取声音特征值的缓存数组
    private double[] featurex = new double[4];
    private double[] mNormalization = new double[800];
    //GC20181119 相关
    private double[] mNormalization1 = new double[800];
    public int svmPredictCount = 0; //预测结果为是的次数统计
    private double p = 0;
    private int[] mSVMLocate = new int[800];    //GC20181201 用于自动定位的缓存数组
    private int[] mSVMLocate2 = new int[800];    //GC20181204 用于自动定位的缓存数组
    private int position;       //光标位置
    private double timeDelay;   //声磁延时值
    private double userDelay;   //一组相关声音的声磁延时值
    /*用户界面增益进度条显示*/
    public int maxShengYin; //GN 声音信号幅值最大的点
    public int maxCiChang;  //GN 磁场信号幅值最大的点
    public int[] maxCichangArray;   //GC20181113
    /*按钮控制*/
    public boolean isSilence;  //是否是静音的标志
    public int streamVolumenow;     //GN 当前音量值
    public boolean isClickRem;  //是否记忆波形的标志
    public boolean isCom;       //是否比较波形的标志
    public MyChartAdapter myChartAdapterShengyin;
    public MyChartAdapter myChartAdapterCichang;
    private CustomDialog customDialog;      //GN 滤波方式选择对话框
    public int clickTongNum;    //GN 滤波方式选择， *低通1 *带通2 *高通3 *全通0
    public int currentFilter;   //GN 当前滤波方式  *低通1 *带通2 *高通3 *全通0
    /*全局的handler对象用来执行UI更新*/
    public int WHAT_LIGHT = 1;             //GN“同步指示” 灯变红
    public int CICHANG_CHANGE_OVER = 2;    //GN“同步指示” 灯变灰
    public int WHAT_REFRESH = 3;   //子线程通知主线程刷新UI的what
    public int LINK_LOST = 11;      //GN20190407
    public int LINK_RECONNECT = 22;     //GC20190613
    public int SEND_SUCCESS = 100;     //GN平板命令下发成功
    public int SEND_ERROR = 200;       //GN平板命令下发失败
    public int BLUETOOTH_DISCONNECTED = 300;   //GN蓝牙设备未连接
    public int WHAT_POSITION_Right = 400;  //GN通过磁场数据判断 仪器 在 电缆 的“右or左”
    public int WHAT_POSITION_LEFT = 500;
    //解密需要的解析常量数组
    public int[] IndexTable = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
    public int[] StepSizeTable = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31,
            34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190,
            209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963,
            1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660,
            4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635,
            13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767};


    //GN全局handle更新UI
    public Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            EventBus.getDefault().post(new UINoticeEvent(msg.what));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initData();
        setAudioTrack();
        getFeaturexData();
        svmTrain.start();      //GN 启动训练声音特征的线程
        getCrcTable();
        startThread();          //GN 获取蓝牙数据
        handleBData.start();   //GN 处理蓝牙数据的线程
        EventBus.getDefault().register(this);

    }

    //初始化
    private void initData() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSocket = MyApplication.getInstances().get_socket();
        hasSendMessage = false;
        seekbarType = 0;
        shengyinSeekbarInts = new int[]{22, 22};
        cichangSeekbarInts = new int[]{22, 22};
        crcNum = 0;
        toastDisconnected = false;
        isExit = false;
        mShengyinFlag = false;
        mShengyinMarkNum = 0;
        mShengyinCount = 0;
        mShengyinArray = new int[500];
        new Random();
        isDraw = true;
        mTempShengyinArray = new int[500];
        mCompareShengyinArray = new int[500];
        mCompareArray = new int[500];
        mCichangArray = new int[400];
        mTempCichangArray = new int[400];
        maxCichangArray = new int[400];     //GC20181113
        isClickRem = false;
        isCom = false;
        clickTongNum = 0;   //初始化滤波方式为全通

    }

    //设置音频播放工具
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

    //GC20180504 从assets文件夹中获取声音特征的数据
    private void getFeaturexData() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "feature.txt");
        BufferedInputStream bis = new BufferedInputStream(mResourceAsStream);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c;//读取bis流中的下一个字节
        try {
            c = bis.read();
            while (c != -1) {
                baos.write(c);
                c = bis.read();
            }
            bis.close();
            String s = baos.toString();         //Log.e("FILE", s);
            String[] split = s.split("\\s+");   //Log.e("FILE","splitSize:"+split.length);
            for (int i = 0; i < split.length; i++) {
                try {
                    readFeature[i] = Double.parseDouble(split[i]);    // Log.e("FILE","crcTable[i]:"+mCrcTable[i]);
                } catch (Exception l_ex) {
                }
            }
            //GC20180504 注意读取文本文件的编码格式为ANSI  Log.e("FILE",""+readFeature);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //GC20180504 训练声音特征的线程
    Thread svmTrain = new Thread(new Runnable() {
        @Override
        public void run() {
            if (!svmTrainThread) {
                voiceSvmTrain(readFeature);
                svmTrainThread = true;
            }
        }
    });

    //GC20180504 训练声音特征生成model
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

    //从assets文件夹中获取crctable的数据
    public void getCrcTable() {
        InputStream mResourceAsStream = this.getClassLoader().getResourceAsStream("assets/" + "crctable.txt");
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
            String s = baos.toString();         //Log.e("FILE", s);
            String[] split = s.split("\\s+");   //Log.e("FILE","splitSize:"+split.length);
            mCrcTable = new long[256];
            for (int i = 0; i < split.length; i++) {
                mCrcTable[i] = Long.parseLong(split[i], 16);    // Log.e("FILE","crcTable[i]:"+mCrcTable[i]);
            }
            /* byte retArr[]=baos.toByteArray();
            for (byte b : retArr) {
                Log.e("FILE",""+b);
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(StartReadThreadEvent event) {
        //GC20190613
        Message message = new Message();
        message.what = LINK_RECONNECT;
        mHandle.sendMessage(message);
        Toast.makeText(this, getResources().getString(R.string.connect) + " " + event.device + " " + getResources().getString(R.string.success),
                Toast.LENGTH_SHORT).show();
        getBDataThread = false;
        startThread();

    }

    //获取蓝牙数据
    private void startThread() {
        try {
            mSocket = MyApplication.getInstances().get_socket();    //GN 进入演示模式后打开仪器依旧可以正常连接
            if (mSocket != null)
                mInputStream = mSocket.getInputStream();    //GN 通过蓝牙socket获得输入流

        } catch (IOException e) {
            Toast.makeText(this, getResources().getString(R.string
                    .Can_not_get_input_stream_via_Bluetooth_socket), Toast.LENGTH_SHORT).show();
            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
            e.printStackTrace();
        }
        if (!getBDataThread) {

            //重连后下发参数
            if(Constant.CurrentVoiceParam!=null)
                sendString(Constant.CurrentVoiceParam);
            if(Constant.CurrentFilterParam!=null)
                sendString(Constant.CurrentFilterParam);
            if(Constant.CurrentMagParam!=null)
                sendString(Constant.CurrentMagParam);

            //当前连接状态为连接
            Constant.BluetoothState = true;
            new Thread(getBluetoothData).start();  //GN 启动获取蓝牙数据的线程
            getBDataThread = true;

        }

    }

    //GN 获取蓝牙数据的线程
    Runnable getBluetoothData = new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] bStream = new byte[1024000];    //GN 存放每个输入流的字节数组
            while (true) {
                try {
                    while (true) {
                        if (mInputStream == null) {
                            Log.e("打印-mInputStream", "null");
                            return;
                        }
                        len = mInputStream.read(bStream, 0, bStream.length);
                        //Log.e("stream", "len:" + len + "时间" + System.currentTimeMillis());     //GT20180321 每个输入流的的长度
                        byte[] tempbStream = new byte[len];    //jwj20180411
                        for (int i = 0, j = mTempLength; i < len; i++, j++) {
                            if (Constant.isStartInterception)
                                tempbStream[i] = bStream[i];
                            mTemp[j] = bStream[i] & 0xff;   //将传过来的字节数组转变为int数组
                        }
                        if (Constant.isStartInterception)
                            Constant.sbData.append(Utils.bytes2HexString(tempbStream));
                        mTempLength += len;
                        mTempcount++;
                        //GC20171129 在没有处理蓝牙数据时缓存数个输入流用做后续蓝牙数据处理
                        if (mTempcount >= 5 && !handleBDataThread) {
                            if (sendCommand){
                                /*//超过590滤除   //GC2.01.006 蓝牙重连功能优化
                                if (mTempLength > 590) {
                                    mTempLength = 590;
                                }*/     //GC20190627 触发灯闪烁bug原因
                                for (int i = 0; i < mTempLength; i++) {
                                    mTemp2[i] = mTemp[i];
                                }
                                mTempLength2 = mTempLength;
                                //Log.e("stream", "lenSum:" + mTempLength2);  //GT20180321 要处理的蓝牙数据的长度
                                handleBDataThread = true;

                            } else {
                                sendCichangInitData();
                                mHandle.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendShengyinInitData();
                                    }
                                }, 1000);   //GN 缓存数据之前首先发送初始化控制命令
                                sendCommand = true;
                            }
                            mTempLength = 0;
                            mTempcount = 0;
                        }
                        //短时间没有数据才跳出进行显示
                        if (mInputStream.available() == 0) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    try {
                        mInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    mInputStream = null;

                    //当前连接状态为断开 //GC2.01.006 蓝牙重连功能优化
                    Constant.BluetoothState = false;
                    //重置变量
                    mTemp = null;
                    mTemp = new int[1024000];
                    mTemp2 = null;
                    mTemp2 = new int[1024000];
                    mTempLength2 = 0;
                    mTempLength = 0;
                    mTempcount = 0;
                    hasLeftLen = 0;
                    mTempLeft = null;
                    mTempLeft = new int[59];
                    handleBDataThread = false;
                    hasLeft = false;


                    //GN20190407 启动重连
                    isFlag = false;
                    Message message = new Message();
                    message.what = LINK_LOST;
                    mHandle.sendMessage(message);
//                    mHandleReconnect.sendEmptyMessage(0);
                    //启动重连线程，是否需要单独线程控制，需要观察
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!isFlag) {
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
    //蓝牙重连handler //GN去掉
    public Handler mHandleReconnect = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                while (!isFlag) {
                    Log.e("蓝牙测试", "connectThread线程，尝试连接");
                    reconnect();
                }
            }
        }
    };


    /**
     * 尝试连接蓝牙
     */
    public void reconnect() {
        // 读取设置数据
        SharedPreferences sharedata1 = getSharedPreferences("Add", 0);
        String address = sharedata1.getString(String.valueOf(1), null);
        // 得到蓝牙设备句柄
        _device = _bluetooth.getRemoteDevice(address);

        // 用服务号得到socket
        try {
            _socket = _device.createRfcommSocketToServiceRecord(UUID
                    .fromString(MY_UUID));

            MyApplication.getInstances().set_socket(_socket);
            MyApplication.getInstances().set_device(_device);
            MyApplication.getInstances().set_bluetooth(_bluetooth);

        } catch (IOException e) {

        }
        //连接socket
        try {
            if (_socket != null) {  //GC2.01.007
                _socket.connect();
                isFlag = true;
                EventBus.getDefault().post(new StartReadThreadEvent(_device.getName()));
            }

        } catch (IOException e) {
            try {
                _socket.close();
                _socket = null;
                //Log.e("蓝牙测试", "connectThread线程，走到异常");
                Thread.sleep(10000);
            } catch (Exception ee) {
            }
            return;
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
    //GN 发送声音初始化控制命令
    public void sendShengyinInitData() {
        int[] ints = {96, 0, 0 + 22};   //GN （控制命令前三个字节的十进制数值）  设备地址：96；增益调整功能：0；控制增益：声音0、阶数22
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

    //GN 发送磁场初始化控制命令
    public void sendCichangInitData() {
        int[] ints = {96, 0, 128 + 22}; //GN （控制命令前三个字节的十进制数值）  设备地址：96；增益调整功能：0；控制增益：声音1、阶数22。
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

    //对发送的控制命令进行CRC校验
    public long getRequestCrcByte(int[] bytes) {
        return testCrc(bytes);

    }

    //GN 控制命令发送
    public void sendString(byte[] request) {
        if (!hasSendMessage) {
            for (int i = 0; i < request.length; i++) {
                tempRequest[i] = request[i];
            }
            if (mSocket == null) {
                Toast.makeText(this, getResources().getString(R.string.Bluetooth_is_not_connected),
                        Toast.LENGTH_SHORT).show();
            }
            try {
                OutputStream os = mSocket.getOutputStream(); // 蓝牙连接输出流
                //byte[] bos = str.getBytes("GB2312");//native的Socket发送字节流默认是GB2312的，所以在Java方面需要指定GB2312
                os.write(request);
                EventBus.getDefault().post(new SendDataFinishEvent());
                hasSendMessage = true;
            } catch (IOException e) {
                //Toast.makeText(this, "发送失败" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        } else {
            //Toast.makeText(MainActivity.this, "还没有收到来自设备端的回复", Toast.LENGTH_SHORT).show();
        }

    }

    //GN 处理蓝牙数据的线程
    Thread handleBData = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (handleBDataThread) {
                    domTemp2(mTemp2, mTempLength2);
                    handleBDataThread = false;
                }
            }
        }

    });

    //GN 处理蓝牙数据
    private void domTemp2(int[] mTemp, int mTempLength) {
        int i = 0;
        int dataNum = 0;    //GN 处理过的数据数组长度
        int[] tempints = new int[59];   //GN 一个数据包的临时数组
        int[] tempints2 = new int[7];   //GN 接收的控制命令临时数组
        //GN 先判断截取数据包后是否有剩余数据
        if (hasLeft) {
            for (int j = hasLeftLen, k = 0; j < 59; j++, k++) {
                mTempLeft[j] = mTemp[k];    //GN 将剩余数据与新数据合并
            }
            for (int i1 = 0; i1 < 59; i1++) {
                if (mTempLeft[i1] == 83 || mTempLeft[i1] == 77 || mTempLeft[i1] == 96) {    //GN 找到数据开头（0x53：声音  0x4d：磁场  0x60：T-506）
                    for (int i2 = 0, j = i1; i2 < 59; i2++, j++) {
                        if (j >= 59) {
                            tempints[i2] = mTemp[i + j - hasLeftLen];
                        } else {
                            tempints[i2] = mTempLeft[j];    //GN 取数据包
                        }
                        boolean isCrc = doTempCrc(tempints);
                        if (isCrc) {    //GN CRC1校验成功，判断为数据包数据
                            doTempBean(tempints);
                            i1 += 58;
                        } else {        //GN CRC1校验失败，判断是否为控制命令
                            if (mTempLeft[i1] == 96) {
                                for (int i3 = 0, k = i1; i3 < 7; i3++, k++) {
                                    if (k >= 59) {
                                        tempints2[i3] = mTemp[i + k - hasLeftLen];
                                    } else {
                                        tempints2[i3] = mTempLeft[k];
                                    }
                                }
                                boolean isCrc2 = doTempCrc2(tempints2);
                                if (isCrc2) {   //GN接收到命令
                                    hasSendMessage = false;
                                    i1 += 6;
                                    if (tempints2[2] != 1) {    //GN Respond：响应值为0，命令未响应
                                        mHandle.sendEmptyMessage(SEND_ERROR);
                                        EventBus.getDefault().post(new HandleReceiveDataEvent());
                                        seekbarType = 0;
                                    } else if (tempints2[1] == tempRequest[1]) {    //GN 接受命令内容与要发送命令一致
                                        seekbarType = 0;
                                        mHandle.sendEmptyMessage(SEND_SUCCESS);
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
        for (; i < mTempLength - 59; i++) {
            if (mTemp[i] == 83 || mTemp[i] == 77 || mTemp[i] == 96) {
                for (int j = i, k = 0; j < (i + 59); j++, k++) {
                    tempints[k] = mTemp[j];    //GN 取数据包
                }
                boolean isCrc = doTempCrc(tempints);
                if (isCrc) {    //GN CRC1校验成功，判断为数据包数据
                    doTempBean(tempints);
                    i += 58;
                } else {        //GN CRC1校验失败，判断是否为控制命令
                    if (mTemp[i] == 96) {
                        for (int j = i, k = 0; j < (i + 7); j++, k++) {
                            tempints2[k] = mTemp[j];   //GN 取控制命令
                        }
                        boolean isCrc2 = doTempCrc2(tempints2);
                        if (isCrc2) {
                            hasSendMessage = false;
                            if (tempints2[2] != 1) {    //GN Respond：响应值，命令未响应
                                mHandle.sendEmptyMessage(SEND_ERROR);
                                EventBus.getDefault().post(new HandleReceiveDataEvent());   //GC20181118
                                seekbarType = 0;
                            } else if (tempints2[1] == tempRequest[1]) {
                                seekbarType = 0;
                                mHandle.sendEmptyMessage(SEND_SUCCESS);
                            }
                            i += 6;
                        }
                    }
                }
            }
            dataNum = i;
        }
        if (dataNum == mTempLength) {
            hasLeft = false;
        } else {    //如果剩余的数少于59个,退出循环 并且把剩下的数据存到临时数组中 同时设置有剩余的数组
            for (int j = dataNum + 1, k = 0; j < mTempLength; j++, k++) {
                mTempLeft[k] = mTemp[j];
            }
            hasLeftLen = mTempLength - i;
            hasLeft = true;
        }

    }

    //GN 对数据包进行CRC校验
    public boolean doTempCrc(int[] tempcrc) {
        int[] ints = new int[55];
        int[] ints2 = new int[4];   //crc校验返回回来进行比对的4个字符
        for (int i1 = 0, j = 0; i1 < 55; i1++, j++) {
            ints[j] = tempcrc[i1];
        }
        for (int i1 = 55, j = 0; i1 < 59; i1++, j++) {
            ints2[j] = tempcrc[i1];
        }
        if (!isCrc(ints, ints2)) {
            crcNum++;
            //Log.d("CRC", crcNum + "");
        }
        return isCrc(ints, ints2);

    }

    //GN 对接收的控制命令进行CRC校验
    public boolean doTempCrc2(int[] tempcrc) {
        int[] ints = new int[3];
        int[] ints2 = new int[4];   //crc校验返回回来进行比对的4个字符
        for (int i1 = 0, j = 0; i1 < 3; i1++, j++) {
            ints[j] = tempcrc[i1];
        }
        for (int i1 = 3, j = 0; i1 < 7; i1++, j++) {
            ints2[j] = tempcrc[i1];
        }
        return isCrc(ints, ints2);

    }

    //测试CRC
    public long testCrc(int[] ints) {
        crcNum++;
        final int qqq = crcNum;
        if (crcNum >= 999999999)
            crcNum = 0;
        mHandle.postDelayed(new Runnable() {
            @Override
            public void run() {
//                if (qqq == crcNum) mHandle.sendEmptyMessage(BLUETOOTH_DISCONNECTED);
            }
        }, 2000);
        long nReg = Long.valueOf("4294967295");
        long integer = Long.valueOf("4294967295");
        for (int i = 0; i < ints.length; i++) {
            nReg = nReg ^ ints[i];
//            Log.e("FILE","nReg=nReg^bytes[i];");
            for (int j = 0; j < 4; j++) {
                long a = nReg >> 24;
//                Log.e("FILE", "a:" + a);
                long b = a & 255;
//                Log.e("FILE", "b:" + b);
                long nTemp = mCrcTable[(int) b];
//                Log.e("FILE", "nTemp:" + nTemp);
                //4294967295 -1
                nReg = (nReg << 8) & integer;
                nReg = nReg ^ nTemp;
            }
        }
        return nReg;

    }

    //判断Crc校验的结果
    public boolean isCrc(int[] ints, int[] ints2) {
        long l = testCrc(ints);
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
    //对59个数据进行bean对象的处理
    private void doTempBean(int[] tempints) {
        PackageBean packageBean = new PackageBean();
        packageBean.setSM(tempints[0]);
        packageBean.setMark(tempints[1]);
        packageBean.setIndex(tempints[2]);
        packageBean.setPredsample(new int[]{tempints[3], tempints[4]});
        int[] date = new int[50];
        for (int i1 = 5, j = 0; i1 < 55; i1++, j++) {
            date[j] = tempints[i1];
        }
        packageBean.setDate(date);
        doPackageBean(packageBean);

    }

    //每当解析完生成一个packageBean,对其进行相应的操作
    public void doPackageBean(PackageBean packageBean) {
//        Log.e("FILE", "packageBean:" + packageBean.toString());
        int[] results = decodeData(packageBean);
        if (packageBean.getSM() == 83) {    //GN 0x53 声音
            int mark = packageBean.getMark();
            if (binaryStartsWithOne(mark)) {    //GN Mark最高位1，仪器在这一包内触发，组成放电声音的第一个包的有效数据
                Message msg = new Message();
                msg.what = WHAT_LIGHT;
                mHandle.sendMessage(msg);   //GN“同步指示” 灯变红
                mShengyinFlag = true;       //GN开始截取声音包
                mShengyinMarkNum = getMarkLastSeven(mark);  //GN Mark后7位，触发时刻数据点所在的位置
                for (int i = 0, j = mShengyinMarkNum; j < 100; i++, j++) {
                    mShengyinArray[i] = results[j];
                    mAIRecognitionArray[i] = mAIRecognitionArray[j];    //GC20180412 凑足触发时刻之前100点的数据1
                }
                //GC20180412 凑足触发时刻之前100点的数据2
                for (int i = 100 - mShengyinMarkNum, j = 0; j < 100; i++, j++) {
                    mAIRecognitionArray[i] = results[j];
                }
                mShengyinCount++;   //GN 获取声音包的个数
            } else {
                if (mShengyinFlag) {    //GN 已经开始获取声音包
                    if (mShengyinCount <= 7) {                  //GN 声音识别需要9个声音包 4
                        for (int i=100+mShengyinCount*100-mShengyinMarkNum , j = 0; j < 100; i++, j++) {
                            mAIRecognitionArray[i] = results[j];
                        }
                        if (mShengyinCount <= 4) {              //GN 要画波形需要5个声音包
                            for (int i = mShengyinCount * 100 - mShengyinMarkNum, j = 0; j < 100; i++, j++) {
                                mShengyinArray[i] = results[j];
                            }
                        }
                        mShengyinCount++;
                    } else {
                        mShengyinFlag = false;
                        mShengyinCount = 0;
                    }
                } else {      //不获取声音包
                    for (int i = 0, j = 0; j < 100; i++, j++) {
                        mAIRecognitionArray[i] = results[j];    //GC2018412 先缓存一包，用于找出声音识别的前100个点
                    }
                }
            }
            playSound(results);

        } else if (packageBean.getSM() == 77) {   //GN 0x4d 磁场
            if (packageBean.getMark() >= 128) {     //GN  Mark "128" 二进制：10000000 代表位7是1
                mHandle.sendEmptyMessage(WHAT_POSITION_Right);  //GC20171205 代表探头在电缆右侧
                packageBean.setMark(packageBean.getMark() - 128); //令位7为0
            } else {
                mHandle.sendEmptyMessage(WHAT_POSITION_LEFT);  //GC20171205  代表探头在电缆左侧
            }
            //GN 按照顺序（00 01 10 11）将磁场数据拼接起来，1包含有100个数据点
            for (int i = 0, j = packageBean.getMark() * 100; i < 100; i++, j++) {
                mCichangArray[j] = results[i];
            }
            //GN 4个磁场包拼接完成，开始画主界面波形
            if (packageBean.getMark() == 3) {
                Message msg = new Message();
                msg.what = CICHANG_CHANGE_OVER;
                mHandle.sendMessage(msg);
                if (isDraw) {
                    for (int i = 0; i < 400; i++) {
                        mTempCichangArray[i] = mCichangArray[i];
                        //GC20181113 找寻磁场信号幅值最大点用于用户界面画进度条高度
                        maxCichangArray[i] = mCichangArray[i] - 2048;
                        if (maxCichangArray[i] > 2047) {
                            maxCichangArray[i] = 2047;
                        } else if (maxCichangArray[i] < -2048) {
                            maxCichangArray[i] = -2048;
                        }
                        maxCichangArray[i] = Math.abs(maxCichangArray[i]);    //取绝对值
                        if (maxCichangArray[i] > maxCiChang) {
                            maxCiChang = maxCichangArray[i];
                        }
                    }
                    for (int i = 0; i < 400; i++) {
                        //mTempShengyinArray[i] = mShengyinArray[i];
                        mTempShengyinArray[i] = mAIRecognitionArray[i + 50];  //GC20180428 声音波形加上触发前50个点的数据
                    }
                    for (int i = 0; i < 400; i++) {
                        mCompareShengyinArray[i] = mTempShengyinArray[i];
                    }
                    //GC20180412 预测800个点的声音数据
                    for (int i = 0; i < 800; i++) {
                        mSVM[i] = mAIRecognitionArray[i];
                        mSVMLocate[i] = mAIRecognitionArray[i];     //GC20181201
                    }
                    obtainFeaturex();
                    if (svmTrainThread)
                        voiceSvmPredict(featurex);
                }
                Message message = new Message();
                message.what = WHAT_REFRESH;
                mHandle.sendMessage(message);
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
            int step = StepSizeTable[INDEX];
            int code = dateArray[i];
            int diffq = step / 8;
            if ((code & 4) == 4) diffq = diffq + step;
            if ((code & 2) == 2) diffq = diffq + step / 2;
            if ((code & 1) == 1) diffq = diffq + step / 4;
            if ((code & 8) == 8) PREDSAMPLE = PREDSAMPLE - diffq;
            else PREDSAMPLE = PREDSAMPLE + diffq;

            if (PREDSAMPLE > 4095)
                PREDSAMPLE = 4095;
            if (PREDSAMPLE < 0)
                PREDSAMPLE = 0;
            //Log.e("FILE", code+"");
            INDEX = INDEX + IndexTable[code];

            if (INDEX < 0)
                INDEX = 0;
            if (INDEX > 88)
                INDEX = 88;
            prevsample = PREDSAMPLE;
            previndex = INDEX;
            result[i] = prevsample;
        }
        return result;

    }

    //判断二进制的最高位是否是1
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

    //播放声音
    public void playSound(int[] results) {
        byte[] bytes = new byte[results.length * 2];
        for (int i = 0; i < results.length; i++) {
            short sh = (short) ((results[i] - 2048) * 16);
            byte[] bytes1 = shortToByte(sh);
            //Log.e("FILE", "byte.length:  " + bytes1.length);
            bytes[i * 2] = bytes1[0];
            bytes[i * 2 + 1] = bytes1[1];
        }
        if (!isExit) mAudioTrack.write(bytes, 0, bytes.length);
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

    //GC20180412 获取当前的声音特征
    private void obtainFeaturex() {
        //GN数据归一化
        int max = 0;
        for (int i = 0; i < 800; i++) {
            mSVM[i] = mSVM[i] - 2048;
            if (mSVM[i] > 2047) {
                mSVM[i] = 2047;
            } else if (mSVM[i] < -2048) {
                mSVM[i] = -2048;
            }
            mSVM[i] = Math.abs(mSVM[i]);    //取绝对值
            if (mSVM[i] > max) {
                max = mSVM[i];
                maxShengYin = max;  //GC20181113 找寻声音信号幅值最大点用于用户界面画进度条高度
            }
        }
        for (int i = 0; i < 800; i++) {
            mNormalization[i] = mSVM[i] / (max * 1.0);  //强制转换类型，int运算得double
        }
        //短时步长为50
        //GN短时能量分布处理
        double[] mED = new double[751];
        for (int i = 0; i < 751; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + mNormalization[i + j] * mNormalization[i + j];
            }
            mED[i] = mtemp / 50;
        }
        //GN短时过零率
        double th = 0.5;     //阈值 threshold
        double[] mZCR = new double[750];
        for (int i = 0; i < 750; i++) {
            double mtemp = 0.0;
            for (int j = 0; j < 50; j++) {
                mtemp = mtemp + Math.abs(Math.signum(mNormalization[i + j + 1] - th) - Math.signum(mNormalization[i + j] - th))
                        + Math.abs(Math.signum(mNormalization[i + j + 1] + th) - Math.signum(mNormalization[i + j] + th));
            }
            mZCR[i] = mtemp / 2;
        }
        //短时能量分布脉冲的宽度、高宽比、位置特征值
        int isStart = 0;    //是否是脉冲宽度的起始
        int Nu = 1;         //数据序数
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
        featurex[0] = widthLast - widthFirst;   //脉冲宽度（大于均值的元素中，最小序数元素与最大序数元素序数的差值）
        featurex[1] = max2 / (widthLast - widthFirst);  //脉冲高宽比观测值
        featurex[2] = Math.round((widthLast + widthFirst) / 2); //脉冲位置取整观测值
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
        featurex[3] = temp; //数组中大于均值的元素和为过零率特征值
        //GC20180707 特征值归一化（参考已采数据特征的最大最小值进行归一化）
        featurex[0] = Math.abs(featurex[0] - 39) / (750 - 39);
        featurex[1] = Math.abs(featurex[1] - 7.6523e-5) / (0.0047 - 7.6523e-5);
        featurex[2] = Math.abs(featurex[2] - 105) / (732 - 105);
        featurex[3] = Math.abs(featurex[3] - 7) / (4871 - 7);

    }

    //GC20180412 使用模型去预测获得的声音
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
        /*未训练过未归一化的故障点声音特征
        test[0].value = 173;
        test[1].value = 0.0021;
        test[2].value = 174;
        test[3].value = 1191;*/
        /*未训练过归一化的故障点声音特征
        test[0].value = (200 - 39) / (750 - 39);
        test[1].value = (0.0021 - 7.6523e-5) / (0.0047 - 7.6523e-5);
        test[2].value = (188 - 105) / (732 - 105);
        test[3].value = (1393 - 7) / (4871 - 7);*/
        //test[2].value = (Double.valueOf(featurex[2])).intValue();
        //double[] l = new double[2];
        //double result_prob = svm.svm_predict_probability(model, test,l);		//测试1，带预测概率的分类测试
        double result_normal = svm.svm_predict(model, test);    //测试2 不带概率的分类测试
        //GC20180417 是否是故障点声音
        if (result_normal == 1.0) {
            EventBus.getDefault().post(new OperationGuideEvent(true));
            //GC20181204 autoLocate();
            for (int i = 0; i < 800; i++) {
                mSVMLocate2[i] = mSVMLocate[i];     //GC20181204
            }
            /*String p1 = String.valueOf(position);
            String t = String.valueOf(timeDelay);
            Log.e("position", p1);
            Log.e("timeDelay", t);*/
            //GC20181119 添加相关判断
            if(svmPredictCount > 0){    //GN 从连续判断为是的第二组开始做相关
                related();
                if(p > 0.9){        //GC20181201
                    autoLocate();
                    userDelay = timeDelay;  //GN 只传递相关通过后的声磁延时值
                    EventBus.getDefault().post(new AcousticMagneticDelay2(userDelay,true,position));    //GC20190218 虚光标位置传递
                }else{
                    EventBus.getDefault().post(new AcousticMagneticDelay2(userDelay,false,position));
                }
            }
            for (int i = 0; i < 800; i++) {
                mNormalization1[i] = mNormalization[i]; //GC20181119 保留上次预测结果为是的声音数据用做相关计算
            }
            svmPredictCount++;

        } else {
            svmPredictCount = 0;
            EventBus.getDefault().post(new OperationGuideEvent(false));
        }

    }

    //GC20180613 计算出光标的位置和相应的声磁延时值
    private void autoLocate() {
        //GC20181204
        for (int i = 0; i < 800; i++) {
            mSVMLocate[i] = ( mSVMLocate[i] + mSVMLocate2[i] ) / 2;
        }
        //求均值
        double ave = 0;
        for (int i = 0; i < 100; i++) {
            ave += mSVMLocate[i];
        }
        ave = ave / 100;
        //求方差
        double var = 0;
        for (int i = 0; i < 100; i++) {
            var += (mSVMLocate[i] - ave) * (mSVMLocate[i] - ave);
        }
        var = var / 100;
        //求标准差
        double sta = Math.sqrt(var);
        //GN 从触发时刻（第101个点i=100）之后，找出越出置信边界的第一个极值点（屏幕波形显示触发时刻前50个点和后349个点）
        int m = 0;
        int n = 0;
        for (int i = 101; i < 449; i++) {   //GN 去头去尾
            //if ((mSVM[i] > (ave + sta * 5)) || (mSVM[i] < (ave - sta * 5))) {       //置信边界
            if ( mSVMLocate[i] < (ave - sta * 5) ){   //GC20181201 只求极小值
                /*if ((mSVM[i] > mSVM[i - 1]) && (mSVM[i] >= mSVM[i + 1])) {
                    m = i;  //极大值点
                } else*/    //GC20181201 只求极小值
                if ((mSVMLocate[i] < mSVMLocate[i - 1]) && (mSVMLocate[i] <= mSVMLocate[i + 1])) {
                    n = i;  //极小值点
                    /*String n1 = String.valueOf(n);
                    Log.e("n", n1);*/
                }
            }
            //if (m > 0 || n > 0) {
            if ( n > 0 ) {
                break;
            }
        }
        /*if (m > 0) {
            position = m - 50;
            timeDelay = (position - 50) * 0.125;
            EventBus.getDefault().post(new AcousticMagneticDelayEvent(position, timeDelay));    //GC20181106
        } else */
        if (n > 0) {
            position = n - 50;
            timeDelay = (position - 50) * 0.125;
            //GC20190218 EventBus.getDefault().post(new AcousticMagneticDelayEvent(position, timeDelay));    //GC20181106
        }

    }

    //GC20181119 计算相关系数
    private void related() {
        //分母1
        double sum1 = 0;
        for (int i = 0; i < 800; i++) {
            sum1 += mNormalization1[i];
        }
        double ave1 = sum1 / 800;
        double var1 = 0;
        for (int i = 0; i < 800; i++) {
            var1 += (mNormalization1[i] - ave1) * (mNormalization1[i] - ave1);
        }
        double sta1 = Math.sqrt(var1);
        //分母2
        double sum2 = 0;
        for (int i = 0; i < 800; i++) {
            sum2 += mNormalization[i];
        }
        double ave2 = sum2 / 800;
        double var2 = 0;
        for (int i = 0; i < 800; i++) {
            var2 += (mNormalization[i] - ave2) * (mNormalization[i] - ave2);
        }
        double sta2 = Math.sqrt(var2);
        //分子
        double sum = 0;
        for (int i = 0; i < 800; i++) {
            sum += (mNormalization1[i] - ave1) * (mNormalization[i] - ave2);
        }
        //相关系数
        p = sum / (sta1 * sta2);
        /*String related = String.valueOf(p);
        Log.e("related", related);*/

    }

    //点击记忆按钮执行的方法
    public void clickMemory() {
        isClickRem = true;
        for (int i = 0; i < 400; i++) {
            mCompareArray[i] = mCompareShengyinArray[i];
        }

    }

    //点击比较按钮执行的方法
    public void clickCompare() {
        if (isClickRem) {
            isCom = !isCom;
        } else {
            Toast.makeText(this, getResources().getString(R.string
                    .You_have_no_memory_data_can_not_compare), Toast.LENGTH_SHORT).show();
        }
        myChartAdapterShengyin.setmTempArray(mTempShengyinArray);
        myChartAdapterShengyin.setShowCompareLine(isCom);
        myChartAdapterShengyin.setmCompareArray(mCompareArray);
        myChartAdapterShengyin.notifyDataSetChanged();
        //refreshUi(false, 10);

    }

    //弹出滤波对话框   //GN 滤波方式选择， *低通1 *带通2 *高通3 *全通0
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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendString(request);

    }

    //发送带通控制命令
    public void clickDaitong() {
        clickTongNum = 2;
        int[] ints = {96, 2, 0};

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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendString(request);

    }

    //发送高通控制命令
    public void clickGaotong() {
        clickTongNum = 3;
        int[] ints = {96, 3, 0};

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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendString(request);

    }

    //发送全通控制命令
    public void clickQuantong() {
        clickTongNum = 0;
        int[] ints = {96, 4, 0};

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
        Constant.CurrentFilterParam = request;  //GC2.01.006 蓝牙重连功能优化
        sendString(request);

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
            PrefUtils.setString(BaseActivity.this, AppConfig.CLICK_MODE, "notClicked");     //GC20181116
            mAudioTrack.release();// 关闭并释放资源
            try {
                mSocket.close();
                if (mInputStream != null)
                    mInputStream.close();
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
//GC20171129  修改蓝牙输入流的处理，缩短声音播放的延时问题
//GC20171205  添加磁场左右判断的功能

//GC20180412  获取声音特征并预测
//GC20180504  训练声音特征生成model
//GC20180417  是否是故障点声音

//GC20180428  画声音波形的位置改变，提前50个点
//GC20180707 声音特征值归一化计算修改

//GC20181106 声磁延时显示优化（光标定位、延时值计算）
//GC20181113 增益进度条显示bug修改
//GC20181116 模式切换时无需点击操作
//GC20181118 接收控制命令修改
//GC20181119 添加相关判断
//GC20181201 自动定位算法优化
//GC20181204 信号迭加平均处理

//GC20190123 智能识别提示优化
//GC201901231 去掉启动APP读取内存记录的模式操作
//GC201901232 用户模式提示框文字添加
//GC20190216 红色虚光标绘制情况修改
//GC20190218 专家界面延时值显示和相关结果一致起来（通过相关后计时自动定位数值不一致也不刷新延时值）
//GC20190307 词条和延时跳动效果
//GN20190407 硬件关闭重连功能添加
//GC20190422 "发现故障"提示音添加
//GC20190613 重连主提示框提示
//GC20190627 触发灯闪烁bug原因

//GN去掉
//GC2.01.005 界面无缝切换
//GC2.01.006 蓝牙重连功能优化
//GC2.01.007 蓝牙重连提示优化
