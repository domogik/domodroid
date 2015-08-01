package mq;

/**
 * Created by mpunie on 12/05/2015.
 */
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.zeromq.ZMQ;

public class ZMQTask extends AsyncTask<ZMQService, Void, Void> {
    private ZMQ.Context context;
    private ZMQ.Socket sub;
    private ZMQService service;
    // TODO : DEL //  public static String url;

    private ZMQService theContext = null;
    public ZMQTask(ZMQService context) {
        theContext = context;
        Log.d("foo", "contextutons!!!");
    }

    protected Void doInBackground(ZMQService... params) {
        Log.d(this.getClass().getSimpleName(), "Task started");
        // TODO : DEL //  Log.d(this.getClass().getSimpleName(), "Url of MQ is : " + this.url);
        service = params[0];
        // TODO : DEL // Log.d(this.getClass().getSimpleName(), "ZMQTask>doInBackground : try to read value to debug setUrl : " + service.url);
        // Prepare our context and subscriber
        context = ZMQ.context(1);
        sub = context.socket(ZMQ.SUB);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(theContext);
        // TODO : DEL // //SharedPreferences SP = PreferenceManager.getSharedPreferences(Main.this, 0);
        // TODO : DEL // //SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext() );
        String ip = SP.getString("rinorIP", "");    // TODO : use a R. for the default value
        String port = SP.getString("MQsubport", "40412");    // TODO : use a R. for the default value
        String sub_url = "tcp://" + ip + ":" + port;
        // TODO : DEL // Log.d("foo", "we setUrl to : " + sub_url);


        sub.connect(sub_url);
        sub.subscribe("interface.output".getBytes());

        // TODO : recharger la conf quand elle a changé
        while (!isCancelled()) {
            // Read message contents
            try {
                String msgId = new String(sub.recv(0));
                String msgContent = "";
                if (sub.hasReceiveMore()) {
                    msgContent = new String(sub.recv());
                }
                Log.d(this.getClass().getSimpleName(),msgId);
                // Do something with the message
                service.handleMessage(msgId, msgContent);
            } catch (Exception e) {
                Log.d(this.getClass().getSimpleName(), e.getMessage());
            }
            Log.d(this.getClass().getSimpleName(), "run");
        }
        Log.d(this.getClass().getSimpleName(), "Task ended");
        return null;
    }

    /* TODO: DEL
    public static void setUrl(String theUrl) {
        Log.d("foo", "ZMQTask > setUrl called with value : " + theUrl);
        url = theUrl;
    }
    */
}
