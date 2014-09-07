package activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.domogik.domodroid13.R;

import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Icon;
import widgets.Entity_Room;

import database.DmdContentProvider;
import database.DomodroidDB;
import database.WidgetUpdate;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Entity;
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
	private Button add_icon_Button;
	private Spinner spinner_area;
	private Spinner spinner_room;
	private Spinner spinner_feature;
	private Spinner spinner_icon;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;
	private int area_id = 0;
	private int room_id = 0;
	private int feature_id = 0;
	private int lastid = 0;
	private String type=null;
	private String icon=null;
	private WidgetUpdate widgetUpdate;
	private HashMap<String,String> map;
	private Dialog dialog_feature;
	private DomodroidDB domodb;
	private Entity_Area[] listArea;
	private Entity_Room[] listRoom;
	private Entity_Feature[] listFeature;
	private Entity_Icon[] listIcon;
	private String mytag;
	
	public Dialog_House(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_house);
		mytag="Dialog_House";
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
		
		add_icon_Button = (Button) findViewById(R.id.house_add_icon);
		add_icon_Button.setTag("add_icon");
		add_icon_Button.setOnClickListener(this);
		
		spinner_area = (Spinner) findViewById(R.id.spin_list_area);
		spinner_room = (Spinner) findViewById(R.id.spin_list_room);
		spinner_feature = (Spinner) findViewById(R.id.spin_list_feature);
		spinner_icon = (Spinner) findViewById(R.id.spin_list_icon);
		// Loading spinner data from database
        loadSpinnerData();
        
	}
	
	public void onClick(final View v) {
		String tag = v.getTag().toString();
		dialog_feature = new Dialog(getContext());

		//list area where to put room
			final AlertDialog.Builder list_area_choice = new AlertDialog.Builder(getContext());
			List<String> list_area = new ArrayList<String>();
			for (Entity_Area area : listArea) {
				list_area.add(area.getName());
				}
			final CharSequence[] char_list_zone =list_area.toArray(new String[list_area.size()]);
			list_area_choice.setTitle(R.string.Wich_AREA_message);
			list_area_choice.setSingleChoiceItems(char_list_zone, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						area_id=listArea[item].getId();
						dialog.cancel();
					}
				}
			);
			
		//list room where to put widget
			final AlertDialog.Builder list_room_choice = new AlertDialog.Builder(getContext());
			List<String> list_room = new ArrayList<String>();
			for (Entity_Room room : listRoom) {
				list_room.add(room.getName());
			}
			final CharSequence[] char_list_room =list_room.toArray(new String[list_room.size()]);
			list_room_choice.setTitle(R.string.Wich_ROOM_message);
			list_room_choice.setSingleChoiceItems(char_list_room, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						room_id=listRoom[item].getId();
						dialog.cancel();
					}
				}
			);
			
		//list widget to put in room
			final AlertDialog.Builder list_feature_choice = new AlertDialog.Builder(getContext());
			List<String> list_feature = new ArrayList<String>();
			for (Entity_Feature feature : listFeature) {
				list_feature.add(feature.getName()+"-"+feature.getState_key());
				}
			final CharSequence[] char_list_feature =list_feature.toArray(new String[list_feature.size()]);
			list_feature_choice.setTitle(R.string.Wich_feature_message);
			list_feature_choice.setSingleChoiceItems(char_list_feature, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							feature_id=listFeature[item].getId();
							dialog.cancel();
						}
					}
			);
		//list type area,room, widget
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
						dialog.cancel();
					}
				}
			);	
		//list type area,room, widget
			final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
			List<String> list_type = new ArrayList<String>();
				list_type.add("area");
				list_type.add("room");
				list_type.add("feature");
			final CharSequence[] char_list_type =list_type.toArray(new String[list_type.size()]);
			list_type_choice.setTitle(R.string.Wich_TYPE_message);
			list_type_choice.setSingleChoiceItems(char_list_type, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						ListView lw = ((AlertDialog)dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
						type = checkedItem.toString();
						v.setTag("add_icon_"+type);
						dialog.cancel();
						Dialog_House.this.onClick(v);
					}
				}
			);					
		//ADD a room
			AlertDialog.Builder alert_Room = new AlertDialog.Builder(getContext());
			//set a title
			alert_Room.setTitle(R.string.New_ROOM_title);
			//set a message
			alert_Room.setMessage(R.string.New_ROOM_message);
			// Set an EditText view to get user input 
			final EditText name = new EditText(getContext());
			alert_Room.setView(name);
			alert_Room.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					lastid = domodb.requestidlastRoom();
					ContentValues values = new ContentValues();
					values.put("area_id", (area_id));
					values.put("name", name.getText().toString());
					values.put("description", "");
					values.put("id", (lastid+1));
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ROOM, values);
					loadSpinnerData();
				}
			});
			alert_Room.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					
				}
			});
		
		//ADD an area
			AlertDialog.Builder alert_Area = new AlertDialog.Builder(getContext());
			//set a title
			alert_Area.setTitle(R.string.New_AREA_title);
			//set a message
			alert_Area.setMessage(R.string.New_AREA_message);
			// Set an EditText view to get user input 
			final EditText name1 = new EditText(getContext());
			alert_Area.setView(name1);
			alert_Area.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
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
			alert_Area.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					
				}
			});
		
		//ADD a feature
			AlertDialog.Builder alert_Feature = new AlertDialog.Builder(getContext());
			//set a title
			//TODO Not the good text
			//alert_Feature.setTitle(R.string.Rename_title);
			//set a message
			//alert_Feature.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			//final EditText name2 = new EditText(getContext());
			//alert_Feature.setView(name2);
			alert_Feature.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					lastid = domodb.requestidlastFeature_association();
					ContentValues values = new ContentValues();
					//roomid must come from the selected in list
					values.put("place_id", (room_id));
					values.put("place_type", ("room"));
					//device_feature_id must come from the selected  one in list
					values.put("device_feature_id",(feature_id));
					values.put("id", (lastid+1));
					//values.put("device_feature", itemArray.getJSONObject(i).getString("device_feature"));
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_ASSOCIATION, values);
					loadSpinnerData();
				}
			});
			alert_Feature.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					
				}
			});
			
		//ADD an icon
			AlertDialog.Builder alert_Icon = new AlertDialog.Builder(getContext());
			//set a title
			//TODO Not the good text
			//alert_Feature.setTitle(R.string.Rename_title);
			//set a message
			//alert_Feature.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			//final EditText name2 = new EditText(getContext());
			//alert_Feature.setView(name2);
			alert_Icon.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					ContentValues values = new ContentValues();
					//type = area, room, feature
					values.put("name", type);
					//icon is the name of the icon wich will be select 
					values.put("value", icon);
					//reference is the id of the area, room, or feature
					int reference = 0;
					if (type.equals("area"))
						reference=area_id;
					if (type.equals("room"))
						reference=room_id;
					if (type.equals("feature"))
						reference=feature_id;
					values.put("reference", reference);
					context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ICON, values);
					loadSpinnerData();
				}
			});
			alert_Icon.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					
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
			alert_Area.show();
		}else if (tag.equals("add_room")) {
			alert_Room.show();
			AlertDialog alert_list_area = list_area_choice.create();
			alert_list_area.show();
		}else if (tag.equals("add_widget")) {
			alert_Feature.show();
			AlertDialog alert_list_room = list_room_choice.create();
			alert_list_room.show();
			AlertDialog alert_list_feature = list_feature_choice.create();
			alert_list_feature.show();
		}else if (tag.equals("add_icon")) {
			list_type_choice.show();
			AlertDialog alert_list_icon = list_icon_choice.create();
			alert_list_icon.show();
			//TODO Ask user what icon i want to modify area, room, widget
			//in function display 
			//display list of all icons
		}else if (tag.equals("add_icon_area")){
			alert_Icon.show();
			AlertDialog alert_list_area = list_area_choice.create();
			alert_list_area.show();	
		}else if (tag.equals("add_icon_room")){
			alert_Icon.show();
			AlertDialog alert_list_room = list_room_choice.create();
			alert_list_room.show();	
		}else if (tag.equals("add_icon_feature")){
			alert_Icon.show();
			AlertDialog alert_list_feature = list_feature_choice.create();
			alert_list_feature.show();	
		}
				
	}
	
	private void loadSpinnerData() {
		domodb = new DomodroidDB(Tracer, context);
		
		listArea = domodb.requestArea();
		listRoom = domodb.requestallRoom();
		listFeature = domodb.requestFeatures();
		listIcon = domodb.requestallIcon();
		
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
		ArrayList<String> list_Feature= new ArrayList<String>();
		for (Entity_Feature feature : listFeature) {
			list_Feature.add(feature.getName()+"-"+feature.getValue_type());
			}
		ArrayAdapter<String> feature_adapter =
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_Feature);
		feature_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_feature.setAdapter(feature_adapter);
		
		//4th list icon to associate with area, room or widget
		ArrayList<String> list_icon= new ArrayList<String>();
		for (Entity_Icon icon : listIcon) {
			list_icon.add(icon.getName()+"-"+icon.getReference()+"-"+icon.getValue());
			}
		ArrayAdapter<String> icon_adapter =
				new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_icon);
		icon_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_icon.setAdapter(icon_adapter);
		
	}
}

