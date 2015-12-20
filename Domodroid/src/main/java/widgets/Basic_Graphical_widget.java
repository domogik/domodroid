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

import misc.List_Icon_Adapter;
import misc.tracerengine;

import org.domogik.domodroid13.R;

import database.Cache_management;
import database.DmdContentProvider;
import activities.Activity_Main;
import activities.Gradients_Manager;
import activities.Graphics_Manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.FeatureInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

public class Basic_Graphical_widget extends FrameLayout implements OnLongClickListener{

	final LinearLayout LL_background;
	final LinearLayout LL_infoPan;
	final LinearLayout LL_featurePan;
	final LinearLayout LL_topPan;
	final ImageView IV_img;
	private final TextView TV_name;
	private final int id;
	private final FrameLayout container;
	private final FrameLayout myself;
	private tracerengine Tracer = null;
	private final Activity context;
	private String icon;
	private final String place_type;
	private final int place_id;
	private final String mytag;
	private final String name;
	private final String state_key;

	Basic_Graphical_widget(Activity context, tracerengine Trac, int id, String name, String state_key, String icon, int widgetSize, int session_type, int place_id, String place_type, String mytag, FrameLayout container) {
		super(context);
		this.Tracer=Trac;
		this.context = context;
		this.icon=icon;
		this.id = id;
		this.setPadding(5, 5, 5, 5);
		this.place_id= place_id;
		this.place_type= place_type;
		this.mytag=mytag;
		this.container=container;
		this.myself=this;
		this.name=name;
		this.state_key=state_key;
		setOnLongClickListener(this);

		//panel with border	
		LL_background = new LinearLayout(context);
		LL_background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)
			LL_background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			LL_background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		LL_background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",LL_background.getHeight()));

		//panel with border
		LL_topPan = new LinearLayout(context);
		LL_topPan.setOrientation(LinearLayout.HORIZONTAL);
		LL_topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		//panel to set icon with padding left
		FrameLayout FL_imgPan = new FrameLayout(context);
		FL_imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
		FL_imgPan.setPadding(5, 8, 10, 10);

		//icon
		IV_img = new ImageView(context);
		IV_img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));

		//info panel
		LL_infoPan=new LinearLayout(context);
		LL_infoPan.setOrientation(LinearLayout.VERTICAL);
		LL_infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		LL_infoPan.setGravity(Gravity.CENTER_VERTICAL);
		LL_infoPan.setPadding(0, 0, 10, 0);

		//feature panel
		LL_featurePan=new LinearLayout(context);
		LL_featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		LL_featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		LL_featurePan.setPadding(0, 0, 20, 0);

		//name of room
		TV_name=new TextView(context);
		try{
			TV_name.setText(context.getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), name.toLowerCase())).toString());
		}catch (Exception e){
			Tracer.d(mytag, "no translation for: "+name);
			TV_name.setText(name);
		}

		TV_name.setTextSize(14);
		TV_name.setTextColor(Color.BLACK);
		TV_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

		LL_infoPan.addView(TV_name);
		FL_imgPan.addView(IV_img);

		LL_topPan.addView(FL_imgPan);
		LL_topPan.addView(LL_infoPan);
		LL_topPan.addView(LL_featurePan);

		LL_background.addView(LL_topPan);
		this.addView(LL_background);
	}

	public boolean onLongClick(View v) {
		final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
		List<String> list_choice = new ArrayList<String>();
		list_choice.add(context.getString(R.string.change_icon));
		list_choice.add(context.getString(R.string.rename));
		list_choice.add(context.getString(R.string.delete));
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
		if(action.equals(context.getString(R.string.rename))) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(context.getString(R.string.Rename_title)+" "+name+"-"+state_key);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
			alert.setView(input);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					String result= input.getText().toString(); 
					Tracer.get_engine().descUpdate(id,result,"feature");
                    try{
                        TV_name.setText(context.getResources().getString(Graphics_Manager.getStringIdentifier(getContext(), result.toLowerCase())).toString());
                    }catch (Exception e){
                        Tracer.d(mytag, "no translation for: "+result);
                        TV_name.setText(result);
                    }
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "Customname Canceled.");
				}
			});
			alert.show();
		}else if (action.equals(context.getString(R.string.delete))){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(context.getString(R.string.Delete_feature_title)+" "+name+"-"+state_key);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.d(mytag,"deleting widget id= "+id+" place_id= "+place_id+" placetype= "+place_type);

					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					//recheck cache element to remove those no more need.
					Cache_management.checkcache(Tracer,context);
					//Refresh the view
					if(container != null) {
						removeView(myself);
						removeAllViews();
						recomputeViewAttributes(myself);
						Tracer.d(mytag, "removing a view");

					}
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}else if (action.equals(context.getString(R.string.change_icon))){
			final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
			List<String> list_icon = new ArrayList<String>();
			String[] fiilliste;
			fiilliste = context.getResources().getStringArray(R.array.icon_area_array); 
			for (int i=0; i < fiilliste.length ; i++){
				list_icon.add(fiilliste[i]);
			}
			final CharSequence[] char_list_icon =list_icon.toArray(new String[list_icon.size()]);
			list_icon_choice.setTitle(context.getString(R.string.Wich_ICON_message)+" "+name+"-"+state_key);
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
					values.put("name", "feature");
					//icon is the name of the icon wich will be select 
					values.put("value", icon);
					//reference is the id of the area, room, or feature
					int reference = 0;
					reference=id;
					values.put("reference", reference);
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
					//TODO need to select good icon in function of his state
					//Redraw it for this.
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

