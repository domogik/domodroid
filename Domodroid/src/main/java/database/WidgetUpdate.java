package database;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import Abstract.pref_utils;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.ConnectivityChangeEvent;
import Event.Event_base_message;
import applications.domodroid;
import misc.tracerengine;
import mq.ZMQReqMessage;
import rinor.Events_manager;
import rinor.Rest_com;
import rinor.Rinor_event;
import rinor.Stats_Com;

//import com.orhanobut.logger.Logger;

public class WidgetUpdate {
    //implements Serializable {
    private static WidgetUpdate instance;
    private static final long serialVersionUID = 1L;

    private boolean activated;
    private static Activity activity;
    private static DomodroidDB domodb;
    private final String mytag = this.getClass().getName();
    private TimerTask doAsynchronousTask;
    private tracerengine Tracer = null;

    private ArrayList<Cache_Feature_Element> cache = new ArrayList<>();
    private Boolean locked = false;
    private Boolean timer_flag = false;
    private Boolean ready = false;
    public Events_manager eventsManager = null;
    private static Handler myselfHandler = null;
    private int last_ticket = -1;
    private int last_position = -1;
    private Timer timer = null;
    private int callback_counts = 0;
    private Boolean init_done = false;

    private Boolean sleeping = false;
    private static Stats_Com stats_com = null;
    private String login;
    private String password;
    private Boolean SSL;
    private float api_version;
    private String last_device_update;

    //
    // Table of handlers to notify
    // pos 0 = Main
    // pos 1 = Map
    // pos 2 = MapView
    private static final Handler[] parent = new Handler[3];
    private pref_utils prefUtils;

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
     * Internal Constructor
     *******************************************************************************/
    private WidgetUpdate() {
        super();
        //subscribe to network change to restart events_manager or stop MQ listening.
        EventBus.getDefault().register(this);
        //com.orhanobut.logger.Logger.init("WidgetUpdate").methodCount(0);

    }

    public static WidgetUpdate getInstance() {
        if (instance == null) {
            Log.i("WidgetUpdate", "Creating instance........................");
            instance = new WidgetUpdate();
        }
        return instance;

    }

