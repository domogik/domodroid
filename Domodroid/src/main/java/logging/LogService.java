package logging;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by tiki on 08/01/2017.
 */
public class LogService extends Service {
    public static final String BROADCAST_FILE_LOG_UPDATE = "org.domogik.domodroid13.log.update";
    private String fileName = Environment.getExternalStorageDirectory() + "/domodroid/.log/Domodroid.txt";

    /**
     * The intent thanks to which is forwarded the alarm to display.
     */
    private Intent logIntent;

    private FileObserver fileObserver;

    @Override
    public void onCreate() {
        Log.e("LogService", "oncreate");
        logIntent = new Intent(BROADCAST_FILE_LOG_UPDATE);
        fileObserver = new FileObserver(fileName) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.MODIFY) {
                    broadcastLogUpdate();
                }
            }
        };
    }

    private void broadcastLogUpdate() {
        sendBroadcast(logIntent);
    }

    @Override
    public void onStart(Intent intent, int startid) {
        fileObserver.startWatching();
    }

    @Override
    public void onDestroy() {
        fileObserver.stopWatching();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
