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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;

import rinor.Rest_com;

import database.DmdContentProvider;
import database.WidgetUpdate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class Graphical_History extends Basic_Graphical_widget implements OnClickListener{


	private ListView listeChoices;
	private TextView value;
	private TextView state;
	private TextView state_key_view;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private int update;
	private static String mytag;
	private Message msg;
	private String wname;
	private String stateS = "";
	private String url = null;
	private String login;
	private String password;
	
	public static FrameLayout container = null;
	public static FrameLayout myself = null;
	private tracerengine Tracer = null;
	
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private String place_type;
	private int place_id;
	private Activity context;
	private String usage;
	private Animation animation;
	
	@SuppressLint("HandlerLeak")
	public Graphical_History(tracerengine Trac,Activity context, int id,int dev_id, String name, 
			final String state_key, String url,final String usage, int update, 
			int widgetSize, int session_type, final String parameters,int place_id,String place_type, SharedPreferences params) {
		super(context,Trac, id, name, "", usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.context = context;
		this.Tracer = Trac;
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.id = id;
		this.url = url;
		this.usage=usage;
		this.update = update;
		this.wname = name;
		this.myself=this;
		this.session_type = session_type;
		this.stateS = getResources().getText(R.string.State).toString();
		this.place_id= place_id;
		this.place_type= place_type;
		setOnClickListener(this);
		
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    			
		mytag="Graphical_History("+dev_id+")";
		
		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(state_key);
		state_key_view.setTextColor(Color.parseColor("#333333"));

		//value
		value = new TextView(context);
		value.setTextSize(28);
		value.setTextColor(Color.BLACK);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);
		
		super.LL_featurePan.addView(value);
		super.LL_infoPan.addView(state_key_view);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {	
				String status;
				if(msg.what == 9999) {
					if(session == null)
						return;
					status = session.getValue();
					String loc_Value = session.getValue();
					Tracer.d(mytag,"Handler receives a new value <"+loc_Value+">" );
						value.setAnimation(animation);
						value.setText(loc_Value);
						//To have the icon colored as it has no state
				    	IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
				    	
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(LL_background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself
				}
				
			}
			
		};
		//================================================================================
		/*
		 * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
		WidgetUpdate cache_engine = WidgetUpdate.getInstance();
		if(cache_engine != null) {
			session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
			if(Tracer.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
										//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}
			
		}
		
		//================================================================================
	}
	
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		
	}
	
	private void getlastvalue() {
		//TODO add something in the view
		//add last 5 values with their dates
		//featurePan2.addView();
		JSONObject json_LastValues = null;
		JSONArray itemArray=null; 
		listeChoices = new ListView(context);
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String,String>>();
		try {
			json_LastValues = Rest_com.connect_jsonobject(url+"stats/"+dev_id+"/"+state_key+"/last/5/",login,password);
			itemArray = json_LastValues.getJSONArray("stats");
			for (int i =itemArray.length(); i >= 0; i--){
				try {
					HashMap<String,String> map=new HashMap<String,String>();
					map.put("value",itemArray.getJSONObject(i).getString("value"));
					map.put("date",itemArray.getJSONObject(i).getString("date"));
					listItem.add(map);
					Tracer.d(mytag, map.toString());
				}catch (Exception e) {
					Tracer.e(mytag,"Error getting json value");
				}
			}
		} catch (Exception e) {
			//return null;
			Tracer.e(mytag,"Error getting json object");
		}
		
		SimpleAdapter adapter_feature=new SimpleAdapter(this.context,listItem,
				R.layout.item_phone,new String[] {"value","date"},new int[] {R.id.phone_value,R.id.phone_date});
		listeChoices.setAdapter(adapter_feature);
		listeChoices.setScrollingCacheEnabled(false);
		}
	
	public void onClick(View arg0) {
	//Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
	float size=262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
	int sizeint=(int)size;
		if(LL_background.getHeight() != sizeint){
			Tracer.d(mytag,"on click");
			try {
				LL_background.removeView(listeChoices);
				Tracer.d(mytag,"removeView(listeChoices)");
				
			} catch (Exception e) {}
			LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,sizeint));
			getlastvalue();
			Tracer.d(mytag,"addView(listeChoices)");
			LL_background.addView(listeChoices);
		}else{
			LL_background.removeView(listeChoices);
			LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		}
		return ;
		
	}
	
	
}



