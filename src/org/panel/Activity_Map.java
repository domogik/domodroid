package org.panel;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.database.DomodroidDB;
import org.map.Dialog_Help;
import org.map.MapView;
import org.panel.Sliding_Drawer.OnPanelListener;
import org.widgets.Entity_Feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Activity_Map extends Activity implements OnPanelListener,OnClickListener{
	private Sliding_Drawer panel;
	private Sliding_Drawer topPanel;
	private Sliding_Drawer bottomPanel;
	private Button add;
	private Button help;
	private Button remove;
	private Dialog dialog_feature;

	private Entity_Feature[] listFeature;
	private HashMap<String,String> map;

	private Vector<String> list_usable_files;
	private MapView mapView;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private ViewGroup panel_widget;
	private ViewGroup panel_button;

	private ListView listeMap;
	private ArrayList<HashMap<String,String>> listItem;
	private Animation animation1;
	private Animation animation2;
	private TextView menu_white;
	private TextView menu_green;

	private DomodroidDB domodb = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		params = getSharedPreferences("PREFS",MODE_PRIVATE);
		prefEditor=params.edit();

		mapView = new MapView(this);
		mapView.setParams(params);
		mapView.setUpdate(params.getInt("UPDATE_TIMER",300));
		setContentView(R.layout.activity_map);
		ViewGroup parent = (ViewGroup) findViewById(R.id.map_container);

		//titlebar
		final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));

		//menu button
		menu_white = (TextView) findViewById(R.id.menu_button1);
		menu_white.setOnClickListener(this);
		menu_white.setTag("menu");
		menu_green = (TextView) findViewById(R.id.menu_button2);
		menu_green.setVisibility(View.GONE);

		animation1 = new AlphaAnimation(0.0f, 1.0f);
		animation1.setDuration(500);
		animation2 = new AlphaAnimation(1.0f, 0.0f);
		animation2.setDuration(500);

		//read files from SDCARD + create directory
		String files[] = null;
		createDirIfNotExists();
		File f=new File(Environment.getExternalStorageDirectory()+"/domodroid/"); 
		if(f.isDirectory()){ 
			files= f.list(); 
		}


		//list Map
		listeMap = (ListView)findViewById(R.id.listeMap);
		listItem=new ArrayList<HashMap<String,String>>();
		list_usable_files = new Vector<String>();
		for (int i=0;i<files.length;i++) {
			if(!files[i].startsWith(".")){
				list_usable_files.add(files[i]);
				map=new HashMap<String,String>();
				map.put("name",files[i].substring(0, files[i].lastIndexOf('.')));
				map.put("position",String.valueOf(i));
				listItem.add(map);
			}
		}
		mapView.setFiles(list_usable_files);


		SimpleAdapter adapter_map=new SimpleAdapter(getBaseContext(),listItem,
				R.layout.item_map,new String[] {"name"},new int[] {R.id.name});
		listeMap.setAdapter(adapter_map);
		listeMap.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mapView.setCurrentFile(position);
				mapView.initMap();
			}
		});


		//sliding drawer
		topPanel = panel = (Sliding_Drawer) findViewById(R.id.map_slidingdrawer);
		panel.setOnPanelListener(this);
		panel.setOnTouchListener(new OnTouchListener() {	 

			public boolean onTouch(View v, MotionEvent event) {return true;}});
		bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
		panel.setOnPanelListener(this);
		mapView.setTopDrawer(topPanel);
		mapView.setBottomDrawer(bottomPanel);

		panel_widget = (ViewGroup) findViewById(R.id.panelWidget);
		panel_button = (ViewGroup) findViewById(R.id.panelButton);

		mapView.setPanel_widget(panel_widget);
		mapView.setPanel_button(panel_button);

		//add remove button
		add = new Button(this);
		add.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		add.setPadding(10, 13, 10, 13);
		add.setText(R.string.map_button1);
		add.setTextColor(Color.parseColor("#cfD1D1"));
		add.setTextSize(15);
		add.setTag("add");
		add.setBackgroundColor(Color.parseColor("#00000000"));
		add.setOnClickListener(this);
		help = new Button(this);
		help.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		help.setPadding(10, 13, 10, 13);
		help.setText(R.string.map_button3);
		help.setTextColor(Color.parseColor("#cfD1D1"));
		help.setTextSize(15);
		help.setTag("help");
		help.setBackgroundColor(Color.parseColor("#00000000"));
		help.setOnClickListener(this);
		remove = new Button(this);
		remove.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		remove.setPadding(10, 13, 10, 13);
		remove.setText(R.string.map_button2);
		remove.setTextColor(Color.parseColor("#cfD1D1"));
		remove.setTextSize(15);
		remove.setTag("remove");
		remove.setBackgroundColor(Color.parseColor("#00000000"));
		remove.setOnClickListener(this);
		panel_button.addView(add);
		panel_button.addView(help);
		panel_button.addView(remove);


		bottomPanel = panel = (Sliding_Drawer) findViewById(R.id.bottomPanel);
		panel.setOnPanelListener(this);

		dialog_feature = new Dialog(this);


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a Widget");

		//get feature list
		domodb = new DomodroidDB(this);
		domodb.owner="Activity_Map";
		listFeature = domodb.requestFeatures();


		//listview feature
		ListView listview_feature = new ListView(this);
		ArrayList<HashMap<String,String>> listItem1=new ArrayList<HashMap<String,String>>();
		for (Entity_Feature feature : listFeature) {
			map=new HashMap<String,String>();
			map.put("name",feature.getName());
			map.put("type",feature.getValue_type());
			map.put("state_key", feature.getState_key());
			listItem1.add(map);

		}
		SimpleAdapter adapter_feature=new SimpleAdapter(getBaseContext(),listItem1,
				R.layout.item_feature,new String[] {"name","type","state_key"},new int[] {R.id.name,R.id.description,R.id.state_key});
		listview_feature.setAdapter(adapter_feature);
		listview_feature.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mapView.temp_id = listFeature[position].getId();
				mapView.setAddMode(true);
				dialog_feature.dismiss();
			}
		});

		builder.setView(listview_feature);
		dialog_feature = builder.create();


		if(!list_usable_files.isEmpty()){
			mapView.initMap();
			mapView.updateTimer();
			parent.addView(mapView);
		}
		else{
			Dialog_Help dialog_help = new Dialog_Help(this);
			dialog_help.show();
		}

		try {
			mapView.drawWidgets();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	@Override
	public void onPause(){
		super.onPause();
		panel.setOpen(false, false);
		Log.e("Activity_Map", "onPause");
		mapView.stopThread();
		mapView=null;
		domodb=null;	//Doume : to stop background dialog with REST
		try {
			finalize();
		} catch (Throwable e) {
			
		}
	}

	public void onPanelClosed(Sliding_Drawer panel) {
		menu_green.startAnimation(animation2);
		menu_green.setVisibility(View.GONE);
		panel_widget.removeAllViews();
		
	}


	public void onPanelOpened(Sliding_Drawer panel) {
		menu_green.setVisibility(View.VISIBLE);
		menu_green.startAnimation(animation1);
	}



	public void onClick(View v) {
		if(v.getTag().equals("menu")){
			if(!topPanel.isOpen()){
				bottomPanel.setOpen(true, true);
				panel_button.setVisibility(View.VISIBLE);
				topPanel.setOpen(true, true);
			}else if(topPanel.isOpen() && !bottomPanel.isOpen()){
				panel_widget.setVisibility(View.GONE);
				panel_button.setVisibility(View.VISIBLE);
				bottomPanel.setOpen(true, true);
			}else{
				bottomPanel.setOpen(false, true);
				topPanel.setOpen(false, true);
			}			


		}else if(v.getTag().equals("add")){
			panel.setOpen(false, true);
			if(list_usable_files.isEmpty()){
				Toast.makeText(this, "No Map Loaded, press \"Help\"", Toast.LENGTH_LONG).show();
			}else{
				dialog_feature.show();
				remove.setTextColor(Color.parseColor("#cfD1D1"));
				mapView.setRemoveMode(false);
			}
		}else if(v.getTag().equals("remove")){
			if(list_usable_files.isEmpty()){
				Toast.makeText(this, "No Map Loaded, press \"Help\"", Toast.LENGTH_LONG).show();
			}else{
				if(mapView.isRemoveMode()==false){
					remove.setTextColor(Color.GREEN);
					mapView.setRemoveMode(true);
				}else{
					remove.setTextColor(Color.parseColor("#cfD1D1"));
					mapView.setRemoveMode(false);
				}
			}
		}else if(v.getTag().equals("help")){
			Dialog_Help dialog_help = new Dialog_Help(this);
			dialog_help.show();
			prefEditor.putBoolean("SPLASH", true);
			prefEditor.commit();
		}	
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==82 && !topPanel.isOpen()){
			bottomPanel.setOpen(true, true);
			panel_button.setVisibility(View.VISIBLE);
			topPanel.setOpen(true, true);
			return false;

		}else if(keyCode==82 && topPanel.isOpen() && !bottomPanel.isOpen()){
			panel_widget.setVisibility(View.GONE);
			panel_button.setVisibility(View.VISIBLE);
			bottomPanel.setOpen(true, true);
			return false;

		}else if((keyCode==82 || keyCode == 4) && topPanel.isOpen()){
			bottomPanel.setOpen(false, true);
			topPanel.setOpen(false, true);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public String getFileAsString(File file){ 
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);
			while (dis.available() != 0) {
				sb.append( dis.readLine() +"\n");
			}
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}



	public static boolean createDirIfNotExists() {
		boolean ret = true;
		File file = new File(Environment.getExternalStorageDirectory(), "/domodroid");
		if (!file.exists()) {
			if (!file.mkdirs()) {
				ret = false;
			}
		}
		return ret;
	}
}
