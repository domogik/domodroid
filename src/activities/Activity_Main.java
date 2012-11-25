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
package activities;

import org.domogik.domodroid.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.json.JSONException;

import widgets.Graphical_Feature;

import database.WidgetUpdate;

import activities.Sliding_Drawer.OnPanelListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Activity_Main extends Activity implements OnPanelListener,OnClickListener,OnSeekBarChangeListener{

	
	@SuppressWarnings("unused")
	private Sliding_Drawer topPanel;
	private Sliding_Drawer panel;
	protected PowerManager.WakeLock mWakeLock;
	private SharedPreferences params;
	private SharedPreferences.Editor prefEditor;
	private Animation animation1;
	private Animation animation2;
	private TextView menu_white;
	private TextView menu_green;
	private TextView menu_about;
	private AlertDialog.Builder notSyncAlert;
	private Widgets_Manager wAgent;
	private Dialog_Synchronize dialog_sync;
	private WidgetUpdate widgetUpdate;
	private Handler sbanim;
	private static Handler widgetHandler;
	private Intent mapI = null;
	private Button sync;
	private Button Exit;	//Added by Doume
	private EditText localIP;
	private CheckBox checkbox3;
	private CheckBox checkbox4;
	private ImageView appname;
	private String format_urlAccess;
	public static String urlAccess;
	private TextView mProgressText1;
	private TextView mProgressText2;
	private TextView mProgressText3;
	private SeekBar mSeekBar1;
	private SeekBar mSeekBar2;
	private SeekBar mSeekBar3;
	private int dayOffset = 1;
	private int secondeOffset = 15;
	private int sizeOffset = 300;
	private ViewGroup parent;
	private LinearLayout ll_area;
	private LinearLayout ll_room;
	private LinearLayout ll_activ;
	private Vector<String[]> history;
	private int historyPosition;
	private LinearLayout house_map;
	private String tempUrl;
	private Boolean reload = false;
	private Boolean init_done = false;
	private File backupprefs = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/settings");
	private Boolean dont_freeze = false;
	private AlertDialog.Builder dialog_reload;
	
	private Thread waiting_thread = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//sharedPref
		params = getSharedPreferences("PREFS",MODE_PRIVATE);
		prefEditor=params.edit();

		//option
		localIP = (EditText)findViewById(R.id.localIP);	
		checkbox3 = (CheckBox)findViewById(R.id.checkbox3);
		checkbox4 = (CheckBox)findViewById(R.id.checkbox4);
		appname = (ImageView)findViewById(R.id.app_name);
		mProgressText1 = (TextView)findViewById(R.id.progress1);
		mProgressText2 = (TextView)findViewById(R.id.progress2);
		mProgressText3 = (TextView)findViewById(R.id.progress3);
		mSeekBar1=(SeekBar)findViewById(R.id.SeekBar1);
		mSeekBar2=(SeekBar)findViewById(R.id.SeekBar2);
		mSeekBar3=(SeekBar)findViewById(R.id.SeekBar3);
		sync=(Button)findViewById(R.id.sync);
		sync.setOnClickListener(this);
		sync.setTag("sync");
		//Added by Doume
		Exit=(Button)findViewById(R.id.Stop_all);
		Exit.setOnClickListener(this);
		Exit.setTag("Exit");
		//
		mSeekBar1.setOnSeekBarChangeListener(this);
		mSeekBar2.setOnSeekBarChangeListener(this);
		mSeekBar3.setOnSeekBarChangeListener(this);

		LoadSelections();
		
		
		//update thread
		sbanim = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
				}else if(msg.what==1){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
				}else if(msg.what==2){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
				}else if(msg.what==3){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
				}
			}	
		};

		
		//power management
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "");
		this.mWakeLock.acquire();

		//titlebar
		final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));


		//menu button
		menu_white = (TextView) findViewById(R.id.menu_button1);
		menu_white.setOnClickListener(this);
		menu_white.setTag("menu");
		menu_green = (TextView) findViewById(R.id.menu_button2);
		menu_green.setVisibility(View.GONE);
		menu_about = (TextView) findViewById(R.id.About_button);
		menu_about.setOnClickListener(this);
		menu_about.setTag("about");
		
		animation1 = new AlphaAnimation(0.0f, 1.0f);
		animation1.setDuration(500);
		animation2 = new AlphaAnimation(1.0f, 0.0f);
		animation2.setDuration(500);

		//Parent view
		parent = (ViewGroup) findViewById(R.id.home_container);

		//sliding drawer
		topPanel = panel = (Sliding_Drawer) findViewById(R.id.topPanel);
		panel.setOnPanelListener(this);
		//panel.setPadding(0, 45, 0, 0);


		house_map = new LinearLayout(this);
		house_map.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		house_map.setOrientation(LinearLayout.HORIZONTAL);
		house_map.setPadding(5, 5, 5, 5);

		Graphical_Feature house = new Graphical_Feature(getApplicationContext(),0,"House","","house",0);
		house.setPadding(0, 0, 5, 0);
		Graphical_Feature map = new Graphical_Feature(getApplicationContext(),0,"Map","","map",0);
		map.setPadding(5, 0, 0, 0);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);

		house.setLayoutParams(param);
		house.setOnClickListener(this);
		house.setTag("house");

		map.setLayoutParams(param);
		map.setOnClickListener(this);
		map.setTag("map");

		house_map.addView(house);
		house_map.addView(map);
		init_done = false;
		
		// Detect if it's the 1st use after installation...
			if(!params.getBoolean("SPLASH", false)){
				// Yes, 1st use !
				init_done = false;
				reload = false;
				if(backupprefs.exists()) {
					// A backup exists : Ask if reload it
					Log.e("Activity_Main","settings backup found after a fresh install...");
					
					DialogInterface.OnClickListener reload_listener = new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Log.e("Activity_Home","Reload dialog returns : "+which);
							if(which == dialog.BUTTON_POSITIVE) {
								reload = true;
							}
							else if(which == dialog.BUTTON_NEGATIVE) {
								reload = false;
							}
							check_answer();
							dialog.dismiss();						
						}
					};
					dialog_reload = new AlertDialog.Builder(this);
					dialog_reload.setMessage(getText(R.string.home_reload));
					dialog_reload.setTitle(getText(R.string.reload_title));
					dialog_reload.setPositiveButton(getText(R.string.reloadOK), reload_listener);
					dialog_reload.setNegativeButton(getText(R.string.reloadNO), reload_listener);
					dialog_reload.show();
					init_done=false;	//A choice is pending : Rest of init has to be completed...
				} else {
					//No settings backup found
					Log.e("Activity_Main","no settings backup found after fresh install...");
					end_of_init();
				}
			} else {
				// It's not the 1st use after fresh install
				Log.e("Activity_Main","First init already done...");
				end_of_init();
			}
			
			// End of onCreate (UIThread)
	}
	
	private void check_answer() {
		Log.e("Activity_Main","reload choice done..");
		if(reload) {
			// If answer is 'yes', load preferences from backup
			Log.e("Activity_Home","reload settings..");
			loadSharedPreferencesFromFile(backupprefs);
			panel.setOpen(false, false);
			dialog_sync = new Dialog_Synchronize(this);
			dialog_sync.reload = this.reload;	//To notify if settings reloaded : don't recreate database !
			dialog_sync.setParams(params);
			dialog_sync.show();
			dialog_sync.startSync();
		} else {
			Log.e("Activity_Main","Settings not reloaded : clear database..");
			File database = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/domodroid.db");
			if(database.exists()) {
				database.delete();
			}
		}

		if(! init_done)
			// Complete the UI init
			end_of_init();
	}
	private void createAlert() {
		notSyncAlert = new AlertDialog.Builder(this);
		notSyncAlert.setMessage(getText(R.string.not_sync)).setTitle("Warning!");
		notSyncAlert.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();						
			}
		});

	}
	private void end_of_init() {
		// Finalize screen appearence
		Log.e("Activity_Main","Finalize onCreate()..");
		
		if(! reload) {
			//alertDialog not syncsplash
			if(notSyncAlert == null)
				createAlert();
		}
		//splash
		if(!params.getBoolean("SPLASH", false)){
			Dialog_Splash dialog_splash = new Dialog_Splash(this);
			dialog_splash.show();
			prefEditor.clear();
			prefEditor.putBoolean("SPLASH", true);
			prefEditor.commit();
		}
		Log.e("Activity_Home", "Starting/restarting WidgetUpdate engine !");
		if(widgetUpdate != null) {
			widgetUpdate.cancelEngine();
			widgetUpdate = null;
		}
		widgetUpdate = new WidgetUpdate(this,sbanim,params);
		
		if(history != null)
			history = null;		//Free resource
		history = new Vector<String[]>();
		
		//load widgets
		Log.e("Activity_Main", "Starting WidgetHandler thread !");
		widgetHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					historyPosition++;
					loadWigets(msg.getData().getInt("id"), msg.getData().getString("type"));
					Log.e("Activity_Main.widgetHandler", "add history "+msg.getData().getInt("id")+" "+msg.getData().getString("type"));
					history.add(historyPosition,new String [] {msg.getData().getInt("id")+"",msg.getData().getString("type")});
				} catch (Exception e) {
					Log.e("Activity_Main.widgetHandler", "handler error into loadWidgets");
					e.printStackTrace();
				}
			}	
		};
		Log.e("Activity_Main", "Starting/restarting wAgent !");
		
		wAgent=new Widgets_Manager(widgetHandler);
		loadWigets(0,"root");
		historyPosition=0;
		history.add(historyPosition,new String [] {"0","root"});
		
		init_done = true;
	}
	
	private boolean saveSharedPreferencesToFile(File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        SharedPreferences pref = 
	                            getSharedPreferences("PREFS", MODE_PRIVATE);
	        output.writeObject(pref.getAll());

	        res = true;
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (output != null) {
	                output.flush();
	                output.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}

	@SuppressWarnings({ "unchecked" })
	private boolean loadSharedPreferencesFromFile(File src) {
	    boolean res = false;
	    ObjectInputStream input = null;
	    try {
	        input = new ObjectInputStream(new FileInputStream(src));
	            Editor prefEdit = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
	            prefEdit.clear();
	            Map<String, ?> entries = (Map<String, ?>) input.readObject();
	            for (Entry<String, ?> entry : entries.entrySet()) {
	                Object v = entry.getValue();
	                String key = entry.getKey();
	                Log.e("Activity_Main","Loading pref : "+key+" -> "+v.toString());
	                if (v instanceof Boolean)
	                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
	                else if (v instanceof Float)
	                    prefEdit.putFloat(key, ((Float) v).floatValue());
	                else if (v instanceof Integer)
	                    prefEdit.putInt(key, ((Integer) v).intValue());
	                else if (v instanceof Long)
	                    prefEdit.putLong(key, ((Long) v).longValue());
	                else if (v instanceof String)
	                    prefEdit.putString(key, ((String) v));
	            }
	            prefEdit.commit();
	            this.LoadSelections();	// to set panel with known values
	        res = true;         
	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }finally {
	        try {
	            if (input != null) {
	                input.close();
	            }
	        } catch (IOException ex) {
	            ex.printStackTrace();
	        }
	    }
	    return res;
	}
	
	public void loadWigets(int id, String type){
		parent.removeAllViews();
		ll_area = new LinearLayout(this);
		ll_area.setOrientation(LinearLayout.VERTICAL);
		ll_room = new LinearLayout(this);
		ll_room.setOrientation(LinearLayout.VERTICAL);
		ll_activ = new LinearLayout(this);
		ll_activ.setOrientation(LinearLayout.VERTICAL);

		try {
			if(type.equals("root")){
				parent.addView(house_map);
				ll_area = wAgent.loadAreaWidgets(this, ll_area, params);
			}
			if(type.equals("root") || type.equals("area"))ll_room = wAgent.loadRoomWidgets(this, id, ll_room, params);
			if(type.equals("area") || type.equals("room") || type.equals("house"))ll_activ = wAgent.loadActivWidgets(this, id, type, ll_activ,params);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		parent.addView(ll_area);
		parent.addView(ll_room);
		parent.addView(ll_activ);
	}

	/**home_reload
	 * save selections preference
	 */
	private void SaveSelections() {
		try{
			SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
			SharedPreferences.Editor prefEditor=params.edit();
			prefEditor.putString("IP1",localIP.getText().toString());
			prefEditor.putBoolean("DRAG", checkbox3.isChecked());
			prefEditor.putBoolean("ZOOM", checkbox4.isChecked());
			int period = mSeekBar1.getProgress();
			if(period < secondeOffset)
				period = secondeOffset;
			prefEditor.putInt("UPDATE_TIMER", period);
			prefEditor.putInt("GRAPH", mSeekBar2.getProgress()+dayOffset);
			prefEditor.putInt("SIZE", mSeekBar3.getProgress()+sizeOffset);

			urlAccess = localIP.getText().toString();

			if(urlAccess.lastIndexOf("/")==localIP.getText().toString().length()-1) format_urlAccess = urlAccess;
			else format_urlAccess = urlAccess.concat("/");

			prefEditor.putString("URL",format_urlAccess);
			prefEditor.commit();
			if(backupprefs != null)
				saveSharedPreferencesToFile(backupprefs);	// Store settings to SDcard
			
			if(!tempUrl.equals(localIP.getText().toString())){
				dialog_sync = new Dialog_Synchronize(this);
				dialog_sync.setParams(params);
				dialog_sync.show();
				dialog_sync.startSync();
			}
		}catch(Exception e){}
	}

	private void LoadSelections() {
		SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
		localIP.setText(params.getString("IP1",null));
		tempUrl=params.getString("IP1",null);
		checkbox3.setChecked(params.getBoolean("DRAG",false));
		checkbox4.setChecked(params.getBoolean("ZOOM",false));
		mSeekBar1.setProgress(params.getInt("UPDATE_TIMER", 300)-secondeOffset);
		mSeekBar2.setProgress(params.getInt("GRAPH", 3)-dayOffset);
		mSeekBar3.setProgress(params.getInt("SIZE", 800)-sizeOffset);
	}



	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		if(seekBar.getId()==R.id.SeekBar1) {
			int	value = progress;
			if(value < secondeOffset) {
				value = secondeOffset;
				seekBar.setProgress(value);
			}
			mProgressText1.setText((value)+" Secondes");
		}
		else if(seekBar.getId()==R.id.SeekBar2) {
			mProgressText2.setText( (Integer.toString(progress+dayOffset))+ getText(R.string.network_Text11a));
		}else{
			mProgressText3.setText((progress+sizeOffset)+" px");
		}
	}


	public void onStartTrackingTouch(SeekBar seekBar) {
	}


	public void onStopTrackingTouch(SeekBar seekBar) {
	}


	public void onPanelClosed(Sliding_Drawer panel) {
		Log.e("Activity_Main","onPanelClosed");
		menu_green.startAnimation(animation2);
		menu_green.setVisibility(View.GONE);
		SaveSelections();
		if(widgetUpdate == null)
			widgetUpdate = new WidgetUpdate(this,sbanim,params);

	}
	public void onPanelOpened(Sliding_Drawer panel) {
		Log.e("Activity_Main","onPanelOpened");
		menu_green.setVisibility(View.VISIBLE);
		menu_green.startAnimation(animation1);
		//widgetUpdate.stopThread();
	}

	public void onClick(View v) {
		dont_freeze = false;		// By default, onPause() will stop WidgetUpdate engine...
		
		if(v.getTag().equals("sync")) {
			panel.setOpen(false, false);
			dialog_sync = new Dialog_Synchronize(this);
			dialog_sync.reload = this.reload;	//To notify if settings reloaded : don't recreate database !
			dialog_sync.setParams(params);
			dialog_sync.show();
			dialog_sync.startSync();
		}	else if(v.getTag().equals("Exit")) {
			//Disconnect all opened sessions....
			Log.e("Activity_Main Exit","Stopping WidgetUpdate thread !");
			this.wAgent=null;
			widgetHandler=null;
			widgetUpdate.cancelEngine();
			widgetUpdate=null;
			//And stop main program
			this.finish();
			return;
		} else if(v.getTag().equals("reload_cancel")) {
			Log.e("Activity_Main","Choosing no reload settings");
			reload = false;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
			return;
		}  else if(v.getTag().equals("reload_ok")) {
			Log.e("Activity_Main","Choosing settings reload");
			reload=true;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
		}
		else if(v.getTag().equals("about")) {
			dont_freeze=true;		//To avoid WidgetUpdate engine freeze
			Intent helpI = new Intent(Activity_Main.this,Activity_About.class);
			startActivity(helpI);
		}
		if(v.getTag().equals("house")) {

			if(params.getBoolean("SYNC", false)==true){
				loadWigets(0, "house");
				historyPosition++;
				history.add(historyPosition,new String [] {"0","house"});
			}else{
				if(notSyncAlert == null)
					createAlert();
				notSyncAlert.show();
			}
		}
		else if(v.getTag().equals("map")) {
			if(params.getBoolean("SYNC", false)==true){
				//dont_freeze=true;		//To avoid WidgetUpdate engine freeze
				mapI = new Intent(Activity_Main.this,Activity_Map.class);
				startActivity(mapI);
			}else{
				if(notSyncAlert == null)
					createAlert();
				notSyncAlert.show();
			}
		}
		else if(v.getTag().equals("menu")) {
			
			if(!panel.isOpen()){
				panel.setOpen(true, true);	//open with animation
			}else{
				panel.setOpen(false, true);	//hide with animation
			}
		}
	}
	
	

	@Override
	public void onPause(){
		super.onPause();
		panel.setOpen(false, false);
		Log.e("Activity_Main.onPause","Going to background !");
		if(! dont_freeze) {
			Log.e("Activity_Main.onPause","Freeze own WidgetUpdate engine");
			if(widgetUpdate != null) {
				widgetUpdate.stopThread();
			}
		} else {
			//Another Activity started : keep WidgetUpdate engine running
			Log.e("Activity_Main.onPause","Keep own WidgetUpdate engine running");
			
		}
		dont_freeze = false;	
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.mWakeLock.release();	// We allow screen shut, now...
		Log.e("Activity_Main.onDestroy","Stopping WidgetUpdate thread !");
		if(widgetUpdate != null) {
			widgetUpdate.stopThread();	// Normally, Exit choice has already stopped this background engine
			widgetUpdate=null;
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		Log.e("Activity_Main.onResume","Try to reactivate  WidgetUpdate thread !");
		if(widgetUpdate == null)
			widgetUpdate = new WidgetUpdate(this,sbanim,params);
		else
			widgetUpdate.restartThread();	// re-allow timers inside WidgetUpdate background engine
		
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==82 && !panel.isOpen()){
			panel.setOpen(true, true);
			return false;
		}else if((keyCode==82 || keyCode == 4) && panel.isOpen()){
			panel.setOpen(false, true);
			return false;
		}else if(keyCode == 4 && !panel.isOpen() && historyPosition > 0){
			historyPosition--;
			loadWigets(Integer.parseInt(history.elementAt(historyPosition)[0]),history.elementAt(historyPosition)[1]);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class SBAnim extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			new Thread(new Runnable() {
				public synchronized void run() {
					try {
						appname.setBackgroundResource(R.drawable.app_name2);
						this.wait(100);
						appname.setBackgroundResource(R.drawable.app_name3);
						this.wait(100);
						appname.setBackgroundResource(R.drawable.app_name1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			return null;
		}
	}

}

