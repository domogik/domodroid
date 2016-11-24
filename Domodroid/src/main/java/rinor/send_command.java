package rinor;

import android.os.AsyncTask;

/**
 * Created by tiki on 24/11/2016.
 */

public class send_command {

    public abstract class CommandeThread extends AsyncTask<Void, Integer, Void> {

        public String doInBackground(String... params) {
            final String url = params[0];
            final String command_id = params[1];
            final String command_type = params[2];
            final String state_progress = params[3];
            final String login = params[4];
            final String password = params[5];
            final String SSL = params[6];
            final float api_version = Float.parseFloat(params[7]);

            String Url2send;
            if (api_version >= 0.7f) {
                Url2send = url + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
            } else {
                Url2send = url + "command/" + command_type + "/" + command_id + "/" + state_progress;
            }
            try {
                new CallUrl().execute(Url2send, login, password, "3000", SSL);
                return "OK";
            } catch (Exception e) {
                return "ERROR";
            }
        }
    }
}
