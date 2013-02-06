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

import rinor.Rest_com;
import database.DomodroidDB;
import database.JSONParser;

import org.domogik.domodroid.R;
import org.json.JSONException;
import org.json.JSONObject;
import activities.Gradients_Manager;
import activities.Graphics_Manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import misc.Tracer;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Trigger extends FrameLayout implements Runnable, OnClickListener, OnLongClickListener {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView nameDevices;
	private Graphical_Trigger_Button trigger;
	private String address;
	private String url;
	private Handler handler;
	private Thread threadCommande;
	private String type; 
	private String command; 
	//private Boolean activate=false;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private int dev_id;
	private DomodroidDB domodb;
	

	public Graphical_Trigger(Activity context, 
			String address, String name, int dev_id,String stat_key, 
			String url, String usage, String parameters, 
			String model_id, int widgetSize) throws JSONException {
		
		super(context);
		this.address = address;
		this.url = url;
		this.myself=this;
		this.dev_id = dev_id;
		domodb = new DomodroidDB(context);
		domodb.owner="Graphical_Trigger("+dev_id+")";
		
		//get parameters
        JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
        if(jparam != null)
        	command = jparam.getString("command");
        else
        	command="";

        String[] model = model_id.split("\\.");
        type = model[0];

		this.setPadding(5, 5, 5, 5);
        
		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)
			background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));


		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 10, 10);
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 0));


		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);

		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name); //debug option
		//nameDevices.setText(name+" ("+dev_id+")");
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(16);
		nameDevices.setOnLongClickListener(this);
		

		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding(0, 0, 15, 0);

		//first seekbar on/off
		trigger = new Graphical_Trigger_Button(context);
		trigger.setLayoutParams(new LinearLayout.LayoutParams(100,100));
		trigger.setOnClickListener(this);

		featurePan.addView(trigger);
		infoPan.addView(nameDevices);
		imgPan.addView(img);
		background.addView(imgPan);
		background.addView(infoPan);
		background.addView(featurePan);

		this.addView(background);
	}


	public void run() {
		JSONObject json_Ack = Rest_com.connect(url+"command/"+type+"/"+address+"/"+command);
		try {
			Boolean ack = JSONParser.Ack(json_Ack);
			if(ack==false){
				handler.sendEmptyMessage(2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	
	public void onClick(View arg0) {
		trigger.startAnim();
		threadCommande = new Thread(this);
		threadCommande.start();	
	}
	public boolean onLongClick(View arg0) {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setTitle(R.string.Rename_title);
		alert.setMessage(R.string.Rename_message);
		// Set an EditText view to get user input 
		final EditText input = new EditText(getContext());
			alert.setView(input);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.e("Graphical_Trigger", "Name set to: "+result);
					domodb.updateFeaturename(dev_id,result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Tracer.e("Graphical_Trigger", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}
}




