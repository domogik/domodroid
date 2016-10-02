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
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import activities.Graphics_Manager;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.CallUrl;

@SuppressWarnings("ALL")
public class Graphical_List extends Basic_Graphical_widget implements OnClickListener {


    private LinearLayout featurePan2;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private Handler handler;
    private Message msg;
    private static String mytag = "Graphical_List";
    private String url = null;
    public static FrameLayout container = null;
    public static FrameLayout myself = null;
    public static Boolean with_list = true;
    private Boolean realtime = false;
    private String[] known_values;
    private String[] real_values;
    JSONObject Values = null;
    private ArrayList<HashMap<String, String>> listItem;
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
            stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
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
            Tracer.v(mytag, "Json with_list :" + with_list.toString());
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

            //used in previous version of domogik until 0.3
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
            ListView listeChoices = new ListView(context);

            listItem = new ArrayList<HashMap<String, String>>();
            //list_usable_choices = new Vector<String>();
            for (int i = 0; i < known_values.length; i++) {
                //list_usable_choices.add(getStringResourceByName(known_values[i]));
                HashMap<String, String> map = new HashMap<String, String>();
                try {
                    map.put("choice", getResources().getString(Graphics_Manager.getStringIdentifier(context, (known_values[i]).toLowerCase())));
                } catch (Exception e) {
                    Tracer.d(mytag, "no translation for: " + known_values[i]);
                    map.put("choice", known_values[i]);
                }
                if (api_version >= 0.7f) {
                    map.put("cmd_to_send", real_values[i]);
                } else {
                    map.put("cmd_to_send", known_values[i]);
                }
                listItem.add(map);

            }


            SimpleAdapter adapter_map = new SimpleAdapter(getContext(), listItem,
                    R.layout.item_choice, new String[]{"choice", "cmd_to_send"}, new int[]{R.id.choice, R.id.cmd_to_send});
            listeChoices.setAdapter(adapter_map);
            listeChoices.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if ((position < listItem.size()) && (position > -1)) {
                        //process selected command
                        HashMap<String, String> map = new HashMap<String, String>();
                        map = listItem.get(position);
                        cmd_requested = map.get("cmd_to_send");
                        Tracer.d(mytag, "command selected at Position = " + position + "  Command = " + cmd_requested);
                        new CommandeThread().execute();
                    }
                }
            });

            listeChoices.setScrollingCacheEnabled(false);
            //feature panel 2 which will contain list of selectable choices
            featurePan2 = new LinearLayout(context);
            featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            featurePan2.setGravity(Gravity.CENTER_VERTICAL);
            featurePan2.setPadding(5, 10, 5, 10);
            featurePan2.addView(listeChoices);

        } else {
            Tracer.v(mytag, "Json with_list :" + with_list.toString());
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

    public class CommandeThread extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Handler temphandler = new Handler(context.getMainLooper());
            temphandler.post(new Runnable() {
                                 public void run() {
                                     if (cmd_requested != null) {
                                         String Url2send = "";
                                         //TODO change for 0.4
                                         if (api_version >= 0.7f) {
                                             Url2send = url + "cmd/id/" + command_id + "?" + command_type + "=" + cmd_requested;
                                         } else {
                                             Url2send = url + "command/" + type + "/" + address + "/" + cmd_requested;
                                         }
                                         Tracer.i(mytag, "Sending to Rinor : <" + Url2send + ">");
                                         JSONObject json_Ack = null;
                                         try {
                                             new CallUrl().execute(Url2send, login, password, "3000", String.valueOf(SSL));
                                             //json_Ack = Rest_com.connect_jsonobject(Url2send,login,password,3000);
                                         } catch (Exception e) {
                                             Tracer.e(mytag, "Rinor exception sending command <" + e.getMessage() + ">");
                                         }
                /*
                try {
                    Boolean ack = JSONParser.Ack(json_Ack);
                    if (!ack) {
                        Tracer.i(mytag, "Received error from Rinor : <" + json_Ack.toString() + ">");
                        handler.sendEmptyMessage(2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                */
                                     }

                                 }
                             }
            );
            return null;

        }
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return tmp / p;
    }


    public void onClick(View v) {
        if (with_list) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = 262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            if (!isopen) {
                this.isopen = true;
                Tracer.d(mytag, "on click");
                try {
                    LL_background.removeView(featurePan2);
                    Tracer.d(mytag, "removeView(featurePan2)");
                } catch (Exception e) {
                }
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
                Tracer.d(mytag, "addView(featurePan2)");
                LL_background.addView(featurePan2);
            } else {
                this.isopen = false;
                LL_background.removeView(featurePan2);
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            }
        }
    }

}



