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

import widgets.Com_Stats;
import widgets.Graphical_Feature;
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
	private Toast starting;
	private Widgets_Manager wAgent;
	private Dialog_Synchronize dialog_sync;
	private WidgetUpdate widgetUpdate;
	private Handler sbanim;
	private static Handler widgetHandler;
	private Intent mapI = null;
	private Button sync;
	private Button Exit;	//Added by Doume
	private Button usage_settings;	//Added by Tikismoke
	private Button server_settings;	//Added by Tikismoke
	private Button map_settings;	//Added by Tikismoke
	private Button debug_settings;	//Added by Doume
	private Dialog_Usage usage_set = null;
	private Dialog_Server server_set = null;
	private Dialog_Map map_set = null;
	private Dialog_Debug debug_set = null;
	private ImageView appname;
	
	private int dayOffset = 1;
	private int secondeOffset = 5;
	private ViewGroup parent;
	private LinearLayout ll_area;
	private LinearLayout ll_room;
	private LinearLayout ll_activ;
	private Vector<String[]> history;
	private int historyPosition;
	private LinearLayout house_map;
	private Graphical_Feature house;
	private Graphical_Feature map;
	private Graphical_Feature stats;
	
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
	//private AlertDialog.Builder dialog_message;
	protected ProgressDialog dialog_message;
	private Boolean cache_ready = false;
	private Boolean end_of_init_requested = true;
	private LinearLayout info;
	private TextView info_msg;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		myself=this;
		setContentView(R.layout.activity_home);
		
		
		//sharedPref
		params = getSharedPreferences("PREFS",MODE_PRIVATE);
		prefEditor=params.edit();
		Tracer = tracerengine.getInstance(params);
		
		//Added by Doume
		File storage = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/");
		if(! storage.exists())
			storage.mkdirs();
		//Configure Tracer tool initial state
		File logpath = new File(Environment.getExternalStorageDirectory()+"/domodroid/.log/");
		if(! logpath.exists())
			logpath.mkdirs();
		
		String currlogpath = params.getString("LOGNAME", "");
		if(currlogpath.equals("")) {
			//Not yet existing prefs : Configure debugging by default, to configure Tracer
			currlogpath=Environment.getExternalStorageDirectory()+"/domodroid/.log/";
			prefEditor.putString("LOGPATH",currlogpath);
			prefEditor.putString("LOGNAME","Domodroid.txt");
			prefEditor.putBoolean("SYSTEMLOG", false);
			prefEditor.putBoolean("TEXTLOG", false);
			prefEditor.putBoolean("SCREENLOG", false);
			prefEditor.putBoolean("LOGCHANGED", true);
			prefEditor.putBoolean("LOGAPPEND", false);
		} else {
			prefEditor.putBoolean("LOGCHANGED", true);		//To force Tracer to consider current settings
		}
		//prefEditor.putBoolean("SYSTEMLOG", false);		// For tests : no system logs....
		prefEditor.putBoolean("SYSTEMLOG", true);		// For tests : with system logs....

		prefEditor.commit();
		
		Tracer.set_profile(params);
		
		//option
		appname = (ImageView)findViewById(R.id.app_name);
		
		Exit=(Button)findViewById(R.id.Stop_all);
		Exit.setOnClickListener(this);
		Exit.setTag("Exit");
		
		usage_settings=(Button)findViewById(R.id.bt_usage_settings);
		usage_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v("Activity_Main.onclick()","Call to usage settings screen");
				if(usage_set != null)
					usage_set.get_params();
				else
					usage_set = new Dialog_Usage(Tracer, params, myself);
				usage_set.show();
				return;
			}
		});
		server_settings=(Button)findViewById(R.id.bt_server_settings);
		server_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v("Activity_Main.onclick()","Call to server settings screen");
				if(server_set != null)
					server_set.get_params();
				else
					server_set = new Dialog_Server(Tracer, params, myself);
				server_set.show();
				return;
			}
		});
		
		map_settings=(Button)findViewById(R.id.bt_map_settings);
		map_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v("Activity_Main.onclick()","Call to Map settings screen");
				if(map_set != null)
					map_set.get_params();
				else
					map_set = new Dialog_Map(Tracer, params, myself);
				map_set.show();
				return;
			}
		});
		
		debug_settings=(Button)findViewById(R.id.bt_debug_settings);
		debug_settings.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				//Disconnect all opened sessions....
				Tracer.v("Activity_Main.onclick()","Call to Debug settings screen");
				if(debug_set != null)
					debug_set.get_params();
				else
					debug_set = new Dialog_Debug(Tracer, params, myself);
				debug_set.show();
				return;
			}
		});
		
		sync=(Button)findViewById(R.id.sync);
		sync.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// click on 'sync' button into Sliding_Drawer View
				panel.setOpen(false, false);	// Hide the View
				run_sync_dialog();		// And run a resync with Rinor server
			}
			
		});
		
		LoadSelections();
		
		// Prepare a listener to know when a sync dialog is closed...
		if( sync_listener == null){
			sync_listener = new DialogInterface.OnDismissListener() {

				public void onDismiss(DialogInterface dialog) {
					
					Tracer.d("Activity_Main","sync dialog has been closed !");
					
					// Is it success or fail ?
					if(((Dialog_Synchronize)dialog).need_refresh) {
						// Sync has been successful : Force to refresh current main view
						Tracer.d("Activity_Main","sync dialog requires a refresh !");
						reload = true;	// Sync being done, consider shared prefs are OK
						parent.removeAllViews();
						if(widgetUpdate != null) {
							widgetUpdate.resync();
						} else {
							Tracer.i("Activity_Main.onCreate","WidgetUpdate is null startCacheengine!");
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
						
					} else {
						Tracer.d("Activity_Main","sync dialog end with no refresh !");
						
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
					/*
					Tracer.e("Activity_Main","Request to display message : 8000");
					if(dialog_message == null) {
						Create_message_box();
					}
					dialog_message.setMessage("Starting cache engine...");
					dialog_message.show();
					
					*/
				} else if(msg.what==8999){
					//Cache engine is ready for use....
					if(Tracer != null)
						Tracer.e("Activity_Main","Cache engine has notified it's ready !");
					cache_ready=true;
					if(end_of_init_requested)
						end_of_init();
					dialog_message.dismiss();
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
		
		menu_green = (TextView) findViewById(R.id.menu_button2);
		menu_green.setVisibility(View.GONE);
		
		menu_white = (TextView) findViewById(R.id.menu_button1);
		menu_white.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// A clic on menu will activate/deactivate panel allowing settings configuration and giving
				//  access to 'sync' button
				if(!panel.isOpen()){
					panel.setOpen(true, true);	//open with animation
				}else{
					panel.setOpen(false, true);	//hide with animation
				}
			}
		});
		menu_white.setVisibility(View.VISIBLE);
		
		menu_about = (TextView) findViewById(R.id.About_button);
		menu_about.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				dont_freeze=true;		//To avoid WidgetUpdate engine freeze
				Intent helpI = new Intent(Activity_Main.this,Activity_About.class);
				startActivity(helpI);				
			}
		});
		
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
		
		house = new Graphical_Feature(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "House"),
				"",
				"house",
				0, mytype);
		house.setPadding(0, 0, 5, 0);
		map = new Graphical_Feature(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "Map"),
				"",
				"map",
				0, mytype);
		map.setPadding(5, 0, 0, 0);
		stats = new Graphical_Feature(getApplicationContext(),0,
				Graphics_Manager.Names_Agent(this, "statistics"),
				"",
				"statistics",0, mytype);
		stats.setPadding(0, 0, 5, 0);
		
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);

		
		house.setLayoutParams(param);
		house.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
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
		});
		
		map.setLayoutParams(param);
		map.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(panel.isOpen()){
					// Ignore map call if panel is opened, to avoid confusion between objects
					return;
				}
				if(params.getBoolean("SYNC", false)==true){
					//dont_freeze=true;		//To avoid WidgetUpdate engine freeze
					Tracer.w("Activity_Main","Before call to Map, Disconnect widgets from engine !");
					if(widgetUpdate != null) {
						widgetUpdate.Disconnect(0);	//That should disconnect all opened widgets from cache engine
						//widgetUpdate.dump_cache();	//For debug
						dont_kill = true;	// to avoid engines kill when onDestroy()
					}
					mapI = new Intent(Activity_Main.this,Activity_Map.class);
					Tracer.d("Activity_Main","Call to Map, run it now !");
					Tracer.Map_as_main = false;
					startActivity(mapI);
				}else{
					if(notSyncAlert == null)
						createAlert();
					notSyncAlert.show();
				}
			}
		});
		
		stats.setLayoutParams(param);
		stats.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				if(params.getBoolean("SYNC", false)==true){
					loadWigets(0, "statistics");
					historyPosition++;
					history.add(historyPosition,new String [] {"0","statistics"});
				}else{
					if(notSyncAlert == null)
						createAlert();
					notSyncAlert.show();
				}
			}
		});

		if(! by_usage)
			house_map.addView(house);
		else
			house_map.addView(stats);
		
		house_map.addView(map);
		init_done = false;
		// Detect if it's the 1st use after installation...
			if(!params.getBoolean("SPLASH", false)){
				// Yes, 1st use !
				init_done = false;
				reload = false;
				if(backupprefs.exists()) {
					// A backup exists : Ask if reload it
					Tracer.v("Activity_Main","settings backup found after a fresh install...");
					
					DialogInterface.OnClickListener reload_listener = new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Tracer.e("Activity_Home","Reload dialog returns : "+which);
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
					Tracer.v("Activity_Main","no settings backup found after fresh install...");
					end_of_init_requested = true;
				}
			} else {
				// It's not the 1st use after fresh install
				// This method will be followed by 'onResume()'
				end_of_init_requested = true;
			}
			if(params.getBoolean("SYNC", false)){
				//A config exists and a sync as been done by past.
				if(widgetUpdate == null) {
					Tracer.i("Activity_Main.onCreate","Params splach is false and WidgetUpdate is null startCacheengine!");
					startCacheEngine();
				}

			}
			
			Tracer.e("Activity_Main","OnCreate() complete !");
			// End of onCreate (UIThread)
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Tracer.e("Activity_Main.onResume","Check if initialize requested !");
		if(! init_done) {
			Tracer.i("Activity_Main.onResume","Init not done!");
			if(params.getBoolean("SPLASH", false)){
				Tracer.i("Activity_Main.onResume","params Splash is false !");
				cache_ready = false;
				//try to solve 1rst launch and orientation problem
				Tracer.i("Activity_Main.onresume","Init not done! and params Splash is false startCacheengine!");
				//startCacheEngine();
				//end_of_init();		//Will be done when cache will be ready
			}
		}
		else {
			Tracer.i("Activity_Main.onResume","Init done!");
				end_of_init_requested=true;
				if(widgetUpdate != null) {
					Tracer.i("Activity_Main.onResume","Widget update is not null so wakeup widget engine!");
					widgetUpdate.wakeup();		//If cache ready, that'll execute end_of_init()
				}
				//end_of_init();	//all client widgets will be re-created
			}
		if(end_of_init_requested)
			end_of_init();
		}
	
	@Override
	public void onPause(){
		super.onPause();
		panel.setOpen(false, false);
		Tracer.w("Activity_Main.onPause","Going to background !");
		if(widgetUpdate != null)  {
			if(! Tracer.Map_as_main) {
				// We're the main initial activity
				widgetUpdate.set_sleeping();	//Don't cancel the cache engine : only freeze it
			}
		}
			
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.wAgent=null;
		widgetHandler=null;
		if(widgetUpdate != null) {
			widgetUpdate.Disconnect(0);	//remove all pending subscribings
			if(! Tracer.Map_as_main) {
				// We're the main initial activity
				Tracer.w("Activity_Main.onDestroy","cache engine set to sleeping !");
				this.mWakeLock.release();	// We allow screen shut, now...
				widgetUpdate.set_sleeping();	//Don't cancel the cache engine : only freeze it
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
		if(dialog_message != null)
			return;
		
		
		dialog_message = new ProgressDialog(this);
		dialog_message.setMessage(getText(R.string.init_in_process));
		//dialog_reload.setPositiveButton("OK", message_listener);
		dialog_message.setTitle(getText(R.string.please_wait));
	}
	
	public void force_DB_update() {
		if(widgetUpdate != null) {
			widgetUpdate.refreshNow();
		}
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
		if(Tracer == null)
			Tracer = Tracer.getInstance();
		
		Tracer.v("Activity_Main","end_of_init Main Screen..");
		
		if(! reload) {
			//alertDialog not sync splash
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
			return;
		}
		end_of_init_requested = false;
		
		if(history != null)
			history = null;		//Free resource
		history = new Vector<String[]>();
		historyPosition = 0;
		
		//load widgets
		if(widgetHandler == null) {
			Tracer.i("Activity_Main", "Starting WidgetHandler thread !");
			widgetHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					
					try {
						historyPosition++;
						loadWigets(msg.getData().getInt("id"), msg.getData().getString("type"));
						Tracer.v("Activity_Main.widgetHandler", "add history "+msg.getData().getInt("id")+" "+msg.getData().getString("type"));
						history.add(historyPosition,new String [] {msg.getData().getInt("id")+"",msg.getData().getString("type")});
					} catch (Exception e) {
						Tracer.e("Activity_Main.widgetHandler", "handler error into loadWidgets");
						e.printStackTrace();
					}
				}	
			};
		}
		if(wAgent == null) {
			Tracer.v("Activity_Main", "Starting wAgent !");
			wAgent=new Widgets_Manager(Tracer, widgetHandler);
			wAgent.widgetupdate = widgetUpdate;
		}
		if(starting != null) {
			starting.cancel();
			starting.setText("Creating widgets....");
			starting.setDuration(Toast.LENGTH_SHORT);
			starting.show();
		}
		init_done = true;
		
		if((params.getBoolean("START_ON_MAP", false) && ( ! Tracer.force_Main) ) ) {
			Tracer.e("Activity_Main", "Direct start on Map requested...");
			Tracer.Map_as_main = true;		//Memorize that Map is now the main screen
			mapI = new Intent(Activity_Main.this,Activity_Map.class);
			startActivity(mapI);
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
		Tracer.v("Activity_Main","reload choice done..");
		if(reload) {
			// If answer is 'yes', load preferences from backup
			Tracer.e("Activity_Home","reload settings..");
			loadSharedPreferencesFromFile(backupprefs);
			panel.setOpen(false, false);
			run_sync_dialog();
			
		} else {
			Tracer.v("Activity_Main","Settings not reloaded : clear database..");
			File database = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/domodroid.db");
			if(database.exists()) {
				database.delete();
			}
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
	                Tracer.v("Activity_Main","Loading pref : "+key+" -> "+v.toString());
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
		Tracer.d("Activity_Main.loadWidgets","Construct main View id="+id+" type="+type);
		parent.removeAllViews();
		ll_area = new LinearLayout(this);
		ll_area.setOrientation(LinearLayout.VERTICAL);
		ll_room = new LinearLayout(this);
		ll_room.setOrientation(LinearLayout.VERTICAL);
		ll_activ = new LinearLayout(this);
		ll_activ.setOrientation(LinearLayout.VERTICAL);
		
		house_map.removeAllViews();
		if( ! by_usage) {
			house_map.addView(house);
			house_map.addView(map);
		} else {
			house_map.addView(stats);
			house_map.addView(map);
		}
		try {
			
			if(type.equals("root")){
				ll_area.removeAllViews();
				if(! by_usage) {
					// Version 0.2 : display house, map and areas
					parent.addView(house_map);	// House & map
					ll_area = wAgent.loadAreaWidgets(this, ll_area, params);
					parent.addView(ll_area);	//and areas
				} else {
					// by_usage
					
					parent.addView(house_map);	// With only map
					ll_room = wAgent.loadRoomWidgets(this, 1, ll_room, params);	//List of known usages 'as rooms'
					parent.addView(ll_room);
					
				}
			} else if(type.equals("house")) {
				//Only possible if version 0.2 (the 'house' is never proposed to be clicked)
				ll_area.removeAllViews();
				parent.addView(house_map);	// House & map
				ll_area = wAgent.loadAreaWidgets(this, ll_area, params);
				parent.addView(ll_area);	//and areas
				ll_activ.removeAllViews();
				ll_activ = wAgent.loadActivWidgets(this, id, type, ll_activ,params, mytype);
				parent.addView(ll_activ);
				
			}else if(type.equals("statistics")) {
				//Only possible if by_usage (the 'stats' is never proposed with version 0.2)
				ll_area.removeAllViews();
				ll_activ.removeAllViews();
				ll_activ = wAgent.loadActivWidgets(this, -1, type, ll_activ ,params, mytype);
				parent.addView(ll_activ);
				
			} else 	if(type.equals("area")) {
				//Only possible if version 0.2 (the area 'usage' is never proposed to be clicked)
				//parent.addView(house_map);	// House & map
				ll_room.removeAllViews();
				ll_room = wAgent.loadRoomWidgets(this, id, ll_room, params);
				parent.addView(ll_room);
				ll_activ.removeAllViews();
				ll_activ = wAgent.loadActivWidgets(this, id, type, ll_activ,params, mytype);
				parent.addView(ll_activ);
			} else 	if(type.equals("room")) {
				ll_activ.removeAllViews();
				ll_activ = wAgent.loadActivWidgets(this, id, type, ll_activ,params, mytype);
				parent.addView(ll_activ);
			} 
				
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	private void SaveSelections(Boolean mode) {
		try{
			SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
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
		SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
		tempUrl=params.getString("IP1",null);
		by_usage = params.getBoolean("BY_USAGE", false);
	}
	
	private void run_sync_dialog() {
		
		if(dialog_sync == null)
			dialog_sync = new Dialog_Synchronize(Tracer, this);
		dialog_sync.reload = reload;
		dialog_sync.setOnDismissListener(sync_listener);
		dialog_sync.setParams(params);
		dialog_sync.show();
		dialog_sync.startSync();
	}
	
	public void onPanelClosed(Sliding_Drawer panel) {
		Tracer.w("Activity_Main","onPanelClosed");
		menu_green.startAnimation(animation2);
		menu_green.setVisibility(View.GONE);
		SaveSelections(false);		// To force a sync operation, if something has been modified...
		/*
		if(widgetUpdate == null) {
			startDBEngine();
		}
		*/
	}
	
	public void onPanelOpened(Sliding_Drawer panel) {
		Tracer.v("Activity_Main","onPanelOpened");
		menu_green.setVisibility(View.VISIBLE);
		menu_green.startAnimation(animation1);
		
	}

	public void onClick(View v) {
		dont_freeze = false;		// By default, onPause() will stop WidgetUpdate engine...
		//ALL other that are not explicitly used
		if(v.getTag().equals("Exit")) {
			//Disconnect all opened sessions....
			Tracer.v("Activity_Main Exit","Stopping WidgetUpdate thread !");
			this.wAgent=null;
			widgetHandler=null;
			Tracer.set_engine(null);
			if (!(widgetUpdate == null)) {
				widgetUpdate.Disconnect(0);	//Disconnect all widgets owned by Main
			}
			dont_kill = false;		//To force OnDestroy() to also kill engines
			//And stop main program
			this.finish();
			return;
		} else if(v.getTag().equals("reload_cancel")) {
			Tracer.v("Activity_Main","Choosing no reload settings");
			reload = false;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
			return;
		}  else if(v.getTag().equals("reload_ok")) {
			Tracer.v("Activity_Main","Choosing settings reload");
			reload=true;
			synchronized(waiting_thread){
				waiting_thread.notifyAll();
	        }
		}
	}
	
	private Boolean startCacheEngine() {
		if(widgetUpdate == null) {
			this.Create_message_box();
			dialog_message.setMessage(getText(R.string.loading_cache)); 
			dialog_message.show();
			Tracer.w("Activity_Main", "Starting WidgetUpdate cache engine !");
			widgetUpdate = WidgetUpdate.getInstance();
			widgetUpdate.set_handler(sbanim, 0);	//put our main handler into cache engine (as Main)
			Boolean result = widgetUpdate.init(Tracer, this,params);
			widgetUpdate.wakeup();
			if(! result)
				return result;
		}  
		Tracer.set_engine(widgetUpdate);
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
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	//this is called when the screen rotates.
	// (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
	{
	    super.onConfigurationChanged(newConfig);
	    //setContentView(R.layout.activity_home);

	}
}

