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
import mq.Main;
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

public class Graphical_butler extends Basic_Graphical_widget implements OnClickListener{

	private final String url;
	private final Context context;
	private static String mytag;
	private tracerengine Tracer = null;
	public static final FrameLayout container = null;
	private final String name_butler;

	public Graphical_butler(tracerengine Trac, Activity context,int id,int dev_id,String name,String state_key, String url,String usage,int widgetSize, int session_type,int place_id,String place_type) {
		super(context,Trac, id, name, state_key, usage, widgetSize, session_type, place_id, place_type,mytag,container);
		this.context = context;
		this.Tracer = Trac;
		this.name_butler=name;
		this.url = url;
		FrameLayout myself = this;
		setOnClickListener(this);
		mytag="Graphical_butler("+dev_id+")";

		//To have the icon colored as it has no state
		change_this_icon(2);

	}

	public void onClick(View v) {
		if(!url.equals(null)){
			Intent intent = new Intent(context,Main.class);
			Bundle b = new Bundle();
			b.putString("url", url);
			Tracer.e(mytag,""+url);
			b.putString("name",name_butler);
			intent.putExtras(b);
			context.startActivity(intent);
		}
	}
}
