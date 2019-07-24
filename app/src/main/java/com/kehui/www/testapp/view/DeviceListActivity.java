package com.kehui.www.testapp.view;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.adpter.DeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Gong
 * @date 2019/07/22
 */
public class DeviceListActivity extends BaseActivity {

    private static final String TAG = "DeviceListActivity";

    @BindView(R.id.tv_app_name)
    TextView tvAppName;
    @BindView(R.id.paired_devices)
    ListView pairedDevices;
    @BindView(R.id.title_new_devices)
    TextView titleNewDevices;
    @BindView(R.id.new_devices)
    ListView newDevices;
    @BindView(R.id.button_scan)
    Button buttonScan;
    @BindView(R.id.button_cancel)
    Button buttonCancel;
    @BindView(R.id.tv_no_device)
    TextView tvNoDevice;
    @BindView(R.id.ll_list)
    LinearLayout llList;
    @BindView(R.id.ll_no_device)
    LinearLayout llNoDevice;

    /**
     * 返回时数据标签
     */
    public static String EXTRA_DEVICE_ADDRESS = "设备地址";

    // 成员域
    private BluetoothAdapter mBtAdapter;
    //    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
//    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private DeviceListAdapter mPairedDevicesArrayAdapter;
    private DeviceListAdapter mNewDevicesArrayAdapter;
    private ArrayList<String> deviceList;//已配对
    private ArrayList<String> deviceList2;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 创建并显示窗口
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  //设置窗口显示模式为窗口方式
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);
        initView();

        Log.e("蓝牙测试", "进入DeviceListActivity");
        // 设定默认返回值为取消
        setResult(Activity.RESULT_CANCELED);

        // 设定扫描按键响应
        Button scanButton = (Button) findViewById(R.id.button_scan);

        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        deviceList = new ArrayList<>();
        deviceList2 = new ArrayList<>();
        // 初使化设备存储数组
        mPairedDevicesArrayAdapter = new DeviceListAdapter(DeviceListActivity.this, deviceList, 1);
        mNewDevicesArrayAdapter = new DeviceListAdapter(DeviceListActivity.this, deviceList2, 0);

        // 设置已配队设备列表


        pairedDevices.setAdapter(mPairedDevicesArrayAdapter);
        pairedDevices.setOnItemClickListener(mDeviceClickListener);

        // 设置新查找设备列表
        newDevices.setAdapter(mNewDevicesArrayAdapter);
        newDevices.setOnItemClickListener(mDeviceClickListener);

        // 注册接收查找到设备action接收器
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // 注册查找结束action接收器
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        //注册异常断开接收器
        // filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //  this.registerReceiver(mReceiver, filter);
        // 得到本地蓝牙句柄
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // 得到已配对蓝牙设备列表
        final Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // 读取设置数据
        SharedPreferences sharedata1 = getSharedPreferences("Add", 0);
        String stringName = sharedata1.getString(String.valueOf(0), null);
        String stringAdd = sharedata1.getString(String.valueOf(1), null);

        // 添加已配对设备到列表并显示
        if (pairedDevices.size() > 0) {
            llNoDevice.setVisibility(View.GONE);
            llList.setVisibility(View.VISIBLE);
            this.pairedDevices.setVisibility(View.VISIBLE);
//            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);

            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
                mPairedDevicesArrayAdapter.notifyDataSetChanged();
                if (stringName != null && stringAdd != null) {
                    if (device.getName().equals(stringName) && device.getAddress().equals(stringAdd))  //如果是,自动连接
                    {
                        Toast.makeText(this, getResources().getString(R.string.Automatic_connection_please_wait), Toast.LENGTH_SHORT).show();
                        ReturnData(device.getAddress());
                    }
                }

            }
        } else {
//            String noDevices = "No devices have been paired";
//            mPairedDevicesArrayAdapter.add(noDevices);
            llNoDevice.setVisibility(View.VISIBLE);
            llList.setVisibility(View.GONE);
        }

    }

    private void initView() {
        //设置字体
        Typeface type = Typeface.createFromAsset(tvAppName.getContext().getAssets(), "founderBlack.ttf");
        tvAppName.setTypeface(type);
        Typeface type2 = Typeface.createFromAsset(tvNoDevice.getContext().getAssets(), "microsoft_black.ttf");
        tvNoDevice.setTypeface(type2);
        Typeface type3 = Typeface.createFromAsset(tvNoDevice.getContext().getAssets(), "microsoft_black.ttf");
        buttonScan.setTypeface(type3);
        Typeface type4 = Typeface.createFromAsset(tvNoDevice.getContext().getAssets(), "microsoft_black.ttf");
        buttonCancel.setTypeface(type4);
        //将字体设置成粗体
        TextPaint tp = tvAppName.getPaint();
        tp.setFakeBoldText(true);
        TextPaint tp2 = buttonScan.getPaint();
        tp2.setFakeBoldText(true);
        TextPaint tp3 = buttonCancel.getPaint();
        tp3.setFakeBoldText(true);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 关闭服务查找
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // 注销action接收器
        this.unregisterReceiver(mReceiver);
    }

    public void OnCancel(View v) {
        finish();
    }

    /**
     * 开始服务和设备查找
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // 在窗口显示查找中信息
        setProgressBarIndeterminateVisibility(true);
        setTitle(getResources().getString(R.string.Find_the_device));

        // 显示其它设备（未配对设备）列表
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // 关闭再进行的服务查找
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        //并重新开始
        mBtAdapter.startDiscovery();
    }

    // 选择设备响应函数
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // 准备连接设备，关闭服务查找
            mBtAdapter.cancelDiscovery();
            DeviceListAdapter.ViewHolder holder = (DeviceListAdapter.ViewHolder) v.getTag();
            // 得到mac地址
            String info = holder.tvDeviceName.getText().toString();
            String address = info.substring(info.length() - 17);

            ReturnData(address);
        }
    };

    private void ReturnData(String address) {
        // 设置返回数据
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

        // 设置返回值并结束程序
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // 查找到设备和搜索完成action监听器
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // 查找到设备action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 得到蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String str = device.getName() + "\n" + device.getAddress();
                    if (!deviceList2.contains(str))  //防止重复添加
                        deviceList2.add(str);
                    mNewDevicesArrayAdapter.notifyDataSetChanged();
                } else { // 添加到已配对设备列表
                    //	mPairedDevicesArrayAdapter.add(device.getName() + "\n"
                    //			+ device.getAddress());
                }
                // 搜索完成action
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(getResources().getString(R.string.Select_the_device_to_connect_to));
                if (mNewDevicesArrayAdapter.getCount() == 0) {
//                    String noDevices = getResources().getString(R.string.No_new_equipment_found);
//                    mNewDevicesArrayAdapter.add(noDevices);
                    llList.setVisibility(View.GONE);
                    llNoDevice.setVisibility(View.VISIBLE);
                } else {
                    llList.setVisibility(View.VISIBLE);
                    llNoDevice.setVisibility(View.GONE);
                }
                //   if(mPairedDevicesArrayAdapter.getCount() > 0)
                //  	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            }
          /*  else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Toast.makeText(DeviceListActivity.this, "线路已断开，请重新连接！", Toast.LENGTH_LONG).show();
            	  if (D) Log.d(TAG, "chen ACTION_ACL_DISCONNECTED");
            }*/
        }
    };


}
