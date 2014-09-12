package com.brew.projecteye;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.brew.projecteye.receiver.LockScreenReceiver;

public class LockScreenService extends Service{
    BroadcastReceiver mReceiver;
    public static boolean ScreenShown = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.setPriority(999);
        mReceiver = new LockScreenReceiver();
        Log.d("Time", "Register receiver");
        registerReceiver(mReceiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        Log.d("Time", "Unregister receiver");
        super.onDestroy();
    }
}
