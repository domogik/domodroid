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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import misc.tracerengine;
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

public class Graphical_Cam extends FrameLayout implements OnClickListener, OnLongClickListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout mainPan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView nameDevices;
	private String name_cam;
	private int dev_id;
	private int id;
	private String url;
	private Context context;
	private String mytag;
	private tracerengine Tracer = null;
	private int session_type;
	private Message msg;
	private String place_type;
	private int place_id;
	

	public Graphical_Cam(tracerengine Trac, Activity context,int id,int dev_id,String name, String url,int widgetSize, int session_type,int place_id,String place_type) {
		super(context);
		this.Tracer = Trac;
		this.dev_id = dev_id;
		this.id = id;
		this.name_cam = name;
		this.url = url;
		this.context = context;
		this.session_type = session_type;
		this.place_id= place_id;
		this.place_type= place_type;
		setOnClickListener(this);
		mytag="Graphical_Boolean("+dev_id+")";
		
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
		img.setTag("img");
		img.setOnClickListener(this);
		img.setOnLongClickListener(this);

		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);

		//name of room
		nameDevices=new TextView(context);
		nameDevices.setText("Camera: "+name);
		nameDevices.setTextSize(14);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTag("namedevices");
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



	public void onClick(View v) {
		if(!url.equals(null)){
			Intent intent = new Intent(context,Activity_Cam.class);
			Bundle b = new Bundle();
			b.putString("url", url);
			Tracer.e(mytag,""+url);
			b.putString("name",name_cam);
			intent.putExtras(b);
			context.startActivity(intent);
			}
		return;
	}
	public boolean onLongClick(View v) {
		if(v.getTag().equals("namedevices")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id,result,"feature");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}else if (v.getTag().equals("img")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					//TODO do this in a menu
					//Tracer.get_engine().remove_one_feature_association(id);
					//Tracer.get_engine().remove_one_feature(id);
					//Tracer.get_engine().remove_one_feature_in_FeatureMap(id);
					removeAllViewsInLayout ();	
					postInvalidate();
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}
		return false;
		
	}
}
