package activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import Entity.Entity_Feature;
import activities.Sliding_Drawer.OnPanelListener;
import database.WidgetUpdate;
import map.Dialog_Help;
import map.Dialog_Move;
import map.MapView;
import misc.CopyFile;
import misc.tracerengine;

public class Activity_Map extends AppCompatActivity implements OnPanelListener, OnClickListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Sliding_Drawer panel;
    private Sliding_Drawer topPanel;
    private Sliding_Drawer bottomPanel;
    public static Dialog dialog_feature;
    private Entity_Feature[] listFeature;
    private HashMap<String, String> map;

    private Vector<String> list_usable_files;
    private MapView mapView;
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences params;
    private ViewGroup panel_widget;
    private ViewGroup panel_button;

    private ListView listeMap;
    private ArrayList<HashMap<String, String>> listItem;
    private Animation animation1;
    private Animation animation2;
    private TextView menu_green;

    private WidgetUpdate widgetUpdate;
    private static Handler sbanim;
    private String[] files = null;
    private File destFile = null;
    private String extension;
    private String fileName;
    private tracerengine Tracer = null;
    private String owner = "Map";
    private Boolean dont_freeze = false;
    private final String mytag = "Activity_Map";

    private static final int PICK_IMAGE = 1;

    /*
     * WARNING : this class does'nt access anymore directly the database
     * 		It must use methods located into WidgetUpdate engine
     * 		which is permanently connected to local database
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO add normal menu
        params = PreferenceManager.getDefaultSharedPreferences(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Tracer = tracerengine.getInstance(params, this);
        //prefEditor = params.edit();
        mapView = new MapView(Tracer, this, params);
        mapView.setParams(params);
        mapView.setUpdate(params.getInt("UPDATE_TIMER", 300));
        setContentView(R.layout.activity_map);
        ViewGroup parent = (ViewGroup) findViewById(R.id.map_container);

        //titlebar
        final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
        titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title", 40));

        //menu button
        TextView menu_white = (TextView) findViewById(R.id.menu_button1);
        menu_white.setOnClickListener(this);
        menu_white.setTag("menu");
        menu_green = (TextView) findViewById(R.id.menu_button2);
        menu_green.setVisibility(View.GONE);

        animation1 = new AlphaAnimation(0.0f, 1.0f);
        animation1.setDuration(500);
        animation2 = new AlphaAnimation(1.0f, 0.0f);
        animation2.setDuration(500);

        //read files from SDCARD + create directory

        createDirIfNotExists();
        File f = new File(Environment.getExternalStorageDirectory() + "/domodroid/");
        if (f.isDirectory()) {
            files = f.list();
            //Reorder method
            List<String> words = new ArrayList<>();
            Collections.addAll(words, files);
            Collections.sort(words);
            files = words.toArray(new String[words.size()]);

        }

        build_maps_list();

        //sliding drawer
        topPanel = panel = (Sliding_Drawer) findViewById(R.id.map_slidingdrawer);
        panel.setOnPanelListener(this);
        panel.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        mapView.setTopDrawer(topPanel);
        mapView.setBottomDrawer(bottomPanel);

        panel_widget = (ViewGroup) findViewById(R.id.panelWidget);
        panel_button = (ViewGroup) findViewById(R.id.panelButton);

        mapView.setPanel_widget(panel_widget);
        mapView.setPanel_button(panel_button);

        //add remove buttonObject engine = (Object)widgetUpdate;
        Button add = new Button(this);
        add.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1));
        add.setPadding(10, 13, 10, 13);
        add.setText(R.string.map_button1);
        add.setTextColor(Color.parseColor("#cfD1D1"));
        add.setTextSize(15);
        add.setTag("add");
        add.setBackgroundColor(Color.parseColor("#00000000"));
        add.setOnClickListener(this);

        Button help = new Button(this);
        help.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1));
        help.setPadding(10, 13, 10, 13);
        help.setText(R.string.map_button3);
        help.setTextColor(Color.parseColor("#cfD1D1"));
        help.setTextSize(15);
        help.setTag("help");
        help.setBackgroundColor(Color.parseColor("#00000000"));
        help.setOnClickListener(this);

        Button remove = new Button(this);
        remove.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1));
        remove.setPadding(10, 13, 10, 13);
        remove.setText(R.string.map_button2);
        remove.setTextColor(Color.parseColor("#cfD1D1"));
        remove.setTextSize(15);
        remove.setTag("remove");
        remove.setBackgroundColor(Color.parseColor("#00000000"));
        remove.setOnClickListener(this);

        Button remove_all = new Button(this);
        remove_all.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1));
        remove_all.setPadding(10, 13, 10, 13);
        remove_all.setText(R.string.map_button2b);
        remove_all.setTextColor(Color.parseColor("#cfD1D1"));
        remove_all.setTextSize(15);
        remove_all.setTag("remove_all");
        remove_all.setBackgroundColor(Color.parseColor("#00000000"));
        remove_all.setOnClickListener(this);

        Button move = new Button(this);
        move.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT, 1));
        move.setPadding(10, 13, 10, 13);
        move.setText(R.string.map_button2c);
        move.setTextColor(Color.parseColor("#cfD1D1"));
        move.setTextSize(15);
        move.setTag("move");
        move.setBackgroundColor(Color.parseColor("#00000000"));
        move.setOnClickListener(this);


        panel_button.addView(add);
        panel_button.addView(help);
        panel_button.addView(remove);
        panel_button.addView(move);
        panel_button.addView(remove_all);


        bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);

        dialog_feature = new Dialog(this);


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.Add_widget_title);

        startCacheEngine();        //Get reference to WidgetUpdate engine
        //When back, the engine should be ready.... (mini widgets and widgets require it to connect !)

        listFeature = widgetUpdate.requestFeatures();

        //listview feature
        ListView listview_feature = new ListView(this);
        ArrayList<HashMap<String, String>> listItem1 = new ArrayList<>();
        if (listFeature != null) {
            int size = listFeature.length;
            Entity_Feature feature;
            for (int pos = 0; pos < size; ++pos) {
                feature = listFeature[pos];
                if (feature != null) {
                    map = new HashMap<>();
                    map.put("name", feature.getName());
                    if (feature.getParameters().contains("command")) {
                        map.put("type", getString(R.string.command) + "-" + feature.getValue_type());
                    } else {
                        map.put("type", feature.getValue_type());
                    }
                    try {
                        map.put("state_key", getResources().getString(Graphics_Manager.getStringIdentifier(getApplicationContext(), feature.getState_key().toLowerCase())));
                    } catch (Exception e) {
                        Tracer.d(mytag, "no translation for: " + feature.getState_key());
                        map.put("state_key", feature.getState_key());
                    }
                    map.put("icon", Integer.toString(feature.getRessources()));
                    listItem1.add(map);
                }
            }
        }
        int i;
        if (list_usable_files != null) {
            for (i = 0; i < list_usable_files.size(); i++) {
                map = new HashMap<>();
                map.put("name", getText(R.string.go_to_Map).toString());
                map.put("type", "");
                map.put("state_key", list_usable_files.elementAt(i));
                map.put("icon", Integer.toString(R.drawable.map_next));
                listItem1.add(map);
            }
        }
        if ((listItem1 != null) && (listItem1.size() > 0)) {
            SimpleAdapter adapter_feature = new SimpleAdapter(getBaseContext(), listItem1,
                    R.layout.item_feature, new String[]{"name", "type", "state_key", "icon"}, new int[]{R.id.name, R.id.description, R.id.state_key, R.id.icon});
            listview_feature.setAdapter(adapter_feature);
            listview_feature.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position < listFeature.length) {
                        // It's a feature element
                        mapView.map_id = -1;
                        mapView.temp_id = listFeature[position].getId();
                    } else {
                        //It's a map switch element
                        mapView.temp_id = -1;
                        mapView.map_id = (position - listFeature.length) + 99999;
                        Tracer.v(mytag, "map_id = <" + mapView.map_id + "> , map selected <" + list_usable_files.elementAt(mapView.map_id - 99999) + ">");
                    }
                    mapView.setAddMode(true);
                    dialog_feature.dismiss();
                }
            });
        }

        builder.setView(listview_feature);
        dialog_feature = builder.create();

        if (!list_usable_files.isEmpty()) {
            mapView.initMap();
            //mapView.updateTimer();
            parent.addView(mapView);
        } else {
            Dialog_Help dialog_help = new Dialog_Help(this);
            dialog_help.show();
        }
        //update thread
        sbanim = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                /*
                if(msg.what==0){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
				}else if(msg.what==1){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
				}else if(msg.what==2){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
				}else if(msg.what==3){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
				}
				 */
            }
        };

        try {
            mapView.drawWidgets();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCacheEngine() {
        if (widgetUpdate == null) {
            Tracer.i(mytag, "Starting WidgetUpdate engine !");
            widgetUpdate = WidgetUpdate.getInstance();
            //Map is'nt the first caller, so init is'nt required (already done by View)
            widgetUpdate.set_handler(sbanim, 1);    //Put our main handler to cache engine (as Map)

            widgetUpdate.wakeup();
        }
        tracerengine.set_engine(widgetUpdate);
        Tracer.v(mytag, "WidgetUpdate engine connected !");
    }

    private void build_maps_list() {

        if (listeMap != null)
            listeMap = null;
        if (listItem != null)
            listItem = null;
        if (list_usable_files != null)
            list_usable_files = null;

        //list Map
        listeMap = (ListView) findViewById(R.id.listeMap);
        listItem = new ArrayList<>();
        list_usable_files = new Vector<>();
        int i;
        for (i = 0; i < files.length; i++) {
            //#1968 don't list file without drawable extension or hidden
            if (!files[i].startsWith(".") && (files[i].toLowerCase().endsWith(".png") || files[i].toLowerCase()
                    .endsWith(".jpg") || files[i].toLowerCase().endsWith(".jpeg") || files[i].toLowerCase()
                    .endsWith(".svg"))) {
                try {
                    list_usable_files.add(files[i]);
                    map = new HashMap<>();
                    map.put("name", files[i].substring(0, files[i].lastIndexOf('.')));
                    map.put("position", String.valueOf(i));
                    listItem.add(map);
                } catch (Exception badfileformat) {
                    Tracer.e(mytag, "Good extension but can't load file");
                }
            }
        }
        if (mapView != null)
            mapView.setFiles(list_usable_files);

        if ((Tracer != null) && (Tracer.Map_as_main)) {
            // Add possibility to invoke Main activity
            map = new HashMap<>();
            map.put("name", getText(R.string.go_Main).toString());
            map.put("position", String.valueOf(i));
            listItem.add(map);
            i++;
        }
        //Add an element in map list to ADD a map
        map = new HashMap<>();
        map.put("name", getText(R.string.map_select_file).toString());
        map.put("position", String.valueOf(i));
        listItem.add(map);
        i++;

        SimpleAdapter adapter_map = new SimpleAdapter(getBaseContext(), listItem,
                R.layout.item_map, new String[]{"name"}, new int[]{R.id.name});
        listeMap.setAdapter(adapter_map);
        listeMap.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tracer.v(mytag, "On click Map selected at Position = " + position);
                int last_map = list_usable_files.size() - 1;
                if ((position <= last_map) && (position > -1)) {
                    mapView.setCurrentFile(position);
                    mapView.initMap();
                } else {
                    //add the return to main screen menu entry if map start mode
                    if ((position == last_map + 1) && (Tracer.Map_as_main)) {
                        //Go to main screen...
                        Tracer.force_Main = true;    //Flag to allow widgets display, even if START_ON_MAP is set !
                        Intent mapI = new Intent(Activity_Map.this, Activity_Main.class);
                        Tracer.v(mytag, "Call to Main, run it now !");
                        startActivity(mapI);
                    }
                    //open the "ADD map"
                    if (((position == last_map + 1) && (!Tracer.Map_as_main)) || ((position == last_map + 2) && (Tracer.Map_as_main))) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
                    }
                }
            }
        });
        listeMap.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Tracer.i(mytag, " longclic on a map");
                //switch to select maps
                int last_map = list_usable_files.size() - 1;
                if ((position <= last_map) && (position > -1)) {
                    mapView.setCurrentFile(position);
                } else {
                    return false;
                }
                // On long click on a map ask if we want to remove this map we just selected
                // prepare an AlertDialog and display it
                final AlertDialog.Builder alert = new AlertDialog.Builder(Activity_Map.this);
                alert.setTitle(R.string.delete_map_title);
                alert.setMessage(R.string.delete_map__message);
                alert.setPositiveButton(R.string.delete_map__OK, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog_customname, int whichButton) {
                        // Don't forget to remove all widget before on this map.
                        mapView.clear_Widgets();
                        // Then remove the file
                        mapView.removefile();
                        Tracer.i(mytag, " User remove a map");
                        //Restart the activity to save change
                        restartactivity();
                    }
                });
                alert.setNegativeButton(R.string.delete_map__NO, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog_customname, int whichButton) {
                        Tracer.i(mytag, " User cancel remove a map");
                    }
                });
                alert.show();

                return false;
            }
        });

    }


    //Wait result of pickup image
    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE && data != null && data.getData() != null) {
                Log.e(mytag, "onActivityResult");
                Uri _uri = data.getData();
                //User had pick an image.
                Cursor cursor = getContentResolver().query(_uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();
                Log.e(mytag, "cursormove");
                //Copy the select picture to Domodroid directory
                Uri uri = data.getData();
                Tracer.i(mytag, "Uri: " + uri.toString());
                File selectFile;
                if (cursor.getString(0) != null) {
                    Tracer.i(mytag, "Image from normal picker");
                    selectFile = new File(cursor.getString(0));
                } else {
                    Tracer.i(mytag, "Image from new picker with uri that may crash");
                    selectFile = new File(getDriveFileAbsolutePath(this, uri));
                }
                Tracer.e(mytag, "selectfile");
                fileName = selectFile.getName();
                Tracer.e(mytag, "filename");
                //filter for extension if not png or svg say it to user
                String filenameArray[] = fileName.split("\\.");
                Tracer.e(mytag, "split");
                //get file extension
                extension = filenameArray[filenameArray.length - 1];
                //put extension in lower case
                extension = extension.toLowerCase();
                if (extension.equals("png") || extension.equals("svg") || extension.equals("jpeg") || extension.equals("jpg")) {
                    // if jpg convert and save it to domodroid dir
                    if (extension.equals("jpeg") || extension.equals("jpg")) {
                        try {
                            //FileOutputStream out = new FileOutputStream(fileName);
                            fileName = fileName.substring(0, fileName.length() - extension.length() - 1) + ".png";
                            extension = "png";
                            File destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                            int i = 1;
                            while (destFile.exists()) {
                                destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                                fileName = "(" + i + ")" + fileName;
                                i++;
                            }
                            FileOutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                            Bitmap bmp = mapView.decodeFile(selectFile);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); //100-best quality
                            out.close();
                            bmp.recycle();
                            Tracer.i(mytag, "On activity result convert image to png !");
                        } catch (Exception e) {
                            //Tracer.e(mytag, e.toString());
                            e.printStackTrace();
                        }
                        //else just copy svg or png to domodroid dir
                    } else {
                        File destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName);
                        int i = 1;
                        while (destFile.exists()) {
                            destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + "(" + i + ")" + fileName);
                            i++;
                        }
                        CopyFile.copyDirectory(selectFile, destFile);
                    }
                    cursor.close();
                    Toast.makeText(this, R.string.map_add_file_ok, Toast.LENGTH_SHORT).show();
                    Tracer.i(mytag, "On activity result No error adding new file in map !");
                    //ask user if he want to rename the file before copy
                    AlertDialog.Builder rename = new AlertDialog.Builder(this);
                    rename.setTitle(R.string.Rename_file_title);
                    rename.setMessage(R.string.Rename_file_message);
                    // Set an EditText view to get user input
                    final EditText input = new EditText(Activity_Map.this);
                    input.setText(fileName.substring(0, fileName.length() - extension.length() - 1));
                    rename.setView(input);
                    rename.setPositiveButton(R.string.Rename_file_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog_customname, int whichButton) {
                            String renamefileName = input.getText().toString();
                            if (!renamefileName.equals(fileName.substring(0, fileName.length() - extension.length() - 1))) {
                                Tracer.i(mytag, "new fileName: " + renamefileName);
                                destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + renamefileName + "." + extension);
                                int i = 1;
                                while (destFile.exists()) {
                                    destFile = new File(Environment.getExternalStorageDirectory() + "/domodroid/" + "(" + i + ")" + renamefileName + "." + extension);
                                    i++;
                                }
                                new File(Environment.getExternalStorageDirectory() + "/domodroid/" + fileName).renameTo(destFile);
                            }//Restart the activity to save change
                            restartactivity();
                        }
                    });
                    rename.setNegativeButton(R.string.Rename_file_NO, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog_customname, int whichButton) {
                            Tracer.i(mytag, "rename file Canceled.");
                            //Restart the activity to save change
                            restartactivity();
                        }
                    });
                    rename.show();
                    //just need to store the new name with his extension to "fileName"

                } else {
                    Toast.makeText(this, R.string.map_add_file_type_nok, Toast.LENGTH_LONG).show();
                    Tracer.d(mytag, "File type is not supported !");
                    return;
                }
            }
            super.onActivityResult(requestCode, resultCode, data);

        } catch (Exception e) {
            Tracer.e(mytag, "Error adding file in map !");
            Toast.makeText(this, R.string.map_add_file_nok, Toast.LENGTH_LONG).show();
            Tracer.e(mytag, e.toString());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        panel.setOpen(false, false);
        if (Tracer != null) {
            Tracer.v(mytag, "onPause");
            if (Tracer.Map_as_main) {
                widgetUpdate.set_sleeping();    //We act as main screen : if going to pause, freeze cache engine
            }
        }

    }

    public void onResume() {
        super.onResume();
        if (Tracer == null) {
            Tracer = tracerengine.getInstance(params, this);
        }
        Tracer.v(mytag, "Onresume Try to connect on cache engine !");

        if (widgetUpdate == null) {
            startCacheEngine();
        } else {
            widgetUpdate.wakeup();
            build_maps_list();
            if (mapView != null) {
                System.gc();
                mapView.refreshMap();
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Tracer != null)
            Tracer.v(mytag, "Ondestroy Leaving Map_Activity : disconnect from engines");

        if (widgetUpdate != null) {
            widgetUpdate.Disconnect(2);    //That'll purge all connected widgets for MapView
        }

        if (mapView != null)
            mapView.purge();
        mapView = null;

        if (Tracer != null) {
            Tracer.close();        //To eventually flush and close txt log file
            Tracer = null;        //Stop own Tracer engine
        }

        System.gc();
    }

    public void onStop() {
        System.gc();
        super.onStop();
        Tracer.v(mytag, "onStop");
        //onDestroy();
    }

    public void onPanelClosed(Sliding_Drawer panel) {
        if (Tracer != null)
            Tracer.v(mytag, "Onpanelclosepanel request to close");
        menu_green.startAnimation(animation2);
        menu_green.setVisibility(View.GONE);
        panel_widget.removeAllViews();
    }


    public void onPanelOpened(Sliding_Drawer panel) {
        //disable menu if set in option
        if (!params.getBoolean("map_menu_disable", false)) {
            if (Tracer != null)
                Tracer.v(mytag, "onPanelOpened panel request to be displayed");
            menu_green.setVisibility(View.VISIBLE);
            menu_green.startAnimation(animation1);
        }
    }


    public void onClick(View v) {
        if (v.getTag().equals("menu")) {
            //disable menu if set in option

            if (!topPanel.isOpen()) {
                bottomPanel.setOpen(true, true);
                panel_button.setVisibility(View.VISIBLE);
                if (!params.getBoolean("map_menu_disable", false))
                    topPanel.setOpen(true, true);

            } else if (topPanel.isOpen() && !bottomPanel.isOpen()) {
                panel_widget.setVisibility(View.GONE);
                panel_button.setVisibility(View.VISIBLE);
                bottomPanel.setOpen(true, true);
            } else {
                bottomPanel.setOpen(false, true);
                topPanel.setOpen(false, true);
            }


        } else if (v.getTag().equals("add")) {
            //Add a widget
            panel.setOpen(false, true);
            if (list_usable_files.isEmpty()) {
                Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
            } else {
                topPanel.setOpen(false, true);
                //show list of feature available
                dialog_feature.show();
                mapView.setRemoveMode(false);
                mapView.setMoveMode(false);
            }

        } else if (v.getTag().equals("remove")) {
            //case when user want to remove only one widget
            if (list_usable_files.isEmpty()) {
                Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
            } else {
                if (!mapView.isRemoveMode()) {
                    //if remove mode is select for the first time
                    //say Mapview.java to turn on remove mode
                    mapView.setMoveMode(false);
                    mapView.setRemoveMode(true);
                } else {
                    //Remove mode was active, return to normal mode
                    //Turn menu text color back
                    mapView.setRemoveMode(false);
                }
                panel.setOpen(false, true);
                topPanel.setOpen(false, true);

            }

        } else if (v.getTag().equals("move")) {
            //case when user want to move one widget
            // first step remove, second add the removed widget
            if (list_usable_files.isEmpty()) {
                Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
            } else {
                //Show the move dialog box to help user
                Dialog_Move dialog_move = new Dialog_Move(this);
                dialog_move.show();
                if (!mapView.isMoveMode()) {
                    //if remove mode is select for the first time
                    //say Mapview.java to turn on remove mode
                    mapView.setRemoveMode(false);
                    mapView.setMoveMode(true);
                } else {
                    //Remove mode was active, return to normal mode
                    //Turn menu text color back
                    mapView.setRemoveMode(false);
                }
                panel.setOpen(false, true);
                topPanel.setOpen(false, true);

            }

        } else if (v.getTag().equals("remove_all")) {
            //case when user select remove all from menu
            if (list_usable_files.isEmpty()) {
                Toast.makeText(this, getText(R.string.map_nothing), Toast.LENGTH_LONG).show();
            } else {
                panel.setOpen(false, true);
                topPanel.setOpen(false, true);
                Tracer.i(mytag, "request to clear widgets");
                mapView.clear_Widgets();
                mapView.setRemoveMode(false);
            }
        } else if (v.getTag().equals("help")) {
            Dialog_Help dialog_help = new Dialog_Help(this);
            dialog_help.show();
            prefEditor = params.edit();
            prefEditor.putBoolean("SPLASH", true);
            prefEditor.commit();
        }
    }

    @Override
    //Physical button keycode 82 is menu button
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //disable menu if set in option
        Tracer.v(mytag, "onKeyDown keyCode = " + keyCode);
        if (keyCode == 82 && !topPanel.isOpen()) {
            bottomPanel.setOpen(true, true);
            panel_button.setVisibility(View.VISIBLE);
            if (!params.getBoolean("map_menu_disable", false)) {
                topPanel.setOpen(true, true);
            }
            return false;

        } else if (keyCode == 82 && topPanel.isOpen() && !bottomPanel.isOpen()) {
            panel_widget.setVisibility(View.GONE);
            panel_button.setVisibility(View.VISIBLE);
            bottomPanel.setOpen(true, true);
            return false;

        } else if ((keyCode == 82 || keyCode == 4) && topPanel.isOpen()) {
            bottomPanel.setOpen(false, true);
            topPanel.setOpen(false, true);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static void createDirIfNotExists() {
        File file = new File(Environment.getExternalStorageDirectory(), "/domodroid");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    //this is called when the screen rotates.
    // (onCreate is no longer called when screen rotates due to manifest, see: android:configChanges)
    {
        super.onConfigurationChanged(newConfig);
        System.gc();
        mapView.initMap();

    }

    private void restartactivity() {
        Intent intent = getIntent();
        System.gc();
        finish();
        startActivity(intent);
    }

    private static String getDriveFileAbsolutePath(Activity context, Uri uri) {
        if (uri == null) return null;
        ContentResolver resolver = context.getContentResolver();
        String filename = "";
        final String[] projection = {
                MediaStore.MediaColumns.DISPLAY_NAME
        };
        ContentResolver cr = context.getApplicationContext().getContentResolver();
        Cursor metaCursor = cr.query(uri, projection, null, null, null);
        if (metaCursor != null) {
            try {
                if (metaCursor.moveToFirst()) {
                    filename = metaCursor.getString(0);
                    Log.e("DriveFileAbsolutePath", "filename=" + filename);
                }
            } finally {
                metaCursor.close();
            }
        }
        FileInputStream input = null;
        FileOutputStream output = null;
        String outputFilePath = new File(context.getCacheDir(), filename).getAbsolutePath();
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(outputFilePath);
            int read = 0;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            System.gc();
            return new File(outputFilePath).getAbsolutePath();
        } catch (IOException ignored) {
            System.gc();// nothing we can do
        } finally {
            try {
                System.gc();
                input.close();
                output.close();
            } catch (IOException e) {
                System.gc();
                e.toString();
            }

        }
        return "";
    }
}
