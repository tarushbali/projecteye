package com.brew.foci.downloaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.brew.foci.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.InputStream;

/**
 * Created by tbali on 9/14/14.
 */
public class LikeImageTask extends AsyncTask<Long, Integer, String> {
    private Context mContext;

    public LikeImageTask(Context context) {
        mContext = context;
    }

    protected String doInBackground(Long... likedVersion) {
        SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        Long currentVersion = likedVersion[0];
        String versionResponse = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Constants.SERVER_LIKE_IMAGE_URL + currentVersion + "/" + Constants.getDeviceId(mContext));
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
    }
}
