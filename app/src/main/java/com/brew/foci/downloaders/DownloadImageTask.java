package com.brew.foci.downloaders;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.brew.foci.Constants;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
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
    private static Long THRESHOLD_TIME_FOR_DELETION = 24*60*60*1000l; // 24 hours in millis

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
            String photographerName = jObject.getString("photographer");
            Long newestVersion = Long.parseLong(versionString);
            if(!newestVersion.equals(currentVersion) || forceDownload[0]) {
                HttpGet imageHttpGet = new HttpGet(Constants.SERVER_DOWNLOAD_IMAGE_URL + newestVersion);
                HttpResponse imageExecute = client.execute(imageHttpGet);
                InputStream imageContent = imageExecute.getEntity().getContent();
                Bitmap bm = BitmapFactory.decodeStream(imageContent);
                saveImage(mContext, bm, newestVersion.toString());
                Constants.setLatestImageVersion(mContext, newestVersion);
                Constants.setPhotographerName(mContext, newestVersion, photographerName);
                String storedFiles = Constants.getStoredFiles(mContext);
                if(TextUtils.isEmpty(storedFiles)) {
                    JSONArray jsonStoredFiles = new JSONArray();
                    jsonStoredFiles.put(newestVersion);
                    JSONObject jsonStoredFilesObject = new JSONObject();
                    jsonStoredFilesObject.put("storedFiles", jsonStoredFiles);
                    Constants.setStoredFiles(mContext, jsonStoredFilesObject.toString());
                } else {
                    JSONObject jsonStoredFilesObject = new JSONObject(storedFiles);
                    JSONArray jsonStoredFiles = jsonStoredFilesObject.getJSONArray("storedFiles");
                    JSONArray newJsonStoredFiles = new JSONArray();
                    for(int i = 0 ; i < jsonStoredFiles.length() ; i++) {
                        Long timestamp = jsonStoredFiles.getLong(i);
                        if(!timestamp.equals(currentVersion) && timestamp < System.currentTimeMillis() - THRESHOLD_TIME_FOR_DELETION) {
                            mContext.deleteFile(timestamp.toString());
                        } else {
                            newJsonStoredFiles.put(timestamp);
                        }
                    }
                    newJsonStoredFiles.put(newestVersion);
                    jsonStoredFilesObject.put("storedFiles", newJsonStoredFiles);
                    Constants.setStoredFiles(mContext, jsonStoredFilesObject.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
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
