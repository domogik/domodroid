package org.panel;

import org.connect.Rest_com;
import org.database.DomodroidDB;
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
				JSONObject json_rinor = Rest_com.connect(urlAccess);
				JSONObject json_AreaList = Rest_com.connect(urlAccess+"base/area/list/");
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
				DomodroidDB db = new DomodroidDB(context);
				db.updateDb();
				db.insertArea(json_AreaList);
				db.insertRoom(json_RoomList);
				db.insertFeature(json_FeatureList);
				db.insertFeatureAssociation(json_FeatureAssociationList);
				db.insertIcon(json_IconList);
				
				
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

