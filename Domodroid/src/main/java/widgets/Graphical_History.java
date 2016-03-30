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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import Abstract.display_sensor_info;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;

public class Graphical_History extends Basic_Graphical_widget implements OnClickListener {


    private ListView listeChoices;
    private TextView value;
    private TextView state;
    private int id;
    private static String mytag;
    private Message msg;
    private String url = null;
    private String login;
    private String password;
    private float api_version;

    public static FrameLayout container = null;
    private static FrameLayout myself = null;

    private Entity_client session = null;
    private Boolean realtime = false;
    private Animation animation;
    private final Entity_Feature feature;
    private String state_key;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;

    public Graphical_History(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_History(tracerengine Trac,
                             final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                             final Entity_Map feature_map, Handler handler) {
        super(params, context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;
        onCreate();
    }

    private void onCreate() {
        String parameters = feature.getParameters();
        this.dev_id = feature.getDevId();
        this.state_key = feature.getState_key();
        this.id = feature.getId();
        myself = this;
        String stateS = "";
        try {
            stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            stateS = state_key;
        }
        setOnClickListener(this);

        login = params.getString("http_auth_username", null);
        password = params.getString("http_auth_password", null);
        api_version = params.getFloat("API_VERSION", 0);

        mytag = "Graphical_History(" + dev_id + ")";

        //state key
        TextView state_key_view = new TextView(context);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //value
        value = new TextView(context);
        value.setTextSize(28);
        value.setTextColor(Color.BLACK);
        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        super.LL_featurePan.addView(value);
        super.LL_infoPan.addView(state_key_view);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String status;
                if (msg.what == 9999) {
                    if (session == null)
                        return;
                    status = session.getValue();
                    String loc_Value = session.getValue();
                    Tracer.d(mytag, "Handler receives a new value <" + loc_Value + ">");
                    value.setAnimation(animation);

                    display_sensor_info.display(Tracer, loc_Value, mytag, feature.getParameters(), value, context, LL_featurePan, null, null, state_key, null, null, null);

                    //To have the icon colored as it has no state
                    change_this_icon(2);

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
                session = new Entity_client(id, "", mytag, handler, session_type);
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

    private void getlastvalue() {
        JSONObject json_LastValues = null;
        JSONArray itemArray = null;
        listeChoices = new ListView(context);
        ArrayList<HashMap<String, String>> listItem = new ArrayList<>();
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/last/5/");
                json_LastValues = Rest_com.connect_jsonobject(url + "stats/" + dev_id + "/" + state_key + "/last/5/", login, password, 10000);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/last/5");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(url + "sensorhistory/id/" + id + "/last/5", login, password, 10000);
                json_LastValues = new JSONObject();
                json_LastValues.put("stats", json_LastValues_0_4);

            }
            itemArray = json_LastValues.getJSONArray("stats");
            if (api_version <= 0.6f) {
                for (int i = itemArray.length(); i >= 0; i--) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("value", itemArray.getJSONObject(i).getString("value"));
                        map.put("date", itemArray.getJSONObject(i).getString("date"));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json value");
                    }
                }
            } else if (api_version == 0.7f) {
                for (int i = 0; i < itemArray.length(); i++) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("value", itemArray.getJSONObject(i).getString("value_str"));
                        map.put("date", itemArray.getJSONObject(i).getString("date"));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json value");
                    }
                }
            } else if (api_version >= 0.8f) {
                //Prepare timestamp conversion
                Calendar calendar = Calendar.getInstance();
                TimeZone tz = TimeZone.getDefault();
                calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                java.util.Date currenTimeZone;
                for (int i = 0; i < itemArray.length(); i++) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("value", itemArray.getJSONObject(i).getString("value_str"));
                        currenTimeZone = new java.util.Date((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                        map.put("date", sdf.format(currenTimeZone));
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json value");
                    }
                }
            }

        } catch (Exception e) {
            //return null;
            Tracer.e(mytag, "Error fetching json object");
        }

        SimpleAdapter adapter_feature = new SimpleAdapter(this.context, listItem,
                R.layout.item_phone, new String[]{"value", "date"}, new int[]{R.id.phone_value, R.id.phone_date});
        listeChoices.setAdapter(adapter_feature);
        listeChoices.setScrollingCacheEnabled(false);
    }

    public void onClick(View arg0) {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = 262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
        int sizeint = (int) size;
        if (LL_background.getHeight() != sizeint) {
            Tracer.d(mytag, "on click");
            try {
                LL_background.removeView(listeChoices);
                Tracer.d(mytag, "removeView(listeChoices)");

            } catch (Exception e) {
                e.printStackTrace();
            }
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
            getlastvalue();
            Tracer.d(mytag, "addView(listeChoices)");
            LL_background.addView(listeChoices);
        } else {
            LL_background.removeView(listeChoices);
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }


}



