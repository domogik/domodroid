package Abstract;

import android.content.Context;

import misc.tracerengine;

/**
 * Created by tiki on 08/10/2016.
 */

public class translate {

    public static int do_translate(Context context, tracerengine Tracer, String name) {
        String mytag = "do_translate";
        //Set to lower case here to simplify other calls
        String temp_name = name.toLowerCase();
        //handle the fact that 'true/false' are reserved word and cant not been translate
        if (temp_name.endsWith("false")) {
            temp_name = "False";
        } else if (temp_name.equals("true")) {
            temp_name = "True";
        }
        //To avoid space or - in name in strings.xml
        temp_name = temp_name.replace(" ", "_");
        temp_name = temp_name.replace("-", "_");
        temp_name = temp_name.replace(":", "_");
        temp_name = temp_name.replace("/", "_");
        temp_name = temp_name.replace("(", "");
        temp_name = temp_name.replace(")", "");
        //To get a drawable R.Drawable
        //context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        //To get a string from R.String
        try {
            return context.getResources().getIdentifier(temp_name, "string", context.getPackageName());
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + name);
            return 0;
        }
    }
}
