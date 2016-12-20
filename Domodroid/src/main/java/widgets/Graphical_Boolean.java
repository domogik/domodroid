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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;

import static activities.Activity_Main.context;

@SuppressWarnings("ALL")
public class Graphical_Boolean extends Basic_Graphical_widget implements View.OnClickListener {

    private ListView listeChoices = new ListView(context);
    private ArrayList<HashMap<String, String>> listItem;
    private TextView state;
    private RelativeTimeTextView TV_Timestamp;
    private String value0;
    private String value1;
    private String Value_0;
    private String Value_1;
    private ImageView bool;
    private static String mytag;
    private Message msg;
    private String stateS = "";
    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    private Entity_Feature feature;
    private String state_key;
    private String parameters;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;
    private final String url;
    private String usage;
    private String address;
    private Boolean realtime = false;
    private int nb_item_for_history;
    private boolean isopen = false;
    private int id;
    private int currentint;
    private int sizeint;

    public Graphical_Boolean(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Boolean(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;
        onCreate();
    }

    public void onCreate() {
        myself = this;
        this.address = feature.getAddress();
        this.usage = feature.getIcon_name();
        this.state_key = feature.getState_key();
        this.dev_id = feature.getDevId();
        this.parameters = feature.getParameters();
        mytag = "Graphical_Boolean(" + dev_id + ")";
        this.id = feature.getId();
        this.isopen = false;
        try {
            String params_nb_item_for_history = params.getString("history_length", "5");
            this.nb_item_for_history = Integer.valueOf(params_nb_item_for_history);
        } catch (Exception e) {
            Tracer.e(mytag, "Error getting number of item to display");
            this.nb_item_for_history = 5;
        }

        try {
            this.stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            this.stateS = state_key;
        }

        try {
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            value0 = jparam.getString("value0");
            value1 = jparam.getString("value1");
        } catch (Exception e) {
            value0 = "0";
            value1 = "1";
        }

        if (usage.equals("light")) {
            this.Value_0 = getResources().getText(R.string.light_stat_0).toString();
            this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
        } else if (usage.equals("shutter")) {
            this.Value_0 = getResources().getText(R.string.shutter_stat_0).toString();
            this.Value_1 = getResources().getText(R.string.shutter_stat_1).toString();
        } else {
            this.Value_0 = value0;
            this.Value_1 = value1;
        }

        setOnClickListener(this);

        //state
        state = new TextView(context);
        state.setTextColor(Color.BLACK);

        state.setText(stateS + " : " + context.getString(translate.do_translate(getContext(), Tracer, "unknown")));

        TV_Timestamp = new RelativeTimeTextView(context, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT;

        //boolean on/off
        bool = new ImageView(context);
        bool.setImageResource(R.drawable.boolean_n_a);
        bool.setLayoutParams(params);

        super.LL_infoPan.addView(state);
        super.LL_featurePan.addView(bool);
        super.LL_featurePan.addView(TV_Timestamp);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 9999) {
                    if (session == null)
                        return;
                    String status = session.getValue();
                    String Value_timestamp = session.getTimestamp();

                    if (status != null) {
                        Tracer.d(mytag, "Handler receives a new TV_Value <" + status + "> at " + Value_timestamp);

                        Long Value_timestamplong = null;
                        Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
                        if (SP_params.getBoolean("widget_timestamp", false)) {
                            TV_Timestamp.setText(display_sensor_info.timestamp_convertion(Value_timestamplong.toString(), context));
                        } else {
                            TV_Timestamp.setReferenceTime(Value_timestamplong);
                        }
                        try {
                            if (status.equals(value0) || status.equals("0")) {
                                bool.setImageResource(R.drawable.boolean_off);
                                //change color if statue=low to (usage, o) means off
                                //note sure if it must be kept as set previously as default color.
                                change_this_icon(0);
                                try {
                                    state.setText(stateS + " : " + context.getString(translate.do_translate(getContext(), Tracer, Value_0)));
                                } catch (Exception e1) {
                                    state.setText(stateS + " : " + Value_0);
                                }
                            } else if (status.equals(value1) || status.equals("1")) {
                                bool.setImageResource(R.drawable.boolean_on);
                                //change color if statue=high to (usage, 2) means on
                                change_this_icon(2);
                                try {
                                    state.setText(stateS + " : " + context.getString(translate.do_translate(getContext(), Tracer, Value_1)));
                                } catch (Exception e1) {
                                    state.setText(stateS + " : " + Value_1);
                                }
                            } else {
                                bool.setImageResource(R.drawable.boolean_n_a);
                                change_this_icon(0);
                                state.setText(stateS + " : " + context.getString(translate.do_translate(getContext(), Tracer, "unknown")));
                            }
                        } catch (Exception e) {
                            Tracer.e(mytag, "handler error device " + name);
                            e.printStackTrace();
                        }
                    }
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
                    }    //kill the handler thread itself
                }

            }

        };
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            if (api_version <= 0.6f) {
                session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            } else if (api_version >= 0.7f) {
                session = new Entity_client(feature.getId(), "", mytag, handler, session_type);
            }
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    realtime = true;        //we're connected to engine
                    //each time our value change, the engine will call handler
                    handler.sendEmptyMessage(9999);    //Force to consider current value in session
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {

    }

    public void onClick(View arg0) {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = ((nb_item_for_history * 35) + 0.5f) * context.getResources().getDisplayMetrics().density + 0.5f;
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
            Toast.makeText(context, R.string.loading_data_from_rest, Toast.LENGTH_SHORT).show();
        }

        protected Void doInBackground(Void... params) {
            JSONObject json_LastValues = null;
            JSONArray itemArray = null;
            try {
                if (api_version <= 0.6f) {
                    Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                    json_LastValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", login, password, 30000, SSL);
                } else if (api_version >= 0.7f) {
                    Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/last/" + nb_item_for_history);
                    //Don't forget old "dev_id"+"state_key" is replaced by "id"
                    JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(Tracer, url + "sensorhistory/id/" + id + "/last/" + nb_item_for_history + "", login, password, 30000, SSL);
                    json_LastValues = new JSONObject();
                    json_LastValues.put("stats", json_LastValues_0_4);

                }
                itemArray = json_LastValues.getJSONArray("stats");
                if (api_version <= 0.6f) {
                    for (int i = itemArray.length(); i >= 0; i--) {
                        try {
                            HashMap<String, String> map = new HashMap<>();
                            if (itemArray.getJSONObject(i).getString("TV_Value").equals(value0) || itemArray.getJSONObject(i).getString("TV_Value").equals("0")) {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, Value_0)));
                                } catch (Exception e1) {
                                    map.put("TV_Value", Value_0);
                                }
                            } else if (itemArray.getJSONObject(i).getString("TV_Value").equals(value1) || itemArray.getJSONObject(i).getString("TV_Value").equals("1")) {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, Value_1)));
                                } catch (Exception e1) {
                                    map.put("TV_Value", Value_1);
                                }
                            } else {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, "N/A")));
                                } catch (Exception e1) {
                                    map.put("TV_Value", "N/A");
                                }
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
                            if (itemArray.getJSONObject(i).getString("value_str").equals(value0) || itemArray.getJSONObject(i).getString("value_str").equals("0")) {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, Value_0)));
                                } catch (Exception e1) {
                                    map.put("TV_Value", Value_0);
                                }
                            } else if (itemArray.getJSONObject(i).getString("value_str").equals(value1) || itemArray.getJSONObject(i).getString("value_str").equals("1")) {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, Value_1)));
                                } catch (Exception e1) {
                                    map.put("TV_Value", Value_1);
                                }
                            } else {
                                try {
                                    map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, "N/A")));
                                } catch (Exception e1) {
                                    map.put("TV_Value", "N/A");
                                }
                            }
                            if (api_version == 0.7f) {
                                map.put("date", itemArray.getJSONObject(i).getString("date"));
                            } else if (api_version >= 0.8f) {
                                String currenTimestamp = String.valueOf((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                                map.put("date", display_sensor_info.timestamp_convertion(currenTimestamp, context));
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
            SimpleAdapter adapter_feature = new SimpleAdapter(context, listItem,
                    R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
            listeChoices.setAdapter(adapter_feature);
            listeChoices.setScrollingCacheEnabled(false);

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



