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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import database.DmdContentProvider;
import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class Graphical_Info extends Basic_Graphical_widget implements OnClickListener {


	LinearLayout featurePan2;
	private View		  featurePan2_buttons;
	private TextView value;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private TextView state_key_view;
	private Graphical_Info_View canvas;
	private int update;
	private Animation animation;
	private Activity context;
	private Message msg;
	private String wname;
	private static String mytag;
	private String place_type;
	private int place_id;
	private String url = null;
	public static FrameLayout container = null;
	public static FrameLayout myself = null;
	public Boolean with_graph = true;
	private tracerengine Tracer = null;
	private String parameters;
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private String login;
	private String password;
	private float api_version;
	
	private SharedPreferences params;
	private int dpiClassification;
	private DisplayMetrics metrics;
	private float size10;
	private float size5;	
	private String usage;
	
	@SuppressLint("HandlerLeak")
	public Graphical_Info(tracerengine Trac,Activity context, int id,int dev_id, String name, 
			final String state_key, String url,final String usage, int update, 
			int widgetSize, int session_type, final String parameters,int place_id,String place_type, SharedPreferences params) {
		super(context,Trac, id, name, "", usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.Tracer = Trac;
		this.context = context;
		this.dev_id = dev_id;
		this.id = id;
		this.usage=usage;
		this.state_key = state_key;
		this.update=update;
		this.wname = name;
		this.url = url;
		this.myself = this;
		this.session_type = session_type;
		this.parameters = parameters;
		this.place_id= place_id;
		this.place_type= place_type;
		this.params=params;
		setOnClickListener(this);
				
		mytag="Graphical_Info ("+dev_id+")";
		metrics = getResources().getDisplayMetrics();
		//Label Text size according to the screen size
		size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
		size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);

		Tracer.e(mytag,"New instance for name = "+wname+" state_key = "+state_key);
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	api_version=params.getFloat("API_VERSION", 0);
		
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

		if(with_graph) {
			
			//feature panel 2 which will contain graphic
			featurePan2=new LinearLayout(context);
			featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			featurePan2.setGravity(Gravity.CENTER_VERTICAL);
			featurePan2.setPadding(5, 10, 5, 10);
			//canvas
			canvas = new Graphical_Info_View(Tracer,context,params);
			canvas.dev_id = dev_id;
			canvas.id = id;
			canvas.state_key = state_key;
			canvas.url = url;
			canvas.update = update;
			
			LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			featurePan2_buttons=layoutInflater.inflate(R.layout.graph_buttons,null);
			View v=null;
			
			v=featurePan2_buttons.findViewById(R.id.bt_prev);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v=featurePan2_buttons.findViewById(R.id.bt_next);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v=featurePan2_buttons.findViewById(R.id.bt_year);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v=featurePan2_buttons.findViewById(R.id.bt_month);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v=featurePan2_buttons.findViewById(R.id.bt_week);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v=featurePan2_buttons.findViewById(R.id.bt_day);
			if(v != null)
				v.setOnClickListener(canvas);
			
			v = featurePan2_buttons.findViewById(R.id.period);
			if(v != null)
				canvas.dates=(TextView)v;
			
			//background_stats.addView(canvas);
			featurePan2.addView(canvas);
		}
		
		LL_featurePan.addView(value);
		LL_infoPan.addView(state_key_view);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 9999) {
						//Message from widgetupdate
						//state_engine send us a signal to notify value changed
					if(session == null)
						return;
					
					String loc_Value = session.getValue();
					Tracer.d(mytag,"Handler receives a new value <"+loc_Value+">" );
					try {
						float formatedValue = 0;
						if(loc_Value != null)
							formatedValue = Round(Float.parseFloat(loc_Value),2); 
						try {
							//Basilic add, number feature has a unit parameter
							JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
							String test_unite = jparam.getString("unit");
							value.setText(formatedValue+ " "+test_unite);
							//TODO #30 add Scale value if too big
//							if (test_unite.equals("b") == true){
//								value.setText(android.text.format.Formatter.formatFileSize(context,(long)formatedValue));
//							}else{
//								value.setText(formatedValue+ " "+test_unite);
//							}
						} catch (JSONException e) {							
						if(state_key.equalsIgnoreCase("temperature") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("pressure") == true) value.setText(formatedValue+" hPa");
						else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("percent") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("visibility") == true) value.setText(formatedValue+" km");
						else if(state_key.equalsIgnoreCase("chill") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("speed") == true) value.setText(formatedValue+" km/h");
						else if(state_key.equalsIgnoreCase("drewpoint") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("condition-code") == true)
							//Add try catch to avoid other case that make #1794
							try {
								value.setText(Graphics_Manager.Names_conditioncodes(getContext(),(int)formatedValue));
							}catch (Exception e1) {
								value.setText(loc_Value);
							}
						else value.setText(loc_Value);
						}
						value.setAnimation(animation);
					} catch (Exception e) {
						// It's probably a String that could'nt be converted to a float
						Tracer.d(mytag,"Handler exception : new value <"+loc_Value+"> not numeric !" );
						try {
							Tracer.d(mytag,"Try to get value translate from R.STRING" );
							value.setText(Graphics_Manager.getStringIdentifier(getContext(), loc_Value.toLowerCase()));
						}catch (Exception e1) {
							Tracer.d(mytag,"Nothing in R.STRING for "+loc_Value );
						value.setText(loc_Value);
						}
					}
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
			if (api_version<=0.6f){
				session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
			}else if (api_version==0.7f){
				session = new Entity_client(id, "", mytag, handler, session_type);
			}
			if(Tracer.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
										//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}
			
		}
		//================================================================================
		//updateTimer();	//Don't use anymore cyclic refresh....	

	}

	public void onClick(View arg0) {
		if(with_graph) {
			//Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
			float size=262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
			int sizeint=(int)size;
			if(LL_background.getHeight() != sizeint){
				try {
					LL_background.removeView(featurePan2_buttons);
					LL_background.removeView(featurePan2);
					
				} catch (Exception e) {}
				
				LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,sizeint));
				LL_background.addView(featurePan2_buttons);
				LL_background.addView(featurePan2);
				//TODO get value and draw graph in async-task to Avoid ANR.
				canvas.activate = true;
				canvas.updateTimer();
			}
			else{
				LL_background.removeView(featurePan2_buttons);
				LL_background.removeView(featurePan2);
				LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				canvas.activate = false;	//notify Graphical_Info_View to stop its UpdateTimer
			}
		}
		return ;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}
		
}



