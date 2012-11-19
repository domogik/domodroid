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
package org.widgets;

import java.util.Timer;
import java.util.TimerTask;

import org.database.DomodroidDB;
import org.json.JSONException;
import org.panel.Gradients_Manager;
import org.panel.Graphics_Manager;
import org.panel.R;
import org.widgets.Graphical_Info.UpdateThread;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Graphical_Boolean extends FrameLayout{


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


	public Graphical_Boolean(Activity context, String address, String name, int dev_id, String state_key, String usage, String model_id, int update, int widgetSize) throws JSONException {
		super(context);
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.update = update;
		this.wname = name;

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
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));


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
		nameDevices.setTextSize(16);
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
				String status = msg.getData().getString("message");
				if(status != null)   {
					try {
						if(status.equals("low")){
							bool.setImageResource(R.drawable.boolean_off);
							state.setText("State : Low");
						}else if(status.equals("high")){
							bool.setImageResource(R.drawable.boolean_on);
							state.setText("State : High");
						}
					} catch (Exception e) {
						Log.e("Graphical_Boolean", "handler error device "+wname);
						e.printStackTrace();
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
							if(getWindowVisibility()==0 || !activate){
								Log.e("Graphical_Boolean ("+dev_id+")", "Execute UpdateThread");
								new UpdateThread().execute();
								
							}else{
								if(timer != null) {
									timer.cancel();
								}
								Log.e("Graphical_Boolean ("+dev_id+")", "Destroy runnable");
								this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} // Runnable run method
				}; //Runnable 
				Log.e("Graphical_Boolean ("+dev_id+")","Queuing Runnable for Device : "+dev_id);	
				try {
					handler.post(myTH);	//Doume : to avoid exception on ICS
					} catch (Exception e) {
						e.printStackTrace();
					}
			} // TimerTask run method
		}; //TimerTask 
		Log.e("Graphical_Boolean ("+dev_id+")","Init timer for Device ");	
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Bundle b = new Bundle();
			b.putString("message", domodb.requestFeatureState(dev_id, state_key));
			msg = new Message();
			msg.setData(b);
			handler.sendMessage(msg);
			return null;
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			activate=true;
		}
	}
}



