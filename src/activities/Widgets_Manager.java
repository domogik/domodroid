package activities;

import database.DomodroidDB;
import database.WidgetUpdate;

import org.json.JSONException;
import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Room;
import widgets.Graphical_Area;
import widgets.Graphical_Binary;
import widgets.Graphical_Boolean;
import widgets.Graphical_Cam;
import widgets.Graphical_Color;
import widgets.Graphical_Info;
import widgets.Graphical_Range;
import widgets.Graphical_Room;
import widgets.Graphical_Trigger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.DisplayMetrics;
import misc.Tracer;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

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
	public WidgetUpdate widgetupdate = null;

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
			String label = feature.getDescription();
			if(label.length() < 1)
				label = feature.getName();
			
			String Value_type = feature.getValue_type();
			String Address = feature.getAddress();
			int DevId = feature.getDevId();
			String State_key = feature.getState_key();
			
			//add debug option to change label adding its Id
			if (params.getBoolean("DEV",false)==true) 
				label = label+" ("+DevId+")";	//neutralized by Doume
			
			String[] model = feature.getDevice_type_id().split("\\.");
			String type = model[1];
			
			Tracer.i("Widgets_Manager", "Call to process device : "+DevId+" Address : "+Address+" Value_type : "+Value_type+" Label : "+label+" Key : "+State_key);
			if (feature.getValue_type().equals("binary")) {
				if(type.equals("rgb_leds") && (State_key.equals("command"))) {
					//ignore it : it'll have another device for Color, displaying the switch !)
				} else {
					onoff = new Graphical_Binary(context,feature.getAddress(),label,
							feature.getDevId(),
							feature.getState_key(),
							params.getString("URL","1.1.1.1"),
							feature.getDevice_usage_id(),
							feature.getParameters(),
							feature.getDevice_type_id(),
							params.getInt("UPDATE_TIMER",300),
							widgetSize);
					onoff.container=tmpPan;
					tmpPan.addView(onoff);
					Tracer.i("Widgets_Manager","   ==> Graphical_Binary");
				}
			} else if (feature.getValue_type().equals("boolean")) {
				bool = new Graphical_Boolean(context,feature.getAddress(),label,
						feature.getDevId(),
						feature.getState_key(),
						feature.getDevice_usage_id(), 
						feature.getDevice_type_id(),
						params.getInt("UPDATE_TIMER",300),
						widgetSize);
				bool.container=tmpPan;
				tmpPan.addView(bool);
				Tracer.i("Widgets_Manager","   ==> Graphical_Boolean");
			} else if (feature.getValue_type().equals("range")) {
				variator = new Graphical_Range(context,feature.getAddress(),label,
						feature.getDevId(),
						feature.getState_key(),
						params.getString("URL","1.1.1.1"),
						feature.getDevice_usage_id(),
						feature.getParameters(),
						feature.getDevice_type_id(),
						params.getInt("UPDATE_TIMER",300), 
						widgetSize);
				variator.container=tmpPan;
				tmpPan.addView(variator);
				Tracer.i("Widgets_Manager","   ==> Graphical_Range");
			} else if (feature.getValue_type().equals("trigger")) {
				trigger = new Graphical_Trigger(context,feature.getAddress(),label,
						feature.getDevId(),
						feature.getState_key(),
						params.getString("URL","1.1.1.1"),
						feature.getDevice_usage_id(),
						feature.getParameters(),
						feature.getDevice_type_id(),
						widgetSize);
				trigger.container=tmpPan;
				tmpPan.addView(trigger);
				Tracer.i("Widgets_Manager","   ==> Graphical_Trigger");
			//} else if(feature.getValue_type().equals("color")){
			} else if(feature.getState_key().equals("color")){
				Tracer.e("Widgets_Manager","add Graphical_Color for "+feature.getName()+" ("+feature.getDevId()+") key="+feature.getState_key());
				color = new Graphical_Color(context, 
						params, 
						feature.getDevId(),
						label,
						feature.getDevice_type_id(),	//Added by Doume to know the 'techno'
						feature.getAddress(),			//  idem to know the address
						feature.getState_key(),
						params.getString("URL","1.1.1.1"),
						feature.getDevice_usage_id(),
						params.getInt("UPDATE_TIMER",300),
						widgetSize);
				color.updateEngine = this.widgetupdate;		//Transfer pointer to update DB engine
				tmpPan.addView(color);
				Tracer.i("Widgets_Manager","   ==> Graphical_Color");
			} else if (feature.getValue_type().equals("number")) {
				Tracer.e("Widgets_Manager","add Graphical_Info for"+feature.getName()+" ("+feature.getDevId()+") key="+feature.getState_key());
				info = new Graphical_Info(context,feature.getDevId(), label,
						feature.getState_key(),
						params.getString("URL","1.1.1.1"),
						feature.getDevice_usage_id(),
						params.getInt("GRAPH",3),
						params.getInt("UPDATE_TIMER",300),
						widgetSize);
				info.setLayoutParams(layout_param);
				info.container=tmpPan;
				tmpPan.addView(info);
				Tracer.i("Widgets_Manager","   ==> Graphical_Info + Graphic");
			} else if(feature.getValue_type().equals("string")){
				if(feature.getDevice_feature_model_id().contains("camera")) {
					cam = new Graphical_Cam(context,feature.getId(),label,
							feature.getAddress(),
							widgetSize);
					tmpPan.addView(cam);
					Tracer.i("Widgets_Manager","   ==> Graphical_Cam");
				} //else if(feature.getDevice_feature_model_id().contains("communication")){
					//info = new Graphical_Info(context,feature.getDevId(),label,
						//	feature.getState_key(),
						//	"",
						//	feature.getDevice_usage_id(),
						//	0,
						//	params.getInt("UPDATE_TIMER",300),
						//	0);
					//info.setLayoutParams(layout_param);
					//info.with_graph=false;
					//tmpPan.addView(info);
					//Tracer.i("Widgets_Manager","   ==> Phone list !!!");
					//Must create a new Graphical widget to get a list of last call instead of just the last one.
				//}
					else {
					info = new Graphical_Info(context,feature.getDevId(),label,
							feature.getState_key(),
							"",
							feature.getDevice_usage_id(),
							0,
							params.getInt("UPDATE_TIMER",300),
							0);
					info.setLayoutParams(layout_param);
					info.with_graph=false;
					tmpPan.addView(info);
					Tracer.i("Widgets_Manager","   ==> Graphical_Info + No graphic !!!");
				}
			// missing getvalue_type().equals("list")
			//used by knx.HVACMode 	HVACMode 	actuator 	knx.HVACMode
				
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
		//Tracer.d("loadAreaWidgets","Areas list size : "+listArea.length);

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
				//e.printStackTrace();
			}
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			//Tracer.d("loadRoomWidgets","Adding area : "+area.getName());
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
		//Tracer.d("loadRoomWidgets","Rooms list size : "+listRoom.length);
		
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
			
			if(iconId.equals("unknown")) {
				//iconId="usage";
				iconId=room.getName();
			}
			tmpPan=null;
			tmpPan=new FrameLayout(context);
			String ref = room.getDescription();
			if(ref.length() == 0)
				ref = room.getName();
			Tracer.d("loadRoomWidgets","Adding room : "+ref);
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