    public Boolean init(tracerengine Trac, final Activity activity) {
        Boolean result;
        if (init_done) {
            Log.w("WidgetUpdate", "init already done");
            return true;
        }
        stats_com = Stats_Com.getInstance();    //Create a statistic counter, with all 0 values
        sleeping = false;
        prefUtils = new pref_utils();
        this.Tracer = Trac;
        WidgetUpdate.activity = activity;
        activated = true;
        login = prefUtils.GetRestAuthUsername();
        password = prefUtils.GetRestAuthPassword();
        api_version = prefUtils.GetDomogikApiVersion();
        last_device_update = prefUtils.GetLastDeviceUpdate();
        String last_sensor_update = prefUtils.GetLastSensorUpdate();
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
        Tracer.d(mytag, "Initial start requested....");
        domodb = DomodroidDB.getInstance(Tracer, activity);
        domodb.owner = mytag;
        timer_flag = false;
        ready = false;
        Tracer.d(mytag, "cache engine starting timer for periodic cache update");
        Timer();        //and initiate the cyclic timer

        //Commented to avoid launching 2 request one from the initial timer and the other below
        //new UpdateThread().execute();    //And force an immediate refresh on init

        this.callback_counts = 0;    //To force a refresh
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
        myselfHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //This handler will receive notifications from Events_Manager and from waitingThread running in background
                // 1 message => 1 event : so, it's serialized !
                if (msg.what == 9900) {
                    if (eventsManager != null) {
                        callback_counts++;
                        Rinor_event event = eventsManager.get_event();
                        if (event != null) {
                            Tracer.v(mytag, "Event from Events_Manager found, ticket : " + event.ticket_id + " # " + event.item);

                            if (last_ticket == -1) {
                                last_ticket = event.item;    //Initial synchro on item
                            } else {
                                if (!((last_ticket + 1) == event.item)) {
                                    Tracer.d(mytag, "Handler event lost ? expected # " + (last_ticket + 1) + " Received # " + event.item);
                                }
                                last_ticket = event.item;
                            }

                            //mapView will be set by update_cache_device if at least one mini widget has to be notified
                            update_cache_device(event.device_id, event.key, event.Value, event.Timestamp);
                        }
                    } else {
                        Tracer.d(mytag, "No Events_Manager known ! ! ! ");
                    }
                } else if (msg.what == 9901) {
                    // Events_Manager thread is dead....
                    Toast.makeText(activity, R.string.event_manager_die, Toast.LENGTH_LONG).show();
                    eventsManager = null;
                    init_done = false;
                    Tracer.i(mytag, "No more Events_Manager now ! ! ! ");
                    //Clean all resources
                    domodb = null;
                    if (timer != null)
                        timer.cancel();
                    timer = null;
                    doAsynchronousTask = null;
                    System.gc();
                    Tracer.i(mytag, "We can really go to end...");
                    try {
                        this.finalize();
                    } catch (Throwable t) {
                        Tracer.e(mytag, t.toString());
                    }

                } else if (msg.what == 9902) {
                    //Time out processed
                    callback_counts++;

                } else if (msg.what == 9903) {
                    //New or update device detected by MQ
                    //Notify on screen
                    Tracer.i(mytag, "Handler send a notification to MainView");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, activity.getText(R.string.device_update_message), Toast.LENGTH_LONG).show();
                        }
                    });

                }
                return true;
            }
        });
        ///////// and pass to it now///////////////////
        Tracer.d(mytag, "Waiting thread started to notify main when cache ready !");
        new waitingThread().execute();

        Tracer.d(mytag, "cache engine initialized !");
        init_done = true;
        result = true;
        return result;
    }


    @Subscribe
    /**
     * Subscribe to the ConnectivityChangeEvent
     */
    public void onEvent(ConnectivityChangeEvent event) {
        //todo find the good solution to stop/reload all the communication with domogik
        Tracer.e(mytag, "Receive event about connectivity");
        /*if (event.isConnected()) {
            if (event.getOn_preferred_Wifi()) {
                this.init(Tracer, activity);
                if (eventsManager == null) {
                    eventsManager = Events_manager.getInstance(activity);
                }
                //eventsManager.init(Tracer, myselfHandler, cache, prefUtils.prefs, instance);
            } else {
                if (api_version >= 0.7f) {
                    //to free the mq listener
                    if (eventsManager != null)
                        eventsManager.Destroy();
                }
            }
        } else {
            //this.cancel();
        }
*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    /**
     * Subscribe to Event_base_message
     */
    public void onEvent(Event_base_message event_base_message) {
        if (event_base_message.getmessage().equals("cache_ready")) {
            // Cache engine being ready, we can start events manager
            Tracer.d(mytag, "Main thread handler : Cache engine is now ready....");
            if (eventsManager == null) {
                eventsManager = Events_manager.getInstance(activity);
            }
            eventsManager.init(Tracer, myselfHandler, cache, prefUtils.prefs, instance);
                    /*
                    if(parent[0] != null) {
						parent[0].sendEmptyMessage(8999);	//Forward event to Main
					}
					 */
        }
    }
    /*
     * Allow callers to set their handler in table
     */

    public void set_handler(Handler parent, int type) {
        //type = 0 if View , or 1 if Map, or 2 if MapView
        if ((type >= 0) && (type <= 2))
            WidgetUpdate.parent[type] = parent;
    }

    public void cancel() {
        activated = false;
        if (timer != null)
            timer.cancel();
        if (eventsManager != null)
            eventsManager.Destroy();
        if (stats_com != null)
            stats_com.cancel();
        stats_com = null;
        EventBus.getDefault().unregister(this);
        Tracer.d(mytag, "cache engine cancel requested : Waiting for events_manager dead !");

    }

    public void Disconnect(int type) {
        String name = "???";

        if (type == 0)
            name = "Main";
        else if (type == 1)
            name = "Map";
        else if (type == 2)
            name = "MapView";
        //Tracer.d(mytag,"Disconnect requested by "+name);

        // Purge all clients matching this container, as soon cache is unlocked
        while (locked) {
            //Somebody else is updating list...
            try {
                Thread.sleep(10);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;


        for (int i = 0; i < cache.size(); i++) {
            Cache_Feature_Element cache_entry = cache.get(i);
            ArrayList<Entity_client> clients_list = null;
            ArrayList<Entity_client> temp_list;

            if (cache_entry != null) {
                clients_list = cache_entry.clients_list;
            }
            if (clients_list != null) {
                int cs = clients_list.size();
                //Tracer.i(mytag, "Processing cache entry # "+i+" <"+cache_entry.DevId+"> clients # = "+cs);
                if (cs > 0) {
                    temp_list = cache_entry.clone_clients_list();
                    int deleted = 0;
                    for (int j = 0; j < cs; j++) {
                        //Tracer.i(mytag, "   Processing client "+j+"/"+(cs-1));
                        Entity_client curclient;

                        try {
                            curclient = clients_list.get(j);

                        } catch (Exception e) {
                            Tracer.e(mytag, "   Exception on client # " + j);
                            curclient = null;
                        }
                        //check each connected client pointed by list
                        if (curclient != null) {
                            int cat = curclient.getClientType();
                            if (cat == type) {
                                //This client was owned by requestor... Remove it  from list
                                Tracer.i(mytag, "remove client # " + j + " <" + curclient.getName() + "> from list " + name);
                                curclient.setClientId(-1);    //note client disconnected
                                curclient.setClientType(-1);    //this entry is'nt owned by anybody
                                //curclient.setHandler(null);    //And must not be notified anymore
                                temp_list.remove(j - deleted);
                                deleted++;
                                if (temp_list.size() == 0) {
                                    //List is empty : remove it from the device entry
                                    temp_list = null;
                                    break;
                                }

                            }
                        }
                    }    //End of loop on clients list, for a cache entry
                    cache_entry.clients_list = temp_list;
                }
            }
            //Next cache entry
        } // End of loop on cache items
        locked = false;
        //dump_cache();	//During development, help to debug !
    }

    public JSONArray dump_cache_to_json() throws JSONException {
        JSONArray json_dump_cache = new JSONArray();

        int size = cache.size();
        Tracer.v(mytag, "Dump of Cache , size = " + cache.size());
        while (locked) {
            //Somebody else is updating list...
            try {
                Thread.sleep(10);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;
        for (int i = 0; i < size; i++) {
            Cache_Feature_Element cache_entry = cache.get(i);
            ArrayList<Entity_client> clients_list;
            if (cache_entry == null) {
                Tracer.e(mytag, "Cache entry # " + i + "   empty ! ");
            } else {
                JSONObject json_dump_current_cache = new JSONObject();
                clients_list = cache_entry.clients_list;
                int clients_list_size = 0;
                if (clients_list != null)
                    clients_list_size = clients_list.size();
                Tracer.v(mytag, "Cache entry # " + i + "   DevID : " + cache_entry.DevId + " Skey : " + cache_entry.skey + " Clients # :" + clients_list_size + " Value= :" + cache_entry.Value);
                json_dump_current_cache.put("id", cache_entry.DevId);
                json_dump_current_cache.put("last_value", cache_entry.Value);
                json_dump_current_cache.put("last_received", cache_entry.Value_timestamp);
                json_dump_cache.put(json_dump_current_cache);
            }
        }
        locked = false;
        return json_dump_cache;
    }

    public void dump_cache() {
        String[] name = new String[]{"Main   ", "Map    ", "MapView", "???    "};
        int size = cache.size();
        Tracer.v(mytag, "Dump of Cache , size = " + cache.size());

        while (locked) {
            //Somebody else is updating list...
            try {
                Thread.sleep(10);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;


        for (int i = 0; i < size; i++) {
            Cache_Feature_Element cache_entry = cache.get(i);
            ArrayList<Entity_client> clients_list;
            if (cache_entry == null) {
                Tracer.e(mytag, "Cache entry # " + i + "   empty ! ");
            } else {
                clients_list = cache_entry.clients_list;
                int clients_list_size = 0;
                if (clients_list != null)
                    clients_list_size = clients_list.size();

                Tracer.v(mytag, "Cache entry # " + i + "   DevID : " + cache_entry.DevId + " Skey : " + cache_entry.skey + " Clients # :" + clients_list_size + " Value= :" + cache_entry.Value);
                if (clients_list_size > 0) {
                    for (int j = 0; j < clients_list_size; j++) {
                        if (clients_list.get(j) == null)
                            break;

                        int cat = clients_list.get(j).getClientType();
                        String client_name = clients_list.get(j).getName();
                        String state = "connected";
                        String type = "widget";
                        if (clients_list.get(j).is_Miniwidget())
                            type = "mini widget";
                        int ctype = clients_list.get(j).getClientType();
                        if (ctype == -1)
                            ctype = 3;

                        Tracer.v(mytag, "           ==> entry : " + j + " owner : " + name[ctype]
                                + " client name : " + client_name
                                + " type = " + type
                                + " state = " + state);

                    }
                }
            }

        }    //End of loop on clients list, for a cache entry

        locked = false;
        Tracer.i(mytag, "End of cache dump ");

    }

    /*
     * Method allowing external methods to force a refresh
     */
    public void refreshNow() {
        if (doAsynchronousTask != null)
            doAsynchronousTask.run();    //To force immediate refresh
    }


    public void resync() {
        //May be URL has been changed : force engine to reconstruct cache
        Disconnect(0);
        Disconnect(1);
        Disconnect(2);
        locked = true;
        cache = null;
        cache = new ArrayList<>();
        ready = false;
        refreshNow();    //To reconstruct cache
        Tracer.d(mytag, "state engine resync : waiting for initial setting of cache !");

        Boolean said = false;
        int counter = 0;
        while (!ready) {
            if (!said) {
                Tracer.d(mytag, "cache engine not yet ready : Wait a bit !");
                said = true;
            }
            try {
                Thread.sleep(100);
                counter++;
                if (counter > 100) {
                    // 10 seconds elapsed
                    //finalize();
                }
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        Tracer.d(mytag, "cache engine ready after resync !");
        locked = false;
    }

    /*
     * This method should only be called once, to create and arm a cyclic timer
     */
    private void Timer() {
        timer = new Timer();

        final Handler loc_handler = new Handler();
        if (timer_flag)
            return;    //Don't run many cyclic timers !

        doAsynchronousTask = new TimerTask() {

            @Override
            public void run() {
                Runnable myTH = new Runnable() {

                    public void run() {
                        if (activated) {
                            try {
                                Tracer.d(mytag, "new UpdateThread().execute() from timer");
                                new UpdateThread().execute(); //on timer
                            } catch (Exception e) {
                                Tracer.e(mytag, e.toString());
                            }

                        }
                    } //End of run methodTimer
                };    // End of runnable bloc

                try {
                    loc_handler.post(myTH);        //To avoid exception on ICS
                } catch (Exception e) {
                    Tracer.e(mytag, e.toString());
                }
            }
        };

        // and arm the timer to do automatically this each 'update' seconds
        timer_flag = true;    //Cyclic timer is running...
        if (timer != null) {
            //timer.schedule(doAsynchronousTask, 0, 125 * 1000);    // for tests with Events_Manager
            // 2'05 is a bit more than events timeout by server (2')
            // dame but using the user option timer
            timer.schedule(doAsynchronousTask, 0, prefUtils.GetRestUpdateTimer() * 1000);
        }
    }

    /*
     * Method to freeze/wakeup server exchanges and cache state ( on Pause / Resume, by example )
     */
    public void set_sleeping() {
        Tracer.d(mytag, "Pause requested...");
        if (eventsManager != null) {
            eventsManager.set_sleeping();
        }
        if (stats_com != null)
            stats_com.set_sleeping();

        sleeping = true;
    }

    public void wakeup() {
        //ADD try catch due to an android error message in dev console on a DROID RAZR i (smi)
        try {
            Tracer.d(mytag, "Wake up requested...");
            if (eventsManager != null) {
                eventsManager.wakeup();
            }
            if (stats_com != null)
                stats_com.wakeup();

            sleeping = false;
            //TODO : if sleep period too long (> 1'50) , we must force a refresh of cache values, because events have been 'masked' !
            if ((eventsManager != null) && eventsManager.cache_out_of_date) {
                callback_counts = 0;
                new UpdateThread().execute();    //Force an immediate cache refresh on wakeup
                eventsManager.cache_out_of_date = false;
            }
            if (ready) {    //Notify cache is ready
                //Notify each registered widget
                EventBus.getDefault().post(new Event_base_message("cache_ready"));
                /*if (parent[0] != null) {
                    parent[0].sendEmptyMessage(8999);
                }
                if (parent[1] != null) {
                    parent[1].sendEmptyMessage(8999);
                }
                if (parent[2] != null) {
                    parent[2].sendEmptyMessage(8999);
                }*/

            }
        } catch (Exception e) {
            e.printStackTrace();
            //Tracer.e(mytag, "Crash cause by: " + e.toString());
        }
    }

    private class waitingThread extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Boolean said = false;
            int counter = 0;
            while (!ready) {
                if (!said) {
                    Tracer.d(mytag, "cache engine not yet ready : Wait a bit !");
                    said = true;
                }
                try {
                    Thread.sleep(100);
                    counter++;
                    if (counter > 100) {
                        said = false;
                        counter = 0;
                    }
                } catch (Exception e) {
                    Tracer.e(mytag, e.toString());
                }
            }
            if (myselfHandler != null) {
                //Tracer.d(mytag,"cache engine ready  ! Notify it....");
                //myselfHandler.sendEmptyMessage(8999);    // cache engine ready.....
                //Notify each registered widget
                EventBus.getDefault().post(new Event_base_message("cache_ready"));
            }
            if (parent[0] != null) {
                Tracer.d(mytag, "cache engine ready  ! Notify Main activity....");
                //parent[0].sendEmptyMessage(8999);    //hide Toast message
            } else {
                Tracer.d(mytag, "cache engine ready  ! No Main activity....");
            }
            if (parent[1] != null) {
                Tracer.d(mytag, "cache engine ready  ! Notify Map activity....");
                //parent[1].sendEmptyMessage(8999);    //hide Toast message
            } else {
                Tracer.d(mytag, "cache engine ready  ! No Map activity....");
            }
            if (parent[2] != null) {
                Tracer.d(mytag, "cache engine ready  ! Notify MapView activity....");
                //parent[2].sendEmptyMessage(8999);    //hide Toast message
            } else {
                Tracer.d(mytag, "cache engine ready  ! No MapView activity....");
            }
            Tracer.d(mytag, "cache engine ready  ! Exiting Waiting thread ....");
            return null;
        }

    }

    private class UpdateThread extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Added by Doume to correctly release resources when exiting
            if (!activated) {
                Tracer.d(mytag, "UpdateThread frozen....");

            } else {
                if (sleeping) {
                    return null;    //Will check stats in 2 minutes
                }
                if (callback_counts > 0) {
                    if (Tracer != null)
                        Tracer.d(mytag, "Events detected since last loop = " + callback_counts + " No stats !");
                    //myselfHandler.sendEmptyMessage(9900);	//Force to drain the pending stack events
                    callback_counts = 0;
                    return null;
                }
                if (Tracer != null)
                    Tracer.d(mytag, "Request to server for stats update...");

                String request = prefUtils.GetUpdateUrl();
                String URL = prefUtils.GetUrl();
                //Because in old time it was the full request that was used and saved, not only the real UPDATE_PATH
                //like "https://192.168.0.1:40406/rest/sensor" instead of just "sensor"
                try {
                    request = request.replace(URL, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    request = prefUtils.GetUpdateUrl();
                }

                Tracer.i(mytag, "urlupdate saved = " + request);

                if (request != null) {
                    JSONObject json_widget_state = null;
                    JSONArray json_widget_state_0_4 = new JSONArray();
                    stats_com.add(Stats_Com.STATS_SEND, request.length());
                    try {
                        if (api_version <= 0.6f) {
                            //Set timeout very high as tickets is a long process
                            json_widget_state = Rest_com.connect_jsonobject(activity, Tracer, request, 300000);
                            if (json_widget_state == null || (json_widget_state.toString().equals("{}"))) {
                                // Cannot get data_type from Rinor server.....
                                Tracer.e(mytag, "Cannot get data_type from Rinor server.....");
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(activity, R.string.rest_error_getting_sensors, Toast.LENGTH_LONG).show();
                                    }
                                });
                                //Notify error to parent Dialog
                                EventBus.getDefault().post(new Event_base_message("datatype"));
                                //todo stop Widgetupdate
                                return null;
                            }
                            Tracer.d(mytag, "json_widget_state for <0.6 API=" + json_widget_state.toString());
                        } else if (api_version >= 0.7f) {
                            json_widget_state = new JSONObject();
                            //todo change by == when device.get will work for 0.5
                            // else if (api_version == 0.7f) {
                            //get all sensors
                            if (api_version < 0.9f) {
                                //get all sensors from rest
                                json_widget_state_0_4 = Rest_com.connect_jsonarray(activity, Tracer, request, 30000);
                                if (json_widget_state_0_4 == null || (json_widget_state_0_4.toString().equals("[]"))) {
                                    // Cannot get data_type from Rinor server.....
                                    Tracer.e(mytag, "Cannot get sensors from Rinor server.....");
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(activity, R.string.rest_error_getting_sensors, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    //Notify error to parent Dialog
                                    EventBus.getDefault().post(new Event_base_message("datatype"));
                                    //todo stop Widgetupdate
                                    return null;
                                }
                            } else if (api_version == 0.9f) {
                                //load timestamp apps was closed
                                String sensor_saved_timestamp = prefUtils.GetSensorSavedTimestamp();
                                Log.e("#124 sensor_timestamp: ", sensor_saved_timestamp);

                                //Modify request to match the timestamp
                                String request_since;
                                //if timestamp is null or 0 get the full sensor list
                                if (sensor_saved_timestamp.equals("0")) {
                                    request_since = request;
                                } else {
                                    request_since = request + "since/" + sensor_saved_timestamp;
                                }
                                JSONArray json_widget_state_0_6 = Rest_com.connect_jsonarray(activity, Tracer, request_since, 30000);
                                if (json_widget_state_0_6 == null || (json_widget_state_0_6.toString().equals("[]"))) {
                                    // Cannot get data_type from Rinor server.....
                                    Tracer.e(mytag, "Cannot get sensors from Rinor server.....");
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(activity, R.string.rest_error_getting_sensors, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    //Notify error to parent Dialog
                                    EventBus.getDefault().post(new Event_base_message("sensor_list_error"));
                                    //todo stop Widgetupdate
                                    return null;
                                }
                                Tracer.d(mytag, "json_widget_state for 0.9 API=" + json_widget_state_0_6.toString());

                                String strJson;
                                try {
                                    Tracer.d(mytag, "124 prefUtils.GetSensorSavedValue(): " + prefUtils.GetSensorSavedValue());
                                    strJson = prefUtils.GetSensorSavedValue();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    strJson = null;
                                }
                                if (strJson != null && strJson != "0") {
                                    //TODO load stored last_value
                                    String tmp_key;
                                    for (int i = 0; i < json_widget_state_0_6.length(); i++) {
                                        json_widget_state_0_4.put(json_widget_state_0_6.getJSONObject(i));
                                        Log.e("#124", "json creating from domogik: " + json_widget_state_0_6.getJSONObject(i).toString());
                                        Log.e("#124", "json creating from domogik: " + json_widget_state_0_4.length());
                                    }
                                    //TODO remove when ok
                                    // Display message something changed since last update
                                    final JSONArray finalJson_widget_state_0_ = json_widget_state_0_6;
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(activity, "json length from rest sensor/since= " + finalJson_widget_state_0_.length(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    try {
                                        JSONArray jsonData = new JSONArray(strJson);
                                        Log.e("#124 json saved: ", jsonData.toString());
                                        for (int i = 0; i < jsonData.length(); i++) {
                                            json_widget_state_0_4.put(jsonData.getJSONObject(i));
                                            Log.e("#124", "json creating from saved: " + jsonData.getJSONObject(i).toString());
                                            Log.e("#124", "json creating from saved: " + json_widget_state_0_4.length());

                                        }
                                        //#124 Todo compare saved value and load value from domogik to remove old ones.

                                        //TODO remove when ok
                                        //Display message something changed since last update
                                        final JSONArray finalJson_widget_state_0_1 = jsonData;
                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(activity, "json length from from saved: " + finalJson_widget_state_0_1.length(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        Log.e("#124", "json combined: " + json_widget_state_0_4.toString());
                                        //TODO remove when ok
                                        final JSONArray finalJson_widget_state_0_2 = json_widget_state_0_4;
                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(activity, "json total: " + finalJson_widget_state_0_2.length(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Tracer.d(mytag, "#124 json saved is null");
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(activity, "json saved is null", Toast.LENGTH_SHORT).show();
                                        }
                                    });//get all sensors from rest
                                    json_widget_state_0_4 = Rest_com.connect_jsonarray(activity, Tracer, request, 30000);
                                    if (json_widget_state_0_4 == null || (json_widget_state_0_4.toString().equals("[]"))) {
                                        // Cannot get data_type from Rinor server.....
                                        Tracer.e(mytag, "Cannot get sensors from Rinor server.....");
                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(activity, R.string.rest_error_getting_sensors, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                        //Notify error to parent Dialog
                                        EventBus.getDefault().post(new Event_base_message("sensor_list_error"));
                                        //todo stop Widgetupdate
                                        return null;
                                    }
                                }
                            }
                            // Create a false JSONObject like if it was domomgik 0.3
                            //(meaning provide value in an stats: array containing a list of value in jsonobject format)
                            json_widget_state.put("stats", json_widget_state_0_4);
                            Tracer.d(mytag, "json_widget_state for API >0.7 =" + json_widget_state.toString());
                            //todo move this part in 0.8 api under when MQ.Get will work
                            if (api_version >= 0.8f) {
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                Date timestamplast_device_update;
                                Date timestamplast_update;
                                boolean newer = false;
                                try {
                                    timestamplast_device_update = df.parse(last_device_update);
                                } catch (Exception e) {
                                    Tracer.e(mytag, "No saved date or error parsing it");
                                    timestamplast_device_update = new Date();
                                }
                                try {
                                    JSONArray json_device_state_0_4 = new JSONArray();
                                    //change url to get device instead of sensor
                                    request = request.replace("sensor", "device");
                                    //Grab device list
                                    if (api_version == 0.8f) {
                                        json_device_state_0_4 = Rest_com.connect_jsonarray(activity, Tracer, request, 30000);
                                        if (json_widget_state_0_4 == null || (json_widget_state_0_4.toString().equals("[]"))) {
                                            // Cannot get data_type from Rinor server.....
                                            Tracer.e(mytag, "Cannot get device list from Rinor server.....");
                                            activity.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(activity, R.string.rest_error_getting_device, Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            //Notify error to parent Dialog
                                            EventBus.getDefault().post(new Event_base_message("device_list_error"));
                                            //todo stop Widgetupdate
                                            return null;
                                        }
                                    } else if (api_version >= 0.9f) {
                                        //todo be sure last_device_update is in the right timestamp format???
                                        //TODO #124
                                        //Tracer.e(mytag, "#124 last_device_update to use in since: " + prefUtils.GetSensorSavedTimestamp());
                                        request = request + "since/" + prefUtils.GetSensorSavedTimestamp();
                                        json_device_state_0_4 = Rest_com.connect_jsonarray(activity, Tracer, request, 30000);
                                        if (json_widget_state_0_4 == null || (json_widget_state_0_4.toString().equals("[]"))) {
                                            // Cannot get data_type from Rinor server.....
                                            Tracer.e(mytag, "Cannot get sensors from Rinor server.....");
                                            activity.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    Toast.makeText(activity, R.string.rest_error_getting_device, Toast.LENGTH_LONG).show();
                                                }
                                            });
                                            //Notify error to parent Dialog
                                            EventBus.getDefault().post(new Event_base_message("device_list_error"));
                                            //todo stop Widgetupdate
                                            return null;
                                        }
                                    }
                                    Tracer.d(mytag, "json_widget_device for 0.8 API=" + json_device_state_0_4.toString());
                                    //test if info_changed:
                                    for (int i = 0; i < json_device_state_0_4.length(); i++) {
                                        try {
                                            String last_update = json_device_state_0_4.getJSONObject(i).getString("info_changed");
                                            timestamplast_update = df.parse(last_update);
                                            //compare to latest update
                                            if (timestamplast_update.compareTo(timestamplast_device_update) > 0) {
                                                newer = true;
                                                timestamplast_device_update = timestamplast_update;
                                                Tracer.v(mytag, "device info_changed at: " + timestamplast_update.toString());
                                            }
                                        } catch (ParseException E) {
                                            //timestamplast_update = new Date();
                                            Tracer.d(mytag, "Exception info_changed:" + E);
                                        }
                                    }
                                    if (newer) {
                                        //Display message something changed since last update
                                        activity.runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(activity, R.string.device_update_message, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    Tracer.e(mytag, "Error trying to parse /device and info_changed");
                                }
                            }
                        }/* else if (api_version >= 0.8f) {
                            //todo will use this when device.get will work
                            json_widget_state = zmqrequest();
                        }*/
                    } catch (final Exception e) {
                        //stats request cannot be completed (broken link or terminal in standby ?)
                        //Will retry automatically in 2'05, if no events received
                        Tracer.e(mytag, "get stats : Rinor error <" + e.getMessage() + ">");
                        //Toast not available in asynctask
                        // TODO handle "Host name may not be null" to avoid white page in domodroid
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, R.string.Error + e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                    //Tracer.d(mytag,"UPDATE_URL = "+ sharedparams.getString("UPDATE_URL", null));
                    //Tracer.d(mytag,"result : "+ json_widget_state.toString());
                    if (json_widget_state != null) {
                        stats_com.add(Stats_Com.STATS_RCV, json_widget_state.toString().length());
                        // cache engine : update cache with new values...
                        int updated_items = update_cache(json_widget_state);
                        //if(updated_items > 0) {
                        //domodb.insertFeatureState(json_widget_state);
                        //}
                        ready = true;        //Accept subscribing, now !
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
        String Value_timestamp = null;

        JSONArray itemArray = null;

        if (json_widget_state == null) {
            if (parent[0] != null) {
                parent[0].sendEmptyMessage(8001);    //Ask main to display message
            }
            return 0;
        }
        //Tracer.i(mytag, "Cache update : stats result <"+json_widget_state.toString()+">");

        try {
            itemArray = json_widget_state.getJSONArray("stats");
        } catch (Exception e) {
            Tracer.e(mytag, "Cache update : No stats result !");
            Tracer.e(mytag, e.toString());
            if (parent[0] != null) {
                parent[0].sendEmptyMessage(8001);    //Ask main to display message
            }
            return 0;
        }
        if (itemArray == null) {
            //Ask main to display message
            EventBus.getDefault().post(new Event_base_message("domogik_error"));
            return 0;
        }
        try {
            if (itemArray.getJSONObject(0).getString("Error").length() > 0) {
                //Ask main to display message
                EventBus.getDefault().post(new Event_base_message("stats_error"));
                return 0;
            }
        } catch (JSONException e) {
            Tracer.e(mytag, e.toString());
        }
        for (int i = 0; i < itemArray.length(); i++) {
            //force to process to true for all item and then to false if something wrong
            //then if false it will not update the cache value, avoiding display of null value.
            to_process = true;
            //Retrieve Json infos
            try {
                if (api_version <= 0.6f) {
                    dev_id = itemArray.getJSONObject(i).getInt("device_id");
                } else if (api_version >= 0.7f) {
                    //todo try to avoid problem with MQ
                    dev_id = itemArray.getJSONObject(i).getInt("id");
                    //dev_id = itemArray.getJSONObject(i).getInt("device_id");
                }
            } catch (Exception e) {
                Tracer.e(mytag, "Cache update : No feature id ! ");
                Tracer.e(mytag, e.toString());
                to_process = false;
            }
            try {
                if (api_version <= 0.6f) {
                    skey = itemArray.getJSONObject(i).getString("skey");
                } else if (api_version >= 0.7f) {
                    //todo try to avoid problem with MQ
                    skey = "";
                    //skey = itemArray.getJSONObject(i).getString("reference");

                }
            } catch (Exception e) {
                Tracer.e(mytag, "Cache update : No skey ! ");
                Tracer.e(mytag, e.toString());
                to_process = false;
            }
            try {
                if (api_version <= 0.6f) {
                    Val = itemArray.getJSONObject(i).getString("value");
                } else if (api_version >= 0.7f) {
                    String temp = itemArray.getJSONObject(i).getString("last_value");
                    if (temp.equals(null) || temp.equals("null")) {
                        to_process = false;
                    } else {
                        Val = temp;
                    }
                }
            } catch (Exception e) {
                Tracer.e(mytag, "Cache update : No value ! ");
                Tracer.e(mytag, e.toString());
                to_process = false;
            }
            try {
                if (api_version <= 0.6f) {
                    Value_timestamp = itemArray.getJSONObject(i).getString("timestamp");
                } else if (api_version >= 0.7f) {
                    //todo try to avoid problem with MQ
                    Value_timestamp = itemArray.getJSONObject(i).getString("last_received");
                    //dev_id = itemArray.getJSONObject(i).getInt("device_id");
                }
            } catch (Exception e) {
                Tracer.e(mytag, "Cache update : No feature id ! ");
                Tracer.e(mytag, e.toString());
                to_process = false;
            }
            // Try to put this in cache, now
            if (to_process) {
                Boolean item_updated = update_cache_device(dev_id, skey, Val, Value_timestamp);    //insert, update or ignore new value for this feature
                if (item_updated)
                    updated_items++;
            }

        } // end of for loop on stats result
        Tracer.i(mytag, "cache size = " + cache.size());

        return updated_items;
    }

    /*
     * Update device value in cache, and eventually notify clients about change
     * This sequence must be protected against concurrent access
     */
    private Boolean update_cache_device(int dev_id, String skey, String Val, String Value_timestamp) {
        if (cache == null)
            return false;

        //synchronized(this) {
        while (locked) {
            //Somebody else is updating list...
            //Tracer.i(mytag, "cache engine locked : wait !");
            try {
                Thread.sleep(100);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;    //Take the lock
        Boolean result = false;
        int cache_position = -1;

        cache_position = locate_device(dev_id, skey, last_position); // Try to retrieve it in cache from the last accessed position
        // because 'stats' return them always in the same order
        if (cache_position >= 0) {
            //device found
            last_position = cache_position;        //Keep the position, for next search
            if ((cache.get(cache_position).Value.equals(Val))) {
                //value not changed
                Tracer.i(mytag, "cache engine no value change for (" + dev_id + ") (" + skey + ") (" + Val + ") with timestamp=" + Value_timestamp);

            } else {
                //value changed : has to notify clients....
                Tracer.i(mytag, "cache engine update value changed for (" + dev_id + ") (" + skey + ") (" + Val + ") with timestamp=" + Value_timestamp);
                cache.get(cache_position).Value = Val;
                cache.get(cache_position).Value_timestamp = Value_timestamp;
                result = true;
                if (cache.get(cache_position).clients_list != null) {
                    for (int j = 0; j < cache.get(cache_position).clients_list.size(); j++) {
                        //update the session structure with new value it will also notify widgets
                        cache.get(cache_position).clients_list.get(j).client_value_update(Val, Value_timestamp);
                    }
                }

            }
        } else {
            // device not yet exist in cache
            // when creating a new cache entry, it can't have clients !
            Tracer.i(mytag, "cache engine inserting (" + dev_id + ") (" + skey + ") (" + Val + ")");
            Cache_Feature_Element device = new Cache_Feature_Element(dev_id, skey, Val, Value_timestamp);
            cache.add(device);
            result = true;
        }
        locked = false;
        return result;
        //} // End protected bloc
    }

    private int locate_device(int dev_id, String skey, int from) {
        if (cache.size() == 0)
            return -1;        //empty cache
        int pos = from + 1;
        if (pos >= cache.size() || pos < 0)
            pos = 0;
        //Check if following entry in cache is the good one...
        for (int i = pos; i < cache.size(); i++) {
            if ((cache.get(i).DevId == dev_id) && (cache.get(i).skey.equals(skey))) {
                return i;        //Bingo, the next one was the good one !
            }
        }
        // If here, it's because from the location 'from + 1', till end of cache, the device was not found !
        // Search from the beginning of table
        for (int i = 0; i <= pos; i++) {
            if ((cache.get(i).DevId == dev_id) && (cache.get(i).skey.equals(skey))) {
                return i;        //Bingo, found !
            }
        }
        return -1;    //Not found in cache !
    }

    /*
     * Method offered to clients, to subscribe to a device/skey value-changed event
     *
     * 	Parameter : Object Entity_client containing references to device
     *  Result : false if subscription failed (already exist, or unknown device )
     *  		 true : subscription accepted : Entity_client contains resulting state
     */
    public Boolean subscribe(Entity_client client) {
        int device = -1;
        String skey = "";
        Boolean result = false;

        if (client == null)
            return result;

        device = client.getDevId();
        skey = client.getskey();

        Tracer.i(mytag, "cache engine subscription requested by <" + client.getName() + "> Device (" + device + ") (" + skey + ")");
        if (!ready) {
            Tracer.i(mytag, "cache engine not yet ready : reject !");
            return result;
        }

        while (locked) {
            //Somebody else is updating list...
            Tracer.i(mytag, "cache engine locked : wait !");
            try {
                Thread.sleep(100);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;    //Take the lock

        for (int i = 0; i < cache.size(); i++) {
            if ((cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
                //found device in list
                client.setValue(cache.get(i).Value);    //return current stat value
                client.setTimestamp(cache.get(i).Value_timestamp);    //return current stat Value_timestamp
                // Try to add this client to list

                cache.get(i).add_client(client);    //The client structure will contain also last known value for this device
                Tracer.i(mytag, "cache engine subscription done for <" + client.getName() + "> Device (" + device + ") (" + skey + ") Value : " + cache.get(i).Value);
                result = true;
                break;
            }
            // not the good one : check next
        }    //loop to search this device in cache
        locked = false;
        //dump_cache();
        return result;
    }

    public void unsubscribe(Entity_client client) {
        int device = -1;
        String skey = "";
        Boolean result = false;

        if (client == null)
            return;
        device = client.getDevId();
        skey = client.getskey();
        Tracer.i(mytag, "cache engine release subscription requested by <" + client.getName() + "> Device (" + device + ") (" + skey + ")");

        while (locked) {
            //Somebody else is updating list...
            try {
                Thread.sleep(10);        //Standby 10 milliseconds
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }
        }
        locked = true;
        for (int i = 0; i < cache.size(); i++) {
            if ((cache.get(i).DevId == device) && (cache.get(i).skey.equals(skey))) {
                //found device in list
                client.setValue(cache.get(i).Value);    //return current stat value
                client.setTimestamp(cache.get(i).Value_timestamp);    //return current stat value
                // Try to remove this client from list
                cache.get(i).remove_client(client);
                Tracer.i(mytag, "cache engine release subscription OK for <" + client.getName() + "> Device (" + device + ") (" + skey + ")");
                result = true;
                break;
            }
            // not the good one : check next

        }    //loop to search this device in cache

        client.setClientId(-1);        //subscribing not located...
        locked = false;
    }


    /*
     * Some methods to help widgets for database access (they don't have anymore to connect to DomodroidDB !
     *
     */
    public void descUpdate(int id, String new_desc, String type) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        boolean changed = false;
        try {
            if (domodroid.instance.isConnected()) {
                String url = null;
                Boolean SSL = false;
                if (domodroid.instance.on_preferred_Wifi) {
                    //If connected to default SSID use local adress
                    url = prefUtils.GetUrl();
                    SSL = prefUtils.GetRestSsl();
                } else {
                    //If not connected to default SSID use external adress
                    url = prefUtils.GetExternalRestIp();
                    SSL = prefUtils.GetExternalRestSsl();
                }

                //Todo Move this method somewhere else and make it reusable.
                if (!SSL) {
                    try {
                        Entity_Feature feature = domodb.requestFeaturesbyid(Integer.toString(id));
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPut httpput = new HttpPut(url + "device/" + feature.getDevId());
                        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                        nameValuePairs.add(new BasicNameValuePair("description", new_desc));
                        httpput.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                        HttpResponse response = httpclient.execute(httpput);
                        Tracer.d(mytag, "Renaming to Domogik without SSL response=" + response.getStatusLine().toString());
                        changed = true;
                    } catch (IOException e) {
                        Tracer.e(mytag, "Renaming to Domogik without SSL error " + e.toString());
                    }
                } else {
                    try {
                        Entity_Feature feature = domodb.requestFeaturesbyid(Integer.toString(id));
                        HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url + "device/" + feature.getDevId(), login, password);
                        urlConnection.setRequestMethod("PUT");
                        List<NameValuePair> nameValuePairs = new ArrayList<>(1);
                        nameValuePairs.add(new BasicNameValuePair("description", new_desc));
                        String result = null;
                        urlConnection.setDoOutput(true);
                        OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(Abstract.httpsUrl.getQuery(nameValuePairs));
                        writer.flush();
                        writer.close();
                        os.close();
                        int responseCode = urlConnection.getResponseCode();
                        Tracer.d(mytag, "Renaming to Domogik with SSL response=" + responseCode);
                        changed = true;
                    } catch (IOException e) {
                        Tracer.e(mytag, "Renaming to Domogik with SSL error " + e.toString());
                    }
                }
                if (changed) {
                    //update db
                    domodb.update_name(id, new_desc, type);
                    //store last update in prefs for next start
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date tempdate = new Date();
                    prefUtils.SetLastDeviceUpdate(df.format(tempdate));
                }
            } else {
                Tracer.e(mytag, "NO CONNECTION");
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, R.string.no_connection, Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());


        }
    }

    /*
     * This one allow Widgets to permut (for moving up or down)
     */

    public void move_one_feature_association(int id, int place_id, String place_type, String
            order) {
        domodb.move_one_feature_association(id, place_id, place_type, order);
    }
    /*
     * This one allow area to permut (for moving up or down)
     */

    public void move_one_area(int id, int place_id, String place_type, String
            order) {
        domodb.move_one_area(id, place_id, place_type, order);
    }
    /*
     * This one allow room to permut (for moving up or down)
     */

    public void move_one_room(int id, int place_id, String place_type, String
            order) {
        domodb.move_one_room(id, place_id, place_type, order);
    }

    /*
     * This one allow MapView to clean all widgets from a map
     */
    public void cleanFeatureMap(String map_name) {
        domodb.cleanFeatureMap(map_name);
    }

    /*
     * Obtain the list of feature located on a map
     */
    public Entity_Map[] getMapFeaturesList(String currentmap) {
        return domodb.requestFeatures(currentmap);
    }

    /*
     * Obtain the list of map switches located on a map
     */
    public Entity_Map[] getMapSwitchesList(String currentmap) {
        return domodb.requestMapSwitches(currentmap);
    }

    /*
     * Put feature at coordinates on a map
     */
    public void insertFeatureMap(int id, int posx, int posy, String mapname) {
        domodb.insertFeatureMap(id, posx, posy, mapname);
    }

    /*
     * remove an area/room/icon or feature by id and type
     */
    public void remove_one_things(int id, String type) {
        //Correct #209O in a better way.
        //seems domodb not create because widgetupdate not init.
        //in case domogik MQ problem
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        switch (type) {
            case "area":
                domodb.remove_one_area(id);
                break;
            case "room":
                domodb.remove_one_room(id);
                break;
            case "icon":
                domodb.remove_one_icon(id);
                break;
            case "feature":
                domodb.remove_one_feature(id);
                break;
        }

    }

    /*
     * remove an icon with his id and type
     */
    public void remove_one_icon(int id, String place_type) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        domodb.remove_one_icon(id, place_type);

    }

    /*
     * remove a feature_association with his id place_id and place_type
     */
    public void remove_one_feature_association(int id, int place_id, String place_type) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        domodb.remove_one_feature_association(id, place_id, place_type);
    }

    /*
     * remove a feature_map with his coordinates and mapname
     */
    public void remove_one_FeatureMap(int id, int posx, int posy, String mapname) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        domodb.remove_one_FeatureMap(id, posx, posy, mapname);
    }

    public void remove_one_feature_in_FeatureMap(int id) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        domodb.remove_one_feature_in_FeatureMap(id);
    }

    /*
     * remove a place with his place_id and place_type
     */
    public void remove_one_place_type_in_Featureassociation(int place_id, String place_type) {
        if (domodb == null) {
            Tracer.d(mytag, "domodb is null");
            this.init(Tracer, activity);
        }
        domodb.remove_one_place_type_in_Featureassociation(place_id, place_type);
    }

    public static Entity_Feature[] requestFeatures() {
        return domodb.requestFeatures();

    }

    public static Activity getactivity() {
        return activity;
    }

    public JSONObject zmqrequest() throws JSONException {
        ZMQReqMessage REQ = new ZMQReqMessage(myselfHandler);
        String ip = prefUtils.GetMqAddress();    // TODO : use a R. for the default value
        String port = prefUtils.GetMqReqRepPort();    // TODO : use a R. for the default value
        final String pub_url = "tcp://" + ip + ":" + port;
        Log.d(mytag, "req address : " + pub_url);
        JSONArray json_widget_state_0_5 = new JSONArray();

        REQ.execute(pub_url, "device.get");
        REQ = null;

        Tracer.json(mytag, json_widget_state_0_5.toString());
        JSONObject json_widget_state = new JSONObject();
        // Create a false jsonarray like if it was domomgik 0.3
        //(meaning provide value in an stats: array containing a list of value in jsonobject format)
        json_widget_state.put("stats", json_widget_state_0_5);
        Tracer.d(mytag, "json_widget_state for 0.8 API=");
        Tracer.json(mytag, json_widget_state.toString());
        return json_widget_state;
    }

}

