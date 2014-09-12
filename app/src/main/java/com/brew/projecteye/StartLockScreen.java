package com.brew.projecteye;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.brew.projecteye.receiver.LockScreenReceiver;

public class StartLockScreen extends Activity {
    public static final String PREFS_NAME = "Preferences";
    public static final String SHOW_BACKGROUND_SETTING_KEY = "ShowBackground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        ToggleButton showBackgroundSwitch = (ToggleButton) findViewById(R.id.show_background);
        SharedPreferences settings = getSharedPreferences(StartLockScreen.PREFS_NAME, Context.MODE_PRIVATE);
        boolean showBackground = settings.getBoolean(StartLockScreen.SHOW_BACKGROUND_SETTING_KEY, false);
        showBackgroundSwitch.setChecked(showBackground);
        showBackgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SHOW_BACKGROUND_SETTING_KEY, isChecked);
                editor.commit();
            }
        });
        startService(new Intent(this, LockScreenService.class));
    }
}
