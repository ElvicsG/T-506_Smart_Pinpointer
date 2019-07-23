package com.kehui.www.testapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.kehui.www.testapp.R;

/**
 * 自定义透明的进度条
 * @author Gong
 * @date 2019/07/22
 */
public class ShowProgressDialog {

    /**
     * @param context   运行环境、场景
     * @return  得到自定义的progressDialog
     */
    public static Dialog createLoadingDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        // 得到加载view
        View v = inflater.inflate(R.layout.dialog_loading, null);
        // 加载布局
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
        // 创建自定义样式dialog
        Dialog loadingDialog = new Dialog(context, R.style.bg80FFDialog);
        // 不可以用“返回键”取消
        loadingDialog.setCancelable(false);
        // 设置布局
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        Window dialogWindow = loadingDialog.getWindow();
        assert dialogWindow != null;
        dialogWindow.setGravity(Gravity.CENTER);
        WindowManager.LayoutParams params = loadingDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        loadingDialog.getWindow().setAttributes(params);
        dialogWindow.setAttributes(params);
        return loadingDialog;
    }

}
