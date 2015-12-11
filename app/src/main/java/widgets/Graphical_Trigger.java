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

import rinor.Rest_com;
import database.DmdContentProvider;
import database.JSONParser;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;
import activities.Gradients_Manager;
import activities.Graphics_Manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Trigger extends Basic_Graphical_widget implements OnClickListener {

	private TextView unusable;
	private Graphical_Trigger_Button trigger;
	private TextView state;
	private final String address;
	private final String url;
	private Handler handler;
	private Thread threadCommande;
	private String type; 
	private String command; 
	public static FrameLayout container = null;
	public static FrameLayout myself = null;
	private final int dev_id;
	private final int id;
	private final String place_type;
	private final int place_id;
	private tracerengine Tracer = null;
	private final int session_type;
	private boolean usable=false;
	private static String mytag;
	private Message msg;
	private final SharedPreferences params;
	private final String login;
	private final String password;
	private final float api_version;
	private final Activity context;
	private final String usage;
	private String command_id;
	private String command_type;
	private String stateS;
	private final String state_key;

	public Graphical_Trigger(tracerengine Trac, Activity context, 
			String address, String name,String state_key, int id,int dev_id,String stat_key, 
			String url, String usage, String parameters, 
			String model_id, int widgetSize,int session_type,int place_id,String place_type, SharedPreferences params) throws JSONException {
		super(context,Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.address = address;
		this.Tracer = Trac;
		this.url = url;
		this.id=id;
		this.state_key = state_key;
		this.context=context;
		this.usage=usage;
		this.myself=this;
		this.session_type = session_type;
		this.dev_id = dev_id;
		this.place_id= place_id;
		this.place_type= place_type;
		try{
			this.stateS = getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), state_key.toLowerCase())).toString();
		}catch (Exception e){
			Tracer.d(mytag, "no translation for this state:"+stat_key);
			this.stateS= state_key;
		}
		mytag="Graphical_Trigger("+dev_id+")";
		this.params=params;
		login = params.getString("http_auth_username",null);
		password = params.getString("http_auth_password",null);
		api_version=params.getFloat("API_VERSION", 0);

		//get parameters
		JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));

		if(jparam != null ) {
			if (api_version>=0.7f) {
				try{
					command_id=jparam.getString("command_id");
					command_type=jparam.getString("command_type");
					usable=true;
				} catch (Exception e) {
					usable=false;
					Tracer.d(mytag, "Error with this widgets command");
					Tracer.d(mytag, "jparam= "+jparam.toString());
					Tracer.d(mytag, e.toString());
				}
			}else{
				try{
					command = jparam.getString("command");
					usable=true;
				} catch (Exception e) {
					usable=false;
					Tracer.d(mytag, "Error with this widgets command");
					Tracer.d(mytag, "jparam= "+jparam.toString());
					Tracer.d(mytag, e.toString());
				}
			}
		}

		String[] model = model_id.split("\\.");
		type = model[0];

		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setText(stateS);
		LL_infoPan.addView(state);

		//button animated
		trigger = new Graphical_Trigger_Button(context);
		trigger.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		trigger.setOnClickListener(this);

		//unusable
		unusable=new TextView(context);
		unusable.setText(R.string.unusable);
		unusable.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		unusable.setTextColor(Color.BLACK);
		unusable.setTextSize(14);
		unusable.setPadding(0, 0, 15, 0);

		if (usable==true){
			LL_featurePan.addView(trigger);
		}else{
			LL_featurePan.addView(unusable);
		}


	}


	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Handler temphandler =  new Handler(context.getMainLooper());
			temphandler.post( new Runnable(){
				public void run(){
					String Url2send;
					if(api_version>=0.7f){
						Url2send = url+"cmd/id/"+command_id+"?"+command_type+"=1";
					}else{
						Url2send = url+"command/"+type+"/"+address+"/"+command;
					}
					Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
					JSONObject json_Ack = null;
					try {
						json_Ack = Rest_com.connect_jsonobject(Url2send,login,password);
					} catch (Exception e) {
						Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
						Toast.makeText(context, "Rinor exception sending command",Toast.LENGTH_LONG).show();
					}
					try {
						Boolean ack = JSONParser.Ack(json_Ack);
						if(ack==false){
							Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
							Toast.makeText(context, "Received error from Rinor",Toast.LENGTH_LONG).show();
							handler.sendEmptyMessage(2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
					);
			return null;

		}
	}

	public void onClick(View arg0) {
		trigger.startAnim();
		new CommandeThread().execute();
	}

}




