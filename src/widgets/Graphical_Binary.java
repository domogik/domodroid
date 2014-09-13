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
import database.WidgetUpdate;

import org.json.JSONException;
import org.json.JSONObject;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class Graphical_Binary extends FrameLayout implements OnSeekBarChangeListener, OnLongClickListener{


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
	private int id;
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
	private Message msg;
	private String name;
	private String wname;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private String mytag = "";
	private tracerengine Tracer = null;
	private Activity context = null;
	private String stateS = "";
	private String Value_0 = "0";
	private String Value_1 = "1";
	private String place_type;
	private int place_id;
	private SharedPreferences params;
	private String login;
	private String password;
	
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	
	
	public Graphical_Binary(tracerengine Trac, 
			Activity context, String address, String name, int id,int dev_id,String state_key, String url, String usage, 
			String parameters, String model_id, int update, int widgetSize, int session_type,int place_id,String place_type, SharedPreferences params) throws JSONException {
		super(context);
		this.Tracer = Trac;
		this.context = context;
		this.address = address;
		this.url = url;
		this.state_key = state_key;
		this.dev_id = dev_id;
		this.id = id;
		this.usage = usage;
		this.update = update;
		this.name = name;
		this.setPadding(5, 5, 5, 5);
		this.wname = name;
		this.myself = this;
		this.session_type = session_type;
		this.stateS = getResources().getText(R.string.State).toString();
		this.place_id= place_id;
		this.place_type= place_type;
		this.params = params;
		setOnLongClickListener(this);
		
		mytag = "Graphical_Binary("+dev_id+")";
		//get parameters		
		
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
		
		String[] model = model_id.split("\\.");
		type = model[0];
		Tracer.d(mytag,"model_id = <"+model_id+"> type = <"+type+"> value0 = "+value0+"  value1 = "+value1 );
		
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
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(activate) {
					Tracer.d(mytag,"Handler receives a request to die " );
					if(realtime) {
						Tracer.get_engine().unsubscribe(session);
						session = null;
						realtime = false;
					}
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
						Bundle b = msg.getData();
						if(( b != null) && (b.getString("message") != null)) {
							if (b.getString("message").equals(value0)){
								//state.setText(stateS+value0);
								state.setText(stateS+Value_0);
								new SBAnim(seekBarOnOff.getProgress(),0).execute();
							}else if(b.getString("message").equals(value1)){
								//state.setText(stateS+value1);
								state.setText(stateS+Value_1);
								new SBAnim(seekBarOnOff.getProgress(),40).execute();
							}
							state.setAnimation(animation);
						} else {
							if(msg.what == 2) {
								Toast.makeText(getContext(), "Command Failed", Toast.LENGTH_SHORT).show();
							} else if(msg.what == 9999) {
								//state_engine send us a signal to notify value changed
								if(session == null)
									return;
								String new_val = session.getValue();
								Tracer.d(mytag,"Handler receives a new value <"+new_val+">" );
								if(new_val.equals(value0)) {
									state.setText(stateS+Value_0);
									new SBAnim(seekBarOnOff.getProgress(),0).execute();
								}else if(new_val.equals(value1)){
									state.setText(stateS+Value_1);
									new SBAnim(seekBarOnOff.getProgress(),40).execute();
								} else {
									state.setText(stateS+new_val);
									new SBAnim(seekBarOnOff.getProgress(),0).execute();
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
						
					} catch (Exception e) {
						Tracer.e(mytag, "Handler error for device "+wname);
						e.printStackTrace();
					}
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
		//updateTimer();	//Don't use anymore cyclic refresh....	

	}


	public void onProgressChanged(SeekBar seekBarOnOff,int progress,boolean fromTouch) {
		switch(progress) {
		case 0:
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
			//state.setText(stateS +value0);
			state.setText(stateS+Value_0);
			break;
		case 40:
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
			//state.setText(stateS + value1);
			state.setText(stateS+Value_1);
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
	
	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			updating=3;
			String Url2send = url+"command/"+type+"/"+address+"/"+state_progress;
			Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
			JSONObject json_Ack = null;
			try {
				json_Ack = Rest_com.connect(Url2send,login,password);
			} catch (Exception e) {
				Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
			}
			try {
				Boolean ack = JSONParser.Ack(json_Ack);
				if(ack==false){
					Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
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
			//activate=true;
		}
	}
	public boolean onLongClick(View v) {
		final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
		List<String> list_choice = new ArrayList<String>();
			list_choice.add("Rename");
			list_choice.add("Change_icon");
			list_choice.add("Delete");
		final CharSequence[] char_list =list_choice.toArray(new String[list_choice.size()]);
		//list_type_choice.setTitle(R.string.What_to_do_message);
		list_type_choice.setSingleChoiceItems(char_list, -1,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					ListView lw = ((AlertDialog)dialog).getListView();
					Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
					do_action(checkedItem.toString());
					dialog.cancel();
				}
			}
		);
	
		list_type_choice.show();
		return false;
	}

	private void do_action(String action) {
		if(action.equals("Rename")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id,result,"feature");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}else if (action.equals("Delete")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}else if (action.equals("Change_icon")){
			final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
			List<String> list_icon = new ArrayList<String>();
			String[] fiilliste;
			fiilliste = context.getResources().getStringArray(R.array.icon_area_array); 
			for (int i=0; i < fiilliste.length ; i++){
				list_icon.add(fiilliste[i].toString());
			}
			final CharSequence[] char_list_icon =list_icon.toArray(new String[list_icon.size()]);
			list_icon_choice.setTitle(R.string.Wich_ICON_message);
			list_icon_choice.setSingleChoiceItems(char_list_icon, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						usage = checkedItem.toString();
						ContentValues values = new ContentValues();
						//type = area, room, feature
						values.put("name", "feature");
						//icon is the name of the icon wich will be select 
						values.put("value", usage);
						//reference is the id of the area, room, or feature
						int reference = 0;
						reference=id;
						values.put("reference", reference);
						context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
						dialog.cancel();
					}
				}
			);	
			AlertDialog alert_list_icon = list_icon_choice.create();
			alert_list_icon.show();
			
		}		
	}
}





