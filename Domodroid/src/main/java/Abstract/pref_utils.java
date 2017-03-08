package Abstract;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import misc.tracerengine;

/**
 * Created by tiki on 04/03/2017.
 * <p>
 * Common method to simplify preferences load  and saved
 */
public class pref_utils {
    private Context Context;
    public static SharedPreferences prefs;
    private static SharedPreferences.Editor editor;

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
     * Commit editor
     */
    public static void commit() {
        editor.commit();
    }

    /**
     * @return metrics checkbox from options
     */
    public boolean GetMetricsEnabled() {
        return prefs.getBoolean("domodroid_metrics", true);
    }

    /**
     * @return sync done
     */
    public boolean GetSyncCompleted() {
        return prefs.getBoolean("SYNC", false);
    }

    /**
     * @param sync status of the sync
     */
    public static void SetSyncCompleted(Boolean sync) {
        editor.putBoolean("SYNC", sync);
        commit();
    }

    /**
     * @return splash displayed
     */
    public boolean GetSplashDisplayed() {
        return prefs.getBoolean("SPLASH", false);
    }

    /**
     * @param splash have been displayed ?
     */
    public void SetSplashDisplayed(Boolean splash) {
        editor.putBoolean("SPLASH", splash);
        commit();
    }

    /**
     * @return Domogik version code
     */
    public static String GetDomogikVersion() {
        return prefs.getString("DOMOGIK-VERSION", "");
    }

    /**
     * @param domogik_Version save the current domogik version
     */
    public static void SetDomogikVersion(String domogik_Version) {
        editor.putString("DOMOGIK-VERSION", domogik_Version);
        commit();
    }

    /**
     * @return domogik api version
     */
    public static float GetDomogikApiVersion() {
        return prefs.getFloat("API_VERSION", 0);
    }

    /**
     * @param Rinor_Api_Version save the current domogik Api version
     */
    public static void SetDomogikApiVersion(Float Rinor_Api_Version) {
        editor.putFloat("API_VERSION", Rinor_Api_Version);
        commit();
    }

    /**
     * @return Start directly on map Option
     */
    public boolean GetStartOnMap() {
        return prefs.getBoolean("START_ON_MAP", false);
    }

    /**
     * @return the area to start in
     */
    public Integer GetAreaToStartIn() {
        return Integer.valueOf(prefs.getString("load_area_at_start", "1"));
    }

    /**
     * @return if user choice by usage
     */
    public static boolean GetWidgetByUsage() {
        return prefs.getBoolean("BY_USAGE", false);
    }

    /**
     * @param usage saved by usage or not
     */
    public static void SetWidgetByUsage(Boolean usage) {
        editor.putBoolean("BY_USAGE", usage);
        commit();
    }

    /**
     * @return if user choice alternative widget
     */
    public static boolean GetAlternativeBinaryWidget() {
        return prefs.getBoolean("WIDGET_CHOICE", false);
    }

    /**
     * @param binary_widget saved by usage or not
     */
    public static void SetAlternativeBinaryWidget(Boolean binary_widget) {
        editor.putBoolean("WIDGET_CHOICE", binary_widget);
        commit();
    }


    /**
     * @return "prefered wifi SSID" from preferences
     */
    public String GetPreferedWifiSsid() {
        return prefs.getString("prefered_wifi_ssid", "");
    }

    /**
     * @param ssid save "prefered wifi SSID" in preferences
     */
    public static void SetPreferedWifiSsid(String ssid) {
        editor.putString("prefered_wifi_ssid", ssid.substring(1, ssid.length() - 1));
        commit();
    }

    /**
     * @return timestamp or ago from options
     */
    public boolean GetWidgetTimestamp() {
        return prefs.getBoolean("widget_timestamp", false);
    }

    /**
     * @return the Rest/Rinor IP save in preferences
     */
    public static String GetRestIp() {
        return prefs.getString("rinorIP", "1.1.1.1");
    }

