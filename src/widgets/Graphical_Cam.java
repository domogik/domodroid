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

import activities.Activity_Cam;
import activities.Gradients_Manager;
import org.domogik.domodroid.R;

import database.DomodroidDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import misc.Tracer;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Cam extends FrameLayout implements OnTouchListener, OnLongClickListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout mainPan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView nameDevices;
	private String name_cam;
	private int dev_id;
	private String url;
	private Context context;
	private DomodroidDB domodb;
	

	public Graphical_Cam(Activity context,int dev_id,String name, String url,int widgetSize) {
		super(context);
		this.dev_id = dev_id;
		this.name_cam = name;
		this.url = url;
		this.context = context;
		setOnTouchListener(this);
		domodb = new DomodroidDB(context);
		domodb.owner="Graphical_Boolean("+dev_id+")";
		
		this.setPadding(5, 5, 5, 5);

		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setOrientation(LinearLayout.VERTICAL);
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));



		//main panel
		mainPan=new LinearLayout(context);
		mainPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		mainPan.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 5, 10);
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(R.drawable.camera);

		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);

		//name of room
		nameDevices=new TextView(context);
		nameDevices.setText("Camera: "+name);
		nameDevices.setTextSize(15);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setOnLongClickListener(this);

		imgPan.addView(img);
		infoPan.addView(nameDevices);		
		mainPan.addView(imgPan);
		mainPan.addView(infoPan);

		//background.addView(camPan);
		background.addView(mainPan);

		this.addView(background);
	}

	public int getId() {
		return dev_id;
	}

	public void setName_cam(String name_cam) {
		this.name_cam = name_cam;
	}



	public boolean onTouch(View v, MotionEvent event) {
		if(!url.equals(null)){
			if((event.getAction() & MotionEvent.ACTION_MASK)==MotionEvent.ACTION_UP){
				Intent intent = new Intent(context,Activity_Cam.class);
				Bundle b = new Bundle();
				b.putString("url", url);
				Tracer.e("tag",""+url);
				b.putString("name",name_cam);
				intent.putExtras(b);
				context.startActivity(intent);
			}
		}
		return true;
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
					Tracer.e("Graphical_Cam", "Name set to: "+result);
					domodb.updateFeaturename(dev_id,result);
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Tracer.e("Graphical_Cam", "Customname Canceled.");
				}
			});
			alert.show();
			return false;
	}}
