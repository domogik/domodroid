package rinor;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.TimerTask;

import org.json.JSONObject;

import database.Cache_Feature_Element;
import database.JSONParser;
import database.WidgetUpdate;
import misc.tracerengine;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Events_manager {
	private static Events_manager instance;
	private tracerengine Tracer;
	private SharedPreferences params;
	public Handler state_engine_handler;
	public Handler events_engine_handler;
	private WidgetUpdate father = null;
	private ArrayList<Cache_Feature_Element> engine_cache;
	private int stack_in = -1;
	private int stack_out = -1;
	private int event_item = 0;
	private int stack_size = 500;
	private String mytag ="Events";
	private String urlAccess;
	private ListenerThread listener = null;
	public Boolean alive = false;
	public Boolean cache_out_of_date = false;
	private int events_seen = 0;
	TimerTask doAsynchronousTask = null;
	private Boolean listener_running = false;
	private Boolean init_done = false;
	private Boolean com_broken = false;
	private Boolean sleeping = false;
	private String login;
	private String password;
	private float api_version;
	
	private Rinor_event[] event_stack = new Rinor_event[stack_size];
	private static Stats_Com stats_com = null; 
	
	/*******************************************************************************
	*		Internal Constructor
	*******************************************************************************/
		private Events_manager()
		{
			super();
			stats_com = Stats_Com.getInstance();	//Create a statistic counter, with all 0 values
			com_broken=false;
		}
	
	public static Events_manager getInstance() {
		if(instance == null) {
			Log.e("Events_Manager", "Creating instance........................");
			instance = new Events_manager();
		}
		return instance;
		
	}
	public void init(tracerengine Trac, 
			Handler state_engine_handler, 
			ArrayList<Cache_Feature_Element> engine_cache,
			SharedPreferences params,
			WidgetUpdate caller) {	
		
		if(init_done)
			return;
		
		this.Tracer = Trac;
		this.engine_cache =  engine_cache;
		this.params = params;
		this.father = caller;
		//setOwner(owner, state_engine_handler);
		mytag="Events";
		urlAccess = params.getString("URL","1.1.1.1");
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	api_version=params.getFloat("API_VERSION", 0);
    	
		//The father's cache should already contain a list of devices features
		this.state_engine_handler = state_engine_handler;
		Tracer.w(mytag,"Events Manager initialized");
		if(listener == null) {
			Tracer.w(mytag,"....start background task for events listening");
			sleeping = false;
			start_listener();
		}
		events_engine_handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 9999) {
					listener = null;	//It's dead
					stats_com = null; 	//let the instance to stop
					init_done = false;
					Tracer.w(mytag,"Events Manager really ending");
					
					try {
						this.finalize();
					} catch (Throwable t) {
						
					}
				}
			};
		};
		Tracer.w(mytag,"Events Manager ready");
		init_done = true;
	}	//End of Constructor
	
	public void set_sleeping() {
		Tracer.d(mytag,"Pause requested...");
		sleeping=true;
		/*
		if(stats_com != null)		//Already done by cache engine !
			stats_com.set_sleeping();
			*/
	}
	public void wakeup() {
		Tracer.d(mytag,"Wake up requested...");
		sleeping=false;
	}
	/*
	 * Request to release all resources and die (when Main is onDestroy ! )
	 */
	public void Destroy() {
		alive = false;		//The end of loop into listener will generate a message
							// to local handler, to complete the destroy
		Tracer.d(mytag,"events engine Destroy() requested : Let the Listener thread to exit !");
		
	}
	
	private void start_listener() {
		if(listener == null) {
			Runnable myrunnable = new Runnable() {
					public void run() {
						try {
								listener = new ListenerThread();
								listener.execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						
					} 
			}; //End of runnable
			
			Thread mylistener = new Thread(myrunnable);
			mylistener.run();
		}
	}
	
	
	public class ListenerThread extends AsyncTask<Void, Integer, Void>{
		
		public void cancel() {
			
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			alive = true;
			sleeping = false;
			if(listener_running) {
				Tracer.e(mytag,"One ListenerThread is already running");
				return null;
			}
			listener_running = true;
			if(stats_com == null)
				stats_com = Stats_Com.getInstance();
			stats_com.wakeup();		//Engine is running... 
			// First, construct the event request
			if(engine_cache.size() == 0) {
				Tracer.e(mytag,"Empty WidgetUpdate cache : cannot create ticket : ListenerThread aborted ! ! !");
				return null;
			}
			//Build the list of devices concerned by ticket request
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
			int counter_max = 5 * 60 * 1000;		//5 minutes max between 2 retry
			int counter_current = 0;			// 0 + 10 seconds per loop, at beginning
			int loop_time = 10000;			//10 seconds per wait at beginning, and grow it till counter_max
			int max_time_for_ticket = 110 * 1000;	//After 1'50 , ticket is dead... So, prepare to recreate another !
			int sleep_time = 2 * 1000;				//When sleeping, check every 2 seconds
			int sleep_duration = 0;
			
			com_broken=false;
			while(alive) {
				while(sleeping) {
					try {
						Thread.sleep(sleep_time);	//Wait for 2s
					} catch (Throwable t) {}
					sleep_duration+=sleep_time;
					if(sleep_duration > max_time_for_ticket) {
						cache_out_of_date = true;
						request = ticket_request;
						com_broken = false;		//Retry immediatly to reconnect
					}
				}
				// Exit from sleep loop ( wake up requested )
				// If sleep was less 1'50, the ticket is still alive
				// Otherwise, the next request will be a get new ticket
				sleep_duration = 0;		// for next sleep.....
				
				if(com_broken) {
					//Link is probably broken... Wait a bit before to re-submit a request to server
					counter_current += loop_time;
					if(counter_current > counter_max)
						counter_current = counter_max;
					
					Tracer.e(mytag,"ListenerThread waiting "+(counter_current/1000)+" seconds before error recovery retry");
					try {
						Thread.sleep(counter_current);	//Wait for 10s, 20s, 30s, ... (max 5 minutes)
					} catch (Throwable t) {}
					//And try to reconnect
					
				}
				int error = 1;
				
				// Try to connect to server and send request
				stats_com.add(Stats_Com.EVENTS_SEND, request.length());
				Tracer.w(mytag,"Requesting server <"+request+">");
				try {
					event = Rest_com.connect_jsonobject(request,login,password);		//Blocking request : we must have an answer to continue...
					error=0;
				} catch (Exception e) {
					error = 1;
					Tracer.e(mytag,"Rinor error : <"+e.getMessage()+">");
				} catch (Throwable t) {
					error = 2;
					Tracer.e(mytag,"Rinor Throwable error ");
				} finally {
					
				}
				
				if(error != 0) {
					Tracer.e(mytag,"Exception Error ==> Network probably not yet ready !");
					com_broken = true;		//Next retry has to be delayed, waiting for an operational link....
					request = ticket_request;	//Having detected a broken link, the current ticket with server is probably lost
												//Create a new one !
					
				} else {
					//No error
					stats_com.add(Stats_Com.EVENTS_RCV, event.toString().length());
					counter_current = 0;	//One packet received : the link is operational !
					com_broken = false;		//no need to temporize before next send...
					
					try {
						ack = JSONParser.Ack(event);
					} catch (Exception e) {
						ack=false;
					}
					if(ack==false){
						// The server's response is'nt "OK"
						Tracer.w(mytag,"Event ERROR <"+event.toString()+"> : ignored !");
						
					} else {
						//An event is available...
						//Tracer.w(mytag,"Processing event");
						
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
										Tracer.w(mytag,"Timeout received !");
										notify_engine(9902); //Time out seen
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
											Tracer.w(mytag,"event ready : Ticket = "+ticket+" Device_id = "+device_id+" Key = "+New_Key+" Value = "+New_Value);
											event_item++;
											Rinor_event to_stack = new Rinor_event(Integer.parseInt(ticket), event_item, Integer.parseInt(device_id), New_Key, New_Value);
											put_event(to_stack);	//Put in stack, and notify cache engine
										} catch (Exception e){
											Tracer.e(mytag,"Malformed data entry ?????????????????");
										}
									}
									
							} // End of loop on event array
		                }	// if event not null
					}	// if ack
				}	// if error
				
			}	//Infinite loop of thread
			
			// should never reach this code.....
			Tracer.e(mytag,"ListenerThread going down !!!!!!!!!!!!!!!!!");
			listener_running = false;
			if(state_engine_handler != null) {
				state_engine_handler.sendEmptyMessage(9901);	//I'm going down....
			}
			// Try to free the ticket, if available
			if(! ticket.equals("")) {
				request = urlAccess+"events/request/free/"+ticket;	//Use the ticket #
				try {
					Tracer.w(mytag,"Freeing ticket <"+request+">");
					stats_com.add(Stats_Com.EVENTS_SEND, request.length());
					event = Rest_com.connect_jsonobject(request,login,password);		//Blocking request : we must have an answer to continue...
					stats_com.add(Stats_Com.EVENTS_RCV, event.length());
					Tracer.w(mytag,"Received on free ticket = <"+event.toString()+">");
				} catch (Exception e) {
					
				}
				
			}
			events_engine_handler.sendEmptyMessage(9999);	//Notify main thread to die
			listener=null;
			return null;	//And die myself
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
				notify_engine(9900); //An event is available
			} else {
				Tracer.w(mytag,"stack is full ! ! !  Event will be lost");
			
			}
		//}	// protected bloc
	}
	/*
	 * This method works only if one client extracts elements....( WidgetUpdate handler, normally)
	 */
	public Rinor_event get_event() {
			stack_out++;
			if(stack_out >= stack_size)
				stack_out = 0;
			if(event_stack[stack_out] == null) {
				//Tracer.w(mytag,"Stack is empty @ "+stack_out);
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
		if(alive) {
			if(state_engine_handler != null) {
				state_engine_handler.sendEmptyMessage(what);
			} else {
				Tracer.w(mytag,"No handler to notify for "+stack_out);
			}
		} else {
			Tracer.w(mytag,"Trying to notify father for "+stack_out+" , while ListenerThread is in Pause state...");
		}
	}
	
}
