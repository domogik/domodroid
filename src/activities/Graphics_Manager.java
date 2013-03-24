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
package activities;

import org.domogik.domodroid.*;

import android.app.Activity;
import android.content.Context;


public class Graphics_Manager {


	public static int Icones_Agent(String usage, int state){
		switch(state){
		case 0: //Called for Off or Room
			//reorder by usage name for easy update
			if(usage.equals("air conditioning")){return R.drawable.usage_heating_off;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_off;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_off;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_off;}
			else if(usage.equals("door")){return R.drawable.usage_door_off;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_off;}
			else if(usage.equals("heating")){return R.drawable.usage_heating_off;}
			else if(usage.equals("light")){return R.drawable.usage_light_off;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_off;}
			else if(usage.equals("music")){return R.drawable.usage_music_off;}
			else if(usage.equals("nanoztag")){return R.drawable.usage_nanoztag_off;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_off;}
			else if(usage.equals("scene")){return R.drawable.usage_scene_off;}
			else if(usage.equals("security_camera")){return R.drawable.usage_security_cam_off;}
			else if(usage.equals("server")){return R.drawable.usage_server_off;}
			else if(usage.equals("socket")){return R.drawable.usage_appliance_off;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_off;}
			else if(usage.equals("telephony")){return R.drawable.usage_telephony_off;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_off;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_off;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_off;}
			else if(usage.equals("water")){return R.drawable.usage_water_off;}
			else if(usage.equals("water_tank")){return R.drawable.usage_water_tank_off;}
			else if(usage.equals("window")){return R.drawable.usage_window_off;}
			//room
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
			else if(usage.equals("usage")){return R.drawable.logo;}
			//area
			else if(usage.equals("basement")){return R.drawable.area_basement;}
			else if(usage.equals("garage")){return R.drawable.area_garage;}
			else if(usage.equals("garden")){return R.drawable.area_garden;}
			else if(usage.equals("groundfloor")){return R.drawable.area_ground_floor;}
			else if(usage.equals("firstfloor")){return R.drawable.area_first_floor;}
			else if(usage.equals("secondfloor")){return R.drawable.area_second_floor;}
			else if(usage.equals("groundfloor2")){return R.drawable.area_ground_floor;}
			else if(usage.equals("firstfloor2")){return R.drawable.area_first_floor;}
			else if(usage.equals("secondfloor2")){return R.drawable.area_second_floor;}
			else if(usage.equals("basement2")){return R.drawable.area_basement;}
			else if(usage.equals("area")){return R.drawable.area_area;}
			else if(usage.equals("house")){return R.drawable.house;}
			else if(usage.equals("map")){return R.drawable.map;}
			else if(usage.equals("statistics")){return R.drawable.statistics;}
			else return R.drawable.usage_default_off;

		case 1: //For median value (50%)
			//reorder by usage name for easy update
			if(usage.equals("air conditioning")){return R.drawable.usage_air_50;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_50;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_50;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_50;}
			else if(usage.equals("door")){return R.drawable.usage_door_50;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_50;}
			else if(usage.equals("heating")){return R.drawable.usage_heating_50;}
			else if(usage.equals("light")){return R.drawable.usage_light_50;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_50;}
			else if(usage.equals("music")){return R.drawable.usage_music_50;}
			else if(usage.equals("nanoztag")){return R.drawable.usage_nanoztag_50;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_50;}
			else if(usage.equals("scene")){return R.drawable.usage_scene_50;}
			else if(usage.equals("security_camera")){return R.drawable.usage_security_cam_50;}
			else if(usage.equals("server")){return R.drawable.usage_server_50;}
			else if(usage.equals("socket")){return R.drawable.usage_appliance_50;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_50;}
			else if(usage.equals("telephony")){return R.drawable.usage_telephony_50;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_50;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_50;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_50;}
			else if(usage.equals("water")){return R.drawable.usage_heating_50;}
			else if(usage.equals("water_tank")){return R.drawable.usage_water_tank_50;}
			else if(usage.equals("window")){return R.drawable.usage_window_50;}
			else return R.drawable.usage_default_50;

		case 2: //For on
			//reorder by usage name for easy update
			if(usage.equals("air conditioning")){return R.drawable.usage_air_on;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_on;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_on;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_on;}
			else if(usage.equals("door")){return R.drawable.usage_door_on;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_on;}
			else if(usage.equals("heating")){return R.drawable.usage_heating_on;}	//Added by Doume
			else if(usage.equals("light")){return R.drawable.usage_light_on;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_on;}
			else if(usage.equals("music")){return R.drawable.usage_music_on;}
			else if(usage.equals("nanoztag")){return R.drawable.usage_nanoztag_on;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_on;}
			else if(usage.equals("scene")){return R.drawable.usage_scene_on;}
			else if(usage.equals("security_camera")){return R.drawable.usage_security_cam_on;}
			else if(usage.equals("server")){return R.drawable.usage_server_on;}
			else if(usage.equals("socket")){return R.drawable.usage_appliance_on;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_on;}
			else if(usage.equals("telephony")){return R.drawable.usage_telephony_on;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_on;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_on;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_on;}
			else if(usage.equals("water")){return R.drawable.usage_water_on;}
			else if(usage.equals("water_tank")){return R.drawable.usage_water_tank_on;}
			else if(usage.equals("window")){return R.drawable.usage_window_on;}
			else return R.drawable.usage_default_on;
		
		case 3: //For undefined
			//reorder by usage name for easy update
			if(usage.equals("air conditioning")){return R.drawable.usage_air_undefined;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_undefined;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_undefined;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_undefined;}
			else if(usage.equals("door")){return R.drawable.usage_door_undefined;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_undefined;}
			else if(usage.equals("heating")){return R.drawable.usage_heating_undefined;}	//Added by Doume
			else if(usage.equals("light")){return R.drawable.usage_light_undefined;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_undefined;}
			else if(usage.equals("music")){return R.drawable.usage_music_undefined;}
			else if(usage.equals("nanoztag")){return R.drawable.usage_nanoztag_undefined;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_undefined;}
			else if(usage.equals("scene")){return R.drawable.usage_scene_undefined;}
			else if(usage.equals("security_camera")){return R.drawable.usage_security_cam_undefined;}
			else if(usage.equals("server")){return R.drawable.usage_server_undefined;}
			else if(usage.equals("socket")){return R.drawable.usage_appliance_undefined;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_undefined;}
			else if(usage.equals("telephony")){return R.drawable.usage_telephony_undefined;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_undefined;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_undefined;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_undefined;}
			else if(usage.equals("water")){return R.drawable.usage_water_undefined;}
			else if(usage.equals("water_tank")){return R.drawable.usage_water_tank_undefined;}
			else if(usage.equals("window")){return R.drawable.usage_window_undefined;}
			else return R.drawable.usage_default_undefined;
		}	
		return R.drawable.icon;
	}

