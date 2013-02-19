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
import misc.tracerengine;

public class WidgetUpdate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SharedPreferences sharedparams;
	
	private boolean activated;
	private DomodroidDB domodb;
	private Handler sbanim;
	private Activity context;
	public  String mytag="WidgetUpdate";
	public  String owner="";
	private TimerTask doAsynchronousTask;
	private tracerengine Tracer = null;
	
	public ArrayList<Cache_Feature_Element> cache = new ArrayList<Cache_Feature_Element>();
	private Boolean locked = false;
	private Boolean timer_flag = false;
	public Boolean ready = false;
	private Handler mapView = null;
	public Events_manager eventsManager ;
	private static Handler myselfHandler ;
	private int last_ticket = -1;
	private int last_position = -1;
	
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
	@SuppressLint("HandlerLeak")
	public WidgetUpdate(tracerengine Trac, Activity context, Handler anim, SharedPreferences params, String owner){
		super();
		this.sharedparams=params;
		this.Tracer = Trac;
		this.context = context;
		this.owner = owner;
		mytag = "WidgetUpdate "+owner;
		activated = true;
		if(Tracer != null) {
			if(Tracer.DBEngine_running) {
				try {
					finalize();
				} catch (Throwable t) {}
			}
			Tracer.DBEngine_running = true;		//To avoid multiple engines running for same Activity
		}
		Tracer.d(mytag,"Initial start requested....");
		domodb = new DomodroidDB(Tracer, context);	
		domodb.owner=mytag;
		sbanim = anim;
		timer_flag = false;
		ready=false;
		Timer();		//and initiate the cyclic timer
		new UpdateThread().execute();	//And force an immediate refres
		Tracer.d(mytag,"state engine waiting for initial setting of cache !");
		
		Boolean said = false;
		while (! ready) {
			if(! said) {
				Tracer.d(mytag,"state engine not yet ready : Wait a bit !");
				said=true;
			}
			try{
				Thread.sleep(100);
			} catch (Exception e) {};
		}
		Tracer.d(mytag,"state engine ready !");
		// Cache contains list of existing devices, now !
		
		///////// Create an handler to exchange with Events_Manager///////////////////
		myselfHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				//This handler will receive notifications from Events_Manager
				// 1 message => 1 event : so, it's serialized !
				if(msg.what == 9900) {
					if(eventsManager != null) {
						Rinor_event event = eventsManager.get_event();
						if(event != null) {
							Tracer.d(mytag,"Event received from Events_Manager, ticket : "+event.ticket_id+" # "+event.item);
						
							if(last_ticket == -1) {
								last_ticket=event.item;	//Initial synchro on item
							} else {
								if(! ( (last_ticket + 1)== event.item)) {
									Tracer.d(mytag,"Handler event lost ? expected # "+(last_ticket + 1)+" Received # "+event.item);
								} 
								last_ticket=event.item;
							}
						
						
							mapView = null;
							update_cache_device(event.device_id,event.key,event.Value);
							if(mapView != null) {
								//It was a mini widget, not yet notified : do it now..
								try {
									Tracer.i(mytag, "Handler send a notification to MapView");
									mapView.sendEmptyMessage(9997);	//notify the group of widgets a new value is there
								} catch (Exception e) {}
							}
						} else {
							Tracer.d(mytag,"Null Event received from Events_Manager ! ! ! ");
							
						}
					} else {
						Tracer.d(mytag,"No Events_Manager known ! ! ! ");
					}
				}
			}
		};
		///////// and pass to it now///////////////////
		eventsManager = new Events_manager(Tracer, context, myselfHandler, cache, sharedparams, owner);
	}
	
	/* 
	 * Method allowing external methods to force a refresh
	 */
	public void refreshNow() {
		if(doAsynchronousTask != null)
			doAsynchronousTask.run();	//To force immediate refresh
	}
	
	/*
	 * This method should only be called once, to create and arm a cyclic timer 
	 */
	public void Timer() {
		final Timer timer = new Timer();
		
		
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
	 
	
	public void stopThread(){
		Tracer.d(mytag,"stopThread requested....stopping also events manager");
		activated = false;
		/*
		if(eventsManager != null) {
			eventsManager.alive=false;	// To force the ListenerThread to stop on next event
			eventsManager.cancel();
			eventsManager = null;
		}
		*/
	}
	public void restartThread(){
		Tracer.d(mytag,"restartThread requested....");
		activated = true;
		if(eventsManager == null) {
			Tracer.d(mytag,"restartThread ....create events manager");
			eventsManager = new Events_manager(Tracer, context, myselfHandler, cache, sharedparams, owner);
		} /* else {
			eventsManager.cancel();
			eventsManager = null;
			System.gc();
			Tracer.d(mytag,"restartThread ....re-create a new  events manager");
			eventsManager = new Events_manager(Tracer, context, myselfHandler, cache, sharedparams, owner);
		}*/
		
	}
	public void cancelEngine(){
		Tracer.d(mytag,"cancelEngine requested....");
		activated = false;
		if(eventsManager != null) {
			eventsManager.alive=false;
			eventsManager.cancel();
			eventsManager = null;
		}
		disconnect_all_clients();
		try {
			Tracer.DBEngine_running=false;
			Tracer.set_engine(null);
			finalize();
		} catch (Throwable e) {
			
		}
	}
	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// Added by Doume to correctly release resources when exiting
			if(! activated) {
				Tracer.d(mytag,"UpdateThread frozen....");
				
			} else {
				int count = 0;
				if(eventsManager != null)
					count=eventsManager.getAndResetEventCount();
				
				if(count > 0) {
					if(Tracer != null)
						Tracer.d(mytag,"Events detected since last loop = "+count+" No stats !");
					return null;
				}
					
					
				if(Tracer != null)
					Tracer.d(mytag,"Request to server for stats update...");
				if(sharedparams.getString("UPDATE_URL", null) != null){
					try {
						//sbanim.sendEmptyMessage(0);
						JSONObject json_widget_state = Rest_com.connect(sharedparams.getString("UPDATE_URL", null));
						//Tracer.d(mytag,"UPDATE_URL = "+ sharedparams.getString("UPDATE_URL", null));
						//Tracer.d(mytag,"result : "+ json_widget_state.toString());
						
						// new realtime engine : update cache with new values...
						int updated_items = update_cache(json_widget_state);
						// and continue to maintain local database
						if(updated_items > 0) {
							//domodb.insertFeatureState(json_widget_state);
						}
						ready = true;		//Accept subscribing, now !
					} catch (Exception e) {
						//sbanim.sendEmptyMessage(3);
						e.printStackTrace();
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
		
		return updated_items;
	}
	/*
	 * Update device value in cache, and eventually notify clients about change
	 * This sequence must be protected against concurrent access
	 */
	public Boolean update_cache_device(int dev_id,String skey,String Val){
		if(cache == null)
			return false;
		synchronized(this) {
			Boolean result = false;
			int cache_position = -1;
			
			cache_position = locate_device(dev_id,skey,last_position); // Try to retrieve it in cache from the last accessed position
																	   // because 'stats' return them always in the same order 
			if(cache_position >=0) {
				//device found
				last_position = cache_position;		//Keep the position, for next search
				if( (cache.get(cache_position).Value.equals(Val))) {
					//value not changed
					Tracer.i(mytag, "cache engine no value change for ("+dev_id+") ("+skey+") ("+Val+")");
					
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
							}
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
			return result;		
		} // End protected bloc
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
		
		
		if(client == null)
			return false;
		device = client.getDevId();
		skey = client.getskey();
		Tracer.i(mytag, "cache engine subscription requested by <"+client.getName()+"> Device ("+device+") ("+skey+")");
		if(! ready) {
			Tracer.i(mytag, "cache engine not yet ready : reject !");
			return false;
			
		}
		while(locked) {
			//Somebody else is updating list...
			try{
				Thread.sleep(10);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		locked=true;	//Take the lock
		for(int i = 0; i < cache.size(); i++) {
			if( (cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
				//found device in list
				client.setValue(cache.get(i).Value);	//return current stat value
				// Try to add this client to list
				if(client.getClientHandler() == null)
					return false;
				cache.get(i).add_client(client);	//The client structure contains also last known value for this device
				Tracer.i(mytag, "cache engine subscription done for <"+client.getName()+"> Device ("+device+") ("+skey+") Value : "+cache.get(i).Value);
				locked=false;
				return true;
			}
			// not the good one : check next
			
		}	//loop to search this device in cache
		// device not yet exist in cache
		locked=false;
		return false;
	}
	
	public Boolean unsubscribe (Entity_client client) {
		int device = -1;
		String skey = "";
		
		
		if(client == null)
			return false;
		device = client.getDevId();
		skey = client.getskey();
		Tracer.i(mytag, "cache engine release subscription requested by <"+client.getName()+"> Device ("+device+") ("+skey+")");
		
		while(locked) {
			//Somebody else is updating list...
			try{
				Thread.sleep(10);		//Standby 10 milliseconds
			} catch (Exception e) {};
		}
		for(int i = 0; i < cache.size(); i++) {
			if( (cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
				//found device in list
				client.setValue(cache.get(i).Value);	//return current stat value
				// Try to remove this client from list
				cache.get(i).remove_client(client);
				Tracer.i(mytag, "cache engine release subscription done for <"+client.getName()+"> Device ("+device+") ("+skey+")");
				locked=false;
				return true;
			}
			// not the good one : check next
			
		}	//loop to search this device in cache
		client.setClientId(-1);		//subscribing not located...
		locked=false;
		return false;
		
	}
	
	private void disconnect_all_clients() {
		//release all pending subscribing (engine itself will die !)
		for(int i = 0; i < cache.size(); i++) {
			if(cache.get(i).clients_list != null) {
				for(int j = 0; j < cache.get(i).clients_list.size(); j++) {
					//Notify each connected client
					Handler client = cache.get(i).clients_list.get(j).getClientHandler();
					if(client != null) {
						cache.get(i).clients_list.get(j).setClientId(-1);	//note client as not connected
						try {
							Tracer.i(mytag, "cache engine send disconnected to client <"+cache.get(i).clients_list.get(j).getName()+">");
							client.sendEmptyMessage(9998);	//notify the widget with disconnect
						} catch (Exception e) {}
					}
				}
			}
		}
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
	public void insertFeatureMap(int id,int posx, int posy, String mapname) {
		domodb.insertFeatureMap(id, posx, posy, mapname);
				
	}
	public void removeFeatureMap(int id,int posx, int posy, String mapname) {
		domodb.removeFeatureMap(id, posx, posy, mapname);
	}
	public Entity_Feature[] requestFeatures(){
		return domodb.requestFeatures();		
	}
	
}

