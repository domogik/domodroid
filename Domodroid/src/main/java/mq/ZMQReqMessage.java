package mq;

/**
 * Created by tiki on 11/10/2016.
 */

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import org.zeromq.ZMQ;

public class ZMQReqMessage extends AsyncTask<String, Void, String> {
    private final Handler uiThreadHandler;

    private String MQaddress;
    private String MQreq_repport;
    private final String mytag = this.getClass().getName();

    public ZMQReqMessage(Handler uiThreadHandler) {
        this.uiThreadHandler = uiThreadHandler;
    }

    @Override
    protected String doInBackground(String... params) {
        Log.e("ZMQReqMessage", "MQ REP address=" + params[0] + " message is " + params[1]);
        String url = params[0];
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REQ);
        socket.connect(url);

        socket.send(params[1].getBytes(), 0);
        String result = new String(socket.recv(0));
        Log.e("ZMQReqMessage", "MQ REP message:" + result);
        socket.close();
        context.term();

        return result;
    }

}