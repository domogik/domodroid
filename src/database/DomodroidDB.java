package database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import database.DmdContentProvider;
import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Icon;
import widgets.Entity_Map;
import widgets.Entity_Room;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;



public class DomodroidDB {

	private Activity context;
	// Added by Doume to clarify debugging
	private String mytag="DomodroidDB";
	public String owner="";
	//////////////////////////////////////
	
	public DomodroidDB(Activity context){
		this.context = context;
	}

	public void updateDb(){
		//That should clear all tables, except feature_map
		context.getContentResolver().delete(DmdContentProvider.CONTENT_URI_UPGRADE_FEATURE_STATE, null, null);
	}
	
	public void closeDb(){
		try {
			context.getContentResolver().cancelSync(null);
		} catch ( Exception e) {}
	}

	public void insertArea(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("area");
		//context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_AREA, null);

		for (int i =0; i < itemArray.length(); i++){
			values.put("description", itemArray.getJSONObject(i).getString("description").toString());
			values.put("id", itemArray.getJSONObject(i).getInt("id"));
			values.put("name", itemArray.getJSONObject(i).getString("name").toString());
			Log.d(mytag,"Inserting Area "+itemArray.getJSONObject(i).getString("name").toString());
			
			context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_AREA, values);
		}
	}

	public void insertRoom(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("room");
		//context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_ROOM, null);

		int area_id;
		for (int i =0; i < itemArray.length(); i++){
			if(itemArray.getJSONObject(i).getString("area_id").equals(""))area_id=0;
			else area_id=itemArray.getJSONObject(i).getInt("area_id");
			values.put("area_id", area_id);
			values.put("description", itemArray.getJSONObject(i).getString("description").toString());
			values.put("id", itemArray.getJSONObject(i).getInt("id"));
			values.put("name", itemArray.getJSONObject(i).getString("name").toString());
			Log.d(mytag,"Inserting Room "+itemArray.getJSONObject(i).getString("name").toString());
			context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ROOM, values);
		}
	}

	public void insertIcon(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("ui_config");
		//context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_ICON, null);

		for (int i =0; i < itemArray.length(); i++){
			values.put("name", itemArray.getJSONObject(i).getString("name").toString());
			values.put("value", itemArray.getJSONObject(i).getString("value").toString());
			values.put("reference", itemArray.getJSONObject(i).getInt("reference"));
			context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_ICON, values);
		}
	}

	public void insertFeature(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("feature");
		//context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE, null);

		for (int i =0; i < itemArray.length(); i++){
			values.put("device_feature_model_id", itemArray.getJSONObject(i).getString("device_feature_model_id").toString());
			values.put("id", itemArray.getJSONObject(i).getInt("id"));
			values.put("device_id", itemArray.getJSONObject(i).getJSONObject("device").getInt("id"));
			values.put("device_usage_id", itemArray.getJSONObject(i).getJSONObject("device").getString("device_usage_id").toString());
			values.put("address", itemArray.getJSONObject(i).getJSONObject("device").getString("address").toString());
			values.put("device_type_id", itemArray.getJSONObject(i).getJSONObject("device").getString("device_type_id").toString());
			values.put("description", itemArray.getJSONObject(i).getJSONObject("device").getString("description").toString());
			values.put("name", itemArray.getJSONObject(i).getJSONObject("device").getString("name").toString());
			values.put("state_key", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("stat_key"));
			values.put("parameters", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("parameters"));
			values.put("value_type", itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("value_type"));
			context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE, values);
		}
	}



	public void insertFeatureAssociation(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("feature_association");
		//context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_ASSOCIATION, null);

		for (int i =0; i < itemArray.length(); i++){
			values.put("place_id", itemArray.getJSONObject(i).getInt("place_id"));
			values.put("place_type", itemArray.getJSONObject(i).getString("place_type"));
			values.put("device_feature_id", itemArray.getJSONObject(i).getInt("device_feature_id"));
			values.put("id", itemArray.getJSONObject(i).getInt("id"));
			values.put("device_feature", itemArray.getJSONObject(i).getString("device_feature"));
			context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_ASSOCIATION, values);
		}
	}


	public void insertFeatureState(JSONObject json) throws JSONException{
		ContentValues values = new ContentValues();
		JSONArray itemArray = json.getJSONArray("stats");
		String skey = null;
		String Val = null;
		Boolean exists = false;
		
		//Log.e(mytag+"("+owner+")", "Processing FeatureSate Array : <"+itemArray.toString()+">");
		
		context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_STATE, null);

		for (int i =0; i < itemArray.length(); i++){
			try {
				exists = itemArray.getJSONObject(i).getBoolean("exists");
				// can be true or false, depends on server's response in version 0.3
			} catch (Exception e) {
				// Servers 0.2 does'nt return this kind of parameter : set true by default
				exists = true;
			}
			//if(exists) {
				try {
					skey = itemArray.getJSONObject(i).getString("skey");
				} catch (Exception e) {
					Log.e(mytag+"("+owner+")", "Database feature No skey for id : "+itemArray.getJSONObject(i).getInt("device_id"));
					skey = "_";
				}
				try {
					Val = itemArray.getJSONObject(i).getString("value");
				}catch (Exception e) {
					Log.e(mytag+"("+owner+")", "Database feature No Value for id : "+itemArray.getJSONObject(i).getInt("device_id")+" "+skey);
					Val = "0";
				}
				values.put("device_id", itemArray.getJSONObject(i).getInt("device_id"));
				values.put("key", skey);
				values.put("value", Val);
				context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_STATE, values);
				Log.v(mytag+"("+owner+")", "Database insert feature : "+itemArray.getJSONObject(i).getInt("device_id")+" "+skey+" "+Val);
			//} else {
				//Log.d(mytag+"("+owner+")", "Device : "+itemArray.getJSONObject(i).getInt("device_id")+" does'nt exist anymore....");
			//}
			
		}
	}


	public void insertFeatureMap(int id, int posx, int posy, String map){
		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("posx", posx);
		values.put("posy", posy);
		values.put("map", map);
		context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_FEATURE_MAP, values);
	}
	
	public void cleanFeatureMap(String map){
		ContentValues values = new ContentValues();
		values.put("map", map);
		context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_FEATURE_MAP,values);
	}



	////////////////// REQUEST



	public Entity_Area[] requestArea() {
		String[] projection = {"description", "id", "name"};
		Entity_Area[] areas=null;
		Cursor curs=null;	
		try {
			curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_AREA, projection, null, null, null);
			areas=new Entity_Area[curs.getCount()];
			int count=curs.getCount();
			for(int i=0;i<count;i++) {
				curs.moveToPosition(i);
				areas[i]=new Entity_Area(curs.getString(0),curs.getInt(1),curs.getString(2));
			}
		} catch (Exception e) {
			Log.e(mytag+"("+owner+")", "request area error");
			e.printStackTrace();
		}
		if(curs != null)
			curs.close();
		return areas;
	}


	public Entity_Room[] requestRoom(int area_id) {
		Entity_Room[] rooms=null;
		String[] projection = { "area_id", "description", "id", "name"};
		Cursor curs=null;	
		try {
			//curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_ROOM, projection, "area_id = \'"+area_id+"\'", null, null);
			curs = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_ROOM,
					projection, 
					"area_id = ?",
					new String[] { area_id+"" },
					null);
			rooms=new Entity_Room[curs.getCount()];
			int count=curs.getCount();
			for(int i=0;i<count;i++) {
				curs.moveToPosition(i);
				rooms[i]=new Entity_Room(curs.getInt(0),curs.getString(1),
						curs.getInt(2),curs.getString(3));
			}
		} catch (Exception e) {
			Log.v(mytag+"("+owner+")","Exception requesting for rooms of area_id "+area_id);
			e.printStackTrace();
		}
		curs.close();
		return rooms;
	}

	public Entity_Icon requestIcons(int reference, String name) {
		Cursor curs=null;
		Entity_Icon icon = null;
		try {
			String[] projection = { "name", "value", "reference"};
			curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_ICON, projection, "reference = ? AND name = ?" , new String[] { reference+"", name }, null);
			curs.moveToFirst();
			icon=new Entity_Icon(curs.getString(0), curs.getString(1), curs.getInt(2));
		} catch (Exception e) {
			//Log.e(mytag+"("+owner+")","request icon error for reference = "+reference+" name = "+name);
			//e.printStackTrace();
		}
		curs.close();
		return icon;
	}


	public Entity_Feature[] requestFeatures(int id, String zone) {
		Cursor curs=null;
		Entity_Feature[] features=null;
		try {
			curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ID, null, null, new String[] {id+" ", zone},"state_key ");
			features=new Entity_Feature[curs.getCount()];
			int count=curs.getCount();
			for(int i=0;i<count;i++) {
				curs.moveToPosition(i);
				features[i]=new Entity_Feature(curs.getString(0),curs.getInt(1),curs.getInt(2),curs.getString(3),curs.getString(4),curs.getString(5),curs.getString(6),curs.getString(7),curs.getString(8),curs.getString(9),curs.getString(10));
			}
		} catch (Exception e) {
			Log.e(mytag+"("+owner+")","request feature error");
			e.printStackTrace();
		}
		curs.close();
		return features;
	}


	public Entity_Map[] requestFeatures(String map){
		Cursor curs=null;
		String[] projection = {"value"};	
		Entity_Map[] features=null;
		try {
			//Log.v(mytag+"("+owner+")","Getting database features for map : "+map);
			curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_MAP, projection, 
					"table_feature_map = ?", 
					new String[] {"\'"+map+"\' "},
					null);
			features=new Entity_Map[curs.getCount()];
			int count=curs.getCount();
			Log.v(mytag+"("+owner+")",count+" Entities_Map returned for map : "+map);
			
			for(int i=0;i<count;i++) {
				curs.moveToPosition(i);
				features[i]=new Entity_Map(curs.getString(0),curs.getInt(1),curs.getInt(2),curs.getString(3),curs.getString(4),curs.getString(5),curs.getString(6),curs.getString(7),curs.getString(8),curs.getString(9),curs.getString(10),curs.getInt(12),curs.getInt(13),curs.getString(14));
			}
		} catch (Exception e) {
			Log.e(mytag+"("+owner+")","request feature_map error");
			e.printStackTrace();
		}
		curs.close();
		return features;
	}


	public Entity_Feature[] requestFeatures(){
		Cursor curs=null;
		String[] projection = {"value"};
		
		Entity_Feature[] features=null;
		try {
			Log.v(mytag+"("+owner+")","requesting features list");
			
			curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_ALL, null, null, null, null);
			features=new Entity_Feature[curs.getCount()];
			int count=curs.getCount();
			for(int i=0;i<count;i++) {
				curs.moveToPosition(i);
				features[i]=new Entity_Feature(curs.getString(0),curs.getInt(1),curs.getInt(2),curs.getString(3),curs.getString(4),curs.getString(5),curs.getString(6),curs.getString(7),curs.getString(8),curs.getString(9),curs.getString(10));
			}
		} catch (Exception e) {
			Log.e(mytag+"("+owner+")","request feature error");
			e.printStackTrace();
		}
		curs.close();
		return features;
	}


	public String requestFeatureState(int device_id, String key){
		String state = null;
		String[] projection = {"value"};
		String sortOrder = "key ";
			try {
				Cursor curs=null;
				curs = context.managedQuery(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_STATE, 
						projection, 
						"device_id = ? AND key = ?", 
						new String [] {device_id+"", key}, 
						null);
				curs.moveToPosition(0);
				if((curs != null) && (curs.getCount() != 0)) {
					state=curs.getString(0);
					curs.close();
					Log.v(mytag+"("+owner+")","Database query feature : "+ device_id+ " "+key+" value : "+state);
					
				} else {
					Log.v(mytag+"("+owner+")","Database query feature : "+ device_id+ " "+key+" not found ");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		

		return state;
	}
}
