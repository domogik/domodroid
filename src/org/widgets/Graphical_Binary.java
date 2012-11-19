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

import org.connect.Rest_com;
import org.database.DomodroidDB;
import org.database.JSONParser;
import org.json.JSONException;
import org.json.JSONObject;
import org.panel.Gradients_Manager;
import org.panel.Graphics_Manager;
import org.panel.R;
import org.widgets.Graphical_Info.UpdateThread;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Binary extends FrameLayout implements OnSeekBarChangeListener{


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView state;
	private SeekBar seekBarOnOff;
	private String address;
	private String state_progress;
	private String url;
	private String usage;
	private int dev_id;
	private Handler handler;
	private String state_key;
	private String value0;
	private String value1;
	private String type;
	private int update;
	public boolean activate=false;
	private Animation animation;
	private boolean touching;
	private int updating=0;
	private DomodroidDB domodb;
	private Message msg;
	private String name;
	private String wname;


	public Graphical_Binary(Activity context, String address, String name, int dev_id,String state_key, String url, String usage, String parameters, String model_id, int update, int widgetSize) throws JSONException {
		super(context);
		this.address = address;
		this.url = url;
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.usage = usage;
		this.update = update;
		this.name = name;
		this.setPadding(5, 5, 5, 5);
		this.wname = name;

		domodb = new DomodroidDB(context);
		domodb.owner="Graphical_Binary("+dev_id+")";
		//get parameters
		JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
		value0 = jparam.getString("value0");
		value1 = jparam.getString("value1");

		String[] model = model_id.split("\\.");
		type = model[0];

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
		nameDevices.setTextSize(14);

		//state
		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);


		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding(10, 0, 10, 0);

		//first seekbar on/off
		seekBarOnOff=new SeekBar(context);
		seekBarOnOff.setProgress(0);
		seekBarOnOff.setMax(40);
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bgseekbaronoff);
		seekBarOnOff.setLayoutParams(new LayoutParams(bMap.getWidth(),bMap.getHeight()));
		seekBarOnOff.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbaronoff));
		seekBarOnOff.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
		seekBarOnOff.setThumbOffset(0);
		seekBarOnOff.setOnSeekBarChangeListener(this);
		seekBarOnOff.setTag("0");

		featurePan.addView(seekBarOnOff);
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
				try {
					if(msg.getData().getString("message").equals(value0)){
						state.setText("State : "+value0);
						new SBAnim(seekBarOnOff.getProgress(),0).execute();
					}else if(msg.getData().getString("message").equals(value1)){
						state.setText("State : "+value1);
						new SBAnim(seekBarOnOff.getProgress(),40).execute();
					}else{
						Toast.makeText(getContext(), "Command Failed", Toast.LENGTH_SHORT).show();
					}
					state.setAnimation(animation);
				} catch (Exception e) {
					Log.e("handler error", "device "+wname);
					e.printStackTrace();
				}
			}	
		};
		updateTimer();	

	}


	public void onProgressChanged(SeekBar seekBarOnOff,int progress,boolean fromTouch) {
		switch(progress) {
		case 0:
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
			state.setText("State : "+value0);
			break;
		case 40:
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
			state.setText("State : "+value1);
			break;
		}
	}


	public void onStartTrackingTouch(SeekBar arg0) {
		touching=true;
		updating=3;
	}

	public void onStopTrackingTouch(SeekBar arg0) {
		if(arg0.getProgress()<20){
			state_progress = value0;
			arg0.setProgress(0);
		}else{
			state_progress = value1;
			arg0.setProgress(40);
		}
		new CommandeThread().execute();
		touching=false;
	}

	public void updateTimer() {
		TimerTask doAsynchronousTask;
		final Timer timer = new Timer();
		
		doAsynchronousTask = new TimerTask() {

			@SuppressWarnings("unused")
			@Override
			public void run() {
				Runnable myTH = null;
				Handler loc_handler = handler;
				Log.e("TimerTask.run", "Create Runnable");
				myTH = new Runnable() {
					public void run() {
						
					try {
							if(getWindowVisibility()==0 || !activate){
								Log.e("update Timer", "Execute UpdateThread");
								new UpdateThread().execute();
								
							}else{
								if(timer != null) {
									timer.cancel();
								}
								Log.e("update Timer", "Destroy runnable");
								this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} // Runnable run method
				}; //Runnable 
				if((myTH != null) && (loc_handler != null)) {
					Log.e("Grapical_Binary","TimerTask.run : Queuing Runnable for Device : "+dev_id);	
					try {
						loc_handler.post(myTH);
					} catch (Exception e) {
						Log.e("Grapical_Binary","TimerTask.run : Cannot post refresh for Device : "+dev_id+" Widget will not be refreshed ! ! !");	
						e.printStackTrace();
					}
				} else {
					Log.e("Grapical_Binary","TimerTask.run : Cannot create Runnable for Device : "+dev_id+" Widget will not be refreshed ! ! !");	
				}
			} // TimerTask run method
		}; //TimerTask 
		Log.e("updateTimer","Init timer for Device : "+this.dev_id);	
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try{
				Log.e("Graphical_Binary", "UpdateThread for device "+dev_id+" "+state_key+" state= "+name);
				if(updating<1){
					Bundle b = new Bundle();
					String result = domodb.requestFeatureState(dev_id, state_key);
					if(result != null) {
						Log.e("Graphical_Binary", "UpdateThread for device "+dev_id+" "+state_key+" state= "+name);
						b.putString("message", result);
						msg = new Message();
						msg.setData(b);
						handler.sendMessage(msg);
					} else {
						Log.e("Graphical_Binary", "UpdateThread no DB state for "+dev_id+" "+state_key+" state= "+name);
					}
				}
				updating--;
			}catch(Exception e){
				Log.e("Graphical_Binary", "error : request feature state= "+name);
			}
			return null;
		}
	}

	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			updating=3;
			JSONObject json_Ack = Rest_com.connect(url+"command/"+type+"/"+address+"/"+state_progress);
			try {
				Boolean ack = JSONParser.Ack(json_Ack);
				if(ack==false){
					handler.sendEmptyMessage(2);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public class SBAnim extends AsyncTask<Void, Integer, Void>{
		private int begin;
		private int end;

		public SBAnim(int begin, int end){
			this.begin=begin;
			this.end=end;
		}

		@Override
		protected Void doInBackground(Void... params) {
			final int steps = java.lang.Math.abs(end-begin);
			new Thread(new Runnable() {
				public synchronized void run() {
					for(int i=0;i<=steps;i++){
						try {
							this.wait(5);
							if(!touching){
								if(end-begin>0)seekBarOnOff.setProgress(begin+i);
								else seekBarOnOff.setProgress(begin-i);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
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





