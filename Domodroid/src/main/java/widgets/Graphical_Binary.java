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
 */
package widgets;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.send_command;

public class Graphical_Binary extends Basic_Graphical_widget implements OnSeekBarChangeListener {

    private TextView state;
    private SeekBar seekBarOnOff;
    private String value0;
    private String value1;
    private final boolean activate = false;
    private Animation animation;
    private boolean touching;
    private Message msg;
    public FrameLayout container = null;
    private FrameLayout myself = null;
    private static String mytag = "";
    private String stateS = "";
    private String Value_0 = "0";
    private String Value_1 = "1";
    private JSONObject jparam;
    private Entity_client session = null;
    private Boolean realtime = false;
    private String command_id = null;
    private String command_type = null;
    private final Entity_Feature feature;
    private final int session_type;
    private String address;
    private String usage;
    private String state_key;
    private int dev_id;
    private String status;
    private String Value_timestamp;

    public Graphical_Binary(tracerengine Trac,
                            final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                            final Entity_Feature feature, Handler handler) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Binary(tracerengine Trac,
                            final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                            final Entity_Map feature_map, Handler handler) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        myself = this;
        this.address = feature.getAddress();
        this.usage = feature.getIcon_name();
        this.state_key = feature.getState_key();

        try {
            this.stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            this.stateS = state_key;
        }
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }

        String parameters = feature.getParameters();
        mytag = "Graphical_Binary(" + dev_id + ")";


        //get parameters

        try {
            jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            value1 = jparam.getString("value1");
            value0 = jparam.getString("value0");

        } catch (Exception e) {
            value0 = "0";
            value1 = "1";
        }

        switch (usage) {
            case "light":
                this.Value_0 = getResources().getText(R.string.light_stat_0).toString();
                this.Value_1 = getResources().getText(R.string.light_stat_1).toString();
                break;
            case "shutter":
                this.Value_0 = getResources().getText(R.string.shutter_stat_0).toString();
                this.Value_1 = getResources().getText(R.string.shutter_stat_1).toString();
                break;
            default:
                this.Value_0 = value0;
                this.Value_1 = value1;
                break;
        }

        String[] model = feature.getDevice_type_id().split("\\.");
        String type = model[0];
        Tracer.d(mytag, "model_id = <" + feature.getDevice_type_id() + "> type = <" + feature.getDevice_type() + "> value0 = " + value0 + "  value1 = " + value1);

        //state
        state = new TextView(activity);
        state.setTextColor(Color.BLACK);
        state.setText(stateS);

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        //first seekbar on/off
        seekBarOnOff = new SeekBar(activity);
        seekBarOnOff.setProgress(0);
        seekBarOnOff.setMax(40);
        Bitmap bMap = BitmapFactory.decodeResource(getResources(), R.drawable.bgseekbaronoff);
        seekBarOnOff.setLayoutParams(new LayoutParams(bMap.getWidth(), bMap.getHeight()));
        seekBarOnOff.setProgressDrawable(getResources().getDrawable(R.drawable.bgseekbaronoff));
        seekBarOnOff.setThumb(getResources().getDrawable(R.drawable.buttonseekbar));
        seekBarOnOff.setThumbOffset(0);
        seekBarOnOff.setOnSeekBarChangeListener(this);
        seekBarOnOff.setTag("0");

        super.LL_infoPan.addView(state);
        super.LL_featurePan.addView(seekBarOnOff);


        if (api_version >= 0.7f) {
            try {
                int number_of_command_parameters = jparam.getInt("number_of_command_parameters");
                if (number_of_command_parameters == 1) {
                    command_id = jparam.getString("command_id");
                    command_type = jparam.getString("command_type1");
                }
            } catch (JSONException e) {
                Tracer.d(mytag, "No command_id for this device");
                seekBarOnOff.setEnabled(false);
            }
        }
        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (activate) {
                    Tracer.d(mytag, "Handler receives a request to die ");
                    if (realtime) {
                        Tracer.get_engine().unsubscribe(session);
                        session = null;
                        realtime = false;
                    }
                    //That seems to be a zombie
                    //removeView(background);
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    try {
                        finalize();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }    //kill the handler thread itself
                } else {
                    try {
                        Bundle b = msg.getData();
                        if ((b != null) && (b.getString("message") != null)) {
                            if (b.getString("message").equals(value0) || b.getString("message").equals(value1)) {
                                status = session.getValue();
                                Value_timestamp = session.getTimestamp();
                                update_display();
                                state.setAnimation(animation);
                            } else {
                                if (msg.what == 2) {
                                    Toast.makeText(getContext(), R.string.command_failed, Toast.LENGTH_SHORT).show();
                                } else if (msg.what == 9989) {
                                    //state_engine send us a signal to notify value changed
                                    if (session == null)
                                        return true;
                                    status = session.getValue();
                                    Value_timestamp = session.getTimestamp();
                                    update_display();
                                } else if (msg.what == 9998) {
                                    // state_engine send us a signal to notify it'll die !
                                    Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                                    session = null;
                                    realtime = false;
                                    //removeView(background);
                                    myself.setVisibility(GONE);
                                    if (container != null) {
                                        container.removeView(myself);
                                        container.recomputeViewAttributes(myself);
                                    }
                                    try {
                                        finalize();
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                    }    //kill the handler thread itself
                                }
                            }
                        }
                    } catch (Exception e) {
                        Tracer.e(mytag, "Handler error for device " + name);
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our value is changed
		 *
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    realtime = true;        //we're connected to engine
                    //each time our value change, the engine will call handler
                    handler.sendEmptyMessage(9989);    //Force to consider current value in session
                    EventBus.getDefault().register(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....

    }

    /**
     * @param event an Entity_client_event_value from EventBus when a new value is received from widgetupdate.
     */
    @Subscribe
    public void onEvent(Entity_client_event_value event) {
        // your implementation
        Tracer.d(mytag, "Receive event from Eventbus" + event.Entity_client_event_get_id() + " With value" + event.Entity_client_event_get_val());
        if (event.Entity_client_event_get_id() == dev_id) {
            status = event.Entity_client_event_get_val();
            Value_timestamp = event.Entity_client_event_get_timestamp();
            update_display();
        }
    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {
        Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);
        if (status.equals(value0)) {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_0)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_0);
            }
            new SBAnim(seekBarOnOff.getProgress(), 0).execute();
        } else if (status.equals(value1)) {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_1)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + Value_1);
            }
            new SBAnim(seekBarOnOff.getProgress(), 40).execute();
        } else {
            try {
                state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, status)));
            } catch (Exception e1) {
                state.setText(stateS + " : " + status);
            }
            new SBAnim(seekBarOnOff.getProgress(), 0).execute();
        }
    }

    public void onProgressChanged(SeekBar seekBarOnOff, int progress, boolean fromTouch) {
        switch (progress) {
            case 0:
                change_this_icon(0);
                try {
                    state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_0)));
                } catch (Exception e1) {
                    state.setText(stateS + " : " + Value_0);
                }
                break;
            case 40:
                change_this_icon(2);
                try {
                    state.setText(stateS + " : " + activity.getString(translate.do_translate(getContext(), Tracer, Value_1)));
                } catch (Exception e1) {
                    state.setText(stateS + " : " + Value_1);
                }
                break;
        }
    }

    public void onStartTrackingTouch(SeekBar arg0) {
        touching = true;
        int updating = 3;
    }

    public void onStopTrackingTouch(SeekBar arg0) {
        String state_progress;
        if (arg0.getProgress() < 20) {
            if (api_version >= 0.7f) {
                state_progress = "0";
            } else {
                state_progress = value0;
            }
            arg0.setProgress(0);
        } else {
            if (api_version >= 0.7f) {
                state_progress = "1";
            } else {
                state_progress = value1;
            }
            arg0.setProgress(40);
        }
        send_command.send_it(activity, Tracer, command_id, command_type, state_progress, api_version);
        touching = false;
    }

    public class SBAnim extends AsyncTask<Void, Integer, Void> {
        private final int begin;
        private final int end;

        public SBAnim(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        protected Void doInBackground(Void... params) {
            final int steps = java.lang.Math.abs(end - begin);
            new Thread(new Runnable() {
                public synchronized void run() {
                    for (int i = 0; i <= steps; i++) {
                        try {
                            this.wait(5);
                            if (!touching) {
                                if (end - begin > 0) seekBarOnOff.setProgress(begin + i);
                                else seekBarOnOff.setProgress(begin - i);
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

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (visibility == View.VISIBLE) {
            //activate=true;
        }
    }

}





