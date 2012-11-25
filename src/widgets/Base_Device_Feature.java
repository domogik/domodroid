package widgets;

import java.util.Arrays;
import java.util.List;


public class Base_Device_Feature{
	public List<String> id_switch = Arrays.asList(
			"x10.switch.switch",
			"x10.dimmer.switch",
			"plcbus.switch.switch",
			"plcbus.dimmer.switch",
			"relayboard.relay.switch",
			"zwave.switch.switch",
			"knx.switch.switch"
	);

	public List<String> id_dimmer = Arrays.asList(
			"plcbus.dimmer.dim",
			"zwave.dimmer.change"
	);

	public List<String> id_info = Arrays.asList(
			"onewire.thermometer.temperature",
			"online_service.weather.temperature",
			"online_service.weather.pressure",
			"online_service.weather.humidity",
			"online_service.weather.visibility",
			"online_service.weather.rising",
			"online_service.weather.chill",
			"online_service.weather.direction",
			"online_service.weather.speed",
			"online_service.weather.uv",
			"online_service.weather.rainfall",
			"rfxcom.th2.temperature",
			"rfxcom.th2.humidity",
			"rfxcom.th2.humidity-desc",
			"rfxcom.th2.battery",
			"rfxcom.wind2.gust",
			"rfxcom.wind2.average-speed",
			"rfxcom.wind2.direction",
			"rfxcom.wind2.battery",
			"rfxcom.uv2.uv",
			"rfxcom.uv2.battery",
			"rfxcom.rain2.rainrate",
			"rfxcom.rain2.raintotal",
			"rfxcom.rain2.battery",
			"onewire.temperature_and_humidity.temperature",
			"onewire.temperature_and_humidity.humidity",
			"rfxcom.temp.battery",
			"rfxcom.th.temperature",
			"rfxcom.th.humidity",
			"rfxcom.th.humidity-desc",
			"rfxcom.th.battery",
			"rfxcom.thb.temperature",
			"rfxcom.thb.humidity",
			"rfxcom.thb.humidity-desc",
			"rfxcom.thb.pressure",
			"rfxcom.thb.forcast",
			"rfxcom.thb.battery",
			"rfxcom.wind.gust",
			"rfxcom.wind.average-speed",
			"rfxcom.wind.direction",
			"rfxcom.wind.battery",
			"rfxcom.uv.uv",
			"rfxcom.uv.battery",
			"rfxcom.rain.rainrate",
			"rfxcom.rain.raintotal",
			"rfxcom.rain.battery",
			"rfxcom.weight.weight",
			"rfxcom.elec1.current",
			"rfxcom.elec2.power",
			"rfxcom.elec2.energy",
			"rfxcom.rfxsensor.temperature",
			"rfxcom.rfxsensor.voltage",
			"rfxcom.rfxmeter.count",
			"rfxcom.digimax.temperature",
			"rfxcom.digimax.setpoint",
			"rfxcom.remote.keys"
	);
}

