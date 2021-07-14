package com.kehui.www.testapp.ui;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.adpter.CustomDeviceListAdapter;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 自定义对话框
 *
 * @author Gong
 * @date 2021/07/12
 */
public class CustomDeviceListDialog extends Dialog {
    @BindView(R.id.paired_devices)
    ListView pairedDevices;
    @BindView(R.id.new_devices)
    ListView newDevices;
    @BindView(R.id.ll_list)
    LinearLayout llList;
    @BindView(R.id.ll_no_device)
    LinearLayout llNoDevice;
    /**
     * 确认、取消按钮部分
     */
    private Button doubleLeftBtn;
    private Button doubleRightBtn;
    private final WindowManager wm;
    /**
     * MAC地址查找和显示
     */
    private BluetoothAdapter bluetoothAdapter;
    private CustomDeviceListAdapter pairedDevicesList;
    private CustomDeviceListAdapter newDevicesList;
    private ArrayList<String> deviceList;
    private ArrayList<String> deviceList2;
    public String headphonesAddress;
    public boolean getAddress;

    public CustomDeviceListDialog(Context context) {
        super(context, R.style.CustomDialogStyle);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_list_layout);
        ButterKnife.bind(this);

        Window win = getWindow();
        assert win != null;
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = (int) (wm.getDefaultDisplay().getWidth() * 0.7);
        lp.height = (int) (wm.getDefaultDisplay().getHeight() * 0.96);
        win.setAttributes(lp);

        doubleLeftBtn = (Button) findViewById(R.id.btn_device_confirm);
        doubleRightBtn = (Button) findViewById(R.id.btn_device_cancel);

        deviceList = new ArrayList<>();
        deviceList2 = new ArrayList<>();
        //初始化设备存储数组
        pairedDevicesList = new CustomDeviceListAdapter(getContext(), deviceList, 1);
        newDevicesList = new CustomDeviceListAdapter(getContext(), deviceList2, 0);
        //设置已配对设备列表
        pairedDevices.setAdapter(pairedDevicesList);
        pairedDevices.setOnItemClickListener(customDeviceClickListener);
        //设置未配对设备列表
        newDevices.setAdapter(newDevicesList);
        newDevices.setOnItemClickListener(customDeviceClickListener);

        //获得本设备的蓝牙适配器实例
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //得到已配对蓝牙设备列表信息
        final Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        //添加已配对设备到列表
        if (pairedDevices.size() > 0) {
            llNoDevice.setVisibility(View.GONE);
            llList.setVisibility(View.VISIBLE);
            this.pairedDevices.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device.getName() + "\n" + device.getAddress());
                pairedDevicesList.notifyDataSetChanged();
            }
        } else {
            llNoDevice.setVisibility(View.VISIBLE);
            llList.setVisibility(View.GONE);
        }

    }

    /**
     * 设置右边按键文字和点击事件
     *
     * @param rightStr      文字
     * @param clickListener 点击事件
     */
    public void setRightButton(String rightStr, View.OnClickListener clickListener) {
        doubleRightBtn.setOnClickListener(clickListener);
        doubleRightBtn.setText(rightStr);
    }

    /**
     * 设置左边按键文字和点击事件
     *
     * @param leftStr       文字
     * @param clickListener 点击事件
     */
    public void setLeftButton(String leftStr, View.OnClickListener clickListener) {
        doubleLeftBtn.setOnClickListener(clickListener);
        doubleLeftBtn.setText(leftStr);
    }

    /**
     * 开始服务和设备查找
     */
    public void doDiscovery() {
        // 显示其它设备（未配对设备）列表
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        // 关闭在进行的服务查找
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        //并重新开始
        bluetoothAdapter.startDiscovery();
    }

    /**
     * 点击列表选择设备时的响应函数
     */
    private AdapterView.OnItemClickListener customDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            //关闭服务查找
            bluetoothAdapter.cancelDiscovery();
            //得到MAC地址
            CustomDeviceListAdapter.ViewHolder holder = (CustomDeviceListAdapter.ViewHolder) v.getTag();
            String info = holder.tvDeviceName.getText().toString();
            //得到字符串“20:21:03:02:18:51” //GC20210714
            headphonesAddress = info.substring(info.length() - 17);
            getAddress = true;
            dismiss();
        }
    };

    /**
     * 查找到设备和搜索完成action监听器
     */
    public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 得到蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果是已配对的则略过，已得到显示，其余的在添加到列表中进行显示
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String str = device.getName() + "\n" + device.getAddress();
                    if (!deviceList2.contains(str)) {
                        //防止重复添加
                        deviceList2.add(str);
                    }
                    newDevicesList.notifyDataSetChanged();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (newDevicesList.getCount() == 0) {
                    llList.setVisibility(View.GONE);
                    llNoDevice.setVisibility(View.VISIBLE);
                } else {
                    llList.setVisibility(View.VISIBLE);
                    llNoDevice.setVisibility(View.GONE);
                }
            }
        }
    };

}
