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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import activities.Activity_Cam;
import database.WidgetUpdate;
import misc.tracerengine;

public class Graphical_Cam extends Basic_Graphical_widget implements OnClickListener {

    private String url;
    private static String mytag;
    private tracerengine Tracer = null;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    private String name_cam;
    private final Entity_Feature feature;
    private final int session_type;
    private final Activity activity;
    private String state_key;
    private int dev_id;
    private String status;

    public Graphical_Cam(tracerengine Trac,
                         final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                         final Entity_Feature feature) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature;
        this.Tracer = Trac;
        this.activity = activity;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Cam(tracerengine Trac,
                         final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                         final Entity_Map feature_map) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature_map;
        this.Tracer = Trac;
        this.activity = activity;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        this.url = feature.getAddress();
        this.name_cam = feature.getName();
        state_key = feature.getState_key();
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }
        mytag = "Graphical_Cam(" + dev_id + ")";
        setOnClickListener(this);
        //To have the icon colored as it has no state
        change_this_icon(2);

        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
        *
        */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    //update value
                    status = session.getValue();
                    //register eventbus for new value
                    EventBus.getDefault().register(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
    }

    /**
     * @param event an Entity_client_event_value from EventBus when a new value is received from widgetupdate.
     */
    @Subscribe
    public void onEvent(Entity_client_event_value event) {
        // your implementation
        Tracer.d(mytag, "Receive event from Eventbus" + event.Entity_client_event_get_id() + " With value" + event.Entity_client_event_get_val());
        if (event.Entity_client_event_get_id() == dev_id) {
            status = event.Entity_client_event_get_val();
            //Not used in this widgets
            //Value_timestamp = event.Entity_client_event_get_timestamp();
        }
    }

    public void onClick(View v) {
        try {
            if (url.equals("Mjpeg video url") || url.equals("Virtual Video"))
                url = session.getValue();
            if (!url.equals(null)) {
                Intent intent = new Intent(activity, Activity_Cam.class);
                Bundle b = new Bundle();
                b.putString("url", url);
                //Tracer.i(mytag, "Opening camera at: " + url);
                b.putString("name", name_cam);
                intent.putExtras(b);
                int requestCode = 1;
                if (activity.toString().contains("Main")) {
                    activity.startActivityForResult(intent, requestCode);
                } else if (activity.toString().contains("Map")) {
                    activity.startActivity(intent);
                }
            }
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
    }
}
