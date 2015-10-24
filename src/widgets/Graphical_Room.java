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

import org.domogik.domodroid13.R;
import java.util.ArrayList;
import java.util.List;

import activities.Graphics_Manager;

import database.Cache_management;
import database.DmdContentProvider;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

public class Graphical_Room extends Basic_Graphical_zone implements OnLongClickListener{

	public FrameLayout container = null;
	public FrameLayout myself = null;
	private Context context;
	private int id_room;
	private tracerengine Tracer = null;
	private String mytag="Graphical_Room";
	private String icon;
	private Activity Activity;
	
	public Graphical_Room(tracerengine Trac, Context context,int id,String name_room, String description_room, String icon, int widgetSize, Handler handler) {
		super(context, id, name_room, description_room, icon, widgetSize, "room", handler);
		this.myself = this;
		this.Tracer = Trac;
		this.id_room = id;
		this.context = context;
		this.icon=icon;
		this.Activity = (android.app.Activity) context;
		setOnLongClickListener(this);
		mytag="Graphical_Room("+id_room+")";		
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
		if(action.equals("Delete")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(context.getString(R.string.Delete_feature_title)+" "+name);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_things(id_room,"room");
					Tracer.get_engine().remove_one_place_type_in_Featureassociation(id_room,"room");
					Tracer.get_engine().remove_one_icon(id_room,"room");
					//recheck cache element to remove those no more need.
					Cache_management.checkcache(Tracer,Activity);
					removeView(LL_background);
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
			alert.setTitle(context.getString(R.string.Rename_title)+" "+name);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id_room,result,"room");
						TV_name.setText(result);
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
			list_icon_choice.setTitle(context.getString(R.string.Wich_ICON_message)+" "+name);
			List_Icon_Adapter adapter=new List_Icon_Adapter(getContext(), fiilliste);
			list_icon_choice.setAdapter(adapter,null );
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
						IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));
						dialog.cancel();
					}
				}
			);	
			AlertDialog alert_list_icon = list_icon_choice.create();
			alert_list_icon.show();
			
		}	
		}

}
