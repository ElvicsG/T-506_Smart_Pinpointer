package com.kehui.www.testapp.view;

import android.app.Activity;
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
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.event.StartReadThreadEvent;
import com.kehui.www.testapp.ui.PercentLinearLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Gong
 * @date 2019/07/22
 */
public class SeekDeviceActivity extends BaseActivity {

    private static final String TAG = "SeekDeviceActivity";

    @BindView(R.id.tv_app_name)
    TextView tvAppName;
    @BindView(R.id.bt_seek_device)
    Button btSeekDevice;
    @BindView(R.id.activity_seek_device)
    PercentLinearLayout activitySeekDevice;

    BluetoothDevice device = null;
    BluetoothSocket socket = null;
    private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB"; // SPP服务UUID号
    public boolean needReconnect;

    /**
     * 宏定义查询设备句柄
     */
    private final static int REQUEST_CONNECT_DEVICE = 1;
    protected static final int REQUEST_ENABLE = 0;
    boolean bRun = true;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;
    private Button mBtSeek;


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
        //G?
        mBtSeek = (Button) this.findViewById(R.id.bt_seek_device);

        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name2);
        if (bluetooth == null) {
            Toast.makeText(this, getResources().getString(R.string.does_not_find_device), Toast.LENGTH_LONG)
                    .show();
            finish();
            return;
        }
        //如果蓝牙服务不可用则提示
        if (!bluetooth.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.open_bluetooth_ing),
                    Toast.LENGTH_SHORT).show();

            new Thread() {
                @Override
                public void run() {
                    if (!bluetooth.isEnabled()) {
                        bluetooth.enable();
                    }
                }
            }.start();
        }
        if (!bluetooth.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.wait_for_Bluetooth_to_open_5seconds_after_trying_to_connect), Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!bluetooth.isEnabled()) {
                        Toast.makeText(SeekDeviceActivity.this, getResources().getString(R.string.Automatically_open_Bluetooth_failure_please_manually_open_the_Bluetooth), Toast.LENGTH_SHORT).show();
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enabler, REQUEST_ENABLE);
                    } else {
                        //自动进入连接
                        connect();
                    }
                }
            }).start();
        } else {
            //自动进入连接
            connect();
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
        //如果蓝牙服务不可用则提示
        if (!bluetooth.isEnabled()) {
            //询问打开蓝牙
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
            return;
        }
        //如未连接设备则打开DeviceListActivity进行设备搜索
        if (socket == null) {
            pairedDevicesArrayAdapter.clear();
            //跳转程序设置
            Intent serverIntent = new Intent(SeekDeviceActivity.this, DeviceListActivity.class);
            //设置返回宏定义
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        } else {
            //disconnect();
        }

    }

    public void disconnect() {
        //取消注册异常断开接收器
        //this.unregisterReceiver(mReceiver);
        SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
        sharedata.clear();
        sharedata.apply();
        pairedDevicesArrayAdapter.clear();
        Toast.makeText(this, getResources().getString(R.string.The_line_has_been_disconnected_please_re_connect), Toast.LENGTH_SHORT).show();
        //关闭连接socket
        try {
            //一定要放在前面
            bRun = false;
            //is.close();
            MyApplication.getInstances().get_socket().close();
            //socket = null;
            bRun = false;
//            btnadd.setText(getResources().getString(R.string.add));
        } catch (IOException ignored) {
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            //连接结果，由DeviceListActivity设置返回
            case REQUEST_CONNECT_DEVICE:
                //响应返回结果——连接成功
                if (resultCode == Activity.RESULT_OK) {
                    // MAC地址，由DeviceListActivity设置返回
                    String address = Objects.requireNonNull(data.getExtras()).getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // 得到蓝牙设备句柄
                    device = bluetooth.getRemoteDevice(address);

                    // 用服务号得到socket
                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID
                                .fromString(MY_UUID));

                        MyApplication.getInstances().set_socket(socket);
                        MyApplication.getInstances().set_device(device);
                        MyApplication.getInstances().set_bluetooth(bluetooth);

                    } catch (IOException e) {
                        Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();
                    }

                    // 连接socket
                    try {
                        socket.connect();

                        Toast.makeText(this, getResources().getString(R.string.connect) + " " + device.getName() + " " + getResources().getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                        pairedDevicesArrayAdapter.add(device.getName() + "\n"
                                + device.getAddress());
                        SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
                        sharedata.putString(String.valueOf(0), device.getName());
                        sharedata.putString(String.valueOf(1), device.getAddress());
                        sharedata.apply();
                        //进入主页面
                        showMain();

                        //注册异常断开接收器  等连接成功后注册
                       /* IntentFilter filter = new IntentFilter(BluetoothDevice
                       .ACTION_ACL_DISCONNECTED);
                        this.registerReceiver(mReceiver, filter);*/
                    } catch (IOException e) {
                        //btnadd.setText(getResources().getString(R.string.add));
                        try {
                            showMain();
                            Toast.makeText(this, getResources().getString(R.string.Connection_failed) + getResources().getString(R.string.demo_notice), Toast.LENGTH_LONG)
                                    .show();
                            socket.close();
                            socket = null;
                            reconnectThread.start();

                        } catch (IOException ignored) {
                        }
                        return;
                    }
                }
                break;
            case 100:
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
        Intent intent = new Intent(this, MainActivity.class);
        //G?
        startActivityForResult(intent, 100);
        finish();
    }

    /**
     * 重连蓝牙线程
     */
    Thread reconnectThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (!needReconnect) {
                Log.e("蓝牙测试", "reconnectThread线程，尝试连接");
                reconnect();
            }
        }
    });

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
            socket.connect();
//            Toast.makeText(this, getResources().getString(R.string.connect) + " " + device.getName() + " " + getResources().getString(R.string
//                            .success), Toast.LENGTH_SHORT).show();
            needReconnect = true;
            pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            SharedPreferences.Editor sharedata = getSharedPreferences("Add", 0).edit();
            sharedata.putString(String.valueOf(0), device.getName());
            sharedata.putString(String.valueOf(1), device.getAddress());
            sharedata.apply();
            Log.e("蓝牙测试", "reconnectThread线程，走到这里");
            EventBus.getDefault().post(new StartReadThreadEvent(device.getName()));

        } catch (IOException e) {
            try {
                socket.close();
                socket = null;
                Log.e("蓝牙测试", "reconnectThread线程，走到异常");
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }

}
