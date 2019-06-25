package com.kehui.www.testapp.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kehui.www.testapp.R;

public class AboutActivity extends BaseActivity {

    private TextView tvVersion;
    private ImageButton ibtBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        tvVersion = (TextView) this.findViewById(R.id.tv_version);
        tvVersion.setText(getResources().getString(R.string.version_code) + getVerCode(this));

        ibtBack = (ImageButton) this.findViewById(R.id.back);
        ibtBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
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
