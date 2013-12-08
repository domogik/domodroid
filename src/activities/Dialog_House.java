package activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.domogik.domodroid.R;

import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Room;

import database.DmdContentProvider;
import database.DomodroidDB;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Dialog_House extends Dialog implements OnClickListener {
	private Button cancelButton;
	private Button OKButton;
	private Button add_area_Button;
	private Button add_room_Button;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;
	private ListView listeMap;
	private int area_id = 0;
	private int lastid = 0;
	
	public Dialog_House(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_house);
		
		cancelButton = (Button) findViewById(R.id.house_Cancel);
		cancelButton.setTag("house_cancel");
		cancelButton.setOnClickListener(this);
		
		OKButton = (Button) findViewById(R.id.house_OK);
		OKButton.setTag("house_ok");
		OKButton.setOnClickListener(this);
		
		add_area_Button = (Button) findViewById(R.id.house_add_area);
		add_area_Button.setTag("add_area");
		add_area_Button.setOnClickListener(this);
		
		add_room_Button = (Button) findViewById(R.id.house_add_room);
		add_room_Button.setTag("add_room");
		add_room_Button.setOnClickListener(this);
		
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("house_cancel"))
			dismiss();
		else if (tag.equals("house_ok")) {
			prefEditor=params.edit();
			try{
				prefEditor=params.edit();
				//To allow the area view we have to remove by usage option
				prefEditor.putBoolean("BY_USAGE", false);
				prefEditor.commit();
				
			} catch(Exception e){}
			
			prefEditor.commit();
			
			dismiss();
		}else if (tag.equals("add_area")) {
			//ADD an area
			AlertDialog.Builder alertArea = new AlertDialog.Builder(getContext());
			//set a title
			alertArea.setTitle(R.string.Rename_title);
			//set a message
			alertArea.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText name = new EditText(getContext());
			alertArea.setView(name);
			//final EditText description = new EditText(getContext());
			//alert.add(description);
			alertArea.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					ContentValues values = new ContentValues();
					values.put("name", name.getText().toString());
					//values.put("description", itemArray.getJSONObject(i).getString("description").toString());
					//put the next available id from db here
					DomodroidDB domodb = new DomodroidDB(Tracer, context);
					int lastid = domodb.requestlastidArea();
					values.put("id", lastid+1);
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_AREA, values);
				}
			});
			alertArea.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e("Graphical_Binary_New", "Customname Canceled.");
				}
			});
			alertArea.show();
		}else if (tag.equals("add_room")) {
			final DomodroidDB domodb = new DomodroidDB(Tracer, context);
			final Entity_Area[] listArea = domodb.requestArea();
			//1st list area where to put room
			final AlertDialog.Builder listroomchoice = new AlertDialog.Builder(getContext());
			List<String> list_zone = new ArrayList<String>();
			for (Entity_Area area : listArea) {
				list_zone.add(area.getName());
				}
			final CharSequence[] char_list_zone =list_zone.toArray(new String[list_zone.size()]);
			listroomchoice.setTitle("Dans quelle zone?");
			listroomchoice.setSingleChoiceItems(char_list_zone, -1,
			 new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int item) {
			  //TODO item must be replace by the area Id of this id because Db could be altered
				  area_id=item;
			  dialog.cancel();
			  }
			 });
			AlertDialog alert_listarea = listroomchoice.create();
			
			//ADD a room
			AlertDialog.Builder alertRoom = new AlertDialog.Builder(getContext());
			alertRoom.setTitle(R.string.Rename_title);
			alertRoom.setMessage(R.string.Rename_message);
			final EditText name = new EditText(getContext());
			alertRoom.setView(name);
			alertRoom.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					lastid = domodb.requestidlastRoom();
					ContentValues values = new ContentValues();
					values.put("area_id", (area_id+1));
					values.put("name", name.getText().toString());
					values.put("description", "");
					values.put("id", (lastid+1));
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ROOM, values);
				}
			});
			alertRoom.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e("Graphical_Binary_New", "Customname Canceled.");
				}
			});
			alertRoom.show();
			alert_listarea.show();
			
		}
	}
	
}

