package org.widgets;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.connect.Rest_com;
import org.json.JSONArray;
import org.json.JSONObject;
import org.widgets.Graphical_Info.UpdateThread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;


public class Graphical_Info_View extends View{

	private int width;
	private int height;
	private Bitmap buffer;
	private Bitmap text;
	private Canvas can;
	private Canvas can2;
	private Vector<Vector<Float>> values;

	private float gridStartX;
	private float gridStartY;
	private float gridStopX;
	private float gridStopY;
	private float gridOffset;
	private float valueOffset;

	private float minf;
	private float maxf;
	private float avgf;

	public int dev_id;
	public String state_key;
	public String url;
	public Thread thread;
	public int period;
	public int update;
	private Handler handler;
	public boolean activate=false;
	public boolean loaded=false;
	private String mytag="";


	public Graphical_Info_View(Context context){
		super(context);
		invalidate();
		values = new Vector<Vector<Float>>();
		activate=true;
		mytag = "Graphical_Info_View";
		
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				invalidate();
			}
		};
	}

	@Override 
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		width = getMeasuredWidth();
		height = getMeasuredHeight();
		gridStartY = height-15;
		gridStopY = 15;
		gridOffset = 15;
		valueOffset = 10;
		
		buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		text = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);		

		can=new Canvas(buffer);
		can2=new Canvas(text);
		
		drawMessage();
		
		try{	
			drawGrid();
			drawValue();
			drawGraph();
		}catch(Exception e){
		}
		if(loaded)canvas.drawBitmap(buffer, 0, 0, new Paint());
		else canvas.drawBitmap(text, 0, 0, new Paint());
	}

	public void drawMessage(){
		Paint paint = new Paint();
		paint.setColor(Color.DKGRAY);	
		paint.setAntiAlias(true);
		paint.setTextSize(15);
		can2.drawText("Loading Data ...", 10, 15, paint);
	}

	public void drawGrid(){
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.GRAY);	
		paint.setStyle(Paint.Style.STROKE);

		can.drawColor(Color.TRANSPARENT);
		can.drawLine(gridStartX, gridStartY, gridStopX, gridStartY, paint);
		can.drawLine(gridStartX, gridStartY, gridStartX, gridStopY, paint);
	}

	public void drawValue(){
		//min - max - avg lines
		float gridSize_values = (gridStartY-gridOffset)-(gridStopY+gridOffset);
		float scale_values = gridSize_values/(maxf-minf);

		DashPathEffect dashPath = new DashPathEffect(new float[]{10,5}, 1);
		Paint paint = new Paint();
		paint.setPathEffect(dashPath);
		paint.setColor(Color.GRAY);
		paint.setStrokeWidth(1);
		can.drawLine(gridStartX, gridStartY-gridOffset , gridStopX, gridStartY-gridOffset, paint);
		can.drawLine(gridStartX, gridStopY+gridOffset , gridStopX, gridStopY+gridOffset, paint);
		paint.setColor(Color.parseColor("#993300"));
		can.drawLine(gridStartX,(gridStartY-gridOffset)-((avgf-minf)*scale_values), gridStopX, (gridStartY-gridOffset)-((avgf-minf)*scale_values), paint);


		//min - max - avg values
		paint.setPathEffect(null);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(10);
		paint.setColor(Color.BLACK);
		can.drawText(minf+"", gridStartX-valueOffset-(Float.toString(minf).length()*5), gridStartY-gridOffset, paint);
		can.drawText(maxf+"", gridStartX-valueOffset-(Float.toString(maxf).length()*5), gridStopY+gridOffset, paint);
		can.drawText(avgf+"", gridStartX-valueOffset-(Float.toString(avgf).length()*5), (gridStartY-gridOffset)-((avgf-minf)*scale_values), paint);

		//temp values
		DashPathEffect dashPath2 = new DashPathEffect(new float[]{3,8}, 1);
		paint.setStyle(Paint.Style.FILL);
		float temp_step = (maxf - minf)/6;
		for(int i=1; i<6;i++){
			paint.setPathEffect(dashPath2);
			paint.setAntiAlias(false);
			paint.setColor(Color.parseColor("#0B909A"));
			can.drawLine(gridStartX,(gridStartY-gridOffset)-((temp_step*i)*scale_values), gridStopX, (gridStartY-gridOffset)-((temp_step*i)*scale_values), paint);
			paint.setPathEffect(null);
			paint.setAntiAlias(true);
			paint.setColor(Color.BLACK);
			can.drawText(minf+temp_step*i+"", gridStopX+5, (gridStartY-gridOffset)-((temp_step*i)*scale_values), paint);
		}
	}

	public void drawGraph(){
		float gridSize_values = (gridStartY-gridOffset)-(gridStopY+gridOffset);
		float scale_values = gridSize_values/(maxf-minf);
		float step = (gridStopX-gridStartX)/(values.size()-1);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);

		for(int i=0; i<values.size();i++){

			if(step > 24){
				paint.setStrokeWidth(1);
				paint.setColor(Color.LTGRAY);
				paint.setAntiAlias(false);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStopY, paint);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStartY-4, paint);

				paint.setAntiAlias(true);
				paint.setStrokeWidth(0);
				paint.setColor(Color.parseColor("#157C9E"));
				can.drawText(values.get(i).get(4).intValue()+"'", gridStartX+(i*step)-8, gridStopY+gridOffset-20, paint);
			}
			else if(step > 8 && (values.get(i).get(4) == 3 || 
					values.get(i).get(4) == 6 ||
					values.get(i).get(4) == 9 || 
					values.get(i).get(4) == 12 || 
					values.get(i).get(4) == 15 || 
					values.get(i).get(4) == 18 || 
					values.get(i).get(4) == 21 ||
					values.get(i).get(4) == 0)){
				paint.setStrokeWidth(1);
				paint.setColor(Color.LTGRAY);
				paint.setAntiAlias(false);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStopY, paint);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStartY-4, paint);

				paint.setAntiAlias(true);
				paint.setStrokeWidth(0);
				paint.setColor(Color.parseColor("#157C9E"));
				can.drawText(values.get(i).get(4).intValue()+"'", gridStartX+(i*step)-8, gridStopY+gridOffset-20, paint);
			}
			else if(step > 4 && (values.get(i).get(4) == 6 ||
					values.get(i).get(4) == 12 || 
					values.get(i).get(4) == 18 || 
					values.get(i).get(4) == 0)){
				paint.setStrokeWidth(1);
				paint.setColor(Color.LTGRAY);
				paint.setAntiAlias(false);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStopY, paint);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStartY-4, paint);

				paint.setAntiAlias(true);
				paint.setStrokeWidth(0);
				paint.setColor(Color.parseColor("#157C9E"));
				can.drawText(values.get(i).get(4).intValue()+"'", gridStartX+(i*step)-8, gridStopY+gridOffset-20, paint);
			}
			else if(step > 2 && (values.get(i).get(4) == 12 || 
					values.get(i).get(4) == 0)){
				paint.setStrokeWidth(1);
				paint.setColor(Color.LTGRAY);
				paint.setAntiAlias(false);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStopY, paint);
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStartY-4, paint);

				paint.setAntiAlias(true);
				paint.setStrokeWidth(0);
				paint.setColor(Color.parseColor("#157C9E"));
				can.drawText(values.get(i).get(4).intValue()+"'", gridStartX+(i*step)-8, gridStopY+gridOffset-20, paint);
			}
			if (values.get(i).get(4) == 0){
				paint.setAntiAlias(false);
				paint.setColor(Color.parseColor("#AEB255"));	
				can.drawLine(gridStartX+(i*step), gridStartY, gridStartX+(i*step), gridStopY, paint);
			}


			if(values.get(i).get(0).intValue() == 0 || values.get(i+1).get(0).intValue() == 0){
				paint.setColor(Color.RED);
			}else{
				paint.setColor(Color.parseColor("#157C9E"));
				paint.setStrokeWidth(4);
				can.drawPoint(gridStartX+(step*(i)), (gridStartY-gridOffset)-((values.get(i).get(5))-minf)*scale_values, paint);
				can.drawPoint(gridStartX+(step*(i+1)),(gridStartY-gridOffset)-((values.get(i+1).get(5))-minf)*scale_values, paint);
			}
			paint.setStrokeWidth(2);
			can.drawLine(gridStartX+(step*(i)), (gridStartY-gridOffset)-((values.get(i).get(5))-minf)*scale_values,gridStartX+(step*(i+1)),(gridStartY-gridOffset)-((values.get(i+1).get(5))-minf)*scale_values, paint);
			if(values.get(i+1).get(4).intValue() == 0){
				paint.setColor(Color.parseColor("#157C9E"));
				can.drawText(values.get(i+1).get(3).intValue()+"/"+values.get(i).get(1).intValue(), gridStartX+((i+1)*step), gridStartY+gridOffset, paint);
			}
		}
	}

	public void updateTimer() {
		
		TimerTask doAsynchronousTask;
		final Timer timer = new Timer();
		doAsynchronousTask = new TimerTask() {

			@Override
			public void run() {
				Runnable myTH = new Runnable() {
				//handler.post(new Runnable() {	//Doume : to avoid Exception on ICS
					public void run() {
						try {
							if(activate){
								new UpdateThread().execute();
							}else{
								Log.e(mytag+"("+dev_id+")","update Timer : Destroy runnable");
								timer.cancel();
								this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}}
				//)
				;
				Log.e(mytag,"TimerTask.run : Queuing Runnable for Device : "+dev_id);
				try {
					handler.post(myTH);		//Doume : to avoid Exception on ICS
					} catch (Exception e) {
						e.printStackTrace();
					}
			} //TimerTask Run method
		}; //TimerTask
		//timer.schedule(doAsynchronousTask, 0, update*10000);
		timer.schedule(doAsynchronousTask, 0, update*1000);	// Fix Doume
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if( ! activate)
				return null;
			loaded=false;
			try {
				avgf=0;
				values.clear();
				long currentTimestamp = System.currentTimeMillis()/1000;
				long lastweekTimestamp = currentTimestamp - 86400*period; 
				Log.e(mytag,"UpdateThread ("+dev_id+") : "+url+"stats/"+dev_id+"/"+state_key+"/from/"+lastweekTimestamp+"/to/"+currentTimestamp+"/interval/hour/selector/avg");

				JSONObject json_GraphValues = Rest_com.connect(url+"stats/"+dev_id+"/"+state_key+"/from/"+lastweekTimestamp+"/to/"+currentTimestamp+"/interval/hour/selector/avg");
				JSONArray itemArray = json_GraphValues.getJSONArray("stats");
				JSONArray valueArray = itemArray.getJSONObject(0).getJSONArray("values");

				minf=(float)valueArray.getJSONArray(0).getDouble(5);
				maxf=(float)valueArray.getJSONArray(0).getDouble(5);
				Log.e(mytag,"UpdateThread ("+dev_id+") : array size "+valueArray.length());

				for (int i =0; i < valueArray.length(); i++){
					Vector<Float> vect = new Vector<Float>();
					for (int j=0; j < 6; j++){
						vect.addElement((float)valueArray.getJSONArray(i).getDouble(j));
					}
					values.add(vect);
					avgf+=valueArray.getJSONArray(i).getDouble(5);
					if(valueArray.getJSONArray(i).getDouble(5) > maxf){  
						maxf = (float)valueArray.getJSONArray(i).getDouble(5);  
					}  
					if(valueArray.getJSONArray(i).getDouble(5) < minf){  
						minf = (float)valueArray.getJSONArray(i).getDouble(5); 
					}

					if(i<valueArray.length()-1){
						if((int)valueArray.getJSONArray(i).getDouble(4)!=23 && ((int)valueArray.getJSONArray(i).getDouble(4)<(int)valueArray.getJSONArray(i+1).getDouble(4))){
							if(((int)valueArray.getJSONArray(i).getDouble(4)+1 != (int)valueArray.getJSONArray(i+1).getDouble(4))){
								for (int k=1; k < (int)valueArray.getJSONArray(i+1).getDouble(4)-(int)valueArray.getJSONArray(i).getDouble(4); k++){
									Vector<Float> vect2 = new Vector<Float>();
									vect2.addElement(0f);
									vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(1)));
									vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(2)));
									vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(3)));
									vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(4))+k);
									vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(5)));
									values.add(vect2);
									avgf+=valueArray.getJSONArray(i).getDouble(5);
								}
							}
						}
						if((int)valueArray.getJSONArray(i).getDouble(4)==23 && (int)valueArray.getJSONArray(i+1).getDouble(4)!= 0){
							for (int k=0; k < (int)valueArray.getJSONArray(i+1).getDouble(4); k++){
								Vector<Float> vect2 = new Vector<Float>();
								vect2.addElement(0f);
								vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(1)));
								vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(2)));
								vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(3))+1);
								vect2.addElement((float)k);
								vect2.addElement((float)(valueArray.getJSONArray(i).getDouble(5)));
								values.add(vect2);
								avgf+=valueArray.getJSONArray(i).getDouble(5);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			avgf=avgf/values.size();

			gridStartX=Float.toString(maxf).length()*7;
			if(Float.toString(minf).length()*7>gridStartX)gridStartX=Float.toString(minf).length()*7;
			if(Float.toString(avgf).length()*7>gridStartX)gridStartX=Float.toString(avgf).length()*7;
			gridStopX=width-gridStartX;
			loaded=true;
			handler.sendEmptyMessage(0);
			return null;
		}
	}


	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}
}
