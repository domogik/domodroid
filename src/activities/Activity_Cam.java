package activities;

import org.domogik.domodroid.R;
import video.MjpegInputStream;
import video.MjpegView;
import video.MjpegViewAsync;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.Tracer;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Activity_Cam extends Activity{
	private MjpegViewAsync mv;
	private LinearLayout activity;
	private FrameLayout title;
	private LinearLayout infoPan;
	private FrameLayout viewPan;
	private TextView name;
	private TextView frameRate;
	private Handler handler;
	private String name_cam;
	private String url;
	private ImageView img;
	private FrameLayout imgPan;
    
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle b=getIntent().getExtras();
		name_cam = b.getString("name");
		url = b.getString("url");
		Tracer.e("Activity_Cam","name_cam = "+name_cam+" , url = "+url);
		//typedDimension
		float scale = getResources().getDisplayMetrics().density; 

		//activity
		activity = new LinearLayout(this);
		activity.setOrientation(LinearLayout.VERTICAL);

		//create title layout
		title = new FrameLayout(this);
		title.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int) (40*scale)));
		title.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",(int) (40*scale)));
		activity.addView(title);
		
		//panel to set img with padding left
		imgPan = new FrameLayout(this);
		imgPan.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,Gravity.RIGHT));
		imgPan.setPadding(0, 8, 8, 0);
		//img
		img = new ImageView(this);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		img.setBackgroundResource(R.drawable.app_name);
		imgPan.addView(img);
		title.addView(imgPan);
		
		//video panel
		viewPan = new FrameLayout(this);
		viewPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

		//info panel
		infoPan=new LinearLayout(this);
		infoPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		infoPan.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		infoPan.setPadding(0, 0, 0, 20);


		//name of room
		name=new TextView(this);
		name.setText("Camera: "+name_cam);
		name.setTextSize(15);
		name.setTextColor(Color.WHITE);
		name.setPadding(0, 0, 15, 0);


		//description
		frameRate=new TextView(this);
		frameRate.setPadding(15, 0, 0, 0);
		frameRate.setText("Frame Rate: 0 Fps");

		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mv = new MjpegViewAsync(this);
		mv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		
		infoPan.addView(name);
		infoPan.addView(frameRate);
		viewPan.addView(mv);
		viewPan.addView(infoPan);
		activity.addView(viewPan);
		
		setContentView(activity);        
		try {
			mv.setSource(MjpegInputStream.read(url));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				frameRate.setText("Frame Rate: "+msg.what+" Fps");
			}	
		};
		mv.setHandler(handler);
	}

	@Override
	public void onPause() {
		super.onPause();
		mv.stopPlayback();
		finish();
	}
	
}
