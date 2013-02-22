package rinor;

import java.util.Timer;
import java.util.TimerTask;

import database.WidgetUpdate.UpdateThread;
import misc.tracerengine;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

public class Stats_Com {
	private static Stats_Com instance = null;
	// Supported kinds of counters
	public static final int EVENTS_SEND = 1; 
	public static final int EVENTS_RCV  = 2; 
	public static final int STATS_SEND  = 3; 
	public static final int STATS_RCV   = 4; 
	//Cumulative counters
	private static int cumul_events_sent_packets = 0;
	private static int cumul_events_recv_packets = 0;
	private static int cumul_events_sent_bytes = 0;
	private static int cumul_events_recv_bytes = 0;
	
	private static int cumul_stats_sent_packets = 0;
	private static int cumul_stats_recv_packets = 0;
	private static int cumul_stats_sent_bytes = 0;
	private static int cumul_stats_recv_bytes = 0;
	
	//periodic counters
	private static int periodic_events_sent_packets = 0;
	private static int periodic_events_recv_packets = 0;
	private static int periodic_events_sent_bytes = 0;
	private static int periodic_events_recv_bytes = 0;
	
	private static int periodic_stats_sent_packets = 0;
	private static int periodic_stats_recv_packets = 0;
	private static int periodic_stats_sent_bytes = 0;
	private static int periodic_stats_recv_bytes = 0;
	
	public static int elapsed_period = 0;
	public static int period;				// Number of seconds between periodic clears
	private static Timer timer=null;
	
	/*******************************************************************************
	*		Internal Constructor
	*******************************************************************************/
	private Stats_Com()
	{
		super();
		if(period < 10)
			period = 10 * 60;	//10 minutes by default
		Timer();
	}

	public static Stats_Com getInstance() {
		if(instance == null) {
			instance = new Stats_Com();
		}
		
		return instance;
		
	}
	
	public void add(int type, int count) {
		switch(type) {
			case EVENTS_SEND :
				periodic_events_sent_packets++;	//1 packet more
				cumul_events_sent_packets++;
				periodic_events_sent_bytes += count;	//And N bytes more
				cumul_events_sent_bytes += count;
				break;
			case EVENTS_RCV :
				periodic_events_recv_packets++;	//1 packet more
				cumul_events_recv_packets++;
				periodic_events_recv_bytes += count;	//And N bytes more
				cumul_events_recv_bytes += count;
				break;
			case STATS_SEND :
				periodic_stats_sent_packets++;	//1 packet more
				cumul_stats_sent_packets++;
				periodic_stats_sent_bytes += count;	//And N bytes more
				cumul_stats_sent_bytes += count;
				break;
			case STATS_RCV :
				periodic_stats_recv_packets++;	//1 packet more
				cumul_stats_recv_packets++;
				periodic_stats_recv_bytes += count;	//And N bytes more
				cumul_stats_recv_bytes += count;
				break;
		}
	}
	public void clear() {
		periodic_events_sent_packets = 0;
		periodic_events_recv_packets = 0;
		periodic_events_sent_bytes = 0;
		periodic_events_recv_bytes = 0;
		
		periodic_stats_sent_packets = 0;
		periodic_stats_recv_packets = 0;
		periodic_stats_sent_bytes = 0;
		periodic_stats_recv_bytes = 0;
		elapsed_period = 0;
	}
	public String get_elapsed_period() {
		String result = "0 sec";
		if(! (elapsed_period == 0)) {
			if(elapsed_period < 60) {
				result = elapsed_period+" secs";
			} else {
				int minuts = elapsed_period / 60;
				int reste = elapsed_period - (minuts * 60);
				result = minuts+" mins "+reste+" secs";
			}
		}
		return result;
	}
	private void Timer() {
		timer = new Timer();
		
		TimerTask doAsynchronousTask = new TimerTask() {
		
			@Override
			public void run() {
						try {
							elapsed_period++;
							if(elapsed_period >= period) {
								clear();
							}
						} catch (Exception e) {
							//e.printStackTrace();
						}
					
			};
		};
		if(timer != null) {
			timer.schedule(doAsynchronousTask, 0, 1000);	// Once per second	
			
		}
	}
}
