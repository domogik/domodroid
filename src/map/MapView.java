package map;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.domogik.domodroid.R;

import database.WidgetUpdate;
import activities.Graphics_Manager;
import activities.Sliding_Drawer;
import widgets.Entity_Map;
import widgets.Entity_client;
import widgets.Graphical_Binary;
import widgets.Graphical_Binary_New;
import widgets.Graphical_Boolean;
import widgets.Graphical_Cam;
import widgets.Graphical_Color;
import widgets.Graphical_Info;
import widgets.Graphical_List;
import widgets.Graphical_Range;
import widgets.Graphical_Trigger;
import widgets.Graphical_Binary.SBAnim;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MapView extends View {
	private Bitmap map;
	private Bitmap widget;
	private Bitmap drawable;
	public int width;
	public int height;
	private Canvas canvasMap;
	private Canvas canvasWidget;
	private TransformManager mat;
	private Matrix origin;
	private SVG svg;
	float currentScale = 1;
	float currentScalewidth = 1;
	float currentScaleheight = 1;
	private int screenwidth;
	private int screenheight;
	private boolean addMode=false;
	private boolean removeMode=false;
	private boolean moveMode=false;
	private int update;
	private static int text_Offset_X = 25;
	private static int text_Offset_Y = 0;
	private int moves;
	private SharedPreferences.Editor prefEditor;
	private boolean map_autozoom=false;
	public int temp_id;
	public int map_id;
	public String map_name = "";

	private Paint paint_map;
	private Paint paint_text;
	private ViewGroup panel_widget;
	private ViewGroup panel_button;
	private Activity context;
	private Sliding_Drawer top_drawer;
	private Sliding_Drawer bottom_drawer;


	private Graphical_Trigger trigger;
	private Graphical_Range variator;
	private Graphical_Binary onoff;
	private Graphical_Binary_New onoff_New;
	private Graphical_Info info;
	private Graphical_Boolean bool;
	private Graphical_Color colorw;
	private Graphical_List list;
	private Graphical_Cam cam;
	
	private Vector<String> files;
	private Entity_Map[] listFeatureMap;
	private Entity_Map[] listMapSwitches;
	private int mode;
	private int formatMode;
	private String svg_string;
	private int currentFile = 0;

	private SharedPreferences params;

	private float pos_X0=0;
	private float pos_X1=0;

	private int screen_width;
	//private Boolean activated; 
	private String mytag="MapView";
	private Boolean locked = false;
	private String parameters;
	private int valueMin;
	private int valueMax;
	private String value0;
	private String value1;
	private static Handler handler = null;
	private tracerengine Tracer = null;
	private int mytype = 2;
	private WidgetUpdate cache_engine = null;
	
	public MapView(tracerengine Trac, Activity context) {
		super(context);
		this.Tracer = Trac;
		this.context=context;
		//activated=true;
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		screen_width = metrics.widthPixels;
		startCacheEngine();
		/*
		 * This view has only one handler for all mini widgets displayed on map
		 * It'll receive a unique notification from WidgetUpdate when one or more values have changed
		 */
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 9997) {
					//state_engine send us a signal to notify at least one value changed
					Tracer.d(mytag,"state engine notify change for mini widget(s) : refresh all of them !" );
					
					for (Entity_Map featureMap : listFeatureMap) {
						// if a miniwidget was connected to engine, session's value could have changed....
						if(featureMap.getSession() != null) {
							featureMap.setCurrentState(featureMap.getSession().getValue());
						} 
						
					}
					refreshMap();
					
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					
					try {
						finalize();
					} catch (Throwable t) {}
					
					
				}
			}	
		};
		//End of create method ///////////////////////
		
	}
	
	private void startCacheEngine() {
		
		if(cache_engine == null) {
			Tracer.w("Activity_Map", "Starting WidgetUpdate engine !");
			cache_engine = WidgetUpdate.getInstance();
			//MapView is'nt the first caller, so init is'nt required (already done by View)
			cache_engine.set_handler(handler, mytype);	//Put our main handler to cache engine (as MapView)
		}  
		Tracer.set_engine(cache_engine);
		Tracer.w("Activity_Map", "WidgetUpdate engine connected !");
		
	}
	
	public void purge() {
		// TODO We've to unsubscribe all connected mini widgets from cache engine
	}
	
	public void  onWindowVisibilityChanged (int visibility) {
		Tracer.i(mytag,"Visibility changed to : "+visibility);
		/*
		if(visibility == View.VISIBLE)
			//this.activated = true;
		else
			//activated=false;
		 */
	}
	
	public void clear_Widgets(){
		String map_name=files.elementAt(currentFile);
		Tracer.i(mytag,"Request to clear all widgets from : "+map_name);
		Tracer.get_engine().cleanFeatureMap(map_name);
		initMap();
		
	}
	
	public void removefile(){
		//remove the current file
		File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile));
		Tracer.i(mytag,"Request to remove "+currentFile);
		f.delete();
		initMap();		
	}
	
	public void initMap(){
		Toast.makeText(context, files.elementAt(currentFile).substring(0,files.elementAt(currentFile).lastIndexOf('.')), Toast.LENGTH_SHORT).show();
		
		//listFeatureMap = domodb.requestFeatures(files.elementAt(currentFile));
		listFeatureMap = Tracer.get_engine().getMapFeaturesList(files.elementAt(currentFile));
		listMapSwitches = Tracer.get_engine().getMapSwitchesList(files.elementAt(currentFile));
		
		//Each real mini widget must be connected to cache engine, to receive notifications
		for (Entity_Map featureMap : listFeatureMap) {
			int map_id = featureMap.getDevId();
			Entity_client cursession = new Entity_client(
						map_id,
						featureMap.getState_key(), 
						"mini widget",
						handler,
						mytype);
			cursession.setType(true);	//It's a mini widget !
			
			if(Tracer.get_engine().subscribe(cursession) ) {
				//This widget is connected to state_engine
				featureMap.setSession(cursession);
				featureMap.setCurrentState(cursession.getValue());
			} else {
				// cannot connect it ????
				Tracer.i(mytag,"Cannot connect mini widget to state engine : ("+cursession.getDevId()+") ("+cursession.getskey()+") => it'll not be updated !");
				featureMap.setCurrentState("????");
			}
			
		}
	    //get file extension
		String extension = files.elementAt(currentFile).substring(files.elementAt(currentFile).lastIndexOf('.'));
	    //put extension in lowercase
		extension=extension.toLowerCase();
		   
		if(extension.equals(".svg")){
			formatMode=1;
		//Try to allow PNG and png extension to solve #1707 on irc tracker.
		//Could also try to put all in lowercase: files.elementAt(currentFile).substring(files.elementAt(currentFile).toLowerCase()......
		}else if(extension.equals(".png")||extension.equals(".jpg")||extension.equals(".jepg")){ 
			formatMode=2;
		}else{
			formatMode=0;
		}
		
		//Load current scale if it exists.
		if(params.getFloat("Mapscale", 1)!=1){
			currentScale=params.getFloat("Mapscale", 1);
		}
			map_autozoom=params.getBoolean("map_autozoom", false);
		origin = new Matrix();
		mat = new TransformManager();
		mat.setZoom(params.getBoolean("ZOOM", false));
		mat.setDrag(params.getBoolean("DRAG", false));
		mat.setScreenConfigScaling();
		paint_text = new Paint();
		paint_text.setPathEffect(null);
		paint_text.setAntiAlias(true);
		paint_text.setStyle(Paint.Style.FILL_AND_STROKE);
		paint_text.setColor(Color.WHITE);
		paint_text.setShadowLayer(1, 0, 0, Color.BLACK);
		
		//Get screen size
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		screenwidth= metrics.widthPixels;
		screenheight= metrics.heightPixels;
		
		//Case using a svg file as map
		if(formatMode==1){	
			try{
			
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			svg_string = getFileAsString(f);
			svg = SVGParser.getSVGFromString(svg_string);
			//adjust to scale
			if (map_autozoom==true){
				currentScale=autoscale((int)svg.getSurfaceWidth(),(int)svg.getSurfaceHeight());
			}
			svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
			Picture picture = svg.getPicture();
			map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			canvasMap.drawPicture(picture);
			widget = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
			
			}catch (Exception e) {
				Tracer.e("MapView initmap()","formatMode=1 "+e.getStackTrace().toString());
				return;
			}
		//Case using a png file as map
		}else if(formatMode==2){
			try{
		
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			Bitmap bitmap = decodeFile(f);
			//adjust to scale
			if (map_autozoom==true){
				currentScale=autoscale(bitmap.getWidth(),bitmap.getHeight());
			}
			map = Bitmap.createBitmap((int)(bitmap.getWidth()*currentScale), (int)(bitmap.getHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			Matrix matScale = new Matrix();
			matScale.postScale(currentScale, currentScale);
			canvasMap.setMatrix(matScale);
			canvasMap.drawBitmap(bitmap, 0, 0, paint_map );
			widget = Bitmap.createBitmap((int)((bitmap.getWidth()+200)*currentScale), (int)((bitmap.getHeight()+200)*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
		
			}catch (Exception e) {
			Tracer.e("MapView initmap()","formatMode=2 "+e.getStackTrace().toString());
			return;
		}
		}
		drawWidgets();

		postInvalidate();
	}

	public void refreshMap(){
		canvasMap=null;
		canvasWidget=null;
		System.gc();	//Run garbage collector to free maximum of memory
		
		//Case using a svg file as map
		if(formatMode==1){
			try{
				
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			svg_string = getFileAsString(f);
			svg = SVGParser.getSVGFromString(svg_string);
			//adjust to scale
			if (map_autozoom==true){
				currentScale=autoscale((int)svg.getSurfaceWidth(),(int)svg.getSurfaceHeight());
			}
			svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
			Picture picture = svg.getPicture();
			map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			canvasMap.drawPicture(picture);	
			widget = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
			//Case using a png file as map
			
			}catch (Exception e) {
			Tracer.e("MapView refreshmap()","formatMode=1 "+e.getStackTrace().toString());
			return;
			}
		}else if(formatMode==2){
			try{
			
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			Bitmap bitmap = decodeFile(f);
			//adjust to scale
			if (map_autozoom==true){
				currentScale=autoscale(bitmap.getWidth(),bitmap.getHeight());
			}
			map = Bitmap.createBitmap((int)(bitmap.getWidth()*currentScale), (int)(bitmap.getHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			Matrix matScale = new Matrix();
			matScale.postScale(currentScale, currentScale);
			canvasMap.setMatrix(matScale);
			canvasMap.drawBitmap(bitmap, 0, 0, paint_map );
			Tracer.e(mytag, "Trying to create widget at scale : "+currentScale);
			widget = Bitmap.createBitmap((int)((bitmap.getWidth()+200)*currentScale), (int)((bitmap.getHeight()+200)*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);

		}catch (Exception e) {
		Tracer.e("MapView refreshmap()","formatMode=2 "+e.getStackTrace().toString());
		return;
		
		}
		}

		drawWidgets();
		postInvalidate();
	}

	public void drawWidgets(){
		if(locked) {
			return;
		}
		locked=true;
		int id = 0;
		// first try to process map switches, if any
		Tracer.e(mytag, "Processing map switches widgets list");
		
		if(listMapSwitches != null) {
			
			for (Entity_Map switchesMap : listMapSwitches) {
				id=switchesMap.getId();
				
				//Its a map switch widget
				id = id - 9999;
				if( (id >= 0 ) && (id < files.size()) ) {
					String mapname = files.elementAt(id);
					Tracer.e(mytag, "Processing switch to map <"+mapname+">");
					// Draw symbol of 'map_next'
					try {
						
						drawable = BitmapFactory.decodeResource(getResources(), R.drawable.map_next);
						if(drawable != null) {
							canvasWidget.drawBitmap(drawable, 
									(switchesMap.getPosx()*currentScale)-drawable.getWidth()/2, 
									(switchesMap.getPosy()*currentScale)-drawable.getWidth()/2, 
									paint_map);
						} else {
							Tracer.e("MapView","No drawable available for map switch");
							return;
						}
					
					} catch (Exception e) {
						Tracer.e("MapView","cannot draw map switch icon ! ! ! !");
						return;
					}
				
					//Draw the map name text
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						canvasWidget.drawText(mapname, 
								(switchesMap.getPosx()*currentScale)+text_Offset_X, 
								(switchesMap.getPosy()*currentScale)+text_Offset_Y, 
								paint_text);
					}
				}
			}
		}
		// An now process real widgets
		Tracer.e(mytag, "Processing normal widgets list");
		
		for (Entity_Map featureMap : listFeatureMap) {
			
			String states = "";
			JSONObject jparam;
			
			if(featureMap != null) {
				states = featureMap.getCurrentState();
			} else {
				Tracer.e("MapView","Wrong feature in featureMap list ! ! ! Abort processing !");
				return;
			}
			
			if(featureMap.isalive()) {
				//set intstate to select correct icon color
				int intstate = 0;
				if(! (featureMap.getState_key().equals("color"))) {
					//get parameters valuemin,max, 0 and 1
					parameters=featureMap.getParameters();
					try {
						jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
						value0 = jparam.getString("value0");
						value1 = jparam.getString("value1");
						valueMin = jparam.getInt("valueMin");
						valueMax = jparam.getInt("valueMax");
					} catch (JSONException e1) {
						//e1.printStackTrace();
						//Tracer.e("MapView","DrawWidget No parameters ! ");
						//Tracer.e("MapView","DrawWidget   for mini widget type <"+featureMap.getValue_type()+">");
						
						//TODO : what to put into value0, 1, min & max ?
					}
					//Tracer.e("MapView","DrawWidget value0  <"+value0+"> value1 <"+value1+"> valueMin <"+valueMin+"> valueMax <"+valueMax+">");
				}
				if ((states.equals(value1)) ||((featureMap.getValue_type().equals("range") && (Integer.parseInt(states)>valueMin))))
				//if ((states.equals("high")) || (states.equals("on") || ((featureMap.getValue_type().equals("range") && (Integer.parseInt(states)>0)))))
				{
					intstate=1;
				}
				//set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
				featureMap.setState(intstate);
				
				try {
					// Draw symbol of feature
					drawable = BitmapFactory.decodeResource(getResources(), featureMap.getRessources());
					if(drawable != null) {
						canvasWidget.drawBitmap(drawable, 
								(featureMap.getPosx()*currentScale)-drawable.getWidth()/2, 
								(featureMap.getPosy()*currentScale)-drawable.getWidth()/2, 
								paint_map);
						Tracer.e("MapView","Draw symbol of feature X="+((featureMap.getPosx()*currentScale)-drawable.getWidth()/2)+" Y="+((featureMap.getPosy()*currentScale)-drawable.getWidth()/2)+" MAP "+paint_map);
					} else {
						Tracer.e("MapView","No drawable available for object");
						return;
					}
				} catch (Exception e) {
					Tracer.e("MapView","cannot draw object ! ! ! !");
					return;
				}
				
				// Draw state and description
				
				if(featureMap.getValue_type().equals("string") && (! featureMap.getState_key().equals("color"))){
					if(! featureMap.getDevice_feature_model_id().contains("camera")) {
						for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(16);
							canvasWidget.drawText(featureMap.getCurrentState(), 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y, 
									paint_text);
						}
					}
					else if(featureMap.getDevice_feature_model_id().contains("camera")) {
						for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(16);
							String label = featureMap.getDescription();
							if(label.length() < 1)
								label = featureMap.getDevice_usage_id();
							canvasWidget.drawText(label, 
								(featureMap.getPosx()*currentScale)+text_Offset_X, 
								(featureMap.getPosy()*currentScale)+text_Offset_Y, 
								paint_text);
						}
					}
					
						
				} else if(featureMap.getValue_type().equals("binary") || featureMap.getValue_type().equals("boolean")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						//canvasWidget.drawText(featureMap.getCurrentState().toUpperCase(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
						paint_text.setTextSize(14);
						if (params.getBoolean("HIDE",false)==false){ 
							String label = featureMap.getDescription();
							if(label.length() < 1)
								label = featureMap.getDevice_usage_id();
							canvasWidget.drawText(featureMap.getCurrentState().toUpperCase(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
							canvasWidget.drawText(label, (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y+15, paint_text);
							//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						}
					}
				
				} else if(featureMap.getValue_type().equals("number")){
					String value;
					try {
						//Basilic add, number feature has a unit parameter
						jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
						String test_unite = jparam.getString("unit");
						value=featureMap.getCurrentState()+test_unite;
					} catch (JSONException e) {				
						//Basilic : no sure that the key state was the better way to find unit
						if(featureMap.getState_key().equals("temperature"))value=featureMap.getCurrentState()+" °C";
						else if(featureMap.getState_key().equals("pressure"))value=featureMap.getCurrentState()+" hPa";
						else if(featureMap.getState_key().equals("humidity"))value=featureMap.getCurrentState()+" %";
						else if(featureMap.getState_key().equals("percent"))value=featureMap.getCurrentState()+" %";
						else if(featureMap.getState_key().equals("visibility"))value=featureMap.getCurrentState()+" km";
						else if(featureMap.getState_key().equals("chill"))value=featureMap.getCurrentState()+" °C";
						else if(featureMap.getState_key().equals("speed"))value=featureMap.getCurrentState()+" km/h";
						else if(featureMap.getState_key().equals("drewpoint"))value=featureMap.getCurrentState()+" °C";
						else if( (featureMap.getState_key().equals("condition-code")) && (!featureMap.getCurrentState().equals("--") ) )
							value=context.getString(Graphics_Manager.Names_conditioncodes(Integer.parseInt(featureMap.getCurrentState())));
						else value=featureMap.getCurrentState();
					}
					if(value == null)
						value = "";
					
						for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(20);
							if(featureMap != null) {
								String label = featureMap.getDescription();
								if(label.length() < 1)
									label = featureMap.getState_key();
								if(label == null)
									label = "";
								
								//Tracer.e("MapView","Drawing value for "+label+"Value = "+value+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
								canvasWidget.drawText(value, (featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y-10, 
										paint_text);
								paint_text.setTextSize(15);
								//Tracer.e("MapView","Drawing label "+label+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
								if (params.getBoolean("HIDE",false)==false){ 
									canvasWidget.drawText(label, (featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y+6, 
										paint_text);
								}
							}
						}
				} else if(featureMap.getValue_type().equals("range")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						//canvasWidget.drawText(featureMap.getCurrentState(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
						paint_text.setTextSize(14);
						if (params.getBoolean("HIDE",false)==false){ 
							//TODO see if we should not use label instead of featureMap.getDevice_usage_id()
							//It is not the same text displayed for this type of device
							canvasWidget.drawText(featureMap.getDevice_usage_id(), 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y+15, 
									paint_text);
							canvasWidget.drawText(featureMap.getCurrentState(), 
									(featureMap.getPosx()*currentScale)+text_Offset_X, 
									(featureMap.getPosy()*currentScale)+text_Offset_Y, 
									paint_text);
							//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						}else{
							if  (featureMap.getState_key().equals("light")){
								if (Integer.parseInt(featureMap.getCurrentState()) > valueMin){
									canvasWidget.drawText(featureMap.getCurrentState(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
								}
							}else{
								canvasWidget.drawText(featureMap.getCurrentState(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
							}
						}
					}

				}else if(featureMap.getValue_type().equals("trigger")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						//TODO see if we should not use label instead of featureMap.getName()
						//It is not the same text displayed for this type of device
						canvasWidget.drawText(featureMap.getName(), 
								(featureMap.getPosx()*currentScale)+text_Offset_X, 
								(featureMap.getPosy()*currentScale)+text_Offset_Y, 
								paint_text);
						//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						
					}
				}else if(featureMap.getState_key().equals("color")){
					Tracer.e("MapView","Drawing color for "+featureMap.getName()+" Value = "+states);
					Paint paint_color = new Paint();
					paint_color.setPathEffect(null);
					paint_color.setAntiAlias(true);
					//paint_color.setStyle(Paint.Style.FILL_AND_STROKE);
					paint_color.setStyle(Paint.Style.FILL);
					String argbS = states;
					//Process RGB value
					if(states.equals("off")) {
						argbS="#000000";
					} else if(argbS.equals("on")) {
						argbS=params.getString("COLORRGB", "#FFFFFF");	//Restore last known color, White by default
						
					} 
					//Tracer.e("MapView","Drawing color for "+featureMap.getName()+" RGB Value = "+Integer.toHexString(loc_argb));
					//Draw first a black background...
					paint_color.setColor(Color.BLACK);
					paint_color.setShadowLayer(1, 0, 0, Color.BLACK);
					int left = (int)(featureMap.getPosx()*currentScale)+text_Offset_X-10;
					int top  = (int)(featureMap.getPosy()*currentScale)+text_Offset_Y-15;
					int right= (int)(featureMap.getPosx()*currentScale)+text_Offset_X+85;
					int bottom=(int)(featureMap.getPosy()*currentScale)+text_Offset_Y+10;
					Rect r = new Rect(left,top,right,bottom);
					canvasWidget.drawRect(r, paint_color);
					
					//And draw real color inside the 1st one
					paint_color.setColor(Color.parseColor(argbS));
					left+=3;
					top+=3;
					right-=3;
					bottom-=3;
					r = new Rect(left,top,right,bottom);
					canvasWidget.drawRect(r, paint_color);
					
					/*
						canvasWidget.drawText(featureMap.getCurrentState(), 
								(featureMap.getPosx()*currentScale)+text_Offset_X, 
								(featureMap.getPosy()*currentScale)+text_Offset_Y-10, 
								paint_text);
					*/	
					for(int j=1;j<5;j++){
							paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
							paint_text.setTextSize(16);
							
						canvasWidget.drawText(featureMap.getDescription(), 
							(featureMap.getPosx()*currentScale)+text_Offset_X, 
							(featureMap.getPosy()*currentScale)+text_Offset_Y+25, 
							paint_text);
					}
				}
			} else {
				// This widget is'nt alive anymore...
				canvasWidget = null; //?????
				}
			
		}
		locked=false;
		
	}	
	
	public void showTopWidget(Entity_Map feature) throws JSONException{
		if(panel_widget.getChildCount()!=0){
			panel_widget.removeAllViews();
		}
		String label = feature.getDescription();
		if(label.length() < 1)
			label = feature.getName();
		
		String State_key = feature.getState_key();
		
		//add debug option to change label adding its Id
		if (params.getBoolean("DEV",false)==true)
			label = label+" ("+feature.getDevId()+")";
		
		String[] model = feature.getDevice_type_id().split("\\.");
		String type = model[1];
		
		if (feature.getValue_type().equals("binary")) {
			if(type.equals("rgb_leds") && (State_key.equals("command"))) {
				//ignore it : it'll have another device for Color, displaying the switch !)
			} else {
				if (params.getBoolean("WIDGET_CHOICE",false)==false) {
				onoff = new Graphical_Binary(Tracer, context,feature.getAddress(),
				label,feature.getId(),feature.getDevId(),feature.getState_key(),
				params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),
				feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE",300),0, mytype);
				onoff.container=(FrameLayout) panel_widget;
				panel_widget.addView(onoff);
				}
				else{
				onoff_New = new Graphical_Binary_New(Tracer, context,feature.getAddress(),
				label,feature.getId(),feature.getDevId(),feature.getState_key(),
				params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),
				feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE",300),0, mytype);
				onoff_New.container=(FrameLayout) panel_widget;
				panel_widget.addView(onoff_New);
				}
					
			}
		}
		else if (feature.getValue_type().equals("boolean")) {
			bool = new Graphical_Boolean(Tracer, context,feature.getAddress(),
					label,feature.getId(),feature.getDevId(),feature.getState_key(),
					feature.getDevice_usage_id(),feature.getParameters(),
					feature.getDevice_type_id(),params.getInt("UPDATE",300),0, mytype);
			bool.container=(FrameLayout) panel_widget;
			panel_widget.addView(bool);}
		else if (feature.getValue_type().equals("range")) {
			variator = new Graphical_Range(Tracer, context,feature.getAddress(),
					label,feature.getId(),feature.getDevId(),feature.getState_key(),
					params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),
					feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE",300),0, mytype);
			variator.container=(FrameLayout) panel_widget;
			panel_widget.addView(variator);}
		else if (feature.getValue_type().equals("trigger")) {
			trigger = new Graphical_Trigger(Tracer, context,feature.getAddress(),
					label,feature.getId(),feature.getDevId(),feature.getState_key(),
					params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),
					feature.getParameters(),feature.getDevice_type_id(),0, mytype);
			trigger.container=(FrameLayout) panel_widget;
			panel_widget.addView(trigger);}
		else if (feature.getValue_type().equals("number")) {
			info = new Graphical_Info(Tracer, context,feature.getId(),feature.getDevId(),
					label,
					feature.getState_key(),params.getString("URL","1.1.1.1"),
					feature.getDevice_usage_id(),
					params.getInt("GRAPH",3),
					params.getInt("UPDATE",300),0, mytype, feature.getParameters());
			info.container=(FrameLayout) panel_widget;
			panel_widget.addView(info);}
		 else if (feature.getValue_type().equals("list")) {
			list = new Graphical_List(Tracer, context,feature.getId(),feature.getDevId(), label,
					feature.getDevice_type_id(),	//Added by Doume to know the 'techno'
					feature.getAddress(),			//  idem to know the address
					feature.getState_key(),
					params.getString("URL","1.1.1.1"),
					feature.getDevice_usage_id(),
					params.getInt("GRAPH",3),
					params.getInt("UPDATE",300),0, mytype, feature.getParameters(),feature.getDevice_type_id());
			list.container=(FrameLayout) panel_widget;
			panel_widget.addView(list);}
		else if (feature.getState_key().equals("color")) {
			colorw = new Graphical_Color(Tracer, context,
					params,
					feature.getId(),feature.getDevId(),
					label,
					feature.getDevice_feature_model_id(),
					feature.getAddress(),
					feature.getState_key(),
					params.getString("URL","1.1.1.1"),
					feature.getDevice_usage_id(),
					params.getInt("UPDATE",300),
					0, mytype
					);
			colorw.container=(FrameLayout) panel_widget;
			panel_widget.addView(colorw);}
		 else if(feature.getValue_type().equals("string")){
			if(feature.getDevice_feature_model_id().contains("camera")) {
				cam = new Graphical_Cam(Tracer, context,
					feature.getId(),
					feature.getDevId(),
					label,
					feature.getAddress(),0, mytype);
				panel_widget.addView(cam);}
			else {
				info = new Graphical_Info(Tracer, context,feature.getId(),feature.getDevId(),label,
						feature.getState_key(),
						"",
						feature.getDevice_usage_id(),
						0,
						params.getInt("UPDATE_TIMER",300),
						0, mytype, feature.getParameters());
				info.container=(FrameLayout) panel_widget;
				info.with_graph=false;
				panel_widget.addView(info);}
			}
		
	//TODO Seems it miss some device type like in Widgets_Manager.java
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		origin = canvas.getMatrix();
		origin.postConcat(mat.matrix);
		canvas.setMatrix(origin);
		canvas.drawBitmap(map, 100*currentScale, 100*currentScale, paint_map);
		canvas.drawBitmap(widget, 0, 0, paint_map);
	}

	public Bitmap decodeFile(File f){
		Bitmap b = null;
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;

			FileInputStream fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			if (o.outHeight > params.getInt("SIZE", 600) || o.outWidth > params.getInt("SIZE", 600)) {
				scale = (int)Math.pow(2, (int) Math.round(Math.log(params.getInt("SIZE", 600) / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
			}

			//Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o2);
			fis.close();
		} catch (IOException e) {
		}
		return b;
	}

	public boolean onTouchEvent(MotionEvent event) {
		int nbPointers = event.getPointerCount();
		float[] value = new float[9];
		float[] saved_value = new float[9];
		mat.matrix.getValues(value);
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			moves=0;
			mat.matrix.getValues(saved_value);
			mat.actionDown(event.getX(), event.getY());
			//save to pos_XO where was release the press
			pos_X0 = event.getX();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mat.actionPointerDown(event);
			break;
			//when stop pressing
		case MotionEvent.ACTION_UP:
			mat.actionUp(event.getX(), event.getY());
			//save to pos_X1 where was release the press
			pos_X1 = event.getX();
			//Select what action to do
			//Add a widget mode
			if (addMode==true){
				int db_id = 0;
				if(temp_id != -1) {
					//insert in the database feature map the device id, its position and map name. 
					db_id = temp_id;
				} else {
					if (map_id != -1) {
						db_id = map_id;
						// a map switch has been selected from list of widgets
					}
				}
				if (db_id != 0) {
					Tracer.get_engine().insertFeatureMap(db_id, 
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
				}
				map_id = -1;
				temp_id = -1;
				addMode=false;
				//refresh the map
				initMap();
			}else if(removeMode==true){
				for (Entity_Map featureMap : listFeatureMap) {
					if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+20 && 
							(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+20){
						//remove entry
						Tracer.get_engine().removeFeatureMap(featureMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
						removeMode=false;
						//new UpdateThread().execute();
						//refresh the map
						initMap();
					}
				}
				for (Entity_Map switchesMap : listMapSwitches) {
					if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+20 && 
							(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+20){
						//remove entry
						Tracer.get_engine().removeFeatureMap(switchesMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
						removeMode=false;
						//new UpdateThread().execute();
						//refresh the map
						initMap();
					}
				}
			}else if(moveMode==true){
				for (Entity_Map featureMap : listFeatureMap) {
					if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+20 && 
							(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+20){
						//remove entry
						Tracer.get_engine().removeFeatureMap(featureMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
						moveMode=false;
						//new UpdateThread().execute();
						//return to add mode on next click
						//refresh the map
						initMap();
						temp_id = featureMap.getId();
						addMode=true;
					}
				}
				for (Entity_Map switchesMap : listMapSwitches) {
					if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+20 && 
							(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+20){
						//remove entry
						Tracer.get_engine().removeFeatureMap(switchesMap.getId(),
							(int)((event.getX()-value[2])/currentScale), 
							(int)((event.getY()-value[5])/currentScale),
							files.elementAt(currentFile));
						moveMode=false;
						//new UpdateThread().execute();
						//return to add mode on next click
						//refresh the map
						initMap();
						temp_id = switchesMap.getId();
						addMode=true;
					}
				}
			}else{
				//Move to left
				if(pos_X1 - pos_X0 > screen_width/2){
					if(currentFile +1 < files.size()) currentFile++;
					else currentFile=0;
					canvasMap=null;
					canvasWidget=null;
					System.gc();
					//refresh the map
					initMap();
					//Re-init last save position
					pos_X0=0;
					pos_X1=0;
				//Move to right
				}else if(pos_X0 - pos_X1 > screen_width/2){
					if(currentFile != 0) 
						currentFile--;
					else 
						currentFile=files.size()-1;
					canvasMap=null;
					canvasWidget=null;
					System.gc();
					//refresh the map
					initMap();
					//Re-init last save position
					pos_X0=0;
					pos_X1=0;
				//Display widget
				}else{
					//show the normal widget on top , or switch map if a map switch clicked
					boolean widgetActiv=false;
					for (Entity_Map switchesMap : listMapSwitches) {
						if((int)((event.getX()-value[2])/currentScale)>switchesMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<switchesMap.getPosx()+20 && 
								(int)((event.getY()-value[5])/currentScale)>switchesMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<switchesMap.getPosy()+20){
							//That seems to be this switch map widget clicked !
							int new_map = switchesMap.getId() - 9999;
							if(new_map < files.size() && new_map >= 0) {
								currentFile=new_map;
							}
							canvasMap=null;
							canvasWidget=null;
							System.gc();
							initMap();
									
							panel_button.setVisibility(View.GONE);
							panel_widget.setVisibility(View.VISIBLE);
							widgetActiv=true;
							postInvalidate();
							return true;
						}
					}
					widgetActiv=false;
					for (Entity_Map featureMap : listFeatureMap) {
						if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+20 && 
								(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+20){
							try {
								showTopWidget(featureMap);	
							} catch (Exception e) {
								e.printStackTrace();
							}
							panel_button.setVisibility(View.GONE);
							panel_widget.setVisibility(View.VISIBLE);
							if(!top_drawer.isOpen())top_drawer.setOpen(true, true);
							if(bottom_drawer.isOpen())bottom_drawer.setOpen(false, true);
							widgetActiv=true;
						}

					}
					//hide it
					if(!widgetActiv && moves < 5){
						top_drawer.setOpen(false, true);
						bottom_drawer.setOpen(false, true);
					}
				}

			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mat.matrix.getValues(value);
			currentScale*=value[0];
			//Save current zoom scale
			prefEditor=params.edit();
			prefEditor.putFloat("Mapscale", currentScale);
			prefEditor.commit();	//To save it really !
			value[0]=1;
			value[4]=1;
			mat.matrix.setValues(value);
			refreshMap();
			break;
		case MotionEvent.ACTION_MOVE:
			moves++;
			mat.currentScale = currentScale;
			mat.actionMove(nbPointers, event);
			break;
		}
		postInvalidate();
		return true;
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
	
	public float autoscale(int image_width,int image_height) {
		currentScalewidth=(float)screenwidth/(float)image_width;
		currentScaleheight=(float)screenheight/(float)image_height;
		//select witch scale is the best
		if (currentScaleheight<currentScalewidth){
			currentScale=currentScaleheight;	
		} else{
			currentScale=currentScalewidth;	
		}
		//Save current zoom scale
		prefEditor=params.edit();
		prefEditor.putFloat("Mapscale", currentScale);
		prefEditor.commit();	//To save it really !
		return currentScale;
	}
	
	public boolean isAddMode() {
		return addMode;
	}

	public void setAddMode(boolean addMode) {
		this.addMode = addMode;
	}

	public boolean isRemoveMode() {
		return removeMode;
	}

	public void setRemoveMode(boolean removeMode) {
		this.removeMode = removeMode;
	}

	public int getUpdate() {
		return update;
	}

	public void setUpdate(int update) {
		this.update = update;
	}

	public void setParams(SharedPreferences params) {
		this.params = params;
	}

	public void setPanel_widget(ViewGroup panel_widget) {
		this.panel_widget = panel_widget;
	}

	public void setPanel_button(ViewGroup panel_button) {
		this.panel_button = panel_button;
	}

	public void setTopDrawer(Sliding_Drawer top_drawer) {
		this.top_drawer = top_drawer;
	}

	public void setBottomDrawer(Sliding_Drawer bottom_drawer) {
		this.bottom_drawer = bottom_drawer;
	}

	public void setFiles(Vector<String> files) {
		this.files = files;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getCurrentFile() {
		return currentFile;
	}

	public void setCurrentFile(int currentFile) {
		this.currentFile = currentFile;
	}
	
	public boolean isMoveMode() {
		return moveMode;
	}
	
	public void setMoveMode(boolean moveMode) {
		this.moveMode = moveMode;	
	}
	
}
