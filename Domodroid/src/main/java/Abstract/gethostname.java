package Abstract;

import android.os.Build;

import java.lang.reflect.Method;

/**
 * Created by tiki on 11/10/2016.
 */

public abstract class gethostname {

    /**
     *
      * @return The hostname of the device
     */
    public static String getHostName() {
        try {
            Method getString = Build.class.getDeclaredMethod("getString", String.class);
            getString.setAccessible(true);
            return getString.invoke(null, "net.hostname").toString();
        } catch (Exception ex) {
            return "unknown";
        }
    }
}
