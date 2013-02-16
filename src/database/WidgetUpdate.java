package database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import rinor.Rest_com;
import widgets.Entity_client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import misc.tracerengine;

public class WidgetUpdate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SharedPreferences sharedparams;
	private static WidgetUpdate instance;
	
	private boolean activated;
	private DomodroidDB domodb;
	private Handler sbanim;
	private String mytag="WidgetUpdate";
	private TimerTask doAsynchronousTask;
	private tracerengine Tracer = null;
	//private static Handler handler = null;
	
	private ArrayList<Cache_Feature_Element> cache = new ArrayList<Cache_Feature_Element>();
	private Boolean locked = false;
	private Boolean timer_flag = false;
	
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
	public WidgetUpdate(tracerengine Trac, Activity context, Handler anim, SharedPreferences params){
		this.sharedparams=params;
		this.Tracer = Trac;
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
		Timer();		//and initiate the cyclic timer
		refreshNow();	// Force an immediate refresh
		
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
		if(timer != null)
			timer.schedule(doAsynchronousTask, 0, sharedparams.getInt("UPDATE_TIMER", 300)*1000);
	}
	 
	
	public void stopThread(){
		Tracer.d(mytag,"stopThread requested....");
		activated = false;
		
	}
	public void restartThread(){
		Tracer.d(mytag,"restartThread requested....");
		activated = true;
		Timer();
		refreshNow();
	}
	public void cancelEngine(){
		Tracer.d(mytag,"cancelEngine requested....");
		activated = false;
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
							domodb.insertFeatureState(json_widget_state);
						}
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
		
		return updated_items;
	}
	
	public Boolean update_cache_device(int dev_id,String skey,String Val){
		if(cache == null)
			return false;
		for(int i = 0; i < cache.size(); i++) {
			if( (cache.get(i).DevId == dev_id) && (cache.get(i).skey.equals(skey))) {
				//found device in list
				if( (cache.get(i).Value.equals(Val))) {
					//value not changed
					//Tracer.i(mytag, "cache engine update same value for ("+dev_id+") ("+skey+") ("+Val+")");
					
					return false;
				} else {
					//value changed : has to notify clients....
					Tracer.i(mytag, "cache engine update value changed for ("+dev_id+") ("+skey+") ("+Val+")");
					cache.get(i).Value = Val;
					
					if(cache.get(i).clients_list != null) {
						for(int j = 0; j < cache.get(i).clients_list.size(); j++) {
							//Notify each connected client
							Handler client = cache.get(i).clients_list.get(j).getClientHandler();
							if(client != null) {
								cache.get(i).clients_list.get(j).setValue(Val);	//update the session structure with new value
								try {
									Tracer.i(mytag, "cache engine send ("+Val+") to client <"+cache.get(i).clients_list.get(j).getName()+">");
									client.sendEmptyMessage(9999);	//notify the widget a new value is ready for display
								} catch (Exception e) {}
							}
						}
					}
					
					return true;
				}
			}
			// not the good one : check next
			
		}	//loop to search this device in cache
		
		// device not yet exist in cache
		Tracer.i(mytag, "cache engine inserting ("+dev_id+") ("+skey+") ("+Val+")");
		Cache_Feature_Element device = new Cache_Feature_Element(dev_id,skey,Val);
		cache.add(device);
		return true;		// when creating new, it can't have clients !
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
	
}

