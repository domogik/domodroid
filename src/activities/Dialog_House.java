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
import database.WidgetUpdate;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;

public class Dialog_House extends Dialog implements OnClickListener {
	private Button cancelButton;
	private Button OKButton;
	private Button add_area_Button;
	private Button add_room_Button;
	private Button add_widget_Button;
	private Spinner spinner_area;
	private Spinner spinner_room;
	private Spinner spinner_feature;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;
	private int area_id = 0;
	private int lastid = 0;
	private WidgetUpdate widgetUpdate;
	private HashMap<String,String> map;
	private Dialog dialog_feature;
	private DomodroidDB domodb;
	private Entity_Area[] listArea;
	private Entity_Room[] listRoom;
	private Entity_Feature[] listFeature;
	
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
		
		add_widget_Button = (Button) findViewById(R.id.house_add_widget);
		add_widget_Button.setTag("add_widget");
		add_widget_Button.setOnClickListener(this);
		spinner_area = (Spinner) findViewById(R.id.spin_list_area);
		spinner_room = (Spinner) findViewById(R.id.spin_list_room);
		spinner_feature = (Spinner) findViewById(R.id.spin_list_feature);
		// Loading spinner data from database
        loadSpinnerData();
        
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		dialog_feature = new Dialog(getContext());

		//1st list area where to put room
		final AlertDialog.Builder listareachoice = new AlertDialog.Builder(getContext());
		List<String> list_area = new ArrayList<String>();
		for (Entity_Area area : listArea) {
			list_area.add(area.getName());
			}
		final CharSequence[] char_list_zone =list_area.toArray(new String[list_area.size()]);
		listareachoice.setTitle("Dans quelle zone?");
		listareachoice.setSingleChoiceItems(char_list_zone, -1,
		 new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int item) {
		  //TODO item must be replace by the area Id of this id because Db could be altered
			  area_id=item;
		  dialog.cancel();
		  }
		 });
		
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
				loadSpinnerData();
			}
		});
		alertRoom.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog_customname, int whichButton) {
				Tracer.e("Graphical_Binary_New", "Customname Canceled.");
			}
		});
		
		//ADD an area
		AlertDialog.Builder alertArea = new AlertDialog.Builder(getContext());
		//set a title
		alertArea.setTitle(R.string.Rename_title);
		//set a message
		alertArea.setMessage(R.string.Rename_message);
		// Set an EditText view to get user input 
		final EditText name1 = new EditText(getContext());
		alertArea.setView(name1);
		//final EditText description = new EditText(getContext());
		//alert.add(description);
		alertArea.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog_customname, int whichButton) {
				ContentValues values = new ContentValues();
				values.put("name", name1.getText().toString());
				//values.put("description", itemArray.getJSONObject(i).getString("description").toString());
				//put the next available id from db here
				int lastid = domodb.requestlastidArea();
				values.put("id", lastid+1);
				context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_AREA, values);
				loadSpinnerData();
			}
		});
		alertArea.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog_customname, int whichButton) {
				Tracer.e("Graphical_Binary_New", "Customname Canceled.");
			}
		});
		
		
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
			alertArea.show();
		}else if (tag.equals("add_room")) {
			AlertDialog alert_listarea = listareachoice.create();
			alertRoom.show();
			alert_listarea.show();
			
		}else if (tag.equals("add_widget")) {
			alertRoom.show();
			
		}
	
	}
	
	private void loadSpinnerData() {
		domodb = new DomodroidDB(Tracer, context);
		
		listArea = domodb.requestArea();
		listRoom = domodb.requestallRoom();
		listFeature = domodb.requestFeatures();
		
		//1st list area where to put room
		ArrayList<String> list_Area = new ArrayList<String>();
		for (Entity_Area area : listArea) {
			list_Area.add(area.getName());
			}
		ArrayAdapter<String> area_adapter =
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_Area);
		area_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_area.setAdapter(area_adapter);
		
		//2nd list room where to put widget but contain also the area
		//widget could be place in an area or a room.
		ArrayList<String> list_Room = new ArrayList<String>();
		for (Entity_Room room : listRoom) {
			list_Room.add(room.getName());
			}
		ArrayAdapter<String> room_adapter =
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_Room);
		room_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_room.setAdapter(room_adapter);

		//3rd list feature to put somewhere
		//TODO finish this part
		ArrayList<String> list_Feature= new ArrayList<String>();
		for (Entity_Feature feature : listFeature) {
			list_Feature.add(feature.getName()+"-"+feature.getValue_type());
			}
		ArrayAdapter<String> feature_adapter =
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_Feature);
		feature_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_feature.setAdapter(feature_adapter);

	}
}

