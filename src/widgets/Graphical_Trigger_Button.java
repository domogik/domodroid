package widgets;

import org.domogik.domodroid.*;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Graphical_Trigger_Button extends LinearLayout{
	private boolean touching;
	private Handler handler;
	private Animation animation;
	private ImageView sign;

	public Graphical_Trigger_Button(Context context) {
		super(context);
		setBackgroundResource(R.drawable.button_trigger_bg_up);

		sign = new ImageView(context);
		sign.setImageResource(R.drawable.button_trigger_anim1);
		//TODO change 40 because it' the source of http://tracker.domogik.org/issues/1822
		sign.setLayoutParams(new LinearLayout.LayoutParams(40,40,Gravity.CENTER));
		animation = new RotateAnimation(0,360,20,20);
		animation.setDuration(2000);
		this.addView(sign);
		

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0){
					sign.setImageResource(R.drawable.button_trigger_anim1);
				}else if(msg.what == 1){
					sign.setImageResource(R.drawable.button_trigger_anim2);
				}

			}	
		};
	}

	public class SBAnim extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			new Thread(new Runnable() {
				public synchronized void run() {
					for(int i=0;i<=3;i++){
						try {
							if(!touching){
								handler.sendEmptyMessage(1);
								this.wait(200);
								handler.sendEmptyMessage(0);
								this.wait(200);
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}		

				}
			}).start();
			return null;
		}
	}
	
	public void startAnim(){
		new SBAnim().execute();
		sign.startAnimation(animation);
	}
}
