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
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.tracerengine;

public class Graphical_Info extends Basic_Graphical_widget implements OnClickListener {


    private LinearLayout featurePan2;
    private View featurePan2_buttons;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private Graphical_Info_View canvas;
    private static String mytag;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    public Boolean with_graph = true;

    private final Entity_Feature feature;
    private String state_key;

    private String parameters;
    private final int session_type;

    private final int update;
    private TextView state_key_view;
    private String stateS;
    private String test_unite;
    private float Float_graph_size;

    private boolean isopen = false;
    private int dev_id;
    private String Value_timestamp;
    private String status;

    public Graphical_Info(tracerengine Trac,
                          final Activity activity, int widgetSize, int session_type, int place_id, String place_type, final int update,
                          final Entity_Feature feature) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature;
        this.session_type = session_type;
        this.update = update;
        onCreate();
    }

    public Graphical_Info(tracerengine Trac,
                          final Activity activity, int widgetSize, int session_type, int place_id, String place_type, final int update,
                          final Entity_Map feature_map) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature_map;
        this.session_type = session_type;
        this.update = update;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        this.parameters = feature.getParameters();
        this.state_key = feature.getState_key();
        this.isopen = false;
        int graphics_height_size = prefUtils.GetWidgetGraphSize();
        this.Float_graph_size = Float.valueOf(graphics_height_size);
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
        mytag = "Graphical_Info (" + this.dev_id + ")";

        setOnClickListener(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Label Text size according to the screen size
        float size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        float size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);

        Tracer.i(mytag, "New instance for name = " + name + " state_key = " + state_key);

        //state key
        state_key_view = new TextView(activity);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //TV_Value
        TV_Value = new TextView(activity);
        TV_Value.setTextSize(28);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TV_Value.setTextIsSelectable(true);
            TV_Value.setOnClickListener(this);
        }
        TV_Value.setTextColor(Color.BLACK);
        TV_Value.setGravity(Gravity.RIGHT);

        //TV_Timestamp
        TV_Timestamp = new RelativeTimeTextView(activity, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);


        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        if (with_graph) {

            //feature panel 2 which will contain graphic
            featurePan2 = new LinearLayout(activity);
            featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            featurePan2.setGravity(Gravity.CENTER_VERTICAL);
            featurePan2.setPadding(5, 10, 5, 10);
            //canvas
            canvas = new Graphical_Info_View(activity, Tracer, activity, parameters);
            canvas.dev_id = dev_id;
            canvas.id = feature.getId();
            canvas.state_key = state_key;
            canvas.update = update;

            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            featurePan2_buttons = layoutInflater.inflate(R.layout.graph_buttons, null);
            View v;

            v = featurePan2_buttons.findViewById(R.id.bt_prev);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_next);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_year);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_month);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_week);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.bt_day);
            if (v != null)
                v.setOnClickListener(canvas);

            v = featurePan2_buttons.findViewById(R.id.period);
            if (v != null)
                canvas.dates = (TextView) v;

            //background_stats.addView(canvas);
            featurePan2.addView(canvas);
        }

        LL_featurePan.addView(TV_Value);
        LL_featurePan.addView(TV_Timestamp);

        test_unite = "";
        try {
            //Basilic add, number feature has a unit parameter
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            test_unite = jparam.getString("unit");
        } catch (JSONException jsonerror) {
            Tracer.i(mytag, "No unit for this feature");
        }


        LL_infoPan.addView(state_key_view);
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
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

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);

                Long Value_timestamplong;
                Value_timestamplong = Long.valueOf(Value_timestamp) * 1000;

                display_sensor_info.display(Tracer, status, Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, activity, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);

                //Change icon if in %
                if ((state_key.equalsIgnoreCase("humidity")) || (state_key.equalsIgnoreCase("percent")) || (test_unite.equals("%"))) {
                    if (Float.parseFloat(status) >= 60) {
                        //To have the icon colored if TV_Value beetwen 30 and 60
                        change_this_icon(2);
                    } else if (Float.parseFloat(status) >= 30) {
                        //To have the icon colored if TV_Value >30
                        change_this_icon(1);
                    } else {
                        //To have the icon colored if TV_Value <30
                        change_this_icon(0);
                    }
                } else {
                    // #93
                    if (status.equals("off") || status.equals("false") || status.equals("0") || status.equals("0.0")) {
                        change_this_icon(0);
                        //set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
                    } else change_this_icon(2);
                }
            }
        });
    }

    public void onClick(View arg0) {
        if (with_graph) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = Float_graph_size * activity.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            if (!isopen) {
                this.isopen = true;
                try {
                    LL_background.removeView(featurePan2_buttons);
                    LL_background.removeView(featurePan2);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, sizeint));
                LL_background.addView(featurePan2_buttons);
                LL_background.addView(featurePan2);
                canvas.activate = true;
                canvas.updateTimer();
            } else {
                this.isopen = false;
                LL_background.removeView(featurePan2_buttons);
                LL_background.removeView(featurePan2);
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                canvas.activate = false;    //notify Graphical_Info_View to stop its UpdateTimer
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }

}



