package activities;

import java.util.ArrayList;
import org.domogik.domodroid13.R;
import rinor.Rest_com;
import database.DomodroidDB;
import org.json.JSONArray;
import org.json.JSONObject;
import widgets.Entity_Feature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class Dialog_Synchronize extends Dialog implements OnClickListener {
	private Button cancelButton;
	private TextView message;
	private String urlAccess;
	private SharedPreferences.Editor prefEditor;
	private static Handler handler = null;
	private SharedPreferences params;
	private LoadConfig sync;
	public Boolean need_refresh = false;
	private Activity context;
	public Boolean reload = false;
	private DomodroidDB db = null;
	private tracerengine Tracer = null;
	private String login;
	private String password;
	private String mytag="Dialog_Synchronize";
	
	public Dialog_Synchronize(tracerengine Trac, Activity context, SharedPreferences params) {
		super(context);
		this.context = context;
		this.Tracer = Trac;
		this.params = params;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_synchronize);
		message = (TextView) findViewById(R.id.message);
		cancelButton = (Button) findViewById(R.id.CancelButton);
		cancelButton.setOnClickListener(this);
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		handler = new  Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					String loc_Value = msg.getData().getString("message");
					if(loc_Value.equals("sync_done")) {
						sync.cancel(true);
						dismiss();
						
						return;
					}
				} catch (Exception e) {}
						
				message.setText("Connection error");
				
			}
		};		
	}

	
	public void onClick(View v) {
		if (v == cancelButton)
			need_refresh = false;
			sync.cancel(true);
			dismiss();
	}
	
	public void setParams(SharedPreferences params) {
		this.params = params;
	}
	
	public void startSync(){
		sync = new LoadConfig();
		sync.execute();
	}
	
	public class LoadConfig extends AsyncTask<Void, Integer, Void>{
		private boolean sync=false;

		public LoadConfig() {
			super();
			prefEditor=params.edit();
			urlAccess = params.getString("URL","1.1.1.1");
			if(db == null)
				db = new DomodroidDB(Tracer, context);
		}

		@Override
		protected void onPreExecute() {
			message.setText("Loading Configuration... 0%");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if(sync){
				Intent reload = new Intent(context,Activity_Main.class);
				context.startActivity(reload);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			message.setText("Loading Configuration... "+values[0]+"%");
			super.onProgressUpdate(values);
		}

		@Override
		protected Void doInBackground(Void... params) {
			//Requests
			
			try{
				
				// if API rinor >0.5 génération auto sinon classic
				JSONObject json_rinor = null;
				try {
					json_rinor = Rest_com.connect(urlAccess,login,password);
				} catch (Exception e) {
					json_rinor = null;
				}
				if(json_rinor == null) {
					//Cannot connect to server...
					handler.sendEmptyMessage(0);
					return null;
				}
				String Rinor_Api_ver = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("REST_API_version");
				Float Rinor_Api_Version =Float.valueOf(Rinor_Api_ver);
				String domogik_Version = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("Domogik_version");
				
				
				JSONObject json_AreaList = null;
				JSONObject json_RoomList = null;
				JSONObject json_FeatureList = null;
				JSONObject json_FeatureAssociationList = null;
				JSONObject json_IconList = null;
				Tracer.d(mytag, "urlAccess = <"+urlAccess+">");
				
				if(Rinor_Api_Version <=0.5){
					json_AreaList = Rest_com.connect(urlAccess+"base/area/list/",login,password);
					if(json_AreaList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					Tracer.d(mytag, "AreaList = <"+json_AreaList.toString()+">");
					
					publishProgress(20);
					json_RoomList = Rest_com.connect(urlAccess+"base/room/list/",login,password);
					if(json_RoomList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					//Tracer.d(mytag, "RoomList = <"+json_RoomList.toString()+">");
					
					publishProgress(40);
					json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list",login,password);
					if(json_FeatureList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					
					publishProgress(60);
					json_FeatureAssociationList = Rest_com.connect(urlAccess+"base/feature_association/list/",login,password);
					if(json_FeatureAssociationList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(80);
					json_IconList = Rest_com.connect(urlAccess+"base/ui_config/list/",login,password);
					if(json_IconList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(100);
					
					//Save result in sharedpref
					prefEditor.putString("AREA_LIST",json_AreaList.toString());
					prefEditor.putString("ROOM_LIST",json_RoomList.toString());
					prefEditor.putString("FEATURE_LIST",json_FeatureList.toString());
					prefEditor.putString("ASSOCIATION_LIST",json_FeatureAssociationList.toString());
					prefEditor.putString("ICON_LIST",json_IconList.toString());
					prefEditor.putBoolean("SYNC", true);
					prefEditor.putBoolean("BY_USAGE", false);
					prefEditor.putString("DOMOGIK-VERSION", domogik_Version);
					
					
				}else{
					// Fonction special Basilic domogik 0.3
					json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list",login,password);
					if(json_FeatureList == null) {
						// Cannot connect to Rinor server.....
						handler.sendEmptyMessage(0);
						return null;
					}
					//JSONObject json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list");
					publishProgress(25);
					
					json_RoomList = new JSONObject();
					
					json_FeatureAssociationList = new JSONObject();
					
					json_AreaList = new JSONObject();
					json_AreaList.put("status","OK");
					json_AreaList.put("code",0);
					json_AreaList.put("description","None");
					JSONArray list = new JSONArray();
					JSONObject map_area = new JSONObject();
					map_area.put("description", "");
					map_area.put("id", "1");
					map_area.put("name", "Usage");
					list.put(map_area);
					json_AreaList.put("area",list);
					publishProgress(45);
					String usage = new String();
					json_RoomList.put("status","OK");
					json_RoomList.put("code",0);
					json_RoomList.put("description","None");
					JSONArray rooms = new JSONArray();
					JSONObject area = new JSONObject();
					area.put("description","");
					area.put("id","1");
					area.put("name","Usage");
					ArrayList<String> list_usage = new ArrayList<String>();
					int j=2;
					json_FeatureAssociationList.put("status","OK");
					json_FeatureAssociationList.put("code","0");
					json_FeatureAssociationList.put("description","");
	                JSONArray ListFeature = new JSONArray();
	                publishProgress(55);
					
	                int list_size = 0;
	                if(json_FeatureList != null)
	                	list_size = json_FeatureList.getJSONArray("feature").length();
	                Tracer.d(mytag,"Features list size = "+list_size);
					//correct a bug #2020 if device is empty.
					for(int i = 0; i < list_size; i++) {
						try {
						
							usage = json_FeatureList.getJSONArray("feature").getJSONObject(i)
									.getJSONObject("device").getString("device_usage_id");
						} catch (Exception e) {
							usage=null;
							// Cannot parse JSON Array or JSONObject
							 Tracer.d(mytag,"Exception processing Features list ("+i+")");
						}
						 Tracer.d(mytag,"Features list processing usage = <"+usage+">");
							
						// Create a pseudo 'room' for each usage returned by Rinor
						if (usage != null) {
							if(! list_usage.contains(usage)){
								if(json_FeatureList.getJSONArray("feature").length() > 0) {
									publishProgress(100*i/json_FeatureList.getJSONArray("feature").length());
									JSONObject room = new JSONObject();
									room.put("area_id","1");
									room.put("description","");
	
									room.put("area",area);
									room.put("id",j);
									j++;
									room.put("name",json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
									rooms.put(room);
									list_usage.add(json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
								}
							}
							// And its associated widget
							JSONObject Widget = new JSONObject();
							Widget.put("place_type","room");
							Widget.put("place_id",list_usage.indexOf( 
									json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"))+2); //id_rooms);
							Widget.put("device_feature_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							Widget.put("id",50+i);
							JSONObject device_feature = new JSONObject();
							device_feature.put("device_feature_model_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_feature_model_id"));
							device_feature.put("id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							device_feature.put("device_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_id"));
							Widget.put("device_feature", device_feature);
							ListFeature.put(Widget);
						}
						
					} // for loop on feature list...
					//Prepare list of rooms, and list of usable features
					json_RoomList.put("room", rooms);
					json_FeatureAssociationList.put("feature_association",ListFeature);
					
					//Save result in sharedpref
					prefEditor.putString("AREA_LIST",json_AreaList.toString());
					prefEditor.putString("ROOM_LIST",json_RoomList.toString());
					prefEditor.putString("FEATURE_LIST",json_FeatureList.toString());
					prefEditor.putString("ASSOCIATION_LIST",json_FeatureAssociationList.toString());
					//prefEditor.putString("ICON_LIST",json_IconList.toString());
					prefEditor.putBoolean("SYNC", true);
					prefEditor.putBoolean("BY_USAGE", true);
					prefEditor.putString("DOMOGIK-VERSION", domogik_Version);
				}
				// Common sequence for both versions sync
				
				// Insert results into local database
				
				db.updateDb();		//Erase all tables contents EXCEPT maps coordinates !
				Tracer.v(mytag,"Updating database tables with new House configuration");
				db.insertArea(json_AreaList);
				db.insertRoom(json_RoomList);
				db.insertFeature(json_FeatureList);
				db.insertFeatureAssociation(json_FeatureAssociationList);
				if(Rinor_Api_Version <=0.5){
					db.insertIcon(json_IconList);
				}
				
				//Tracer.v(mytag, "AreaList = <"+json_AreaList+">");
				//Tracer.v(mytag, "RoomList = <"+json_RoomList+">");
				//Tracer.v(mytag, "FeatureList = <"+json_FeatureList+">");
				//Tracer.v(mytag, "FeatureAssociationList = <"+json_FeatureAssociationList+">");
				
				//TODO change UrlAccess to make cache more light.
				Entity_Feature[] listFeature = db.requestFeatures();
				String urlUpdate = urlAccess+"stats/multi/";
				Tracer.v(mytag,"prepare UPDATE_URL items="+listFeature.length);
				for (Entity_Feature feature : listFeature) {
					urlUpdate = urlUpdate.concat(feature.getDevId()+"/"+feature.getState_key()+"/");
				}
				prefEditor.putString("UPDATE_URL", urlUpdate);
				need_refresh = true;	// To notify main activity that screen must be refreshed
				prefEditor.commit();
				/*
				db.closeDb();
				db = null;
				*/
				publishProgress(100);
				
				Tracer.v(mytag,"UPDATE_URL = "+urlUpdate);
				
				Bundle b = new Bundle();
				//Notify sync complete to parent Dialog
				b.putString("message", "sync_done");
			    Message msg = new Message();
			    msg.setData(b);
			    handler.sendMessage(msg);
				return null;
				

			}catch(Exception e){
				handler.sendEmptyMessage(0);
				e.printStackTrace();
			}			
			return null;
		}

	}
}

