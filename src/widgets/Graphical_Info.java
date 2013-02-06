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
import java.util.Timer;
import java.util.TimerTask;

import database.DomodroidDB;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import misc.Tracer;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Info extends FrameLayout implements OnTouchListener, OnLongClickListener {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout featurePan2;
	private View		  featurePan2_buttons;
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView value;
	private int dev_id;
	private Handler handler;
	private String state_key;
	private TextView state_key_view;
	private Graphical_Info_View canvas;
	private int update;
	private Animation animation;
	public boolean activate=false;	// When true, stop all background activities....
	private DomodroidDB domodb;
	private Message msg;
	private String wname;
	private String mytag="";
	private String url = null;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	public Boolean with_graph = true;
	
		
	public Graphical_Info(Activity context, int dev_id, String name, final String state_key, String url,String usage, int period, int update, int widgetSize) {
		super(context);
		this.dev_id = dev_id;
		this.state_key = state_key;
		this.update=update;
		this.wname = name;
		this.url = url;
		this.myself = this;
		this.activate=false;
		mytag="Graphical_Info ("+dev_id+")";
		this.setPadding(5, 5, 5, 5);
		Tracer.e(mytag,"New instance for name = "+wname+" state_key = "+state_key);
		domodb = new DomodroidDB(context);
		domodb.owner="Graphical_Info("+dev_id+")";
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
		Tracer.e("Graphical_Info Frame", "Get icone for usage : "+usage);
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		img.setOnTouchListener(this);


		// info panel
		infoPan = new LinearLayout(context);final int TID = 0;
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);
		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name); //debug option
		//nameDevices.setText(name+" ("+dev_id+")");
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

		if(with_graph) {
			//feature panel 2 which will contain graphic
			featurePan2=new LinearLayout(context);
			featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
			featurePan2.setGravity(Gravity.CENTER_VERTICAL);
			featurePan2.setPadding(5, 10, 5, 10);
			//canvas
			canvas = new Graphical_Info_View(context);
			canvas.dev_id = dev_id;
			canvas.state_key = state_key;
			canvas.url = url;
			//canvas.period = period;
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
		featurePan.addView(value);
		infoPan.addView(nameDevices);
		infoPan.addView(state_key_view);
		imgPan.addView(img);

		topPan.addView(imgPan);
		topPan.addView(infoPan);
		topPan.addView(featurePan);
		background.addView(topPan);
		this.addView(background);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(activate) {
					Tracer.d(mytag,"Handler receives a request to die " );
					//That seems to be a zombie
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { finalize(); } catch (Throwable t) {}	//kill the handler thread itself
				} else {
					try {
						float formatedValue = 0;
						String loc_Value = msg.getData().getString("message");
						if(loc_Value != null)
							formatedValue = Round(Float.parseFloat(msg.getData().getString("message")),2);
						
						if(state_key.equalsIgnoreCase("temperature") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("pressure") == true) value.setText(formatedValue+" hPa");
						else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("visibility") == true) value.setText(formatedValue+" km");
						else if(state_key.equalsIgnoreCase("chill") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("speed") == true) value.setText(formatedValue+" km/h");
						else if(state_key.equalsIgnoreCase("drewpoint") == true) value.setText(formatedValue+" °C");
						else if(state_key.equalsIgnoreCase("condition-code") == true) value.setText(ConditionCode(Integer.parseInt(msg.getData().getString("message"))));
						else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
						else if(state_key.equalsIgnoreCase("percent") == true) value.setText(formatedValue+" %");
						else value.setText(msg.getData().getString("message"));
						Tracer.e(mytag, "UIThread handler : Value "+Float.toString(formatedValue) +" refreshed for device "+state_key+" "+wname);
						value.setAnimation(animation);
					} catch (Exception e) {
						// It's probably a String that could'nt be converted to a float
						value.setText(msg.getData().getString("message"));
						//Tracer.e(mytag, "handler error device "+wname);
						//e.printStackTrace();
					}
				}
			}
		};
		updateTimer();

	}

	public static int ConditionCode(int code){
		switch (code){
		case 0: return R.string.info0;
		case 1: return R.string.info1;
		case 2: return R.string.info2;
		case 3: return R.string.info3;
		case 4: return R.string.info4;
		case 5: return R.string.info5;
		case 6: return R.string.info6;
		case 7: return R.string.info7;
		case 8: return R.string.info8;
		case 9: return R.string.info9;
		case 10: return R.string.info10;
		case 11: return R.string.info11;
		case 12: return R.string.info12;
		case 13: return R.string.info13;
		case 14: return R.string.info14;
		case 15: return R.string.info15;
		case 16: return R.string.info16;
		case 17: return R.string.info17;
		case 18: return R.string.info18;
		case 19: return R.string.info19;
		case 20: return R.string.info20;
		case 21: return R.string.info21;
		case 22: return R.string.info22;
		case 23: return R.string.info23;
		case 24: return R.string.info24;
		case 25: return R.string.info25;
		case 26: return R.string.info26;
		case 27: return R.string.info27;
		case 28: return R.string.info28;
		case 29: return R.string.info29;
		case 30: return R.string.info30;
		case 31: return R.string.info31;
		case 32: return R.string.info32;
		case 33: return R.string.info33;
		case 34: return R.string.info34;
		case 35: return R.string.info35;
		case 36: return R.string.info36;
		case 37: return R.string.info37;
		case 38: return R.string.info38;
		case 39: return R.string.info39;
		case 40: return R.string.info40;
		case 41: return R.string.info41;
		case 42: return R.string.info42;
		case 43: return R.string.info43;
		case 44: return R.string.info44;
		case 45: return R.string.info45;
		case 46: return R.string.info46;
		case 47: return R.string.info47;
		case 3200: return R.string.info3200;
		}
		return R.string.info48;
	}

	public void updateTimer() {
		TimerTask doAsynchronousTask;
		final Timer timer = new Timer();
		
		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				//Tracer.e(mytag, "TimerTask.run : Create Runnable");
				Runnable myTH = new Runnable() {
					public void run() {
					try {
							if(getWindowVisibility()== 0){
								//Tracer.e(mytag, "update Timer : Execute UpdateThread");
								new UpdateThread().execute();
								
							}else{
								if(timer != null) {
									timer.cancel();
								}
								//Tracer.d(mytag, "update Timer : No UpdateThread started...");
								//this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} // Runnable run method
				}; //Runnable 
				//Tracer.e(mytag,"TimerTask.run : Queuing Runnable");	
				try {
					handler.post(myTH);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} // TimerTask run method
		}; //TimerTask 
		Tracer.e(mytag,"Init timer for Device : "+this.dev_id);	
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			//Tracer.e(mytag, "UpdateThread : Prepare a request for "+dev_id+ " "+state_key+" "+wname);
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
				//activate=true;
				handler.sendEmptyMessage(0);
				
			}
			
			return null;
		}
	}
	
	public boolean onTouch(View arg0, MotionEvent arg1) {
		if(with_graph) {
			if(background.getHeight() != 350){
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,350));
				background.addView(featurePan2_buttons);
				background.addView(featurePan2);
				canvas.activate = true;
				canvas.updateTimer();
			}
			else{
				background.removeView(featurePan2_buttons);
				background.removeView(featurePan2);
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				canvas.activate = false;	//notify Graphical_Info_View to stop its UpdateTimer
			}
		}
		return false;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			activate=true;
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
				public void onClick(DialogInterface dialog, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.e("Graphical_info", "Name set to: "+result);
					domodb.updateFeaturename(dev_id,result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Tracer.e("Graphical_info", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}
}



