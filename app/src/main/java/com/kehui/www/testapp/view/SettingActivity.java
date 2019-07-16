package com.kehui.www.testapp.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kehui.www.testapp.R;
import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.MyApplication;
import com.kehui.www.testapp.ui.CustomDialog;
import com.kehui.www.testapp.util.MultiLanguageUtil;
import com.kehui.www.testapp.util.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Gong
 * @date 2019/07/16
 */
public class SettingActivity extends BaseActivity {

    @BindView(R.id.ll_language)
    LinearLayout llLanguage;
    @BindView(R.id.tv_current_version_text)
    TextView tvCurrentVersionText;
    @BindView(R.id.tv_colon)
    TextView tvColon;
    @BindView(R.id.tv_version_value)
    TextView tvVersionValue;
    @BindView(R.id.tv_software_notice)
    TextView tvSoftwareNotice;
    @BindView(R.id.rl_soft_upgrade)
    RelativeLayout rlSoftUpgrade;
    @BindView(R.id.iv_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_address)
    TextView tvAddress;
    @BindView(R.id.ll_about_us)
    LinearLayout llAboutUs;
    @BindView(R.id.btn_language)
    LinearLayout btnLanguage;
    @BindView(R.id.btn_software_upgrade)
    LinearLayout btnSoftwareUpgrade;
    @BindView(R.id.btn_about_us)
    LinearLayout btnAboutUs;
    @BindView(R.id.btn_back)
    LinearLayout btnBack;
    @BindView(R.id.rb_zh)
    RadioButton rbZh;
    @BindView(R.id.rb_en)
    RadioButton rbEn;
    @BindView(R.id.rb_de)
    RadioButton rbDe;
    @BindView(R.id.rb_fr)
    RadioButton rbFr;
    @BindView(R.id.rb_spain)
    RadioButton rbSpain;
    @BindView(R.id.rg_language)
    RadioGroup rgLanguage;
    @BindView(R.id.btn_switch)
    Button btnSwitch;
    @BindView(R.id.tv_update_version)
    TextView tvUpdateVersion;
    @BindView(R.id.rb_follow_sys)
    RadioButton rbFollowSys;
    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    private String currentLanguage;
    private CustomDialog customDialog;
    public static SettingActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        instance = this;
        initView();
    }

    private void initView() {
        currentLanguage = PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys");
        switch (currentLanguage) {
            case "follow_sys":
                rbFollowSys.setChecked(true);
                break;
            case "ch":
                rbZh.setChecked(true);
                break;
            case "en":
                rbEn.setChecked(true);
                break;
            case "de":
                rbDe.setChecked(true);
                break;
            case "fr":
                rbFr.setChecked(true);
                break;
            case "es":
                rbSpain.setChecked(true);
                break;
            default:
                break;
        }
        tvVersionValue.setText(getVerCode(SettingActivity.this));
        rgLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == rbFollowSys.getId()) {
                    currentLanguage = "follow_sys";
                } else if (checkedId == rbZh.getId()) {
                    currentLanguage = "ch";
                } else if (checkedId == rbEn.getId()) {
                    currentLanguage = "en";
                } else if (checkedId == rbDe.getId()) {
                    currentLanguage = "de";
                } else if (checkedId == rbFr.getId()) {
                    currentLanguage = "fr";
                } else if (checkedId == rbSpain.getId()) {
                    currentLanguage = "es";
                }
            }
        });
    }

    /**
     * 弹出对话框，提示用户更新
     */
    protected void showLanguageDialog() {
        customDialog = new CustomDialog(SettingActivity.this);
        customDialog.show();

        customDialog.setHintText(getString(R.string.switch_language_success_restart_app));
        customDialog.setLeftButton(getString(R.string.yes), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                setLanguage(currentLanguage);
            }
        });
        customDialog.setRightButton(getString(R.string.no), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });
    }

    private void setLanguage(String language) {
        switch (language) {
            case "follow_sys":
                MultiLanguageUtil.getInstance().updateLanguage("follow_sys");
                break;
            case "ch":
                MultiLanguageUtil.getInstance().updateLanguage("ch");
                break;
            case "en":
                MultiLanguageUtil.getInstance().updateLanguage("en");
                break;
            case "de":
                MultiLanguageUtil.getInstance().updateLanguage("de");
                break;
            case "fr":
                MultiLanguageUtil.getInstance().updateLanguage("fr");
                break;
            case "es":
                MultiLanguageUtil.getInstance().updateLanguage("es");
                break;
            default:
                break;
        }
        Intent intent = new Intent();
        intent.setAction("restartapp");
        sendBroadcast(intent);
        if (MainActivity.instance != null) {
            MainActivity.instance.finish();
        }
        if (UserMainActivity.instance != null) {
            UserMainActivity.instance.finish();
        }
//        finish();
    }

    @OnClick({R.id.btn_language, R.id.btn_software_upgrade, R.id.btn_about_us, R.id.btn_back, R.id.btn_switch, R.id.tv_update_version})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_language:
                llLanguage.setVisibility(View.VISIBLE);
                rlSoftUpgrade.setVisibility(View.GONE);
                llAboutUs.setVisibility(View.GONE);
                break;
            case R.id.btn_software_upgrade:
                llLanguage.setVisibility(View.GONE);
                rlSoftUpgrade.setVisibility(View.VISIBLE);
                llAboutUs.setVisibility(View.GONE);
                break;
            case R.id.btn_about_us:
                llLanguage.setVisibility(View.GONE);
                rlSoftUpgrade.setVisibility(View.GONE);
                llAboutUs.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_switch:
                if (!currentLanguage.equals(PrefUtils.getString(SettingActivity.this, AppConfig.CURRENT_LANGUAGE, "follow_sys"))) {
                    showLanguageDialog();
                }
                break;
            case R.id.tv_update_version:
                SplashActivity.instance.isToast = true;
                SplashActivity.instance.initUpdateApk();
                break;
            default:
                break;
        }
    }

    /**
     * @param context   //G?
     * @return  获取软件版本号
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

    /**
     * 弹出对话框，提示用户更新
     */
    public void showUpdateDialog() {
        try {
            customDialog = new CustomDialog(SettingActivity.this);
            customDialog.show();

            customDialog.setHintText(getString(R.string.version_update));
            customDialog.setLeftButton(getString(R.string.confirm), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SplashActivity.isCancel = true;
                    // 下载apk，，apk链接地址，downloadUrl
                    SplashActivity.DownloadApk(SettingActivity.this);
                    customDialog.dismiss();
                }
            });
            customDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
