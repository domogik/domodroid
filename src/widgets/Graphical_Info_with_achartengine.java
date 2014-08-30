/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 * 
 * SPECIAL
 * Thank's to http://wptrafficanalyzer.in/blog/android-combined-chart-using-achartengine-library/
 * 
 */
package widgets;

import java.lang.Thread.State;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.PanListener;
import org.domogik.domodroid.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rinor.Rest_com;
import org.json.JSONArray;
import org.json.JSONObject;

import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import misc.tracerengine;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class Graphical_Info_with_achartengine extends FrameLayout implements OnLongClickListener, OnClickListener {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout chartContainer;
	
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView value;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private TextView state_key_view;

	private int update;
	private Animation animation;
	private Activity context;
	private Message msg;
	private String name;
	private String mytag="";
	private String url = null;
	private String place_type;
	private int place_id;

	public FrameLayout container = null;
	public FrameLayout myself = null;
	public Boolean with_graph = true;
	private tracerengine Tracer = null;
	private String parameters;
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private GraphicalView mChart;
	private String login;
	private String password;
	private SharedPreferences params;

	private String step="hour";
	private int limit = 6;		// items returned by Rinor on stats arrays when 'hour' average
	private long currentTimestamp = 0;
	private long startTimestamp = 0; 
	private Date time_start=new Date();
	private Date time_end=new Date();
	private Vector<Vector<Float>> values;
	private float minf;
	private float maxf;
	private float avgf;
	private Double real_val;	
	private int period_type = 0;		// 0 = period defined by settings
										// 1 = 1 day
										// 8 = 1 week
										// 30 = 1 month
										// 365 = 1 year
	private int sav_period;
	
	private DisplayMetrics metrics;
	private float size12;
	private float size10;
	private float size5;	
	@SuppressLint("HandlerLeak")
	public Graphical_Info_with_achartengine(tracerengine Trac,Activity context, int id,int dev_id, String name, 
			final String state_key, String url,String usage, int period, int update, 
			int widgetSize, int session_type, final String parameters,int place_id,String place_type, SharedPreferences params) throws JSONException {
		super(context);
		this.Tracer = Trac;
		this.context = context;
		this.dev_id = dev_id;
		this.id = id;
		this.state_key = state_key;
		this.update=update;
		this.name = name;
		this.url = url;
		this.myself = this;
		this.session_type = session_type;
		this.parameters = parameters;
		this.place_id= place_id;
		this.place_type= place_type;
		this.params=params;
		metrics = getResources().getDisplayMetrics();
		//Label Text size according to the screen size
		size12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, metrics);
		size10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics);
		size5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, metrics);

		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		mytag="Graphical_Info_with_achartengine ("+dev_id+")";
		this.setPadding((int)size5, (int)size5, (int)size5, (int)size5);
		Tracer.e(mytag,"New instance for name = "+name+" state_key = "+state_key);
		//panel with border
		background = new LinearLayout(context);
		background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)
			background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//panel with border
		topPan = new LinearLayout(context);
		topPan.setOrientation(LinearLayout.HORIZONTAL);
		topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding((int)size5, (int)size10, (int)size5, (int)size10);
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		Tracer.e(mytag, "Get icone for usage : "+usage);
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		img.setTag("img");
		img.setOnLongClickListener(this);
		img.setOnClickListener(this);


		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);

		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);
		nameDevices.setTag("namedevices");
		nameDevices.setOnLongClickListener(this);
		//nameDevices.setLines(1);

		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(state_key);
		state_key_view.setTextColor(Color.parseColor("#333333"));


		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding((int)size10, 0, (int)size10, 0);
		//value
		value = new TextView(context);
		value.setTextSize(28);
		value.setTextColor(Color.BLACK);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);

		featurePan.addView(value);
		infoPan.addView(nameDevices);
		infoPan.addView(state_key_view);
		imgPan.addView(img);

		topPan.addView(imgPan);
		topPan.addView(infoPan);
		topPan.addView(featurePan);
		background.addView(topPan);
		this.addView(background);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 9999) {
						//Message from widgetupdate
						//state_engine send us a signal to notify value changed
					if(session == null)
						return;
					
					String loc_Value = session.getValue();
					Tracer.d(mytag,"Handler receives a new value <"+loc_Value+">" );
					try {
						float formatedValue = 0;
						if(loc_Value != null)
							formatedValue = Round(Float.parseFloat(loc_Value),2);
						try {
							//Basilic add, number feature has a unit parameter
							JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
							String test_unite = jparam.getString("unit");
							value.setText(formatedValue+ " "+test_unite);
						} catch (JSONException e) {							
							if(state_key.equalsIgnoreCase("temperature") == true) value.setText(formatedValue+" °C");
							else if(state_key.equalsIgnoreCase("pressure") == true) value.setText(formatedValue+" hPa");
							else if(state_key.equalsIgnoreCase("humidity") == true) value.setText(formatedValue+" %");
							else if(state_key.equalsIgnoreCase("percent") == true) value.setText(formatedValue+" %");
							else if(state_key.equalsIgnoreCase("visibility") == true) value.setText(formatedValue+" km");
							else if(state_key.equalsIgnoreCase("chill") == true) value.setText(formatedValue+" °C");
							else if(state_key.equalsIgnoreCase("speed") == true) value.setText(formatedValue+" km/h");
							else if(state_key.equalsIgnoreCase("drewpoint") == true) value.setText(formatedValue+" °C");
							else if(state_key.equalsIgnoreCase("condition-code") == true)
								//Add try catch to avoid other case that make #1794
								try {

									value.setText(Graphics_Manager.Names_conditioncodes(getContext(),(int)formatedValue));
								}catch (Exception e1) {
									value.setText(loc_Value);
								}
							else value.setText(loc_Value);
							}


							value.setAnimation(animation);
					} catch (Exception e) {
						// It's probably a String that could'nt be converted to a float
						Tracer.d(mytag,"Handler exception : new value <"+loc_Value+"> not numeric !" );





						value.setText(loc_Value);

						

					}
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"state engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself
				}
				}
			
		};
		
		//================================================================================
		/*
		 * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
		WidgetUpdate cache_engine = WidgetUpdate.getInstance();
		if(cache_engine != null) {
			session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
			if(Tracer.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
										//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}
			
		}
		//================================================================================
		//updateTimer();	//Don't use anymore cyclic refresh....	

	}


@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			
		}
	}
	
	private void compute_period() {
		long duration = 0; 
		//Calendar cal = Calendar.getInstance(); // The 'now' time
		
		switch(period_type ) {
		case -1 :
			//user requires the 'Prev' period
			period_type=sav_period;
			duration = 86400l * 1000l * period_type;
			if(time_end != null) {
				long new_end = time_end.getTime();
				new_end -= duration;
				time_end.setTime(new_end);
				new_end -= duration;
				time_start.setTime(new_end);
				
			}
			//Tracer.i(mytag,"type prev on "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
			break;
		case 0 :
			//user requires the 'Next' period
			period_type=sav_period;
			duration = 86400l * 1000l * period_type;
			if(time_start != null) {
				long new_start = time_start.getTime();
				new_start += duration;
				time_start.setTime(new_start);
				new_start+= duration;
				time_end.setTime(new_start);
			}
			long new_start = time_start.getTime();
			long new_end = time_end.getTime();
			long now = System.currentTimeMillis();
			if(new_end > now) {
				time_end.setTime(now);
				double new_timestamp = now - duration;
				new_start = (long)new_timestamp;
				time_start.setTime(new_start);
			}
			//Tracer.i(mytag,"type next on "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
			break;
		default :
			//period_type indicates the number of days to graph
			// relative to 'now' date
			duration = 86400l * 1000l * period_type;
			long new_end_time = System.currentTimeMillis();
			time_end.setTime(new_end_time);	//Get actual system time
			new_end_time -= duration;
			time_start.setTime(new_end_time);
			//Tracer.i(mytag,"type = "+period_type+" Begin at :"+sdf.format(time_start)+"  End at : "+sdf.format(time_end));
			break;
		}
		
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

	public void onClick(View arg0) {
		if(with_graph) {
			//Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
			float size=262.5f * context.getResources().getDisplayMetrics().density + 0.5f;
			int sizeint=(int)size;
			if(background.getHeight() != sizeint){
				try {
					background.removeView(chartContainer);
					
				} catch (Exception e) {}
				try {
					
					//background_stats.addView(canvas)
					final String[] mMonth = new String[] {
							"Jan", "Feb" , "Mar", "Apr", "May", "Jun",
							"Jul", "Aug" , "Sep", "Oct", "Nov", "Dec"
						};
					int[] x = { 0,1,2,3,4,5,6,7 };
					int[] income = { 2000,2500,2700,3000,2800,3500,3700,3800};
					int nb_of_value = 20;
					
					period_type = 1;	//by default, display 24 hours
					compute_period();	//To initialize time_start & time_end
					drawgraph();
				} catch (JSONException e) {
					Tracer.d(mytag, "Acharengine failed"+ e.toString());
				}
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,sizeint));
				background.addView(chartContainer);
				
			}
			else{
				background.removeView(chartContainer);
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		return;
	}

	private void drawgraph() throws JSONException {
		values = new Vector<Vector<Float>>();
		chartContainer = new LinearLayout(context);
		currentTimestamp=time_end.getTime()/1000;
		startTimestamp=time_start.getTime()/1000;
		// Creating a XYMultipleSeriesRenderer to customize the whole chart
		final XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
		// Creating XYSeriesRenderer to customize incomeSeries
		XYSeriesRenderer incomeRenderer = new XYSeriesRenderer();
		// Creating a dataset to hold each series
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// Creating an  XYSeries for Income
		XYSeries nameSeries = new XYSeries(name);
			
		//Tracer.i(mytag,"UpdateThread ("+dev_id+") : "+url+"stats/"+dev_id+"/"+state_key+"/from/"+startTimestamp+"/to/"+currentTimestamp+"/interval/"+step+"/selector/avg");
		Tracer.i(mytag,"UpdateThread ("+dev_id+") : "+url+"stats/"+dev_id+"/"+state_key+"/from/"+startTimestamp+"/to/"+currentTimestamp+"/interval/"+step+"/selector/avg");
		JSONObject json_GraphValues = null;
		try {
			//json_GraphValues = Rest_com.connect(url+"stats/"+dev_id+"/"+state_key+"/from/"+startTimestamp+"/to/"+1385857510+"/interval/"+step+"/selector/avg");
			json_GraphValues = Rest_com.connect(url+"stats/"+dev_id+"/"+state_key+"/from/"+startTimestamp+"/to/"+currentTimestamp+"/interval/"+step+"/selector/avg",login,password);
			
		} catch (Exception e) {
			//return null;
			Tracer.e(mytag,"Error with json");
		}
			JSONArray itemArray = json_GraphValues.getJSONArray("stats");
			JSONArray valueArray = itemArray.getJSONObject(0).getJSONArray("values");
		int j=0;
		for (int i =0; i < valueArray.length()-1; i++){
			real_val = valueArray.getJSONArray(i).getDouble(limit-1);
			real_val=round(real_val, 2);
			int year=valueArray.getJSONArray(i).getInt(0);
	    	int month=valueArray.getJSONArray(i).getInt(1);
	    	int week=valueArray.getJSONArray(i).getInt(2);
	    	int day=valueArray.getJSONArray(i).getInt(3);
	    	int hour=valueArray.getJSONArray(i).getInt(4);
	    	int hour_next=valueArray.getJSONArray(i+1).getInt(4);
	    	if (hour != 23 && (hour < hour_next)){
	    		//no day change
	    		if((hour+1) != hour_next) {
					//ruptur : simulate next missing steps
	    			for (int k=1; k < (hour_next - hour); k++){
		    			nameSeries.add(j+k, real_val);
		    		}
	    			j = j + hour_next - hour;
	    		} else{
	    			nameSeries.add(j, real_val); //change to j to avoid missing value
		        	String date=String.valueOf(hour)+"'";
		    		multiRenderer.addXTextLabel(j, date);
		    		j++;
	    		}
	    	}
	    			
			if(minf == 0)
				minf=real_val.floatValue();
			
			avgf+=real_val;	// Get the real 'value'
			
			if(real_val > maxf){  
				maxf = real_val.floatValue();  
			}  
			if(real_val < minf){  
				minf = real_val.floatValue(); 
			}
		}
		avgf=avgf/values.size();
		Tracer.d(mytag,"minf ("+dev_id+")="+minf);
		Tracer.d(mytag,"maxf ("+dev_id+")="+maxf);
		Tracer.d(mytag,"avgf ("+dev_id+")="+avgf);
					
		Tracer.d(mytag,"UpdateThread ("+dev_id+") Refreshing graph");
	
		// Adding nameSeries Series to the dataset
		dataset.addSeries(nameSeries);
		
		incomeRenderer.setColor(0xff006400);
		incomeRenderer.setPointStyle(PointStyle.CIRCLE);
		incomeRenderer.setFillPoints(true);
		incomeRenderer.setLineWidth(2);
		incomeRenderer.setDisplayChartValues(true);
		incomeRenderer.setChartValuesTextSize(size12);
		
		//Remove default X axis label
		multiRenderer.setXLabels(0);
		multiRenderer.setLabelsTextSize(size12);
		
		//Disable zoom button
		multiRenderer.setZoomButtonsVisible(false);
		//get background transparent
		multiRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
		
		// Adding incomeRenderer and expenseRenderer to multipleRenderer
		// Note: The order of adding dataseries to dataset and renderers to multipleRenderer
		// should be same
		multiRenderer.addSeriesRenderer(incomeRenderer);
		
		// Getting a reference to LinearLayout of the MainActivity Layout
		chartContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		chartContainer.setGravity(Gravity.CENTER_VERTICAL);
		//TODO at size
		chartContainer.setPadding((int)size5, (int)size10, (int)size5, (int)size10);
			
		// Specifying chart types to be drawn in the graph
		// Number of data series and number of types should be same
		// Order of data series and chart type will be same
		String[] types = new String[] { LineChart.TYPE };
	
		// Creating a combined chart with the chart types specified in types array
		mChart = (GraphicalView) ChartFactory.getCombinedXYChartView(context, dataset, multiRenderer, types);
			//Disable Zoom in Y axis
			multiRenderer.setZoomEnabled(true, false);
			//Disable Pan in Y axis
			multiRenderer.setPanEnabled(true, false);
			//Limits pan mouvement
			//[panMinimumX, panMaximumX, panMinimumY, panMaximumY] 
			double[] panLimits={-48,24,0,0};
			multiRenderer.setPanLimits(panLimits);
			
			//Sets the selectable radius value around clickable points. 
			multiRenderer.setSelectableBuffer(10);     	
			
			mChart.addPanListener(
				new PanListener() {
					public void panApplied() {
						//compute_period();
						Tracer.i("Pan", "New X range=[" + multiRenderer.getXAxisMin() + ", " + multiRenderer.getXAxisMax()
						+ "], Y range=[" + multiRenderer.getYAxisMax() + ", " + multiRenderer.getYAxisMax() + "]");
					}
				}
			);
			
		// Adding the Combined Chart to the LinearLayout
		chartContainer.addView(mChart);
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}
	
	public boolean onLongClick(View v) {
		if(v.getTag().equals("namedevices")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id,result,"feature");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}else if (v.getTag().equals("img")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					//TODO do this in a menu
					//Tracer.get_engine().remove_one_feature_association(id);
					//Tracer.get_engine().remove_one_feature(id);
					//Tracer.get_engine().remove_one_feature_in_FeatureMap(id);
					removeAllViewsInLayout ();	
					postInvalidate();
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}
		return false;
		
	}


}



