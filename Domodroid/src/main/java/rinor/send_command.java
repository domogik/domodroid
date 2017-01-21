package rinor;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import misc.tracerengine;

/**
 * Created by tiki on 24/11/2016.
 */

public class send_command {

    public static void send_it(final Activity activity, tracerengine Tracer, String command_id, String command_type, String state_progress, float api_version) {
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        final String login = SP_params.getString("http_auth_username", "Anonymous");
        final String password = SP_params.getString("http_auth_password", "");
        Boolean SSL = false;

        String mytag = "send_it";
        String Url2send;

        String URL = null;
        if (Abstract.Connectivity.on_prefered_Wifi) {
            //If connected to default SSID use local adress
            URL = SP_params.getString("URL", "1.1.1.1");
            SSL = SP_params.getBoolean("ssl_activate", false);
        } else {
            //If connected to default SSID use external adress
            URL = SP_params.getString("external_URL", "1.1.1.1");
            SSL = SP_params.getBoolean("ssl_external_activate", false);
        }
        if (api_version >= 0.7f) {
            if (command_type == null) {
                //when the command contains mutiple value
                Url2send = URL + "cmd/id/" + command_id + "?" + state_progress;
            } else {
                Url2send = URL + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
            }
        } else {
            Url2send = URL + "command/" + command_type + "/" + command_id + "/" + state_progress;
        }
        try {
            new CallUrl().execute(Url2send, login, password, "30000", String.valueOf(SSL));
            Tracer.d(mytag, "Sending the command...");
        } catch (Exception e) {
            Tracer.e(mytag, "ERROR while sending the command");
            Tracer.e(mytag, e.toString());
        }
    }
}