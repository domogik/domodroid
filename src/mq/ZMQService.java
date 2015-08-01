package mq;

/**
 * Created by mpunie on 12/05/2015.
 */
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ZMQService extends Service {
    public static String TAG = "ZMQService";
    // TODO : DEL // public static String url = "default";   // TODO : empty

    private ZMQTask task;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.getClass().getSimpleName(), "Service oncreate ...");
    }

    @Override
    public void onDestroy() {
        task.cancel(true);
        super.onDestroy();
        Toast.makeText(this, "Service destroyed...", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(this.getClass().getSimpleName(), "Service onStart ...");
        super.onStartCommand(intent, flags, startId);
        doWork();
        return START_STICKY;
    }

    private void doWork(){
        Log.d(this.getClass().getSimpleName(), "Service Dowork Started ...");
        try{
            //task = new ZMQTask();
            task = new ZMQTask(this);
            // TODO : DEL // Log.d("foo", "ZMQService : calling ZMQTask setUrl with ZMQService.url : " + ZMQService.url);
            // TODO : DEL // Log.d("foo", "ZMQService : calling ZMQTask setUrl with url : " + url);
            // TODO : DEL // task.setUrl(ZMQService.url);
            task.execute(this);
        } catch(Exception e){
            Log.e(this.getClass().getSimpleName(), e.toString());
        }
        Log.d(this.getClass().getSimpleName(), "Service Dowork finished ...");
    }

    public void handleMessage(String msgId, String json){
        JSONObject jsonMessage = null;
        try {
            jsonMessage = new JSONObject(json);
        } catch (JSONException e) {
            Log.d(TAG, "Unable to parse message JSON", e);
        }
        if(jsonMessage == null){
            Log.e(TAG, "msg was not properly parsed");
            return; // return early to bail out of processing
        }
        ZMQMessage msg = new ZMQMessage();
        msg.setId(msgId);
        msg.setMessage(json);

        Intent i = new Intent("domogik.domodroid.MESSAGE_RECV");
        i.putExtra("message", msg);
        sendBroadcast(i);
    }

    /* TODO : DEL
    public static void setUrl(String theUrl) {

        Log.d("foo", "ZMQService > setUrl called for url : " + theUrl);
        url = theUrl;
        Log.d("foo", "ZMQService > ZMQService.url = " + ZMQService.url);
        Log.d("foo", "ZMQService > url = " + url);
    }
    */
}