    /**
     * @param rinor_IP the Rest/Rinor IP to save
     */
    public static void SetRestIp(String rinor_IP) {
        editor.putString("rinorIP", rinor_IP);
        Log.e("pref_utils", "rinorIP=" + rinor_IP);
        commit();
    }

    /**
     * @return the Rest/Rinor Port save in preferences
     */
    public static String GetRestPort() {
        return prefs.getString("rinorPort", "40405");
    }

    /**
     * @param rinorPort the Rest/Rinor Port to save
     */
    public static void SetRestPort(String rinorPort) {
        editor.putString("rinorPort", rinorPort);
        commit();
    }

    /**
     * @return the Rest/Rinor Path save in preferences
     */
    public static String GetRestPath() {
        return prefs.getString("rinorPath", "/");
    }

    /**
     * @param rinorPath the Rest/Rinor Path to save
     */
    public static void SetRestPath(String rinorPath) {
        editor.putString("rinorPath", rinorPath);
        commit();
    }

    /**
     * @return the update url save in preferences
     */
    public static String GetUpdateUrl() {
        return prefs.getString("UPDATE_URL", "");
    }

    /**
     * @param urlUpdate the update url to save
     */
    public static void SetUpdateUrl(String urlUpdate) {
        editor.putString("UPDATE_URL", urlUpdate);
        commit();
    }

    /**
     * @param external_format_urlAccess the update external url to save
     */
    public static void SetExternalUrl(String external_format_urlAccess) {
        editor.putString("external_URL", external_format_urlAccess);
        Log.e("pref_utils", "SetExternalUrl=" + external_format_urlAccess);
        commit();
    }

    /**
     * @return the Url save in preferences
     */
    public static String GetUrl() {
        return prefs.getString("URL", "1.1.1.1");
    }

    /**
     * @param format_urlAccess Save the formated Url (IP:Port/path)
     */
    public static void SetUrl(String format_urlAccess) {
        editor.putString("URL", format_urlAccess);
        Log.e("pref_utils", "SetUrl=" + format_urlAccess);
        commit();
    }

    /**
     * @return the area save in preferences
     */
    public static String GetArea() {
        return prefs.getString("AREA_LIST", null);
    }

    /**
     * @param area a json representation of db area
     */
    public static void SetArea(String area) {
        editor.putString("AREA_LIST", area);
        commit();
    }

    /**
     * @return the room save in preferences
     */
    public static String GetRoom() {
        return prefs.getString("ROOM_LIST", null);
    }

    /**
     * @param room a json representation of db room
     */
    public static void SetRoom(String room) {
        editor.putString("ROOM_LIST", room);
        commit();
    }

    /**
     * @return the FeatureListAssociation save in preferences
     */
    public static String GetFeatureListAssociation() {
        return prefs.getString("FEATURE_LIST_association", null);
    }

    /**
     * @param FeatureListAssociation a json representation of db FeatureListAssociation
     */
    public static void SetFeatureListAssociation(String FeatureListAssociation) {
        editor.putString("FEATURE_LIST_association", FeatureListAssociation);
        commit();
    }

    /**
     * @return the FeatureList save in preferences
     */
    public static String GetFeatureList() {
        return prefs.getString("FEATURE_LIST", null);
    }

    /**
     * @param FeatureList a json representation of db FeatureList
     */
    public static void SetFeatureList(String FeatureList) {
        editor.putString("FEATURE_LIST", FeatureList);
        commit();
    }

    /**
     * @return the IconList save in preferences
     */
    public static String GetIconList() {
        return prefs.getString("ICON_LIST", null);
    }

    /**
     * @param IconList a json representation of db Icon
     */
    public static void SetIconList(String IconList) {
        editor.putString("ICON_LIST", IconList);
        commit();
    }

    /**
     * @return date as string where we save the last device update
     */
    public static String GetLastDeviceUpdate() {
        return prefs.getString("last_device_update", "1900-01-01 00:00:00");
    }

    /**
     * @param date where we save the last device update
     */
    public static void SetLastDeviceUpdate(String date) {
        editor.putString("last_device_update", date);
        commit();
    }

