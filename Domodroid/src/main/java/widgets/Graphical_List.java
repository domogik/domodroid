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
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;
import rinor.send_command;

import static activities.Activity_Main.SV_Main_ScrollView;

@SuppressWarnings("ALL")
public class Graphical_List extends Basic_Graphical_widget implements OnClickListener {

    private ListView LV_listChoices = new ListView(context);
    ;
    private ListView LV_listCommands;
    private ArrayList<HashMap<String, String>> listItem;
    private LinearLayout featurePan2;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private Handler handler;
    private Message msg;
    private static String mytag = "Graphical_List";
    private String url = null;
    public static FrameLayout container = null;
    public static FrameLayout myself = null;
    public Boolean with_list = true;
    private Boolean realtime = false;
    private String[] known_values;
    private String[] real_values;
    JSONObject Values = null;
    private ArrayList<HashMap<String, String>> listItemCommands;
    private TextView cmd_to_send = null;
    private String cmd_requested = null;
    private String address;
    private String type;
    private int id;
    private Entity_Feature feature;
    private String state_key;
    private String parameters;
    private int dev_id;
    private final int session_type;
    private String command_id = null;
    private String command_type = null;
    private final SharedPreferences params;
    private String stateS;
    private boolean isopen = false;
    private int nb_item_for_history;
    private int currentint;
    private int sizeint;

