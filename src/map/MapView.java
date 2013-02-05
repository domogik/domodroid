package map;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import database.DomodroidDB;
import org.json.JSONException;
import activities.Activity_Main;
import org.domogik.domodroid.R;
import activities.Sliding_Drawer;
import widgets.Entity_Map;
import widgets.Graphical_Binary;
import widgets.Graphical_Boolean;
import widgets.Graphical_Info;
import widgets.Graphical_Range;
import widgets.Graphical_Trigger;
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
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import misc.Tracer;
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
	private boolean addMode=false;
	private boolean removeMode=false;

	private int update;
	private static int text_Offset_X = 25;
	private static int text_Offset_Y = 0;
	private int moves;

	public int temp_id;

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
	private Graphical_Info info;
	private Graphical_Boolean bool;

	private Vector<String> files;
	private Entity_Map[] listFeatureMap;
	private int mode;
	private int formatMode;
	private String svg_string;
	private int currentFile = 0;

	private SharedPreferences params;

	private float pos_X0=0;
	private float pos_X1=0;

	private int screen_width;
	private DomodroidDB domodb;
	private Boolean activated; 
	private String mytag="MapView";
	private Boolean locked = false;
	
	public MapView(Activity context) {
		super(context);
		this.context=context;
		activated=true;
		domodb = new DomodroidDB(context);
		domodb.owner="MapView";
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		screen_width = display.getWidth();
	}
	public void  onWindowVisibilityChanged (int visibility) {
		Tracer.i(mytag,"Visibility changed to : "+visibility);
		if(visibility == View.VISIBLE)
			this.activated = true;
		else
			activated=false;
	}
	public void stopThread(){
		activated = false;
	}
	public void startThread(){
		activated = true;
	}
	public void clear_Widgets(){
		String map_name=files.elementAt(currentFile);
		Tracer.i(mytag,"Request to clear all widgets from : "+map_name);
		domodb.cleanFeatureMap(map_name);
		initMap();
		
	}
	public void initMap(){
		Toast.makeText(context, files.elementAt(currentFile).substring(0,files.elementAt(currentFile).lastIndexOf('.')), Toast.LENGTH_SHORT).show();
		
		listFeatureMap = domodb.requestFeatures(files.elementAt(currentFile));

		for (Entity_Map featureMap : listFeatureMap) {
			featureMap.setCurrentState(domodb.requestFeatureState(featureMap.getDevId(), featureMap.getState_key()));
		}
		
		if(files.elementAt(currentFile).substring(files.elementAt(currentFile).lastIndexOf('.')).equals(".svg")){
			formatMode=1;
		}else if(files.elementAt(currentFile).substring(files.elementAt(currentFile).lastIndexOf('.')).equals(".png")){ 
			formatMode=2;
		}else{
			formatMode=0;
		}

		//Saved scale should be use here if exists
		//if (savedScale!=null)
		//currentScale= savedScale;
		currentScale = 1;
		
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

		//Case using a svg file as map
		if(formatMode==1){	
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			svg_string = getFileAsString(f);
			svg = SVGParser.getSVGFromString(svg_string);
			svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
			Picture picture = svg.getPicture();
			map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			canvasMap.drawPicture(picture);
			widget = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
		//Case using a png file as map
		}else if(formatMode==2){
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			Bitmap bitmap = decodeFile(f);
			map = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			canvasMap.drawBitmap(bitmap, 0, 0, paint_map);
			widget = Bitmap.createBitmap((int)(bitmap.getWidth()+200), bitmap.getHeight()+200, Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
		}
		drawWidgets();

		postInvalidate();
	}

	public void refreshMap(){
		canvasMap=null;
		canvasWidget=null;
		if(formatMode==1){
			svg = SVGParser.getScaleSVGFromString(svg_string, (int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale));
			Picture picture = svg.getPicture();
			map = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			canvasMap.drawPicture(picture);	
			widget = Bitmap.createBitmap((int)(svg.getSurfaceWidth()*currentScale), (int)(svg.getSurfaceHeight()*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
		}else if(formatMode==2){
			File f = new File(Environment.getExternalStorageDirectory()+"/domodroid/"+files.elementAt(currentFile)); 
			Bitmap bitmap = decodeFile(f);
			map = Bitmap.createBitmap((int)(bitmap.getWidth()*currentScale), (int)(bitmap.getHeight()*currentScale), Bitmap.Config.ARGB_4444);
			canvasMap = new Canvas(map);
			Matrix matScale = new Matrix();
			matScale.postScale(currentScale, currentScale);
			canvasMap.setMatrix(matScale);
			canvasMap.drawBitmap(bitmap, 0, 0, paint_map);
			widget = Bitmap.createBitmap((int)((bitmap.getWidth()+200)*currentScale), (int)((bitmap.getHeight()+200)*currentScale), Bitmap.Config.ARGB_8888);
			canvasWidget = new Canvas(widget);
		}

		drawWidgets();
		postInvalidate();
	}

	public void drawWidgets(){
		if(locked) {
			return;
		}
		locked=true;
		for (Entity_Map featureMap : listFeatureMap) {

			//set state to 1 if widget is on or high
			//set intstate to select correct icon color
			int intstate = 0;
			//set intstate to 1 to change color
			//Remove the 1 because if temperature is equal to 1°C it put the icon to on 
			//Replace contains by equals to avoid problem if CurrentState comes from a phone number like "highlander" or "Jhon" 
			//if ((featureMap.getCurrentState().contains("high")) || (featureMap.getCurrentState().contains("on"))|| (featureMap.getCurrentState().equals("1"))){
			String states = "";
			if(featureMap != null) {
				states = featureMap.getCurrentState();
			} else {
				Tracer.e("MapView","Wrong feature in featureMap list ! ! ! Abort processing !");
				return;
			}
			//Could not be null because it is set to "" 7 rows up
			//if(states == null)
			//	states = "";
			
			if ((states.equals("high")) || (states.equals("on"))){
				intstate=1;
			}
			//set featuremap.state to 1 so it could select the correct icon in entity_map.get_ressources
			featureMap.setState(intstate);
			
			if(featureMap.isalive()) {
				try {
				drawable = BitmapFactory.decodeResource(getResources(), featureMap.getRessources());
					if(drawable != null) {
						canvasWidget.drawBitmap(drawable, 
								(featureMap.getPosx()*currentScale)-drawable.getWidth()/2, 
								(featureMap.getPosy()*currentScale)-drawable.getWidth()/2, 
								paint_map);
					} else {
						Tracer.e("MapView","No drawable available for object");
						return;
					}
				} catch (Exception e) {
					Tracer.e("MapView","cannot draw object ! ! ! !");
					return;
				}
				//if(listEntity.elementAt(i).getCurrentState()==null)listEntity.elementAt(i).setCurrentState("--");
				
				
				if(featureMap.getValue_type().equals("binary") || featureMap.getValue_type().equals("boolean")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						canvasWidget.drawText(featureMap.getCurrentState().toUpperCase(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
						paint_text.setTextSize(14);
						String label = featureMap.getDescription();
						if(label.length() < 1)
							label = featureMap.getDevice_usage_id();
						//Tracer.i(mytag,"label = "+label);
						canvasWidget.drawText(label, (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y+15, paint_text);
						//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						
					}
				
				} else if(featureMap.getValue_type().equals("number")){
					String value;
					if(featureMap.getState_key().equals("temperature"))value=featureMap.getCurrentState()+"°C";
					else if(featureMap.getState_key().equals("pressure"))value=featureMap.getCurrentState()+"hPa";
					else if(featureMap.getState_key().equals("humidity"))value=featureMap.getCurrentState()+"%";
					else if(featureMap.getState_key().equals("percent"))value=featureMap.getCurrentState()+" %";
					else if(featureMap.getState_key().equals("visibility"))value=featureMap.getCurrentState()+"km";
					else if(featureMap.getState_key().equals("chill"))value=featureMap.getCurrentState()+"°C";
					else if(featureMap.getState_key().equals("speed"))value=featureMap.getCurrentState()+"km/h";
					else if(featureMap.getState_key().equals("drewpoint"))value=featureMap.getCurrentState()+"°C";
					else if(featureMap.getState_key().equals("condition-code") && !featureMap.getCurrentState().equals("--"))value=context.getString(ConditionCode(Integer.parseInt(featureMap.getCurrentState())));
					else value=featureMap.getCurrentState();
	
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
								canvasWidget.drawText(label, (featureMap.getPosx()*currentScale)+text_Offset_X, 
										(featureMap.getPosy()*currentScale)+text_Offset_Y+6, 
										paint_text);
							}
						}
				} else if(featureMap.getValue_type().equals("range")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						canvasWidget.drawText(featureMap.getCurrentState(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
						paint_text.setTextSize(14);
						canvasWidget.drawText(featureMap.getDevice_usage_id(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y+15, paint_text);
						//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
					}

				}else if(featureMap.getValue_type().equals("trigger")){
					for(int j=1;j<5;j++){
						paint_text.setShadowLayer(2*j, 0, 0, Color.BLACK);
						paint_text.setTextSize(16);
						canvasWidget.drawText(featureMap.getName(), (featureMap.getPosx()*currentScale)+text_Offset_X, (featureMap.getPosy()*currentScale)+text_Offset_Y, paint_text);
						//Tracer.e("MapView","Drawing value for "+featureMap.getDescription()+" X = "+featureMap.getPosx()+" Y = "+featureMap.getPosy());
						
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
		
		//add debug option to change label adding its Id
		if (params.getBoolean("DEV",false)==true) label = label+" ("+feature.getDevId()+")";	//neutralized by Doume
		
		if (feature.getValue_type().equals("binary")) {
			onoff = new Graphical_Binary(context,feature.getAddress(),
					label,feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE",300),0);
			onoff.container=(FrameLayout) panel_widget;
			panel_widget.addView(onoff);}
		else if (feature.getValue_type().equals("boolean")) {
			bool = new Graphical_Boolean(context,feature.getAddress(),
					label,feature.getDevId(),feature.getState_key(),feature.getDevice_usage_id(), feature.getDevice_type_id(),params.getInt("UPDATE",300),0);
			bool.container=(FrameLayout) panel_widget;
			panel_widget.addView(bool);}
		else if (feature.getValue_type().equals("range")) {
			variator = new Graphical_Range(context,feature.getAddress(),
					label,feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),params.getInt("UPDATE",300),0);
			variator.container=(FrameLayout) panel_widget;
			panel_widget.addView(variator);}
		else if (feature.getValue_type().equals("trigger")) {
			trigger = new Graphical_Trigger(context,feature.getAddress(),
					label,feature.getDevId(),feature.getState_key(),params.getString("URL","1.1.1.1"),feature.getDevice_usage_id(),feature.getParameters(),feature.getDevice_type_id(),0);
			trigger.container=(FrameLayout) panel_widget;
			panel_widget.addView(trigger);}
		else if (feature.getValue_type().equals("number")) {
			info = new Graphical_Info(context,feature.getDevId(),
					label,
					feature.getState_key(),params.getString("URL","1.1.1.1"),
					feature.getDevice_usage_id(),
					params.getInt("GRAPH",3),
					params.getInt("UPDATE",300),0);
			info.container=(FrameLayout) panel_widget;
			panel_widget.addView(info);}
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

	private Bitmap decodeFile(File f){
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

	
	@Override
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
				//insert in the database feature map the device id, his position and name map. 
				domodb.insertFeatureMap(temp_id, 
						(int)((event.getX()-value[2])/currentScale), 
						(int)((event.getY()-value[5])/currentScale),
						files.elementAt(currentFile));
				addMode=false;
				//refresh the map
				initMap();
			}else if(removeMode==true){
				for (Entity_Map featureMap : listFeatureMap) {
					if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+20 && 
							(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+20){
						//remove entry
						domodb.removeFeatureMap(featureMap.getId(),
						(int)((event.getX()-value[2])/currentScale), 
						(int)((event.getY()-value[5])/currentScale),
						files.elementAt(currentFile));
						//removeMode=false;
						new UpdateThread().execute();
						//refresh the map
						initMap();
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
					if(currentFile != 0) currentFile--;
					else currentFile=files.size()-1;
					canvasMap=null;
					canvasWidget=null;
					System.gc();
					//refresh the map
					initMap();
					//Re-init last save position
					pos_X0=0;
					pos_X1=0;
				}else{
					boolean widgetActiv=false;
					for (Entity_Map featureMap : listFeatureMap) {
						if((int)((event.getX()-value[2])/currentScale)>featureMap.getPosx()-20 && (int)((event.getX()-value[2])/currentScale)<featureMap.getPosx()+20 && 
								(int)((event.getY()-value[5])/currentScale)>featureMap.getPosy()-20 && (int)((event.getY()-value[5])/currentScale)<featureMap.getPosy()+20){
							try {
								showTopWidget(featureMap);	
							} catch (JSONException e) {
								e.printStackTrace();
							}
							panel_button.setVisibility(View.GONE);
							panel_widget.setVisibility(View.VISIBLE);
							if(!top_drawer.isOpen())top_drawer.setOpen(true, true);
							if(bottom_drawer.isOpen())bottom_drawer.setOpen(false, true);
							widgetActiv=true;
						}

					}
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

	
	public void updateTimer() {
		TimerTask doAsynchronousTask;
		final Handler handler = new Handler();
		final Timer timer = new Timer();
		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				Runnable myTH = new Runnable() {
					
				//handler.post(new Runnable() {	//Doume change
					public void run() {
						if(activated){
							//Tracer.i(mytag, "update Timer : Execute UpdateThread");
							try {
								new UpdateThread().execute();
							} catch (Exception e) {
								e.printStackTrace();
							}
							
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
		//timer.schedule(doAsynchronousTask, 0, 3000);
		Tracer.i(mytag, "updateTimer : Arming timer of "+update+" seconds");
		timer.schedule(doAsynchronousTask, 0, update * 1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... p) {
			//Tracer.i(mytag, "UpdateThread call on timer !");
			
			// Added by Doume to correctly release resources when exiting
			
			if(! activated) {
				
				Tracer.i(mytag, "UpdateThread : timer frozen...");
				
			} else {
			
			//////////////
				for (Entity_Map featureMap : listFeatureMap) {
					String state = domodb.requestFeatureState(featureMap.getDevId(), featureMap.getState_key());
					if(state != null) {
						Tracer.i(mytag, "UpdateThread : Refreshing device :"+featureMap.getDevId());
						featureMap.setCurrentState(state);
					} else {
						Tracer.i(mytag, "UpdateThread : device :"+featureMap.getDevId()+" seems to be a zombie !");
						featureMap.setalive(false);
					}
				}
				refreshMap();
			}
			return null;
		}
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

	public static int ConditionCode(int code){
		switch (code){
		case 0: return R.string.info0;
		case 1: return R.string.info1;
		case 2: return R.string.info2;
		case 3: return R.string.info3;
		case 4: return R.string.info4;
		case 5: return R.string.info5;
		case 6: return R.string.info6;
		case 7: return R.string.info7;
		case 8: return R.string.info8;
		case 9: return R.string.info9;
		case 10: return R.string.info10;
		case 11: return R.string.info11;
		case 12: return R.string.info12;
		case 13: return R.string.info13;
		case 14: return R.string.info14;
		case 15: return R.string.info15;
		case 16: return R.string.info16;
		case 17: return R.string.info17;
		case 18: return R.string.info18;
		case 19: return R.string.info19;
		case 20: return R.string.info20;
		case 21: return R.string.info21;
		case 22: return R.string.info22;
		case 23: return R.string.info23;
		case 24: return R.string.info24;
		case 25: return R.string.info25;
		case 26: return R.string.info26;
		case 27: return R.string.info27;
		case 28: return R.string.info28;
		case 29: return R.string.info29;
		case 30: return R.string.info30;
		case 31: return R.string.info31;
		case 32: return R.string.info32;
		case 33: return R.string.info33;
		case 34: return R.string.info34;
		case 35: return R.string.info35;
		case 36: return R.string.info36;
		case 37: return R.string.info37;
		case 38: return R.string.info38;
		case 39: return R.string.info39;
		case 40: return R.string.info40;
		case 41: return R.string.info41;
		case 42: return R.string.info42;
		case 43: return R.string.info43;
		case 44: return R.string.info44;
		case 45: return R.string.info45;
		case 46: return R.string.info46;
		case 47: return R.string.info47;
		case 3200: return R.string.info3200;
		}
		return R.string.info48;
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
}
