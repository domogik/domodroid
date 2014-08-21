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

import rinor.Rest_com;
import widgets.Graphical_Binary.SBAnim;
import database.JSONParser;
import database.WidgetUpdate;

import org.json.JSONException;
import org.json.JSONObject;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



public class Graphical_Range extends FrameLayout implements SeekBar.OnSeekBarChangeListener, OnLongClickListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private FrameLayout rightPan;
	private LinearLayout bodyPanHorizontal;
	private LinearLayout infoPan;
	private LinearLayout leftPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView state;
	private SeekBar seekBarVaria;
	private String address;
	private int state_progress;
	private String usage;
	private String url;
	private final int dev_id;
	private int id;
	private Handler handler;
	private final String state_key;
	private int range;
	private int scale;
	private int valueMin;
	private int valueMax;
	private String type; 
	private String command;
	public static int stateThread; 
	private int update;
	public boolean activate=false;
	private Animation animation;
	private boolean touching;
	private int updating=0;
	private final String wname;
	private String place_type;
	private int place_id;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private String mytag;
	private tracerengine Tracer = null;
	private Message msg;
	
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private String stateS = "";
	private String login;
	private String password;
	private SharedPreferences params;
	
	public Graphical_Range(tracerengine Trac, Activity context, String address, String name,int id,int dev_id,
			String state_key, String url, String usage, 
			String parameters, String model_id, int update, 
			int widgetSize, int session_type,int place_id,String place_type, SharedPreferences params) throws JSONException {
		super(context);
		this.Tracer = Trac;
		this.address = address;
		this.url = url;
		this.dev_id=dev_id;
		this.id=id;
		this.state_key=state_key;
		this.usage = usage;
		this.update = update;
		this.wname = name;
		this.myself=this;
		this.session_type = session_type;
		this.place_id= place_id;
		this.place_type= place_type;
		stateThread = 1;
		this.stateS = getResources().getText(R.string.State).toString();
		mytag="Graphical_Range("+dev_id+")";
		this.params=params;
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		//get parameters
		JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
		command = jparam.getString("command");
		valueMin = jparam.getInt("valueMin");
		valueMax = jparam.getInt("valueMax");
		range = valueMax-valueMin;
		scale = 100/range;

		String[] model = model_id.split("\\.");
		type = model[0];

		this.setPadding(5, 5, 5, 5);

		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//linearlayout horizontal body		
		bodyPanHorizontal=new LinearLayout(context);
		bodyPanHorizontal.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.CENTER_VERTICAL));
		bodyPanHorizontal.setOrientation(LinearLayout.HORIZONTAL);

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 10, 10);

		//img
		img = new ImageView(context);
		img.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
		img.setTag("img");
		img.setOnLongClickListener(this);

		//right panel with different info and seekbars		
		rightPan=new FrameLayout(context);
		rightPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		rightPan.setPadding(0, 0, 10, 0);

		// panel
		leftPan = new LinearLayout(context);
		leftPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.BOTTOM));
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setGravity(Gravity.CENTER_VERTICAL);
		leftPan.setPadding(4, 5, 0, 0);

		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		infoPan.setOrientation(LinearLayout.HORIZONTAL);
		infoPan.setPadding(4, 5, 0, 0);


		//name
		nameDevices=new TextView(context);
		nameDevices.setText(name);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);
		nameDevices.setTag("namedevices");
		nameDevices.setOnLongClickListener(this);
		
		state=new TextView(context);
		state.setTextColor(Color.BLACK);
		state.setPadding(20, 0, 0, 0);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);

		//first seekbar variator
		seekBarVaria=new SeekBar(context);
		seekBarVaria.setProgress(0);
		seekBarVaria.setMax(range);
		seekBarVaria.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL));
		seekBarVaria.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbarvaria));
		seekBarVaria.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
		seekBarVaria.setThumbOffset(-3);
		seekBarVaria.setOnSeekBarChangeListener(this);
		seekBarVaria.setPadding(0, 0, 15, 7);

		infoPan.addView(nameDevices);
		infoPan.addView(state);
		leftPan.addView(infoPan);
		leftPan.addView(seekBarVaria);
		rightPan.addView(leftPan);

		imgPan.addView(img);
		bodyPanHorizontal.addView(imgPan);
		bodyPanHorizontal.addView(rightPan);

		background.addView(bodyPanHorizontal);

		this.addView(background);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/// Deprecated method to die /////////////////////////////////////////
				if(activate) {
					Tracer.d("Graphical_Range","Handler receives a request to die " );
					//That seems to be a zombie
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { finalize(); } catch (Throwable t) {}	//kill the handler thread itself
					///////////////////////////////////////////////////////////////////
				} else {
					int new_val = 0;
					if(msg.what == 9999) {
						//state_engine send us a signal to notify value changed
						if(session == null)
							return;
						try {
							Tracer.d("Graphical_Range","Handler receives a new value from cache_engine <"+session.getValue()+">" );
							new_val = Integer.parseInt(session.getValue());
						} catch (Exception e) {
							new_val = 0;
						}
						//TODO #1649
						//Two problem:
						//Missing unit
						//Value min and max should be the limit of the widget
						if(new_val==valueMin) {
							state.setText(stateS+"0 %");
						}else if(new_val>valueMin && new_val<valueMax){
							state.setText(stateS+(int)(new_val*(100f/(float)valueMax))+" %");
						}else if(new_val==valueMax){
							state.setText(stateS+"100 %");
						}
						state.setAnimation(animation);
						new SBAnim(seekBarVaria.getProgress(),new_val).execute();
						
					} else if(msg.what == 9998) {
						// state_engine send us a signal to notify it'll die !
						Tracer.d("Graphical_Range","state engine disappeared ===> Harakiri !" );
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


	public void onProgressChanged(SeekBar seekBar,int progress,boolean fromTouch) {
		//TODO #1649
		//Two problem:
		//Missing unit
		//Value min and max should be the limit of the widget
		if(progress==valueMin){
			state.setText("State : "+0+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));
		}else if(progress>valueMin && progress<valueMax){
			state.setText("State : "+(int)(progress*(100f/(float)valueMax))+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 1));
		}else if(progress==valueMax){
			state.setText("State : "+100+"%");
			img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		}
	}



	public void onStartTrackingTouch(SeekBar arg0) {
		touching=true;
		updating=3;
	}


	public void onStopTrackingTouch(SeekBar arg0) {
		state_progress = arg0.getProgress();
		new CommandeThread().execute();
		touching=false;
	}
	
	
	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			updating=3;
			JSONObject json_Ack = null;
			try {
				json_Ack = Rest_com.connect(url+"command/"+type+"/"+address+"/"+command+"/"+state_progress,login,password);
			} catch (Exception e) {
				Tracer.e(mytag, "Rest exception getting state : <"+e.getMessage()+">");
			}
			try {
				@SuppressWarnings("unused")
				Boolean ack = JSONParser.Ack(json_Ack);
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
							this.wait(7*scale);
							if(!touching){
								if(end-begin>0)seekBarVaria.setProgress(begin+i);
								else seekBarVaria.setProgress(begin-i);
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
						Tracer.get_engine().descUpdate(id,result,"feature");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}else if (v.getTag().equals("img")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					//TODO do this in a menu
					//Tracer.get_engine().remove_one_feature_association(id);
					//Tracer.get_engine().remove_one_feature(id);
					//Tracer.get_engine().remove_one_feature_in_FeatureMap(id);
					removeAllViewsInLayout ();	
					postInvalidate();
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}
		return false;
		
	}
}