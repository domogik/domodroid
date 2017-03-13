package metrics;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.domogik.domodroid13.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Date;

import Abstract.pref_utils;
import applications.domodroid;


/**
 * Created by tiki on 29/10/2016.
 */

public class MetricsServiceReceiver extends BroadcastReceiver {
    private static final String mytag = "MetricsServiceReceiver";
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    private final DecimalFormat df = new DecimalFormat("#.##");
    private final JSONObject measurements = new JSONObject();
    private final JSONObject tags = new JSONObject();
    private static final JSONObject metrics = new JSONObject();
    private pref_utils prefUtils;

    @Override
    public void onReceive(Context context, Intent intent) {
        prefUtils = new pref_utils();
        new getmetrics().execute();
    }

    private class getmetrics extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                tags.put("component", "domodroid");
                tags.put("interface", "yes");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {

                Float api_version = prefUtils.GetDomogikApiVersion();
                try {
                    tags.put("domogik_api_version", api_version.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String domogik_version = prefUtils.GetDomogikVersion();
                try {
                    tags.put("domogik_version", domogik_version);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String versionName = BuildConfig.VERSION_NAME;
                try {
                    tags.put("domodroid_version_name", versionName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int versionCode = BuildConfig.VERSION_CODE;
                String vcs = "??";
                if (versionCode != -1)
                    vcs = Integer.toString(versionCode);
                try {
                    tags.put("domodroid_version_code", vcs);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int Android_code = Build.VERSION.SDK_INT;
                try {
                    tags.put("domodroid_sdk", String.valueOf(Android_code));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String Device = Build.DEVICE;
                try {
                    tags.put("domodroid_device", Device);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String deviceid = Settings.Secure.getString(domodroid.GetInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
                try {
                    metrics.put("id", deviceid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //process with update timer for new 30 sec
                Log.d(mytag, "New timer " + executeTop());
                try {
                    Runtime info = Runtime.getRuntime();
                    int availableProcessors = info.availableProcessors();
                    try {
                        tags.put("num_core", availableProcessors);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Float maxmemavailable = Float.valueOf(info.maxMemory());
                    maxmemavailable = maxmemavailable / 1024; //in KB
                    maxmemavailable = maxmemavailable / 1024; //in MB
                    measurements.put("memory_total", df.format(maxmemavailable));
                    measurements.put("unit", 1);
                    Float freeSize = Float.valueOf(info.freeMemory());
                    Float totalAllocatedSize = Float.valueOf(info.totalMemory());
                    Float usedSize = totalAllocatedSize - freeSize;
                    freeSize = freeSize / 1024; //in KB
                    freeSize = freeSize / 1024; //in MB
                    totalAllocatedSize = totalAllocatedSize / 1024; //in KB
                    totalAllocatedSize = totalAllocatedSize / 1024; //in MB
                    usedSize = usedSize / 1024; //in KB
                    usedSize = usedSize / 1024; //in MB
                    Log.d(mytag, "New timer totalAllocatedSize: " + df.format(totalAllocatedSize) + "MB");
                    Log.d(mytag, "New timer freeSize: " + df.format(freeSize) + "MB");
                    Log.d(mytag, "New timer usedSize: " + df.format(usedSize) + "MB");
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d(mytag, "error getting used memory :" + e.toString());
                }
                long millis = new Date().getTime();
                String gmtTime = String.valueOf(millis);
                gmtTime = gmtTime.substring(0, gmtTime.length() - 3) + "." + gmtTime.substring(gmtTime.length() - 3, gmtTime.length());
                try {
                    metrics.put("tags", tags);
                    metrics.put("timestamp", gmtTime);
                    metrics.put("measurements", measurements);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(mytag, "metrics=" + metrics.toString());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //Try to send metrics to domogik metrics server
            //String result = POST("http://metrics.domogik.org/metrics/");
            //Log.e(mytag, "result=" + result);

            return null;
        }
    }

    public static String POST(String url) {
        InputStream inputStream;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            StringEntity se = new StringEntity(metrics.toString());
            httpPost.setEntity(se);

            httpPost.setHeader("accept", "json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
                Log.d(mytag, "inputStream result" + result);
            } else
                result = "Did not work!";
            Log.d(mytag, "result" + result);

        } catch (Exception e) {
            Log.d(mytag, "e.getLocalizedMessage()" + e.getLocalizedMessage());
        }

        //  return result
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    private String executeTop() {
        java.lang.Process p = null;
        BufferedReader in = null;
        String returnString = null;
        try {
            p = Runtime.getRuntime().exec("top -n 1");
            in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (returnString == null || returnString.contentEquals("")) {
                returnString = in.readLine();
            }
        } catch (IOException e) {
            Log.e("executeTop", "error in getting first line of top");
            e.printStackTrace();
        } finally {
            try {
                in.close();
                p.destroy();
            } catch (IOException e) {
                Log.e("executeTop",
                        "error in closing and destroying top process");
                e.printStackTrace();
            }
        }
        return returnString;
    }
}