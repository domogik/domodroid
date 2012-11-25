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

import org.domogik.domodroid.R;

public class Graphics_Manager {


	public static int Icones_Agent(String usage, int state){
		switch(state){
		case 0:
			if(usage.equals("light")){return R.drawable.usage_light_off;}
			else if(usage.equals("appliance")){return R.drawable.usage_socket_off;}
			else if(usage.equals("socket")){return R.drawable.usage_socket_off;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_off;}
			else if(usage.equals("air conditioning")){return R.drawable.usage_heating_off;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_off;}
			else if(usage.equals("water")){return R.drawable.usage_water_off;}
			else if(usage.equals("heater")){return R.drawable.usage_heating_off;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_off;}
			else if(usage.equals("server")){return R.drawable.usage_computer_off;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_off;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_off;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_off;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_off;}
			else if(usage.equals("music")){return R.drawable.usage_music_off;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_off;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_off;}
			//room
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
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
			else if(usage.equals("unknown")){return R.drawable.unknown;}
			break;

		case 1:
			if(usage.equals("light")){return R.drawable.usage_light_50;}
			else if(usage.equals("air conditioning")){return R.drawable.usage_heating_on;}
			else if(usage.equals("socket")){return R.drawable.usage_socket_50;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_on;}
			else if(usage.equals("water")){return R.drawable.usage_heating_on;}
			else if(usage.equals("heater")){return R.drawable.usage_heating_on;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_on;}
			else if(usage.equals("telephony")){return R.drawable.usage_heating_on;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_on;}
			else if(usage.equals("music")){return R.drawable.usage_music_on;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_on;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_on;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_on;}
			else if(usage.equals("appliance")){return R.drawable.usage_socket_on;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_on;}
			else if(usage.equals("server")){return R.drawable.usage_computer_on;}
			break;

		case 2:
			if(usage.equals("light")){return R.drawable.usage_light_on;}
			else if(usage.equals("temperature")){return R.drawable.usage_temperature_on;}
			else if(usage.equals("heating")){return R.drawable.usage_temperature_on;}	//Added by Doume
			else if(usage.equals("water")){return R.drawable.usage_water_on;}
			else if(usage.equals("heater")){return R.drawable.usage_heating_on;}
			else if(usage.equals("tv")){return R.drawable.usage_tv_on;}
			else if(usage.equals("telephony")){return R.drawable.usage_heating_on;}
			else if(usage.equals("mirror")){return R.drawable.usage_mirror_on;}
			else if(usage.equals("music")){return R.drawable.usage_music_on;}
			else if(usage.equals("ventilation")){return R.drawable.usage_ventilation_on;}
			else if(usage.equals("air conditioning")){return R.drawable.usage_air_on;}
			else if(usage.equals("electricity")){return R.drawable.usage_electricity_on;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_on;}
			else if(usage.equals("appliance")){return R.drawable.usage_socket_on;}
			else if(usage.equals("socket")){return R.drawable.usage_socket_on;}
			else if(usage.equals("shutter")){return R.drawable.usage_shutter_on;}
			else if(usage.equals("server")){return R.drawable.usage_computer_on;}
			else if(usage.equals("portal")){return R.drawable.usage_portal_on;}
			else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_on;}
			break;	
		}	
		return R.drawable.icon;
	}

	public static int Map_Agent(String usage, int state){
		switch(state){
		case 0:
			if(usage.equals("light")){return R.drawable.map_usage_light_off;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_off;}
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_off;}
			else if(usage.equals("server")){return R.drawable.map_usage_server_off;}
			else if(usage.equals("appliance")){return R.drawable.map_usage_appliance_off;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_off;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_off;}
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else return R.drawable.map_led_off;
		case 1:
			if(usage.equals("light")){return R.drawable.map_usage_light_on;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_on;}
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_on;}
			else if(usage.equals("server")){return R.drawable.map_usage_server_on;}
			else if(usage.equals("appliance")){return R.drawable.map_usage_appliance_on;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_on;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_on;}
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else return R.drawable.map_led_on;
		}
		return R.drawable.map_led_off;
	}
}
