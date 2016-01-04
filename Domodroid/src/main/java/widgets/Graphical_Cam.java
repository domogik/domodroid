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
package widgets;

import activities.Activity_Cam;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import misc.tracerengine;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class Graphical_Cam extends Basic_Graphical_widget implements OnClickListener {

    private final String url;
    private final Context context;
    private static String mytag;
    private tracerengine Tracer = null;
    public static FrameLayout container = null;
    private static String state_key;
    private final String name_cam;

    public Graphical_Cam(tracerengine Trac, Activity context, int id, int dev_id, String name, String state_key, String url, String usage, int widgetSize, int session_type, int place_id, String place_type) {
        super(context, Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type, mytag, container);
        this.context = context;
        this.Tracer = Trac;
        this.name_cam = name;
        this.url = url;
        FrameLayout myself = this;
        setOnClickListener(this);
        mytag = "Graphical_Cam(" + dev_id + ")";

        //To have the icon colored as it has no state
        change_this_icon(2);

    }

    public void onClick(View v) {
        if (!url.equals(null)) {
            Intent intent = new Intent(context, Activity_Cam.class);
            Bundle b = new Bundle();
            b.putString("url", url);
            Tracer.e(mytag, "" + url);
            b.putString("name", name_cam);
            intent.putExtras(b);
            context.startActivity(intent);
        }
    }
}
