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

import org.domogik.domodroid.R;

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;

public class Graphical_Area extends FrameLayout implements OnClickListener, OnLongClickListener{

	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout infoPan;
	private ImageView img;
	private TextView name;
	private TextView description;
	private Context context;
	private String name_area;
	private int id_area;
	private Handler widgetHandler;
	private tracerengine Tracer = null;
	private String mytag="Graphical_Area";
	
	public Graphical_Area(tracerengine Trac, Context context, int id,String name_area, String description_area, String icon, int widgetSize, Handler handler) {
		super(context);
		this.Tracer = Trac;
		this.id_area = id;
		this.name_area = name_area;
		this.context = context;
		this.widgetHandler=handler;
		this.setPadding(5, 5, 5, 5);
		setOnClickListener(this);
		
		mytag="Graphical_Area("+id_area+")";
		//Log.d("Graphical_Area("+id+")","creating view for "+name_area+" "+description_area);
		//panel with border	
		background = new LinearLayout(context);
		if(widgetSize==0)background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("black",background.getHeight()));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 8, 10, 10);
		
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));
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
		name.setText(name_area);
		name.setTextSize(18);
		name.setTextColor(Color.WHITE);
		name.setGravity(Gravity.RIGHT);

		//description
		description=new TextView(context);
		description.setText(description_area);
		name.setTextSize(17);
		description.setGravity(Gravity.RIGHT);

		infoPan.addView(name);
		infoPan.addView(description);
		imgPan.addView(img);

		background.addView(imgPan);
		background.addView(infoPan);

		this.addView(background);
	}

	
	public void onClick(View v) {
			Bundle b = new Bundle();
			b.putInt("id", id_area);
			b.putString("name",name_area);
			b.putString("type","area");
			Message msg = new Message();
			msg.setData(b);
			widgetHandler.sendMessage(msg);
		return;
	}


	public boolean onLongClick(View v) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_area(id_area);
					Tracer.get_engine().remove_one_place_type_in_Featureassociation(id_area,"area");
					//TODO remove all room in this area
					//need to list them first and then delete also all feature in those rooms
					}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		return false;
		
	}


}
