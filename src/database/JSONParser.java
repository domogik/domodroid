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
package database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import widgets.Entity_Area;
import widgets.Entity_Feature;
import widgets.Entity_Feature_Association;
import widgets.Entity_Icon;
import widgets.Entity_Room;

public class JSONParser{
	private static Entity_Area[] listArea = null;
	private static Entity_Room[] listRoom = null;
	private static Entity_Feature[] listFeature = null;
	private static Entity_Feature_Association[] listFeatureAssociation=null;
	private static Entity_Icon[] listIcon=null;


	//Parse JSON object and create list of AREA----------------------
	public static Entity_Area[] ListArea(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("area");
		listArea = new Entity_Area[itemArray.length()];

		//parsing JSON area list
		for (int i =0; i < itemArray.length(); i++){
			listArea[i] = new Entity_Area(
					itemArray.getJSONObject(i).getString("description").toString(),
					itemArray.getJSONObject(i).getInt("id"),
					itemArray.getJSONObject(i).getString("name").toString());
		}
		return listArea;
	}



	//Parse JSON object and create list of ROOM----------------------
	public static Entity_Room[] ListRoom(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("room");
		listRoom = new Entity_Room[itemArray.length()];
		int area_id;
		
		//parsing JSON room list
		for (int i =0; i < itemArray.length(); i++){
			if(itemArray.getJSONObject(i).getString("area_id").equals(""))area_id=0;
			else area_id=itemArray.getJSONObject(i).getInt("area_id");
			listRoom[i] = new Entity_Room(
					area_id,
					itemArray.getJSONObject(i).getString("description").toString(),
					itemArray.getJSONObject(i).getInt("id"),
					itemArray.getJSONObject(i).getString("name").toString());
		}
		return listRoom;
	}


	//Parse JSON object and create list of FEATURE-------------------------
	public static Entity_Feature[] ListFeature(JSONObject json) throws JSONException{
		JSONArray itemArray = json.getJSONArray("feature");
		listFeature = new Entity_Feature[itemArray.length()];

		//parsing JSON feature list
		for (int i =0; i < itemArray.length(); i++){
			listFeature[i] = new Entity_Feature(
					itemArray.getJSONObject(i).getString("device_feature_model_id").toString(),
					itemArray.getJSONObject(i).getInt("id"),
					itemArray.getJSONObject(i).getJSONObject("device").getInt("id"),
					itemArray.getJSONObject(i).getJSONObject("device").getString("device_usage_id").toString(),
					itemArray.getJSONObject(i).getJSONObject("device").getString("address").toString(),
					itemArray.getJSONObject(i).getJSONObject("device").getString("device_type_id").toString(),
					itemArray.getJSONObject(i).getJSONObject("device").getString("description").toString(),
					itemArray.getJSONObject(i).getJSONObject("device").getString("name").toString(),
					itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("stat_key"),
					itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("parameters"),
					itemArray.getJSONObject(i).getJSONObject("device_feature_model").getString("value_type"));	
		}
		return listFeature;
	}

	//Parse JSON object and create list of FEATURE ASSOCIATION-------------------------
	public static Entity_Feature_Association[] ListFeatureAssociation(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("feature_association");
		listFeatureAssociation = new Entity_Feature_Association[itemArray.length()];

		//parsing JSON feature list
		for (int i =0; i < itemArray.length(); i++){
			listFeatureAssociation[i] = new Entity_Feature_Association(
					itemArray.getJSONObject(i).getInt("place_id"),
					itemArray.getJSONObject(i).getString("place_type"),
					itemArray.getJSONObject(i).getInt("device_feature_id"),
					itemArray.getJSONObject(i).getInt("id"),
					itemArray.getJSONObject(i).getString("device_feature"));	
		}
		return listFeatureAssociation;
	}

	//Parse JSON object, result of a request-------------------------
	public static Boolean Ack(JSONObject json) throws JSONException {	
		if(json.getString("status").equals("ERROR")){
			return false;	
		}else{
			return true;
		}
	}

	public static int StateValueINT(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("stats");
		int temp = itemArray.getJSONObject(0).getInt("value");
		return temp;
	}

	public static String StateValueSTRING(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("stats");
		String temp = itemArray.getJSONObject(0).getString("value").toString();
		return temp;
	}

	
	
	//Parse JSON object and create list of ICON-------------------------
	public static Entity_Icon[] ListIcon(JSONObject json) throws JSONException {
		JSONArray itemArray = json.getJSONArray("ui_config");
		listIcon = new Entity_Icon[itemArray.length()];

		//parsing JSON feature list
		for (int i =0; i < itemArray.length(); i++){
			listIcon[i] = new Entity_Icon(
					itemArray.getJSONObject(i).getString("name").toString(),
					itemArray.getJSONObject(i).getString("value").toString(),
					itemArray.getJSONObject(i).getInt("reference"));
		}
		return listIcon;
	}
}



