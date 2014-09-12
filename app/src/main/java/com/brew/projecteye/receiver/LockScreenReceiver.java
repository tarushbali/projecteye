package com.brew.projecteye.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.brew.projecteye.LockScreenAppActivity;
import com.brew.projecteye.LockScreenService;
import com.brew.projecteye.StartLockScreen;

public class LockScreenReceiver extends BroadcastReceiver  {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Time", intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ? "Off" : "On");
        if(!LockScreenService.ScreenShown) {
            SharedPreferences settings = context.getSharedPreferences(StartLockScreen.PREFS_NAME, Context.MODE_PRIVATE);
            if (settings.getBoolean(StartLockScreen.SHOW_BACKGROUND_SETTING_KEY, false)) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                    || intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                    LockScreenService.ScreenShown = true;
                    Intent startActivityIntent = new Intent(context, LockScreenAppActivity.class);
                    startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(startActivityIntent);
                    Log.d("Time", "Receiver started activity");
                }
            }
        }
    }
}