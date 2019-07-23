package com.kehui.www.testapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kehui.www.testapp.view.SplashActivity;

/**
 * @author jwj
 * @date 2018/04/09
 */
public class RestartAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentSplash = new Intent(context, SplashActivity.class);
        intentSplash.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentSplash);
        System.exit(0);
    }

}
