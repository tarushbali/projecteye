package com.brew.foci;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import com.brew.foci.downloaders.DownloadNumLikesTask;
import com.brew.foci.downloaders.LikeImageTask;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @author tbali
 */
public class Constants {
    public static final String SERVER_LATEST_VERSION_URL = "http://arcane-anchorage-5123.herokuapp.com/latestVersion";
    public static final String SERVER_DOWNLOAD_IMAGE_URL = "http://arcane-anchorage-5123.herokuapp.com/download/";
    public static final String SERVER_IMAGE_LIKES_URL = "http://arcane-anchorage-5123.herokuapp.com/imageLikes/"; // imageLikes/timestamp
    public static final String SERVER_LIKE_IMAGE_URL = "http://arcane-anchorage-5123.herokuapp.com/likeImage/"; // likeImage/timestamp/deviceId
    public static final String PREFS_NAME = "Preferences";
    public static final String LATEST_VERSION_KEY = "LatestVersion";
    public static final String LATEST_PHOTOGRAPHER_NAME = "PhotographerFor";
    public static final String HAS_LIKED_IMAGE_PREFIX = "HasLikedImage_";
    public static final String PREF_DEVICE_ID = "DeviceId";
    public static final String LIKE_COUNT_PREFIX = "LikeCount_";
    public static final String HAS_DISMISSED_IMAGE = "HasDismissedImage_";
    public static final String DEFAULT_OVERLAY_SLEEP_TIME = "60"; //60 seconds
    public static final String STORED_FILES = "StoredFiles";

    public static void setPhotographerName(Context context, Long timestamp, String photographerName) {
        setStringPreference(context, Constants.LATEST_PHOTOGRAPHER_NAME + timestamp, photographerName);
    }

    public static String getPhotographerName(Context context, Long timestamp) {
        return getSharedPreferences(context).getString(Constants.LATEST_PHOTOGRAPHER_NAME + timestamp, "");
    }

    public static void setStoredFiles(Context context, String storedFiles) {
        setStringPreference(context, Constants.STORED_FILES, storedFiles);
    }

    public static String getStoredFiles(Context context) {
        return getSharedPreferences(context).getString(Constants.STORED_FILES, "");
    }

    public static void setLatestImageVersion(Context context, Long latestVersion) {
        setLongPreference(context, Constants.LATEST_VERSION_KEY, latestVersion);
    }

    public static Long getLatestImageVersion(Context context) {
        return getSharedPreferences(context).getLong(Constants.LATEST_VERSION_KEY, -1);
    }

    public static boolean isImageDismissed(Context context, Long currentImageTimestamp) {
        return getSharedPreferences(context).getBoolean(Constants.HAS_DISMISSED_IMAGE + currentImageTimestamp, false);
    }

    public static void setDismissImagePreference(Context context, Long currentImageTimestamp) {
        setBooleanPreference(context, Constants.HAS_DISMISSED_IMAGE + currentImageTimestamp, true);
    }

    public static void setImageLikes(Context context, Long currentImageTimestamp, Long likes) {
        Long currentLikes = getImageLikes(context, currentImageTimestamp);
        if(likes > currentLikes) {
            setLongPreference(context, Constants.LIKE_COUNT_PREFIX + currentImageTimestamp, likes);
        }
    }

    public static Long getImageLikes(Context context, Long currentImageTimestamp) {
        // Set off async task to get image likes and update shared pref. Return shared pref for now.
        new DownloadNumLikesTask(context).execute();
        return getSharedPreferences(context).getLong(Constants.LIKE_COUNT_PREFIX + currentImageTimestamp, -1);
    }

    public static void likeImage(Context context, Long currentImageTimestamp) {
        // Add 1 to shared pref. Like image online. Add pref to have liked image.
        new LikeImageTask(context).execute(currentImageTimestamp);
        setLongPreference(context, Constants.LIKE_COUNT_PREFIX + currentImageTimestamp, getImageLikes(context, currentImageTimestamp) + 1);
        setBooleanPreference(context, Constants.HAS_LIKED_IMAGE_PREFIX + currentImageTimestamp, true);
    }

    public static Boolean hasLikedImage(Context context, Long currentImageTimestamp) {
        return getSharedPreferences(context).getBoolean(Constants.HAS_LIKED_IMAGE_PREFIX + currentImageTimestamp, false);
    }

    public static String getDeviceId(Context context) {
        // first check if the deviceId is already saved in the preference
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);

        String deviceId = settings.getString(Constants.PREF_DEVICE_ID, "");

        if (TextUtils.isEmpty(deviceId)) {
            String androidId = getAndroidId(context);
            // ANDROID_ID is perfectly reliable on versions of Android <=2.1 or >=2.3. Only 2.2 has the problems
            // Several devices affected by the ANDROID_ID bug in 2.2 all have the same ANDROID_ID, which is 9774d56d682e549c
            if (TextUtils.isEmpty(androidId) || "9774d56d682e549c".equals(androidId)) {
                // For this case, we will simply generate a UUID as the deviceId.
                // For each new installation, this id will be different.
                deviceId = UUID.randomUUID().toString();
            } else {
                // If we have a good ANDROID_ID, then using ANDROID_ID as the seed to generate the UUID
                try {
                    deviceId = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            // persist in the preference
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Constants.PREF_DEVICE_ID, deviceId);
            editor.commit();
        }
        return deviceId;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static void setLongPreference(Context context, String key, Long value) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static void setStringPreference(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
