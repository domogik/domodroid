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

package Abstract;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import activities.Graphics_Manager;
import misc.tracerengine;

import static activities.Activity_Main.context;


public abstract class display_sensor_info {

    public static void display(tracerengine Tracer, String loc_Value, Long Value_timestamp, String mytag, String parameters, TextView value, RelativeTimeTextView timestamp,
                               Activity activity, LinearLayout LL_featurePan, Typeface typefaceweather, Typeface typefaceawesome,
                               String state_key, TextView state_key_view, String stateS, String test_unite) {
        TextView value1;
        pref_utils prefUtils = new pref_utils(context);
        if (Value_timestamp != 0) {
            if (prefUtils.timestamp()) {
                timestamp.setText(timestamp_convertion(Value_timestamp.toString(), activity));
            } else {
                timestamp.setReferenceTime(Value_timestamp);
            }
        }
        try {
            float formatedValue = 0;
            if (loc_Value != null) {
                formatedValue = calcul.Round_float(Float.parseFloat(loc_Value), 2);
                Tracer.v(mytag, " Round_float the value: " + loc_Value + " to " + formatedValue);
            }
            if (!test_unite.equals("")) {
                //Basilic add, number feature has a unit parameter
                //#30 add Scale value if too big for byte, ko and Wh unit
                switch (test_unite) {
                    case "b":
                        value.setText(android.text.format.Formatter.formatFileSize(activity, Long.parseLong(loc_Value)));
                        break;
                    case "ko":
                        value.setText(android.text.format.Formatter.formatFileSize(activity, Long.parseLong(loc_Value) * 1024));
                        break;
                    case "Wh":
                        //#30
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        break;
                    case "°":
                        //TODO find how to update the rotate when a new value is receive from events or mq
                        //remove the textView from parent LinearLayout
                        LL_featurePan.removeView(value);
                        LL_featurePan.removeView(timestamp);
                        //Display an arrow with font-awesome
                        value.setTypeface(typefaceweather, Typeface.NORMAL);
                        value.setText("\uf0b1");
                        //display the real value in smaller font
                        value1 = new TextView(activity);
                        value1.setTextSize(14);
                        value1.setTextColor(Color.BLACK);
                        value1.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        //Create a rotate animation for arrow with formatedValue as angle
                        RotateAnimation animation = new RotateAnimation(0.0f, formatedValue, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
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
                        LinearLayout LL_Temp = new LinearLayout(activity);
                        //Re-add the view in parent's one
                        LL_Temp.addView(value1);
                        LL_Temp.addView(value);
                        LL_featurePan.addView(LL_Temp);
                        LL_featurePan.addView(timestamp);
                        break;
                    case "°C":
                    case "K":
                    case "°F":
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf055;"), TextView.BufferType.SPANNABLE);
                        break;
                    case "bar":
                    case "mbar":
                    case "Pa":
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf079;"), TextView.BufferType.SPANNABLE);
                        break;
                    case "ms":
                    case "s":
                    case "min":
                    case "h":
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf08a;"), TextView.BufferType.SPANNABLE);
                        break;
                    case "Year":
                    case "Month":
                    case "Day":
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        state_key_view.setTypeface(typefaceawesome, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf073;"), TextView.BufferType.SPANNABLE);
                        break;
                    default:
                        value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " " + test_unite);
                        if (state_key.equalsIgnoreCase("current_wind_speed")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf03e;"), TextView.BufferType.SPANNABLE);
                        } else if (state_key.equalsIgnoreCase("current_humidity")) {
                            state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf07a;"), TextView.BufferType.SPANNABLE);
                        } else if (state_key.equalsIgnoreCase("weight")) {
                            state_key_view.setTypeface(typefaceawesome, Typeface.NORMAL);
                            state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf24e;"), TextView.BufferType.SPANNABLE);
                        }
                        break;
                }
            } else {
                //It has no unit in database or in json
                if (state_key.equalsIgnoreCase("temperature"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("pressure"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " hPa");
                else if (state_key.equalsIgnoreCase("humidity"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " %");
                else if (state_key.equalsIgnoreCase("percent"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " %");
                else if (state_key.equalsIgnoreCase("visibility"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " km");
                else if (state_key.equalsIgnoreCase("chill"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("speed"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " km/h");
                else if (state_key.equalsIgnoreCase("drewpoint"))
                    value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value) + " °C");
                else if (state_key.equalsIgnoreCase("condition-code") || state_key.toLowerCase().contains("condition_code") || state_key.toLowerCase().contains("current_code")) {
                    //Add try catch to avoid other case that make #1794
                    try {
                        //use xml and weather fonts here
                        value.setTypeface(typefaceweather, Typeface.NORMAL);
                        value.setText(Graphics_Manager.Names_conditioncodes(activity, (int) formatedValue));
                    } catch (Exception e1) {
                        Tracer.e(mytag, "no translation for condition-code: " + loc_Value);
                        value.setText(loc_Value);
                    }
                } else if (state_key.equalsIgnoreCase("callerid")) {
                    value.setText(phone_convertion(Tracer, mytag, loc_Value));
                } else if (state_key.toLowerCase().startsWith("rainlevel")) {
                    //Add try catch to avoid other case that make #1794
                    try {
                        //use xml and weather fonts here
                        value.setTypeface(typefaceweather, Typeface.NORMAL);
                        switch (loc_Value) {
                            case "0":
                                value.setText(activity.getResources().getIdentifier("wi_na", "string", activity.getPackageName()));
                                break;
                            case "1":
                                value.setText(activity.getResources().getIdentifier("wi_cloud", "string", activity.getPackageName()));
                                break;
                            case "3":
                                value.setText(activity.getResources().getIdentifier("wi_hail", "string", activity.getPackageName()));
                                break;
                            case "4":
                                value.setText(activity.getResources().getIdentifier("wi_rain", "string", activity.getPackageName()));
                                break;
                            case "5":
                                value.setText(activity.getResources().getIdentifier("wi_showers", "string", activity.getPackageName()));
                                break;
                            default:
                                value.setText(activity.getResources().getIdentifier("wi_na", "string", activity.getPackageName()));
                                break;
                        }

                    } catch (Exception e1) {
                        Tracer.e(mytag, "no translation for rainlevel: " + loc_Value);
                        e1.printStackTrace();
                        value.setText(loc_Value);
                    }
                } else value.setText(value_convertion(Tracer, mytag, formatedValue, loc_Value));
            }
        } catch (Exception e) {
            // It's probably a String that could not be converted to a float
            Tracer.d(mytag, "Handler exception : new value <" + loc_Value + "> not numeric !");
            try {
                //todo #90
                if (loc_Value.startsWith("AM") && loc_Value.contains("/PM")) {
                    Tracer.d(mytag, "Try to split: " + loc_Value + " in two parts to translate it");
                    StringTokenizer st = new StringTokenizer(loc_Value, "/");
                    String AM = st.nextToken();
                    String PM = st.nextToken();
                    try {
                        AM = AM.replace("AM ", "");
                        AM = activity.getResources().getString(translate.do_translate(activity, Tracer, AM));
                    } catch (Exception amexception) {

                    }
                    try {
                        PM = PM.replace("PM ", "");
                        PM = activity.getResources().getString(translate.do_translate(activity, Tracer, PM));
                    } catch (Exception pmexception) {

                    }
                    value.setText(R.string.am + " " + AM + "/" + R.string.pm + " " + PM);
                } else {

                    if (state_key.equalsIgnoreCase("current_sunset")) {
                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf052;"), TextView.BufferType.SPANNABLE);
                        value.setText(hour_convertion(Tracer, mytag, loc_Value));
                    } else if (state_key.equalsIgnoreCase("current_sunrise")) {
                        state_key_view.setTypeface(typefaceweather, Typeface.NORMAL);
                        state_key_view.setText(Html.fromHtml(stateS + " " + "&#xf051;"), TextView.BufferType.SPANNABLE);
                        value.setText(hour_convertion(Tracer, mytag, loc_Value));
                    } else if (state_key.equalsIgnoreCase("current_last_updated")) {
                        // convert value to translated date in locale settings
                        try {
                            loc_Value = loc_Value.substring(0, loc_Value.lastIndexOf(" "));
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm a", Locale.ENGLISH);
                            Date testDate = sdf.parse(loc_Value);
                            Tracer.d(mytag + " Date conversion", "Works");
                            SimpleDateFormat formatter = new SimpleDateFormat("EEE dd MMM yyyy HH:mm", Locale.getDefault());
                            String newFormat = formatter.format(testDate);
                            value.setText(newFormat);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Tracer.e(mytag + " Date conversion", "Error: " + ex.toString());
                            value.setText(loc_Value);
                        }
                    } else if (state_key.equalsIgnoreCase("callerid")) {
                        value.setText(phone_convertion(Tracer, mytag, loc_Value));
                    } else {
                        value.setText(translate.do_translate(activity, Tracer, loc_Value));
                    }
                }
            } catch (Exception e1) {
                Tracer.d(mytag, "no translation for this STRING value: " + loc_Value);
                value.setText(loc_Value);
            }
        }
    }

    /**
     * @param Tracer Tracerengine used for logging
     * @param mytag  Tag to know where it was called from
     * @param phone  a String to convert
     * @return the convertion to Locale User phone number display
     */
    public static String phone_convertion(tracerengine Tracer, String mytag, String phone) {
        try {
            String convert_phone = PhoneNumberUtils.formatNumber(phone);
            // todo it remove the "-" like in jean-phillipe replace bye jeanphillipe
            // Tracer.d(mytag, "phone convertion from:" + phone + " to " + convert_phone);
            return convert_phone;
        } catch (Exception ex) {
            ex.printStackTrace();
            Tracer.e(mytag + "Phone conversion", "Error: " + ex.toString());
            return phone;
        }
    }

    /**
     * @param Tracer        Tracerengine used for logging
     * @param mytag         Tag to know where it was called from
     * @param number        in float format
     * @param origin_number in string format
     * @return A string convert to number but return as string in User Locale format
     */
    public static String value_convertion(tracerengine Tracer, String mytag, Float number, String origin_number) {
        try {
            String convert_number = NumberFormat.getInstance().format(number);
            return convert_number;
        } catch (Exception ex) {
            ex.printStackTrace();
            Tracer.e(mytag + "value_convertion", "Error: " + ex.toString());
            return origin_number;
        }
    }

    /**
     * @param timeStampStr the timestamp to convert
     * @param context      Context used to get date format
     * @return a Timestamp convert to date
     */
    public static String timestamp_convertion(String timeStampStr, Context context) {
        try {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
            DateFormat timeFormat = DateFormat.getTimeInstance();
            Date netDate = (new Date(Long.parseLong(timeStampStr)));
            return (dateFormat.format(netDate) + " - " + timeFormat.format(netDate));
        } catch (Exception ignored) {
            return timeStampStr;
        }
    }

    /**
     * @param Tracer Tracerengine used for logging
     * @param mytag  Tag to know where it was called from
     * @param hour   a String from domogik in hh:mm:ss
     * @return hour in User Locale language
     */
    public static String hour_convertion(tracerengine Tracer, String mytag, String hour) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.ENGLISH);
        Date testDate = null;
        try {
            testDate = sdf.parse(hour);
        } catch (Exception ex) {
            ex.printStackTrace();
            Tracer.e(mytag + "Date conversion", "Error: " + ex.toString());
        }
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(testDate);
    }
}
