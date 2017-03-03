package Abstract;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import static activities.Activity_Main.context;

/**
 * Created by tiki on 09/01/2017.
 */

public class Connectivity {
    public static boolean on_prefered_Wifi = false;
    public static boolean on_other_network = false;
    static String mytag = "Connectivity";

    /**
     * Method to check the current availability of internet on the device
     *
     * @return a boolean depending on internet connection True if connected else False
     */
    public static boolean IsInternetAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected()) {
                    on_other_network = true;
                }
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                    WifiInfo wifiInfo;
                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        String ssid = wifiInfo.getSSID().replace("\"", "");
                        String prefered_wifi = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("prefered_wifi_ssid", "");
                        if (ssid.equals(prefered_wifi)) {
                            on_prefered_Wifi = true;
                        } else {
                            on_prefered_Wifi = false;
                        }
                        //handle the case where user do not set is local SSID in options
                        if (prefered_wifi.equals("")) {
                            on_prefered_Wifi = true;
                        }
                    }
                }
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
