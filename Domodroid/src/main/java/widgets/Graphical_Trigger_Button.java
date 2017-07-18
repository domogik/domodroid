package widgets;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.domogik.domodroid13.R;

public class Graphical_Trigger_Button extends LinearLayout {
    private boolean touching;
    private final Handler handler;
    private final Animation animation;
    private final ImageView sign;

    public Graphical_Trigger_Button(Context context, final String icon_name) {
        super(context);
        setBackgroundResource(R.drawable.button_trigger_bg_up);

        sign = new ImageView(context);
        Log.e("eeeeeeeeeeeeeeeee", "" + icon_name);
        switch (icon_name) {
            case "send next":
                sign.setImageResource(R.drawable.ic_skip_next_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "send previous":
                sign.setImageResource(R.drawable.ic_skip_previous_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "send vol up":
                sign.setImageResource(R.drawable.ic_volume_up_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "send vol down":
                sign.setImageResource(R.drawable.ic_volume_down_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "toggle mute":
                sign.setImageResource(R.drawable.ic_volume_mute_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "send play":
                sign.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "send stop":
                sign.setImageResource(R.drawable.ic_stop_white_24dp);
                //todo change animation style
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            case "wake on lan":
                sign.setImageResource(R.drawable.button_trigger_anim1);
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
            default:
                sign.setImageResource(R.drawable.button_trigger_anim1);
                animation = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(2000);
                break;
        }
        sign.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, Gravity.CENTER));

        this.addView(sign);


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    switch (icon_name) {
                        case "send next":
                            sign.setImageResource(R.drawable.ic_skip_next_white_24dp);
                            break;
                        case "send previous":
                            sign.setImageResource(R.drawable.ic_skip_previous_white_24dp);
                            break;
                        case "send vol up":
                            sign.setImageResource(R.drawable.ic_volume_up_white_24dp);
                            break;
                        case "send vol down":
                            sign.setImageResource(R.drawable.ic_volume_down_white_24dp);
                            break;
                        case "toggle mute":
                            sign.setImageResource(R.drawable.ic_volume_mute_white_24dp);
                            break;
                        case "send play":
                            sign.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                            break;
                        case "send stop":
                            sign.setImageResource(R.drawable.ic_stop_white_24dp);
                            break;
                        case "wake on lan":
                            sign.setImageResource(R.drawable.button_trigger_anim1);
                            break;
                        default:
                            sign.setImageResource(R.drawable.button_trigger_anim1);
                            break;
                    }
                } else if (msg.what == 1) {
                    switch (icon_name) {
                        case "send next":
                            sign.setImageResource(R.drawable.ic_skip_next_white_24dp);
                            break;
                        case "send previous":
                            sign.setImageResource(R.drawable.ic_skip_previous_white_24dp);
                            break;
                        case "send vol up":
                            sign.setImageResource(R.drawable.ic_volume_up_white_24dp);
                            break;
                        case "send vol down":
                            sign.setImageResource(R.drawable.ic_volume_down_white_24dp);
                            break;
                        case "toggle mute":
                            sign.setImageResource(R.drawable.ic_volume_mute_white_24dp);
                            break;
                        case "send play":
                            sign.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                            break;
                        case "send stop":
                            sign.setImageResource(R.drawable.ic_stop_white_24dp);
                            break;
                        case "wake on lan":
                            sign.setImageResource(R.drawable.button_trigger_anim2);
                            break;
                        default:
                            sign.setImageResource(R.drawable.button_trigger_anim2);
                            break;
                    }
                }
                return ;
            }
        };
    }


    private class SBAnim extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            new Thread(new Runnable() {
                public synchronized void run() {
                    for (int i = 0; i <= 3; i++) {
                        try {
                            if (!touching) {
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

    public void startAnim() {
        new SBAnim().execute();
        sign.startAnimation(animation);
    }
}
