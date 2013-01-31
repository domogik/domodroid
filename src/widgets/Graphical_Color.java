package widgets;

import misc.Color_Progress;
import misc.Color_RGBField;
import misc.Color_Result;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;
import org.json.JSONObject;

import rinor.Rest_com;
import database.JSONParser;

import widgets.Graphical_Range.CommandeThread;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Graphical_Color extends FrameLayout implements OnSeekBarChangeListener,  OnTouchListener{


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
	
	private SharedPreferences params;




	public Graphical_Color(Context context, 
			SharedPreferences params, 
			int dev_id, 
			String name, 
			final String state_key, 
			String url,
			String usage, 
			int update, 
			int widgetSize) {
		
		super(context);
		this.dev_id = dev_id;
		this.state_key = state_key;
		this.url = url;
		this.update=update;
		this.widgetSize=widgetSize;
		this.setPadding(5, 5, 5, 5);
		this.params = params;


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


		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);
		infoPan.setOnTouchListener(this);
		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name+" ("+dev_id+")");
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);
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
		seekBarOnOff.setTag("0");

		//feature panel 2
		featurePan2=new LinearLayout(context);
		featurePan2.setOrientation(LinearLayout.HORIZONTAL);
		featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		featurePan2.setGravity(Gravity.CENTER_VERTICAL);
		featurePan2.setPadding(20, 0, 0, 0);

		//left panel
		color_LeftPan = new LinearLayout(context);
		color_LeftPan.setOrientation(LinearLayout.VERTICAL);
		color_LeftPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1));

		TextView title1 = new TextView(context);
		title1.setText("RGB Color");
		title1.setTextSize(10);
		title1.setTextColor(Color.parseColor("#333333"));
		TextView title2 = new TextView(context);
		title2.setText("RGB Saturation");
		title2.setTextSize(10);
		title2.setTextColor(Color.parseColor("#333333"));
		TextView title3 = new TextView(context);
		title3.setText("RGB Brightness");
		title3.setTextSize(10);
		title3.setTextColor(Color.parseColor("#333333"));
		TextView title4 = new TextView(context);
		title4.setText("Luminosity");
		title4.setTextSize(10);
		title4.setTextColor(Color.parseColor("#333333"));
		TextView title5 = new TextView(context);
		title5.setText("RGB Field");
		title5.setTextSize(10);
		title5.setTextColor(Color.parseColor("#333333"));
		TextView title6 = new TextView(context);
		title6.setText("Current Color");
		title6.setTextSize(10);
		title6.setTextColor(Color.parseColor("#333333"));
		title7 = new TextView(context);
		title7.setText("Red: 255");
		title7.setTextSize(10);
		title7.setTextColor(Color.parseColor("#333333"));
		title8 = new TextView(context);
		title8.setText("Green: 0");
		title8.setTextSize(10);
		title8.setTextColor(Color.parseColor("#333333"));
		title9 = new TextView(context);
		title9.setText("Blue: 0");
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
		color_RightPan.setPadding(20, 0, 0, 0);

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
		color_LeftPan.addView(title4);
		color_LeftPan.addView(seekBarPowerBar);
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
		LoadSelections();
	}


	
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		if(arg0.getTag().equals("hue")){				
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
			
		}
		if(arg0.getTag().equals("rgbx")){
			rgbX = arg0.getProgress();
			float[] hsv2 = {0,0,0};
			float[] hsv3 = {mCurrentHue,(float)rgbX/255f,1};
			seekBarRGBYBar.hsv2 = hsv2;
			seekBarRGBYBar.hsv3 = hsv3;
			seekBarRGBYBar.invalidate();
			
			rgbView.mCurrentX = arg0.getProgress();
			seekBarRGBYBar.invalidate();
			rgbView.invalidate();

		}
		if(arg0.getTag().equals("rgby")){
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
		int argb = Color.HSVToColor(hsvCurrent);
		resultView.hsvCurrent = hsvCurrent;
		title7.setText("Red: "+((argb>>16)&0xFF));
		title8.setText("Green: "+((argb>>8)&0xFF));
		title9.setText("Blue: "+((argb)&0xFF));
		resultView.invalidate();
	}
	

	
	public void onStartTrackingTouch(SeekBar seekBar) {
		touching=true;
		updating=3;
	}


	
	public void onStopTrackingTouch(SeekBar seekBar) {
		state_progress = seekBar.getProgress();
		SaveSelections();
		Log.i("Graphical_Color","onStopTrackingTouch -> SaveSelections !");
		//new CommandeThread().execute();
		touching=false;
		
	}
	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			updating=3;
			//JSONObject json_Ack = Rest_com.connect(url+"command/"+type+"/"+address+"/"+command+"/"+state_progress);
			try {
				//@SuppressWarnings("unused")
				//Boolean ack = JSONParser.Ack(json_Ack);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	private void SaveSelections() {
		SharedPreferences.Editor prefEditor=params.edit();
		prefEditor.putInt("COLORHUE",seekBarHueBar.getProgress());
		prefEditor.putInt("COLORSATURATION",seekBarRGBXBar.getProgress());
		prefEditor.putInt("COLORBRIGHTNESS",seekBarRGBYBar.getProgress());
		prefEditor.putInt("COLORPOWER",seekBarPowerBar.getProgress());
		prefEditor.commit();
		Log.i("Graphical_Color", "SaveSelections");
	}

	private void LoadSelections() {
		seekBarHueBar.setProgress(params.getInt("COLORHUE",0));
		seekBarRGBXBar.setProgress(params.getInt("COLORSATURATION",255));
		seekBarRGBYBar.setProgress(params.getInt("COLORBRIGHTNESS",255));
		seekBarPowerBar.setProgress(params.getInt("COLORPOWER",255));
		Log.i("Graphical_Color", "LoadSelections");
	}
	public boolean onTouch(View arg0, MotionEvent arg1) {
		Log.i("Graphical_Color", "Touch....");
		if(featurePan2.getVisibility()== INVISIBLE){
			background.addView(featurePan2);
			featurePan2.setVisibility(VISIBLE);
			Log.i("Graphical_Color", "FeaturePan2 set to VISIBLE");
		}
		else{
			background.removeView(featurePan2);
			featurePan2.setVisibility(INVISIBLE);
			Log.i("Graphical_Color", "FeaturePan2 set to INVISIBLE");
		}
		return false;
	}
}
