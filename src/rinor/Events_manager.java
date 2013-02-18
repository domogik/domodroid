package rinor;

import java.util.ArrayList;

import org.json.JSONObject;

import database.Cache_Feature_Element;
import misc.tracerengine;
import android.app.Activity;
import android.content.SharedPreferences;
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
		Tracer.w(mytag,"Events Manager created....connecting to Urlaccess : <"+urlAccess+">");
		
	}	//End of Constructor
	
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
