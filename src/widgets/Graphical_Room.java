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

import java.util.ArrayList;
import java.util.List;

import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.domogik.domodroid13.R;

import database.DmdContentProvider;
import android.app.AlertDialog;
import android.content.ContentValues;
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
import android.widget.ListView;
import android.widget.TextView;

import android.view.View.OnLongClickListener;
import android.view.View.OnClickListener;

public class Graphical_Room extends FrameLayout implements OnClickListener, OnLongClickListener{

	public FrameLayout container = null;
	public FrameLayout myself = null;
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
	private String icon;
	
	public Graphical_Room(tracerengine Trac, Context context,int id,String name_room, String description_room, String icon, int widgetSize, Handler handler) {
		super(context);
		this.myself = this;
		this.Tracer = Trac;
		this.id_room = id;
		this.name_room = name_room;
		this.context = context;
		this.widgetHandler=handler;
		this.setPadding(5, 5, 5, 5);
		this.icon=icon;
		setOnClickListener(this);
		setOnLongClickListener(this);
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
		final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
		List<String> list_choice = new ArrayList<String>();
			list_choice.add("Rename");
			list_choice.add("Change_icon");
			list_choice.add("Delete");
		final CharSequence[] char_list =list_choice.toArray(new String[list_choice.size()]);
		//list_type_choice.setTitle(R.string.What_to_do_message);
		list_type_choice.setSingleChoiceItems(char_list, -1,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					ListView lw = ((AlertDialog)dialog).getListView();
					Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
					do_action(checkedItem.toString());
					dialog.cancel();
				}
			}
		);
	
		list_type_choice.show();
		return false;
	}
	
	private void do_action(String action) {
		//TODO CHANGE icon.
		
		if(action.equals("Delete")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_things(id_room,"room");
					Tracer.get_engine().remove_one_place_type_in_Featureassociation(id_room,"room");
					Tracer.get_engine().remove_one_icon(id_room,"room");
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
				Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}else if (action.equals("Rename")){
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
		}else if (action.equals("Change_icon")){
			final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
			List<String> list_icon = new ArrayList<String>();
			String[] fiilliste;
			fiilliste = context.getResources().getStringArray(R.array.icon_area_array); 
			for (int i=0; i < fiilliste.length ; i++){
				list_icon.add(fiilliste[i].toString());
			}
			final CharSequence[] char_list_icon =list_icon.toArray(new String[list_icon.size()]);
			list_icon_choice.setTitle(R.string.Wich_ICON_message);
			list_icon_choice.setSingleChoiceItems(char_list_icon, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						icon = checkedItem.toString();
						ContentValues values = new ContentValues();
						//type = area, room, feature
						values.put("name", "room");
						//icon is the name of the icon wich will be select 
						values.put("value", icon);
						//reference is the id of the area, room, or feature
						int reference = 0;
						reference=id_room;
						values.put("reference", reference);
						context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
						dialog.cancel();
					}
				}
			);	
			AlertDialog alert_list_icon = list_icon_choice.create();
			alert_list_icon.show();
			
		}	
		}

}
