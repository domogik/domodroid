package org.database;

import java.util.Timer;
import java.util.TimerTask;

import org.connect.Rest_com;
import org.json.JSONObject;
import org.map.MapView.UpdateThread;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class WidgetUpdate {

	private SharedPreferences sharedparams;
	private boolean activated;
	private DomodroidDB domodb;
	private Handler sbanim;

	public WidgetUpdate(Activity context, Handler anim, SharedPreferences params){
		this.sharedparams=params;
		activated = true;
		domodb = new DomodroidDB(context);	
		sbanim = anim;
		Timer();
	}
	

	public void Timer() {
		TimerTask doAsynchronousTask;
		final Handler handler = new Handler();
		final Timer timer = new Timer();

		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				Runnable myTH = new Runnable() {
					
					//handler.post(new Runnable() {	//Doume change
						public void run() {
							try {
								new UpdateThread().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}}
					//)
					;
					try {
						handler.post(myTH);		//To avoid exception on ICS
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
		};
		timer.schedule(doAsynchronousTask, 0, sharedparams.getInt("UPDATE_TIMER", 300)*1000);
	}

	public void stopThread(){
		activated = false;
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// Added by Doume to correctly release resources when exiting
			if(! activated) {
				domodb = null;
				try {
					finalize();
					} 
				catch (Throwable e) {
				}
			//////////////
			} else {
				if(sharedparams.getString("UPDATE_URL", null) != null){
					try {
						sbanim.sendEmptyMessage(0);
						JSONObject json_widget_state = Rest_com.connect(sharedparams.getString("UPDATE_URL", null));
						Log.e("update url", sharedparams.getString("UPDATE_URL", null).toString());
						Log.e("result", json_widget_state+"");
						sbanim.sendEmptyMessage(1);
						domodb.insertFeatureState(json_widget_state);
						sbanim.sendEmptyMessage(2);
					} catch (Exception e) {
						sbanim.sendEmptyMessage(3);
						e.printStackTrace();
					}
				}
			}
			return null;
		}
	}
}

