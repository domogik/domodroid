package rinor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import database.Cache_Feature_Element;
import database.JSONParser;
import database.WidgetUpdate.UpdateThread;
import misc.tracerengine;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;

public class Events_manager {
	private tracerengine Tracer;
	private Activity context;
	private SharedPreferences params;
	private Handler state_engine_handler;
	private ArrayList<Cache_Feature_Element> engine_cache;
	private int next_to_extract = 0;
	private int next_event = 0;
	private Boolean endloop = false;
	private int stack_size = 500;
	private String mytag = "Events_manager";
	private String urlAccess;
	private ListenerThread listener = null;
	private Boolean alive = false;
	
	private JSONObject[] event_stack = new JSONObject[stack_size];
	
	public Events_manager(tracerengine Trac, Activity context, 
			Handler state_engine_handler, 
			ArrayList<Cache_Feature_Element> engine_cache,
			SharedPreferences params
			) {
		super();
		this.Tracer = Trac;
		this.context = context;
		this.state_engine_handler = state_engine_handler;
		this.engine_cache =  engine_cache;
		this.params = params;
		urlAccess = params.getString("URL","1.1.1.1");
		urlAccess = urlAccess.replaceAll("[\r\n]+", "");
		//Try to solve #1623
		urlAccess = urlAccess.replaceAll(" ", "%20");
		//The engine cache should already contain a list of devices features
		Tracer.w(mytag,"Events Manager created....start background task for events listening");
		try {
			listener = new ListenerThread();
			if(listener != null)
				listener.execute();
		} catch (Exception e) {
			Tracer.w(mytag,"Events Manager exeception starting background task : Abort event engine");
			e.printStackTrace();
			try {
				this.finalize();
			} catch (Throwable t) {}
		}
	}	//End of Constructor
	
	public void cancel() {
		alive = false;
		if(listener != null)
			listener = null;
	}
	public class ListenerThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			alive = true;
			// First, construct the event request
			String ticket_request = urlAccess+"events/request/new";
			for(int i = 0; i < engine_cache.size(); i++) {
				String skey = engine_cache.get(i).skey;
				if(! (skey.equals("_") && ! (skey.equals("command"))) ) {
					ticket_request+="/";
					ticket_request+= engine_cache.get(i).DevId;
				}
			}
			//And send it to server....to create an event ticket
			String request = ticket_request;
			JSONObject event = null;
			Boolean ack = false;
			while(alive) {
				try {
					Tracer.w(mytag,"Requesting server <"+request+">");
					event = Rest_com.connect(request);		//Blocking request : we must have an answer to continue...
					Tracer.w(mytag,"Received event = <"+event.toString()+">");
				} catch (Exception e) {
					Tracer.e(mytag,"Exception on wait for event ! ! !");
					e.printStackTrace();
					alive=false;
					break;
				}
				
				try {
					ack = JSONParser.Ack(event);
				} catch (Exception e) {
					ack=false;
				}
				if(ack==false){
					// The server's response is'nt "OK"
					Tracer.w(mytag,"Event ERROR <"+event.toString()+">");
					alive=false;
					break;
				} else {
					//An event is available...
					Tracer.w(mytag,"Processing event");
					// First, take the ticket ID to resubmit an event request....
					JSONArray json_ValuesList = null;
					int list_size = 0;
	                if(event != null) {
	                	String ticket = "";
	                	String device_id = "";
	                	JSONObject objectest;
	                	JSONArray arraytest;
	                	try {
	                		list_size = event.getJSONArray("event").length();
	                	} catch (Exception e) {}
	                	
						for(int i = 0; i < list_size; i++) {
							try {
								ticket = event.getJSONArray("event").getJSONObject(i).getString("ticket_id");
								Tracer.w(mytag,"Ticket = "+ticket);
								device_id = event.getJSONArray("event").getJSONObject(i).getString("device_id");
								Tracer.w(mytag,"Device_id = "+device_id);
								//json_ValuesList = event.getJSONArray("event").getJSONObject(i).getJSONObject("data").getJSONArray("value");
								int data_size = event.getJSONArray("event").getJSONObject(i).getJSONArray("data").length();
								for(int j = 0; j < data_size; j++) {
									try {
										String New_Key =event.getJSONArray("event").getJSONObject(i).getJSONArray("data").getJSONObject(j).getString("key");
										String New_Value = event.getJSONArray("event").getJSONObject(i).getJSONArray("data").getJSONObject(j).getString("value");
										Tracer.w(mytag,"Device_id = "+New_Key);										
										Tracer.w(mytag,"Device_id = "+New_Value);
										} catch (Exception e){
											//je sais pas quoi mettre dans les catch :)
										}
									}
								//Tracer.w(mytag,"ValuesList <"+json_ValuesList.toString()+">");
								
							} catch (Exception e) {
								// Cannot parse JSON Array or JSONObject
								 Tracer.d("Dialog_Synchronize","Exception processing event ("+i+")");
									
							}
							
						}
	                }
				}
				
			}
			
			//Should never reach the end of thread !!!!
			Tracer.e(mytag,"ListenerThread going down !!!!!!!!!!!!!!!!!");
			return null;
		}
	}
	/*
	 * Fill stack with events received from server
	 */
	private int put_event(JSONObject event) {
		event_stack[next_event] = event;
		Tracer.w(mytag,"Event stored at position : "+next_event);
		next_event++;
		if(next_event >= stack_size) {
			next_event = 0;
			endloop = true;
		}
		return 0;
	}
	
	/*
	 * Notify WidgetUpdate that some JSONObject is available in stack
	 */
	private void notify_engine() {
		if(state_engine_handler != null) {
			state_engine_handler.sendEmptyMessage(9900);
		}
	}
	/*
	 * This method works only if one client extracts elements....
	 */
	private JSONObject get_event() {
		Boolean ok = false;
		int to_return = 0;
		
		if(! endloop) {
			if (next_to_extract < next_event) {
				//We're not overpassing entries
				to_return = next_to_extract;
				ok=true;
			} else {
				// No available event...
				return null;
			}
		} else {
			// we're near end of stack, but next_event is back to beginning
			ok=true;
			to_return = next_to_extract;
		}
		if(ok) {
			next_to_extract++;
			if(next_to_extract >= stack_size) {
				next_to_extract = 0;		//restart from beginning of stack
				endloop = false;
			}
			Tracer.w(mytag,"Event extracted from position : "+to_return);
			return event_stack[to_return];
		} else {
			Tracer.w(mytag,"No available event ! ");
			return null;
		}
	}
}
