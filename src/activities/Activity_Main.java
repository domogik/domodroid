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

import org.domogik.domodroid13.R;
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

import widgets.Com_Stats;
import widgets.Basic_Graphical_zone;
import misc.tracerengine;
import database.WidgetUpdate;

import activities.Sliding_Drawer.OnPanelListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings({ "static-access" })
public class Activity_Main extends Activity implements OnPanelListener,OnClickListener{

	
	@SuppressWarnings("unused")
	private Sliding_Drawer SD_topPanel;
	private Sliding_Drawer SD_panel;
	protected PowerManager.WakeLock PM_WakeLock;
	private SharedPreferences SP_params;
	private SharedPreferences.Editor SP_prefEditor;
	private Animation A_animation1;
	private Animation A_animation2;
	private TextView TV_menu_white;
	private TextView TV_menu_green;
	private TextView TV_menu_about;
	private AlertDialog.Builder AD_notSyncAlert;
	private Toast T_starting;
	private Widgets_Manager WM_Agent;
	private Dialog_Synchronize DIALOG_dialog_sync;
	private WidgetUpdate WU_widgetUpdate;
	private Handler sbanim;
	private static Handler widgetHandler;
	private Intent INTENT_map = null;
	private Button BUTTON_sync;
	private Button BUTTON_Exit;	//Added by Doume
	private Button BUTTON_usage_settings;	//Added by Tikismoke
	private Button BUTTON_server_settings;	//Added by Tikismoke
	private Button BUTTON_New_settings;	//Added by Tikismoke
	private Button BUTTON_map_settings;	//Added by Tikismoke
	private Button BUTTON_debug_settings;	//Added by Doume
	private Button BUTTON_house_settings;	//Added by Doume
	private Dialog_Usage DIALOG_usage_set = null;
	private Dialog_Server DIALOG_server_set = null;
	private Dialog_Map DIALOG_map_set = null;
	private Dialog_Debug DIALOG_debug_set = null;
	private Dialog_House DIALOG_house_set = null;
	private ImageView appname;
	
	private int dayOffset = 1;
	private int secondeOffset = 5;
	private ViewGroup VG_parent;
	private LinearLayout LL_area;
	private LinearLayout LL_room;
	private LinearLayout LL_activ;
	private Vector<String[]> history;
	private int historyPosition;
	private LinearLayout LL_house_map;
	private Basic_Graphical_zone house;
	private Basic_Graphical_zone map;
	private Basic_Graphical_zone stats;
	
	private String tempUrl;
	private Boolean reload = false;
	DialogInterface.OnClickListener reload_listener = null;
	DialogInterface.OnDismissListener sync_listener = null;
	private Boolean by_usage = false;
	private Boolean init_done = false;
	private File backupprefs = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/settings");
	private Boolean dont_freeze = false;
	private AlertDialog.Builder dialog_reload;
	private Thread waiting_thread = null;
	private Activity_Main myself = null;
	private tracerengine Tracer = null;
	private String tracer_state = "false";
	private Boolean dont_kill = false;		//Set by call to map, to avoid engines destruction
	private int mytype = 0;		// All objects will be 'Main" type
	//private AlertDialog.Builder AD_dialog_message;
	protected ProgressDialog PG_dialog_message;
	private Boolean cache_ready = false;
	private Boolean end_of_init_requested = true;
	private LinearLayout LL_info;
	private TextView TV_info_msg;
	private String mytag="Activity_Main";
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myself=this;
		setContentView(R.layout.activity_home);
		
		
		//sharedPref
		//SP_params = getSharedPreferences("PREFS",MODE_PRIVATE);
		//TODO use normal menu
		SP_params = PreferenceManager
                .getDefaultSharedPreferences(this);
		SP_prefEditor=SP_params.edit();
		Tracer = tracerengine.getInstance(SP_params);
		
