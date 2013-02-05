package database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;


import rinor.Rest_com;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import misc.Tracer;

public class WidgetUpdate implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SharedPreferences sharedparams;
	private static WidgetUpdate instance;
	
	private boolean activated;
	private DomodroidDB domodb;
	private Handler sbanim;
	private String mytag="WidgetUpdate";
	private TimerTask doAsynchronousTask;
	/*
	 * This class is a background engine 
	 * 		On instantiation, it connects to Rinor server, and submit queries 
	 * 		each 'update' timer, to update local database values for all known devices
	 * When variable 'activated' is set to false, the thread is kept alive, 
	 *     but each timer is ignored (no more requests to server...)
	 * When variable 'activated' is true, each timer generates a database update with server's response
	 */
	public WidgetUpdate(Activity context, Handler anim, SharedPreferences params){
		this.sharedparams=params;
		activated = true;
		domodb = new DomodroidDB(context);	
		domodb.owner=mytag;
		sbanim = anim;
		Tracer.d(mytag,"Initial start requested....");
		Timer();
		refreshNow();	// Force an immediate refresh
	}
	
	/* 
	 * Method allowing external methods to force a refresh
	 */
	public void refreshNow() {
		if(doAsynchronousTask != null)
			doAsynchronousTask.run();	//To force immediate refresh
	}
	
	/*
	 * This method should only be called once, to create and arm a cyclic timer 
	 */
	public void Timer() {
		final Timer timer = new Timer();
		
		
		final Handler handler = new Handler();
		doAsynchronousTask = new TimerTask() {
		
			@Override
			public void run() {
				Runnable myTH = new Runnable() {
				
					public void run() {
						if(activated) {
							try {
								new UpdateThread().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
						
						}
					} //End of run method
				};	// End of runnable bloc
				
				try {
					handler.post(myTH);		//To avoid exception on ICS
				} catch (Exception e) {
						e.printStackTrace();
				}
			}
		};
		
		// and arm the timer to do automatically this each 'update' seconds
		if(timer != null)
			timer.schedule(doAsynchronousTask, 0, sharedparams.getInt("UPDATE_TIMER", 300)*1000);
	}
	 
	
	public void stopThread(){
		Tracer.d(mytag,"stopThread requested....");
		activated = false;
	}
	public void restartThread(){
		Tracer.d(mytag,"restartThread requested....");
		activated = true;
	}
	public void cancelEngine(){
		Tracer.d(mytag,"cancelEngine requested....");
		activated = false;
		try {
			Timer();	//That should cancel running timer
			finalize();
		} catch (Throwable e) {
			
		}
	}
	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// Added by Doume to correctly release resources when exiting
			if(! activated) {
				Tracer.d(mytag,"UpdateThread frozen....");
				
			} else {
				Tracer.d(mytag,"UpdateThread Getting widget infos from server...");
				if(sharedparams.getString("UPDATE_URL", null) != null){
					try {
						sbanim.sendEmptyMessage(0);
						JSONObject json_widget_state = Rest_com.connect(sharedparams.getString("UPDATE_URL", null));
						//Tracer.d(mytag,"UPDATE_URL = "+ sharedparams.getString("UPDATE_URL", null).toString());
						//Tracer.d(mytag,"result : "+ json_widget_state);
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

