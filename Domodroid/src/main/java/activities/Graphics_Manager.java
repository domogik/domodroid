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

import org.domogik.domodroid13.*;

import android.app.Activity;
import android.content.Context;


public class Graphics_Manager {


	public static int Icones_Agent(String usage, int state){
		usage=adapt_usage(usage);
		switch(state){
		case 0: //Called for Off or Room
			//reorder by usage name for easy update
			if(usage.equals("air_conditionning")){return R.drawable.usage_air_off;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_off;}
			else if(usage.equals("battery")){return R.drawable.usage_battery_off;}
            else if(usage.equals("bluetooth")){return R.drawable.usage_bluetooth_off;}
            else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_off;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_off;}
			else if(usage.equals("cron")){return R.drawable.usage_cron_off;}
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
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
			else if(usage.equals("usage")){return R.drawable.logo;}
			else if(usage.equals("unknow")){return R.drawable.unknown;}
			//area
			else if(usage.equals("area")){return R.drawable.area_area;}
			else if(usage.equals("attic")){return R.drawable.area_attic;}
			else if(usage.equals("basement")){return R.drawable.area_basement;}
			else if(usage.equals("basement2")){return R.drawable.area_basement2;}
			else if(usage.equals("garage")){return R.drawable.area_garage;}
			else if(usage.equals("garden")){return R.drawable.area_garden;}
			else if(usage.equals("ground_floor")){return R.drawable.area_ground_floor;}
			else if(usage.equals("ground_floor2")){return R.drawable.area_ground_floor2;}
			else if(usage.equals("first_floor")){return R.drawable.area_first_floor;}
			else if(usage.equals("first_floor2")){return R.drawable.area_first_floor2;}
			else if(usage.equals("second_floor")){return R.drawable.area_second_floor;}
			else if(usage.equals("second_floor2")){return R.drawable.area_second_floor2;}
			else if(usage.equals("house")){return R.drawable.house;}
			else if(usage.equals("map")){return R.drawable.map;}
			else return R.drawable.usage_default_off;

		case 1: //For median value (50%)
			//reorder by usage name for easy update
			if(usage.equals("air_conditionning")){return R.drawable.usage_air_50;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_50;}
			else if(usage.equals("battery")){return R.drawable.usage_battery_50;}
            //Todo add bluetooth 50
            else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_50;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_50;}
			else if(usage.equals("cron")){return R.drawable.usage_cron_50;}
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
			//room
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
			else if(usage.equals("usage")){return R.drawable.logo;}
			else if(usage.equals("unknow")){return R.drawable.unknown;}
			//area
			else if(usage.equals("area")){return R.drawable.area_area;}
			else if(usage.equals("attic")){return R.drawable.area_attic;}
			else if(usage.equals("basement")){return R.drawable.area_basement;}
			else if(usage.equals("basement2")){return R.drawable.area_basement2;}
			else if(usage.equals("garage")){return R.drawable.area_garage;}
			else if(usage.equals("garden")){return R.drawable.area_garden;}
			else if(usage.equals("ground_floor")){return R.drawable.area_ground_floor;}
			else if(usage.equals("ground_floor2")){return R.drawable.area_ground_floor2;}
			else if(usage.equals("first_floor")){return R.drawable.area_first_floor;}
			else if(usage.equals("first_floor2")){return R.drawable.area_first_floor2;}
			else if(usage.equals("second_floor")){return R.drawable.area_second_floor;}
			else if(usage.equals("second_floor2")){return R.drawable.area_second_floor2;}
			else if(usage.equals("house")){return R.drawable.house;}
			else if(usage.equals("map")){return R.drawable.map;}
			else return R.drawable.usage_default_50;