		//Added by Doume
		File storage = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/");
		if(! storage.exists())
			storage.mkdirs();
		//Configure Tracer tool initial state
		File logpath = new File(Environment.getExternalStorageDirectory()+"/domodroid/.log/");
		if(! logpath.exists())
			logpath.mkdirs();
		
		String currlogpath = SP_params.getString("LOGNAME", "");
		if(currlogpath.equals("")) {
			//Not yet existing prefs : Configure debugging by default, to configure Tracer
			currlogpath=Environment.getExternalStorageDirectory()+"/domodroid/.log/";
			SP_prefEditor.putString("LOGPATH",currlogpath);
			SP_prefEditor.putString("LOGNAME","Domodroid.txt");
			SP_prefEditor.putBoolean("SYSTEMLOG", false);
			SP_prefEditor.putBoolean("TEXTLOG", false);
			SP_prefEditor.putBoolean("SCREENLOG", false);
			SP_prefEditor.putBoolean("LOGCHANGED", true);
			SP_prefEditor.putBoolean("LOGAPPEND", false);
		} else {
			SP_prefEditor.putBoolean("LOGCHANGED", true);		//To force Tracer to consider current settings
		}
		//prefEditor.putBoolean("SYSTEMLOG", false);		// For tests : no system logs....
		SP_prefEditor.putBoolean("SYSTEMLOG", true);		// For tests : with system logs....

		SP_prefEditor.commit();
		
		Tracer.set_profile(SP_params);
		// Create .nomedia file, that will prevent Android image gallery from showing domodroid file
		String nomedia = Environment.getExternalStorageDirectory()+"/domodroid/.nomedia";
			try {
				if (! (new File(nomedia).exists())) {
					new FileOutputStream(nomedia).close();
				}
			}
				catch(Exception e) {
			}
		//option
		appname = (ImageView)findViewById(R.id.app_name);
		
		BUTTON_Exit=(Button)findViewById(R.id.Stop_all);
		BUTTON_Exit.setOnClickListener(this);
		BUTTON_Exit.setTag("Exit");
		
