package activities;

import java.util.ArrayList;
import org.domogik.domodroid13.R;
import rinor.Rest_com;
import database.Cache_management;
import database.DomodroidDB;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import widgets.Entity_Feature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Dialog_Synchronize extends Dialog implements OnClickListener {
	private Button cancelButton;
	private TextView message;
	private String urlAccess;
	private SharedPreferences.Editor prefEditor;
	private static Handler handler = null;
	private SharedPreferences params;
	private LoadConfig sync;
	public Boolean need_refresh = false;
	private Activity context;
	public Boolean reload = false;
	private DomodroidDB db = null;
	private tracerengine Tracer = null;
	private String login;
	private String password;
	private String mytag="Dialog_Synchronize";
	
	public Dialog_Synchronize(tracerengine Trac, Activity context, SharedPreferences params) {
		super(context);
		this.context = context;
		this.Tracer = Trac;
		this.params = params;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_synchronize);
		message = (TextView) findViewById(R.id.message);
		cancelButton = (Button) findViewById(R.id.CancelButton);
		cancelButton.setOnClickListener(this);
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		handler = new  Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					String loc_Value = msg.getData().getString("message");
					if(loc_Value.equals("sync_done")) {
						sync.cancel(true);
						dismiss();
						
						return;
					}
				} catch (Exception e) {}
						
				message.setText("Connection error");
				
			}
		};		
	}

	
	public void onClick(View v) {
		if (v == cancelButton)
			need_refresh = false;
			sync.cancel(true);
			dismiss();
	}
	
	public void setParams(SharedPreferences params) {
		this.params = params;
	}
	
	public void startSync(){
		sync = new LoadConfig();
		sync.execute();
	}
	
	public class LoadConfig extends AsyncTask<Void, Integer, Void>{
		private boolean sync=false;

		public LoadConfig() {
			super();
			prefEditor=params.edit();
			urlAccess = params.getString("rinor_IP","1.1.1.1")+":"+params.getString("rinorPort","40405")+params.getString("rinorPath","/");
			urlAccess = urlAccess.replaceAll("[\r\n]+", "");
			//Try to solve #1623
			urlAccess = urlAccess.replaceAll(" ", "%20");
			String format_urlAccess;
			//add a '/' at the end of the IP address
			if(urlAccess.lastIndexOf("/")==urlAccess.toString().length()-1)
				format_urlAccess = urlAccess;
			else
				format_urlAccess = urlAccess.concat("/");
			prefEditor.putString("URL",format_urlAccess);
			prefEditor.commit();
			urlAccess = params.getString("URL","1.1.1.1");
			if(db == null)
				db = new DomodroidDB(Tracer, context);
		}

		@Override
		protected void onPreExecute() {
			message.setText("Loading Configuration... 0%");
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			if(sync){
				Intent reload = new Intent(context,Activity_Main.class);
				context.startActivity(reload);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			message.setText("Loading Configuration... "+values[0]+"%");
			super.onProgressUpdate(values);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			//Requests
			
			try{
				
				// if API rinor >0.5 génération auto sinon classic
				JSONObject json_rinor = null;
				try {
					json_rinor = Rest_com.connect_jsonobject(urlAccess,login,password);
				} catch (Exception e) {
					json_rinor = null;
				}
				if(json_rinor == null) {
					//Cannot connect to server...
					handler.sendEmptyMessage(0);
					return null;
				}
				String Rinor_Api_ver=new String();
				try {
					Rinor_Api_ver = json_rinor.getJSONObject("info").getString("REST_API_version");
				} catch (Exception e){
					try{
						Rinor_Api_ver = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("REST_API_version");
					}catch (Exception e1){
						Tracer.e(mytag, "ERROR getting Rest version");
					}
				}
				Tracer.d(mytag, "RinorAPI= "+Rinor_Api_ver);
				Float Rinor_Api_Version =Float.valueOf(Rinor_Api_ver);
				String domogik_Version =new String();
				try{
					domogik_Version = json_rinor.getJSONObject("info").getString("Domogik_version");
				}catch (Exception e){
					domogik_Version = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("Domogik_version");	
				}
				Tracer.d(mytag, "domogik_Version= "+domogik_Version);
				
				JSONObject json_AreaList = null;
				JSONObject json_RoomList = null;
				JSONObject json_FeatureList = null;
				JSONObject device_feature1 = null;
				JSONArray json_FeatureList1 = null;
				JSONObject json_Sensors=null;
				JSONObject json_Commands=null;
				JSONObject json_FeatureAssociationList = null;
				JSONObject json_IconList = null;
				Tracer.d(mytag, "urlAccess = <"+urlAccess+">");
				
				db.updateDb();		//Erase all tables contents EXCEPT maps coordinates !
				
				if(Rinor_Api_Version <=0.5f){
					json_AreaList = Rest_com.connect_jsonobject(urlAccess+"base/area/list/",login,password);
					if(json_AreaList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					Tracer.d(mytag, "AreaList = <"+json_AreaList.toString()+">");
					
					publishProgress(20);
					json_RoomList = Rest_com.connect_jsonobject(urlAccess+"base/room/list/",login,password);
					if(json_RoomList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					//Tracer.d(mytag, "RoomList = <"+json_RoomList.toString()+">");
					
					publishProgress(40);
					json_FeatureList = Rest_com.connect_jsonobject(urlAccess+"base/feature/list",login,password);
					if(json_FeatureList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					
					publishProgress(60);
					json_FeatureAssociationList = Rest_com.connect_jsonobject(urlAccess+"base/feature_association/list/",login,password);
					if(json_FeatureAssociationList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(80);
					json_IconList = Rest_com.connect_jsonobject(urlAccess+"base/ui_config/list/",login,password);
					if(json_IconList == null) {
						//Cannot connect to server...
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(100);
					
				}else if (Rinor_Api_Version <= 0.6f){
					// Fonction special Basilic domogik 0.3
					json_FeatureList = Rest_com.connect_jsonobject(urlAccess+"base/feature/list",login,password);
					if(json_FeatureList == null) {
						// Cannot connect to Rinor server.....
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(25);

					//TODO grab area,room,feature and feature_assotiation from previous sync if exists.
					//Avoiding the user organization to be lost
					
					//Create JSONObject
					json_RoomList = new JSONObject();
					json_FeatureAssociationList = new JSONObject();
					json_AreaList = new JSONObject();
					JSONObject map_area = new JSONObject();
					JSONObject area = new JSONObject();
					//Create JSONArray
					JSONArray list = new JSONArray();
					JSONArray rooms = new JSONArray();
					JSONArray ListFeature = new JSONArray();
	                //Create string
					String usage = new String();
					//Create an ArrayList
					ArrayList<String> list_usage = new ArrayList<String>();
										
					json_AreaList.put("status","OK");
					json_AreaList.put("code",0);
					json_AreaList.put("description","None");
					map_area.put("description", "");
					map_area.put("id", "1");
					map_area.put("name", "Usage");
					list.put(map_area);
					json_AreaList.put("area",list);
					publishProgress(45);
					
					json_RoomList.put("status","OK");
					json_RoomList.put("code",0);
					json_RoomList.put("description","None");
					area.put("description","");
					area.put("id","1");
					area.put("name","Usage");
					
					int j=2;
					json_FeatureAssociationList.put("status","OK");
					json_FeatureAssociationList.put("code","0");
					json_FeatureAssociationList.put("description","");
	                publishProgress(55);
					
	                int list_size = 0;
	                if(json_FeatureList != null)
	                	list_size = json_FeatureList.getJSONArray("feature").length();
	                Tracer.d(mytag,"Features list size = "+list_size);
					//correct a bug #2020 if device is empty.
					for(int i = 0; i < list_size; i++) {
						try {
						
							usage = json_FeatureList.getJSONArray("feature").getJSONObject(i)
									.getJSONObject("device").getString("device_usage_id");
						} catch (Exception e) {
							usage=null;
							// Cannot parse JSON Array or JSONObject
							 Tracer.d(mytag,"Exception processing Features list ("+i+")");
						}
						 Tracer.d(mytag,"Features list processing usage = <"+usage+">");
							
						// Create a pseudo 'room' for each usage returned by Rinor
						if (usage != null) {
							if(! list_usage.contains(usage)){
								if(json_FeatureList.getJSONArray("feature").length() > 0) {
									publishProgress(100*i/json_FeatureList.getJSONArray("feature").length());
									JSONObject room = new JSONObject();
									room.put("area_id","1");
									room.put("description","");
	
									room.put("area",area);
									room.put("id",j);
									j++;
									room.put("name",json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
									rooms.put(room);
									list_usage.add(json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
								}
							}
							// And its associated widget
							JSONObject Widget = new JSONObject();
							Widget.put("place_type","room");
							Widget.put("place_id",list_usage.indexOf( 
									json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"))+2); //id_rooms);
							Widget.put("device_feature_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							Widget.put("id",50+i);
							JSONObject device_feature = new JSONObject();
							device_feature.put("device_feature_model_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_feature_model_id"));
							device_feature.put("id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
							device_feature.put("device_id",json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_id"));
							Widget.put("device_feature", device_feature);
							ListFeature.put(Widget);
						}
						
					} // for loop on feature list...
					
					//TODO load existing room from prefeditor(room) and grab rooms as JSONArray
					//The put method replace value by new one so loading previous version will
					//avoid lost of user organization
					//rooms.put(prefEditor.getString("ROOM_LIST");

					//Prepare list of rooms, and list of usable features
					json_RoomList.put("room", rooms);
					json_FeatureAssociationList.put("feature_association",ListFeature);
					
				}else if (Rinor_Api_Version <= 0.7f){
					//TODO lot of work on this.
					// Fonction special Domogik 0.4
					//get value from rinor.
					try{
						String MQaddress = json_rinor.getJSONObject("mq").getString("ip");
						String MQsubport = json_rinor.getJSONObject("mq").getString("sub_port");
						String MQpubport = json_rinor.getJSONObject("mq").getString("pub_port");
					prefEditor.putString("MQaddress", MQaddress);
					prefEditor.putString("MQsubport", MQsubport);
					prefEditor.putString("MQpubport", MQpubport);
					}catch (Exception e1){
					    Toast.makeText(context, "Problem with MQ information", Toast.LENGTH_LONG).show();
					    Toast.makeText(context, "Check server part in Option", Toast.LENGTH_LONG).show();
					    Tracer.e(mytag, "ERROR getting MQ information");
					}
					json_FeatureList1 = Rest_com.connect_jsonarray(urlAccess+"device",login,password);
					JSONObject Json_data_type = new JSONObject();
					Json_data_type = Rest_com.connect_jsonobject(urlAccess+"datatype",login,password);
					if(json_FeatureList1 == null) {
						// Cannot connect to Rinor server.....
						Tracer.d(mytag, "Cannot connect to Rinor server.....");
						handler.sendEmptyMessage(0);
						return null;
					}
					if(Json_data_type == null) {
						// Cannot get data_type from Rinor server.....
						Tracer.d(mytag, "Cannot get data_type from Rinor server.....");
						handler.sendEmptyMessage(0);
						return null;
					}
					publishProgress(25);
					Tracer.d(mytag, "connected to a 0.4 domogik Version");

					//TODO grab area,room,feature and feature_assotiation from previous sync if exists.
					//Avoiding the user organization to be lost
					
					//Create JSONObject
					json_RoomList = new JSONObject();
					json_IconList = new JSONObject();
					json_FeatureAssociationList = new JSONObject();
					json_AreaList = new JSONObject();
					JSONObject map_area = new JSONObject();
					JSONObject area = new JSONObject();
					//Create JSONArray
					JSONArray list = new JSONArray();
					JSONArray rooms = new JSONArray();
					JSONArray icons= new JSONArray();
					JSONArray ListFeature = new JSONArray();
	                //Create string
					String usage = new String();
					//Create an ArrayList
					ArrayList<String> list_usage = new ArrayList<String>();
										
					json_AreaList.put("status","OK");
					json_AreaList.put("code",0);
					json_AreaList.put("description","None");
					map_area.put("description", "");
					map_area.put("id", "1");
					map_area.put("name", "Usage");
					list.put(map_area);
					json_AreaList.put("area",list);
					publishProgress(45);
					
					json_RoomList.put("status","OK");
					json_RoomList.put("code",0);
					json_RoomList.put("description","None");
					
					json_IconList.put("status","OK");
					json_IconList.put("code",0);
					json_IconList.put("description","None");
					
					area.put("description","");
					area.put("id","1");
					area.put("name","Usage");
															
					int j=2;
					int k=50;
					json_FeatureAssociationList.put("status","OK");
					json_FeatureAssociationList.put("code","0");
					json_FeatureAssociationList.put("description","");
	                publishProgress(55);
					
	                int list_size = 0;
	                if(json_FeatureList1 != null)
	                	list_size = json_FeatureList1.length();
	                Tracer.d(mytag,"Device list size = "+list_size);
					for(int i = 0; i < list_size; i++) {
						int list_sensors = 0;
						//List sensors for this device
		                json_Sensors=json_FeatureList1.getJSONObject(i).getJSONObject("sensors");
						if(json_Sensors != null)
		                	list_sensors = json_Sensors.length();
		                Tracer.d(mytag,list_sensors+" sensors for device id "+ json_FeatureList1.getJSONObject(i).getString("id") );
		                JSONArray listsensor = json_Sensors.names();
		                for(int y = 0; y < list_sensors; y++) {
							try {
								//TODO reorder for the moment it his done by data_Type
								usage = json_FeatureList1.getJSONObject(i).getString("name");
							} catch (Exception e) {
								usage=null;
								// Cannot parse JSON Array or JSONObject
								 Tracer.d(mytag,"Exception processing sensor list ("+y+")");
							}
							 Tracer.d(mytag,"Features list processing usage = "+usage);
								
							// Create a pseudo 'room' for each usage returned by Rinor
							//TODO prepare icon_table for room base on device_type_id in 0.4
							if (usage != null) {
								if(! list_usage.contains(usage)){
									if(json_Sensors.length() > 0) {
										publishProgress(55+(45*y/json_Sensors.length()));
										JSONObject room = new JSONObject();
										JSONObject icon = new JSONObject();
										room.put("area_id","1");
										room.put("description","");
										room.put("area",area);
										room.put("id",j);
										room.put("name",json_FeatureList1.getJSONObject(i).getString("name"));
										rooms.put(room);
										icon.put("name", "room");
										icon.put("value", json_FeatureList1.getJSONObject(i).getString("device_type_id"));
										icon.put("reference", j);
										icons.put(icon);
										list_usage.add(json_FeatureList1.getJSONObject(i).getString("name"));
										j++;
									}
								}
								// And its associated widget
								JSONObject Widget = new JSONObject();
								Widget.put("place_type","room");
								Widget.put("place_id",list_usage.indexOf( 
										json_FeatureList1.getJSONObject(i).getString("name"))+2); //id_rooms);
								Widget.put("device_feature_id",json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
								Widget.put("id",k);
								k++;
								JSONObject device_feature = new JSONObject();
								device_feature1 = new JSONObject();
								device_feature.put("device_feature_model_id",json_FeatureList1.getJSONObject(i).getString("device_type_id")+"."+json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
								device_feature.put("id",json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
								device_feature.put("device_id",json_FeatureList1.getJSONObject(i).getString("id"));
								Widget.put("device_feature", device_feature);
								ListFeature.put(Widget);
								json_FeatureAssociationList.put("feature_association",ListFeature);
								device_feature1.put("device_feature_model_id",json_FeatureList1.getJSONObject(i).getString("device_type_id")+"."+json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
								device_feature1.put("id",json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
								device_feature1.put("device_id",json_FeatureList1.getJSONObject(i).getString("id"));
								device_feature1.put("device_usage_id",json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
								device_feature1.put("adress",json_Sensors.getJSONObject(listsensor.getString(y)).getString("name"));
								device_feature1.put("device_type_id",json_FeatureList1.getJSONObject(i).getString("device_type_id"));
								device_feature1.put("description",json_FeatureList1.getJSONObject(i).getString("description"));
								device_feature1.put("name",usage);
								device_feature1.put("stat_key",json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
								String data_type=json_Sensors.getJSONObject(listsensor.getString(y)).getString("data_type");
								String parent_type=null;
								try{
								parent_type=Json_data_type.getJSONObject(data_type).getString("parent");
								}catch (JSONException e){
									parent_type=data_type;
								}
								parent_type=parent_type.replace("DT_", "");
								parent_type=parent_type.toLowerCase();
								device_feature1.put("value_type",parent_type);
								JSONObject parameters= new JSONObject();
								
								try{
									//List commands for this device
					                int list_commands = 0;
					                json_Commands=json_FeatureList1.getJSONObject(i).getJSONObject("commands");
									if(json_Commands != null){
										Tracer.d(mytag, "Json list_command="+json_Commands.toString());
					                	list_commands = json_Commands.length();
									}
					                Tracer.d(mytag,list_commands+" commands for device id "+ json_FeatureList1.getJSONObject(i).getString("id") );
					                for(int z = 0; z < list_commands; z++) {
										JSONArray list_command = json_Commands.names();
										try{
											//TODO FOR 0.4 get other params
						                	//this is just a try to get a binary switch working....
											Tracer.d(mytag, "Json this id="+json_Commands.getJSONObject(list_command.getString(z)).getString("id"));
											String command_id=json_Commands.getJSONObject(list_command.getString(z)).getString("id");
											parameters.put("command_id",command_id);
											String  command_type=json_Commands.getJSONObject(list_command.getString(z)).getJSONArray("parameters").getJSONObject(0).getString("key");
											if (command_type!=null)
											Tracer.d(mytag, "Json command_type="+command_type);		                
											parameters.put("command_type",command_type);
											
						                }catch(JSONException e){
						                	
						                }					                
									}
									try{
									String unit=Json_data_type.getJSONObject(data_type).getString("unit");
									if(!unit.equals(null)&& !unit.equals("null"))
										parameters.put("unit",unit);
									}catch (JSONException e){
									}
								}catch (JSONException e){
								}
								device_feature1.put("parameters",parameters);
								db.insertFeature_0_4(device_feature1);
							}
							
						}
		                // for loop on feature list...
		                
		                //Prepare list of rooms, and list of usable features
						json_RoomList.put("room", rooms);
						json_IconList.put("ui_config", icons);
						//List sensors for this device
						  
					}
						
				}
				
				// Common sequence for all versions sync
				
				// Insert results into local database
				// And sharedpref
				prefEditor.putFloat("API_VERSION", Rinor_Api_Version);
				prefEditor.putString("DOMOGIK-VERSION", domogik_Version);
				prefEditor.putBoolean("SYNC", true);
				prefEditor.putString("ASSOCIATION_LIST",json_FeatureAssociationList.toString());
				Tracer.v(mytag,"Updating database tables with new House configuration");
				db.insertArea(json_AreaList);
				prefEditor.putString("AREA_LIST",json_AreaList.toString());
				db.insertRoom(json_RoomList);
				prefEditor.putString("ROOM_LIST",json_RoomList.toString());
				db.insertIcon(json_IconList);
				prefEditor.putString("ICON_LIST",json_IconList.toString());
				if(Rinor_Api_Version <=0.6f){
					db.insertFeature(json_FeatureList);
					prefEditor.putString("FEATURE_LIST",json_FeatureList.toString());
				}else{
					prefEditor.putString("FEATURE_LIST",json_FeatureList1.toString());
				}
				db.insertFeatureAssociation(json_FeatureAssociationList);
				
				if(Rinor_Api_Version <=0.5f){
					db.insertIcon(json_IconList);
					prefEditor.putString("ICON_LIST",json_IconList.toString());
					prefEditor.putBoolean("BY_USAGE", false);
				}else{
					prefEditor.putBoolean("BY_USAGE", true);
				}
				
				//refresh cache address
				Cache_management.checkcache(Tracer,(android.app.Activity) context);
				need_refresh = true;	// To notify main activity that screen must be refreshed
				prefEditor.commit();
				/*
				db.closeDb();
				db = null;
				*/
				publishProgress(100);
				
				Bundle b = new Bundle();
				//Notify sync complete to parent Dialog
				b.putString("message", "sync_done");
			    Message msg = new Message();
			    msg.setData(b);
			    handler.sendMessage(msg);
				return null;
				

			}catch(Exception e){
				handler.sendEmptyMessage(0);
				e.printStackTrace();
			}			
			return null;
		}

	}
}

