package com.brew.e.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.brew.e.DownloadImageTask;
import com.brew.e.LockScreenAppActivity;
import com.brew.e.Constants;

public class LockScreenReceiver extends BroadcastReceiver {
    public static Long currentVersion = -1l;
    Handler handler = new Handler();
    Runnable currentRunnable = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        new DownloadImageTask(context).execute(false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_overlay_switch", false)) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if(currentRunnable == null) {
                    currentRunnable = new LockScreenStarter(context);
                    int sleepTime = sharedPref.getInt("pref_sleep_time", 5) * 1000;
                    handler.postDelayed(currentRunnable, sleepTime);
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                if(currentRunnable != null) {
                    handler.removeCallbacks(currentRunnable);
                    currentRunnable = null;
                }
            }
        }
    }

    public class LockScreenStarter implements Runnable {
        private Context mContext;
        public LockScreenStarter (Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            Intent startActivityIntent = new Intent(mContext, LockScreenAppActivity.class);
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(startActivityIntent);
        }
    }
}