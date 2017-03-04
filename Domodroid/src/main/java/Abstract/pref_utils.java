package Abstract;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tiki on 04/03/2017.
 * <p>
 * Common method to simplify preferences load  and saved
 */
public class pref_utils {
    private Context Context;
    public SharedPreferences prefs;
    public SharedPreferences.Editor editor;

    /**
     * Constructor
     *
     * @param Context
     */
    public pref_utils(Context Context) {
        this.Context = Context;
        prefs = PreferenceManager.getDefaultSharedPreferences(Context);
        editor = prefs.edit();
    }

    /**
     * @return metrics checkbox from options
     */
    public boolean metricsEnabled() {
        return prefs.getBoolean("domodroid_metrics", true);
    }

    /**
     * @return sync done
     */
    public boolean SyncCompleted() {
        return prefs.getBoolean("SYNC", false);
    }

    /**
     * @return splash displayed
     */
    public boolean SplashDisplayed() {
        return prefs.getBoolean("SPLASH", false);
    }

    /**
     * @return Domogik version code
     */
    public String DomogikVersion() {
        return prefs.getString("DOMOGIK-VERSION", "");
    }

    /**
     * @return domogik api version
     */
    public float DomogikApiVersion() {
        return prefs.getFloat("API_VERSION", 0);
    }

    /**
     * @return Start directly on map Option
     */
    public boolean StartOnMap() {
        return prefs.getBoolean("START_ON_MAP", false);
    }

    /**
     * @return the area to start in
     */
    public Integer AreaToStartIn() {
        return Integer.valueOf(prefs.getString("load_area_at_start", "1"));
    }

    /**
     * @return if user choice by usage
     */
    public boolean WidgetByUsage() {
        return prefs.getBoolean("BY_USAGE", false);
    }

    /**
     * @param ssid save "prefered wifi SSID" in preferences
     */
    public void savePreferedWifiSsid(String ssid) {
        editor.putString("prefered_wifi_ssid", ssid.substring(1, ssid.length() - 1));
        editor.commit();
    }

    /**
     * @param cached_dump      dump sensor value to save them
     * @param currentTimestamp timestamp we saved the dump
     */
    public void SaveSensor_saved_value(String cached_dump, String currentTimestamp) {
        editor.putString("sensor_saved_value", cached_dump);
        editor.putString("sensor_saved_timestamp", currentTimestamp);
        editor.commit();
    }

    /**
     * @param splash have been displayed ?
     */
    public void SaveSplashDisplayed(Boolean splash) {
        editor.putBoolean("SPLASH", splash);
        editor.commit();
    }

    /**
     * @param sync status of the sync
     */
    public void SaveSyncCompleted(Boolean sync) {
        editor.putBoolean("SYNC", sync);
        editor.commit();
    }

}