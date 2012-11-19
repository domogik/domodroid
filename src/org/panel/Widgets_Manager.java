package org.panel;

import org.database.DomodroidDB;
import org.json.JSONException;
import org.widgets.Entity_Area;
import org.widgets.Entity_Feature;
import org.widgets.Entity_Room;
import org.widgets.Graphical_Area;
import org.widgets.Graphical_Binary;
import org.widgets.Graphical_Boolean;
import org.widgets.Graphical_Cam;
import org.widgets.Graphical_Color;
import org.widgets.Graphical_Info;
import org.widgets.Graphical_Range;
import org.widgets.Graphical_Room;
import org.widgets.Graphical_Trigger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

public class Widgets_Manager {

	private Graphical_Binary onoff;
	private Graphical_Boolean bool;
	private Graphical_Range variator;
	private Graphical_Info info;
	private Graphical_Trigger trigger;
	private Graphical_Cam cam;
	private Graphical_Color color;
	private Graphical_Area graph_area;
	private Graphical_Room graph_room;
	private int widgetSize;
	private int maxSize=700;
	private int width;
	private boolean columns=false;
	private Handler widgetHandler;

	public Widgets_Manager(Handler handler) {
		super();
		this.widgetHandler=handler;
	}

	public LinearLayout loadActivWidgets(Activity context, int id, String zone, LinearLayout ll, SharedPreferences params) throws JSONException{

		DomodroidDB domodb = new DomodroidDB(context);
		domodb.owner="Widgets_Manager.loadActivWidgets";
		Entity_Feature[] listFeature = domodb.requestFeatures(id, zone);

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);

		int counter=0;

		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width= metrics.widthPixels;


		if(width>maxSize){
			columns=true;
			mainPan.addView(leftPan);
			mainPan.addView(rightPan);
			ll.addView(mainPan);
		}

		for (Entity_Feature feature : listFeature) {

			//-----add component-------
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			if (feature.getValue_type().equals("binary")) {
				onoff = new Graphical_Binary(context,feature.getAddress(),feature.getName(),feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE_TIMER",300),widgetSize);
				tmpPan.addView(onoff);}
			if (feature.getValue_type().equals("boolean")) {
				bool = new Graphical_Boolean(context,feature.getAddress(),feature.getName(),feature.getDevId(),feature.getState_key(),feature.getDevice_usage_id(), feature.getDevice_type_id(),params.getInt("UPDATE_TIMER",300),widgetSize);
				tmpPan.addView(bool);}
			if (feature.getValue_type().equals("range")) {
				variator = new Graphical_Range(context,feature.getAddress(),feature.getName(),feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE_TIMER",300), widgetSize);
				tmpPan.addView(variator);}
			if (feature.getValue_type().equals("trigger")) {
				trigger = new Graphical_Trigger(context,feature.getAddress(),feature.getName(),feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),widgetSize);
				tmpPan.addView(trigger);}
			if (feature.getValue_type().equals("number")) {
				Log.e("Widgets_Manager","add "+feature.getName());
				info = new Graphical_Info(context,feature.getDevId(),feature.getName(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),params.getInt("GRAPH",3),params.getInt("UPDATE_TIMER",300),0);
				info.setLayoutParams(layout_param);
				tmpPan.addView(info);
			}
			if(feature.getValue_type().equals("string")){
				if(feature.getDevice_feature_model_id().contains("camera")) {
					cam = new Graphical_Cam(context,feature.getId(),feature.getName(),feature.getAddress(),widgetSize);
					tmpPan.addView(cam);
				} else {
					info = new Graphical_Info(context,feature.getDevId(),feature.getName(),feature.getState_key(),"",feature.getDevice_usage_id(),0,params.getInt("UPDATE_TIMER",300),0);
					info.setLayoutParams(layout_param);
					tmpPan.addView(info);
				}
				
			}
			if(feature.getValue_type().equals("color")){
				color = new Graphical_Color(context, params, feature.getDevId(),feature.getName(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),params.getInt("UPDATE_TIMER",300),0);
				tmpPan.addView(color);
			}
			if(columns){	
				if(counter==0){
					leftPan.addView(tmpPan);
				}
				else if(counter==1){
					rightPan.addView(tmpPan);
				}
				counter++;
				if(counter==2)counter=0;
			}else ll.addView(tmpPan);
		}
		return ll;
	}


	public LinearLayout loadAreaWidgets(Activity context, LinearLayout ll, SharedPreferences params) throws JSONException {

		DomodroidDB domodb = new DomodroidDB(context);
		domodb.owner="Widgets_Manager.loadAreaWidgets";
		Entity_Area[] listArea = domodb.requestArea();

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);

		int counter=0;

		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width= metrics.widthPixels;


		if(width>maxSize){
			columns=true;
			mainPan.addView(leftPan);
			mainPan.addView(rightPan);
			ll.addView(mainPan);
		}


		for (Entity_Area area : listArea) {
			String iconId = "unknown";
			try {
				iconId = domodb.requestIcons(area.getId(), "area").getValue().toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			graph_area = new Graphical_Area(context,area.getId(),area.getName(),area.getDescription(),iconId,widgetSize,widgetHandler);
			tmpPan.addView(graph_area);
			if(columns){	
				if(counter==0){
					leftPan.addView(tmpPan);
				}
				else if(counter==1){
					rightPan.addView(tmpPan);
				}
				counter++;
				if(counter==2)counter=0;
			}else ll.addView(tmpPan);
		}
		return ll;
	}

	public LinearLayout loadRoomWidgets(Activity context, int id, LinearLayout ll, SharedPreferences params) throws JSONException {

		DomodroidDB domodb = new DomodroidDB(context);
		domodb.owner="Widgets_Manager.loadRoomWidgets";
		Entity_Room[] listRoom = domodb.requestRoom(id);

		LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT, 1.0f);
		LinearLayout mainPan = new LinearLayout(context);
		mainPan.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout leftPan = new LinearLayout(context);
		leftPan.setOrientation(LinearLayout.VERTICAL);
		leftPan.setLayoutParams(layout_param);
		LinearLayout rightPan = new LinearLayout(context);
		rightPan.setOrientation(LinearLayout.VERTICAL);
		rightPan.setLayoutParams(layout_param);
		FrameLayout tmpPan = new FrameLayout(context);
		int counter=0;

		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width= metrics.widthPixels;


		if(width>maxSize){
			columns=true;
			mainPan.addView(leftPan);
			mainPan.addView(rightPan);
			ll.addView(mainPan);
		}


		for (Entity_Room room : listRoom) {

			String iconId = "unknown";
			try{
				iconId = domodb.requestIcons(room.getId(),"room").getValue().toString();
			}catch(Exception e){};

			tmpPan=null;
			tmpPan=new FrameLayout(context);
			graph_room = new Graphical_Room(context,room.getId(),room.getName(),room.getDescription(),iconId,widgetSize,widgetHandler);
			tmpPan.addView(graph_room);

			if(columns){	
				if(counter==0){
					leftPan.addView(tmpPan);
				}
				else if(counter==1){
					rightPan.addView(tmpPan);
				}
				counter++;
				if(counter==2)counter=0;
			}else ll.addView(tmpPan);
		}
		return ll;
	}

}


