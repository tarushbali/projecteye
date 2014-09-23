package com.brew.e.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.brew.e.LockScreenService;

/**
 * Created by tbali on 9/15/14.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, LockScreenService.class));
    }
}
