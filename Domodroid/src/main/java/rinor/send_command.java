package rinor;


import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import misc.tracerengine;

/**
 * Created by tiki on 24/11/2016.
 */

public class send_command {

    public static String send_it(Activity activity, tracerengine Tracer, String command_id, String command_type, String state_progress, float api_version) {
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        final String login = SP_params.getString("http_auth_username", "Anonymous");
        final String password = SP_params.getString("http_auth_password", "");
        final Boolean SSL = SP_params.getBoolean("ssl_activate", false);
        final String URL = SP_params.getString("URL", "1.1.1.1");

        String mytag = "send_it";
        String Url2send;
        if (api_version >= 0.7f) {
            Url2send = URL + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
        } else {
            Url2send = URL + "command/" + command_type + "/" + command_id + "/" + state_progress;
        }
        try {
            new CallUrl().execute(Url2send, login, password, "3000", String.valueOf(SSL));
            Tracer.d(mytag, "Sending the command...");
            return "OK";
        } catch (Exception e) {
            Tracer.e(mytag, "ERROR while sending the command");
            Tracer.e(mytag, e.toString());
            return "ERROR";
        }
    }

    public static String send_it_without_address(Activity activity, tracerengine Tracer, String request, float api_version) {
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        final String login = SP_params.getString("http_auth_username", "Anonymous");
        final String password = SP_params.getString("http_auth_password", "");
        final Boolean SSL = SP_params.getBoolean("ssl_activate", false);
        final String URL = SP_params.getString("URL", "1.1.1.1");

        String Url2send = URL +request;
        String mytag = "send_it";

        try {
            new CallUrl().execute(Url2send, login, password, "3000", String.valueOf(SSL));
            Tracer.d(mytag, "Sending the command...");
            return "OK";
        } catch (Exception e) {
            Tracer.e(mytag, "ERROR while sending the command");
            Tracer.e(mytag, e.toString());
            return "ERROR";
        }
    }
}
