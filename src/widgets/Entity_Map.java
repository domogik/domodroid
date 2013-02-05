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

import org.json.JSONException;
import org.json.JSONObject;
import activities.Graphics_Manager;

public class Entity_Map{
	private int id;
	private JSONObject device;
	private String description;
	private String device_usage_id; 
	private String address;
	private String device_type_id;
	private int devId;
	private String name;
	private String device_feature_model_id;
	private String state_key;
	private String parameters;
	private String value_type;
	private String currentState;
	private int state;
	private int posx;
	private int posy;
	private String map;
	private Boolean isalive = true;
	
	public Entity_Map(String device_feature_model_id, int id, int devId, String device_usage_id, String address, String device_type_id, String description, String name, String state_key, String parameters, String value_type, int posx, int posy, String map) throws JSONException{
		this.device_feature_model_id = device_feature_model_id;
		this.id = id;
		this.devId = devId;
		this.device_usage_id = device_usage_id;
		this.address = address;
		this.device_type_id = device_type_id;
		this.description = description;
		this.name = name;
		this.state_key = state_key;
		this.parameters = parameters;
		this.value_type = value_type;
		this.posx = posx;
		this.posy = posy;
		this.map = map;
		this.isalive = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public JSONObject getDevice() {
		return device;
	}

	public void setDevice(JSONObject device){
		this.device = device;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDevice_usage_id() {
		return device_usage_id;
	}

	public void setDevice_usage_id(String device_usage_id) {
		this.device_usage_id = device_usage_id;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getDevId() {
		return devId;
	}

	public void setDevId(int devId) {
		this.devId = devId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDevice_feature_model_id() {
		return device_feature_model_id;
	}

	public void setDevice_feature_model_id(String device_feature_model_id) {
		this.device_feature_model_id = device_feature_model_id;
	}

	public String getState_key() {
		return state_key;
	}

	public void setState_key(String state_key) {
		this.state_key = state_key;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getValue_type() {
		return value_type;
	}

	public void setValue_type(String value_type) {
		this.value_type = value_type;
	}
	public int getRessources() {
		return Graphics_Manager.Map_Agent(getDevice_usage_id(),getState());
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}

	public String getCurrentState() {
		return currentState;
	}
	public Boolean isalive() {
		return this.isalive;
	}
	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}
	public void setalive(Boolean mode) {
		this.isalive = mode;
	}
	public String getDevice_type_id() {
		return device_type_id;
	}

	public void setDevice_type_id(String device_type_id) {
		this.device_type_id = device_type_id;
	}

	public int getPosx() {
		return posx;
	}

	public void setPosx(int posx) {
		this.posx = posx;
	}

	public int getPosy() {
		return posy;
	}

	public void setPosy(int posy) {
		this.posy = posy;
	}

	public String getMap() {
		return map;
	}

	public void setMap(String map) {
		this.map = map;
	}

}
