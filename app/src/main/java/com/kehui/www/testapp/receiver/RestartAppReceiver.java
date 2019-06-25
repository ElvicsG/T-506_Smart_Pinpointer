package com.kehui.www.testapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kehui.www.testapp.view.SplashActivity;

/**
 * Created by jwj on 2018/4/9.
 */

public class RestartAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        int type = intent.getIntExtra("type", 0);
//        if (type == 0) {//语言切换

        Intent intentSplash = new Intent(context, SplashActivity.class);
        intentSplash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentSplash);
        System.exit(0);

//        } else { //专家和用户模式切换
//            String jumpActivity = intent.getStringExtra("name");
//            Intent intentSplash = null;
//            if (jumpActivity.equals("expert")) {
//                intentSplash = new Intent(context, MainActivity.class);
//            } else {
//                intentSplash = new Intent(context, UserMainActivity.class);
//            }
//            intentSplash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intentSplash);
//        }
    }
}
