package com.cry.opengldemo5.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by xieguohua on 2019/6/21.
 */
public class ScreenListener {
    private Context mContext;
    private ScreenBroadcastReceiver mReceiver;
    private ScreenStateListener mScreenStateListener;

    public ScreenListener(Context context) {
        mContext = context;
        mReceiver = new ScreenBroadcastReceiver();
    }

    public void register(ScreenStateListener screenStateListener) {
        if (screenStateListener != null) {
            mScreenStateListener = screenStateListener;
        }
        if (mReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(mReceiver, filter);
        }
    }

    public void unregister() {
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && mScreenStateListener != null) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.d("data", "ScreenBroadcastReceiver --> ACTION_SCREEN_ON");
                    mScreenStateListener.onScreenOn();
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.d("data", "ScreenBroadcastReceiver --> ACTION_SCREEN_OFF");
                    mScreenStateListener.onScreenOff();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.d("data", "ScreenBroadcastReceiver --> ACTION_USER_PRESENT");
                    mScreenStateListener.onUserPresent();
                }
            }
        }
    }

    public interface ScreenStateListener {
        void onScreenOn();

        void onScreenOff();

        void onUserPresent();
    }
}
