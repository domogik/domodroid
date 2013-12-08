package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import rinor.Events_manager;
import rinor.Rest_com;
import rinor.Rinor_event;
import rinor.Stats_Com;
import widgets.Entity_Feature;
import widgets.Entity_Map;
import widgets.Entity_client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import misc.tracerengine;

public class WidgetUpdate  {
	//implements Serializable {

	/**
	 * 
	 */
	private static WidgetUpdate instance;
	private static final long serialVersionUID = 1L;
	private SharedPreferences sharedparams;
	
	private boolean activated;
	private Activity context;
	private DomodroidDB domodb;
	public  String mytag="WidgetUpdate";
	private TimerTask doAsynchronousTask;
	private tracerengine Tracer = null;
	
	public ArrayList<Cache_Feature_Element> cache = new ArrayList<Cache_Feature_Element>();
	private Boolean locked = false;
	private Boolean timer_flag = false;
	public Boolean ready = false;
	private Handler mapView = null;
	public Events_manager eventsManager = null;
	private static Handler myselfHandler = null ;
	private int last_ticket = -1;
	private int last_position = -1;
	private Timer timer = null;
	private int callback_counts = 0;
	private Boolean init_done = false;
	
	private Boolean sleeping = false;
	private static Stats_Com stats_com = null; 
	
	//
	// Table of handlers to notify
	// pos 0 = Main
	// pos 1 = Map
	// pos 2 = MapView
	private static Handler[] parent = new Handler[3];
	
	/*
	 * This class is a background engine 
	 * 		On instantiation, it connects to Rinor server, and submit queries 
	 * 		each 'update' timer, to update local database values for all known devices
	 * When variable 'activated' is set to false, the thread is kept alive, 
	 *     but each timer is ignored (no more requests to server...)
	 * When variable 'activated' is true, each timer generates a database update with server's response
	 */
	/*
	 * New concept introduced by Doume at 2013/02/15
	 * This engine will maintain a cache of state values
	 * This cache will be updated after each request to server (in parallel to database during transition phase)
	 * 	When a value change, this engine will notify each client having subscribed to the device
	 * 		for an immediate screen update
	 * 		so, clients have not to use anymore a timer to display the changes
	 * May be in future, this engine will also use Rest events with server, to avoid
	 * 		use of timer and delayed updates
	 */
	/*******************************************************************************
	*		Internal Constructor
	*******************************************************************************/
		private WidgetUpdate()
		{
			super();
			
		}
	
	public static WidgetUpdate getInstance() {
		if(instance == null) {
			Log.e("Events_Manager", "Creating instance........................");
			instance = new WidgetUpdate();
		}
		return instance;
		
	}
	
