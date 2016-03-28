/*
 * This file is part of Domodroid.
 *
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 *
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package Abstract;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;

import activities.Graphics_Manager;
import activities.Preference;
import database.DomodroidDB;
import misc.tracerengine;

public abstract class common_method {

    public static void save_params_to_file(tracerengine Tracer, SharedPreferences.Editor prefEditor, String mytag, Context context) {
        //#76
        prefEditor.commit();
        Tracer.i(mytag, "Saving pref to file");
        Preference.saveSharedPreferencesToFile(new File(Environment.getExternalStorageDirectory() + "/domodroid/.conf/settings"), context);
    }

    public static void refresh_the_views(Handler widgetHandler) {
        //Refresh the view
        Bundle b = new Bundle();
        b.putBoolean("refresh", true);
        Message msg = new Message();
        msg.setData(b);
        widgetHandler.sendMessage(msg);
    }

}
