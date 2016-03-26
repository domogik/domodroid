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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;

import activities.Graphics_Manager;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import database.WidgetUpdate;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;

import misc.tracerengine;

import android.text.Html;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Graphical_Info extends Basic_Graphical_widget implements OnClickListener {


    LinearLayout featurePan2;
    private View featurePan2_buttons;
    private TextView value;
    private TextView value1;
    private Graphical_Info_View canvas;
    private Animation animation;
    private Message msg;
    private static String mytag;
    public static FrameLayout container = null;
    public static FrameLayout myself = null;
    public Boolean with_graph = true;
    private Entity_client session = null;
    private Boolean realtime = false;
    private final Entity_Feature feature;
    private String state_key;
    private String parameters;
    private int dev_id;
    private final int session_type;
    private final SharedPreferences params;
    private String url = null;
    private int dpiClassification;
    private int update;
    private TextView state_key_view;
    private String stateS;
    private Typeface typefaceweather;
    private Typeface typefaceawesome;

    public Graphical_Info(tracerengine Trac,
                          final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params, final int update,
                          final Entity_Feature feature, Handler handler) {
        super(context, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature;
        this.url = url;
        this.params = params;
        this.session_type = session_type;
        this.update = update;
        onCreate();
    }

    public Graphical_Info(tracerengine Trac,
                          final Activity context, String url, int widgetSize, int session_type, int place_id, String place_type, SharedPreferences params, final int update,
                          final Entity_Map feature_map, Handler handler) {
        super(context, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, container, handler);
        this.feature = feature_map;
        this.url = url;
        this.session_type = session_type;
        this.params = params;
        this.update = update;
        onCreate();
    }

    private void onCreate() {
        this.parameters = feature.getParameters();
        this.dev_id = feature.getDevId();
        this.state_key = feature.getState_key();

        try {
            stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase()));
        } catch (Exception e) {
            Tracer.d(mytag, "no translation for: " + state_key);
            stateS = state_key;
        }
        myself = this;
        setOnClickListener(this);

        mytag = "Graphical_Info (" + dev_id + ")";
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        //Label Text size according to the screen size
        float size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        float size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);

        Tracer.i(mytag, "New instance for name = " + name + " state_key = " + state_key);
        String login = params.getString("http_auth_username", null);
        String password = params.getString("http_auth_password", null);
        float api_version = params.getFloat("API_VERSION", 0);

        //state key
        state_key_view = new TextView(context);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //value
        value = new TextView(context);
        value.setTextSize(28);
        value.setTextColor(Color.BLACK);
        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);
        typefaceweather = Typeface.createFromAsset(context.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        typefaceawesome = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");

        if (with_graph) {

            //feature panel 2 which will contain graphic
            featurePan2 = new LinearLayout(context);
            featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            featurePan2.setGravity(Gravity.CENTER_VERTICAL);
            featurePan2.setPadding(5, 10, 5, 10);
            //canvas
            canvas = new Graphical_Info_View(Tracer, context, params);
            canvas.dev_id = dev_id;
            canvas.id = feature.getId();
            canvas.state_key = state_key;
            canvas.url = url;
            canvas.update = update;

            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            featurePan2_buttons = layoutInflater.inflate(R.layout.graph_buttons, null);
            View v = null;

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

        LL_featurePan.addView(value);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 9999) {
                    //Message from widgetupdate
                    //state_engine send us a signal to notify value changed
                    if (session == null)
                        return;

                    String loc_Value = session.getValue();
                    Tracer.d(mytag, "Handler receives a new value <" + loc_Value + ">");
                    String test_unite = "";
                    try {
                        float formatedValue = 0;
                        if (loc_Value != null) {
                            formatedValue = Round(Float.parseFloat(loc_Value), 2);
                            Tracer.v(mytag, " Round the value: " + loc_Value + " to " + formatedValue);
                        }
                        try {
                            //Basilic add, number feature has a unit parameter
                            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
                            test_unite = jparam.getString("unit");
                            //#30 add Scale value if too big for byte only
                            switch (test_unite) {
                                case "b":
                                    value.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(loc_Value)));
                                    break;
                                case "ko":
                                    value.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(loc_Value) * 1024));
                                    break;
                                case "°":
                                    //TODO find how to update the rotate when a new value is receiveds from events or mq
                                    //remove the textView from parent LinearLayout
                                    LL_featurePan.removeView(value);
                                    //Display an arrow with font-awesome
                                    value.setTypeface(typefaceawesome, Typeface.NORMAL);
                                    value.setText("\uf176");
                                    //display the real value in smaller font
                                    value1 = new TextView(context);
                                    value1.setTextSize(14);
                                    value1.setTextColor(Color.BLACK);
                                    value1.setText(formatedValue + test_unite);
                                    //Create a rotate animation for arrow with formatedValue as angle
                                    RotateAnimation animation = new RotateAnimation(0, formatedValue, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    animation.setDuration(0);
                                    animation.setFillEnabled(true);
                                    animation.setFillAfter(true);
                                    animation.setFillBefore(true);
                                    //apply animation to textView
                                    value.startAnimation(animation);
                                    //apply gravity and size to textview with font-awesome
                                    value.setMinimumHeight(LL_featurePan.getHeight());
                                    value.setMinimumWidth(100);
                                    value.setGravity(Gravity.CENTER);
                                    //Create an empty linearlayout that will contains the value
                                    LinearLayout LL_Temp = new LinearLayout(context);
                                    //Re-add the view in parent's one
                                    LL_Temp.addView(value1);
                                    LL_Temp.addView(value);
                                    LL_featurePan.addView(LL_Temp);
                                    break;
                                default:
                                    if (state_key.equalsIgnoreCase("current_wind_speed")) {
                                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf03e;"), TextView.BufferType.SPANNABLE);
                                    } else if (state_key.equalsIgnoreCase("current_humidity")) {
                                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf07a;"), TextView.BufferType.SPANNABLE);
                                    } else if (state_key.equalsIgnoreCase("current_barometer_value")) {
                                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf079;"), TextView.BufferType.SPANNABLE);
                                    } else if (state_key.contains("temperature")) {
                                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf053;"), TextView.BufferType.SPANNABLE);
                                    }
                                    value.setText(formatedValue + " " + test_unite);
                                    break;
                            }
                        } catch (JSONException e) {
                            //It has no unit in database or in json
                            if (state_key.equalsIgnoreCase("temperature"))
                                value.setText(formatedValue + " °C");
                            else if (state_key.equalsIgnoreCase("pressure"))
                                value.setText(formatedValue + " hPa");
                            else if (state_key.equalsIgnoreCase("humidity"))
                                value.setText(formatedValue + " %");
                            else if (state_key.equalsIgnoreCase("percent"))
                                value.setText(formatedValue + " %");
                            else if (state_key.equalsIgnoreCase("visibility"))
                                value.setText(formatedValue + " km");
                            else if (state_key.equalsIgnoreCase("chill"))
                                value.setText(formatedValue + " °C");
                            else if (state_key.equalsIgnoreCase("speed"))
                                value.setText(formatedValue + " km/h");
                            else if (state_key.equalsIgnoreCase("drewpoint"))
                                value.setText(formatedValue + " °C");
                            else if (state_key.equalsIgnoreCase("condition-code") || state_key.toLowerCase().contains("condition_code") || state_key.toLowerCase().contains("current_code")) {
                                //Add try catch to avoid other case that make #1794
                                try {
                                    //use xml and weather fonts here
                                    value.setTypeface(typefaceweather, Typeface.NORMAL);
                                    value.setText(Graphics_Manager.Names_conditioncodes(getContext(), (int) formatedValue));
                                } catch (Exception e1) {
                                    Tracer.d(mytag, "no translation for: " + loc_Value);
                                    value.setText(loc_Value);
                                }
                            } else value.setText(loc_Value);
                        }
                    } catch (Exception e) {
                        // It's probably a String that could'nt be converted to a float
                        Tracer.d(mytag, "Handler exception : new value <" + loc_Value + "> not numeric !");
                        try {
                            Tracer.d(mytag, "Try to get value translate from R.STRING");
                            //todo #90
                            if (loc_Value.startsWith("AM") && loc_Value.contains("/PM")) {
                                Tracer.d(mytag, "Try to split: " + loc_Value + " in two parts to translate it");
                                StringTokenizer st = new StringTokenizer(loc_Value, "/");
                                String AM = st.nextToken();
                                String PM = st.nextToken();
                                try {
                                    AM = AM.replace("AM ", "");
                                    AM = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), AM.toLowerCase()));
                                } catch (Exception amexception) {
                                    Tracer.d(mytag, "no translation for: " + AM);
                                }
                                try {
                                    PM = PM.replace("PM ", "");
                                    PM = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), PM.toLowerCase()));
                                } catch (Exception pmexception) {
                                    Tracer.d(mytag, "no translation for: " + PM);
                                }
                                value.setText(R.string.am + " " + AM + "/" + R.string.pm + " " + PM);
                            } else {
                                value.setText(Graphics_Manager.getStringIdentifier(getContext(), loc_Value.toLowerCase()));
                            }
                        } catch (Exception e1) {
                            Tracer.d(mytag, "no translation for: " + loc_Value);
                            if (state_key.equalsIgnoreCase("current_sunset")) {
                                state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf052;"), TextView.BufferType.SPANNABLE);
                                // Convert value to hour
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                                Date testDate = null;
                                try {
                                    testDate = sdf.parse(loc_Value);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                                String newFormat = formatter.format(testDate);
                                value.setText(newFormat);
                            } else if (state_key.equalsIgnoreCase("current_sunrise")) {
                                state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                                state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf051;"), TextView.BufferType.SPANNABLE);
                                // Convert value to hour
                                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                                Date testDate = null;
                                try {
                                    testDate = sdf.parse(loc_Value);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                                String newFormat = formatter.format(testDate);
                                value.setText(newFormat);
                            } else if (state_key.equalsIgnoreCase("current_last_updated")) {
                                // TODO: convert value to translated date
                                value.setText(loc_Value);
                            } else {
                                value.setText(loc_Value);
                            }

                        }
                    }
                    //Change icon if in %
                    if ((state_key.equalsIgnoreCase("humidity")) || (state_key.equalsIgnoreCase("percent")) || (test_unite.equals("%"))) {
                        if (Float.parseFloat(loc_Value) >= 60) {
                            //To have the icon colored if value beetwen 30 and 60
                            change_this_icon(2);
                        } else if (Float.parseFloat(loc_Value) >= 30) {
                            //To have the icon colored if value >30
                            change_this_icon(1);
                        } else {
                            //To have the icon colored if value <30
                            change_this_icon(0);
                        }
                    } else {
                        //To have the icon colored as it has no state
                        change_this_icon(2);
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
                        t.printStackTrace();
                    }    //kill the handler thread itself
                }
            }

        };
        LL_infoPan.addView(state_key_view);
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

    public void onClick(View arg0) {
        if (with_graph) {
            //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
            float size = 262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
            int sizeint = (int) size;
            if (LL_background.getHeight() != sizeint) {
                try {
                    LL_background.removeView(featurePan2_buttons);
                    LL_background.removeView(featurePan2);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, sizeint));
                LL_background.addView(featurePan2_buttons);
                LL_background.addView(featurePan2);
                canvas.activate = true;
                canvas.updateTimer();
            } else {
                LL_background.removeView(featurePan2_buttons);
                LL_background.removeView(featurePan2);
                LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
                canvas.activate = false;    //notify Graphical_Info_View to stop its UpdateTimer
            }
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {

        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float Round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return tmp / p;
    }

}



