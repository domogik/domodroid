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

import java.lang.Thread.State;
import java.util.Timer;
import java.util.TimerTask;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid.R;

import rinor.Stats_Com;

import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import misc.tracerengine;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

public class Com_Stats extends FrameLayout  {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout featurePan2;
	private View		  featurePan2_buttons;
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private int id;
	private Handler handler;
	private Activity context;
	private Message msg;
	private String mytag;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private tracerengine Tracer = null;
	
	private Stats_Com stats = null;
	private static Timer timer=null;
	private TextView elapsed_period;
	private TextView cumul_period;
	private TextView cum_statsPR;
	private TextView cum_statsBR;
	private TextView cum_statsPS;
	private TextView cum_statsBS;
	private TextView cum_eventsPR;
	private TextView cum_eventsBR;
	private TextView cum_eventsPS;
	private TextView cum_eventsBS;
	
	private TextView period_statsPR;
	private TextView period_statsBR;
	private TextView period_statsPS;
	private TextView period_statsBS;
	private TextView period_eventsPR;
	private TextView period_eventsBR;
	private TextView period_eventsPS;
	private TextView period_eventsBS;
	
	@SuppressLint("HandlerLeak")
	public Com_Stats(tracerengine Trac,Activity context, int widgetSize) {
		super(context);
		this.Tracer = Trac;
		this.context = context;
		this.myself = this;
		this.container = container;
		
		mytag="Com_Stats";
		this.setPadding(5, 5, 5, 5);
		Tracer.e(mytag,"New instance");
		stats = Stats_Com.getInstance();
		
		//panel with border
		background = new LinearLayout(context);
		background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)
			background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		
		//background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));
		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.com_stats,null);
		
		background.addView(view);
		this.addView(background);
		elapsed_period = (TextView) findViewById(R.id.textPeriodValue);
		cumul_period = (TextView) findViewById(R.id.textCumulValue);
		cum_statsPR = (TextView) findViewById(R.id.statsPR);
		cum_statsBR = (TextView) findViewById(R.id.statsBR);
		cum_statsPS = (TextView) findViewById(R.id.statsPS);
		cum_statsBS = (TextView) findViewById(R.id.statsBS);
		cum_eventsPR = (TextView) findViewById(R.id.eventsPR);
		cum_eventsBR = (TextView) findViewById(R.id.eventsBR);
		cum_eventsPS = (TextView) findViewById(R.id.eventsPS);
		cum_eventsBS = (TextView) findViewById(R.id.eventsBS);
		
		period_statsPR = (TextView) findViewById(R.id.PstatsPR);
		period_statsBR = (TextView) findViewById(R.id.PstatsBR);
		period_statsPS = (TextView) findViewById(R.id.PstatsPS);
		period_statsBS = (TextView) findViewById(R.id.PstatsBS);
		period_eventsPR = (TextView) findViewById(R.id.PeventsPR);
		period_eventsBR = (TextView) findViewById(R.id.PeventsBR);
		period_eventsPS = (TextView) findViewById(R.id.PeventsPS);
		period_eventsBS = (TextView) findViewById(R.id.PeventsBS);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == 0) {
					//Message from timer expired
					cumul_period.setText(stats.get_cumul_period());
					elapsed_period.setText(stats.get_elapsed_period());
					cum_statsPR.setText(Integer.toString(stats.cumul_stats_recv_packets));
					cum_statsBR.setText(Integer.toString(stats.cumul_stats_recv_bytes));
					cum_statsPS.setText(Integer.toString(stats.cumul_stats_sent_packets));
					cum_statsBS.setText(Integer.toString(stats.cumul_stats_sent_bytes));
					cum_eventsPR.setText(Integer.toString(stats.cumul_events_recv_packets));
					cum_eventsBR.setText(Integer.toString(stats.cumul_events_recv_bytes));
					cum_eventsPS.setText(Integer.toString(stats.cumul_events_sent_packets));
					cum_eventsBS.setText(Integer.toString(stats.cumul_events_sent_bytes));
					
					period_statsPR.setText(Integer.toString(stats.periodic_stats_recv_packets));
					period_statsBR.setText(Integer.toString(stats.periodic_stats_recv_bytes));
					period_statsPS.setText(Integer.toString(stats.periodic_stats_sent_packets));
					period_statsBS.setText(Integer.toString(stats.periodic_stats_sent_bytes));
					period_eventsPR.setText(Integer.toString(stats.periodic_events_recv_packets));
					period_eventsBR.setText(Integer.toString(stats.periodic_events_recv_bytes));
					period_eventsPS.setText(Integer.toString(stats.periodic_events_sent_packets));
					period_eventsBS.setText(Integer.toString(stats.periodic_events_sent_bytes));
					
					//elapsed_period.setText(Integer.toString(stats.elapsed_period));
				} 
			}
			
		};
		Tracer.e(mytag,"Instance created");
		
		Timer();	
		

	}
	private void Timer() {
		timer = new Timer();
		
		TimerTask doAsynchronousTask = new TimerTask() {
		
			@Override
			public void run() {
				try {
					//Tracer.e(mytag,"Update statistics.."+stats.elapsed_period);
					handler.sendEmptyMessage(0);
					
				} catch (Exception e) {
					//e.printStackTrace();
				}
			};
			
		};
		if(timer != null) {
			timer.schedule(doAsynchronousTask, 0, 5000);	// Once per 5 seconds	
			
		}
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			
		}
	}
	
	
}



