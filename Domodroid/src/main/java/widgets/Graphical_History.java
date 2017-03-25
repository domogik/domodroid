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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.Color_Result;
import misc.tracerengine;
import rinor.Rest_com;

public class Graphical_History extends Basic_Graphical_widget implements OnClickListener {


    private final ListView listeChoices = new ListView(activity);
    private ArrayList<HashMap<String, String>> listItem;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private TextView state;
    private static String mytag;
    private Message msg;

    public FrameLayout container = null;
    private FrameLayout myself = null;

    private Boolean realtime = false;
    private Animation animation;
    private final Entity_Feature feature;
    private String state_key;
    private int dev_id;
    private final int session_type;
    private boolean isopen = false;
    private int nb_item_for_history;
    private TextView state_key_view;
    private String stateS;

    private String test_unite;
    private Color_Result resultView;
    private int currentint;
    private int sizeint;
    private String status;
    private String Value_timestamp;

    public Graphical_History(tracerengine Trac,
                             final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                             final Entity_Feature feature, Handler handler) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_History(tracerengine Trac,
                             final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                             final Entity_Map feature_map, Handler handler) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        String parameters = feature.getParameters();
        this.state_key = feature.getState_key();
        this.isopen = false;
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
        try {
            String params_nb_item_for_history = prefUtils.GetWidgetHistoryLength();
            this.nb_item_for_history = Integer.valueOf(params_nb_item_for_history);
        } catch (Exception e) {
            Tracer.e(mytag, "Error getting number of item to display");
            this.nb_item_for_history = 5;
        }
        myself = this;
        mytag = "Graphical_History(" + dev_id + ")";

        if (stateS.equals("null"))
            stateS = state_key;
        test_unite = "";
        try {
            //Basilic add, number feature has a unit parameter
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            test_unite = jparam.getString("unit");
        } catch (JSONException jsonerror) {
            Tracer.i(mytag, "No unit for this feature");
        }
        setOnClickListener(this);

        //color view if need
        resultView = new Color_Result(activity);

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

