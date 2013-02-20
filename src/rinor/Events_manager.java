package rinor;

import java.util.ArrayList;
import java.util.TimerTask;

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
	private int stack_in = -1;
	private int stack_out = -1;
	private int event_item = 0;
	private int stack_size = 500;
	private String mytag ;
	private String urlAccess;
	private ListenerThread listener = null;
	public Boolean alive = false;
	private int events_seen = 0;
	TimerTask doAsynchronousTask = null;
	private Boolean listener_running = false;
	
	private Rinor_event[] event_stack = new Rinor_event[stack_size];
	
	public Events_manager(tracerengine Trac, Activity context, 
			Handler state_engine_handler, 
			ArrayList<Cache_Feature_Element> engine_cache,
			SharedPreferences params,
			String owner) {
		super();
		this.Tracer = Trac;
		this.context = context;
		this.state_engine_handler = state_engine_handler;
		this.engine_cache =  engine_cache;
		this.params = params;
		mytag="Events_manager "+owner;
		urlAccess = params.getString("URL","1.1.1.1");
		urlAccess = urlAccess.replaceAll("[\r\n]+", "");
		//Try to solve #1623
		urlAccess = urlAccess.replaceAll(" ", "%20");
		//The engine cache should already contain a list of devices features
		Tracer.w(mytag,"Events Manager created....start background task for events listening");
		if(listener == null) {
			Runnable myrunnable = new Runnable() {
					public void run() {
						try {
								new ListenerThread().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						
					} 
			}; //End of runnable
			
			listener = new ListenerThread();
			Thread mylistener = new Thread(myrunnable);
			mylistener.run();
		}
		Tracer.w(mytag,"Events Manager ready");
		
	}	//End of Constructor
	
	public void cancel() {
		Tracer.w(mytag,"cancel requested !");
		alive = false;
		if(listener != null) {
			listener.cancel(true);
			listener = null;
			listener_running = false;
		}
		try {
			finalize();
		} catch (Throwable t) {}
		
	}
	public class ListenerThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			alive = true;
			if(listener_running) {
				Tracer.e(mytag,"One ListenerThread is already running");
				
				return null;
			}
			listener_running = true;
			
			// First, construct the event request
			if(engine_cache.size() == 0) {
				Tracer.e(mytag,"Empty WidgetUpdate cache : cannot create ticket : ListenerThread aborted ! ! !");
				return null;
			}
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
			Tracer.e(mytag,"ListenerThread starts the loop");
			String ticket = "";
        	
			while(alive) {
				try {
					Tracer.w(mytag,"Requesting server <"+request+">");
					event = Rest_com.connect(request);		//Blocking request : we must have an answer to continue...
					Tracer.w(mytag,"Received event = <"+event.toString()+">");
				} catch (Exception e) {
					Tracer.e(mytag,"Exception on wait for event ! ! ! Socket disconnected ?");
					alive=false;
					break;
				}
				
				if(! alive) {
					break;		//The father asks to die...
				}
				try {
					ack = JSONParser.Ack(event);
				} catch (Exception e) {
					ack=false;
				}
				if(ack==false){
					// The server's response is'nt "OK"
					Tracer.w(mytag,"Event ERROR <"+event.toString()+"> : ignored !");
					//alive=false;		// will stop the event engine..
					//break;
				} else {
					//An event is available...
					Tracer.w(mytag,"Processing event");
					// First, take the ticket ID to resubmit an event request....
					
					int list_size = 0;
	                if(event != null) {
	                	String device_id = "";
	                	try {
	                		list_size = event.getJSONArray("event").length();
	                	} catch (Exception e) {
	                		Tracer.w(mytag,"Very strange message, ignored !");
							request=ticket_request;
	                		break;
	                	}
	                	ticket="";
	                	// Process the event array
						for(int i = 0; i < list_size; i++) {
								try {
									ticket = event.getJSONArray("event").getJSONObject(i).getString("ticket_id");
								} catch (Exception e) {
									Tracer.w(mytag,"Wrong event : No ticket !");
									request = ticket_request;	//Create a new ticket on next query, now !
									break;
								}
								if( (ticket != null) && (! ticket.equals("")))
									request = urlAccess+"events/request/get/"+ticket;	//Use the ticket on next query
								else {
									ticket="";
									request = ticket_request;	//Create a new ticket on next query
								}
								events_seen++;
								try {
									device_id = event.getJSONArray("event").getJSONObject(i).getString("device_id");
								} catch (Exception e) {
									//No device_id : it's a timeout
									Tracer.w(mytag,"It's a timeout !");
									break;		//Force to redo the loop from while(alive)
								}
								//json_ValuesList = event.getJSONArray("event").getJSONObject(i).getJSONObject("data").getJSONArray("value");
								int data_size = 0;
								try {
									data_size = event.getJSONArray("event").getJSONObject(i).getJSONArray("data").length();
								} catch (Exception e) {
									data_size = 0;	//No data ==> no values to process !
								}
								for(int j = 0; j < data_size; j++) {
									try {
										String New_Key =event.getJSONArray("event").getJSONObject(i).getJSONArray("data").getJSONObject(j).getString("key");
										String New_Value = event.getJSONArray("event").getJSONObject(i).getJSONArray("data").getJSONObject(j).getString("value");
										Tracer.w(mytag,"event to stack  : Ticket = "+ticket+" Device_id = "+device_id+" Key = "+New_Key+" Value = "+New_Value);
										event_item++;
										Rinor_event to_stack = new Rinor_event(Integer.parseInt(ticket), event_item, Integer.parseInt(device_id), New_Key, New_Value);
										put_event(to_stack);
										notify_engine(9900); //An event is available
										alive=true;
									} catch (Exception e){
										Tracer.w(mytag,"Malformed data entry ?????????????????");
									}
								}
								
						} // End of loop on event array
	                }	// if event not null
				}	// if ack
				
			}	//Infinite loop on alive
			
			//Should never reach the end of thread !!!!
			Tracer.e(mytag,"ListenerThread going down !!!!!!!!!!!!!!!!!");
			listener_running = false;
			notify_engine(9901); //Listener down
			// Try to free the ticket, if available
			if(! ticket.equals("")) {
				request = urlAccess+"events/request/free/"+ticket;	//Use the ticket #
				try {
					Tracer.w(mytag,"Freeing ticket <"+request+">");
					event = Rest_com.connect(request);		//Blocking request : we must have an answer to continue...
					Tracer.w(mytag,"Received on free = <"+event.toString()+">");
				} catch (Exception e) {
					
				}
				
			}
			listener=null;
			return null;
		}
	}
	/*
	 * Fill stack with events received from server (by ListenerThread)
	 */
	private void put_event(Rinor_event event) {
		//synchronized(this) {
			stack_in++;
			if(stack_in >= stack_size)
				stack_in = 0;
			if(event_stack[stack_in] == null) {
				//Position is free !
				Tracer.w(mytag,"Event stacked at :"+stack_in);
				event_stack[stack_in] = event;
			} else {
				Tracer.w(mytag,"stack is full ! ! !  Event will be lost");
			
			}
		//}	// protected bloc
	}
	/*
	 * This method works only if one client extracts elements....( WidgetUpdate handler, normally
	 */
	public Rinor_event get_event() {
		//synchronized(this) {
			stack_out++;
			if(stack_out >= stack_size)
				stack_out = 0;
			if(event_stack[stack_out] == null) {
				Tracer.w(mytag,"Stack is empty @ "+stack_out);
				stack_out--;
				if(stack_out < 0) {
					stack_out = stack_size;
				}
				return null;
			} else {
				Rinor_event result = event_stack[stack_out];
				Tracer.w(mytag,"Event unstacked from "+stack_out);
				event_stack[stack_out] = null;		//free the entry
				return result;
			}
		//} // protected bloc
	}
	public int getAndResetEventCount() {
		int	count = events_seen;
		events_seen = 0;
		return count;
	}
	/*
	 * Notify WidgetUpdate that some Rinor_event is available in stack
	 */
	private void notify_engine(int what) {
		if(state_engine_handler != null) {
			state_engine_handler.sendEmptyMessage(what);
		}
	}
	
}
