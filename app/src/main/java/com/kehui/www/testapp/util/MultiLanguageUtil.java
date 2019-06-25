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
 * Created by jwj on 2018/7/11.
 */

public class MultiLanguageUtil {
    private static final String TAG = "MultiLanguageUtil";
    private static MultiLanguageUtil instance;
    private Context mContext;
    public static final String SAVE_LANGUAGE = "save_language";

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
    public void setConfiguration() {
        Locale targetLocale = getLanguageLocale();
//        if (targetLocale==Locale.CHINESE){
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "ch");
//        }else if (targetLocale==Locale.ENGLISH){
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "en");
//        }else if (targetLocale==Locale.SIMPLIFIED_CHINESE) {
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "ch");
//        }else if (targetLocale==Locale.GERMANY){
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "de");
//        }else if (targetLocale==Locale.FRANCE){
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "fr");
//        }else if (targetLocale==Locale_Spanisch){
//            PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, "es");
//        }
        Configuration configuration = mContext.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
        } else {
            configuration.locale = targetLocale;
        }
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);//语言更换生效的代码!
    }

    public final Locale Locale_Spanisch = new Locale("Es", "es", "");

    /**
     * 如果不是英文、简体中文、繁体中文，默认返回简体中文
     *
     * @return
     */
    private Locale getLanguageLocale() {
//        PrefUtils.setString(App.getInstances(), AppConfig.CURRENT_LANGUAGE, language);
        String languageType = PrefUtils.getString(MyApplication.getInstances(), AppConfig.CURRENT_LANGUAGE, "follow_sys");
        if (languageType.equals("follow_sys")) {
            Locale sysLocale=getSysLocale();
            return sysLocale;
        } else if (languageType.equals("en")) {
            return Locale.ENGLISH;
        } else if (languageType.equals("ch")) {
            return Locale.SIMPLIFIED_CHINESE;
        } else if (languageType.equals("de")) {
            return Locale.GERMANY;
        } else if (languageType.equals("fr")) {
            return Locale.FRANCE;
        } else if (languageType.equals("es")) {
            return Locale_Spanisch;
        }
//        Log.e(TAG, "getLanguageLocale" + languageType + languageType);
        getSystemLanguage(getSysLocale());
        return Locale.SIMPLIFIED_CHINESE;
    }

    private String getSystemLanguage(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();

    }

    //7.0以上获取方式需要特殊处理一下
    public Locale getSysLocale() {
        if (Build.VERSION.SDK_INT < 24) {
            return mContext.getResources().getConfiguration().locale;
        } else {
            return mContext.getResources().getConfiguration().getLocales().get(0);
        }
    }

    /**
     * 更新语言
     *
     * @param languageType
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
