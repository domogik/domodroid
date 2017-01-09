package Abstract;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static activities.Activity_Main.context;

/**
 * Created by tiki on 09/01/2017.
 */

public class Connectivity {
    public static boolean IsInternetAvailable() {
        //return true;
        ConnectivityManager connectivityManager
                = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
