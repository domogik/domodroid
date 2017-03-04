package Abstract;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import misc.tracerengine;

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
    public String PreferedWifiSsid() {
        return prefs.getString("prefered_wifi_ssid", "");
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

    /**
     * This method save the parameters to a file
     *
     * @param Tracer     tracerengine
     * @param prefEditor A SharedPreferences.Editor
     * @param mytag      a tag to know where the method was called
     * @param context    a Context used to write file
     */
    public static void save_params_to_file(tracerengine Tracer, SharedPreferences.Editor prefEditor, String mytag, Context context) {
        //#76
        prefEditor.commit();
        Tracer.i(mytag, "Saving pref to file");
        saveSharedPreferencesToFile(new File(Environment.getExternalStorageDirectory() + "/domodroid/.conf/settings"), context, Tracer, mytag);
    }

    /**
     * This method really save the file to a destination
     *
     * @param dst     destination of the file
     * @param context Context used to write file
     * @param Tracer  tracerengine Used to log essentially
     * @param mytag   a tag to know wher method was called from
     */
    private static void saveSharedPreferencesToFile(File dst, Context context, tracerengine Tracer, String mytag) {
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            output.writeObject(PreferenceManager.getDefaultSharedPreferences(context).getAll());
        } catch (FileNotFoundException e) {
            Tracer.e(mytag, "Files error: " + e.toString());
        } catch (IOException e) {
            Tracer.e(mytag, "IO error: " + e.toString());
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                Tracer.e(mytag, "IO error: " + ex.toString());
            }
        }
    }
}