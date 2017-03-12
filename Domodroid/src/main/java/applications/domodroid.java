package applications;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;

import Abstract.pref_utils;
import Event.ConnectivityChangeEvent;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Created by tiki on 07/10/2016.
 */


@ReportsCrashes(formUri = "http://yourserver.com/yourscript",
        mailTo = "new.domodroid@gmail.com",
        customReportContent = {ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT},
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash)

public class domodroid extends Application {
    public static domodroid instance;
    public static WifiInfo wifiInfo;
    private String mytag = "domodroid application";
    private pref_utils prefUtils;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
        //new ANRWatchDog().start();
        //Only to debug locally
        LeakCanary.install(this);
        Stetho.initializeWithDefaults(this);

        prefUtils = new pref_utils(this);

        //manage connectivity state
        manageConnectivityState();
        connectivityChangedReceiever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                manageConnectivityState();
            }
        };
        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityChangedReceiever, filter);

    }

    public static domodroid GetInstance() {
        return instance;
    }

    /***********************************************************
     *  Manage Connectivity
     **********************************************************/
    /***
     * BroadCastr Receiver to listen to connectivity changes
     */
    BroadcastReceiver connectivityChangedReceiever;
    /**
     * To know if there is a connection (wifi or GPRS)
     */
    private boolean isConnected = false;
    /**
     * To know if the connection is Wifi
     */
    private boolean isWifi = false;
    /**
     * To know the GRPS connectivity
     */
    private int telephonyType = 0;
    /**
     * To know if it is user preferred wifi to access domogik local
     */
    public boolean on_preferred_Wifi = false;

    /**
     * Manage the connectivity state of the device
     */
    public void manageConnectivityState() {
        Log.e(mytag, "manageConnectivityState() called with: " + "");
        // Here we are because we receive either the boot completed event
        // either the connection changed event
        // either the wifi state changed event
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (null == networkInfo) {
            // This is the airplane mode
            isConnected = false;
            isWifi = false;
            on_preferred_Wifi = false;
            telephonyType = 0;
        } else {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    isConnected = true;
                    isWifi = true;
                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        String prefered_wifi = prefUtils.GetPreferedWifiSsid();
                        if (wifiInfo.getSSID().replace("\"", "").equals(prefered_wifi)) {
                            on_preferred_Wifi = true;
                        } else {
                            on_preferred_Wifi = false;
                        }
                        //handle the case where user do not set is local SSID in options
                        if (prefered_wifi.equals("")) {
                            on_preferred_Wifi = true;
                        }
                    }
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    isConnected = true;
                    on_preferred_Wifi = false;
                    telephonyType = telephonyManager.getNetworkType();
                    break;
                default:
                    on_preferred_Wifi = false;
                    break;
            }
        }
        notifyConnectivityChanged();
        Log.e(mytag, "manageConnectivityState called and return isConnected=" + isConnected + ", isWifi="
                + isWifi + ", telephonyType=" + telephonyType + ", on_preferred_Wifi=" + on_preferred_Wifi);
    }

    /**
     * This method is called when we switch from no connectivity to connected to the internet
     */
    private void notifyConnectivityChanged() {
        Log.d(mytag, "notifyConnectivityChanged() called with: " + "");
        // notify the listeners (if there is some because this method can be called even if no
        // activity alived)
        EventBus.getDefault().post(new ConnectivityChangeEvent(telephonyType, isConnected, isWifi, on_preferred_Wifi));
    }

    /**
     * Return if the device is connected to internet
     *
     * @return the isConnected
     */
    public final boolean isConnected() {
        return isConnected;
    }
}
