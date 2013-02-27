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
import java.util.Vector;
import android.content.Context;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Graphical_List extends FrameLayout implements OnTouchListener, OnLongClickListener {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout featurePan2;
	//private View		  featurePan2_buttons;
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView value;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private TextView state_key_view;
	//private Graphical_Info_View canvas;
	private int update;
	private Animation animation;
	private final Context context;
	private Message msg;
	private String wname;
	private String mytag="";
	private String url = null;
	public FrameLayout container = null;
	public static FrameLayout myself = null;
	public Boolean with_list = true;
	private tracerengine Tracer = null;
	private String parameters;
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private String[] known_values;
	private ArrayList<HashMap<String,String>> listItem;
	private Vector<String> list_usable_choices;
	private ListView listeChoices;
	
	
		
	@SuppressLint("HandlerLeak")
	public Graphical_List(tracerengine Trac,Activity context, int id,int dev_id, String name, 
			final String state_key, String url,String usage, int period, int update, 
			int widgetSize, int session_type, final String parameters) {
		super(context);
		this.Tracer = Trac;
		this.context = context;
		this.dev_id = dev_id;
		this.id = id;
		this.state_key = state_key;
		this.update=update;
		this.wname = name;
		this.url = url;
		Graphical_List.myself = this;
		this.session_type = session_type;
		this.parameters = parameters;
		mytag="Graphical_List ("+dev_id+")";
		this.setPadding(5, 5, 5, 5);
		Tracer.e(mytag,"New instance for name = "+wname+" state_key = "+state_key);
		//panel with border
		background = new LinearLayout(context);
		background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)
			background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//panel with border
		topPan = new LinearLayout(context);
		topPan.setOrientation(LinearLayout.HORIZONTAL);
		topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 5, 10);
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		Tracer.e(mytag, "Get icone for usage : "+usage);
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		img.setOnTouchListener(this);


		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);
		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);
		nameDevices.setOnLongClickListener(this);
		//nameDevices.setLines(1);

		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(state_key);
		state_key_view.setTextColor(Color.parseColor("#333333"));


		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding(10, 0, 10, 0);
		//value
		value = new TextView(context);
		value.setTextSize(28);
		value.setTextColor(Color.BLACK);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);

		
		if(with_list) {
			//Exploit parameters
			JSONObject jparam = null;
			String command;
			JSONArray commandValues = null;
			try {
				jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
				command = jparam.getString("command");
				commandValues = jparam.getJSONArray("commandValues");
			} catch (Exception e) {
				command = "";
				commandValues = null;
			}
			int nbitems = 0;
			String list = "";
			if(commandValues != null) {
				nbitems = commandValues.length();
				if(nbitems > 0) {
					if(known_values != null)
						known_values = null;
					
					known_values = new String[nbitems];
					for(int i=0; i < nbitems; i++) {
						try {
							known_values[i] = commandValues.getString(i);
						} catch (Exception e) {
							known_values[i] = "???";
						}
						list+=known_values[i];
						list+=", ";
						
					}
				}
					
			}
			Tracer.e(mytag, "command = <"+command+"> Values list = <"+list+">");
			

			//list of choices
			
			listeChoices = new ListView(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			
			listeChoices.setLayoutParams(lp);
			
			listItem=new ArrayList<HashMap<String,String>>();
			list_usable_choices = new Vector<String>();
			for (int i=0;i<known_values.length;i++) {
				list_usable_choices.add(ValueCode(known_values[i]));
				HashMap<String,String> map=new HashMap<String,String>();
					map.put("choice",ValueCode( known_values[i]));
					map.put("position",String.valueOf(i));
					listItem.add(map);
				
			}
			

			SimpleAdapter adapter_map=new SimpleAdapter(getContext(),listItem,
					R.layout.item_choice,new String[] {"choice"},new int[] {R.id.choice});
			listeChoices.setAdapter(adapter_map);
			listeChoices.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Tracer.d(mytag,"command selected at Position = "+position);
					if((position < listItem.size()) && (position > -1) ) {
						//TODO Process command
					}
				}
			});

			
			//feature panel 2 which will contain list of selectable choices
			featurePan2=new LinearLayout(context);
			featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			featurePan2.setGravity(Gravity.CENTER_VERTICAL);
			featurePan2.setPadding(5, 10, 5, 10);
			featurePan2.addView(listeChoices);
			
		}
		featurePan.addView(value);
		infoPan.addView(nameDevices);
		//infoPan.addView(state_key_view);
		imgPan.addView(img);

		topPan.addView(imgPan);
		topPan.addView(infoPan);
		topPan.addView(featurePan);
		background.addView(topPan);
		this.addView(background);
		
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
						} catch (JSONException e) {							
						if(state_key.equalsIgnoreCase("temperature") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("pressure") == true) value.setText(formatedValue+" hPa");
						else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("visibility") == true) value.setText(formatedValue+" km");
						else if(state_key.equalsIgnoreCase("chill") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("speed") == true) value.setText(formatedValue+" km/h");
						else if(state_key.equalsIgnoreCase("drewpoint") == true) value.setText(formatedValue+" °C");
						//else if(state_key.equalsIgnoreCase("condition-code") == true) value.setText(ConditionCode(Integer.parseInt(msg.getData().getString("message"))));
						else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("percent") == true) value.setText(formatedValue+" %");
						else value.setText(ValueCode(loc_Value));
						}
						value.setAnimation(animation);
					} catch (Exception e) {
						// It's probably a String that could'nt be converted to a float
						Tracer.d(mytag,"Handler exception : new value <"+loc_Value+"> not numeric !" );
						value.setText(loc_Value);
						
					}
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(background);
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
			if(tracerengine.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
										//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}
			
		}
		//================================================================================
		//updateTimer();	//Don't use anymore cyclic refresh....	

	}

	public String ValueCode(String ValueType){
		String result = ValueType;
		
		if(ValueType.equals("HVACstop"))
			result = context.getResources().getText(R.string.HVACstop).toString();
		else if (ValueType.equals("HVACnofreeze"))
			result = context.getResources().getText(R.string.HVACnofreeze).toString();
		else if (ValueType.equals("HVACeco"))
			result = context.getResources().getText(R.string.HVACeco).toString();
		else if (ValueType.equals("HVACnormal"))
			result = context.getResources().getText(R.string.HVACnormal).toString();
	
		Tracer.e(mytag,"Converting <"+ValueType+"> to <"+result+">");
		return result;
	}

	public boolean onTouch(View arg0, MotionEvent arg1) {
		if(with_list) {
			if(background.getHeight() != 350){
				try {
					background.removeView(featurePan2);
					
				} catch (Exception e) {}
				
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,350));
				background.addView(featurePan2);
				
			}
			else{
				background.removeView(featurePan2);
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		return false;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			
		}
	}
	
	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}
	public boolean onLongClick(View arg0) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setTitle(R.string.Rename_title);
		alert.setMessage(R.string.Rename_message);
		// Set an EditText view to get user input 
		final EditText input = new EditText(getContext());
			alert.setView(input);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.e("Graphical_info", "Description set to: "+result);
					
					tracerengine.get_engine().descUpdate(id, result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e("Graphical_info", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}
}



