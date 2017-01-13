package Abstract;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static activities.Activity_Main.context;

/**
 * Created by tiki on 09/01/2017.
 */

public class Connectivity {
    public static boolean on_prefered_Wifi = false;
    public static boolean on_other_network = false;

    public static boolean IsInternetAvailable() {
        //return true;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    on_other_network = true;
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    //// TODO: 13/01/2017 check if ssid is same as user defined
                    on_prefered_Wifi = true;
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
