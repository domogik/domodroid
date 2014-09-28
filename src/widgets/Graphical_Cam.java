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

import activities.Activity_Cam;
import activities.Gradients_Manager;
import activities.Graphics_Manager;

import org.domogik.domodroid13.R;

import database.DmdContentProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import misc.List_Icon_Adapter;
import misc.tracerengine;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Graphical_Cam extends Basic_Graphical implements OnClickListener{

	private int dev_id;
	private String url;
	private Context context;
	private static String mytag;
	private tracerengine Tracer = null;
	public FrameLayout container = null;
	public FrameLayout myself = null;
	private String name_cam;
	
	public Graphical_Cam(tracerengine Trac, Activity context,int id,int dev_id,String name, String url,String usage,int widgetSize, int session_type,int place_id,String place_type) {
		super(context,Trac, id, name, "", usage, widgetSize, session_type, place_id, place_type,mytag);
		this.context = context;
		this.Tracer = Trac;
		this.dev_id = dev_id;
		this.name_cam=name;
		this.url = url;
		this.myself = this;
		this.setPadding(5, 5, 5, 5);
		setOnClickListener(this);
		mytag="Graphical_Cam("+dev_id+")";
		
		//panel with border
		
		
	}

	public int getId() {
		return dev_id;
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
}
