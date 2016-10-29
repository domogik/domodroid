package applications;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

import com.github.anrwatchdog.ANRWatchDog;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.domogik.domodroid13.R;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Created by tiki on 07/10/2016.
 */


@ReportsCrashes(formUri = "http://yourserver.com/yourscript",
        mailTo = "new.domodroid@gmail.com",
        customReportContent = {ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT},
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash)

public class domodroid extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        new ANRWatchDog().start();

        //get metrics every 30s
        int repeatTime = 30;  //Repeat alarm time in seconds
        AlarmManager processTimer = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, metrics.MetricsServiceReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
        processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),repeatTime*1000, pendingIntent);

    }
}
