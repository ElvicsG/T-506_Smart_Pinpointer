package com.kehui.www.testapp.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import com.kehui.www.testapp.event.RestartGetStreamEvent;
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

    private BluetoothSocket bluetoothSocket = null;

    /**
     * 获得本设备的蓝牙适配器实例      返回值：如果设备具备蓝牙功能，返回BluetoothAdapter 实例；否则，返回null对象
     */
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 宏定义查询设备句柄
     */
    private final static int REQUEST_CONNECT_DEVICE = 1;
    protected static final int REQUEST_ENABLE = 0;
    private ArrayAdapter<String> pairedDevicesArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seek_device);
        ButterKnife.bind(this);
        initView();
        init();
        Log.e(TAG, "进入!");
    }

    private void initView() {
        //设置字体
        Typeface type = Typeface.createFromAsset(tvAppName.getContext().getAssets(), "founderBlack.ttf");
        tvAppName.setTypeface(type);
        Typeface type2 = Typeface.createFromAsset(btSeekDevice.getContext().getAssets(), "microsoft_black.ttf");
        btSeekDevice.setTypeface(type2);
    }

    private void init() {
        pairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name2);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.does_not_find_device), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //如果蓝牙服务不可用则提示
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.open_bluetooth_ing), Toast.LENGTH_SHORT).show();

            new Thread() {
                @Override
                public void run() {
                    if (!bluetoothAdapter.isEnabled()) {
                        bluetoothAdapter.enable();
                    }
                }
            }.start();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getResources().getString(R.string.wait_for_Bluetooth_to_open_5seconds_after_trying_to_connect), Toast.LENGTH_SHORT).show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!bluetoothAdapter.isEnabled()) {
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

    public void clickSeekDevice(View view) {
        connect();
    }

    private void connect() {
        //如果蓝牙服务不可用则提示
        if (!bluetoothAdapter.isEnabled()) {
            //询问打开蓝牙
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enabler, REQUEST_ENABLE);
            return;
        }
        //如未连接设备则打开DeviceListActivity进行设备搜索
        if (bluetoothSocket == null) {
            pairedDevicesArrayAdapter.clear();
            //跳转程序设置
            Intent serverIntent = new Intent(SeekDeviceActivity.this, DeviceListActivity.class);
            //设置返回宏定义
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
    }

    public void disconnect() {
        SharedPreferences.Editor shareData = getSharedPreferences("Add", 0).edit();
        shareData.clear();
        shareData.apply();
        pairedDevicesArrayAdapter.clear();
        Toast.makeText(this, getResources().getString(R.string.The_line_has_been_disconnected_please_re_connect), Toast.LENGTH_SHORT).show();
        //关闭连接socket
        try {
            MyApplication.getInstances().getBluetoothSocket().close();
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
                    //MAC地址，由DeviceListActivity设置返回
                    String address = Objects.requireNonNull(data.getExtras()).getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    //通过MAC地址获取蓝牙设备
                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                    // 用服务号得到socket // SPP服务UUID号
                    try {
                        bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        MyApplication.getInstances().setBluetoothSocket(bluetoothSocket);
                        MyApplication.getInstances().setBluetoothDevice(bluetoothDevice);
                        MyApplication.getInstances().setBluetoothAdapter(bluetoothAdapter);

                    } catch (IOException e) {
                        Toast.makeText(this, getResources().getString(R.string.Connection_failed_unable_to_get_Socket) + e, Toast.LENGTH_SHORT).show();
                    }
                    // 连接socket
                    try {
                        bluetoothSocket.connect();
                        Toast.makeText(this, getResources().getString(R.string.connect) + " " + bluetoothDevice.getName() + " " + getResources().getString(R.string.success), Toast.LENGTH_SHORT).show();
                        pairedDevicesArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
                        SharedPreferences.Editor shareData = getSharedPreferences("Add", 0).edit();
                        shareData.putString(String.valueOf(0), bluetoothDevice.getName());
                        shareData.putString(String.valueOf(1), bluetoothDevice.getAddress());
                        shareData.apply();
                        //进入主页面
                        showMain();
                    } catch (IOException e) {
                        try {
                            Toast.makeText(this, getResources().getString(R.string.Connection_failed) + getResources().getString(R.string.demo_notice), Toast.LENGTH_LONG).show();
                            showMain();
                            bluetoothSocket.close();
                            bluetoothSocket = null;

                        } catch (IOException ignored) {
                        }
                        return;
                    }
                }
                break;
            case 100:
                //G?
                disconnect();
                try {
                    MyApplication.getInstances().getBluetoothSocket().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            default:
                break;
        }
    }

    private void showMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, 100);
        finish();
    }

}
