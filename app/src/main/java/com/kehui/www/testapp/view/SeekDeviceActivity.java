package com.kehui.www.testapp.view;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.event.StartReadThreadEvent;
import com.kehui.www.testapp.ui.PercentLinearLayout;
import com.kehui.www.testapp.util.PrefUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SeekDeviceActivity extends BaseActivity {
    private final static int REQUEST_CONNECT_DEVICE = 1; // 宏定义查询设备句柄
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号
    protected static final int REQUEST_ENABLE = 0;
    BluetoothDevice _device = null; // 蓝牙设备
    BluetoothSocket _socket = null; // 蓝牙通信socket
    boolean bRun = true;
    @BindView(R.id.tv_app_name)
    TextView tvAppName;
    @BindView(R.id.bt_seek_device)
    Button btSeekDevice;
    @BindView(R.id.activity_seek_device)
    PercentLinearLayout activitySeekDevice;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Button mBtSeek;
    private Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_device);
        ButterKnife.bind(this);
        initView();
        init();


    }

    private void initView() {
        //设置字体
        Typeface type = Typeface.createFromAsset(tvAppName.getContext().getAssets(), "founderBlack.ttf");
        tvAppName.setTypeface(type);

        Typeface type2 = Typeface.createFromAsset(btSeekDevice.getContext().getAssets(), "microsoft_black.ttf");
        btSeekDevice.setTypeface(type2);
    }

    private void init() {
        mBtSeek = (Button) this.findViewById(R.id.bt_seek_device);

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name2);
        if (_bluetooth == null) {
            Toast.makeText(this, getResources().getString(R.string.does_not_find_device), Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }
        if (_bluetooth.isEnabled() == false) { // 如果蓝牙服务不可用则提示
            Toast.makeText(this, getResources().getString(R.string.open_bluetooth_ing),
                    Toast.LENGTH_SHORT).show();

            new Thread() {
                public void run() {
                    if (_bluetooth.isEnabled() == false) {
                        _bluetooth.enable();
                    }
                }
            }.start();
        }
        if (_bluetooth.isEnabled() == false) {

            Toast.makeText(this, getResources().getString(R.string.wait_for_Bluetooth_to_open_5seconds_after_trying_to_connect), Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (_bluetooth.isEnabled() == false) {

                        Toast.makeText(SeekDeviceActivity.this, getResources().getString(R.string.Automatically_open_Bluetooth_failure_please_manually_open_the_Bluetooth), Toast
                                .LENGTH_SHORT).show();
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enabler, REQUEST_ENABLE);
                    } else
                        connect(); //自动进入连接
                }
            }).start();
        } else {
            connect(); //自动进入连接
        }

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //disconnect();
            }
        }
    };

    public void clickSeekdevice(View view) {
        connect();
    }

    private void connect() {

        if (_bluetooth.isEnabled() == false) { // 如果蓝牙服务不可用则提示
            //询问打开蓝牙
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
            return;
        }

        // 如未连接设备则打开DeviceListActivity进行设备搜索
        if (_socket == null) {
            mPairedDevicesArrayAdapter.clear();
            Intent serverIntent = new Intent(SeekDeviceActivity.this,
                    DeviceListActivity.class); // 跳转程序设置
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义
        } else {
            //disconnect();
        }
        return;


    }

    public void disconnect() {
        //取消注册异常断开接收器
        //this.unregisterReceiver(mReceiver);
        SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
        sharedata.clear();
        sharedata.commit();
        mPairedDevicesArrayAdapter.clear();
        Toast.makeText(this, getResources().getString(R.string.The_line_has_been_disconnected_please_re_connect), Toast.LENGTH_SHORT).show();
        // 关闭连接socket
        try {
            bRun = false; // 一定要放在前面
            //is.close();
            MyApplication.getInstances().get_socket().close();
            //_socket = null;
            bRun = false;
//            btnadd.setText(getResources().getString(R.string.add));
        } catch (IOException e) {
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: // 连接结果，由DeviceListActivity设置返回
                // 响应返回结果
                if (resultCode == Activity.RESULT_OK) { // 连接成功，由DeviceListActivity设置返回
                    // MAC地址，由DeviceListActivity设置返回
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
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

                        Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();

                    }


                    // 连接socket
                    try {
                        _socket.connect();

                        Toast.makeText(this, getResources().getString(R.string.connect) + " " + _device.getName() + " " + getResources().getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                        mPairedDevicesArrayAdapter.add(_device.getName() + "\n"
                                + _device.getAddress());
                        SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
                        sharedata.putString(String.valueOf(0), _device.getName());
                        sharedata.putString(String.valueOf(1), _device.getAddress());
                        sharedata.commit();
                        //进入主页面
                        showMain();


                        //注册异常断开接收器  等连接成功后注册
                       /* IntentFilter filter = new IntentFilter(BluetoothDevice
                       .ACTION_ACL_DISCONNECTED);
                        this.registerReceiver(mReceiver, filter);*/
                        if (dialog != null)
                            dialog.dismiss();
                    } catch (IOException e) {
                        //btnadd.setText(getResources().getString(R.string.add));
                        if (dialog != null)
                            dialog.dismiss();
                        try {
                            showMain();
//                            Toast.makeText(this, getResources().getString(R.string.Connection_failed) + e, Toast.LENGTH_SHORT)
//                                    .show();
                            Toast.makeText(this, getResources().getString(R.string.Connection_failed) + getResources().getString(R.string.demo_notice), Toast.LENGTH_LONG)
                                    .show();
                            _socket.close();
                            _socket = null;

                            connectThread.start();
                        } catch (IOException ee) {
                        }
                        return;
                    }
                }
                break;
            case 100:
                if (dialog != null)
                    dialog.dismiss();
                disconnect();
                try {
                    MyApplication.getInstances().get_socket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            default:
                break;
        }
    }

    private void showMain() {
//        if (PrefUtils.getString(SeekDeviceActivity.this, AppConfig.CURRENT_MODE, "user").equals("expert")) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivityForResult(intent, 100);
//            finish();
//        } else {
//            Intent intent = new Intent(this, UserMainActivity.class);
//            startActivityForResult(intent, 100);
//            finish();
//        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 100);
        finish();   //GC201901231 模式切换更改
    }

    //是否尝试连接
    public boolean isFlag;

    //自动连接蓝牙线程
    Thread connectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!isFlag) {
                Log.e("蓝牙测试", "connectThread线程，尝试连接");
                reconnect();
//
//                try {
//                    Log.e("蓝牙测试", "connectThread线程，尝试连接");
//                    _socket = MyApplication.getInstances().get_socket();
//                    _socket.connect();
//                    mPairedDevicesArrayAdapter.add(_device.getName() + "\n"
//                            + _device.getAddress());
//                    SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
//                    sharedata.putString(String.valueOf(0), _device.getName());
//                    sharedata.putString(String.valueOf(1), _device.getAddress());
//                    sharedata.commit();
//                    Log.e("蓝牙测试", "connectThread线程，走到这里");
//                    isFlag = true;
//                } catch (Exception e) {
//                    Log.e("蓝牙测试", "connectThread线程，走到异常");
//                    try {
//                        _socket.close();
//                        _socket = null;
//                        Thread.sleep(10000);
//                    } catch (Exception e1) {
//                        e1.printStackTrace();
//                    }
//                    e.printStackTrace();
//                }
//                Toast.makeText(this, getResources().getString(R.string.connect) + " " + _device.getName() + " " + getResources().getString(R.string.success),
//                        Toast.LENGTH_SHORT).show();

            }
        }
    });

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

