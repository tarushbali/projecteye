package com.brew.projecteye.receiver;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.brew.projecteye.Constants;
import com.brew.projecteye.DownloadImageTask;
import com.brew.projecteye.LockScreenAppActivity;
import com.brew.projecteye.LockScreenService;
import com.brew.projecteye.StartLockScreen;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LockScreenReceiver extends BroadcastReceiver {
    public static Long currentVersion = -1l;

    @Override
    public void onReceive(Context context, Intent intent) {
        new DownloadImageTask(context).execute(false);
        Log.d("Time", intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ? "Off" : "On");
        if (!LockScreenService.ScreenShown) {
            SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            if (settings.getBoolean(Constants.SHOW_BACKGROUND_SETTING_KEY, false)) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                        || intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
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