		case 2: //For on
			//reorder by usage name for easy update
			if(usage.equals("air_conditionning")){return R.drawable.usage_air_on;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_on;}
			else if(usage.equals("battery")){return R.drawable.usage_battery_on;}
            else if(usage.equals("bluetooth")){return R.drawable.usage_bluetooth_on;}
            else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_on;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_on;}
			else if(usage.equals("cron")){return R.drawable.usage_cron_on;}
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
			//room
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
			else if(usage.equals("usage")){return R.drawable.logo;}
			else if(usage.equals("unknow")){return R.drawable.unknown;}
			//area
			else if(usage.equals("area")){return R.drawable.area_area;}
			else if(usage.equals("attic")){return R.drawable.area_attic;}
			else if(usage.equals("basement")){return R.drawable.area_basement;}
			else if(usage.equals("basement2")){return R.drawable.area_basement2;}
			else if(usage.equals("garage")){return R.drawable.area_garage;}
			else if(usage.equals("garden")){return R.drawable.area_garden;}
			else if(usage.equals("ground_floor")){return R.drawable.area_ground_floor;}
			else if(usage.equals("ground_floor2")){return R.drawable.area_ground_floor2;}
			else if(usage.equals("first_floor")){return R.drawable.area_first_floor;}
			else if(usage.equals("first_floor2")){return R.drawable.area_first_floor2;}
			else if(usage.equals("second_floor")){return R.drawable.area_second_floor;}
			else if(usage.equals("second_floor2")){return R.drawable.area_second_floor2;}
			else if(usage.equals("house")){return R.drawable.house;}
			else if(usage.equals("map")){return R.drawable.map;}
			else return R.drawable.usage_default_on;

		case 3: //For undefined
			//reorder by usage name for easy update
			if(usage.equals("air_conditionning")){return R.drawable.usage_air_undefined;}
			else if(usage.equals("appliance")){return R.drawable.usage_appliance_undefined;}
            else if(usage.equals("battery")){return R.drawable.usage_battery_undefnied;}
            else if(usage.equals("bluetooth")){return R.drawable.usage_bluetooth_undefined;}
            else if(usage.equals("christmas_tree")){return R.drawable.usage_christmas_tree_undefined;}
			else if(usage.equals("computer")){return R.drawable.usage_computer_undefined;}
			else if(usage.equals("cron")){return R.drawable.usage_cron_undefined;}
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
			//room
			else if(usage.equals("bathroom")){return R.drawable.room_bathroom;}
			else if(usage.equals("bedroom")){return R.drawable.room_bedroom;}
			else if(usage.equals("garage")){return R.drawable.room_garage;}
			else if(usage.equals("kidsroom")){return R.drawable.room_kidsroom;}
			else if(usage.equals("kitchen")){return R.drawable.room_kitchen;}
			else if(usage.equals("office")){return R.drawable.room_office;}
			else if(usage.equals("tvlounge")){return R.drawable.room_tvlounge;}
			else if(usage.equals("usage")){return R.drawable.logo;}
			else if(usage.equals("unknow")){return R.drawable.unknown;}
			//area
			else if(usage.equals("area")){return R.drawable.area_area;}
			else if(usage.equals("attic")){return R.drawable.area_attic;}
			else if(usage.equals("basement")){return R.drawable.area_basement;}
			else if(usage.equals("basement2")){return R.drawable.area_basement2;}
			else if(usage.equals("garage")){return R.drawable.area_garage;}
			else if(usage.equals("garden")){return R.drawable.area_garden;}
			else if(usage.equals("ground_floor")){return R.drawable.area_ground_floor;}
			else if(usage.equals("ground_floor2")){return R.drawable.area_ground_floor2;}
			else if(usage.equals("first_floor")){return R.drawable.area_first_floor;}
			else if(usage.equals("first_floor2")){return R.drawable.area_first_floor2;}
			else if(usage.equals("second_floor")){return R.drawable.area_second_floor;}
			else if(usage.equals("second_floor2")){return R.drawable.area_second_floor2;}
			else if(usage.equals("house")){return R.drawable.house;}
			else if(usage.equals("map")){return R.drawable.map;}
			else return R.drawable.usage_default_undefined;
		}
		return R.drawable.icon;
	}


	public static String Names_Agent(Context context, String usage){
		//Use to translate value in current language
		//For example if an a room is named kitchen,
		//in French The text display will be cuisine
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

	public static int Names_conditioncodes(Context context,int code){
		//used to get the translate text from yahoo weather infocode.
		try{
			return context.getResources().getIdentifier("info"+code, "string", context.getPackageName());
		} catch (Exception e){
			return R.string.info48;
		}
	}

	public static int Map_Agent(String usage, int state){
		usage=adapt_usage(usage);
		switch(state){
		case 0:
			//reorder by usage name for easy update
			/*
			 * TODO add missing usage
			 * air_conditionning
			 * heating
			 * mirror
			 * music
			 * nanoztag
			 * portal
			 * scene
			 * shutter
			 * water
			 * window
			 */
			/*
			try{
				return context.getResources().getIdentifier("map_usage_"+usage+"_off", "drawable", context.getPackageName());
			} catch (Exception e){
				return R.drawable.map_led_off;
			}
			 */
			if(usage.equals("appliance")){return R.drawable.map_usage_appliance_off;}
            else if(usage.equals("battery")){return R.drawable.map_usage_battery_off;}
            else if(usage.equals("christmas_tree")){return R.drawable.map_usage_christmas_tree_off;}
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_off;}
			else if(usage.equals("cron")){return R.drawable.map_usage_cron_off;}
			else if(usage.equals("door")){return R.drawable.map_usage_door_off;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_off;}
			else if(usage.equals("light")){return R.drawable.map_usage_light_off;}
			else if(usage.equals("security_camera")){return R.drawable.map_usage_security_cam_off;}
			else if(usage.equals("server")){return R.drawable.map_usage_server_off;}
			else if(usage.equals("socket")){return R.drawable.map_usage_appliance_off;}
			else if(usage.equals("telephony")){return R.drawable.map_usage_telephony_off;}//TODO need an on/off icon
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_off;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_off;}
			else if(usage.equals("water_tank")){return R.drawable.map_usage_water_tank;}
			else return R.drawable.map_led_off;

		case 1:
			//reorder by usage name for easy update
			if(usage.equals("appliance")){return R.drawable.map_usage_appliance_on;}
            else if(usage.equals("battery")){return R.drawable.map_usage_battery_on;}
            else if(usage.equals("christmas_tree")){return R.drawable.map_usage_christmas_tree_on;}
			else if(usage.equals("computer")){return R.drawable.map_usage_computer_on;}
			else if(usage.equals("cron")){return R.drawable.map_usage_cron_on;}
			else if(usage.equals("door")){return R.drawable.map_usage_door_on;}
			else if(usage.equals("electricity")){return R.drawable.map_usage_electricity_on;}
			else if(usage.equals("light")){return R.drawable.map_usage_light_on;}
			else if(usage.equals("security_camera")){return R.drawable.map_usage_security_cam_on;}
			else if(usage.equals("server")){return R.drawable.map_usage_server_on;}
			else if(usage.equals("socket")){return R.drawable.map_usage_appliance_on;}
			else if(usage.equals("telephony")){return R.drawable.map_usage_telephony_off;}//TODO need an on/off icon
			else if(usage.equals("temperature")){return R.drawable.map_usage_temperature;}
			else if(usage.equals("tv")){return R.drawable.map_usage_tv_on;}
			else if(usage.equals("ventilation")){return R.drawable.map_usage_ventilation_on;}
			else if(usage.equals("water_tank")){return R.drawable.map_usage_water_tank;}
			else return R.drawable.map_led_on;
		}
		return R.drawable.map_led_off;
	}

	public static int getStringIdentifier(Context context, String name) {
		//To avoid space or - in name in strings.xml
		name=name.replace(" ", "_");
		name=name.replace("-", "_");
		name=name.replace(":", "_");
		//To get a drawable R.Drawable
		//context.getResources().getIdentifier(name, "drawable", context.getPackageName());
		//To get a string from R.String
		return context.getResources().getIdentifier(name, "string", context.getPackageName());
	}

	public static String adapt_usage(String usage) {
		//TODO adapt for 0.4
		//information are in json device_types of each plugin
		//BLUEZ "available"
        if (usage.equals("available")||usage.contains("bluez"))
            usage="bluetooth";
        //CID "callerid"
		if (usage.contains("callerid"))
			usage="telephony";
		//DAIKCODE "set_power", "set_setpoint", "set_mode", "set_vertical_swing", "set_horizontal_swing",
		//"set_speedfan", "set_powerfull", "set_silent", "set_home_leave", "set_sensor",
		//"set_start_time", "set_stop_time", "power", "vertical_swing", "horizontal_swing", "powerfull"
		//"silent", "home_leave", "sensor", "setpoint", "setmode", "speedfan", "starttime", "stoptime"
		if (usage.contains("swing")||usage.contains("fan"))
			usage="ventilation";
		//DISKFREE "get_total_space", "get_percent_used", "get_free_space", "get_used_space"
		if (usage.contains("diskfree")||usage.equals("get_total_space")||usage.equals("get_percent_used")
				||usage.equals("get_free_space")||usage.equals("get_used_space"))
			usage="server";
		//GENERIC "temperature", "humidity", "rgb_color", "rgb_command", "osd_command", "osd_text", "osd_row", "osd_column", "osd_delay"
		//GEOLOC "position_degrees"
		//IPX800 "state", "input", "count"
		//IRTRANS "send_bintimings", "send_raw", "send_hexa", "code_ir","ack_ir_cmd"
		//K8056 "sensor_switch_relay", "cmd_switch_relay"
		//MQTT "sensor_temperature", "sensor_humidity", "sensor_battery", "sensor_luminosity","sensor_pressure"
		//"sensor_power", "sensor_energy", "sensor_water", "sensor_count", "sensor_uv", "sensor_windspeed"
		//"sensor_rainfall", "sensor_outflow", "sensor_voltage", "sensor_current",
        if (usage.equals("sensor_battery"))
            usage="battery";
        if (usage.contains("power")||usage.contains("energy")||usage.contains("voltage")||usage.equals("sensor_current"))
			usage="electricity";
		if (usage.equals("sensor_rainfall")||usage.equals("sensor_water"))
			usage="water";
		//NOTIFY "msg_status", "error_send"
		//NUTSERVE "test_battery_start", "test_battery_start_deep", "ups_status", "ups_event", "input_voltage", "output_voltage"
		//"battery_voltage", "battery_charge", "ack_command",
		//ONEWIRE "temperature", "humidity", "serial", "gpio",
        if (usage.equals("test_battery_start")||usage.equals("test_battery_start_deep")||usage.equals("battery_voltage")||usage.equals("battery_charge"))
            usage="battery";
        if (usage.contains("thermometer"))
			usage="temperature";
		//PING "ping"
		if (usage.contains("ping"))
			usage="computer";
		//SCRIPT "sensor_script_action", "sensor_script_info", "run_script_action", "run_script_info"
		if (usage.contains("script"))
			usage="scene";
		//TELEINFO "adco", "optarif", "isousc", "base", "iinst", "imax", "motdetat", "hchc", "hchp"
		//"ptec", "papp", "hhphc", "iinst1", "iinst2", "iinst3", "imax1", "imax2", "imax3", "adps"
		//"ejphn", "ejphpm", "pejp", "bbrhcjb", "bbrhpjb", "bbrhcjw", "bbrhpjw", "bbrhcjr", "bbrhpjr"
		if (usage.contains("teleinfo")||usage.equals("adco")||usage.equals("optarif")||usage.equals("isousc")||usage.equals("base")
				||usage.equals("iinst")||usage.equals("imax")||usage.equals("motdetat")
				||usage.equals("hchc")||usage.equals("hchp")||usage.equals("ptec")
				||usage.equals("papp")||usage.equals("hhphc")||usage.equals("iinst1")
				||usage.equals("iinst2")||usage.equals("iinst3")||usage.equals("imax1")
				||usage.equals("imax2")||usage.equals("imax3")||usage.equals("adps")
				||usage.equals("ejphn")||usage.equals("ejphpm")||usage.equals("pejp")
				||usage.equals("bbrhcjb")||usage.equals("bbrhpjb")||usage.equals("bbrhcjw")
				||usage.equals("bbrhpjw")||usage.equals("bbrhcjr")||usage.equals("bbrhpjr")
				)
			usage="electricity";
		//RFXCOM "temperature", "humidity", "battery", "rssi", "switch_lighting_2", "rssi_lighting_2","open_close", "rssi_open_close"
        if (usage.equals("battery"))
            usage="battery";
        if (usage.contains("lighting"))
			usage="light";
		if (usage.contains("open_close"))
			usage="door";
		//VELBUS "level_bin", "level_range", "temp", "power", "energy", "input"
		if (usage.equals("temp"))
			usage="temperature";
		//WEATHER "current_barometer_value", "current_feels_like", "current_humidity", "current_last_updated", "current_station", "current_temperature"
		//"current_text", "current_code", "current_visibility","current_wind_direction", "current_wind_speed", "current_sunrise", "current_sunset"
		//"forecast_0_day", "forecast_0_temperature_high", "forecast_0_temperature_low", "forecast_0_condition_text", "forecast_0_condition_code"
		//"forecast_1_day", "forecast_1_temperature_high", "forecast_1_temperature_low", "forecast_1_condition_text", "forecast_1_condition_code"
		//"forecast_2_day", "forecast_2_temperature_high", "forecast_2_temperature_low", "forecast_2_condition_text", "forecast_2_condition_code"
		//"forecast_3_day", "forecast_3_temperature_high", "forecast_3_temperature_low", "forecast_3_condition_text", "forecast_3_condition_code"
		//"forecast_4_day","forecast_4_temperature_high", "forecast_4_temperature_low","forecast_4_condition_text","forecast_4_condition_code"
		if (usage.contains("temperature")||usage.equals("current_feels_like"))
			usage="temperature";
		if (usage.contains("humidity"))
			usage="water";
		//TODO change this on with a sun up and down icon
		if (usage.contains("current_sunrise")||usage.equals("current_sunset"))
			usage="cron";
		//WOL "wol"
		if (usage.equals("wol"))
			usage="computer";
		//ZWAVE "ctrl_status", "switch_state", "switch_state", "energy", "power", "switch_state", "energy", "energy_k", "power",
		//"opening_sensor", "power_applied", "battery_level", "low_battery", "tamper_event", "temperature_c", "battery_level", "humidity", "relative_humidity"
		//"level", "motion_sensor_level", "luminance", "sensor_alarm", "thermostat_setpoint"
        if (usage.equals("battery_level")||usage.equals("low_battery")||usage.equals("battery_level"))
            usage="battery";
        if (usage.contains("thermostat"))
			usage="temperature";
		if (usage.contains("opening_sensor")||usage.contains("opening.sensor"))
			usage="door";
		return usage;

	}

}