    /**
     * This method save the parameters to a file
     *
     * @param Tracer  tracerengine
     * @param mytag   a tag to know where the method was called
     * @param context a Context used to write file
     */
    public static void save_params_to_file(tracerengine Tracer, String mytag, Context context) {
        //#76
        commit();
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

    /**
     * Method to reload a file containing Preferences saved
     *
     * @param src    a file where params where saved
     * @param Tracer Tracerengine used for log
     * @return True if success
     */
    public Boolean loadSharedPreferencesFromFile(File src, tracerengine Tracer) {
        Boolean result = false;
        ObjectInputStream input = null;
        String mytag = "loadSharedPreferencesFromFile";
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            editor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                Tracer.i(mytag, "Loading pref : " + key + " -> " + v.toString());
                if (v instanceof Boolean)
                    editor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    editor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    editor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    editor.putLong(key, (Long) v);
                else if (v instanceof String)
                    editor.putString(key, (String) v);
            }
            commit();
            result = true;
        } catch (IOException e) {
            Tracer.e(mytag, "Can't load preferences file");
            Tracer.e(mytag, e.toString());
        } catch (ClassNotFoundException e) {
            Tracer.e(mytag, "Can't load preferences file");
            Tracer.e(mytag, e.toString());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Tracer.e(mytag, "Can't load preferences file");
                Tracer.e(mytag, ex.toString());
            }
        }
        return result;
    }

    /**
     * Somes basic preferences need to be initialise if not exist
     */
    public static void load_preferences() {
        //Load default value to avoid crash.
        String currlogpath = prefs.getString("LOGNAME", "");
        String mytag = "load_preferences";
        if (currlogpath.equals("")) {
            //Not yet existing prefs : Configure debugging by default, to configure Tracer
            currlogpath = Environment.getExternalStorageDirectory() + "/domodroid/.log/";
            editor.putString("LOGPATH", currlogpath);
            editor.putString("LOGNAME", "Domodroid.txt");
            editor.putBoolean("SYSTEMLOG", false);
            editor.putBoolean("TEXTLOG", false);
            editor.putBoolean("SCREENLOG", false);
            editor.putBoolean("LOGCHANGED", true);
            editor.putBoolean("LOGAPPEND", false);
            //set other default value
            editor.putBoolean("twocol_lanscape", true);
            editor.putBoolean("twocol_portrait", true);
        } else {
            editor.putBoolean("LOGCHANGED", true);        //To force Tracer to consider current settings
        }
        //editor.putBoolean("SYSTEMLOG", false);		// For tests : no system logs....
        editor.putBoolean("SYSTEMLOG", true);        // For tests : with system logs....
        commit();
    }

    /**
     * @return the order User choose to sync the default area
     */
    public static String GetDeviceSyncOrder() {
        return prefs.getString("device_sync_order", "Usage");
    }

    /**
     * @return the MQ address
     */
    public static String GetMqAddress() {
        return prefs.getString("MQaddress", "");
    }

    /**
     * @param MQaddress MQ address to save
     */
    public static void SetMqAddress(String MQaddress) {
        editor.putString("MQaddress", MQaddress);
        commit();
    }

    /**
     * @return the MQ Sub Port
     */
    public static String GetMqSubPort() {
        return prefs.getString("MQsubport", "");
    }

    /**
     * @param SubPort MQ Sub Port to save
     */
    public static void SetMqSubPort(String SubPort) {
        editor.putString("MQsubport", SubPort);
        commit();
    }

    /**
     * @return the MQ Pub Port
     */
    public static String GetMqPubPort() {
        return prefs.getString("MQpubport", "");
    }

    /**
     * @param MQpubport MQ Pub Port to save
     */
    public static void SetMqPubPort(String MQpubport) {
        editor.putString("MQpubport", MQpubport);
        commit();
    }

    /**
     * @return the MQ req_rep Port
     */
    public static String GetMqReqRepPort() {
        return prefs.getString("MQreq_repport", "");
    }

    /**
     * @param MQreq_repport MQ req_rep Port to save
     */
    public static void SetMqReqRepPort(String MQreq_repport) {
        editor.putString("MQreq_repport", MQreq_repport);
        commit();
    }

    /**
     * @return the boolean corresponding to answer choice to question about untrusted cert in admin view
     */
    public static Boolean GetSslTrusted() {
        return prefs.getBoolean("SSL_Trusted", false);
    }

    /**
     * @param b a boolean corresponding to answer choice to question about untrusted cert in admin view
     */
    public static void SetSslTrusted(boolean b) {
        editor.putBoolean("SSL_Trusted", b);
        commit();
    }

    /**
     * @return SSL acces for domogik in locale access
     */
    public static Boolean GetRestSsl() {
        return prefs.getBoolean("ssl_activate", false);
    }

    /**
     * @param ssl a boolean to save the ssl access state of domogik in locale
     */
    public static void SetRestSsl(Boolean ssl) {
        editor.putBoolean("ssl_activate", ssl);
        commit();
    }

    /**
     * @return external_ip to join domogik from external access otherwise internal ip if not configure
     */
    public static String GetExternalRestIp() {
        return prefs.getString("rinorexternal_IP", GetRestIp());
    }

    /**
     * @param external_ip to join domogik from external access
     */
    public static void SetExternalRestIp(String external_ip) {
        editor.putString("rinorexternal_IP", external_ip);
        Log.e("pref_utils", "rinorexternal_IP=" + external_ip);
        commit();
    }

    /**
     * @return external_port to join domogik from external access otherwise internal port if not configure
     */
    public static String GetExternalRestPort() {
        return prefs.getString("rinor_external_Port", GetRestPort());
    }

    /**
     * @param external_port to join domogik from external access
     */
    public static void SetExternalRestPort(String external_port) {
        editor.putString("rinor_external_Port", external_port);
        commit();
    }

    /**
     * @return SSL acces for domogik in external access otherwise internal ssl if not configure
     */
    public static Boolean GetExternalRestSsl() {
        return prefs.getBoolean("ssl_external_activate", false);
    }


    /**
     * @param external_ssl a boolean to save the ssl access state of domogik from external
     */
    public static void SetExternalRestSsl(Boolean external_ssl) {
        editor.putBoolean("ssl_external_activate", external_ssl);
        commit();
    }

    /**
     * @param butler_name Set the butler name
     */
    public static void SetButlerName(String butler_name) {
        editor.putString("dmg_butler_name", butler_name);
        commit();
    }


    /**
     * @return map menu disable status from options
     */
    public boolean GetMapMenuDisabled() {
        return prefs.getBoolean("map_menu_disable", false);
    }

    /**
     * @return the current map Scale
     */
    public Float GetMapScale() {
        return prefs.getFloat("Mapscale", 1);
    }

    /**
     * @param currentScale the current map Scale to save
     */
    public static void SetMapScale(float currentScale) {
        editor.putFloat("Mapscale", currentScale);
        commit();
    }

    /**
     * @return true if autozoom is enable for map
     */
    public boolean GetMapAutozoom() {
        return prefs.getBoolean("map_autozoom", false);
    }


    /**
     * @return true if map is set to not display text
     */
    public boolean GetMapHideText() {
        return prefs.getBoolean("HIDE", false);
    }

    /***
     *
     * @return true if Show device id is set in debug options
     */
    public static boolean GetDebugIdShow() {
        return prefs.getBoolean("DEV", false);
    }

    /**
     * @return the reresh timer period in seconds between 2 rest call
     */
    public static int GetRestUpdateTimer() {
        return prefs.getInt("UPDATE_TIMER", 300);
    }

    /**
     * @return true if user used alternative graph widget
     */
    public boolean GetAlternativeGraphWidget() {
        return prefs.getBoolean("Graph_CHOICE", false);
    }

    /**
     * TODO find what it's used really for???
     *
     * @return
     */
    public int GetMapGraphInt() {
        return prefs.getInt("GRAPH", 3);
    }

    /**
     * TODO find what it's used really for???
     *
     * @return
     */
    public int GetMapIntSize() {
        return prefs.getInt("SIZE", 600);
    }

    /**
     * @return time last sensor value was update
     */
    public static String GetLastSensorUpdate() {
        return prefs.getString("last_sensor_update", "1900-01-01 00:00:00");
    }

    /**
     * @return Username for credentials
     */
    public static String GetRestAuthUsername() {
        return prefs.getString("http_auth_username", "Anonymous");
    }

    /**
     * @param login to be set for http auth method
     */
    public static void SetHttpAuthLogin(String login) {
        editor.putString("http_auth_username", login);
        Log.e("pref_utils", "http_auth_username=" + login);

        commit();
    }

    /**
     * @return Password for credentials
     */
    public static String GetRestAuthPassword() {
        return prefs.getString("http_auth_password", "");
    }

    /**
     * @param password to be set for http auth method
     */
    public static void SetHttpAuthPassword(String password) {
        editor.putString("http_auth_password", password);
        Log.e("pref_utils", "http_auth_password=" + password);
        commit();
    }

    /**
     * #124
     *
     * @return Last timestamp when apps was closed
     */
    public String GetSensorSavedTimestamp() {
        return prefs.getString("sensor_saved_timestamp", "0");
    }

    /**
     * #124
     *
     * @return Last timestamp when apps was closed
     */
    public String GetSensorSavedValue() {
        return prefs.getString("sensor_saved_value", "0");
    }

    /**
     * #124
     *
     * @param cached_dump      dump sensor value to save them
     * @param currentTimestamp timestamp we saved the dump
     */
    public static void SetSensorSavedValueAndTimestamp(String cached_dump, String currentTimestamp) {
        editor.putString("sensor_saved_value", cached_dump);
        editor.putString("sensor_saved_timestamp", currentTimestamp);
        commit();
    }

    /**
     * @return True if 2 columns are allow in Landscape mode
     */
    public static boolean GetTwoColumnsLandscape() {
        return prefs.getBoolean("twocol_lanscape", false);
    }

    /**
     * @return True if 2 columns are allow in Portrait mode
     */
    public static boolean GetTwoColumnsPortait() {
        return prefs.getBoolean("twocol_portrait", false);
    }

    /**
     * @return last known color, White by default
     */
    public String GetLastColorRgb() {
        return prefs.getString("COLORRGB", "#FFFFFF");
    }

    public static void SetColorRgb(String value) {
        editor.putString("COLORRGB", "#" + value);
        commit();
    }

    public Integer GetLastColorHue() {
        return prefs.getInt("COLORHUE", 0);
    }

    public static void SetColorHue(int progress) {
        editor.putInt("COLORHUE", progress);
        commit();
    }

    public Integer GetLastColorSaturation() {
        return prefs.getInt("COLORSATURATION", 255);
    }

    public static void SetColorSaturation(int progress) {
        editor.putInt("COLORSATURATION", progress);
        commit();
    }

    public Integer GetLastColorBrightness() {
        return prefs.getInt("COLORBRIGHTNESS", 255);
    }

    public static void SetColorBrightness(int progress) {
        editor.putInt("COLORBRIGHTNESS", progress);
        commit();
    }

    public static void SetDebugLocCanged(boolean b) {
        editor.putBoolean("LOGCHANGED", b);
        commit();
    }

    public static void SetDebugTextlog(Boolean to_txtFile) {
        editor.putBoolean("TEXTLOG", to_txtFile);
        commit();
    }

    /**
     * @return length of history value to retrieve in widgets
     */
    public String GetWidgetHistoryLength() {
        return prefs.getString("history_length", "5");
    }

    /**
     * @return the height of Graphics widget
     */
    public int GetWidgetGraphSize() {
        return prefs.getInt("graphics_height_size", 262);
    }

}