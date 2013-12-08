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

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;

import database.WidgetUpdate;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Boolean extends FrameLayout implements OnLongClickListener{


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView state;
	private String value0;
	private String value1;
	private String Value_0;
	private String Value_1;
	private ImageView bool;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private int update;
	private String mytag;
	private Message msg;
	private String wname;
	private String stateS = "";
	
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private tracerengine Tracer = null;
	
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;

	@SuppressLint("HandlerLeak")
	public Graphical_Boolean(tracerengine Trac, Activity context, 
			String address, String name, 
			int id,int dev_id, 
			String state_key, final String usage,
			String parameters, 
			String model_id, int update, 
			int widgetSize,
			int session_type) throws JSONException {
		super(context);
		this.Tracer = Trac;
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.id = id;
		this.update = update;
		this.wname = name;
		this.myself=this;
		this.session_type = session_type;
		this.setPadding(5, 5, 5, 5);
		this.stateS = getResources().getText(R.string.State).toString();

		try {
			JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
			value0 = jparam.getString("value0");
			value1 = jparam.getString("value1");
		} catch (Exception e) {
			value0 = "0";
			value1 = "1";
		}		
		
		if (usage.equals("light")){
			this.Value_0 =  getResources().getText(R.string.light_stat_0).toString();
			this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
		}else if (usage.equals("shutter")){
			this.Value_0 =  getResources().getText(R.string.shutter_stat_0).toString();
			this.Value_1 =  getResources().getText(R.string.shutter_stat_1).toString();
		}else{
			this.Value_0 = value0;
			this.Value_1 = value1;		
		}
		
		mytag="Graphical_Boolean("+dev_id+")";
		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 5, 10);
		
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		//set default color to (usage,0) off.png
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
		img.setTag("img");
		img.setOnLongClickListener(this);

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
		nameDevices.setTag("namedevices");
		nameDevices.setOnLongClickListener(this);
		
		//state
		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setText("State :"+this.Value_0);


		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding(0, 0, 20, 0);

		//boolean on/off
		bool = new ImageView(context);
		bool.setImageResource(R.drawable.boolean_off);

		featurePan.addView(bool);
		infoPan.addView(nameDevices);
		infoPan.addView(state);
		imgPan.addView(img);
		background.addView(imgPan);
		background.addView(infoPan);
		background.addView(featurePan);

		this.addView(background);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {	
				String status;
				if(msg.what == 9999) {
					if(session == null)
						return;
					status = session.getValue();
					if(status != null)   {
						Tracer.d(mytag,"Handler receives a new status <"+status+">" );
						
						try {
							if(status.equals(value0)){
								bool.setImageResource(R.drawable.boolean_off);
								//change color if statue=low to (usage, o) means off
								//note sure if it must be kept as set previously as default color.
								img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
								state.setText(stateS+Value_0);
							}else if(status.equals(value1)){
								bool.setImageResource(R.drawable.boolean_on);
								//change color if statue=high to (usage, 2) means on
								img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
								state.setText(stateS+Value_1);
							}
						} catch (Exception e) {
							Tracer.e(mytag, "handler error device "+wname);
							e.printStackTrace();
						}
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
	
	public boolean onLongClick(View v) {
		if(v.getTag().equals("namedevices")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id,result);
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e("Graphical_Binary_New", "Customname Canceled.");
					}
				});
				alert.show();
		}else if (v.getTag().equals("img")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature(id);
					Tracer.get_engine().remove_one_feature_association(id);
					Tracer.get_engine().remove_one_feature_in_FeatureMap(id);}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e("Graphical_Binary_New", "delete Canceled.");
				}
			});
			alert.show();
		}
		return false;
		
	}
}