		BUTTON_usage_settings=(Button)findViewById(R.id.bt_usage_settings);
		BUTTON_usage_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v(mytag+".onclick()","Call to usage settings screen");
				if(DIALOG_usage_set != null)
					DIALOG_usage_set.get_params();
				else
					DIALOG_usage_set = new Dialog_Usage(Tracer, SP_params, myself);
				DIALOG_usage_set.show();
				return;
			}
		});
		
		BUTTON_server_settings=(Button)findViewById(R.id.bt_server_settings);
		BUTTON_server_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v(mytag+".onclick()","Call to server settings screen");
				if(DIALOG_server_set != null)
					DIALOG_server_set.get_params();
				else
					DIALOG_server_set = new Dialog_Server(Tracer, SP_params, myself);
				DIALOG_server_set.show();
				return;
			}
		});
		
		BUTTON_map_settings=(Button)findViewById(R.id.bt_map_settings);
		BUTTON_map_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v(mytag+".onclick()","Call to Map settings screen");
				if(DIALOG_map_set != null)
					DIALOG_map_set.get_params();
				else
					DIALOG_map_set = new Dialog_Map(Tracer, SP_params, myself);
				DIALOG_map_set.show();
				return;
			}
		});
		
		BUTTON_debug_settings=(Button)findViewById(R.id.bt_debug_settings);
		BUTTON_debug_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v(mytag+".onclick()","Call to Debug settings screen");
				if(DIALOG_debug_set != null)
					DIALOG_debug_set.get_params();
				else
					DIALOG_debug_set = new Dialog_Debug(Tracer, SP_params, myself);
				DIALOG_debug_set.show();
				return;
			}
		});
		
		BUTTON_house_settings=(Button)findViewById(R.id.bt_house_settings);
		BUTTON_house_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v(mytag+".onclick()","Call to House settings screen");
				DIALOG_house_set = new Dialog_House(Tracer, SP_params, myself);
				DIALOG_house_set.show();
				return;
			}
		});
		BUTTON_New_settings=(Button)findViewById(R.id.New_settings);
		BUTTON_New_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// click on 'sync' button into Sliding_Drawer View
				SD_panel.setOpen(false, false);	// Hide the View
				//TODO prepare a normal menu call. 
				Intent helpI = new Intent(Activity_Main.this,Preference.class);
				startActivity(helpI);
			}
			
		});
		
		BUTTON_sync=(Button)findViewById(R.id.sync);
		BUTTON_sync.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// click on 'sync' button into Sliding_Drawer View
				SD_panel.setOpen(false, false);	// Hide the View
				run_sync_dialog();		// And run a resync with Rinor server
			}
			
		});
		
		LoadSelections();
		
		// Prepare a listener to know when a sync dialog is closed...
		if( sync_listener == null){
			sync_listener = new DialogInterface.OnDismissListener() {

				public void onDismiss(DialogInterface dialog) {
					
					Tracer.d(mytag,"sync dialog has been closed !");
					
					// Is it success or fail ?
					if(((Dialog_Synchronize)dialog).need_refresh) {
						// Sync has been successful : Force to refresh current main view
						Tracer.d(mytag,"sync dialog requires a refresh !");
						reload = true;	// Sync being done, consider shared prefs are OK
						VG_parent.removeAllViews();
						if(WU_widgetUpdate != null) {
							WU_widgetUpdate.resync();
						} else {
							Tracer.i(mytag+".onCreate","WidgetUpdate is null startCacheengine!");
							startCacheEngine();
						}
						Bundle b = new Bundle();
						//Notify sync complete to parent Dialog
						b.putInt("id", 0);
						b.putString("type", "root");
					    Message msg = new Message();
					    msg.setData(b);
					    if(widgetHandler != null)
							widgetHandler.sendMessage(msg); 	// That should force to refresh Views
						/* */
						if(WU_widgetUpdate != null) {
							WU_widgetUpdate.Disconnect(0);	//That should disconnect all opened widgets from cache engine
							//widgetUpdate.dump_cache();	//For debug
							dont_kill = true;	// to avoid engines kill when onDestroy()
						}
						onResume();
					} else {
						Tracer.d(mytag,"sync dialog end with no refresh !");
						
					}
					((Dialog_Synchronize)dialog).need_refresh = false;					
				}
			};
		}
		
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
				} else if(msg.what==8000){
					Tracer.e(mytag,"Request to display message : 8000");
					/*
					if(dialog_message == null) {
						Create_message_box();
					}
					dialog_message.setMessage("Starting cache engine...");
					dialog_message.show();
					
					*/
				} else if(msg.what==8999){
					//Cache engine is ready for use....
					if(Tracer != null)
						Tracer.e(mytag,"Cache engine has notified it's ready !");
					cache_ready=true;
					if(end_of_init_requested)
						end_of_init();
					PG_dialog_message.dismiss();
				}
			}	
		};

		
		//power management
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.PM_WakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "");
		this.PM_WakeLock.acquire();

		//titlebar
		final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));


		//menu button
		
		TV_menu_green = (TextView) findViewById(R.id.menu_button2);
		TV_menu_green.setVisibility(View.GONE);
		
		TV_menu_white = (TextView) findViewById(R.id.menu_button1);
		TV_menu_white.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// A clic on menu will activate/deactivate panel allowing settings configuration and giving
				//  access to 'sync' button
				if(!SD_panel.isOpen()){
					SD_panel.setOpen(true, true);	//open with animation
				}else{
					SD_panel.setOpen(false, true);	//hide with animation
				}
			}
		});
		TV_menu_white.setVisibility(View.VISIBLE);
		
		TV_menu_about = (TextView) findViewById(R.id.About_button);
		TV_menu_about.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				dont_freeze=true;		//To avoid WidgetUpdate engine freeze
				Intent helpI = new Intent(Activity_Main.this,Activity_About.class);
				//TODO prepare a normal menu call. 
				//Intent helpI = new Intent(Activity_Main.this,Preference.class);
				startActivity(helpI);				
			}
		});
		
		A_animation1 = new AlphaAnimation(0.0f, 1.0f);
		A_animation1.setDuration(500);
		A_animation2 = new AlphaAnimation(1.0f, 0.0f);
		A_animation2.setDuration(500);

		//Parent view
		VG_parent = (ViewGroup) findViewById(R.id.home_container);
		//sliding drawer
		SD_topPanel = SD_panel = (Sliding_Drawer) findViewById(R.id.topPanel);
		SD_panel.setOnPanelListener(this);
		//panel.setPadding(0, 45, 0, 0);

		
		LL_house_map = new LinearLayout(this);
		LL_house_map.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		LL_house_map.setOrientation(LinearLayout.HORIZONTAL);
		LL_house_map.setPadding(5, 5, 5, 5);
		
		house = new Basic_Graphical_zone(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "House"),
				"",
				"house",
				0,"",null);
		house.setPadding(0, 0, 5, 0);
		map = new Basic_Graphical_zone(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "Map"),
				"",
				"map",
				0,"",null);
		map.setPadding(5, 0, 0, 0);
		stats = new Basic_Graphical_zone(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "statistics"),
				"",
				"statistics",
				0,"",null);
		stats.setPadding(0, 0, 5, 0);
		
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);

		
		house.setLayoutParams(param);
		house.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(SP_params.getBoolean("SYNC", false)==true){
					loadWigets(0, "root");
					historyPosition++;
					history.add(historyPosition,new String [] {"0","house"});
				}else{
					if(AD_notSyncAlert == null)
						createAlert();
					AD_notSyncAlert.show();
				}		
			}
		});
		
		map.setLayoutParams(param);
		map.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(SD_panel.isOpen()){
					// Ignore map call if panel is opened, to avoid confusion between objects
					return;
				}
				if(SP_params.getBoolean("SYNC", false)==true){
					//dont_freeze=true;		//To avoid WidgetUpdate engine freeze
					Tracer.w(mytag,"Before call to Map, Disconnect widgets from engine !");
					if(WU_widgetUpdate != null) {
						WU_widgetUpdate.Disconnect(0);	//That should disconnect all opened widgets from cache engine
						//widgetUpdate.dump_cache();	//For debug
						dont_kill = true;	// to avoid engines kill when onDestroy()
					}
					INTENT_map = new Intent(Activity_Main.this,Activity_Map.class);
					Tracer.d(mytag,"Call to Map, run it now !");
					Tracer.Map_as_main = false;
					startActivity(INTENT_map);
				}else{
					if(AD_notSyncAlert == null)
						createAlert();
					AD_notSyncAlert.show();
				}
			}
		});
		
		stats.setLayoutParams(param);
		stats.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(SP_params.getBoolean("SYNC", false)==true){
					loadWigets(0, "statistics");
					historyPosition++;
					history.add(historyPosition,new String [] {"0","statistics"});
				}else{
					if(AD_notSyncAlert == null)
						createAlert();
					AD_notSyncAlert.show();
				}
			}
		});

		if(! by_usage)
			LL_house_map.addView(house);
		else
			LL_house_map.addView(stats);
		
		LL_house_map.addView(map);
		init_done = false;
		// Detect if it's the 1st use after installation...
			if(!SP_params.getBoolean("SPLASH", false)){
				// Yes, 1st use !
				init_done = false;
				reload = false;
				if(backupprefs.exists()) {
					// A backup exists : Ask if reload it
					Tracer.v(mytag,"settings backup found after a fresh install...");
					
					DialogInterface.OnClickListener reload_listener = new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Tracer.e(mytag,"Reload dialog returns : "+which);
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
					Tracer.v(mytag,"no settings backup found after fresh install...");
					end_of_init_requested = true;
					// open server config view
					BUTTON_server_settings.performClick();
				}
			} else {
				// It's not the 1st use after fresh install
				// This method will be followed by 'onResume()'
				end_of_init_requested = true;
			}
			if(SP_params.getBoolean("SYNC", false)){
				//A config exists and a sync as been done by past.
				if(WU_widgetUpdate == null) {
					Tracer.i(mytag+".onCreate","Params splach is false and WidgetUpdate is null startCacheengine!");
					startCacheEngine();
				}

			}
			
			Tracer.e(mytag,"OnCreate() complete !");
			// End of onCreate (UIThread)
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Tracer.e(mytag+".onResume","Check if initialize requested !");
		if(! init_done) {
			Tracer.i(mytag+".onResume","Init not done!");
			if(SP_params.getBoolean("SPLASH", false)){
				Tracer.i(mytag+".onResume","params Splash is false !");
				cache_ready = false;
				//try to solve 1rst launch and orientation problem
				Tracer.i(mytag+".onresume","Init not done! and params Splash is false startCacheengine!");
				//startCacheEngine();
				//end_of_init();		//Will be done when cache will be ready
			}
		}
		else {
			Tracer.i(mytag+".onResume","Init done!");
				end_of_init_requested=true;
				if(WU_widgetUpdate != null) {
					Tracer.i(mytag+".onResume","Widget update is not null so wakeup widget engine!");
					WU_widgetUpdate.wakeup();		//If cache ready, that'll execute end_of_init()
				}
				//end_of_init();	//all client widgets will be re-created
			}
		if(end_of_init_requested)
			end_of_init();
		}
	
	@Override
	public void onPause(){
		super.onPause();
		SD_panel.setOpen(false, false);
		Tracer.w(mytag+".onPause","Going to background !");
		if(WU_widgetUpdate != null)  {
			if(! Tracer.Map_as_main) {
				// We're the main initial activity
				WU_widgetUpdate.set_sleeping();	//Don't cancel the cache engine : only freeze it
			}
		}
			
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.WM_Agent=null;
		widgetHandler=null;
		if(WU_widgetUpdate != null) {
			WU_widgetUpdate.Disconnect(0);	//remove all pending subscribings
			if(! Tracer.Map_as_main) {
				// We're the main initial activity
				Tracer.w(mytag+".onDestroy","cache engine set to sleeping !");
				this.PM_WakeLock.release();	// We allow screen shut, now...
				WU_widgetUpdate.set_sleeping();	//Don't cancel the cache engine : only freeze it
												// only if we are the main initial activity
			}
			
		}
		/*
		if(Tracer != null) {
			Tracer.close();		//To flush text file, eventually
			Tracer = null;
		}
		*/
	}
	
	private void Create_message_box() {
		if(PG_dialog_message != null)
			return;
		PG_dialog_message = new ProgressDialog(this);
		PG_dialog_message.setMessage(getText(R.string.init_in_process));
		//dialog_reload.setPositiveButton("OK", message_listener);
		PG_dialog_message.setTitle(getText(R.string.please_wait));
	}
	
	public void force_DB_update() {
		if(WU_widgetUpdate != null) {
			WU_widgetUpdate.refreshNow();
		}
	}
	
	private void createAlert() {
		AD_notSyncAlert = new AlertDialog.Builder(this);
		AD_notSyncAlert.setMessage(getText(R.string.not_sync)).setTitle("Warning!");
		AD_notSyncAlert.setNeutralButton("OK", new DialogInterface.OnClickListener() {

	public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();						
			}
		});

	}
	
	private void end_of_init() {
		// Finalize screen appearence
		if(Tracer == null)
			Tracer = Tracer.getInstance();
		Tracer.v(mytag,"end_of_init Main Screen..");
		if(! reload) {
			//alertDialog not sync splash
			if(AD_notSyncAlert == null)
				createAlert();
			}
		//splash
		if(!SP_params.getBoolean("SPLASH", false)){
			Dialog_Splash dialog_splash = new Dialog_Splash(this);
			dialog_splash.show();
			SP_prefEditor.clear();
			SP_prefEditor.putBoolean("SPLASH", true);
			SP_prefEditor.commit();
			return;
		}
		end_of_init_requested = false;
		
		if(history != null)
			history = null;		//Free resource
		history = new Vector<String[]>();
		historyPosition = 0;
		
		//load widgets
		if(widgetHandler == null) {
			Tracer.i(mytag, "Starting WidgetHandler thread !");
			widgetHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					
					try {
						historyPosition++;
						loadWigets(msg.getData().getInt("id"), msg.getData().getString("type"));
						Tracer.v(mytag+".widgetHandler", "add history "+msg.getData().getInt("id")+" "+msg.getData().getString("type"));
						history.add(historyPosition,new String [] {msg.getData().getInt("id")+"",msg.getData().getString("type")});
					} catch (Exception e) {
						Tracer.e(mytag+".widgetHandler", "handler error into loadWidgets");
						e.printStackTrace();
					}
				}	
			};
		}
		if(WM_Agent == null) {
			Tracer.v(mytag, "Starting wAgent !");
			WM_Agent=new Widgets_Manager(Tracer, widgetHandler);
			WM_Agent.widgetupdate = WU_widgetUpdate;
		}
		if(T_starting != null) {
			T_starting.cancel();
			T_starting.setText("Creating widgets....");
			T_starting.setDuration(Toast.LENGTH_SHORT);
			T_starting.show();
		}
		init_done = true;
		
		if((SP_params.getBoolean("START_ON_MAP", false) && ( ! Tracer.force_Main) ) ) {
			Tracer.e(mytag, "Direct start on Map requested...");
			Tracer.Map_as_main = true;		//Memorize that Map is now the main screen
			INTENT_map = new Intent(Activity_Main.this,Activity_Map.class);
			startActivity(INTENT_map);
		} else {
			Tracer.force_Main = false;	//Reset flag 'called from Map view'
			loadWigets(0,"root");
			historyPosition=0;
			history.add(historyPosition,new String [] {"0","root"});
		}
		init_done = true;
		dont_kill = false;	//By default, the onDestroy activity will also kill engines
	}
	
	/*
	 * Check the answer after the proposal to reload existing settings (fresh install)
	 */
	private void check_answer() {
		Tracer.v(mytag,"reload choice done..");
		if(reload) {
			// If answer is 'yes', load preferences from backup
			Tracer.e(mytag,"reload settings..");
			loadSharedPreferencesFromFile(backupprefs);
			SD_panel.setOpen(false, false);
			run_sync_dialog();
			
		} else {
			Tracer.v(mytag,"Settings not reloaded : clear database..");
			File database = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/domodroid.db");
			if(database.exists()) {
				database.delete();
			}
			// open server config view
			BUTTON_server_settings.performClick();
		}

		if(! init_done) {
			// Complete the UI init
			end_of_init();
		}
	}
	
	private boolean saveSharedPreferencesToFile(File dst) {
	    boolean res = false;
	    ObjectOutputStream output = null;
	    try {
	        output = new ObjectOutputStream(new FileOutputStream(dst));
	        //SharedPreferences pref = getSharedPreferences("PREFS", MODE_PRIVATE);
	        //TODO add normal menu
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this); 
			
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
	            //Editor prefEdit = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
	            //TODO add normal menu
	        	Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(this).edit();
	            prefEdit.clear();
	            Map<String, ?> entries = (Map<String, ?>) input.readObject();
	            for (Entry<String, ?> entry : entries.entrySet()) {
	                Object v = entry.getValue();
	                String key = entry.getKey();
	                Tracer.v(mytag,"Loading pref : "+key+" -> "+v.toString());
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
		Tracer.d(mytag+".loadWidgets","Construct main View id="+id+" type="+type);
		VG_parent.removeAllViews();
		LL_area = new LinearLayout(this);
		LL_area.setOrientation(LinearLayout.VERTICAL);
		LL_room = new LinearLayout(this);
		LL_room.setOrientation(LinearLayout.VERTICAL);
		LL_activ = new LinearLayout(this);
		LL_activ.setOrientation(LinearLayout.VERTICAL);
		
		LL_house_map.removeAllViews();
		if( ! by_usage) {
			LL_house_map.addView(house);
			LL_house_map.addView(map);
		} else {
			LL_house_map.addView(stats);
			LL_house_map.addView(map);
		}
		try {
			if(type.equals("root")){
				LL_area.removeAllViews();
				if(! by_usage) {
					// Version 0.2 or un-force by_usage : display house, map and areas
					VG_parent.addView(LL_house_map);	// House & map
					LL_area = WM_Agent.loadAreaWidgets(this, LL_area, SP_params);
					VG_parent.addView(LL_area);	//and areas
					LL_activ.removeAllViews();
					LL_activ = WM_Agent.loadActivWidgets(this, 1, "root", LL_activ,SP_params, mytype);//add widgets in root
					VG_parent.addView(LL_activ);
				} else {
					// by_usage
					VG_parent.addView(LL_house_map);	// With only map
					//TODO #2021 change 1 in loadRoomWidgets by the right value.
					LL_room = WM_Agent.loadRoomWidgets(this, 1, LL_room, SP_params);	//List of known usages 'as rooms'
					VG_parent.addView(LL_room);
					LL_activ.removeAllViews();
					LL_activ = WM_Agent.loadActivWidgets(this, 1, "area", LL_activ,SP_params, mytype);//add widgets in area 1
					VG_parent.addView(LL_activ);
				}
			/*Should never arrive in this type.	
			}else if(type.equals("house")) {
				//Only possible if Version 0.2 or un-force by_usage (the 'house' is never proposed to be clicked)
				LL_area.removeAllViews();
				VG_parent.addView(LL_house_map);	// House & map
				LL_area = WM_Agent.loadAreaWidgets(this, LL_area, SP_params);
				VG_parent.addView(LL_area);	//and areas
				LL_activ.removeAllViews();
				LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ,SP_params, mytype);
				VG_parent.addView(LL_activ);
			*/	
			}else if(type.equals("statistics")) {
				//Only possible if by_usage (the 'stats' is never proposed with Version 0.2 or un-force by_usage)
				LL_area.removeAllViews();
				LL_activ.removeAllViews();
				LL_activ = WM_Agent.loadActivWidgets(this, -1, type, LL_activ ,SP_params, mytype);
				VG_parent.addView(LL_activ);
				
			} else 	if(type.equals("area")) {
				//Only possible if Version 0.2 or un-force by_usage (the area 'usage' is never proposed to be clicked)
				if(! by_usage){
					VG_parent.addView(LL_house_map);	// House & map
				}
				LL_room.removeAllViews();
				LL_room = WM_Agent.loadRoomWidgets(this, id, LL_room, SP_params);//Add room in this area
				VG_parent.addView(LL_room);
				LL_activ.removeAllViews();
				LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ,SP_params, mytype);//add widgets in this area
				VG_parent.addView(LL_activ);
				
			} else 	if(type.equals("room")) {
				LL_activ.removeAllViews();
				LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ,SP_params, mytype);//add widgets in this room
				VG_parent.addView(LL_activ);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void SaveSelections(Boolean mode) {
		try{
			//SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
			//TODO add normal menu
			SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this); 
			SharedPreferences.Editor prefEditor=params.edit();
			prefEditor.commit();
			if(backupprefs != null)
				saveSharedPreferencesToFile(backupprefs);	// Store settings to SDcard
			/*
			if(! mode)
				run_sync_dialog();	// force a resync with server if just after a reload
			*/
		} catch(Exception e){}
	}
	
	private void LoadSelections() {
		//SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
		//TODO add normal menu
		SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this); 
		tempUrl=params.getString("IP1",null);
		by_usage = params.getBoolean("BY_USAGE", false);
	}
	
	private void run_sync_dialog() {
		//change sync parameter in case it fail.
		SP_prefEditor.putBoolean("SYNC", false);
		SP_prefEditor.commit();
		if (!(WU_widgetUpdate == null)) {
			WU_widgetUpdate.Disconnect(0);	//Disconnect all widgets owned by Main
		}
		if(DIALOG_dialog_sync == null)
			DIALOG_dialog_sync = new Dialog_Synchronize(Tracer, this,SP_params);
		DIALOG_dialog_sync.reload = reload;
		DIALOG_dialog_sync.setOnDismissListener(sync_listener);
		DIALOG_dialog_sync.setParams(SP_params);
		DIALOG_dialog_sync.show();
		DIALOG_dialog_sync.startSync();
	}
	
	public void onPanelClosed(Sliding_Drawer panel) {
		Tracer.w(mytag,"onPanelClosed");
		TV_menu_green.startAnimation(A_animation2);
		TV_menu_green.setVisibility(View.GONE);
		SaveSelections(false);		// To force a sync operation, if something has been modified...
		/*
		if(widgetUpdate == null) {
			startDBEngine();
		}
		*/
	}
	
	public void onPanelOpened(Sliding_Drawer panel) {
		Tracer.v(mytag,"onPanelOpened");
		TV_menu_green.setVisibility(View.VISIBLE);
		TV_menu_green.startAnimation(A_animation1);
	}

	public void onClick(View v) {
		dont_freeze = false;		// By default, onPause() will stop WidgetUpdate engine...
		//ALL other that are not explicitly used
		if(v.getTag().equals("Exit")) {
			//Disconnect all opened sessions....
			Tracer.v(mytag+"Exit","Stopping WidgetUpdate thread !");
			this.WM_Agent=null;
			widgetHandler=null;
			Tracer.set_engine(null);
			if (!(WU_widgetUpdate == null)) {
				WU_widgetUpdate.Disconnect(0);	//Disconnect all widgets owned by Main
			}
			dont_kill = false;		//To force OnDestroy() to also kill engines
			//And stop main program
			this.finish();
			return;
		} else if(v.getTag().equals("reload_cancel")) {
			Tracer.v(mytag,"Choosing no reload settings");
			reload = false;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
			return;
		}  else if(v.getTag().equals("reload_ok")) {
			Tracer.v(mytag,"Choosing settings reload");
			reload=true;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
		}
	}
	
	private Boolean startCacheEngine() {
		if(WU_widgetUpdate == null) {
			this.Create_message_box();
			PG_dialog_message.setMessage(getText(R.string.loading_cache)); 
			PG_dialog_message.show();
			Tracer.w(mytag, "Starting WidgetUpdate cache engine !");
			WU_widgetUpdate = WidgetUpdate.getInstance();
			WU_widgetUpdate.set_handler(sbanim, 0);	//put our main handler into cache engine (as Main)
			Boolean result = WU_widgetUpdate.init(Tracer, this,SP_params);
			WU_widgetUpdate.wakeup();
			if(! result)
				return result;
		}  
		Tracer.set_engine(WU_widgetUpdate);
		return true;
	}
	
	private Boolean restartdomodroid(){
		Intent i = getBaseContext().getPackageManager()
	            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
		return true;
	}
	
	private Boolean restartactivity(){
		Intent intent = getIntent();
	    finish();
	    startActivity(intent);
		return true;
	}
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==82 && !SD_panel.isOpen()){
			SD_panel.setOpen(true, true);
			return false;
		}else if((keyCode==82 || keyCode == 4) && SD_panel.isOpen()){
			SD_panel.setOpen(false, true);
			return false;
		}else if(keyCode == 4 && !SD_panel.isOpen() && historyPosition > 0){
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	//this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	{
	    super.onConfigurationChanged(newConfig);
	    //setContentView(R.layout.activity_home);
	    refresh();
	}
	
	public void refresh()
	{
	   //setContentView(R.layout.activity_home);
	    loadWigets(Integer.parseInt(history.elementAt(historyPosition)[0]),history.elementAt(historyPosition)[1]);
	}
}

