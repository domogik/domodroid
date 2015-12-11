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

import java.lang.Thread.State;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import rinor.Rest_com;

import database.DmdContentProvider;
import database.JSONParser;
import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.FeatureInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.text.Html;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView.OnEditorActionListener;

public class Graphical_Info_commands extends Basic_Graphical_widget {


	LinearLayout featurePan2;
	private View		  featurePan2_buttons;
	private final EditText value;
	private final Button button_send;
	private final int dev_id;
	private final int id;
	private final String state_key;
	private final TextView state_key_view;
	private final int update;
	private final Activity context;
	private Message msg;
	private final String wname;
	private static String mytag;
	private final String place_type;
	private final int place_id;
	private String url = null;
	public static FrameLayout container = null;
	public static FrameLayout myself = null;
	private tracerengine Tracer = null;
	private final String parameters;
	private Entity_client session = null;
	private Boolean realtime = false;
	private final int session_type;
	private final String login;
	private final String password;
	private final float api_version;

	private final SharedPreferences params;
	private int dpiClassification;
	private final DisplayMetrics metrics;
	private final float size60;
	private final float size70;
	private final String usage;
	private JSONObject jparam;
	private String command_id = null;
	private String command_type = null;
	private final String value_type;
	private String stateS;

	@SuppressLint("HandlerLeak")
	public Graphical_Info_commands(tracerengine Trac,final Activity context, int id,int dev_id, String name, 
			final String state_key, String url,final String usage, int update, 
			int widgetSize, int session_type, final String parameters,int place_id,String place_type, SharedPreferences params, String value_type) {
		super(context,Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.Tracer = Trac;
		this.context = context;
		this.dev_id = dev_id;
		this.id = id;
		this.usage=usage;
		this.state_key = state_key;
		try{
			this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase())).toString();
		}catch (Exception e){
			Tracer.d(mytag, "no translation for this state:"+state_key);
			this.stateS= state_key;
		}
		this.update=update;
		this.wname = name;
		this.url = url;
		this.myself = this;
		this.session_type = session_type;
		this.parameters = parameters;
		this.place_id= place_id;
		this.place_type= place_type;
		this.params=params;
		this.value_type=value_type;

		mytag="Graphical_Info_commands ("+dev_id+")";
		metrics = getResources().getDisplayMetrics();
		//Label Text size according to the screen size
		size60 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, metrics);
		size70 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, metrics);

		Tracer.e(mytag,"New instance for name = "+wname+" state_key = "+state_key);
		login = params.getString("http_auth_username",null);
		password = params.getString("http_auth_password",null);
		api_version=params.getFloat("API_VERSION", 0);
		try {
			jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
		} catch (Exception e) {
			Tracer.d(mytag,"no param for this device");
		}
		try {
			command_id = jparam.getString("command_id");
			command_type= jparam.getString("command_type");
		} catch (JSONException e) {
			Tracer.d(mytag, "No command_id for this device");
		}

		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(stateS);
		state_key_view.setTextColor(Color.parseColor("#333333"));

		//value
		value = new EditText(context);
		//#46 change keyboard type in fonction of datatype
		if (value_type.equals("string"))
			value.setInputType(InputType.TYPE_CLASS_TEXT);
		if (value_type.equals("number"))
			value.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		value.setTextSize(18);
		value.setTextColor(Color.BLACK);
		value.setMinWidth((int) (size70));
		value.setMaxWidth((int) (size70));

		button_send= new Button(context);
		//TODO translation
		button_send.setMinWidth((int) (size60));
		button_send.setText("Send");
		button_send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new CommandeThread().execute();
			}
		}
				);

		LL_featurePan.addView(value);
		LL_featurePan.addView(button_send);
		LL_infoPan.addView(state_key_view);

	}

	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Handler handler =  new Handler(context.getMainLooper());
			handler.post( new Runnable(){
				public void run(){
					String Url2send;
					if(api_version>=0.7f){
						Url2send = url+"cmd/id/"+command_id+"?"+command_type+"="+ URLEncoder.encode(value.getText().toString());
						Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
						JSONObject json_Ack = null;
						try {
							json_Ack = Rest_com.connect_jsonobject(Url2send,login,password);
							value.setText("");
						} catch (Exception e) {
							Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
							Toast.makeText(context, "Rinor exception sending command",Toast.LENGTH_LONG).show(); 
						}
						try {
							Boolean ack = JSONParser.Ack(json_Ack);
							if(ack==false){
								try{
									Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
									Toast.makeText(context, "Received error from Rinor",Toast.LENGTH_LONG).show(); 
								} catch (Exception e) {
									Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
									Toast.makeText(context, "Received error from Rinor",Toast.LENGTH_LONG).show(); 
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
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
		if(visibility==VISIBLE){

		}
	}
}