//            Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();

        }


        // 连接socket
        try {
            _socket.connect();

//            Toast.makeText(this, getResources().getString(R.string.connect) + " " + _device.getName() + " " + getResources().getString(R.string.success),
//                    Toast.LENGTH_SHORT).show();
            isFlag = true;
            mPairedDevicesArrayAdapter.add(_device.getName() + "\n"
                    + _device.getAddress());
            SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
            sharedata.putString(String.valueOf(0), _device.getName());
            sharedata.putString(String.valueOf(1), _device.getAddress());
            sharedata.commit();
            Log.e("蓝牙测试", "connectThread线程，走到这里");
            EventBus.getDefault().post(new StartReadThreadEvent(_device.getName()));

            //注册异常断开接收器  等连接成功后注册
                       /* IntentFilter filter = new IntentFilter(BluetoothDevice
                       .ACTION_ACL_DISCONNECTED);
                        this.registerReceiver(mReceiver, filter);*/
            if (dialog != null)
                dialog.dismiss();
        } catch (IOException e) {
            //btnadd.setText(getResources().getString(R.string.add));
            if (dialog != null)
                dialog.dismiss();
            try {
                _socket.close();
                _socket = null;
                Log.e("蓝牙测试", "connectThread线程，走到异常");
                Thread.sleep(10000);
            } catch (Exception ee) {
            }
            return;
        }
    }
}
