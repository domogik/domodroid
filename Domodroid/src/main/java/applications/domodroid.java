package applications;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.domogik.domodroid13.R;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Created by tiki on 07/10/2016.
 */


@ReportsCrashes(formUri = "http://yourserver.com/yourscript",
        mailTo = "new.domodroid@gmail.com",
        customReportContent = { ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash)

public class domodroid extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
