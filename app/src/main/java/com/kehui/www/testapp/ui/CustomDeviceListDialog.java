package com.kehui.www.testapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kehui.www.testapp.R;

import butterknife.ButterKnife;

/**
 * 自定义对话框
 * @author Gong
 * @date 2021/07/12
 */
public class CustomDeviceListDialog extends Dialog {
    /**
     * 确认、取消按钮部分
     */
    private Button doubleLeftBtn;
    private Button doubleRightBtn;
    private final WindowManager wm;

    public CustomDeviceListDialog(Context context) {
        super(context, R.style.CustomDialogStyle);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_device_list_layout);
        ButterKnife.bind(this);

        doubleLeftBtn = (Button) findViewById(R.id.btn_device_confirm);
        doubleRightBtn = (Button) findViewById(R.id.btn_device_cancel);

        Window win = getWindow();
        assert win != null;
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = (int) (wm.getDefaultDisplay().getWidth() * 0.6);
        lp.height = (int) (wm.getDefaultDisplay().getHeight() * 0.9);
        win.setAttributes(lp);

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

}