	public static String Names_Agent(Context context, String usage){
			  int resId;
			  String result = usage;
		      String packageName = context.getPackageName();
		      String search = usage;
		      if(search.equals("air conditioning"))
		    	  search = "conditioning";
		      
		      resId = context.getResources().getIdentifier(search, "string", packageName);
		      if(resId != 0) {
		    	  try {
		    		  result = context.getString(resId);
		    	  } catch (Exception e) {
		    		  result = usage;
		    	  }
		      }
		      return result;
	}
	
	public static int Names_conditioncodes(int code){
		switch (code){
		case 0: return R.string.info0;
		case 1: return R.string.info1;
		case 2: return R.string.info2;
		case 3: return R.string.info3;
		case 4: return R.string.info4;
		case 5: return R.string.info5;
		case 6: return R.string.info6;
		case 7: return R.string.info7;
		case 8: return R.string.info8;
		case 9: return R.string.info9;
		case 10: return R.string.info10;
		case 11: return R.string.info11;
		case 12: return R.string.info12;
		case 13: return R.string.info13;
		case 14: return R.string.info14;
		case 15: return R.string.info15;
		case 16: return R.string.info16;
		case 17: return R.string.info17;
		case 18: return R.string.info18;
		case 19: return R.string.info19;
		case 20: return R.string.info20;
		case 21: return R.string.info21;
		case 22: return R.string.info22;
		case 23: return R.string.info23;
		case 24: return R.string.info24;
		case 25: return R.string.info25;
		case 26: return R.string.info26;
		case 27: return R.string.info27;
		case 28: return R.string.info28;
		
		case 29: return R.string.info29;
		case 30: return R.string.info30;
		case 31: return R.string.info31;
		case 32: return R.string.info32;
		case 33: return R.string.info33;
		case 34: return R.string.info34;
		case 35: return R.string.info35;
		case 36: return R.string.info36;
		case 37: return R.string.info37;
		case 38: return R.string.info38;
		case 39: return R.string.info39;
		case 40: return R.string.info40;
		case 41: return R.string.info41;
		case 42: return R.string.info42;	
		case 43: return R.string.info43;
		case 44: return R.string.info44;
		case 45: return R.string.info45;
		case 46: return R.string.info46;
		case 47: return R.string.info47;
		case 3200: return R.string.info3200;
		}
		return R.string.info48;
	}
	
	public static int Map_Agent(String usage, int state){
		switch(state){
		case 0:
			//reorder by usage name for easy update
			if(usage.equals("appliance")){return R.drawable.map_usage_appliance_off;}
			else if(usage.equals("christmas_tree")){return R.drawable.map_usage_christmas_tree_off;}//TODO need an on/off icon
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_off;}
			else if(usage.equals("door")){return R.drawable.map_usage_usage_door_off;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_off;}
			else if(usage.equals("light")){return R.drawable.map_usage_light_off;}
			else if(usage.equals("security_camera")){return R.drawable.map_usage_security_cam_off;}//TODO need an on/off icon
			else if(usage.equals("server")){return R.drawable.map_usage_server_off;}
			else if(usage.equals("telephony")){return R.drawable.map_usage_telephony_off;}//TODO need an on/off icon
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_off;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_off;}
			else if(usage.equals("water_tank")){return R.drawable.map_usage_water_tank;}
			else return R.drawable.map_led_off;
		case 1:
			//reorder by usage name for easy update
			if(usage.equals("appliance")){return R.drawable.map_usage_appliance_on;}
			else if(usage.equals("christmas_tree")){return R.drawable.map_usage_christmas_tree_off;}//TODO need an on/off icon
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_on;}
			else if(usage.equals("door")){return R.drawable.map_usage_usage_door_on;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_on;}
			else if(usage.equals("light")){return R.drawable.map_usage_light_on;}
			else if(usage.equals("security_camera")){return R.drawable.map_usage_security_cam_off;}//TODO need an on/off icon
			else if(usage.equals("server")){return R.drawable.map_usage_server_on;}
			else if(usage.equals("telephony")){return R.drawable.map_usage_telephony_off;}//TODO need an on/off icon
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_on;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_on;}
			else if(usage.equals("water_tank")){return R.drawable.map_usage_water_tank;}
			else return R.drawable.map_led_on;
		}
		return R.drawable.map_led_off;
	}
}
