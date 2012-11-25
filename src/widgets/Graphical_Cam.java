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

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Graphical_Cam extends FrameLayout implements OnTouchListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout mainPan;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView name;
	private String name_cam;
	private int id;
	private String url;
	private Context context;


	public Graphical_Cam(Context context,int id,String name_cam, String url,int widgetSize) {
		super(context);
		this.id = id;
		this.name_cam = name_cam;
		this.url = url;
		this.context = context;
		setOnTouchListener(this);

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
		name=new TextView(context);
		name.setText("Camera: "+name_cam);
		name.setTextSize(15);
		name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		name.setTextColor(Color.BLACK);

		imgPan.addView(img);
		infoPan.addView(name);		
		mainPan.addView(imgPan);
		mainPan.addView(infoPan);

		//background.addView(camPan);
		background.addView(mainPan);

		this.addView(background);
	}

	public int getId() {
		return id;
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
				Log.e("tag",""+url);
				b.putString("name",name_cam);
				intent.putExtras(b);
				context.startActivity(intent);
			}
		}
		return true;
	}	
}
