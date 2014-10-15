package com.brew.foci.downloaders;

import android.content.Context;
import android.os.AsyncTask;

import com.brew.foci.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tbali on 9/14/14.
 */
public class DownloadNumLikesTask extends AsyncTask<Void, Integer, Long> {
    private Context mContext;

    public DownloadNumLikesTask(Context context) {
        mContext = context;
    }

    protected Long doInBackground(Void... nothing) {
        Long currentVersion = Constants.getLatestImageVersion(mContext);
        String versionResponse = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Constants.SERVER_IMAGE_LIKES_URL + currentVersion);
        Long likeCount = -1l;
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while ((s = buffer.readLine()) != null) {
                versionResponse += s;
            }
            JSONObject jObject = new JSONObject(versionResponse);
            likeCount = jObject.getLong("likeCount");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Constants.setImageLikes(mContext, currentVersion, likeCount);
        return likeCount;
    }

    @Override
    protected void onPostExecute(Long result) {
    }
}
