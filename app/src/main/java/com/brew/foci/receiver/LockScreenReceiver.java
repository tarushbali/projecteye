package com.brew.foci.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.brew.foci.Constants;
import com.brew.foci.downloaders.DownloadImageTask;
import com.brew.foci.downloaders.DownloadNumLikesTask;
import com.brew.foci.LockScreenAppActivity;

public class LockScreenReceiver extends BroadcastReceiver {
    Handler handler = new Handler();
    Runnable currentRunnable = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        new DownloadImageTask(context).execute(false);
        new DownloadNumLikesTask(context).execute();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPref.getBoolean("pref_overlay_switch", false) && !Constants.isImageDismissed(context, Constants.getLatestImageVersion(context))) {
            if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                if(currentRunnable == null) {
                    currentRunnable = new LockScreenStarter(context);
                    int sleepTime = Integer.parseInt(sharedPref.getString("pref_overlay_sleep_time", Constants.DEFAULT_OVERLAY_SLEEP_TIME)) * 1000;
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