	@SuppressLint("HandlerLeak")
	public Boolean init(tracerengine Trac, Activity context, SharedPreferences params){
		Boolean result = false;
		if(init_done) {
			Log.e(mytag,"init already done");
			return true;
		}
		stats_com = Stats_Com.getInstance();	//Create a statistic counter, with all 0 values
		sleeping=false;
		this.sharedparams=params;
		this.Tracer = Trac;
		this.context = context;
		activated = true;
		/*
		if(Tracer != null) {
			if(Tracer.DBEngine_running) {
				try {
					finalize();
				} catch (Throwable t) {}
			}
			Tracer.DBEngine_running = true;		//To avoid multiple engines running for same Activity
		}
		*/
		Tracer.d(mytag,"Initial start requested....");
		domodb = new DomodroidDB(Tracer, context);	
		domodb.owner=mytag;
		timer_flag = false;
		ready=false;
		/*
		if(parent[0] != null) {
			parent[0].sendEmptyMessage(8000);	//Ask main to display message
		}
		*/
		Tracer.d(mytag,"cache engine starting timer for periodic cache update");
		Timer();		//and initiate the cyclic timer
		new UpdateThread().execute();	//And force an immediate refresh
		this.callback_counts = 0;	//To force a refresh
		/*
		Boolean said = false;
		int max_time_for_sync = 15 * 1000;		// On initial cache initialization, return an error 
												// if cannot connect in 15 seconds
												// Probably wrong URL ?
		int sync_duration = 0;
		while (! ready) {
			if(! said) {
				Tracer.d(mytag,"cache engine not yet ready : Wait a bit !");
				said=true;
			}
			try{
				Thread.sleep(200);		// Wait 0,2 second
			} catch (Exception e) {};
			sync_duration += 200;
			if(sync_duration > max_time_for_sync) {
				Tracer.d(mytag,"cache engine not synced after "+(max_time_for_sync/1000)+" seconds !");
				sync_duration = 0;
				//return result;		//false, if sync not success
			}
		}
		if(parent[0] != null) {
			parent[0].sendEmptyMessage(8999);	//hide Toast message
		}
		Tracer.d(mytag,"cache engine ready !");
		*/
		// Cache contains list of existing devices, now !
		
		///////// Create an handler to exchange with Events_Manager///////////////////
		myselfHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				//This handler will receive notifications from Events_Manager and from waitingThread running in background
				// 1 message => 1 event : so, it's serialized !
				if(msg.what == 8999) {
					// Cache engine being ready, we can start events manager
					Tracer.d(mytag,"Main thread handler : Cache engine is now ready....");
					
					if(eventsManager == null) {
						eventsManager = Events_manager.getInstance(); 
					}
					eventsManager.init(Tracer, myselfHandler, cache, sharedparams, instance);
					/*
					if(parent[0] != null) {
						parent[0].sendEmptyMessage(8999);	//Forward event to Main
					}
					*/
				} else	if(msg.what == 9900) {
					if(eventsManager != null) {
						callback_counts++;
						Rinor_event event = eventsManager.get_event();
						if(event != null) {
							Tracer.d(mytag,"Event from Events_Manager found, ticket : "+event.ticket_id+" # "+event.item);
						
							if(last_ticket == -1) {
								last_ticket=event.item;	//Initial synchro on item
							} else {
								if(! ( (last_ticket + 1)== event.item)) {
									Tracer.d(mytag,"Handler event lost ? expected # "+(last_ticket + 1)+" Received # "+event.item);
								} 
								last_ticket=event.item;
							}
						
						
							mapView = null;
							//mapView will be set by update_cache_device if at least one mini widget has to be notified
							update_cache_device(event.device_id,event.key,event.Value);
							if(mapView != null) {
								//It was a mini widget, not yet notified : do it now..
								try {
									Tracer.i(mytag, "Handler send a notification to MapView");
									mapView.sendEmptyMessage(9997);	//notify the group of widgets a new value is there
								} catch (Exception e) {}
							}
							//event = eventsManager.get_event();		//Try to get the next...
						} // if event
					} else {
						Tracer.d(mytag,"No Events_Manager known ! ! ! ");
					}
				} else if (msg.what == 9901) {
					// Events_Manager thread is dead....
					eventsManager = null;
					init_done = false;
					Tracer.i(mytag,"No more Events_Manager now ! ! ! ");
					//Clean all resources
					domodb = null;
					if(timer != null)
						timer.cancel();
					timer = null;
					doAsynchronousTask = null;
					System.gc();
					Tracer.i(mytag,"We can really go to end...");
					try {
						this.finalize();
					} catch (Throwable t) {}
					
				} else if (msg.what == 9902) {
					//Time out processed
					callback_counts++;
					
				}
			}
		};
		///////// and pass to it now///////////////////
		Tracer.d(mytag,"Waiting thread started to notify main when cache ready !");
		new waitingThread().execute();
		
		Tracer.d(mytag,"cache engine initialized !");
		init_done = true;
		result=true;
		return result;
	}
	/*
	 * Allow callers to set their handler in table
	 */
	public void set_handler(Handler parent, int type) {
		//type = 0 if View , or 1 if Map
		if((type >= 0) && (type <= 2))
			this.parent[type] = parent;
	}
	
	public void cancel() {
		activated = false;
		if(timer != null)
			timer.cancel();
		if(eventsManager != null)
			eventsManager.Destroy();
		if(stats_com != null)
			stats_com.cancel();
		stats_com = null;
		Tracer.d(mytag,"cache engine cancel requested : Waiting for events_manager dead !");
		
		
		
	}
	
	public void Disconnect(int type){
		String name = "???";
		
		if(type == 0)
			name="Main";
		else if (type == 1)
			name="Map";
		else if (type == 2)
			name="MapView";
		//Tracer.d(mytag,"Disconnect requested by "+name);
		
		// Purge all clients matching this container, as soon cache is unlocked
		while(locked) {
			//Somebody else is updating list...
			try{
				Thread.sleep(10);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		locked=true;
		
		
		for(int i=0; i < cache.size(); i++) {
			Cache_Feature_Element cache_entry = cache.get(i);
			ArrayList<Entity_client> clients_list = null;
			ArrayList<Entity_client> temp_list = null;
			
			if(cache_entry != null) {
				clients_list = cache_entry.clients_list;
			}
			if(clients_list != null) {
				int cs = clients_list.size();
				//Tracer.i(mytag, "Processing cache entry # "+i+" <"+cache_entry.DevId+"> clients # = "+cs);
				if(cs > 0) {
					temp_list = cache_entry.clone_clients_list();
					int deleted = 0;
					for(int j = 0; j < cs; j++) {
						//Tracer.i(mytag, "   Processing client "+j+"/"+(cs-1));
						Entity_client curclient = null;
						
						try {
							curclient = clients_list.get(j);
								
						} catch (Exception e) {
							Tracer.i(mytag, "   Exception on client # "+j);
							curclient = null;
						}
						//check each connected client pointed by list
						if(curclient != null) {
							int cat = curclient.getClientType();
							if(cat == type) {
								//This client was owned by requestor... Remove it  from list
								Tracer.i(mytag, "remove client # "+j+" <"+curclient.getName()+"> from list "+name);
								curclient.setClientId(-1);	//note client disconnected
								curclient.setClientType(-1);	//this entry is'nt owned by anybody
								curclient.setHandler(null);	//And must not be notified anymore
								temp_list.remove(j-deleted);
								deleted++;
								if(temp_list.size() == 0) {
									//List is empty : remove it from the device entry
									temp_list = null;
									break;
								}
								
							}
						}
					}	//End of loop on clients list, for a cache entry
					cache_entry.clients_list=temp_list;
				}
			}
			//Next cache entry
		} // End of loop on cache items
		locked=false;
		//dump_cache();	//During development, help to debug !
		return;
	}
	public void dump_cache() {
		String[] name = new String[]{ "Main   ","Map    ","MapView", "???    "};
		int size = cache.size(); 
		Tracer.e(mytag, "Dump of Cache , size = "+cache.size());
		
		while(locked) {
			//Somebody else is updating list...
			try{
				Thread.sleep(10);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		locked=true;
		
		
		for(int i=0; i < size; i++) {
			Cache_Feature_Element cache_entry = cache.get(i);
			ArrayList<Entity_client> clients_list = null;
			if(cache_entry == null) {
				Tracer.e(mytag, "Cache entry # "+i+"   empty ! ");
			} else {
				clients_list = cache_entry.clients_list;
				int clients_list_size = 0;
				if(clients_list != null)
					clients_list_size = clients_list.size();
				
				Tracer.e(mytag, "Cache entry # "+i+"   DevID : "+cache_entry.DevId+" Skey : "+cache_entry.skey+" Clients # :"+clients_list_size);
				if(clients_list_size > 0) {
					for(int j = 0; j < clients_list_size; j++) {
						if(clients_list.get(j) == null) 
							break;
						
						int cat = clients_list.get(j).getClientType();
						String client_name = clients_list.get(j).getName();
						Handler h = clients_list.get(j).getClientHandler();
						String state = "connected";
						if(h == null)
							state="zombie";
						String type ="widget";
						if (clients_list.get(j).is_Miniwidget())
							type = "mini widget";
						int ctype = clients_list.get(j).getClientType();
						if (ctype == -1)
							ctype = 3;
						
						Tracer.e(mytag, "           ==> entry : "+j+" owner : "+name[ctype]
								+" client name : "+client_name
								+" type = "+type
								+" state = "+state);
								
					}	
				}
			}
				
		}	//End of loop on clients list, for a cache entry
			
		locked=false;
		Tracer.e(mytag, "End of cache dump ");
		
	}
	/* 
	 * Method allowing external methods to force a refresh
	 */
	public void refreshNow() {
		if(doAsynchronousTask != null)
			doAsynchronousTask.run();	//To force immediate refresh
	}
	public void resync(){
		//May be URL has been changed : force engine to reconstruct cache
		Disconnect(0);
		Disconnect(1);
		Disconnect(2);
		locked = true;
		cache=null;
		cache = new ArrayList<Cache_Feature_Element>();
		ready=false;
		refreshNow();	//To reconstruct cache
		Tracer.d(mytag,"state engine resync : waiting for initial setting of cache !");
		
		Boolean said = false;
		int counter = 0;
		while (! ready) {
			if(! said) {
				Tracer.d(mytag,"cache engine not yet ready : Wait a bit !");
				said=true;
			}
			try{
				Thread.sleep(100);
				counter++;
				if(counter > 100) {
					// 10 seconds elapsed
					//finalize();
				}
			} catch (Exception e) {};
		}
		Tracer.d(mytag,"cache engine ready after resync !");
		locked=false;
	}
	/*
	 * This method should only be called once, to create and arm a cyclic timer 
	 */
	public void Timer() {
		timer = new Timer();
		
		
		final Handler loc_handler = new Handler();
		if(timer_flag)
			return;	//Don't run many cyclic timers !
		
		doAsynchronousTask = new TimerTask() {
		
			@Override
			public void run() {
				Runnable myTH = new Runnable() {
				
					public void run() {
						if(activated) {
							try {
								new UpdateThread().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						
						} 
					} //End of run methodTimer
				};	// End of runnable bloc
				
				try {
					loc_handler.post(myTH);		//To avoid exception on ICS
				} catch (Exception e) {
						e.printStackTrace();
				}
			}
		};
		
		// and arm the timer to do automatically this each 'update' seconds
		timer_flag=true;	//Cyclic timer is running...
		if(timer != null) {
			timer.schedule(doAsynchronousTask, 0, 125*1000);	// for tests with Events_Manager 
																// 2'05 is a bit more than events timeout by server (2')
			
			//timer.schedule(doAsynchronousTask, 0, sharedparams.getInt("UPDATE_TIMER", 300)*1000);
		}
	}
	
	/*
	 * Method to freeze/wakeup server exchanges and cache state ( on Pause / Resume, by example )
	 */
	public void set_sleeping() {
		Tracer.d(mytag,"Pause requested...");
		if(eventsManager != null) {
			eventsManager.set_sleeping();
		}
		if(stats_com != null)
			stats_com.set_sleeping();
		
		sleeping = true;
	}
	
	public void wakeup() {
		Tracer.d(mytag,"Wake up requested...");
		if(eventsManager != null) {
			eventsManager.wakeup();
		}
		if(stats_com != null)
			stats_com.wakeup();
		
		sleeping = false;
		//TODO : if sleep period too long (> 1'50) , we must force a refresh of cache values, because events have been 'masked' !
		if((eventsManager != null) && eventsManager.cache_out_of_date) {
			callback_counts = 0;
			new UpdateThread().execute();	//Force an immediate cache refresh
			eventsManager.cache_out_of_date = false;
		}
		if(ready) {
			if(parent[0] != null) {
				parent[0].sendEmptyMessage(8999);	//Notify cache is ready
			}
			
		}
	}
	public class waitingThread extends AsyncTask<Void, Integer, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			Boolean said = false;
			int counter = 0;
			while (! ready) {
				if(! said) {
					Tracer.d(mytag,"cache engine not yet ready : Wait a bit !");
					said=true;
				}
				try{
					Thread.sleep(100);
					counter++;
					if(counter > 100) {
						said = false;
						counter = 0;
					}
				} catch (Exception e) {};
			}
			if(myselfHandler != null) {
				//Tracer.d(mytag,"cache engine ready  ! Notify it....");
				myselfHandler.sendEmptyMessage(8999);	// cache engine ready.....
			}
			if(parent[0] != null) {
				Tracer.d(mytag,"cache engine ready  ! Notify Main activity....");
				parent[0].sendEmptyMessage(8999);	//hide Toast message
			}
			Tracer.d(mytag,"cache engine ready  ! Exiting Waiting thread ....");
			return null;
		}
		
	}
	
	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// Added by Doume to correctly release resources when exiting
			if(! activated) {
				Tracer.d(mytag,"UpdateThread frozen....");
				
			} else {
				if(sleeping) {
					return null;	//Will check stats in 2 minutes
				}
				if(callback_counts > 0) {
					if(Tracer != null)
						Tracer.d(mytag,"Events detected since last loop = "+callback_counts+" No stats !");
					//myselfHandler.sendEmptyMessage(9900);	//Force to drain the pending stack events
					callback_counts = 0;
					return null;
				}
				if(Tracer != null)
					Tracer.d(mytag,"Request to server for stats update...");
				String request = sharedparams.getString("UPDATE_URL", null);
				if(request != null){
					JSONObject json_widget_state = null;
					stats_com.add(Stats_Com.STATS_SEND, request.length());
					try{
						json_widget_state = Rest_com.connect(request);
					} catch (Exception e) {
						//stats request cannot be completed (broken link or terminal in standby ?)
						//Will retry automatically in 2'05, if no events received
						Tracer.e(mytag,"get stats : Rinor error <"+e.getMessage()+">");
						//TODO make a Toast with error getMessage()
						return null;
					}
					//Tracer.d(mytag,"UPDATE_URL = "+ sharedparams.getString("UPDATE_URL", null));
					//Tracer.d(mytag,"result : "+ json_widget_state.toString());
					if(json_widget_state != null) {
						stats_com.add(Stats_Com.STATS_RCV, json_widget_state.toString().length());
						// cache engine : update cache with new values...
						int updated_items = update_cache(json_widget_state);
						//if(updated_items > 0) {
							//domodb.insertFeatureState(json_widget_state);
						//}
						ready = true;		//Accept subscribing, now !
					}
				}
			}
			return null;
		}
	}
	/*
	 * Methods concerning cache management
	 * 	(Doume, 2013/02/15)
	 */
	/*
	 * Private method to update values in cache, and eventually notify all connected clients
	 * Parameter : result of Rest request (multiple stats)
	 */
	private int update_cache(JSONObject json_widget_state) {
		int updated_items = 0;
		Boolean to_process = false;
		int dev_id = 0;
		String skey = null;
		String Val = null;
		
		JSONArray itemArray = null;
		
		if(json_widget_state == null)
			return 0;
		//Tracer.i(mytag, "Cache update : stats result <"+json_widget_state.toString()+">");
		
		try {
			itemArray = json_widget_state.getJSONArray("stats");
			to_process = true;
		}catch (Exception e) {
			Tracer.i(mytag, "Cache update : No stats result !");
			return 0;
		}
		if(itemArray == null)
			return 0;
		mapView = null;
		for (int i =0; i < itemArray.length(); i++){
			//Retrieve Json infos
			try {
				dev_id = itemArray.getJSONObject(i).getInt("device_id");
			}catch (Exception e) {
				Tracer.i(mytag, "Cache update : No feature id ! ");
				to_process = false;
			}
			try {
				skey = itemArray.getJSONObject(i).getString("skey");
			} catch (Exception e) {
				skey = "_";
				Val = "0";
			}
			try {
				Val = itemArray.getJSONObject(i).getString("value");
			}catch (Exception e) {
				Val = "0";
			}
			// Try to put this in cache, now
			if(to_process) {
				Boolean item_updated = update_cache_device(dev_id,skey,Val);	//insert, update or ignore new value for this feature
				if(item_updated)
					updated_items++;
			}
			
		} // end of for loop on stats result
		if(mapView != null ) {
			//At least 1 mini widget has to be notified
			// send a 'group' notification to MapView !
			try {
				Tracer.i(mytag, "cache engine send a unique notification to MapView");
				mapView.sendEmptyMessage(9997);	//notify the group of widgets a new value is there
			} catch (Exception e) {}
		}
		mapView = null;
		Tracer.i(mytag, "cache size = "+cache.size());
		
		return updated_items;
	}
	/*
	 * Update device value in cache, and eventually notify clients about change
	 * This sequence must be protected against concurrent access
	 */
	public Boolean update_cache_device(int dev_id,String skey,String Val){
		if(cache == null)
			return false;
		
		//synchronized(this) {
			while(locked) {
				//Somebody else is updating list...
				//Tracer.i(mytag, "cache engine locked : wait !");
				try{
					Thread.sleep(100);		//Standby 10 milliseconds
				} catch (Exception e) {};
			}
			locked=true;	//Take the lock
			Boolean result = false;
			int cache_position = -1;
			
			cache_position = locate_device(dev_id,skey,last_position); // Try to retrieve it in cache from the last accessed position
																	   // because 'stats' return them always in the same order 
			if(cache_position >=0) {
				//device found
				last_position = cache_position;		//Keep the position, for next search
				if( (cache.get(cache_position).Value.equals(Val))) {
					//value not changed
					//Tracer.i(mytag, "cache engine no value change for ("+dev_id+") ("+skey+") ("+Val+")");
					
				} else {
					//value changed : has to notify clients....
					Tracer.i(mytag, "cache engine update value changed for ("+dev_id+") ("+skey+") ("+Val+")");
					cache.get(cache_position).Value = Val;
					result=true;
					if(cache.get(cache_position).clients_list != null) {
						for(int j = 0; j < cache.get(cache_position).clients_list.size(); j++) {
							//Notify each connected client
							Handler client = cache.get(cache_position).clients_list.get(j).getClientHandler();
							if(client != null) {
								cache.get(cache_position).clients_list.get(j).setValue(Val);	//update the session structure with new value
								if(cache.get(cache_position).clients_list.get(j).is_Miniwidget()) {
									// This client is a mapView's miniwidget
									// Don't' notify it immediately
									// A unique notification will be done by Handler, or higher level after all updates processed !
									mapView = client;
								} else {
									// It's not a mini_widget : notify it now
									try {
										Tracer.i(mytag, "cache engine send ("+Val+") to client <"+cache.get(cache_position).clients_list.get(j).getName()+">");
										client.sendEmptyMessage(9999);	//notify the widget a new value is ready for display
									} catch (Exception e) {}
								}
							} // test of valid client handler
						}
					}
					
				}
			} else {
				// device not yet exist in cache
				// when creating a new cache entry, it can't have clients !
				Tracer.i(mytag, "cache engine inserting ("+dev_id+") ("+skey+") ("+Val+")");
				Cache_Feature_Element device = new Cache_Feature_Element(dev_id,skey,Val);
				cache.add(device);
				result=true;
			}
			locked = false;
			return result;		
		//} // End protected bloc
	}
	private int locate_device(int dev_id,String skey,int from) {
		if(cache.size() == 0)
			return -1;		//empty cache
		int pos = from+1;
		if(pos >= cache.size() || pos < 0)
			pos=0;
			
		//Check if following entry in cache is the good one...
		for(int i = pos; i < cache.size(); i++) {
			if((cache.get(i).DevId == dev_id) && (cache.get(i).skey.equals(skey))) {
				return i;		//Bingo, the next one was the good one !
			}
		}
		// If here, it's because from the location 'from + 1', till end of cache, the device was not found !
		// Search from the beginning of table
		for(int i=0; i <= pos; i++) {
			if((cache.get(i).DevId == dev_id) && (cache.get(i).skey.equals(skey))) {
				return i;		//Bingo, found !
			}
		}
		return -1;	//Not found in cache !
	}
	
	/*
	 * Method offered to clients, to subscribe to a device/skey value-changed event
	 * 	The client must provide a Handler, to be notified
	 * 	Parameter : Object Entity_client containing references to device , and handler for callbacks
	 *  Result : false if subscription failed (already exist, or unknown device )
	 *  		 true : subscription accepted : Entity_client contains resulting state
	 */
	public Boolean subscribe (Entity_client client) {
		int device = -1;
		String skey = "";
		Boolean result = false;
		
		if(client == null)
			return result;
		Handler h = client.getClientHandler();
		if(h == null)
			return result;
		device = client.getDevId();
		skey = client.getskey();
		Tracer.i(mytag, "cache engine subscription requested by <"+client.getName()+"> Device ("+device+") ("+skey+")");
		if(! ready) {
			Tracer.i(mytag, "cache engine not yet ready : reject !");
			return result;
			
		}
		
		while(locked) {
			//Somebody else is updating list...
			//Tracer.i(mytag, "cache engine locked : wait !");
			try{
				Thread.sleep(100);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		locked=true;	//Take the lock
		
		for(int i = 0; i < cache.size(); i++) {
			if( (cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
				//found device in list
				client.setValue(cache.get(i).Value);	//return current stat value
				// Try to add this client to list
				
				cache.get(i).add_client(client);	//The client structure will contain also last known value for this device
				Tracer.i(mytag, "cache engine subscription done for <"+client.getName()+"> Device ("+device+") ("+skey+") Value : "+cache.get(i).Value);
				result = true;
				break;
			}
			// not the good one : check next
		}	//loop to search this device in cache
		locked=false;
		//dump_cache();
		return result;
	}
	
	public Boolean unsubscribe (Entity_client client) {
		int device = -1;
		String skey = "";
		Boolean result = false;
		
		if(client == null)
			return result;
		device = client.getDevId();
		skey = client.getskey();
		Tracer.i(mytag, "cache engine release subscription requested by <"+client.getName()+"> Device ("+device+") ("+skey+")");
		
		while(locked) {
			//Somebody else is updating list...
			try{
				Thread.sleep(10);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		locked=true;
		for(int i = 0; i < cache.size(); i++) {
			if( (cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
				//found device in list
				client.setValue(cache.get(i).Value);	//return current stat value
				// Try to remove this client from list
				cache.get(i).remove_client(client);
				Tracer.i(mytag, "cache engine release subscription OK for <"+client.getName()+"> Device ("+device+") ("+skey+")");
				result = true;
				break;
			}
			// not the good one : check next
			
		}	//loop to search this device in cache
		
		client.setClientId(-1);		//subscribing not located...
		locked=false;
		return result;
	}
	
	/*
	 * Some methods to help widgets for database access (they don't have anymore to connect to DomodroidDB !
	 * 
	 */
	public void descUpdate(int id,String new_desc) {
		domodb.updateFeaturename(id,new_desc);
	}
	/*
	 * This one allow MapView to clean all widgets from a map
	 */
	public void cleanFeatureMap(String map_name){
		domodb.cleanFeatureMap(map_name);
	}
	/*
	 * Obtain the list of feature located on a map
	 */
	public Entity_Map[]  getMapFeaturesList(String currentmap) {
		return domodb.requestFeatures(currentmap);
	}
	/*
	 * Obtain the list of map switches located on a map
	 */
	public Entity_Map[]  getMapSwitchesList(String currentmap) {
		return domodb.requestMapSwitches(currentmap);
	}
	public void insertFeatureMap(int id,int posx, int posy, String mapname) {
		domodb.insertFeatureMap(id, posx, posy, mapname);
	}
	public void remove_one_area(int id) {
		domodb.remove_one_area(id);
	}
	public void remove_one_room(int id) {
		domodb.remove_one_room(id);
	}
	public void remove_one_icon(int id) {
		domodb.remove_one_icon(id);
	}
	public void remove_one_feature(int id) {
		domodb.remove_one_feature(id);
	}
	public void remove_one_feature_association(int id) {
		domodb.remove_one_feature_association(id);
	}
	public void remove_one_FeatureMap(int id,int posx, int posy, String mapname) {
		domodb.remove_one_FeatureMap(id, posx, posy, mapname);
	}
	public Entity_Feature[] requestFeatures(){
		return domodb.requestFeatures();		
	}
	
}

