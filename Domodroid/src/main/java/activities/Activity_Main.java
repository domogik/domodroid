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

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import Abstract.common_method;
import Abstract.load_parameters;
import Dialog.Dialog_House;
import Dialog.Dialog_Splash;
import Dialog.Dialog_Synchronize;
import Entity.Entity_Area;
import Entity.Entity_Room;
import database.Cache_management;
import database.WidgetUpdate;
import misc.changelog;
import misc.tracerengine;
import mq.Main;
import widgets.Basic_Graphical_zone;


@SuppressWarnings({"static-access"})
public class Activity_Main extends AppCompatActivity implements OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private final String mytag = this.getClass().getName();
    public static Context context;
    private SharedPreferences SP_params;
    private SharedPreferences.Editor SP_prefEditor;
    private AlertDialog.Builder AD_notSyncAlert;
    private AlertDialog.Builder AD_wifi_prefered;
    private Widgets_Manager WM_Agent;
    private Dialog_Synchronize DIALOG_dialog_sync;
    private WidgetUpdate WU_widgetUpdate;
    private Handler sbanim;
    private static Handler widgetHandler;
    private Intent INTENT_map = null;
    private ImageView appname;

    private ViewGroup VG_parent;
    public static ScrollView SV_Main_ScrollView;
    private Vector<String[]> history;
    private int historyPosition;
    private LinearLayout LL_house_map;
    private LinearLayout LL_area;
    private LinearLayout LL_room;
    private LinearLayout LL_activ;
    private Basic_Graphical_zone house;
    private Basic_Graphical_zone map;

    private Boolean reload = false;
    private DialogInterface.OnDismissListener sync_listener = null;
    private DialogInterface.OnDismissListener house_listener = null;

    private static Boolean by_usage = false;
    private Boolean init_done = false;
    private final File backupprefs = new File(Environment.getExternalStorageDirectory() + "/domodroid/.conf/settings");
    private final Thread waiting_thread = null;
    private Activity_Main myself = null;
    private tracerengine Tracer = null;
    private ProgressDialog PG_dialog_message;
    private Boolean end_of_init_requested = true;
    private Entity_Room[] listRoom;
    private Entity_Area[] listArea;
    private Menu mainMenu;

    private Toolbar toolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private int mSelectedId;

    public static ArrayList<HashMap<String, String>> listItem;
    private ListView listePlace;
    private SimpleAdapter adapter_map;

    private PendingIntent pendingIntent_for_metrics;
    private Intent intent_for_metrics;
    private AlarmManager processTimer_for_metrics;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
            return; // add this to prevent from doing unnecessary stuffs
        }
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ignored) {
        }

        Activity_Main.context = getApplicationContext();
        myself = this;
        if (android.os.Build.VERSION.SDK_INT == 8) // FROYO (8)
        {
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        }

        SP_params = PreferenceManager.getDefaultSharedPreferences(this);
        SP_prefEditor = SP_params.edit();
        Tracer = tracerengine.getInstance(SP_params, this);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // this sets the button visible
            getSupportActionBar().setHomeButtonEnabled(true); // makes it clickable
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);// set your own icon
        }
        //Register metrics
        if (SP_params.getBoolean("domodroid_metrics", true)) {
            int repeatTime = 30;  //Repeat alarm time in seconds
            processTimer_for_metrics = (AlarmManager) getSystemService(ALARM_SERVICE);
            intent_for_metrics = new Intent(this, metrics.MetricsServiceReceiver.class);
            pendingIntent_for_metrics = PendingIntent.getBroadcast(this, 0, intent_for_metrics, PendingIntent.FLAG_UPDATE_CURRENT);
            //get metrics every 30s
            processTimer_for_metrics.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeatTime * 1000, pendingIntent_for_metrics);
        }

        initView();

        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);// set your own icon
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);// set your own icon
                drawerToggle.syncState();
            }
        };
        mDrawerLayout.setDrawerListener(drawerToggle);

        //load default pref
        //Added by Doume
        try {
            File storage = new File(Environment.getExternalStorageDirectory() + "/domodroid/.conf/");
            if (!storage.exists()) {
                boolean sucess = storage.mkdirs();
                if (sucess == false)
                    Tracer.i(mytag, "No dir .conf created");
            }
        } catch (Exception e) {
            Tracer.e(mytag, "creating dir /.conf/ error " + e.toString());
        }
        //Configure Tracer tool initial state
        try {
            File logpath = new File(Environment.getExternalStorageDirectory() + "/domodroid/.log/");
            if (!logpath.exists()) {
                boolean sucess = logpath.mkdirs();
                if (sucess == false)
                    Tracer.i(mytag, "No dir .log created");
            }
        } catch (Exception e) {
            Tracer.e(mytag, "creating dir /.log/ error " + e.toString());
        }
        //load_preferences(); //moved to abstract
        load_parameters.load_preferences(SP_params, SP_prefEditor);

        Tracer.set_profile(SP_params);
        // Create .nomedia file, that will prevent Android image gallery from showing domodroid file
        File nomedia = new File(Environment.getExternalStorageDirectory() + "/domodroid/.nomedia");
        try {
            if (!(nomedia.exists())) {
                new FileOutputStream(nomedia).close();
                boolean sucess = nomedia.createNewFile();
                if (sucess == false)
                    Tracer.i(mytag, "No File .nomedia created");
            }
        } catch (Exception e) {
            Tracer.e(mytag, "creating file .nomedia error " + e.toString());
        }

        appname = (ImageView) findViewById(R.id.app_name);

        //todo try to solve history problems like in:
        /*
        STACK_TRACE=java.lang.NullPointerException: Attempt to invoke virtual method 'void java.util.Vector.add(int, java.lang.Object)' on a null object reference
        at activities.Activity_Main.onOptionsItemSelected(Activity_Main.java:1096)
        at android.app.Activity.onMenuItemSelected(Activity.java:3008)
        at android.support.v4.b.l.onMenuItemSelected(FragmentActivity.java:403)
        at android.support.v7.a.f.onMenuItemSelected(AppCompatActivity.java:189)
         */
        history = new Vector<>();

        LoadSelections();

        // Prepare a listener to know when the house organization dialog is closed...
        if (house_listener == null) {
            house_listener = new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    //Redraw after house dialog closed.
                    refresh();
                }
            };
        }

        // Prepare a listener to know when a sync dialog is closed...
        if (sync_listener == null) {
            sync_listener = new DialogInterface.OnDismissListener() {

                public void onDismiss(DialogInterface dialog) {

                    Tracer.i(mytag, "sync dialog has been closed !");

                    // Is it success or fail ?
                    if (((Dialog_Synchronize) dialog).need_refresh) {
                        /*
                        //todo #141 ask user if it's is prefered wifi SSID.
                        ConnectivityManager connectivityManager
                                = (ConnectivityManager) (context.getSystemService(Context.CONNECTIVITY_SERVICE));
                        NetworkInfo[] netInfo = connectivityManager.getAllNetworkInfo();
                        for (NetworkInfo ni : netInfo) {
                            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                                if (ni.isConnected()) {
                                    WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                                    WifiInfo wifiInfo;
                                    wifiInfo = wifiManager.getConnectionInfo();
                                    String ssid = wifiInfo.getSSID().replace("\"", "");
                                    SP_prefEditor.putString("prefered_wifi_ssid",ssid);
                                            SP_prefEditor.commit();
                                }
                        }*/
                        // Sync has been successful : Force to refresh current main view
                        // Store settings to SDcard
                        common_method.save_params_to_file(Tracer, SP_prefEditor, mytag, getApplicationContext());
                        Tracer.i(mytag, "sync dialog requires a refresh !");
                        reload = true;    // Sync being done, consider shared prefs are OK
                        VG_parent.removeAllViews();
                        if (WU_widgetUpdate == null) {
                            Tracer.i(mytag, "OnCreate WidgetUpdate is null startCacheengine!");
                            startCacheEngine(); //if sync dialog is closed
                        }
                        Bundle b = new Bundle();
                        //Notify sync complete to parent Dialog
                        b.putInt("id", 0);
                        b.putString("type", "root");
                        Message msg = new Message();
                        msg.setData(b);
                        if (widgetHandler != null)
                            widgetHandler.sendMessage(msg);    // That should force to refresh Views
                        /* */
                        if (WU_widgetUpdate != null) {
                            WU_widgetUpdate.Disconnect(0);    //That should disconnect all opened widgets from cache engine
                            //widgetUpdate.dump_cache();	//For debug
                            //dont_kill = true;	// to avoid engines kill when onDestroy()
                        }
                        onResume();
                    } else {
                        Tracer.v(mytag, "sync dialog end with no refresh !");

                    }
                    ((Dialog_Synchronize) dialog).need_refresh = false;
                }
            };
        }

        //update thread
        sbanim = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
                    getSupportActionBar().setLogo(R.drawable.app_name2);
                } else if (msg.what == 1) {
                    appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
                    getSupportActionBar().setLogo(R.drawable.app_name3);
                } else if (msg.what == 2) {
                    appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
                    getSupportActionBar().setLogo(R.drawable.app_name1);
                } else if (msg.what == 3) {
                    appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
                    getSupportActionBar().setLogo(R.drawable.app_name4);
                } else if (msg.what == 8000) {
                    Tracer.d(mytag, "Request to display message : 8000");
                    /*
                    if(dialog_message == null) {
						Create_message_box();
					}
					dialog_message.setMessage("Starting cache engine...");
					dialog_message.show();
					 */
                } else if (msg.what == 8001) {
                    AlertDialog.Builder dialog_stats_error = new AlertDialog.Builder(Activity_Main.this);
                    dialog_stats_error.setTitle(R.string.domogik_error);
                    dialog_stats_error.setMessage(R.string.stats_error);
                    dialog_stats_error.show();
                } else if (msg.what == 8002) {
                    AlertDialog.Builder dialog_stats_error = new AlertDialog.Builder(Activity_Main.this);
                    dialog_stats_error.setTitle(R.string.domogik_error);
                    dialog_stats_error.setMessage("ERROR");
                    dialog_stats_error.show();
                } else if (msg.what == 8999) {
                    //Cache engine is ready for use....
                    if (Tracer != null)
                        Tracer.i(mytag, "Cache engine has notified it's ready !");
                    //cache_ready=true;
                    if (end_of_init_requested)
                        end_of_init();
                    PG_dialog_message.dismiss();
                    //refresh view when initial cache ready #33
                    refresh();
                }
            }
        };

        //window manager to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Mains global scroll view
        SV_Main_ScrollView = (ScrollView) findViewById(R.id.Main_ScrollView);
        //Parent view
        VG_parent = (ViewGroup) findViewById(R.id.home_container);

        LL_house_map = new LinearLayout(this);
        LL_house_map.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        LL_house_map.setOrientation(LinearLayout.HORIZONTAL);
        LL_house_map.setPadding(5, 5, 5, 5);

        LL_area = new LinearLayout(this);
        LL_area.setOrientation(LinearLayout.VERTICAL);
        LL_room = new LinearLayout(this);
        LL_room.setOrientation(LinearLayout.VERTICAL);
        LL_activ = new LinearLayout(this);
        LL_activ.setOrientation(LinearLayout.VERTICAL);

        house = new Basic_Graphical_zone(Tracer, getApplicationContext(), 0,
                Graphics_Manager.Names_Agent(this, "House"),
                "",
                "house",
                0, "", null);
        house.setPadding(0, 0, 5, 0);
        map = new Basic_Graphical_zone(Tracer, getApplicationContext(), 0,
                Graphics_Manager.Names_Agent(this, "Map"),
                "",
                "map",
                0, "", null);
        map.setPadding(5, 0, 0, 0);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);

        house.setLayoutParams(param);
        house.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (SP_params.getBoolean("SYNC", false)) {
                    loadWigets(0, "root");
                    historyPosition++;
                    try {
                        history.add(historyPosition, new String[]{"0", "root"});
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        Tracer.e("mytag", "ArrayIndexOutOfBoundsException when adding history navigation");
                        //Todo solved this bug:
                        /*
                        STACK_TRACE=java.lang.ArrayIndexOutOfBoundsException: length=0; index=2
at java.util.Vector.arrayIndexOutOfBoundsException(Vector.java:907)
at java.util.Vector.insertElementAt(Vector.java:590)
at java.util.Vector.add(Vector.java:140)
at activities.Activity_Main$8.onClick(Activity_Main.java:396)
at android.view.View.performClick(View.java:5207)
at android.view.View$PerformClick.run(View.java:21177)
at android.os.Handler.handleCallback(Handler.java:739)
at android.os.Handler.dispatchMessage(Handler.java:95)
at android.os.Looper.loop(Looper.java:148)
at android.app.ActivityThread.main(ActivityThread.java:5441)
at java.lang.reflect.Method.invoke(Native Method)
at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:738)
at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:628)
                         */
                    }

                } else {
                    if (AD_notSyncAlert == null)
                        createAlert();
                    AD_notSyncAlert.show();
                }
            }
        });

        map.setLayoutParams(param);
        map.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (SP_params.getBoolean("SYNC", false)) {
                    //dont_freeze=true;		//To avoid WidgetUpdate engine freeze
                    Tracer.w(mytag, "Before call to Map, Disconnect widgets from engine !");
                    if (WU_widgetUpdate != null) {
                        WU_widgetUpdate.Disconnect(0);    //That should disconnect all opened widgets from cache engine
                        //widgetUpdate.dump_cache();	//For debug
                        //dont_kill = true;	// to avoid engines kill when onDestroy()
                    }
                    INTENT_map = new Intent(Activity_Main.this, Activity_Map.class);
                    Tracer.i(mytag, "Call to Map, run it now !");
                    Tracer.Map_as_main = false;
                    startActivity(INTENT_map);
                } else {
                    if (AD_notSyncAlert == null)
                        createAlert();
                    AD_notSyncAlert.show();
                }
            }
        });

        LL_house_map.addView(house);
        LL_house_map.addView(map);

        init_done = false;
        // Detect if it's the 1st use after installation...
        if (!SP_params.getBoolean("SPLASH", false)) {
            // Yes, 1st use !
            init_done = false;
            reload = false;
            if (backupprefs.exists()) {
                // A backup exists : Ask if reload it
                Tracer.i(mytag, "settings backup found after a fresh install...");

                DialogInterface.OnClickListener reload_listener = new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Tracer.d(mytag, "Reload dialog returns : " + which);
                        if (which == dialog.BUTTON_POSITIVE) {
                            reload = true;
                        } else if (which == dialog.BUTTON_NEGATIVE) {
                            reload = false;
                        }
                        check_answer();
                        dialog.dismiss();
                    }
                };
                AlertDialog.Builder dialog_reload = new AlertDialog.Builder(this);
                dialog_reload.setTitle(getText(R.string.reload_title));
                dialog_reload.setMessage(getText(R.string.home_reload));
                dialog_reload.setPositiveButton(getText(R.string.reloadOK), reload_listener);
                dialog_reload.setNegativeButton(getText(R.string.reloadNO), reload_listener);
                //todo #94
                dialog_reload.show();
                init_done = false;    //A choice is pending : Rest of init has to be completed...
            } else {
                //No settings backup found
                Tracer.i(mytag, "no settings backup found after fresh install...");
                end_of_init_requested = true;
                // open server config view
                Intent helpI = new Intent(Activity_Main.this, Preference.class);
                //todo #94
                //startActivity(helpI);
            }
        } else {
            // It's not the 1st use after fresh install
            // This method will be followed by 'onResume()'
            end_of_init_requested = true;
        }
        if (SP_params.getBoolean("SYNC", false)) {
            //A config exists and a sync as been done by past.
            if (WU_widgetUpdate == null) {
                Tracer.i(mytag, "OnCreate Params splash is false and WidgetUpdate is null startCacheengine!");
                startCacheEngine();//if sync is done on create
            }
        }
        // Changelog view
        changelog changelog = new changelog(this);
        if (changelog.firstRun())
            try {
                changelog.getLogDialog().show();
            } catch (Exception e) {
                Tracer.e(mytag, e.toString());
            }

        Tracer.v(mytag, "OnCreate() complete !");
        // End of onCreate (UIThread)
    }

    @Override
    public void onResume() {
        super.onResume();
        //get metrics every 30s
        if (SP_params.getBoolean("domodroid_metrics", true)) {
            int repeatTime = 30;  //Repeat alarm time in seconds
            AlarmManager processTimer = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, metrics.MetricsServiceReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            processTimer.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), repeatTime * 1000, pendingIntent);
        }
        Tracer.v(mytag + ".onResume", "Check if initialize requested !");
        if (!init_done) {
            Tracer.v(mytag + ".onResume", "Init not done!");
            if (SP_params.getBoolean("SPLASH", false)) {
                Tracer.v(mytag + ".onResume", "params Splash is false !");
                //cache_ready = false;
                //try to solve 1rst launch and orientation problem
                Tracer.v(mytag + ".onresume", "Init not done! and params Splash is false startCacheengine!");
                //startCacheEngine();
                //end_of_init();		//Will be done when cache will be ready
            }
        } else {
            Tracer.v(mytag + ".onResume", "Init done!");
            end_of_init_requested = true;
            if (WU_widgetUpdate != null) {
                Tracer.v(mytag + ".onResume", "Widget update is not null so wakeup widget engine!");
                WU_widgetUpdate.wakeup();        //If cache ready, that'll execute end_of_init()
            }
        }
        if (end_of_init_requested)
            refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        Tracer.v(mytag + ".onPause", "Going to background !");
        if (WU_widgetUpdate != null) {
            if (!Tracer.Map_as_main) {
                // We're the main initial activity
                WU_widgetUpdate.set_sleeping();    //Don't cancel the cache engine : only freeze it
            }
        }
        //Stop metrics.
        if (SP_params.getBoolean("domodroid_metrics", true)) {
            processTimer_for_metrics.cancel(pendingIntent_for_metrics);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.WM_Agent = null;
        widgetHandler = null;
        if (SP_params.getFloat("API_VERSION", 0) >= 0.9f) {
            SharedPreferences.Editor prefEditor = SP_params.edit();
            JSONArray cached_dump = null;
            if (WU_widgetUpdate != null) {
                Tracer.d("#124", "dump cache");
                try {
                    cached_dump = WU_widgetUpdate.dump_cache_to_json();
                    // save last value to sharedparams to load them later
                    prefEditor.putString("sensor_saved_value", cached_dump.toString());
                    Tracer.d("#124", cached_dump.toString());
                    Tracer.d("#124", "dump cached");
                    // save current time stamp to know when the pass was exit.
                    long currentTimestamp = (System.currentTimeMillis() / 1000);
                    Tracer.d("#124", "sensor_saved_timestamp" + currentTimestamp);
                    prefEditor.putString("sensor_saved_timestamp", String.valueOf(currentTimestamp));
                } catch (JSONException e) {
                    Tracer.e("#124", "sensor_saved at exit error");
                    e.printStackTrace();
                }
            }
            prefEditor.commit();
        }

        //Stop metrics.
        if (SP_params.getBoolean("domodroid_metrics", true)) {
            processTimer_for_metrics.cancel(pendingIntent_for_metrics);
        }

        if (WU_widgetUpdate != null) {
            WU_widgetUpdate.Disconnect(0);    //remove all pending subscribings
            if (!Tracer.Map_as_main) {
                // We're the main initial activity
                Tracer.v(mytag + ".onDestroy", "cache engine set to sleeping !");
                //PM_WakeLock.release();    // We allow screen shut, now...
                WU_widgetUpdate.set_sleeping();    //Don't cancel the cache engine : only freeze it
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
        if (PG_dialog_message != null)
            return;
        PG_dialog_message = new ProgressDialog(this);
        PG_dialog_message.setTitle(getText(R.string.please_wait));
        PG_dialog_message.setMessage(getText(R.string.init_in_process));
        //dialog_reload.setPositiveButton("OK", message_listener);

    }

	/*
    public void force_DB_update() {
		if(WU_widgetUpdate != null) {
			WU_widgetUpdate.refreshNow();
		}
	}
	*/

    private void createAlert() {
        AD_notSyncAlert = new AlertDialog.Builder(this);
        AD_notSyncAlert.setTitle(getText(R.string.warning));
        AD_notSyncAlert.setMessage(getText(R.string.not_sync));
        AD_notSyncAlert.setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

    }

    private void end_of_init() {
        // Finalize screen appearances
        if (Tracer == null)
            Tracer = Tracer.getInstance(this);
        Tracer.v(mytag, "end_of_init Main Screen..");
        if (!reload) {
            //alertDialog not sync splash
            if (AD_notSyncAlert == null)
                createAlert();
        }
        //splash
        if (!SP_params.getBoolean("SPLASH", false)) {
            Dialog_Splash dialog_splash = new Dialog_Splash(this);
            dialog_splash.show();
            SP_prefEditor.clear();
            SP_prefEditor.putBoolean("SPLASH", true);
            SP_prefEditor.commit();
            return;
        }
        end_of_init_requested = false;

        if (history != null)
            Tracer.d(mytag, "OnactivityResult end of init history=" + history.toString() + " historyposition=" + historyPosition);

        if (!init_done) {
            history = null;        //Free resource
            history = new Vector<>();
        }


        //load widgets
        if (widgetHandler == null) {
            Tracer.v(mytag, "Starting WidgetHandler thread !");
            widgetHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    //#107 around here
                    Tracer.d("debug map bak #107", msg.getData().toString() + " history= " + history.toString() + " hystoryposition= " + historyPosition);
                    try {
                        if (msg.getData().getBoolean("refresh")) {
                            refresh();
                        } else if (!msg.getData().getBoolean("refresh")) {
                            historyPosition++;
                            loadWigets(msg.getData().getInt("id"), msg.getData().getString("type"));
                            //redraw the scrollview at the top position of the screen
                            SV_Main_ScrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    SV_Main_ScrollView.scrollTo(0, 0);
                                }
                            });
                            Tracer.v(mytag + ".widgetHandler", "add history " + msg.getData().getInt("id") + " " + msg.getData().getString("type"));
                            history.add(historyPosition, new String[]{msg.getData().getInt("id") + "", msg.getData().getString("type")});
                        }
                    } catch (Exception e) {
                        Tracer.e(mytag + ".widgetHandler", "handler error into loadWidgets");
                        Tracer.e("debug map bak", e.toString());
                    }
                }
            };
        }
        if (WM_Agent == null) {
            Tracer.v(mytag, "Starting wAgent !");
            WM_Agent = new Widgets_Manager(Tracer, widgetHandler);
            WM_Agent.widgetupdate = WU_widgetUpdate;
        }
        /*
        if(T_starting != null) {
			T_starting.cancel();
			T_starting.setText("Creating widgets....");
			T_starting.setDuration(Toast.LENGTH_SHORT);
			T_starting.show();
		}
		*/

        //dont_kill = false;	//By default, the onDestroy activity will also kill engines

        listePlace = (ListView) findViewById(R.id.listplace);
        try {
            listItem = new ArrayList<>();
            adapter_map = new SimpleAdapter(getBaseContext(), listItem,
                    R.layout.item_in_listview_navigation_drawer, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon});
            listePlace.setAdapter(adapter_map);
            listePlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    HashMap<String, String> map = listItem.get(position);
                    if (map.get("type").equals("action")) {
                        if (map.get("name").equals(context.getApplicationContext().getResources().getString(R.string.action_back))) {
                            Tracer.v(mytag, "clic move back in navigation drawer");
                            if (historyPosition != 0) {
                                historyPosition--;
                            }
                            refresh();
                        }
                    } else {
                        //redraw the scrollview at the top position of the screen
                        SV_Main_ScrollView.post(new Runnable() {
                            @Override
                            public void run() {
                                SV_Main_ScrollView.scrollTo(0, 0);
                            }
                        });
                        loadWigets(Integer.parseInt(map.get("id")), map.get("type"));
                        historyPosition++;
                        try {
                            history.add(historyPosition, new String[]{map.get("id"), map.get("type")});
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            Tracer.e("mytag", "ArrayIndexOutOfBoundsException when adding history navigation");
                            //Todo solved this bug:
                        }
                        if (map.get("type").equals("room")) {
                            //close navigationdrawer if select a room
                            mDrawerLayout.closeDrawer(GravityCompat.START);
                        }
                    }
                }
            });
            listePlace.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Tracer.d(mytag, " On Longclick Place selected at Position = " + position);
                    HashMap<String, String> map = listItem.get(position);
                    if (map.get("type").equals("action")) {
                        Tracer.d(mytag, "long clic on action button");
                        return false;
                    } else {
                        Tracer.d(mytag, "On click Place selected at Position = " + map.toString());
                        //Todo delete this place directly from navigation drawer
                        return false;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if ((SP_params.getBoolean("START_ON_MAP", false) && (!Tracer.force_Main))) {
            //#125 wait cache ready
            //Solve #2029
            if (SP_params.getBoolean("SYNC", false)) {
                Tracer.v(mytag, "Direct start on Map requested...");
                Tracer.Map_as_main = true;        //Memorize that Map is now the main screen
                INTENT_map = new Intent(Activity_Main.this, Activity_Map.class);
                startActivity(INTENT_map);
            } else {
                if (AD_notSyncAlert == null)
                    createAlert();
                AD_notSyncAlert.show();
            }
        } else {
            Tracer.force_Main = false;    //Reset flag 'called from Map view'
            if (SP_params.getBoolean("SYNC", false)) {
                if (!init_done) {
                    historyPosition = 0;
                    try {
                        history.add(historyPosition, new String[]{"0", "root"});
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                        Tracer.e("mytag", "ArrayIndexOutOfBoundsException when adding history navigation");
                        //Todo solved this bug:
                    }
                    refresh();
                }
            } else {
                if (AD_notSyncAlert == null)
                    createAlert();
                AD_notSyncAlert.show();
            }
        }

        init_done = true;

    }


    /*
     * Check the answer after the proposal to reload existing settings (fresh install)
     */

    private void check_answer() {
        Tracer.v(mytag, "reload choice done..");
        if (reload) {
            // If answer is 'yes', load preferences from backup
            Tracer.v(mytag, "reload settings..");
            //loadSharedPreferencesFromFile(backupprefs); //moved to Abstract
            if (load_parameters.loadSharedPreferencesFromFile(backupprefs, SP_prefEditor, Tracer)) {
                LoadSelections();    // to set panel with known values
            }
            run_sync_dialog(); //after reload prefs at start

        } else {
            Tracer.v(mytag, "Settings not reloaded : clear database..");
            File database = new File(Environment.getExternalStorageDirectory() + "/domodroid/.conf/domodroid.db");
            try {
                if (database.exists()) {
                    boolean sucess = database.delete();
                    if (sucess == false)
                        Tracer.i(mytag, "Database not deleted");
                }
            } catch (Exception e) {
                Tracer.e(mytag, "deleting domodroid.db error " + e.toString());
            }
            // open server config view
            Intent helpI = new Intent(Activity_Main.this, Preference.class);
            startActivity(helpI);
        }

        if (!init_done) {
            // Complete the UI init
            end_of_init();
        }
    }

    /*
    private void loadSharedPreferencesFromFile(File src) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SP_prefEditor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                Tracer.i(mytag, "Loading pref : " + key + " -> " + v.toString());
                if (v instanceof Boolean)
                    SP_prefEditor.putBoolean(key, (Boolean) v);
                else if (v instanceof Float)
                    SP_prefEditor.putFloat(key, (Float) v);
                else if (v instanceof Integer)
                    SP_prefEditor.putInt(key, (Integer) v);
                else if (v instanceof Long)
                    SP_prefEditor.putLong(key, (Long) v);
                else if (v instanceof String)
                    SP_prefEditor.putString(key, (String) v);
            }
            SP_prefEditor.commit();
            LoadSelections();    // to set panel with known values
        } catch (IOException e) {
            Tracer.e(mytag, "Can't load preferences file");
            Tracer.e(mytag, e.toString());
        } catch (ClassNotFoundException e) {
            Tracer.e(mytag, "Can't load preferences file");
            Tracer.e(mytag, e.toString());
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                Tracer.e(mytag, "Can't load preferences file");
                Tracer.e(mytag, ex.toString());
            }
        }
    }
    */

    private void loadWigets(int id, String type) {
        Tracer.i(mytag + ".loadWidgets", "Construct main View id=" + id + " type=" + type);
        VG_parent.removeAllViews();

        LL_house_map.removeAllViews();
        LL_house_map.addView(house);
        LL_house_map.addView(map);
        LL_area.removeAllViews();
        LL_activ.removeAllViews();
        LL_room.removeAllViews();

        try {
            int mytype = 0;
            switch (type) {
                case "root":
                    VG_parent.addView(LL_house_map);    // House & map
                    if (!by_usage) {
                        // Version 0.2 or un-force by_usage : display house, map and areas
                        LL_area = WM_Agent.loadAreaWidgets(this, LL_area, SP_params);
                        VG_parent.addView(LL_area);    //and areas

                        LL_activ = WM_Agent.loadActivWidgets(this, 1, "root", LL_activ, SP_params, mytype);//add widgets in root
                    } else {
                        // by_usage
                        //TODO #19 change 1 in loadRoomWidgets by the right value.
                        int load_area;
                        try {
                            load_area = Integer.valueOf(SP_params.getString("load_area_at_start", "1"));
                        } catch (Exception e) {
                            Tracer.e(mytag, e.toString());
                            load_area = 1;
                        }
                        //LL_room = WM_Agent.loadRoomWidgets(this, 1, LL_room, SP_params);    //List of known usages 'as rooms'
                        LL_room = WM_Agent.loadRoomWidgets(this, load_area, LL_room, SP_params);    //List of known usages 'as rooms'
                        VG_parent.addView(LL_room);

                        //LL_activ = WM_Agent.loadActivWidgets(this, 1, "area", LL_activ, SP_params, mytype);//add widgets in area 1
                        LL_activ = WM_Agent.loadActivWidgets(this, load_area, "area", LL_activ, SP_params, mytype);//add widgets in area 1
                    }
                    VG_parent.addView(LL_activ);
                /*Should never arrive in this type.
                }else if(type.equals("house")) {
				//Only possible if Version 0.2 or un-force by_usage (the 'house' is never proposed to be clicked)
				VG_parent.addView(LL_house_map);	// House & map
				LL_area = WM_Agent.loadAreaWidgets(this, LL_area, SP_params);
				VG_parent.addView(LL_area);	//and areas
				LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ,SP_params, mytype);
				VG_parent.addView(LL_activ);
				 */
                    break;
                case "statistics":
                    //Only possible if by_usage (the 'stats' is never proposed with Version 0.2 or un-force by_usage)
                    LL_activ = WM_Agent.loadActivWidgets(this, -1, type, LL_activ, SP_params, mytype);
                    VG_parent.addView(LL_activ);

                    break;
                case "area":
                    //Only possible if Version 0.2 or un-force by_usage (the area 'usage' is never proposed to be clicked)
                    if (!by_usage) {
                        VG_parent.addView(LL_house_map);    // House & map
                    }
                    LL_room = WM_Agent.loadRoomWidgets(this, id, LL_room, SP_params);//Add room in this area
                    VG_parent.addView(LL_room);

                    LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ, SP_params, mytype);//add widgets in this area
                    VG_parent.addView(LL_activ);

                    break;
                case "room":
                    LL_activ = WM_Agent.loadActivWidgets(this, id, type, LL_activ, SP_params, mytype);//add widgets in this room
                    VG_parent.addView(LL_activ);
                    break;
            }
            update_navigation_menu();
            Tracer.d(mytag, "List item= " + listItem.toString());
        } catch (Exception e) {
            Tracer.e(mytag, "Can't load area/room or widgets");
            Tracer.e(mytag, e.toString());
        }
    }

    private void LoadSelections() {
        by_usage = SP_params.getBoolean("BY_USAGE", false);
    }

    private void run_sync_dialog() {
        AD_wifi_prefered = new AlertDialog.Builder(this);
        AD_wifi_prefered.setTitle(getText(R.string.sync_wifi_preferred_title));
        AD_wifi_prefered.setMessage(getText(R.string.sync_wifi_preferred_message));
        AD_wifi_prefered.setPositiveButton(getText(R.string.reloadOK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    //replace and save "SSID" by SSID
                    SP_prefEditor.putString("prefered_wifi_ssid", ssid.substring(1, ssid.length()-1));
                    SP_prefEditor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.error_getting_wifi_ssid, Toast.LENGTH_LONG);
                }
                dialog.dismiss();
            }
        });
        AD_wifi_prefered.setNegativeButton(getText(R.string.reloadNO), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AD_wifi_prefered.show();

        //change sync parameter in case it fail.
        SP_prefEditor.putBoolean("SYNC", false);
        SP_prefEditor.commit();
        if (!(WU_widgetUpdate == null)) {
            WU_widgetUpdate.Disconnect(0);    //Disconnect all widgets owned by Main
        }
        if (DIALOG_dialog_sync == null)
            DIALOG_dialog_sync = new Dialog_Synchronize(Tracer, this, SP_params);
        DIALOG_dialog_sync.reload = reload;
        DIALOG_dialog_sync.setOnDismissListener(sync_listener);
        DIALOG_dialog_sync.setParams(SP_params);
        DIALOG_dialog_sync.show();
        DIALOG_dialog_sync.startSync();
    }

    public void onClick(View v) {
        //dont_freeze = false;		// By default, onPause() will stop WidgetUpdate engine...
        //ALL other that are not explicitly used
        if (v.getTag().equals("reload_cancel")) {
            Tracer.v(mytag, "Choosing no reload settings");
            reload = false;
            synchronized (waiting_thread) {
                waiting_thread.notifyAll();
            }
        } else if (v.getTag().equals("reload_ok")) {
            Tracer.v(mytag, "Choosing settings reload");
            reload = true;
            synchronized (waiting_thread) {
                waiting_thread.notifyAll();
            }
        }
    }

    private void startCacheEngine() {
        Cache_management.checkcache(Tracer, myself);
        if (WU_widgetUpdate == null) {
            this.Create_message_box();
            PG_dialog_message.setMessage(getText(R.string.loading_cache));
            PG_dialog_message.show();
            Tracer.i(mytag, "Starting WidgetUpdate cache engine !");
            WU_widgetUpdate = WidgetUpdate.getInstance();
            WU_widgetUpdate.set_handler(sbanim, 0);    //put our main handler into cache engine (as Main)
            Boolean result = WU_widgetUpdate.init(Tracer, this, SP_params);
            Tracer.i(mytag, "widgetupdate_wakup");
            WU_widgetUpdate.wakeup();
            if (!result)
                return;
        }
        Tracer.set_engine(WU_widgetUpdate);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (SP_params.getBoolean("SYNC", false)) {
            float api_version = SP_params.getFloat("API_VERSION", 0);
            if (api_version < 0.7f) {
                menu.findItem(R.id.menu_butler).setVisible(false);
            }
        }
        menu.findItem(R.id.menu_exit).setVisible(!SP_params.getBoolean("START_ON_MAP", false));

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mainMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            Tracer.d(mytag, "clic on drawertoggle");
            return true;
        }
        //normal menu call.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_butler:
                if (SP_params.getBoolean("SYNC", false)) {
                    Intent intent = new Intent(this, Main.class);
                    this.startActivity(intent);
                    return true;
                } else {
                    if (AD_notSyncAlert == null)
                        createAlert();
                    AD_notSyncAlert.show();
                    return true;
                }
            case R.id.menu_exit:
                //Disconnect all opened sessions....
                Tracer.v(mytag + "Exit", "Stopping WidgetUpdate thread !");
                this.WM_Agent = null;
                widgetHandler = null;
                Tracer.set_engine(null);
                if (!(WU_widgetUpdate == null)) {
                    WU_widgetUpdate.Disconnect(0);    //Disconnect all widgets owned by Main
                }
                //dont_kill = false;		//To force OnDestroy() to also kill engines
                //And stop main program
                finish();
                /*todo uncomment this block to really quit the apps
                //but it ctash in on destroy as some values are not initialize
                Intent intent = new Intent(this, Activity_Main.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Exit me", true);
                startActivity(intent);
                */
                return true;
            case R.id.menu_house_config:
                Tracer.v(mytag + ".onclick()", "Call to House settings screen");
                Dialog_House DIALOG_house_set = new Dialog_House(Tracer, SP_params, myself);
                DIALOG_house_set.show();
                DIALOG_house_set.setOnDismissListener(house_listener);
                return true;
            case R.id.menu_preferences:
                //Prepare a normal preferences activity.
                Intent helpI = new Intent(Activity_Main.this, Preference.class);
                startActivity(helpI);
                return true;
            case R.id.menu_about:
                //dont_freeze=true;		//To avoid WidgetUpdate engine freeze
                Intent helpI1 = new Intent(Activity_Main.this, Activity_About.class);
                startActivity(helpI1);
                return true;
            case R.id.menu_stats:
                try {
                    if (SP_params.getBoolean("SYNC", false)) {
                        loadWigets(0, "statistics");
                        historyPosition++;
                        try {
                            history.add(historyPosition, new String[]{"0", "statistics"});
                        } catch (ArrayIndexOutOfBoundsException e) {
                            e.printStackTrace();
                            Tracer.e("mytag", "ArrayIndexOutOfBoundsException when adding history navigation");
                            //Todo solved this bug:
                        }
                    } else {
                        if (AD_notSyncAlert == null)
                            createAlert();
                        AD_notSyncAlert.show();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                    //todo find why java.lang.ArrayIndexOutOfBoundsException: 1 > 0
                    //Maybe when not sync or because of  E/activities.Activity_Main(12801): Can not refresh this view

                }
                return true;
            case R.id.menu_sync:
                // click on 'sync' button into Sliding_Drawer View
                run_sync_dialog();        // And run a resync with Rinor server
                return true;
            case R.id.menu_domogik_admin:
                //launch a webview of domogik admin
                Intent intent_webview = new Intent(context, webview_domogik_admin.class);
                startActivity(intent_webview);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    //Physical button keycode 82 is menu button
    //Physical button keycode 4 is back button
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Tracer.v(mytag, "onKeyUp keyCode = " + keyCode);
        if ((keyCode == 82 || keyCode == 4) && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        } else if ((keyCode == 4) && historyPosition > 0) {
            if (history != null) {
                Tracer.e("debug map bak", " history= " + history.toString() + " hystoryposition= " + historyPosition);
                historyPosition--;
                refresh();
                return false;
            } else {
                Tracer.e(mytag, "history is null at this point");
            }
        } else if ((keyCode == 82) && mainMenu != null) {
            mainMenu.performIdentifierAction(R.id.menu_overflow, 0);
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    //this is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
    {
        //Check if sync as been done by past to avoid crash
        //on orientation change when user have to reload saved parameters.
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
        if (SP_params.getBoolean("SYNC", false))
            refresh();
    }

    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        itemSelection(mSelectedId);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //save selected item so it will remains same even after orientation change
        outState.putInt("SELECTED_ID", mSelectedId);
    }

    private void initView() {
        mDrawer = (NavigationView) findViewById(R.id.home_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_home_layout);
    }

    private void itemSelection(int mSelectedId) {
        Tracer.d(mytag, "Selected this item from navigation drawer: " + mSelectedId);
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void refresh() {
        try {
            loadWigets(Integer.parseInt(history.elementAt(historyPosition)[0]), history.elementAt(historyPosition)[1]);
        } catch (Exception e) {
            Tracer.e(mytag, "Can not refresh this view");
        }
    }

    public void update_navigation_menu() {
        adapter_map.notifyDataSetChanged();
        listePlace.setAdapter(new SimpleAdapter(getBaseContext(), listItem,
                R.layout.item_in_listview_navigation_drawer, new String[]{"name", "icon"}, new int[]{R.id.name, R.id.icon}));
        Tracer.d(mytag, "Update navigation drawer listview");
    }

    /*
    private void load_preferences() {
        //Load default value to avoid crash.
        String currlogpath = SP_params.getString("LOGNAME", "");
        if (currlogpath.equals("")) {
            //Not yet existing prefs : Configure debugging by default, to configure Tracer
            currlogpath = Environment.getExternalStorageDirectory() + "/domodroid/.log/";
            SP_prefEditor.putString("LOGPATH", currlogpath);
            SP_prefEditor.putString("LOGNAME", "Domodroid.txt");
            SP_prefEditor.putBoolean("SYSTEMLOG", false);
            SP_prefEditor.putBoolean("TEXTLOG", false);
            SP_prefEditor.putBoolean("SCREENLOG", false);
            SP_prefEditor.putBoolean("LOGCHANGED", true);
            SP_prefEditor.putBoolean("LOGAPPEND", false);
            //set other default value
            SP_prefEditor.putBoolean("twocol_lanscape", true);
            SP_prefEditor.putBoolean("twocol_portrait", true);
        } else {
            SP_prefEditor.putBoolean("LOGCHANGED", true);        //To force Tracer to consider current settings
        }
        //prefEditor.putBoolean("SYSTEMLOG", false);		// For tests : no system logs....
        SP_prefEditor.putBoolean("SYSTEMLOG", true);        // For tests : with system logs....
        SP_prefEditor.commit();
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Tracer.i(mytag, "OnactivityResult requestcode=" + requestCode + " resultcode=" + resultCode + " intent=" + data);
        //because it will be follow by on resume() method
        init_done = true;
    }
}

