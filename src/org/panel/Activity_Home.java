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
package org.panel;

import java.util.Vector;

import org.database.WidgetUpdate;
import org.json.JSONException;
import org.panel.Sliding_Drawer.OnPanelListener;
import org.widgets.Graphical_Color;
import org.widgets.Graphical_Feature;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Activity_Home extends Activity implements OnPanelListener,OnClickListener,OnSeekBarChangeListener{

	@SuppressWarnings("unused")
	private Sliding_Drawer topPanel;
	private Sliding_Drawer panel;
	protected PowerManager.WakeLock mWakeLock;
	private SharedPreferences params;
	private SharedPreferences.Editor prefEditor;
	private Animation animation1;
	private Animation animation2;
	private TextView menu_white;
	private TextView menu_green;
	private AlertDialog.Builder notSyncAlert;
	private Widgets_Manager wAgent;
	private Dialog_Synchronize dialog_sync;
	private WidgetUpdate widgetUpdate;
	private Handler sbanim;
	private Handler widgetHandler;

	private Button sync;
	private EditText localIP;
	private CheckBox checkbox3;
	private CheckBox checkbox4;
	private ImageView appname;
	private String format_urlAccess;
	public static String urlAccess;
	private TextView mProgressText1;
	private TextView mProgressText2;
	private TextView mProgressText3;
	private SeekBar mSeekBar1;
	private SeekBar mSeekBar2;
	private SeekBar mSeekBar3;
	private int dayOffset = 1;
	private int secondeOffset = 2;
	private int sizeOffset = 300;
	private ViewGroup parent;
	private LinearLayout ll_area;
	private LinearLayout ll_room;
	private LinearLayout ll_activ;
	private Vector<String[]> history;
	private int historyPosition;
	private LinearLayout house_map;
	private String tempUrl;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//sharedPref
		params = getSharedPreferences("PREFS",MODE_PRIVATE);
		prefEditor=params.edit();

		//option
		localIP = (EditText)findViewById(R.id.localIP);	
		checkbox3 = (CheckBox)findViewById(R.id.checkbox3);
		checkbox4 = (CheckBox)findViewById(R.id.checkbox4);
		appname = (ImageView)findViewById(R.id.app_name);
		mProgressText1 = (TextView)findViewById(R.id.progress1);
		mProgressText2 = (TextView)findViewById(R.id.progress2);
		mProgressText3 = (TextView)findViewById(R.id.progress3);
		mSeekBar1=(SeekBar)findViewById(R.id.SeekBar1);
		mSeekBar2=(SeekBar)findViewById(R.id.SeekBar2);
		mSeekBar3=(SeekBar)findViewById(R.id.SeekBar3);
		sync=(Button)findViewById(R.id.sync);
		sync.setOnClickListener(this);
		sync.setTag("sync");
		mSeekBar1.setOnSeekBarChangeListener(this);
		mSeekBar2.setOnSeekBarChangeListener(this);
		mSeekBar3.setOnSeekBarChangeListener(this);

		LoadSelections();

		//update thread
		sbanim = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what==0){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name2));
				}else if(msg.what==1){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name3));
				}else if(msg.what==2){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name1));
				}else if(msg.what==3){
					appname.setImageDrawable(getResources().getDrawable(R.drawable.app_name4));
				}
			}	
		};

		widgetUpdate = new WidgetUpdate(this,sbanim,params);

		//power management
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "");
		this.mWakeLock.acquire();

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

		//Parent view
		parent = (ViewGroup) findViewById(R.id.home_container);

		//sliding drawer
		topPanel = panel = (Sliding_Drawer) findViewById(R.id.topPanel);
		panel.setOnPanelListener(this);
		//panel.setPadding(0, 45, 0, 0);


		house_map = new LinearLayout(this);
		house_map.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		house_map.setOrientation(LinearLayout.HORIZONTAL);
		house_map.setPadding(5, 5, 5, 5);

		Graphical_Feature house = new Graphical_Feature(getApplicationContext(),0,"House","","house",0);
		house.setPadding(0, 0, 5, 0);
		Graphical_Feature map = new Graphical_Feature(getApplicationContext(),0,"Map","","map",0);
		map.setPadding(5, 0, 0, 0);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);

		house.setLayoutParams(param);
		house.setOnClickListener(this);
		house.setTag("house");

		map.setLayoutParams(param);
		map.setOnClickListener(this);
		map.setTag("map");

		house_map.addView(house);
		house_map.addView(map);

		//alertDialog not sync
		notSyncAlert = new AlertDialog.Builder(this);
		notSyncAlert.setMessage("Domodroid is not synchronized").setTitle("Warning!");
		notSyncAlert.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();						
			}
		});


		//splash
		if(!params.getBoolean("SPLASH", false)){
			Dialog_Splash dialog_splash = new Dialog_Splash(this);
			dialog_splash.show();
			prefEditor.clear();
			prefEditor.putBoolean("SPLASH", true);
			prefEditor.commit();
		}

		//load widgets
		history = new Vector<String[]>();


		widgetHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					historyPosition++;
					loadWigets(msg.getData().getInt("id"), msg.getData().getString("type"));
					Log.e("add history", msg.getData().getInt("id")+" "+msg.getData().getString("type"));
					history.add(historyPosition,new String [] {msg.getData().getInt("id")+"",msg.getData().getString("type")});
				} catch (Exception e) {
					//Log.e("handler error", "load widget");
					e.printStackTrace();
				}
			}	
		};

		wAgent=new Widgets_Manager(widgetHandler);
		loadWigets(0,"root");
		historyPosition=0;
		history.add(historyPosition,new String [] {"0","root"});
	}


	public void loadWigets(int id, String type){
		parent.removeAllViews();
		ll_area = new LinearLayout(this);
		ll_area.setOrientation(LinearLayout.VERTICAL);
		ll_room = new LinearLayout(this);
		ll_room.setOrientation(LinearLayout.VERTICAL);
		ll_activ = new LinearLayout(this);
		ll_activ.setOrientation(LinearLayout.VERTICAL);

		try {
			if(type.equals("root")){
				parent.addView(house_map);
				ll_area = wAgent.loadAreaWidgets(this, ll_area, params);
			}
			if(type.equals("root") || type.equals("area"))ll_room = wAgent.loadRoomWidgets(this, id, ll_room, params);
			if(type.equals("area") || type.equals("room") || type.equals("house"))ll_activ = wAgent.loadActivWidgets(this, id, type, ll_activ,params);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		parent.addView(ll_area);
		parent.addView(ll_room);
		parent.addView(ll_activ);
	}

	/**
	 * save selections preference
	 */
	private void SaveSelections() {
		try{
			SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
			SharedPreferences.Editor prefEditor=params.edit();
			prefEditor.putString("IP1",localIP.getText().toString());
			prefEditor.putBoolean("DRAG", checkbox3.isChecked());
			prefEditor.putBoolean("ZOOM", checkbox4.isChecked());
			prefEditor.putInt("UPDATE_TIMER", mSeekBar1.getProgress()+secondeOffset);
			prefEditor.putInt("GRAPH", mSeekBar2.getProgress()+dayOffset);
			prefEditor.putInt("SIZE", mSeekBar3.getProgress()+sizeOffset);

			urlAccess = localIP.getText().toString();

			if(urlAccess.lastIndexOf("/")==localIP.getText().toString().length()-1) format_urlAccess = urlAccess;
			else format_urlAccess = urlAccess.concat("/");

			prefEditor.putString("URL",format_urlAccess);
			prefEditor.commit();

			if(!tempUrl.equals(localIP.getText().toString())){
				dialog_sync = new Dialog_Synchronize(this);
				dialog_sync.setParams(params);
				dialog_sync.show();
				dialog_sync.startSync();
			}
		}catch(Exception e){}
	}

	private void LoadSelections() {
		SharedPreferences params = getSharedPreferences("PREFS",MODE_PRIVATE);
		localIP.setText(params.getString("IP1",null));
		tempUrl=params.getString("IP1",null);
		checkbox3.setChecked(params.getBoolean("DRAG",false));
		checkbox4.setChecked(params.getBoolean("ZOOM",false));
		mSeekBar1.setProgress(params.getInt("UPDATE_TIMER", 300)-secondeOffset);
		mSeekBar2.setProgress(params.getInt("GRAPH", 3)-dayOffset);
		mSeekBar3.setProgress(params.getInt("SIZE", 800)-sizeOffset);
	}



	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		if(seekBar.getId()==R.id.SeekBar1) {
			mProgressText1.setText((progress+secondeOffset)+" Secondes");
		}
		else if(seekBar.getId()==R.id.SeekBar2) {
			mProgressText2.setText((progress+dayOffset)+" Days");
		}else{
			mProgressText3.setText((progress+sizeOffset)+" px");
		}
	}


	public void onStartTrackingTouch(SeekBar seekBar) {
	}


	public void onStopTrackingTouch(SeekBar seekBar) {
	}


	public void onPanelClosed(Sliding_Drawer panel) {
		menu_green.startAnimation(animation2);
		menu_green.setVisibility(View.GONE);
		SaveSelections();
		widgetUpdate = new WidgetUpdate(this,sbanim,params);

	}
	public void onPanelOpened(Sliding_Drawer panel) {
		menu_green.setVisibility(View.VISIBLE);
		menu_green.startAnimation(animation1);
		widgetUpdate.stopThread();
	}

	public void onClick(View v) {
		if(v.getTag().equals("sync")) {
			panel.setOpen(false, false);
			dialog_sync = new Dialog_Synchronize(this);
			dialog_sync.setParams(params);
			dialog_sync.show();
			dialog_sync.startSync();
		}	
		else if(v.getTag().equals("about")) {
			Intent helpI = new Intent(Activity_Home.this,Activity_About.class);
			startActivity(helpI);
		}
		if(v.getTag().equals("house")) {

			if(params.getBoolean("SYNC", false)==true){
				loadWigets(0, "house");
				historyPosition++;
				history.add(historyPosition,new String [] {"0","house"});
			}else{
				notSyncAlert.show();
			}
		}
		else if(v.getTag().equals("map")) {
			if(params.getBoolean("SYNC", false)==true){
				Intent mapI = new Intent(Activity_Home.this,Activity_Map.class);
				startActivity(mapI);
			}else{
				notSyncAlert.show();
			}
		}
		else if(v.getTag().equals("menu")) {
			if(!panel.isOpen()){
				panel.setOpen(true, true);
			}else{
				panel.setOpen(false, true);
			}
		}
	}



	@Override
	public void onPause(){
		super.onPause();
		panel.setOpen(false, false);
		widgetUpdate.stopThread();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.mWakeLock.release();
		widgetUpdate.stopThread();
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==82 && !panel.isOpen()){
			panel.setOpen(true, true);
			return false;
		}else if((keyCode==82 || keyCode == 4) && panel.isOpen()){
			panel.setOpen(false, true);
			return false;
		}else if(keyCode == 4 && !panel.isOpen() && historyPosition > 0){
			historyPosition--;
			loadWigets(Integer.parseInt(history.elementAt(historyPosition)[0]),history.elementAt(historyPosition)[1]);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class SBAnim extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			new Thread(new Runnable() {
				public synchronized void run() {
					try {
						appname.setBackgroundResource(R.drawable.app_name2);
						this.wait(100);
						appname.setBackgroundResource(R.drawable.app_name3);
						this.wait(100);
						appname.setBackgroundResource(R.drawable.app_name1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			return null;
		}
	}

}

