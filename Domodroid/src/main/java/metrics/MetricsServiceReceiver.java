package metrics;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.domogik.domodroid13.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import Abstract.dmd_gettime;

import static activities.Activity_Main.context;

/**
 * Created by tiki on 29/10/2016.
 */

public class MetricsServiceReceiver extends BroadcastReceiver {
    private final String mytag = this.getClass().getName();
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    Float freeSize = 0f;
    Float totalAllocatedSize = 0f;
    Float usedSize = -1f;
    Float maxmemavailable = 0f;
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    public void onReceive(Context context, Intent intent) {
        new getmetrics().execute();
    }

    private class getmetrics extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            //process with update timer for new 30 sec
            Log.e(mytag, "New timer " + executeTop());
            SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
            Float api_version = SP_params.getFloat("API_VERSION", 0);
            Log.e(mytag, "New timer api_version:" + api_version.toString());
            String domogik_version = SP_params.getString("DOMOGIK-VERSION", "");
            Log.e(mytag, "New timer domogik_version:" + domogik_version);
            String versionName = BuildConfig.VERSION_NAME;
            Log.e(mytag, "New timer versionName:" + versionName);
            int versionCode = BuildConfig.VERSION_CODE;
            String vcs = "??";
            if (versionCode != -1)
                vcs = Integer.toString(versionCode);
            Log.e(mytag, "New timer VERSION_CODE:" + vcs);
            int Android_code = Build.VERSION.SDK_INT;
            Log.e(mytag, "New timer SDK_INT:" + String.valueOf(Android_code));
            String Device = Build.DEVICE;
            Log.e(mytag, "New timer Device:" + Device);
            String devicid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.e(mytag, "New timer devicid:" + devicid);
            try {
                Runtime info = Runtime.getRuntime();
                int availableProcessors = info.availableProcessors();
                Log.e(mytag, "New timer availableProcessors: " + availableProcessors);
                maxmemavailable = Float.valueOf(info.maxMemory());
                maxmemavailable = maxmemavailable / 1024; //in KB
                maxmemavailable = maxmemavailable / 1024; //in MB
                Log.e(mytag, "New timer maxmemavailable: " + df.format(maxmemavailable) + "MB");
                freeSize = Float.valueOf(info.freeMemory());
                totalAllocatedSize = Float.valueOf(info.totalMemory());
                usedSize = totalAllocatedSize - freeSize;
                freeSize = freeSize / 1024; //in KB
                freeSize = freeSize / 1024; //in MB
                totalAllocatedSize = totalAllocatedSize / 1024; //in KB
                totalAllocatedSize = totalAllocatedSize / 1024; //in MB
                usedSize = usedSize / 1024; //in KB
                usedSize = usedSize / 1024; //in MB
                Log.e(mytag, "New timer totalAllocatedSize: " + df.format(totalAllocatedSize) + "MB");
                Log.e(mytag, "New timer freeSize: " + df.format(freeSize) + "MB");
                Log.e(mytag, "New timer usedSize: " + df.format(usedSize) + "MB");
            } catch (Exception e) {
                Log.e(mytag, "error getting used memory :" + e.toString());
            }
            String gmtTime = dmd_gettime.GetUTCdatetimeAsString();
            Log.e(mytag, "time stamp=" + gmtTime);
            return null;
        }
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