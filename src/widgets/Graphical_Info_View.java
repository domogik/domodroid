package widgets;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import rinor.Rest_com;
import org.json.JSONArray;
import org.json.JSONObject;

import org.domogik.domodroid.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.FrameLayout;


public class Graphical_Info_View extends View implements OnClickListener {

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
	public FrameLayout container = null;
	public View myself = null;
	private Calendar calendar = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	private Date time_start=new Date();
	private Date time_end=new Date();
	private int period_type = 0;		// 0 = period defined by settings
										// 1 = 1 day
										// 8 = 1 week
										// 30 = 1 month
										// 365 = 1 year
	private View Prev = null;
	private View Next = null;
	private Button Year = null;
	private Button Month = null;
	private Button Week = null;
	private Button Day = null;
	
	private String step="hour";
	private int limit = 6;		// items returned by Rinor on stats arrays when 'hour' average
	private long currentTimestamp = 0;
	private long startTimestamp = 0; 
	private   OnClickListener listener = null;
	
	public Graphical_Info_View(Context context){
		super(context);
		invalidate();
		values = new Vector<Vector<Float>>();
		activate=true;
		mytag = "Graphical_Info_View";
		this.myself=this;
		/*
		listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String tag = (String)v.getTag();
				Log.d(mytag,"Click on : "+tag);
			}
			
		};
		if(listener != null) {
			Prev = findViewById(R.id.bt_prev);
			if(Prev != null)
				Prev.setOnClickListener(listener);
			
			Next = findViewById(R.id.bt_prev);
			Next.setOnClickListener(listener);
			Year = (Button)findViewById(R.id.bt_prev);
			Year.setOnClickListener(listener);
			Month = (Button)findViewById(R.id.bt_prev);
			Month.setOnClickListener(listener);
			Week = (Button)findViewById(R.id.bt_prev);
			Week.setOnClickListener(listener);
			Day = (Button)findViewById(R.id.bt_prev);
			Day.setOnClickListener(listener);
		}
		*/
		period_type = 8;	//for tests
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(! activate) {
					Log.d(mytag,"Handler receives a request to die " );
					//That seems to be a zombie
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					invalidate();
					try { finalize(); } catch (Throwable t) {}	//kill the handler thread itself
				} else {
					invalidate();
				}
			}
		};
	}
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String tag = (String)v.getTag();
		Log.d(mytag,"Click on : "+tag);
		if(tag.equals("Prev")) {
			
		} else if(tag.equals("Next")) {
			
		} else if(tag.equals("Next")) {
			
		} else if(tag.equals("Year")) {
			period_type = 365;
		}else if(tag.equals("Month")) {
			period_type = 31;
		}else if(tag.equals("Week")) {
			period_type = 8;
		}else if(tag.equals("Day")) {
			period_type = 1;
		}
		updateTimer();
	}
	
	public void  onWindowVisibilityChanged (int visibility) {
		Log.i(mytag,"Visibility changed to : "+visibility);
		if(visibility == View.VISIBLE)
			this.activate = true;
		else
			activate=false;
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
		if(loaded)
			canvas.drawBitmap(buffer, 0, 0, new Paint());
		else 
			canvas.drawBitmap(text, 0, 0, new Paint());
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
								//Log.i(mytag+"("+dev_id+")","update Timer : Destroy runnable");
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
				Log.i(mytag,"TimerTask.run : Queuing Runnable for Device : "+dev_id);
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
				compute_period(true);
				
				//Log.i(mytag,"UpdateThread ("+dev_id+") : "+url+"stats/"+dev_id+"/"+state_key+"/from/"+startTimestamp+"/to/"+currentTimestamp+"/interval/"+step+"/selector/avg");

				JSONObject json_GraphValues = Rest_com.connect(url+"stats/"+dev_id+"/"+
						state_key+
						"/from/"+
						startTimestamp+
						"/to/"+
						currentTimestamp+
						"/interval/"+step+"/selector/avg");
				//Log.d(mytag,"UpdateThread ("+dev_id+") Rinor result: "+json_GraphValues.toString());
				if(! ((json_GraphValues != null) && (json_GraphValues.getJSONArray("stats") != null))) {
					//That seems to be a zombie
					loaded=false;
					handler.sendEmptyMessage(0);	// To force a close of this instance
					return null;
				}
				JSONArray itemArray = json_GraphValues.getJSONArray("stats");
				JSONArray valueArray = itemArray.getJSONObject(0).getJSONArray("values");

				//minf=(float)valueArray.getJSONArray(0).getDouble(limit-1);
				maxf=minf=0;
				//Log.i(mytag,"UpdateThread ("+dev_id+") : array size "+valueArray.length());

				for (int i =0; i < valueArray.length(); i++){
					// Create a vector with all entry components
					
					Vector<Float> vect = new Vector<Float>();
					Double real_val = valueArray.getJSONArray(i).getDouble(limit-1);	// Get the real 'value'
					
					if(limit == 6) {
						// stats per hour return [ year, month, week, day, hour, value]
						for (int j=0; j < 6; j++){
							vect.addElement((float)valueArray.getJSONArray(i).getDouble(j));
						}
					} else if(limit == 5) {
						// stats per day return [year, month, week, day, value]
						for (int j=0; j < 4; j++){
							vect.addElement((float)valueArray.getJSONArray(i).getDouble(j));
						}
						vect.addElement((float)0f);	//null hour
						vect.addElement(real_val.floatValue());
					} else  {
						// stats per week return [ year,  week, value ]
						Calendar date_value = Calendar.getInstance();
						date_value.setTimeInMillis(0);
						date_value.set(Calendar.YEAR, (int)valueArray.getJSONArray(i).getDouble(0));
						date_value.set(Calendar.WEEK_OF_YEAR, (int)valueArray.getJSONArray(i).getDouble(1));
						Date loc_date = new Date();
						loc_date.setTime(date_value.getTimeInMillis());
						//Log.d(mytag,"Case week : Inserting value at "+sdf.format(loc_date));
						vect.addElement((float)valueArray.getJSONArray(i).getDouble(0));	//year
						vect.addElement((float)loc_date.getMonth());	// month
						vect.addElement((float)valueArray.getJSONArray(i).getDouble(1));	//week
						vect.addElement((float)loc_date.getDay());	// day
						vect.addElement((float)0f);	//null hour
						vect.addElement(real_val.floatValue());
					}
					// each vector contains 6  floats
					values.add(vect);
					if(minf == 0)
						minf=real_val.floatValue();
					
					avgf+=real_val;	// Get the real 'value'
					if(real_val > maxf){  
						maxf = real_val.floatValue();  
					}  
					if(real_val < minf){  
						minf = real_val.floatValue(); 
					}

					if( i < valueArray.length()-1){
						// It's not the last component
						int loc_hour,loc_hour_next = 0;
						int loc_day,loc_day_next = 0;
						int loc_week,loc_week_next = 0;
						int loc_year = 0;
						int loc_month = 0;
						float loc_value = 0;
						
						if(limit == 6) {
							// range between 1 to 8 days (average per hour)
							loc_year = (int)valueArray.getJSONArray(i).getDouble(0);
							loc_month = (int)valueArray.getJSONArray(i).getDouble(1);
							loc_week  = (int)valueArray.getJSONArray(i).getDouble(2);
							loc_day   = (int)valueArray.getJSONArray(i).getDouble(3);
							loc_hour = (int)valueArray.getJSONArray(i).getDouble(4);
							loc_hour_next = (int)valueArray.getJSONArray(i+1).getDouble(4);
							loc_value = (float)valueArray.getJSONArray(i).getDouble(5);
							//Log.d(mytag,"Case hour : "+loc_year+" - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+loc_hour+" - "+loc_value);

							if(loc_hour != 23 && (loc_hour < loc_hour_next) ) {
								//no day change
								//Log.d(mytag,"Case hour 1 ");

								if((loc_hour+1) != loc_hour_next) {
									//ruptur : simulate next missing steps
									for (int k=1; k < (loc_hour_next - loc_hour); k++){
										Vector<Float> vect2 = new Vector<Float>();
										vect2.addElement(0f);
										vect2.addElement((float)loc_month);	//month
										vect2.addElement((float)loc_week);	//week
										vect2.addElement((float)loc_day);	//day
										vect2.addElement((float)loc_hour +k);	//hour
										vect2.addElement(loc_value);	//value
										values.add(vect2);
										//Log.e(mytag,"Case 1 : added : 0 - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+(loc_hour+k)+" - "+loc_value);
										avgf+=loc_value;	//value
									}
								}
							} else {
								//Log.d(mytag,"Case hour 2 ");
								//if((loc_hour ==23) && (loc_hour_next != 0) ){
								if( loc_hour ==23 ){
									// day change : simulate missing steps
									//Log.d(mytag,"Case hour 2a ");

									for (int k=0; k < loc_hour_next ; k++){
										Vector<Float> vect2 = new Vector<Float>();
										vect2.addElement(0f);
										vect2.addElement((float)loc_month+1);	//month
										vect2.addElement((float)loc_week);	//week
										vect2.addElement((float)loc_day+1);	// next day
										vect2.addElement((float)k);			//simulated hour
										vect2.addElement(loc_value);	//value
										values.add(vect2);
										//Log.e(mytag,"Case 2 : added : 0 - "+loc_month+" - "+loc_week+" - "+(loc_day+1)+" - "+k+" - "+loc_value);
										avgf+=loc_value;	//value
									}
								}
							}
						} else if(limit == 5) {
							// range between 9 to 32 days (average per day)
							loc_day_next = (int)valueArray.getJSONArray(i+1).getDouble(limit-2);
							loc_year = (int)valueArray.getJSONArray(i).getDouble(0);
							loc_month = (int)valueArray.getJSONArray(i).getDouble(1);
							loc_week  = (int)valueArray.getJSONArray(i).getDouble(2);
							loc_day   = (int)valueArray.getJSONArray(i).getDouble(3);
							loc_day_next = (int)valueArray.getJSONArray(i+1).getDouble(3);
							loc_hour = 0;
							loc_value = (float)valueArray.getJSONArray(i).getDouble(4);
							//Log.d(mytag,"Case day : "+loc_year+" - "+loc_month+" - "+loc_week+" - "+loc_day+" - "+loc_value);

							//if(loc_day != 31 && (loc_day < loc_day_next) ) {
							if(loc_day < loc_day_next ) {
								// month continues...
								//Log.d(mytag,"Case day 1 ");

								if((loc_day+1) != loc_day_next){
									// but a hole exists : simulate missing days
									//Log.d(mytag,"Case day 1a ");
									for (int k=1; k < (loc_day_next - loc_day); k++){
										Vector<Float> vect2 = new Vector<Float>();
										vect2.addElement(0f);
										vect2.addElement((float)loc_month);	//month
										vect2.addElement((float)loc_week);	//week
										vect2.addElement((float)(loc_day+k));	//day
										vect2.addElement((float)loc_hour); // = 0
										vect2.addElement(loc_value);	//value
										values.add(vect2);
										avgf+=loc_value;	//value
									}
								}
							} else {
								// next day being less than current day, month has changed !
								//Log.d(mytag,"Case day 2 ");
								
								//if((loc_day == 31) && (loc_day_next != 0) ){
								if( loc_day_next != 0 ){
									//Log.d(mytag,"Case day 2a ");
									
									for (int k=0; k < loc_day_next ; k++){
										Vector<Float> vect2 = new Vector<Float>();
										vect2.addElement(0f);
										vect2.addElement((float)loc_month);	//month
										vect2.addElement((float)loc_week);	//week
										//vect2.addElement(0f);				// no week in scale
										vect2.addElement((float)k);			//day
										vect2.addElement((float)loc_hour); // = 0
										vect2.addElement(loc_value);		//value
										values.add(vect2);
										//Log.d(mytag,"Case day 2a with k="+k);
										
										avgf+=loc_value;	//value
									}
								}
							}
						} else if(limit == 3) {
							Calendar date_value = Calendar.getInstance();
							date_value.setTimeInMillis(0);
							date_value.set(Calendar.YEAR, (int)valueArray.getJSONArray(i).getDouble(0));
							date_value.set(Calendar.WEEK_OF_YEAR, (int)valueArray.getJSONArray(i).getDouble(1));
							Date loc_date = new Date();
							loc_date.setTime(date_value.getTimeInMillis());
							//loc_date.setYear((int)valueArray.getJSONArray(i).getDouble(0));
							//loc_date.setMonth(date_value.get(Calendar.MONTH));
							//loc_date.setDate(date_value.get(Calendar.DAY_OF_MONTH));
							//Log.d(mytag,"Case week : process :"+sdf.format(loc_date));
							// range of 1 year (average per week)
							loc_year = (int)valueArray.getJSONArray(i).getDouble(0);
							loc_month=loc_date.getMonth();	// month
							loc_week  = (int)valueArray.getJSONArray(i).getDouble(1);
							loc_week_next = (int)valueArray.getJSONArray(i+1).getDouble(1);
							loc_day   = loc_date.getDate();
							loc_day_next = 0;
							loc_hour = loc_date.getHours();
							loc_value = (float)valueArray.getJSONArray(i).getDouble(2);
							//Log.d(mytag,"Case week : "+loc_year+" - "+loc_week+" - "+loc_value);

							if(loc_week != 52 && (loc_week < loc_week_next) ) {
								if((loc_week+1) != loc_week_next){
									// Changing year
									for (int k=1; k < (loc_week_next - loc_week); k++){
										Vector<Float> vect2 = new Vector<Float>();
										//vect2.addElement((float)loc_year);	//year
										vect2.addElement(0f);
										vect2.addElement((float)loc_month);	//month
										vect2.addElement((float)(loc_week+k));	//week
										vect2.addElement((float)loc_day);	//day
										vect2.addElement(0f);	//hour
										vect2.addElement((float)loc_value);	//value
										values.add(vect2);
										avgf+=loc_value;	//value
									}
								}
							}
							if((loc_week == 52) && (loc_week_next != 0) ){
								//last week of the year
								for (int k=0; k < loc_week_next ; k++){
									Vector<Float> vect2 = new Vector<Float>();
									//vect2.addElement((float)loc_year);	//year
									vect2.addElement(0f);
									vect2.addElement((float)loc_month);	//month
									vect2.addElement((float)k);	//week
									vect2.addElement((float)loc_day);	//day
									vect2.addElement(0f);	//hour
									vect2.addElement((float)loc_value);	//value
									values.add(vect2);
									avgf+=valueArray.getJSONArray(i).getDouble(limit-1);	//value
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			avgf=avgf/values.size();

			gridStartX=Float.toString(maxf).length()*7;
			if(Float.toString(minf).length()*7 > gridStartX)
				gridStartX=Float.toString(minf).length()*7;
			if(Float.toString(avgf).length()*7 > gridStartX)
				gridStartX=Float.toString(avgf).length()*7;
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
	
	
	
	private void compute_period(Boolean mode) {
		// if mode == true, graph should end on actual time
		//	otherwise, use current time stamps (Next & Previous)
		
		long duration = 86400 * 1000 * period;	//By default, get the settings's graph duration, in days;
		/*
		period_type = 0;		// 0 = period defined by settings
		// 1 = 1 day
		// 8 = 1 week
		// 30 = 1 month
		// 365 = 1 year
		 * */
		Calendar cal = Calendar.getInstance();
		if( period_type != 0) {
			duration = 86400 * 1000 * period_type;
		}
		if(mode) {
			time_end = cal.getTime();	//Get actual system time
		} else {
			cal.setTime(time_end);		// keep current end_time, for previous/next
		}
		cal.add(Calendar.DATE, - period_type);
		time_start = cal.getTime();
		//Log.d(mytag,"Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
		currentTimestamp=time_end.getTime()/1000;
		startTimestamp=time_start.getTime()/1000;
		if(period_type < 9) {
			step="hour";
			limit=6;
		} else if(period_type < 32) {
			step="day";
			limit=5;
		} else {
			step="week";
			limit=3;
		}
	}
	
}
