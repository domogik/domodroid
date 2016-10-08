package Abstract;

import android.content.Context;

/**
 * Created by tiki on 08/10/2016.
 */

public class Translate {

    public static int do_translate(Context context, String name) {
        //Set to lower case here to simplify other calls
        name = name.toLowerCase();
        //handle the fact that 'true/false' are reserved word and cant not been translate
        if (name == "false") {
            name = "False";
        } else if (name == "true") {
            name = "True";
        }
        //To avoid space or - in name in strings.xml
        name = name.replace(" ", "_");
        name = name.replace("-", "_");
        name = name.replace(":", "_");
        name = name.replace("/", "_");
        name = name.replace("(", "");
        name = name.replace(")", "");
        //To get a drawable R.Drawable
        //context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        //To get a string from R.String
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }
}
