package com.kehui.www.testapp.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kehui.www.testapp.R;

/**
 * @author Gong
 * @date 2019/07/22
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvVersion = (TextView) this.findViewById(R.id.tv_version);
        tvVersion.setText(getResources().getString(R.string.version_code) + getVerCode(this));

        ImageButton ibtBack = (ImageButton) this.findViewById(R.id.back);
        ibtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /**
     * @param context   运行环境、场景
     * @return  软件版本名称
     */
    public String getVerCode(Context context) {
        String verCode = "";
        try {
            //注意："com.example.try_downloadfile_progress"对应AndroidManifest.xml里的package="……"部分
            verCode = context.getPackageManager().getPackageInfo(
                    "com.kehui.www.testapp", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("msg", e.getMessage());
        }
        return verCode;
    }

}
