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

import java.util.ArrayList;
import java.util.List;

import rinor.CallUrl;
import rinor.Rest_com;
import database.DmdContentProvider;
import database.JSONParser;
import database.WidgetUpdate;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.domogik.domodroid13.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import misc.List_Icon_Adapter;
import misc.tracerengine;

import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Binary_New extends Basic_Graphical_widget implements OnClickListener {

    private Button ON;
    private Button OFF;
    private final TextView state;
    private final String address;
    private String state_progress;
    private final String url;
    private final String usage;
    private final Handler handler;
    private String value0;
    private String value1;
    private final String type;
    public final boolean activate = false;
    private Animation animation;
    private boolean touching;
    private int updating = 0;
    private Message msg;
    private final String wname;
    public static FrameLayout container = null;
    private static FrameLayout myself = null;
    private static String mytag = "";
    private tracerengine Tracer = null;
    private Activity context = null;
    private String stateS = "";
    private String Value_0 = "0";
    private String Value_1 = "1";
    private final String login;
    private final String password;
    private final float api_version;
    private JSONObject jparam;
    private String command_id = null;
    private String command_type = null;

    private Entity_client session = null;
    private Boolean realtime = false;


    public Graphical_Binary_New(tracerengine Trac,
                                final Activity context, String address, String name, int id, int dev_id, String state_key, String url, final String usage,
                                String parameters, String model_id, int update, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params) {
        super(context, Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type, mytag, container);
        this.Tracer = Trac;
        this.context = context;
        this.address = address;
        this.url = url;
        this.usage = usage;
        this.wname = name;
        this.myself = this;
        try {
            this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            this.stateS = state_key;
        }

        login = params.getString("http_auth_username", null);
        password = params.getString("http_auth_password", null);
        api_version = params.getFloat("API_VERSION", 0);

        mytag = "Graphical_Binary_New(" + dev_id + ")";
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
                }
            } catch (JSONException e) {
                Tracer.d(mytag, "No command_id for this device");
                ON.setEnabled(false);
                OFF.setEnabled(false);
            }
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

        String[] model = model_id.split("\\.");
        type = model[0];
        Tracer.d(mytag, "model_id = <" + model_id + "> type = <" + type + "> value0 = " + value0 + "  value1 = " + value1);

        //state
        state = new TextView(context);
        state.setTextColor(Color.BLACK);
        state.setText(stateS);
        //		if(api_version>=0.7f)
        //		state.setVisibility(INVISIBLE);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        float dps = 40;
        int pixels = (int) (dps * scale + 0.5f);
        //first seekbar on/off
        ON = new Button(context);
        ON.setOnClickListener(this);
        ON.setHeight(pixels);
        //ON.setWidth(60);
        ON.setTag("ON");
        try {
            Tracer.d(mytag, "Try to get value translate from R.STRING");
            ON.setText(context.getString(Graphics_Manager.getStringIdentifier(getContext(), this.Value_1.toLowerCase())));
        } catch (Exception e1) {
            Tracer.d(mytag, "no translation for: " + this.Value_1);
            ON.setText(this.Value_1);
        }
        ON.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        //ON.setBackgroundResource(R.drawable.boolean_on);
        //ON.setPadding(10, 0, 10, 0);

        OFF = new Button(context);
        OFF.setOnClickListener(this);
        OFF.setTag("OFF");
        OFF.setHeight(pixels);
        //OFF.setWidth(60);
        OFF.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        //OFF.setBackgroundResource(R.drawable.boolean_off);
        try {
            Tracer.d(mytag, "Try to get value translate from R.STRING");
            OFF.setText(context.getString(Graphics_Manager.getStringIdentifier(getContext(), this.Value_0.toLowerCase())));
        } catch (Exception e1) {
            Tracer.d(mytag, "no translation for: " + this.Value_0);
            OFF.setText(this.Value_0);
        }
        //OFF.setPadding(0,10,0,10);

        super.LL_featurePan.addView(ON);
        super.LL_featurePan.addView(OFF);
        super.LL_infoPan.addView(state);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (activate) {
                    Tracer.d(mytag, "Handler receives a request to die ");
                    if (realtime) {
                        Tracer.get_engine().unsubscribe(session);
                        session = null;
                        realtime = false;
                    }
                    //That seems to be a zombie
                    //removeView(background);
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
                } else {
                    try {
                        Bundle b = msg.getData();
                        if ((b != null) && (b.getString("message") != null)) {
                            if (b.getString("message").equals(value0)) {
                                try {
                                    Tracer.d(mytag, "Try to get value translate from R.STRING");
                                    state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_0.toLowerCase())));
                                } catch (Exception e1) {
                                    Tracer.d(mytag, "no translation for: " + Value_0);
                                    state.setText(stateS + " : " + Value_0);
                                }
                                change_this_icon(0);
                            } else if (b.getString("message").equals(value1)) {
                                try {
                                    Tracer.d(mytag, "Try to get value translate from R.STRING");
                                    state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_1.toLowerCase())));
                                } catch (Exception e1) {
                                    Tracer.d(mytag, "no translation for: " + Value_1);
                                    state.setText(stateS + " : " + Value_1);
                                }
                                change_this_icon(2);
                            }
                            state.setAnimation(animation);
                        } else {
                            if (msg.what == 2) {
                                Toast.makeText(getContext(), "Command Failed", Toast.LENGTH_SHORT).show();
                            } else if (msg.what == 9999) {
                                //state_engine send us a signal to notify value changed
                                if (session == null)
                                    return;
                                String new_val = session.getValue();
                                Tracer.d(mytag, "Handler receives a new value <" + new_val + ">");
                                if (new_val.equals(value0)) {
                                    try {
                                        Tracer.d(mytag, "Try to get value translate from R.STRING");
                                        state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_0.toLowerCase())));
                                    } catch (Exception e1) {
                                        Tracer.d(mytag, "no translation for: " + Value_0);
                                        state.setText(stateS + " : " + Value_0);
                                    }
                                    change_this_icon(0);
                                } else if (new_val.equals(value1)) {
                                    try {
                                        Tracer.d(mytag, "Try to get value translate from R.STRING");
                                        state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_1.toLowerCase())));
                                    } catch (Exception e1) {
                                        Tracer.d(mytag, "no translation for: " + Value_1);
                                        state.setText(stateS + " : " + Value_1);
                                    }
                                    change_this_icon(2);
                                } else {
                                    try {
                                        Tracer.d(mytag, "Try to get value translate from R.STRING");
                                        state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), new_val.toLowerCase())));
                                    } catch (Exception e1) {
                                        Tracer.d(mytag, "no translation for: " + new_val);
                                        state.setText(stateS + " : " + new_val);
                                    }
                                }
                            } else if (msg.what == 9998) {
                                // state_engine send us a signal to notify it'll die !
                                Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                                session = null;
                                realtime = false;
                                //removeView(background);
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

                    } catch (Exception e) {
                        Tracer.e(mytag, "Handler error for device " + wname);
                        e.printStackTrace();
                    }
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
            if (Tracer.get_engine().subscribe(session)) {
                realtime = true;        //we're connected to engine
                //each time our value change, the engine will call handler
                handler.sendEmptyMessage(9999);    //Force to consider current value in session
            }

        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....

    }

    public void onClick(View v) {
        if (v.getTag().equals("OFF")) {
            change_this_icon(0);
            try {
                Tracer.d(mytag, "Try to get value translate from R.STRING");
                state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_0.toLowerCase())));
            } catch (Exception e1) {
                Tracer.d(mytag, "no translation for: " + Value_0);
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
                Tracer.d(mytag, "Try to get value translate from R.STRING");
                state.setText(stateS + " : " + context.getString(Graphics_Manager.getStringIdentifier(getContext(), Value_1.toLowerCase())));
            } catch (Exception e1) {
                Tracer.d(mytag, "no translation for: " + Value_1);
                state.setText(stateS + " : " + Value_1);
            }
            if (api_version >= 0.7f) {
                state_progress = "1";
            } else {
                state_progress = value1;
            }
        }
        new CommandeThread().execute();
    }

    public class CommandeThread extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Handler temphandler = new Handler(context.getMainLooper());
            temphandler.post(new Runnable() {
                                 public void run() {
                                     updating = 3;
                                     String Url2send;
                                     if (api_version >= 0.7f) {
                                         Url2send = url + "cmd/id/" + command_id + "?" + command_type + "=" + state_progress;
                                     } else {
                                         Url2send = url + "command/" + type + "/" + address + "/" + state_progress;
                                     }
                                     Tracer.i(mytag, "Sending to Rinor : <" + Url2send + ">");
                                     //JSONObject json_Ack = null;
                                     try {
                                         new CallUrl().execute(Url2send, login, password, "3000");
                                         //json_Ack = Rest_com.connect_jsonobject(Url2send, login, password,3000);
                                     } catch (Exception e) {
                                         Tracer.e(mytag, "Rinor exception sending command <" + e.getMessage() + ">");
                                         Toast.makeText(context, "Rinor exception sending command", Toast.LENGTH_LONG).show();
                                     }
                                     /*
                                     try {
                                        Boolean ack = JSONParser.Ack(json_Ack);
                                         if (!ack) {
                                             Tracer.i(mytag, "Received error from Rinor : <" + json_Ack.toString() + ">");
                                             Toast.makeText(context, "Received error from Rinor", Toast.LENGTH_LONG).show();
                                             handler.sendEmptyMessage(2);
                                         }
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                    */
                                 }
                             }
            );
            return null;

        }
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            //activate=true;
        }
    }

}





