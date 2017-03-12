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
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import misc.tracerengine;
import rinor.send_command;

@SuppressWarnings("Convert2Diamond")
public class Graphical_Info_commands extends Basic_Graphical_widget {


    private LinearLayout featurePan2;
    private View featurePan2_buttons;
    private EditText value1 = null;
    private Message msg;
    private static String mytag;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    private Boolean realtime = false;
    private int dpiClassification;
    private JSONObject jparam;
    private String command_id = null;
    private String command_type[] = null;
    private String value_type = null;
    private List<EditText> allEds = null;
    private int number_of_command_parameters;

    private final Entity_Feature feature;
    private final int session_type;


    public Graphical_Info_commands(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Feature feature, Handler handler) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag,  handler);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Info_commands(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Map feature_map, Handler handler) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag,  handler);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        String parameters = feature.getParameters();
        int dev_id = feature.getDevId();
        String state_key = feature.getState_key();
        value_type = feature.getValue_type();
        String stateS;
        mytag = "Graphical_Info_commands (" + dev_id + ")";
        try {
            stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            stateS = state_key;
        }
        myself = this;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Label Text size according to the screen size
        float size60 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics);
        float size120 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, metrics);

        Tracer.i(mytag, "New instance for name = " + name + " state_key = " + state_key);

        try {
            jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
        } catch (Exception e) {
            Tracer.d(mytag, "no param for this device");
        }
        try {
            number_of_command_parameters = jparam.getInt("number_of_command_parameters");
            command_id = jparam.getString("command_id");
            command_type = new String[number_of_command_parameters];
            String[] command_data_type = new String[number_of_command_parameters];
            EditText ed;
            TextView tv_edittext;
            allEds = new ArrayList<>();
            //allEds will list references to EditTexts, so we can iterate it and get the data.
            for (int current_parameter = 0; current_parameter < number_of_command_parameters; current_parameter++) {
                command_type[current_parameter] = jparam.getString("command_type" + (current_parameter + 1));
                command_data_type[current_parameter] = jparam.getString("command_data_type" + (current_parameter + 1));
                Tracer.d(mytag, "command_type_" + current_parameter + "=" + command_type[current_parameter]);
                Tracer.d(mytag, "command_data_type" + current_parameter + "=" + command_data_type[current_parameter]);
                tv_edittext = new TextView(activity);
                tv_edittext.setTextSize(20.0f);
                //translate this command_type
                String command_type_display = "";
                try {
                    command_type_display = getContext().getString(translate.do_translate(getContext(), Tracer, command_type[current_parameter]));
                } catch (Exception e1) {
                    command_type_display = command_type[current_parameter];
                }
                command_type_display += " :";
                tv_edittext.setText(command_type_display);
                ed = new EditText(activity);
                allEds.add(ed);
                if (value_type.equals("string"))
                    ed.setInputType(InputType.TYPE_CLASS_TEXT);
                if (value_type.equals("number"))
                    ed.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                ed.setTextSize(18);
                ed.setTextColor(Color.BLACK);
                ed.setMinWidth((int) (size120));
                featurePan2 = new LinearLayout(activity);
                featurePan2.setPadding(5, 10, 5, 10);
                featurePan2.addView(tv_edittext);
                featurePan2.addView(ed);
                LL_background.addView(featurePan2);
            }
        } catch (JSONException e) {
            Tracer.d(mytag, "No command_id/or number of commands or type or data_type for this device");
            e.printStackTrace();
        }

        //state key
        TextView state_key_view = new TextView(activity);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));


        Button button_send = new Button(activity);
        button_send.setMinWidth((int) (size60));
        button_send.setText(activity.getString(translate.do_translate(getContext(), Tracer, "send")));
        button_send.setOnClickListener(new OnClickListener() {
                                           public void onClick(View v) {
                                               new CommandeThread().execute();
                                           }
                                       }
        );

        LL_featurePan.addView(button_send);
        LL_infoPan.addView(state_key_view);

    }

    private class CommandeThread extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Handler handler = new Handler(activity.getMainLooper());
            handler.post(new Runnable() {
                             public void run() {
                                 String state_progress = "";
                                 if (api_version >= 0.7f) {
                                     if (value_type.equals("number")) {
                                         //#134 from here
                                         for (int current_parameter = 0; current_parameter < number_of_command_parameters; current_parameter++) {
                                             String temp_value = (allEds.get(current_parameter).getText().toString()).replace(",", ".");
                                             allEds.get(current_parameter).setText(String.valueOf(temp_value));
                                         }
                                     }
                                     for (int current_parameter = 0; current_parameter < number_of_command_parameters; current_parameter++) {
                                         state_progress = command_type[current_parameter] + "=" + URLEncoder.encode(allEds.get(current_parameter).getText().toString()) + "&";
                                     }
                                     //remove last &
                                     if (state_progress.endsWith("&")) {
                                         state_progress = state_progress.substring(0, state_progress.length() - 1);
                                     }
                                     Tracer.i(mytag, "Sending to Rinor : <" + state_progress + ">");
                                     JSONObject json_Ack = null;
                                     try {
                                         send_command.send_it(activity, Tracer, command_id, null, state_progress, api_version);
                                         //Clean all text from allEds
                                         for (int i = 0; i < allEds.size(); i++) {
                                             allEds.get(i).setText("");
                                         }
                                     } catch (Exception e) {
                                         Tracer.e(mytag, "Rinor exception sending command <" + e.getMessage() + ">");
                                     }
                                 }

                             }
                         }
            );
            return null;

        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == VISIBLE) {

        }
    }
}