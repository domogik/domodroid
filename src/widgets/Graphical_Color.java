package widgets;

import java.util.Timer;
import java.util.TimerTask;

import misc.Color_Progress;
import misc.Color_RGBField;
import misc.Color_Result;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;
import org.json.JSONObject;

import rinor.Rest_com;
import database.DomodroidDB;
import database.JSONParser;
import database.WidgetUpdate;

import widgets.Graphical_Binary.SBAnim;
import widgets.Graphical_Binary.UpdateThread;
import widgets.Graphical_Range.CommandeThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.Tracer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Graphical_Color extends FrameLayout implements OnSeekBarChangeListener,  OnTouchListener, OnLongClickListener{


	private int mInitialColor, mDefaultColor;
	private String mKey;

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout color_LeftPan;
	private LinearLayout color_RightPan;
	private LinearLayout featurePan;
	private LinearLayout featurePan2;
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView value;
	private int dev_id;
	private Handler handler;
	private String state_key;
	private Color_Progress seekBarHueBar;
	private Color_Progress seekBarPowerBar;
	private Color_Progress seekBarRGBXBar;
	private Color_Progress seekBarRGBYBar;
	private Color_RGBField rgbView;
	private Color_Result resultView;
	private TextView state_key_view;
	private String url;
	private int update;
	private Animation animation;
	private boolean touching;
	private int updating=0;
	private int state_progress;
	
	private int argb = 0;
	private String argbS = "";
	private Message msg;
	private String mytag = "";
	private String name;
	private String wname;
	private String type;
	private String address;
	private DomodroidDB domodb;
	private Activity mycontext;
	public WidgetUpdate updateEngine = null;
	private FrameLayout myself = null;
	private Boolean switch_state = false;
	private Boolean request_in_process = false;
	private TimerTask doAsynchronousTask;
	
	
	public boolean activate=false;
	private int widgetSize;
	private Color currentColor;
	private SeekBar seekBarOnOff;
	private int[] mMainColors = new int[65536];
	public float mCurrentHue = 0;
	public int rgbHue = 0;
	public int rgbX = 0;
	public int rgbY = 0;
	
	private TextView title7;
	private TextView title8;
	private TextView title9;
	private String t7s,t8s,t9s = "";
	private SharedPreferences params;




	public Graphical_Color(Context context, 
			SharedPreferences params, 
			int dev_id, 
			String name,
			String model_id,
			String address,
			final String state_key, 
			String url,
			String usage, 
			int update, 
			int widgetSize) {
		
		super(context);
		mycontext = (Activity) context;
		this.dev_id = dev_id;
		this.state_key = state_key;
		this.name=name;
		this.wname=name;
		this.address=address;
		this.url = url;
		this.update=update;
		this.widgetSize=widgetSize;
		this.setPadding(5, 5, 5, 5);
		this.params = params;
		this.myself = this;
		domodb = new DomodroidDB(mycontext);
		domodb.owner="Graphical_Color("+dev_id+")";
		mytag = domodb.owner;
		
		String[] model = model_id.split("\\.");
		type = model[0];
		Tracer.d(mytag,"model_id = <"+model_id+"> type = <"+type+">" );
		
		//panel with border
		background = new LinearLayout(context);
		background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//panel with border
		topPan = new LinearLayout(context);
		topPan.setOrientation(LinearLayout.HORIZONTAL);
		topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 5, 10);
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		img.setOnTouchListener(this);
		
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
		featurePan.setPadding(10, 0, 10, 0);

		//first seekbar on/off
		seekBarOnOff=new SeekBar(context);
		seekBarOnOff.setProgress(0);
		seekBarOnOff.setMax(100);
		Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bgseekbaronoff);
		seekBarOnOff.setLayoutParams(new LayoutParams(bMap.getWidth(),bMap.getHeight()));
		seekBarOnOff.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbaronoff));
		seekBarOnOff.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
		seekBarOnOff.setThumbOffset(0);
		seekBarOnOff.setOnSeekBarChangeListener(this);
		seekBarOnOff.setTag("onoff");

		//feature panel 2
		featurePan2=new LinearLayout(context);
		featurePan2.setOrientation(LinearLayout.HORIZONTAL);
		//featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		featurePan2.setGravity(Gravity.CENTER_VERTICAL);
		featurePan2.setPadding(20, 0, 0, 10);

		//left panel
		color_LeftPan = new LinearLayout(context);
		color_LeftPan.setOrientation(LinearLayout.VERTICAL);
		//color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));
		color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1));
		color_LeftPan.setPadding(0, 0, 0, 10);
		
		TextView title1 = new TextView(context);
		title1.setText(context.getString(R.string.Hue));
		title1.setTextSize(10);
		title1.setTextColor(Color.parseColor("#333333"));
		TextView title2 = new TextView(context);
		title2.setText(context.getString(R.string.Sat));
		title2.setTextSize(10);
		title2.setTextColor(Color.parseColor("#333333"));
		TextView title3 = new TextView(context);
		title3.setText(context.getString(R.string.Bright));
		title3.setTextSize(10);
		title3.setTextColor(Color.parseColor("#333333"));
		//TextView title4 = new TextView(context);
		//title4.setText("Luminosity");
		//title4.setTextSize(10);
		//title4.setTextColor(Color.parseColor("#333333"));
		TextView title5 = new TextView(context);
		title5.setText(context.getString(R.string.Field));
		title5.setTextSize(10);
		title5.setTextColor(Color.parseColor("#333333"));
		TextView title6 = new TextView(context);
		title6.setText(context.getString(R.string.Curcolor));
		title6.setTextSize(10);
		title6.setTextColor(Color.parseColor("#333333"));
		title7 = new TextView(context);
		t7s = context.getString(R.string.Red);
		title7.setText(t7s+" : 255");
		title7.setTextSize(10);
		title7.setTextColor(Color.parseColor("#333333"));
		title8 = new TextView(context);
		t8s = context.getString(R.string.Green);
		title8.setText(t8s+" : 0");
		title8.setTextSize(10);
		title8.setTextColor(Color.parseColor("#333333"));
		title9 = new TextView(context);
		t9s = context.getString(R.string.Blue);
		title9.setText(t9s+" : 0");
		title9.setTextSize(10);
		title9.setTextColor(Color.parseColor("#333333"));

		
		//seekbar huebar
		seekBarHueBar=new Color_Progress(context,0,0);
		seekBarHueBar.setProgress(0);
		seekBarHueBar.setMax(255);
		seekBarHueBar.setProgressDrawable(null);
		seekBarHueBar.setOnSeekBarChangeListener(this);
		seekBarHueBar.setTag("hue");

		//seekbar rgbbarX
		seekBarRGBXBar=new Color_Progress(context,1,0);
		seekBarRGBXBar.setProgress(0);
		seekBarRGBXBar.setMax(255);
		seekBarRGBXBar.setProgressDrawable(null);
		seekBarRGBXBar.setOnSeekBarChangeListener(this);
		seekBarRGBXBar.setTag("rgbx");

		//seekbar rgbbarY
		seekBarRGBYBar=new Color_Progress(context,2,0);
		seekBarRGBYBar.setProgress(0);
		seekBarRGBYBar.setMax(255);
		seekBarRGBYBar.setProgressDrawable(null);
		seekBarRGBYBar.setOnSeekBarChangeListener(this);
		seekBarRGBYBar.setTag("rgby");

		//seekbar powerbar
		seekBarPowerBar=new Color_Progress(context,3,0);
		seekBarPowerBar.setProgress(0);
		seekBarPowerBar.setMax(255);
		seekBarPowerBar.setProgressDrawable(null);
		seekBarPowerBar.setOnSeekBarChangeListener(this);
		seekBarPowerBar.setTag("power");


		//RGBField
		rgbView = new Color_RGBField(getContext(), Color.RED, Color.RED);
		//rgbView.drawRGBField();

		//right panel
		color_RightPan = new LinearLayout(context);
		color_RightPan.setOrientation(LinearLayout.VERTICAL);
		color_RightPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.RIGHT));
		color_RightPan.setPadding(20, 0, 0, 10);

		//Color result
		resultView = new Color_Result(context);

		featurePan.addView(seekBarOnOff);
		infoPan.addView(nameDevices);
		infoPan.addView(state_key_view);
		imgPan.addView(img);

		color_LeftPan.addView(title1);
		color_LeftPan.addView(seekBarHueBar);
		color_LeftPan.addView(title2);
		color_LeftPan.addView(seekBarRGBXBar);
		color_LeftPan.addView(title3);
		color_LeftPan.addView(seekBarRGBYBar);
		//color_LeftPan.addView(title4);
		//color_LeftPan.addView(seekBarPowerBar);
		color_LeftPan.addView(title5);
		color_LeftPan.addView(rgbView);

		color_RightPan.addView(title6);
		color_RightPan.addView(resultView);
		color_RightPan.addView(title7);
		color_RightPan.addView(title8);
		color_RightPan.addView(title9);


		featurePan2.addView(color_LeftPan);
		featurePan2.addView(color_RightPan);
		featurePan2.setVisibility(INVISIBLE);

		topPan.addView(imgPan);
		topPan.addView(infoPan);
		topPan.addView(featurePan);

		background.addView(topPan);
		
		this.addView(background);
		
		//LoadSelections();
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(activate) {
					Tracer.d(mytag,"Handler receives a request to die " );
					//That seems to be a zombie
					removeView(background);
					
					myself.setVisibility(GONE);
					
					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself
					
				} else {
					try {
						Bundle bu = msg.getData();
						if(( bu != null) && (bu.getString("message") != null) ) {
							argbS =  bu.getString("message");
							if(argbS.equals("off")) {
								switch_state=false;
								argbS="000000";
								argb=0;
							} else if(argbS.equals("on")) {
								seekBarOnOff.setProgress(100);
								switch_state=true;
								LoadSelections();	//Recall last values known from shared preferences
													// argb and argbS will be set when seekBars will be changed
								return;
								
							} else {
								if(request_in_process) {
									request_in_process = false;
									return;		//When a 'off' command has been sent, ignore this update
												// to don't display old value present in database
												// let it time to refresh from server
								}
								argbS = argbS.substring(1);	//It's the form #RRGGBB : ignore the #
								try {
									argb = Integer.parseInt(argbS,16);
								} catch (Exception e) {
									argb = 1;
								}
							}
							
							if (argb == 0){
								seekBarOnOff.setProgress(0);
								switch_state=false;
								
								
							}else {
								seekBarOnOff.setProgress(100);
								switch_state=true;
								
							}
							//Convert RGB to HSV color, and set sliders
							float hsv[] = new float[3];
							int r, g , b;
							r=((argb>>16)&0xFF);
							g=((argb>>8)&0xFF);
							b=((argb)&0xFF);
							Color.colorToHSV(argb, hsv);
							//Tracer.d(mytag,"Handler ==> RGB values after process = <"+r+"> <"+g+"> <"+b+">" );
							//Tracer.d(mytag,"Handler ==> HSV values after process = <"+hsv[0]+"> <"+hsv[1]+"> <"+hsv[2]+">" );
							
							//Seekbars are in range 0-255 : convert HSV values
							//Hue is an angle : convert it to linear
							seekBarHueBar.setProgress((int) ( 255f -(hsv[0]*255f/360)) );
							seekBarRGBXBar.setProgress((int) (hsv[1]*255f));
							seekBarRGBYBar.setProgress((int) (hsv[2]*255f));
							
							title7.setText(t7s+" : "+r);
							title8.setText(t8s+" : "+g);
							title9.setText(t9s+" : "+b);
							if( (r != 0) || (g != 0) || (b != 0)) {
								SaveSelections();
							}
						} else {
							if(msg.what == 2) {
								Toast.makeText(getContext(), "Command to server rejected", Toast.LENGTH_SHORT).show();
							}else if(msg.what == 3) {
								//Request to refresh screen now....
								if(updateEngine != null) {
									Tracer.d(mytag,"Handler ==> Request to refresh DB values" );
									request_in_process=true;
									updateEngine.refreshNow();	//Force engine to reload states from server
								}
								
							}
						}
						
					} catch (Exception e) {
						Tracer.e(mytag, "Handler error for device "+wname);
						e.printStackTrace();
					}
				}
			}
		};	
		
		//new UpdateThread().execute();
		updating = 0;
		updateTimer();	
	}


	
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		if(arg0.getTag().equals("onoff")) {
			//User is moving on/off object....
		} else	if(arg0.getTag().equals("hue")){				
			//rgb view
			mCurrentHue = (255-arg0.getProgress())*360/255;
			rgbView.mCurrentHue = mCurrentHue;
			rgbView.invalidate();
			
			//rgb X
			float[] hsv0 = {0,0,(float)rgbY/255f};
			float[] hsv1 = {mCurrentHue,1,(float)rgbY/255f};
			seekBarRGBXBar.hsv0 = hsv0;
			seekBarRGBXBar.hsv1 = hsv1;
			seekBarRGBXBar.invalidate();
			
			//rgb Y
			float[] hsv2 = {0,0,0};
			float[] hsv3 = {mCurrentHue,(float)rgbX/255f,1};
			seekBarRGBYBar.hsv2 = hsv2;
			seekBarRGBYBar.hsv3 = hsv3;
			seekBarRGBYBar.invalidate();
			
		} else if(arg0.getTag().equals("rgbx")){
			rgbX = arg0.getProgress();
			float[] hsv2 = {0,0,0};
			float[] hsv3 = {mCurrentHue,(float)rgbX/255f,1};
			seekBarRGBYBar.hsv2 = hsv2;
			seekBarRGBYBar.hsv3 = hsv3;
			seekBarRGBYBar.invalidate();
			
			rgbView.mCurrentX = arg0.getProgress();
			seekBarRGBYBar.invalidate();
			rgbView.invalidate();

		}else if(arg0.getTag().equals("rgby")){
			rgbY = arg0.getProgress();
			float[] hsv0 = {0,0,(float)rgbY/255f};
			float[] hsv1 = {mCurrentHue,1,(float)rgbY/255f};
			seekBarRGBXBar.hsv0 = hsv0;
			seekBarRGBXBar.hsv1 = hsv1;
			rgbView.mCurrentY = 255-arg0.getProgress();
			seekBarRGBXBar.invalidate();
			rgbView.invalidate();
		}
		
		float[] hsvCurrent = {mCurrentHue,(float)rgbX/255f,(float)rgbY/255f};
		argb = Color.HSVToColor(hsvCurrent);
		resultView.hsvCurrent = hsvCurrent;
		argbS = Integer.toHexString((argb>>16)&0xFF)+Integer.toHexString((argb>>8)&0xFF)+Integer.toHexString((argb)&0xFF);
		title7.setText(t7s+" : "+((argb>>16)&0xFF));
		title8.setText(t8s+" : "+((argb>>8)&0xFF));
		title9.setText(t9s+" : "+((argb)&0xFF));
		resultView.invalidate();
	}
	

	
	public void onStartTrackingTouch(SeekBar seekBar) {
		touching=true;
		updating=3;
	}


	
	public void onStopTrackingTouch(SeekBar seekBar) {
		String tag = (String )seekBar.getTag();
		if(tag.equals("onoff")) {
			if(seekBar.getProgress()<20){
				seekBar.setProgress(0);
				switch_state=false;
				Tracer.i("Graphical_Color","Change switch to OFF" );
			}else{
				seekBar.setProgress(100);
				switch_state=true;
				LoadSelections();
				Tracer.i("Graphical_Color","Change switch to ON" );
			}
			new CommandeThread().execute();		//And send switch_state to Domogik
			
		} else {
			state_progress = seekBar.getProgress();
			SaveSelections();
			Tracer.i("Graphical_Color","End of change : new rgb value =  #"+argbS );
			if(seekBarOnOff.getProgress() > 50)
				if(switch_state)
					new CommandeThread().execute();		//send new color
		}
		touching=false;
		
	}
	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			String Url2send = url+"command/"+type+"/"+address+"/setcolor/";
			
			if( ( argb != 0) && switch_state) {
				
				String srgb = Integer.toHexString(argb);
				if(srgb.length() > 6)
					srgb = srgb.substring(2);
				Url2send+="#"+srgb;
			} else {
				String State="";
				if(switch_state) {
					State="on";
					
				} else {
					State="off";
					seekBarHueBar.setProgress(255);
					seekBarRGBXBar.setProgress(0);
					seekBarRGBYBar.setProgress(0);
				}
				Url2send+=State;
			}
				
			updating=1;
			
			Tracer.i("Graphical_Color","Sending to Rinor : <"+Url2send+">");
			request_in_process = true;
			
			JSONObject json_Ack = Rest_com.connect(Url2send);
			Boolean ack = false;
			if(json_Ack != null) {
				try {
					//@SuppressWarnings("unused")
					ack = JSONParser.Ack(json_Ack);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(ack==false){
					Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
					handler.sendEmptyMessage(2);
				} else {
					// TODO Try to refresh immediatly screen....
					updating=0;
					handler.sendEmptyMessage(3);
				}
			}
			updating=0;
			return null;
		}
	}
	/*
	 * Saving HSV parameters allow to restore them when Domogik server 
	 * only notify 'on' state (without any RGB parameters)
	 * We've to know which was the last one, kept by Domogik
	 */
	private void SaveSelections() {
		SharedPreferences.Editor prefEditor=params.edit();
		prefEditor.putInt("COLORHUE",seekBarHueBar.getProgress());
		prefEditor.putInt("COLORSATURATION",seekBarRGBXBar.getProgress());
		prefEditor.putInt("COLORBRIGHTNESS",seekBarRGBYBar.getProgress());
		prefEditor.commit();
		/*
		Tracer.i("Graphical_Color", "SaveSelections()");
		Tracer.i("Graphical_Color","Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i("Graphical_Color","Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i("Graphical_Color","Bright = "+params.getInt("COLORBRIGHTNESS",0));
		*/
	}

	private void LoadSelections() {
		seekBarHueBar.setProgress(params.getInt("COLORHUE",0));
		seekBarRGBXBar.setProgress(params.getInt("COLORSATURATION",255));
		seekBarRGBYBar.setProgress(params.getInt("COLORBRIGHTNESS",255));
		/*
		Tracer.i("Graphical_Color", "LoadSelections()");
		Tracer.i("Graphical_Color","Hue    = "+params.getInt("COLORHUE",0));
		Tracer.i("Graphical_Color","Sat    = "+params.getInt("COLORSATURATION",0));
		Tracer.i("Graphical_Color","Bright = "+params.getInt("COLORBRIGHTNESS",0));
		*/
	}
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Tracer.i("Graphical_Color", "Touch....");
		if(featurePan2.getVisibility()== INVISIBLE){
			background.addView(featurePan2);
			featurePan2.setVisibility(VISIBLE);
			Tracer.i("Graphical_Color", "FeaturePan2 set to VISIBLE");
		}
		else{
			background.removeView(featurePan2);
			featurePan2.setVisibility(INVISIBLE);
			Tracer.i("Graphical_Color", "FeaturePan2 set to INVISIBLE");
		}
		return false;
	}
	
	public void updateTimer() {
		final Timer timer = new Timer();
		
		doAsynchronousTask = new TimerTask() {

			@SuppressWarnings("unused")
			@Override
			public void run() {
				Runnable myTH = null;
				Handler loc_handler = handler;
				//Tracer.e(mytag, "Create Runnable");
				myTH = new Runnable() {
					public void run() {
						
					try {
							if(getWindowVisibility()==0){
								//Tracer.e(mytag, "Execute UpdateThread");
								new UpdateThread().execute();
								
							}else{
								if(timer != null) {
									timer.cancel();
								}
								Tracer.e(mytag, "UpdateTimer : Destroy runnable");
								//this.finalize();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					} // Runnable run method
				}; //Runnable 
				if((myTH != null) && (loc_handler != null)) {
					//Tracer.e(mytag,"TimerTask.run : Queuing Runnable for Device : "+dev_id);	
					try {
						loc_handler.post(myTH);
					} catch (Exception e) {
						Tracer.e(mytag,"TimerTask.run : Cannot post refresh for Device : "+dev_id+" Widget will not be refreshed ! ! !");	
						e.printStackTrace();
					}
				} else {
					Tracer.e(mytag,"TimerTask.run : Cannot create Runnable for Device : "+dev_id+" Widget will not be refreshed ! ! !");	
				}
			} // TimerTask run method
		}; //TimerTask 
		Tracer.e(mytag,"Init timer for Device : "+this.dev_id);
		doAsynchronousTask.run();		//For an immediate update
		timer.schedule(doAsynchronousTask, 0, update*1000);
	}

	public class UpdateThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try{
				//Tracer.e(mytag, "UpdateThread for device "+dev_id+" "+state_key+" description= "+wname);
				if(updating<1){
					Bundle b = new Bundle();
					String result = domodb.requestFeatureState(dev_id, state_key);
					if(result != null) {
						Tracer.d(mytag, "UpdateThread for device "+dev_id+" "+state_key+" description= "+wname+" Value = "+result);
						b.putString("message", result);
						msg = new Message();
						msg.setData(b);
						handler.sendMessage(msg);
					} else {
						Tracer.e(mytag, "UpdateThread no DB state for "+dev_id+" "+state_key+" description= "+wname+" (No value!)");
					}
				}
				updating--;
			}catch(Exception e){
				Tracer.e(mytag, "error : request feature state= "+wname);
			}
			return null;
		}
	}
	public boolean onLongClick(View arg0) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setTitle(R.string.Rename_title);
		alert.setMessage(R.string.Rename_message);
		// Set an EditText view to get user input 
		final EditText input = new EditText(getContext());
			alert.setView(input);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.e("Graphical_Color", "Name set to: "+result);
					domodb.updateFeaturename(dev_id,result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Tracer.e("Graphical_Color", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}
}
