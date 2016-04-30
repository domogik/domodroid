package activities;

import org.domogik.domodroid13.R;
import video.MjpegInputStream;
import video.MjpegView;
import video.MjpegViewAsync;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Activity_Cam extends AppCompatActivity {
	private MjpegViewAsync mv;
	private TextView TV_frameRate;


	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Bundle b=getIntent().getExtras();
		String name_cam = b.getString("name");
		String url = b.getString("url");
		//typedDimension
		float scale = getResources().getDisplayMetrics().density; 

		//activity
		LinearLayout LL_activity = new LinearLayout(this);
		LL_activity.setOrientation(LinearLayout.VERTICAL);

		//video panel
		FrameLayout FL_viewPan = new FrameLayout(this);
		FL_viewPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		//info panel
		LinearLayout LL_infoPan = new LinearLayout(this);
		LL_infoPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		LL_infoPan.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
		LL_infoPan.setPadding(0, 0, 0, 20);


		//name of room
		TextView TV_name = new TextView(this);
		TV_name.setText(getText(R.string.camera)+": " + name_cam);
		TV_name.setTextSize(15);
		TV_name.setTextColor(Color.WHITE);
		TV_name.setPadding(0, 0, 15, 0);


		//description
		TV_frameRate=new TextView(this);
		TV_frameRate.setPadding(15, 0, 0, 0);
		TV_frameRate.setText(getText(R.string.frame_rate_default));


		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mv = new MjpegViewAsync(this);
		mv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		LL_infoPan.addView(TV_name);
		LL_infoPan.addView(TV_frameRate);
		FL_viewPan.addView(mv);
		FL_viewPan.addView(LL_infoPan);
		LL_activity.addView(FL_viewPan);

		setContentView(LL_activity);        
		try {
			mv.setSource(MjpegInputStream.read(url));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
		mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
		mv.showFps(true);

		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				TV_frameRate.setText(getText(R.string.frame_rate)+": " + msg.what + " "+getText(R.string.fps));
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
