package com.kehui.www.testapp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.ui.CustomDeviceListDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Gong
 * @date 2021/07/13 //GC20210713
 */
public class CustomDeviceListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> deviceList;
    private int type;

    public CustomDeviceListAdapter(Context context, ArrayList<String> deviceList, int type) {
        this.context = context;
        this.deviceList = deviceList;
        this.type = type;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.headphone_name, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        if (type == 0) {
            //未配对
            holder.ivDeviceStatus.setImageResource(R.drawable.ic_no_paired_device);
        } else {
            //已配对
            holder.ivDeviceStatus.setImageResource(R.drawable.ic_paired);
        }
        holder.tvDeviceName.setText(deviceList.get(position));

        return view;
    }

    public static class ViewHolder {
        @BindView(R.id.tv_device_name)
        public TextView tvDeviceName;
        @BindView(R.id.iv_device_status)
        public ImageView ivDeviceStatus;
        
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
