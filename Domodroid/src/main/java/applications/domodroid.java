package applications;

import android.app.Application;

import com.github.anrwatchdog.ANRWatchDog;
import com.squareup.leakcanary.LeakCanary;

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
    private static domodroid instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        new ANRWatchDog().start();
        LeakCanary.install(this);
    }

    public static domodroid GetInstance() {
        return instance;
    }
}