    public Graphical_List(tracerengine Trac,
                          final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params,
                          final Entity_Feature feature, Handler handler) {
        super(params, context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_List(tracerengine Trac,
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
        this.state_key = feature.getState_key();
        this.dev_id = feature.getDevId();
        this.parameters = feature.getParameters();
        this.id = feature.getId();
        this.address = feature.getAddress();
        this.isopen = false;
        try {
            String params_nb_item_for_history = params.getString("history_length", "5");
            this.nb_item_for_history = Integer.valueOf(params_nb_item_for_history);
        } catch (Exception e) {
            Tracer.e(mytag, "Error getting number of item to display");
            this.nb_item_for_history = 5;
        }
        String[] model = feature.getDevice_type_id().split("\\.");
        this.type = model[0];
        String packageName = context.getPackageName();
        this.myself = this;
        setOnLongClickListener(this);
        setOnClickListener(this);

        mytag = "Graphical_List (" + dev_id + ")";

        //state key
        final TextView state_key_view = new TextView(context);
        try {
            stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            stateS = state_key;
        }
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //value
        TV_Value = new TextView(context);
        TV_Value.setTextSize(28);
        TV_Value.setTextColor(Color.BLACK);
        TV_Value.setGravity(Gravity.RIGHT);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        TV_Timestamp = new RelativeTimeTextView(context, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        if (api_version >= 0.7f) {
            //get values from json parameters
            JSONObject jparam = null;
            try {
                jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
                String temp = jparam.getString("values");
                Values = new JSONObject(temp.replaceAll("&quot;", "\""));
                Tracer.d(mytag, "Json Values :" + Values);
            } catch (Exception e) {
                Values = null;
                Tracer.e(mytag, "Json Values error " + e.toString());
            }
        }

        if (with_list) {
            //Exploit parameters
            JSONObject jparam = null;
            String command;
            JSONArray commandValues = null;
            try {
                jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
                if (api_version < 0.7f) {
                    command = jparam.getString("command");
                    commandValues = jparam.getJSONArray("commandValues");
                    Tracer.d(mytag, "Json command :" + commandValues);
                } else if (api_version >= 0.7f) {
                    //get commands for domogik >= 0.4
                    int number_of_command_parameters = jparam.getInt("number_of_command_parameters");
                    if (number_of_command_parameters == 1) {
                        command_id = jparam.getString("command_id");
                        command_type = jparam.getString("command_type1");
                        Tracer.d(mytag, "Json command_id :" + command_id + " & command_type :" + command_type);
                    }
                }
            } catch (Exception e) {
                command = "";
                commandValues = null;
                Tracer.e(mytag, "Json command error " + e.toString());
            }

            //used in previous version of domogik until 0.3 if commands
            if (commandValues != null) {
                if (commandValues.length() > 0) {
                    if (known_values != null)
                        known_values = null;

                    known_values = new String[commandValues.length()];
                    for (int i = 0; i < commandValues.length(); i++) {
                        try {
                            known_values[i] = commandValues.getString(i);
                        } catch (Exception e) {
                            known_values[i] = "???";
                        }
                    }
                }
            }

            // used after domogik 0.4 if commands need to display an open informations
            if (command_id != null) {
                TV_Value.setTypeface(typefaceawesome, Typeface.NORMAL);
                //TV_Value.setRotation(180f);
                TV_Value.setText(Html.fromHtml("&#xf13a;"), TextView.BufferType.SPANNABLE);
                //TV_Value.setText(R.string.open_show_command);
            }

            // used after domogik 0.4
            if (Values != null) {
                if (Values.length() > 0) {
                    if (known_values != null)
                        known_values = null;

                    known_values = new String[Values.length()];
                    real_values = new String[Values.length()];
                    Iterator<String> iter = Values.keys();
                    int i = 0;
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            known_values[i] = Values.get(key).toString();
                            real_values[i] = key;
                            Tracer.d(mytag, "Json key :" + key);
                            Tracer.d(mytag, "Json value :" + known_values[i]);
                        } catch (JSONException e) {
                            known_values[i] = "N/A";
                            real_values[i] = "";
                            Tracer.e(mytag, "Json iteration ERROR:" + e.toString());
                        }
                        i++;
                    }
                }
            }
            //list of choices
            LV_listCommands = new ListView(context);

            listItemCommands = new ArrayList<HashMap<String, String>>();
            //list_usable_choices = new Vector<String>();
            for (int i = 0; i < known_values.length; i++) {
                //list_usable_choices.add(getStringResourceByName(known_values[i]));
                HashMap<String, String> map = new HashMap<String, String>();
                try {
                    map.put("choice", getResources().getString(translate.do_translate(context, Tracer, (known_values[i]))));
                } catch (Exception e) {
                    map.put("choice", known_values[i]);
                }
                if (api_version >= 0.7f) {
                    map.put("cmd_to_send", real_values[i]);
                } else {
                    map.put("cmd_to_send", known_values[i]);
                }
                listItemCommands.add(map);
            }


            SimpleAdapter adapter_map = new SimpleAdapter(getContext(), listItemCommands,
                    R.layout.item_choice, new String[]{"choice", "cmd_to_send"}, new int[]{R.id.choice, R.id.cmd_to_send});
            LV_listCommands.setAdapter(adapter_map);
            LV_listCommands.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if ((position < listItemCommands.size()) && (position > -1)) {
                        //process selected command
                        HashMap<String, String> map = new HashMap<String, String>();
                        map = listItemCommands.get(position);
                        cmd_requested = map.get("cmd_to_send");
                        Tracer.d(mytag, "command selected at Position = " + position + "  Command = " + cmd_requested);
                        send_command.send_it(Tracer, url, command_id, command_type, cmd_requested, login, password, SSL, api_version);

                    }
                }
            });