        TV_Timestamp = new RelativeTimeTextView(activity, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        super.LL_featurePan.addView(TV_Value);
        super.LL_featurePan.addView(TV_Timestamp);
        super.LL_infoPan.addView(state_key_view);

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 9989) {
                    if (session == null)
                        return true;
                    status = session.getValue();
                    Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new TV_Value <" + status + "> at " + Value_timestamp);
                    update_display();
                } else if (msg.what == 9998) {
                    // state_engine send us a signal to notify it'll die !
                    Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                    session = null;
                    realtime = false;
                    removeView(LL_background);
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    try {
                        finalize();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }    //kill the handler thread itself
                }
                return true;
            }
        });
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    realtime = true;        //we're connected to engine
                    //each time our TV_Value change, the engine will call handler
                    handler.sendEmptyMessage(9989);    //Force to consider current TV_Value in session
                    EventBus.getDefault().register(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....
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

    @Override
    protected void onWindowVisibilityChanged(int visibility) {

    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {
        Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);
        TV_Value.setAnimation(animation);

        Long Value_timestamplong;
        Value_timestamplong = Long.valueOf(Value_timestamp) * 1000;
        if (feature.getDevice_feature_model_id().startsWith("DT_Color")) {
            LL_featurePan.removeView(resultView);
            LL_featurePan.removeView(TV_Value);
            LL_featurePan.removeView(TV_Timestamp);
            if (prefUtils.GetWidgetTimestamp()) {
                TV_Timestamp.setText(display_sensor_info.timestamp_convertion(Value_timestamplong.toString(), activity));
            } else {
                TV_Timestamp.setReferenceTime(Value_timestamplong);
            }
            if (feature.getDevice_feature_model_id().startsWith("DT_ColorRGBHexa.")) {
                //Color result
                //16 means that you should interpret the string as 16-based (hexadecimal)
                Tracer.d(mytag, "debug_color RGBHexa=" + status);
                status = "#" + status.toUpperCase();
                resultView.color = status;
            } else if (feature.getDevice_feature_model_id().startsWith("DT_ColorRGB.")) {
                //Color result
                //16 means that you should interpret the string as 16-based (hexadecimal)
                Tracer.d(mytag, "debug_color RGB=" + status);
                resultView.colorrgb = status;

            } else if (feature.getDevice_feature_model_id().startsWith("DT_ColorCMYK.")) {
                //Color result
                //16 means that you should interpret the string as 16-based (hexadecimal)
                Tracer.d(mytag, "debug_color CMYK=" + status);
                resultView.colorCMYK = status;
            } else if (feature.getDevice_feature_model_id().startsWith("DT_ColorCII.")) {
                //Color result
                //16 means that you should interpret the string as 16-based (hexadecimal)
                Tracer.d(mytag, "debug_color ColorCII=" + status);
                resultView.colorCII = status;
            }
            LL_featurePan.addView(resultView);
            LL_featurePan.addView(TV_Timestamp);
        } else {
            display_sensor_info.display(Tracer, status, Value_timestamplong, mytag, feature.getParameters(), TV_Value, TV_Timestamp, activity, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);
        }

        //To have the icon colored as it has no state
        change_this_icon(2);
    }

    public void onClick(View arg0) {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = ((nb_item_for_history * 35) + 0.5f) * activity.getResources().getDisplayMetrics().density + 0.5f;
        sizeint = (int) size;
        currentint = LL_background.getHeight();
        listItem = new ArrayList<>();
        if (!isopen) {
            Tracer.d(mytag, "on click");
            try {
                LL_background.removeView(listeChoices);
                Tracer.d(mytag, "removeView(listeChoices)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tracer.d(mytag, "getting history");
            display_last_value sync = new display_last_value();
            sync.execute();
        } else {
            isopen = false;
            LL_background.removeView(listeChoices);
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }

    private class display_last_value extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(activity, R.string.loading_data_from_rest, Toast.LENGTH_SHORT).show();
        }

        protected Void doInBackground(Void... params) {
            JSONObject json_LastValues = null;
            JSONArray itemArray;
            try {
                if (api_version <= 0.6f) {
                    Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                    json_LastValues = Rest_com.connect_jsonobject(activity, Tracer, "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", 30000);
                } else if (api_version >= 0.7f) {
                    Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + "sensorhistory/id/" + dev_id + "/last/" + nb_item_for_history);
                    //Don't forget old "dev_id"+"state_key" is replaced by "id"
                    JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(activity, Tracer, "sensorhistory/id/" + dev_id + "/last/" + nb_item_for_history + "", 30000);
                    json_LastValues = new JSONObject();
                    json_LastValues.put("stats", json_LastValues_0_4);

                }
                itemArray = json_LastValues.getJSONArray("stats");
                if (api_version <= 0.6f) {
                    for (int i = itemArray.length(); i >= 0; i--) {
                        try {
                            HashMap<String, String> map = new HashMap<>();
                            try {
                                map.put("TV_Value", activity.getString(translate.do_translate(activity, Tracer, itemArray.getJSONObject(i).getString("TV_Value"))));
                            } catch (Exception e1) {
                                map.put("TV_Value", itemArray.getJSONObject(i).getString("TV_Value"));
                            }
                            map.put("date", itemArray.getJSONObject(i).getString("date"));
                            listItem.add(map);
                            Tracer.d(mytag, map.toString());
                        } catch (Exception e) {
                            Tracer.e(mytag, "Error getting json TV_Value");
                        }
                    }
                } else if (api_version >= 0.7f) {
                    for (int i = 0; i < itemArray.length(); i++) {
                        try {
                            HashMap<String, String> map = new HashMap<>();
                            try {
                                map.put("TV_Value", activity.getString(translate.do_translate(activity, Tracer, itemArray.getJSONObject(i).getString("value_str"))));
                            } catch (Exception e1) {
                                map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                            }
                            if (api_version == 0.7f) {
                                map.put("date", itemArray.getJSONObject(i).getString("date"));
                            } else if (api_version >= 0.8f) {
                                String currenTimestamp = String.valueOf((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                                map.put("date", display_sensor_info.timestamp_convertion(currenTimestamp, activity));
                            }
                            listItem.add(map);
                            Tracer.d(mytag, map.toString());
                        } catch (Exception e) {
                            Tracer.e(mytag, "Error getting json TV_Value");
                        }
                    }
                }
            } catch (Exception e) {
                //return null;
                Tracer.e(mytag, "Error fetching json object");
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (feature.getDevice_feature_model_id().startsWith("DT_Color")) {
                //TODO change to display color in history
                SimpleAdapter adapter_feature = new SimpleAdapter(activity, listItem,
                        R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
                listeChoices.setAdapter(adapter_feature);
                listeChoices.setScrollingCacheEnabled(false);
            } else {
                SimpleAdapter adapter_feature = new SimpleAdapter(activity, listItem,
                        R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
                listeChoices.setAdapter(adapter_feature);
                listeChoices.setScrollingCacheEnabled(false);
            }


            Tracer.d(mytag, "history is: " + listItem);
            if (!listItem.isEmpty()) {
                Tracer.d(mytag, "addView(listeChoices)");
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, currentint + sizeint));
                try {
                    LL_background.removeView(listeChoices);
                } catch (Exception e) {
                    //to avoid #135
                }
                LL_background.addView(listeChoices);
                isopen = true;
            } else {
                Tracer.d(mytag, "history is empty nothing to display");
            }
        }

    }
}




