package metrics;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.domogik.domodroid13.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static activities.Activity_Main.context;

/**
 * Created by tiki on 29/10/2016.
 */

public class MetricsServiceReceiver extends BroadcastReceiver {
    private final String mytag = this.getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
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