package com.brew.e;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.brew.e.receiver.LockScreenReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by tbali on 9/14/14.
 */
public class DownloadImageTask extends AsyncTask<Boolean, Integer, String> {
    private Context mContext;

    public DownloadImageTask(Context context) {
        mContext = context;
    }

    protected String doInBackground(Boolean... forceDownload) {
        SharedPreferences settings = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        Long currentVersion = settings.getLong(Constants.LATEST_VERSION_KEY, 0);
        String versionResponse = "";
        // Get current version
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(Constants.SERVER_LATEST_VERSION_URL);
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while ((s = buffer.readLine()) != null) {
                versionResponse += s;
            }
            JSONObject jObject = new JSONObject(versionResponse);
            String versionString = jObject.getString("version");
            Long newestVersion = Long.parseLong(versionString);
            if(!newestVersion.equals(currentVersion) || forceDownload[0]) {
                HttpGet imageHttpGet = new HttpGet(Constants.SERVER_DOWNLOAD_IMAGE_URL + newestVersion);
                HttpResponse imageExecute = client.execute(imageHttpGet);
                InputStream imageContent = imageExecute.getEntity().getContent();
                Bitmap bm = BitmapFactory.decodeStream(imageContent);
                saveImage(mContext, bm, newestVersion.toString());
                SharedPreferences.Editor editor = settings.edit();
                editor.putLong(Constants.LATEST_VERSION_KEY, newestVersion);
                editor.commit();
                LockScreenReceiver.currentVersion = newestVersion;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        //textView.setText(result);
    }


    public void saveImage(Context context, Bitmap bitmap, String name){
        FileOutputStream out;
        try {
            out = context.openFileOutput(name, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
