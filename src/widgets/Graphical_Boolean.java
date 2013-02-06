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

import database.DomodroidDB;
import org.json.JSONException;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;
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
import misc.Tracer;
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
	private ImageView bool;
	private int dev_id;
	private Handler handler;
	private String state_key;
	private int update;
	public boolean activate=false;
	private DomodroidDB domodb;
	private Message msg;
	private String wname;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	

	public Graphical_Boolean(Activity context, String address, String name, int dev_id, String state_key, final String usage, String model_id, int update, int widgetSize) throws JSONException {
		super(context);
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.update = update;
		this.wname = name;
		this.myself=this;
		this.activate=false;
		this.setPadding(5, 5, 5, 5);
		
		domodb = new DomodroidDB(context);
		domodb.owner="Graphical_Boolean("+dev_id+")";
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


		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);

		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name); //debug option
		//nameDevices.setText(name+" ("+dev_id+")");
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(16);
		nameDevices.setOnLongClickListener(this);
		//state
		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setText("State : Low");


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
				if(activate) {
					Tracer.d("Graphical_Boolean","Handler receives a request to die " );
					//That seems to be a zombie
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { finalize(); } catch (Throwable t) {}	//kill the handler thread itself
				} else {
					String status = msg.getData().getString("message");
					if(status != null)   {
						try {
							if(status.equals("low")){
								bool.setImageResource(R.drawable.boolean_off);
								//change color if statue=low to (usage, o) means off
								//note sure if it must be kept as set previously as default color.
								img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
								state.setText("State : Low");
							}else if(status.equals("high")){
								bool.setImageResource(R.drawable.boolean_on);
								//change color if statue=high to (usage, 2) means on
								img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
								state.setText("State : High");
							}
						} catch (Exception e) {
							Tracer.e("Graphical_Boolean", "handler error device "+wname);
							e.printStackTrace();
						}
					}
				}
			}	
		};
		updateTimer();	


	}
	public void updateTimer() {
		TimerTask doAsynchronousTask;
		final Timer timer = new Timer();
		
		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				Runnable myTH = new Runnable() {
					public void run() {
					try {
							if(getWindowVisibility()==0 ){
								//Tracer.e("Graphical_Boolean ("+dev_id+")", "Execute UpdateThread");
								new UpdateThread().execute();
								
							}else{
								if(timer != null) {
									timer.cancel();
								}
								//Tracer.e("Graphical_Boolean ("+dev_id+")", "Destroy runnable");
								//this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} // Runnable run method
				}; //Runnable 
				Tracer.e("Graphical_Boolean ("+dev_id+")","Queuing Runnable for Device : "+dev_id);	
				try {
					handler.post(myTH);	//Doume : to avoid exception on ICS
					} catch (Exception e) {
						e.printStackTrace();
					}
			} // TimerTask run method
		}; //TimerTask 
		Tracer.e("Graphical_Boolean ("+dev_id+")","Init timer for Device ");	
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Bundle b = new Bundle();
			String state = domodb.requestFeatureState(dev_id, state_key);
			if(state != null) {
				activate=false;
				b.putString("message", state);
			    msg = new Message();
			    msg.setData(b);
			    handler.sendMessage(msg);
			} else {
				// This widget has no feature_state : probably a zombie ????
				activate=true;
				handler.sendEmptyMessage(0);
				
			}
			return null;
			
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			activate=true;
		}
	}
	public boolean onLongClick(View arg0) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setTitle(R.string.Rename_title);
		alert.setMessage(R.string.Rename_message);
		// Set an EditText view to get user input 
		final EditText input = new EditText(getContext());
			alert.setView(input);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.e("Graphical_Boolean", "Name set to: "+result);
					domodb.updateFeaturename(dev_id,result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Tracer.e("Graphical_Boolean", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}	
}