            //LV_listCommands.setScrollingCacheEnabled(false);
            //feature panel 2 which will contain list of selectable choices
            featurePan2 = new LinearLayout(context);
            featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            featurePan2.setGravity(Gravity.CENTER_VERTICAL);
            featurePan2.setPadding(5, 10, 5, 10);
            featurePan2.addView(LV_listCommands);

        }

        super.LL_infoPan.addView(state_key_view);
        super.LL_featurePan.addView(TV_Value);
        super.LL_featurePan.addView(TV_Timestamp);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 2) {
                    Toast.makeText(getContext(), R.string.command_failed, Toast.LENGTH_SHORT).show();

                } else if (msg.what == 9999) {

                    //Message from cache engine
                    //state_engine send us a signal to notify value changed
                    if (session == null)
                        return;

                    String new_val = session.getValue();
                    String Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new value <" + new_val + "> at " + Value_timestamp);

                    Long Value_timestamplong = null;
                    Value_timestamplong = Value_timestamplong.valueOf(Value_timestamp) * 1000;

                    SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(context);
                    if (SP_params.getBoolean("widget_timestamp", false)) {
                        TV_Timestamp.setText(display_sensor_info.timestamp_convertion(Value_timestamp.toString(), context));
                    } else {
                        TV_Timestamp.setReferenceTime(Value_timestamplong);
                    }
                    if (api_version > 0.7f) {
                        try {
                            display_sensor_info.display(Tracer, Values.getString(new_val), Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, context, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, "");
                        } catch (Exception e) {
                            display_sensor_info.display(Tracer, new_val, Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, context, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, "");
                            Tracer.e(mytag, "Can not convert new_val " + e.toString());
                        }
                    } else {
                        display_sensor_info.display(Tracer, new_val, Value_timestamplong, mytag, parameters, TV_Value, TV_Timestamp, context, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, "");
                    }
                    //To have the icon colored as it has no state
                    change_this_icon(2);

                } else if (msg.what == 9998) {
                    // state_engine send us a signal to notify it'll die !
                    Tracer.d(mytag, "cache engine disappeared ===> Harakiri !");
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

        };    //End of handler

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

    private void Hide(Boolean command) {
        if (command_id != null) {
            TV_Value.setTypeface(typefaceawesome, Typeface.NORMAL);
            TV_Value.setText(Html.fromHtml("&#xf13a;"), TextView.BufferType.SPANNABLE);
        }
        isopen = false;
        super.LL_background.removeView(featurePan2);
        super.LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        if (command) {
            String text_to_display = context.getResources().getString(R.string.command_sent) + " " + state_key;
            Toast.makeText(getContext(), text_to_display, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }

    public void onClick(View v) {
        if (with_list) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = 262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
            sizeint = (int) size;
            currentint = LL_background.getHeight();
            if (!isopen) {
                this.isopen = true;
                Tracer.d(mytag, "on click");
                try {
                    super.LL_background.removeView(featurePan2);
                    Tracer.d(mytag, "removeView(featurePan2)");
                } catch (Exception e) {
                }
                super.LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
                Tracer.d(mytag, "addView(featurePan2)");
                super.LL_background.addView(featurePan2);
                this.LV_listCommands.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        SV_Main_ScrollView.requestDisallowInterceptTouchEvent(true);
                        int action = event.getActionMasked();
                        switch (action) {
                            case MotionEvent.ACTION_UP:
                                SV_Main_ScrollView.requestDisallowInterceptTouchEvent(false);
                                break;
                        }
                        return false;
                    }
                });
                // used after domogik 0.4 if commands need to display an open informations
                if (command_id != null) {
                    TV_Value.setTypeface(typefaceawesome, Typeface.NORMAL);
                    TV_Value.setText(Html.fromHtml("&#xf139;"), TextView.BufferType.SPANNABLE);
                }
            } else {
                Hide(false);
            }
        } else {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = ((nb_item_for_history * 35) + 0.5f) * context.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            int currentint = LL_background.getHeight();
            if (!isopen) {
                Tracer.d(mytag, "on click");
                try {
                    super.LL_background.removeView(LV_listChoices);
                    Tracer.d(mytag, "removeView(listeChoices)");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Tracer.d(mytag, "getting history");
                display_last_value sync = new display_last_value();
                sync.execute();
            } else {
                isopen = false;
                super.LL_background.removeView(LV_listChoices);
                super.LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            }

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
                            try {
                                map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, itemArray.getJSONObject(i).getString("TV_Value"))));
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
                            String temp_value_str = "";
                            try {
                                temp_value_str = Values.getString(itemArray.getJSONObject(i).getString("value_str").toLowerCase());
                            } catch (Exception e) {
                                temp_value_str = itemArray.getJSONObject(i).getString("value_str").toLowerCase();
                            }
                            try {
                                map.put("TV_Value", context.getString(translate.do_translate(getContext(), Tracer, temp_value_str)));
                            } catch (Exception e1) {
                                map.put("TV_Value", temp_value_str);
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
            LV_listChoices.setAdapter(adapter_feature);
            LV_listChoices.setScrollingCacheEnabled(false);

            Tracer.d(mytag, "history is: " + listItem);
            if (!listItem.isEmpty()) {
                Tracer.d(mytag, "addView(listeChoices)");
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, currentint + sizeint));
                LL_background.addView(LV_listChoices);
                isopen = true;
            } else {
                Tracer.d(mytag, "history is empty nothing to display");
            }
        }

    }
}



