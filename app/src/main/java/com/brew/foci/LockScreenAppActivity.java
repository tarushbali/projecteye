package com.brew.foci;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.brew.foci.downloaders.DownloadImageTask;

import java.io.FileInputStream;

public class LockScreenAppActivity extends Activity {
    private CallStateListener mCallStateListener;
    /*
      * TODO: Delete old files
      */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Add necessary flags to show screen on top.
         */

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);

        /*
         * Get current version
         */
        final Long currentVersion = Constants.getLatestImageVersion(this);

        /*
         * Set the image. If none exists, set off task to download latest. Put default for now.
         */
        ImageView imageView = (ImageView) findViewById(R.id.lockScreenImage);
        Bitmap bm = getImageBitmap(this, currentVersion.toString());

        if (bm == null) {
            new DownloadImageTask(this).execute(true);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.defaultimage));
        } else {
            imageView.setImageBitmap(bm);
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LockScreenAppActivity.this.finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                return false;
            }
        });

        TextView authorTextView = (TextView) findViewById(R.id.authorDisplay);
        Typeface type = Typeface.createFromAsset(getAssets(),"frutiger.ttf");
        authorTextView.setTypeface(type);
        authorTextView.setText(Constants.getPhotographerName(this, currentVersion));

        mCallStateListener = new CallStateListener();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        Button dismissImageButton = (Button) findViewById(R.id.dismissImageButton);
        dismissImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Constants.setDismissImagePreference(getApplicationContext(), currentVersion);
                finish();
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                return true;
            }
        });

        /*
         * Like logic
         */
        final View likeAndNumberView = findViewById(R.id.likeButtonAndNumberView);
        final TextView likeCountTextView = (TextView) findViewById(R.id.numberOfLikes);
        final View likeButton = findViewById(R.id.likeButton);
        likeCountTextView.setText(Constants.getImageLikes(this, currentVersion).toString());
        likeCountTextView.setTextColor(getResources().getColor(R.color.white));
        likeCountTextView.setShadowLayer(0.4f, 2, 2, Color.BLACK);
        likeCountTextView.setTypeface(null, Typeface.BOLD);
        if(Constants.hasLikedImage(getApplicationContext(), currentVersion)) {
            likeCountTextView.setVisibility(View.VISIBLE);
            likeButton.setVisibility(View.INVISIBLE);
        }
        likeAndNumberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Constants.hasLikedImage(getApplicationContext(), currentVersion)) {
                    Constants.likeImage(getApplicationContext(), currentVersion);
                    Long numLikes = Long.parseLong(likeCountTextView.getText().toString()) + 1;
                    likeCountTextView.setText(numLikes.toString());
                    likeCountTextView.setVisibility(View.VISIBLE);
                    likeButton.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    public void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        TextView timeDisplay = (TextView) findViewById(R.id.timeDisplay);
        if (sharedPref.getBoolean("pref_overlay_show_clock", false)) {
            timeDisplay.setText(DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME));
        } else {
            timeDisplay.setText("");
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
}