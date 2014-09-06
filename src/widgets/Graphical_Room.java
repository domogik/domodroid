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

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.domogik.domodroid13.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;

public class Graphical_Room extends FrameLayout implements OnClickListener, OnLongClickListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView name;
	private TextView description;
	private Context context;
	private String name_room;
	private int id_room;
	private Handler widgetHandler;
	private tracerengine Tracer = null;
	private String mytag="Graphical_Room";
	
	public Graphical_Room(tracerengine Trac, Context context,int id,String name_room, String description_room, String icon, int widgetSize, Handler handler) {
		super(context);
		this.Tracer = Trac;
		this.id_room = id;
		this.name_room = name_room;
		this.context = context;
		this.widgetHandler=handler;
		this.setPadding(5, 5, 5, 5);
		setOnClickListener(this);
		mytag="Graphical_Room("+id_room+")";
		
		//panel with border
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("ltblack",background.getHeight()));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(20, 8, 20, 10);
		
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));
		img.setTag("img");
		img.setOnLongClickListener(this);
		img.setOnClickListener(this);
		
		//info panel
		infoPan=new LinearLayout(context);
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		infoPan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		infoPan.setPadding(0, 0, 10, 0);


		//name of room
		name=new TextView(context);
		name.setText(name_room);
		name.setTextSize(18);
		name.setTextColor(Color.WHITE);
		name.setGravity(Gravity.RIGHT);
		name.setTag("name");
		name.setOnLongClickListener(this);
				
		//description
		description=new TextView(context);
		description.setText(description_room);
		name.setTextSize(17);
		description.setGravity(Gravity.RIGHT);

		infoPan.addView(name);
		infoPan.addView(description);
		imgPan.addView(img);

		background.addView(imgPan);
		background.addView(infoPan);
		//Tracer.d(mytag+"("+id+")","creating view for "+name_room+" "+description_room);
		
		this.addView(background);
	}

	public int getId() {
		return id_room;
	}
	
	
	public void onClick(View v) {
			Bundle b = new Bundle();
			b.putInt("id", id_room);
			b.putString("name",name_room);
			b.putString("type", "room");
			Message msg = new Message();
			msg.setData(b);
			widgetHandler.sendMessage(msg);
		return;
	}
	
	public boolean onLongClick(View v) {
		if(v.getTag().equals("img")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_things(id_room,"room");
					Tracer.get_engine().remove_one_place_type_in_Featureassociation(id_room,"room");
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
		}else if (v.getTag().equals("name")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id_room,result,"room");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}
	return false;
	
}

}
