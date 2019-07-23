package com.kehui.www.testapp.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.kehui.www.testapp.application.AppConfig;
import com.kehui.www.testapp.application.MyApplication;

import java.util.Locale;

/**
 * @author jwj
 * @date 2018/07/11
 */
public class MultiLanguageUtil {

    private static MultiLanguageUtil instance;
    private Context mContext;

    public static void init(Context mContext) {
        if (instance == null) {
            synchronized (MultiLanguageUtil.class) {
                if (instance == null) {
                    instance = new MultiLanguageUtil(mContext);
                }
            }
        }
    }

    public static MultiLanguageUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You must be init MultiLanguageUtil first");
        }
        return instance;
    }

    private MultiLanguageUtil(Context context) {
        this.mContext = context;
    }

    /**
     * 设置语言
     */
    private void setConfiguration() {
        Locale targetLocale = getLanguageLocale();
        Configuration configuration = mContext.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
        } else {
            configuration.locale = targetLocale;
        }
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        //语言更换生效的代码!
        resources.updateConfiguration(configuration, dm);
    }

    private final Locale Locale_Spanisch = new Locale("Es", "es", "");

    /**
     * @return  如果不是英文、简体中文、繁体中文，默认返回简体中文
     */
    private Locale getLanguageLocale() {
        String languageType = PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys");
        switch (languageType) {
            case "follow_sys":
                return getSysLocale();
            case "en":
                return Locale.ENGLISH;
            case "ch":
                return Locale.SIMPLIFIED_CHINESE;
            case "de":
                return Locale.GERMANY;
            case "fr":
                return Locale.FRANCE;
            case "es":
                return Locale_Spanisch;
            default:
                break;
        }
        getSystemLanguage(getSysLocale());
        return Locale.SIMPLIFIED_CHINESE;
    }

    private String getSystemLanguage(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * @return  7.0以上获取方式需要特殊处理一下
     */
    private Locale getSysLocale() {
        if (Build.VERSION.SDK_INT < 24) {
            return mContext.getResources().getConfiguration().locale;
        } else {
            return mContext.getResources().getConfiguration().getLocales().get(0);
        }
    }

    /**
     * @param languageType  更新语言
     */
    public void updateLanguage(String languageType) {
        PrefUtils.setString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE,languageType);
        MultiLanguageUtil.getInstance().setConfiguration();
    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context);
        } else {
            MultiLanguageUtil.getInstance().setConfiguration();
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getInstance().getLanguageLocale();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

}
