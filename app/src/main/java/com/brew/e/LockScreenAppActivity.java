package com.brew.e;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brew.e.receiver.LockScreenReceiver;

import java.io.FileInputStream;
import java.util.TimeZone;

public class LockScreenAppActivity extends Activity implements GestureDetector.OnGestureListener {
    private GestureDetector gDetector;
    private CallStateListener mCallStateListener;
    private static final int SWIPE_MIN_DISTANCE = 50;
    // private static final int SWIPE_MAX_OFF_PATH = 200;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    /*
      * TODO: Delete old files
      */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCallStateListener = new CallStateListener();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        Long currentVersion = LockScreenReceiver.currentVersion;
        if(currentVersion == -1) {
            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
            currentVersion = settings.getLong(Constants.LATEST_VERSION_KEY, -1);
        }

        Bitmap bm = getImageBitmap(this, currentVersion.toString());
        if (bm == null) {
            new DownloadImageTask(this).execute(true);
        }

        ImageView imageView = (ImageView) findViewById(R.id.lockScreenImage);
        imageView.setImageBitmap(bm);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Button closeButton = (Button) findViewById(R.id.closeImageButton);
        closeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });
        gDetector = new GestureDetector(this);
    }

    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sharedPref.getBoolean("pref_overlay_show_clock", false)) {
            TextView timeDisplay = (TextView) findViewById(R.id.timeDisplay);
            timeDisplay.setText(DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        }
    }

    public Bitmap getImageBitmap(Context context, String name){
        try{
            FileInputStream fis = context.openFileInput(name);
            Bitmap b = BitmapFactory.decodeStream(fis);
            fis.close();
            return b;
        }
        catch(Exception e){
        }
        return null;
    }

    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    LockScreenAppActivity.this.finish();
                    break;
            }
        }
    }

    protected void onDestroy() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mCallStateListener, PhoneStateListener.LISTEN_NONE);
        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        LockScreenAppActivity.this.finish();
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            float diffAbs = Math.abs(e1.getY() - e2.getY());
            float diff = e1.getX() - e2.getX();
/*
            if (diffAbs > SWIPE_MAX_OFF_PATH)
                return false;
*/

            // Left swipe
            if (diff > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                LockScreenAppActivity.this.finish();
            }
            // Right swipe
            else if (-diff > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                LockScreenAppActivity.this.finish();
            }
        } catch (Exception e) {
            Log.e("Home", "Error on gestures");
        }
        return false;
    }
}