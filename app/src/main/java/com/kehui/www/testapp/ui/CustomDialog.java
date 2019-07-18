package com.kehui.www.testapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kehui.www.testapp.R;

/**
 * @author jwj  /   zhuwentao
 * @date 2018/04/16     /   2016-08-19.
 */
public class CustomDialog extends Dialog {

    /**
     * 提示
     */
    protected TextView hintTv;

    /**
     * 左边按钮
     */
    protected Button doubleLeftBtn;

    /**
     * 右边按钮
     */
    protected Button doubleRightBtn;
    private final WindowManager wm;

    public LinearLayout llFilter;
    public RadioGroup rgFilter1;
    public RadioGroup rgFilter2;
    public RadioButton rbDiTong;
    public RadioButton rbDaiTong;
    public RadioButton rbGaoTong;
    public RadioButton rbQuanTong;

    public CustomDialog(Context context) {
        super(context, R.style.CustomDialogStyle);
        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_layout);
        hintTv = (TextView) findViewById(R.id.tv_notice_text);
        doubleLeftBtn = (Button) findViewById(R.id.btn_confirm);
        doubleRightBtn = (Button) findViewById(R.id.btn_cancel);
        llFilter = (LinearLayout) findViewById(R.id.ll_filter);
        rgFilter1 = (RadioGroup) findViewById(R.id.rg_filter1);
        rgFilter2 = (RadioGroup) findViewById(R.id.rg_filter2);
        rbDiTong = (RadioButton) findViewById(R.id.rb_di);
        rbDaiTong = (RadioButton) findViewById(R.id.rb_dai);
        rbGaoTong = (RadioButton) findViewById(R.id.rb_gao);
        rbQuanTong = (RadioButton) findViewById(R.id.rb_quan);

        Window win = getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = (int) (wm.getDefaultDisplay().getWidth() * 0.6);
        lp.height = (int) (wm.getDefaultDisplay().getHeight() * 0.6);
        win.setAttributes(lp);

    }

    /**
     * 设置右键文字和点击事件
     *
     * @param rightStr      文字
     * @param clickListener 点击事件
     */
    public void setRightButton(String rightStr, View.OnClickListener clickListener) {
        doubleRightBtn.setOnClickListener(clickListener);
        doubleRightBtn.setText(rightStr);
    }

    public void setRightGone() {
        doubleRightBtn.setVisibility(View.GONE);
    }

    public void setFilterVisible() {
        llFilter.setVisibility(View.VISIBLE);
    }

    public void setTextGone() {
        hintTv.setVisibility(View.GONE);
    }

//    public void setRadioGroup1(RadioGroup.OnCheckedChangeListener checkedChangeListener) {
//        rgFilter1.setOnCheckedChangeListener(checkedChangeListener);
//    }

    public void setRadioGroup(RadioGroup.OnCheckedChangeListener checkedChangeListener) {
        rgFilter2.setOnCheckedChangeListener(checkedChangeListener);
        rgFilter1.setOnCheckedChangeListener(checkedChangeListener);
    }
    public void clearFilter1(){
        rgFilter1.clearCheck();
    }
    public void clearFilter2(){
        rgFilter2.clearCheck();
    }

    /**
     * 设置左键文字和点击事件
     *
     * @param leftStr       文字
     * @param clickListener 点击事件
     */
    public void setLeftButton(String leftStr, View.OnClickListener clickListener) {
        doubleLeftBtn.setOnClickListener(clickListener);
        doubleLeftBtn.setText(leftStr);
    }

    /**
     * 设置提示内容
     *
     * @param str 内容
     */
    public void setHintText(String str) {
        hintTv.setText(str);
        hintTv.setVisibility(View.VISIBLE);
    }

    /**
     * 给两个按钮 设置文字
     *
     * @param leftStr  左按钮文字
     * @param rightStr 右按钮文字
     */
    public void setBtnText(String leftStr, String rightStr) {
        doubleLeftBtn.setText(leftStr);
        doubleRightBtn.setText(rightStr);
    }
}
