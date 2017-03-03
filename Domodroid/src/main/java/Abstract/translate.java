package Abstract;

import android.content.Context;

import misc.tracerengine;

/**
 * Created by tiki on 08/10/2016.
 */

public abstract class translate {

    /**
     *
     * @param context Context to get resources
     * @param Tracer Tracerngine used to log
     * @param name Value to translate
     * @return An integer from R.STRING containing the value translate in Locale device language
     */
    public static int do_translate(Context context, tracerengine Tracer, String name) {
        String mytag = "do_translate";
        //Set to lower case here to simplify other calls
        String temp_name = name.toLowerCase();
        //handle the fact that 'true/false' are reserved word and cant not been translate
        if (temp_name.endsWith("false")) {
            temp_name = "False";
        } else if (temp_name.equals("true")) {
            temp_name = "True";
        }else if (temp_name.equals("switch")) {
            temp_name = "Switch";
        }
        //To avoid space or - in name in strings.xml
        temp_name = temp_name.replace(" ", "_");
        temp_name = temp_name.replace("-", "_");
        temp_name = temp_name.replace(":", "_");
        temp_name = temp_name.replace("/", "_");
        temp_name = temp_name.replace("(", "");
        temp_name = temp_name.replace(")", "");
        //handle start with a number as it is not allow by android
        if (Character.isDigit(temp_name.charAt(0))) {
            temp_name = "_" + temp_name;
        }
        // TODO for #144 handle some plugins function like concatenation with a comma or point of multiple value
        //To get a drawable R.Drawable
        //context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        //To get a string from R.String
        int newstring = context.getResources().getIdentifier(temp_name, "string", context.getPackageName());
        //Catch untranslated value
        if (newstring == 0 && !name.equals(""))
            Tracer.e(mytag, "no translation for: " + name + " parse as: " + temp_name);
        return newstring;
    }
}
