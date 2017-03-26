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
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.send_command;

public class Graphical_Binary_New extends Basic_Graphical_widget implements OnClickListener {

    private Button ON;
    private Button OFF;
    private TextView state;
    private String state_progress;
    private String value0;
    private String value1;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    private static String mytag = "";
    private String stateS = "";
    private String Value_0 = "0";
    private String Value_1 = "1";
    private final Entity_Feature feature;
    private JSONObject jparam;
    private String command_id = null;
    private String command_type = null;
    private Entity_client session = null;
    private final int session_type;
    private String Value_timestamp;
    private String status;
    private int dev_id;
    private String state_key;


    public Graphical_Binary_New(tracerengine Trac,
                                final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                final Entity_Feature feature, Handler handler) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Binary_New(tracerengine Trac,
                                final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                final Entity_Map feature_map, Handler handler) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        String address = feature.getAddress();
        String usage = feature.getIcon_name();
        this.state_key = feature.getState_key();
        String parameters = feature.getParameters();
        mytag = "Graphical_Binary_New(" + dev_id + ")";
        try {
            this.stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            this.stateS = state_key;
        }
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }


        //get parameters

        try {
            jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            value1 = jparam.getString("value1");
            value0 = jparam.getString("value0");
        } catch (Exception e) {
            value0 = "0";
            value1 = "1";
        }
        if (api_version >= 0.7f) {
            try {
                int number_of_command_parameters = jparam.getInt("number_of_command_parameters");
                if (number_of_command_parameters == 1) {
                    command_id = jparam.getString("command_id");
                    command_type = jparam.getString("command_type1");
                    Tracer.v(mytag, "Json command_id :" + command_id + " & command_type :" + command_type);
                }
            } catch (JSONException e) {
                Tracer.d(mytag, "No command_id for this device");
                ON.setEnabled(false);
                OFF.setEnabled(false);
            }
        }

        switch (usage) {
            case "light":
                this.Value_0 = getResources().getText(R.string.light_stat_0).toString();
                this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
                break;
            case "shutter":
                this.Value_0 = getResources().getText(R.string.shutter_stat_0).toString();
                this.Value_1 = getResources().getText(R.string.shutter_stat_1).toString();
                break;
            default:
                this.Value_0 = value0;
                this.Value_1 = value1;
                break;
        }

        String[] model = feature.getDevice_type_id().split("\\.");
        String type = model[0];
        Tracer.d(mytag, "model_id = <" + feature.getDevice_type_id() + "> type = <" + type + "> value0 = " + value0 + "  value1 = " + value1);

        //state
        state = new TextView(activity);
        state.setTextColor(Color.BLACK);
        state.setText(stateS);
        //		if(api_version>=0.7f)
        //		state.setVisibility(INVISIBLE);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        float dps = 40;
        int pixels = (int) (dps * scale + 0.5f);
        //first seekbar on/off
        ON = new Button(activity);
        ON.setOnClickListener(this);
        ON.setHeight(pixels);
        //ON.setWidth(60);
        ON.setTag("ON");
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.weight = 0.5f;
        try {
            ON.setText(activity.getString(translate.do_translate(getContext(), Tracer, this.Value_1)));
        } catch (Exception e1) {
            ON.setText(this.Value_1);
        }
        ON.setLayoutParams(params);
        ON.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        //ON.setBackgroundResource(R.drawable.boolean_on);
        //ON.setPadding(10, 0, 10, 0);

        OFF = new Button(activity);
        OFF.setOnClickListener(this);
        OFF.setTag("OFF");
        OFF.setHeight(pixels);
        //OFF.setWidth(60);
        //OFF.setBackgroundResource(R.drawable.boolean_off);
        try {
            OFF.setText(activity.getString(translate.do_translate(getContext(), Tracer, this.Value_0)));
        } catch (Exception e1) {
            OFF.setText(this.Value_0);
        }
        OFF.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        OFF.setLayoutParams(params);
        //OFF.setPadding(0,10,0,10);

        super.LL_featurePan.addView(ON);
        super.LL_featurePan.addView(OFF);
        super.LL_infoPan.addView(state);

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
                    status = session.getValue();
                    Value_timestamp = session.getTimestamp();
                    update_display();
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
            Value_timestamp = event.Entity_client_event_get_timestamp();
            update_display();
        }
    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {
        Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);
        if (status.equals(value0)) {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_0)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_0);
            }
            change_this_icon(0);
        } else if (status.equals(value1)) {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_1)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_1);
            }
            change_this_icon(2);
        } else {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, status)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + status);
            }
        }
    }

    public void onClick(View v) {
        if (v.getTag().equals("OFF")) {
            change_this_icon(0);
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_0)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_0);
            }
            if (api_version >= 0.7f) {
                state_progress = "0";
            } else {
                state_progress = value0;
            }
        } else if (v.getTag().equals("ON")) {
            change_this_icon(2);
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_1)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_1);
            }
            if (api_version >= 0.7f) {
                state_progress = "1";
            } else {
                state_progress = value1;
            }
        }

        send_command.send_it(activity, Tracer, command_id, command_type, state_progress, api_version);
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            //activate=true;
        }
    }

}





