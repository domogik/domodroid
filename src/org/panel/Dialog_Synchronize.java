package org.panel;

import java.util.ArrayList;

import org.connect.Rest_com;
import org.database.DomodroidDB;
import org.json.JSONArray;
import org.json.JSONObject;
import org.widgets.Entity_Feature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
	private Handler handler;
	private SharedPreferences params;
	private LoadConfig sync;
	private Activity context;
	

	public Dialog_Synchronize(Activity context) {
		super(context);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_synchronize);
		message = (TextView) findViewById(R.id.message);
		cancelButton = (Button) findViewById(R.id.CancelButton);
		cancelButton.setOnClickListener(this);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				message.setText("Connection error");
			}
		};		
	}

	
	public void onClick(View v) {
		if (v == cancelButton)
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
			urlAccess = urlAccess.replaceAll("[\r\n]+", "");
		}

		@Override
		protected void onPreExecute() {
			message.setText("Loading Configuration... 0%");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if(sync){
				Intent reload = new Intent(context,Activity_Home.class);
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
				JSONObject json_rinor = Rest_com.connect(urlAccess);
				String Rinor_Api_ver = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("REST_API_version");
				Float Rinor_Api_Version =Float.valueOf(Rinor_Api_ver);
				DomodroidDB db = new DomodroidDB(context);
				if(Rinor_Api_Version <=0.5){
					JSONObject json_AreaLnvist = Rest_com.connect(urlAccess+"base/area/list/");
					publishProgress(20);
					JSONObject json_RoomList = Rest_com.connect(urlAccess+"base/room/list/");
					publishProgress(40);
					JSONObject json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list");
					publishProgress(60);
					JSONObject json_FeatureAssociationList = Rest_com.connect(urlAccess+"base/feature_association/list/");
					publishProgress(80);
					JSONObject json_IconList = Rest_com.connect(urlAccess+"base/ui_config/list/");

					//Save result in sharedpref
					prefEditor.putString("AREA_LIST",json_AreaList.toString());
					prefEditor.putString("ROOM_LIST",json_RoomList.toString());
					prefEditor.putString("FEATURE_LIST",json_FeatureList.toString());
					prefEditor.putString("ASSOCIATION_LIST",json_FeatureAssociationList.toString());
					prefEditor.putString("ICON_LIST",json_IconList.toString());
					prefEditor.putBoolean("SYNC", true);
					
					//insert in DB
					db.updateDb();
					db.insertArea(json_AreaList);
					db.insertRoom(json_RoomList);
					db.insertFeature(json_FeatureList);
					db.insertFeatureAssociation(json_FeatureAssociationList);
					db.insertIcon(json_IconList);
				}else{
					// Fonction special Basilic domogik 0.3
					JSONObject json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list");
					
					//JSONObject json_FeatureList = Rest_com.connect(urlAccess+"base/feature/list");
					publishProgress(20);
					
					JSONObject json_RoomList = new JSONObject();
					JSONObject json_FeatureAssociationList = new JSONObject();
					
					JSONObject json_AreaList = new JSONObject();
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
					publishProgress(40);
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
					for(int i = 0; i < json_FeatureList.getJSONArray("feature").length(); i++) {
						usage = json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id");
						JSONObject Widget = new JSONObject();
						
						if (list_usage.contains(usage)== false){
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
							Widget.put("place_type","room");
							Widget.put("place_id",list_usage.indexOf( json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"))+2); //id_rooms);
							Widget.put("device_feature_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							Widget.put("id",50+i);
							JSONObject device_feature = new JSONObject();
							device_feature.put("device_feature_model_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_feature_model_id"));
							device_feature.put("id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							device_feature.put("device_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_id"));
							Widget.put("device_feature", device_feature);
							ListFeature.put(Widget);
					}
					json_RoomList.put("room", rooms);
					json_FeatureAssociationList.put("feature_association",ListFeature);
					//Save result in sharedpref
					prefEditor.putString("AREA_LIST",json_AreaList.toString());
					prefEditor.putString("ROOM_LIST",json_RoomList.toString());
					prefEditor.putString("FEATURE_LIST",json_FeatureList.toString());
					prefEditor.putString("ASSOCIATION_LIST",json_FeatureAssociationList.toString());
//					prefEditor.putString("ICON_LIST",json_IconList.toString());
					prefEditor.putBoolean("SYNC", true);
					
					//insert in DB
					
					db.updateDb();
					db.insertArea(json_AreaList);
					db.insertRoom(json_RoomList);
					db.insertFeature(json_FeatureList);
					db.insertFeatureAssociation(json_FeatureAssociationList);
//					db.insertIcon(json_IconList);
				}
				
				Entity_Feature[] listFeature = db.requestFeatures();
				String urlUpdate = urlAccess+"stats/multi/";
				for (Entity_Feature feature : listFeature) {
					urlUpdate = urlUpdate.concat(feature.getDevId()+"/"+feature.getState_key()+"/");
				}
				prefEditor.putString("UPDATE_URL", urlUpdate);
				prefEditor.commit();
				Log.e("url", urlUpdate);
				sync=true;

			}catch(Exception e){
				handler.sendEmptyMessage(0);
				e.printStackTrace();
			}			return null;
		}

	}
}

