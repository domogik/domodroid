package Abstract;

import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

import misc.tracerengine;

/**
 * Created by tiki on 01/01/2017.
 */

public class load_parameters {
    static String mytag="load_parameters";

    /**
     * Method to reload a file containing Preferences saved
     *
     * @param src a file where params where saved
     * @param SP_prefEditor SharedPreferences.Edtor
     * @param Tracer Tracerengine used for log
     * @return True if success
     */
    public static Boolean loadSharedPreferencesFromFile(File src, SharedPreferences.Editor SP_prefEditor, tracerengine Tracer) {
        Boolean result= false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SP_prefEditor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                Tracer.i(mytag, "Loading pref : " + key + " -> " + v.toString());
                if (v instanceof Boolean)
                    SP_prefEditor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    SP_prefEditor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    SP_prefEditor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    SP_prefEditor.putLong(key, (Long) v);
                else if (v instanceof String)
                    SP_prefEditor.putString(key, (String) v);
            }
            SP_prefEditor.commit();
            result=true;
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
     *
     * @param SP_params SharedPreferences
     * @param SP_prefEditor SharedPreferences.Editor
     */
    public static void load_preferences(SharedPreferences SP_params, SharedPreferences.Editor SP_prefEditor) {
        //Load default value to avoid crash.
        String currlogpath = SP_params.getString("LOGNAME", "");
        if (currlogpath.equals("")) {
            //Not yet existing prefs : Configure debugging by default, to configure Tracer
            currlogpath = Environment.getExternalStorageDirectory() + "/domodroid/.log/";
            SP_prefEditor.putString("LOGPATH", currlogpath);
            SP_prefEditor.putString("LOGNAME", "Domodroid.txt");
            SP_prefEditor.putBoolean("SYSTEMLOG", false);
            SP_prefEditor.putBoolean("TEXTLOG", false);
            SP_prefEditor.putBoolean("SCREENLOG", false);
            SP_prefEditor.putBoolean("LOGCHANGED", true);
            SP_prefEditor.putBoolean("LOGAPPEND", false);
            //set other default value
            SP_prefEditor.putBoolean("twocol_lanscape", true);
            SP_prefEditor.putBoolean("twocol_portrait", true);
        } else {
            SP_prefEditor.putBoolean("LOGCHANGED", true);        //To force Tracer to consider current settings
        }
        //prefEditor.putBoolean("SYSTEMLOG", false);		// For tests : no system logs....
        SP_prefEditor.putBoolean("SYSTEMLOG", true);        // For tests : with system logs....
        SP_prefEditor.commit();
    }
}
