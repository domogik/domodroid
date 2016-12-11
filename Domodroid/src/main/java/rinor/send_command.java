package rinor;


import misc.tracerengine;

/**
 * Created by tiki on 24/11/2016.
 */

public class send_command {

    public static String send_it(tracerengine Tracer, String url, String command_id, String command_type, String state_progress, String login, String password, Boolean SSL, float api_version) {
        String mytag = "send_it";
        String Url2send;
        if (api_version >= 0.7f) {
            Url2send = url + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
        } else {
            Url2send = url + "command/" + command_type + "/" + command_id + "/" + state_progress;
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

}
