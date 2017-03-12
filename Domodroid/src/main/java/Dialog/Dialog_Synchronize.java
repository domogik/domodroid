package Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import Abstract.pref_utils;
import activities.Activity_Main;
import database.Cache_management;
import database.DomodroidDB;
import misc.tracerengine;
import rinor.Rest_com;

public class Dialog_Synchronize extends Dialog implements OnClickListener {
    private final Button cancelButton;
    private final TextView message;
    private final pref_utils prefUtils;
    private String urlAccess;
    private static Handler handler = null;
    private LoadConfig sync;
    public Boolean need_refresh = false;
    private final Activity activity;
    public Boolean reload = false;
    private DomodroidDB db = null;
    private tracerengine Tracer = null;
    private final String mytag = this.getClass().getName();
    private float previous_api_version = 0f;
    private boolean by_usage;
    private int progress;
    private String last_device_update;

    public Dialog_Synchronize(tracerengine Trac, final Activity activity) {
        super(activity);
        this.activity = activity;
        this.Tracer = Trac;
        prefUtils = new pref_utils();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_synchronize);
        message = (TextView) findViewById(R.id.message);
        cancelButton = (Button) findViewById(R.id.CancelButton);
        cancelButton.setOnClickListener(this);
        last_device_update = prefUtils.GetLastDeviceUpdate();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    String loc_Value = msg.getData().getString("message");
                    switch (loc_Value) {
                        case "sync_done":
                            sync.cancel(true);
                            dismiss();
                            return;
                        case "conn_error":
                            message.setText(R.string.sync_rinor_error);
                            return;
                        case "2_area":
                            message.setText(R.string.sync_2_error_area);
                            return;
                        case "2_room":
                            message.setText(R.string.sync_2_error_room);
                            return;
                        case "2_feature":
                            message.setText(R.string.sync_2_error_feature);
                            return;
                        case "2_feature_association":
                            message.setText(R.string.sync_2_error_feature);
                            return;
                        case "2_ui_config":
                            message.setText(R.string.sync_2_error_ui);
                            return;
                        case "3_feature":
                            message.setText(R.string.sync_3_error_feature);
                            return;
                        case "device":
                            message.setText(R.string.sync_4_error_device);
                            return;
                        case "datatype":
                            message.setText(R.string.sync_4_error_datatype);
                            return;
                    }

                } catch (Exception e) {
                    Tracer.e(mytag, e.toString());
                    message.setText(R.string.connection_error);
                }
                message.setText(R.string.connection_error);

            }
        };
    }


    public void onClick(View v) {
        if (v == cancelButton)
            need_refresh = false;
        sync.cancel(true);
        dismiss();
    }

    public void startSync() {
        sync = new LoadConfig();
        sync.execute();
    }

    public class LoadConfig extends AsyncTask<Void, Integer, Void> {
        private final boolean sync = false;

        public LoadConfig() {
            super();
            if (db == null)
                db = new DomodroidDB(Tracer, activity);
            try {
                previous_api_version = prefUtils.GetDomogikApiVersion();
                Tracer.d(mytag, "Previous Api version value exist");
            } catch (Exception e) {
                e.printStackTrace();
                Tracer.d(mytag, "Can't grab previous value");
            }
            try {
                by_usage = prefUtils.GetWidgetByUsage();
                Tracer.d(mytag, "Previous by usage value exist");
            } catch (Exception e) {
                by_usage = true;
                e.printStackTrace();
                Tracer.d(mytag, "Can't grab previous value of by usage");
            }
        }

        @Override
        protected void onPreExecute() {
            message.setText(activity.getString(R.string.sync_0));
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (sync) {
                Intent reload = new Intent(activity, Activity_Main.class);
                activity.startActivity(reload);
            }
            super.onPostExecute(result);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            message.setText(String.format("%s %d%%", activity.getString(R.string.sync_load), values[0]));
            super.onProgressUpdate(values);
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(Params[])
         */
        @Override
        protected Void doInBackground(Void... param_void) {
            //Requests


            // if API rinor >0.5 génération auto sinon classic
            JSONObject json_rinor;
            String mytag = "Dialog_Synchronize";
            try {
                json_rinor = Rest_com.connect_jsonobject(activity, Tracer, "", 30000);
                publishProgress(2);
            } catch (Exception e) {
                Tracer.e(mytag, "Error connecting to rinor");
                json_rinor = null;
            }
            if (json_rinor == null || json_rinor.toString().equals("{}")) {
                //Cannot connect to server...
                Bundle b = new Bundle();
                //Notify error to parent Dialog
                b.putString("message", "conn_error");
                Message msg = new Message();
                msg.setData(b);
                handler.sendMessage(msg);
                return null;
            }
            String Rinor_Api_ver = "";
            try {
                Rinor_Api_ver = json_rinor.getJSONObject("info").getString("REST_API_version");
                publishProgress(4);
            } catch (Exception e) {
                try {
                    Rinor_Api_ver = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("REST_API_version");
                } catch (Exception e1) {
                    Tracer.e(mytag, "ERROR getting Rest version");
                }
            }
            Tracer.i(mytag, "RinorAPI= " + Rinor_Api_ver);
            Float Rinor_Api_Version = Float.valueOf(Rinor_Api_ver);
            String domogik_Version = "";
            try {
                domogik_Version = json_rinor.getJSONObject("info").getString("Domogik_version");
                publishProgress(6);
            } catch (Exception e) {
                try {
                    domogik_Version = json_rinor.getJSONArray("rest").getJSONObject(0).getJSONObject("info").getString("Domogik_version");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
            }
            Tracer.i(mytag, "domogik_Version= " + domogik_Version);

            JSONObject json_AreaList = null;
            JSONObject json_RoomList = null;
            JSONObject json_FeatureList = null;
            JSONObject device_feature1;
            JSONArray json_FeatureList1 = null;
            JSONObject json_Sensors = null;
            JSONObject json_Commands = null;
            JSONObject json_FeatureAssociationList = null;
            JSONObject json_IconList = null;
            publishProgress(8);
            // grab a new method if sync by past that only erase what concern area id 1 if previous api >0.6f
            // and if syncing with the same api version
            if ((previous_api_version == Rinor_Api_Version) && (Rinor_Api_Version >= 0.6f)) {
                // Erase in tables only device/usage  list !
                // surround with a try catch due to possible error in previous sync
                try {
                    if (reload) {
                        //Reload is defined by activity_main if user reload from previous settings
                        //Erase all data in this case before reload value from file
                        db.updateDb();
                        // todo call a method to load saved preferences map_feature to db
                        try {
                            json_AreaList = new JSONObject(prefUtils.GetArea());
                            db.insertArea(json_AreaList);
                            Tracer.d(mytag, "inserting area to db");
                        } catch (Throwable t) {
                            Tracer.e(mytag, "Could not parse malformed area JSON: \"" + prefUtils.GetArea() + "\"");
                        }
                        try {
                            json_RoomList = new JSONObject(prefUtils.GetRoom());
                            db.insertRoom(json_RoomList);
                            Tracer.d(mytag, "inserting room to db");
                        } catch (Throwable t) {
                            Tracer.e(mytag, "Could not parse malformed room JSON: \"" + prefUtils.GetRoom() + "\"");
                        }
                        try {
                            json_IconList = new JSONObject(prefUtils.GetIconList());
                            db.insertIcon(json_IconList);
                            Tracer.d(mytag, "inserting icon to db");
                        } catch (Throwable t) {
                            Tracer.e(mytag, "Could not parse malformed icon JSON: \"" + prefUtils.GetIconList() + "\"");
                        }
                        try {
                            json_FeatureAssociationList = new JSONObject(prefUtils.GetFeatureListAssociation());
                            db.insertFeatureAssociation(json_FeatureAssociationList);
                            Tracer.d(mytag, "inserting FeatureAssociationList to db");
                        } catch (Throwable t) {
                            Tracer.e(mytag, "Could not parse malformed feature association JSON: \"" + prefUtils.GetFeatureListAssociation() + "\"");
                        }
                    }
                    db.NewsyncDb();
                    Tracer.i(mytag, "Doing a sync update only, not erasing all");
                    publishProgress(10);
                } catch (Exception e) {
                    Tracer.e(mytag, e.toString());
                    db.updateDb();
                    Tracer.i(mytag, "Doing a full sync, erasing all !!!");
                }
            } else {
                //Erase all tables contents EXCEPT maps coordinates !
                db.updateDb();
                Tracer.i(mytag, "Doing a full sync, erasing all !!!");
                publishProgress(10);
            }

            if (Rinor_Api_Version <= 0.5f) {
                json_AreaList = Rest_com.connect_jsonobject(activity, Tracer, "base/area/list/", 30000);
                if (json_AreaList == null || json_AreaList.toString().equals("{}")) {
                    //Cannot connect to server...
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "2_base");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                Tracer.d(mytag, "AreaList = <" + json_AreaList.toString() + ">");

                publishProgress(20);
                json_RoomList = Rest_com.connect_jsonobject(activity, Tracer, "base/room/list/", 30000);
                if (json_RoomList == null || json_RoomList.toString().equals("{}")) {
                    //Cannot connect to server...
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "2_room");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                Tracer.d(mytag, "RoomList = <" + json_RoomList.toString() + ">");

                publishProgress(40);
                json_FeatureList = Rest_com.connect_jsonobject(activity, Tracer, "base/feature/list", 30000);
                if (json_FeatureList == null || json_FeatureList.toString().equals("{}")) {
                    //Cannot connect to server...
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "2_feature");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }

                publishProgress(60);
                json_FeatureAssociationList = Rest_com.connect_jsonobject(activity, Tracer, "base/feature_association/list/", 30000);
                if (json_FeatureAssociationList == null || json_FeatureAssociationList.toString().equals("{}")) {
                    //Cannot connect to server...
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "2_feature_association");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                publishProgress(80);
                json_IconList = Rest_com.connect_jsonobject(activity, Tracer, "base/ui_config/list/", 30000);
                if (json_IconList == null || json_IconList.toString().equals("{}")) {
                    //Cannot connect to server...
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "2_ui_config");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                publishProgress(90);

            } else if (Rinor_Api_Version <= 0.6f) {
                // Function special Basilic domogik 0.3
                json_FeatureList = Rest_com.connect_jsonobject(activity, Tracer, "base/feature/list", 30000);
                if (json_FeatureList == null || json_FeatureList.toString().equals("{}")) {
                    // Cannot connect to Rinor server.....
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "3_feature");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                publishProgress(25);

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
                String usage;
                //Create an ArrayList
                ArrayList<String> list_usage = new ArrayList<>();

                try {
                    json_AreaList.put("status", "OK");
                    json_AreaList.put("code", 0);
                    json_AreaList.put("description", "None");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                try {
                    map_area.put("description", "");
                    map_area.put("id", "1");
                    map_area.put("name", "Usage");
                    list.put(map_area);
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                try {
                    json_AreaList.put("area", list);
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                publishProgress(45);

                try {
                    json_RoomList.put("status", "OK");
                    json_RoomList.put("code", 0);
                    json_RoomList.put("description", "None");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                try {
                    area.put("description", "");
                    area.put("id", "1");
                    area.put("name", "Usage");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }

                int numberofroom = db.requestidlastRoom();
                int j = numberofroom + 1;
                try {
                    json_FeatureAssociationList.put("status", "OK");
                    json_FeatureAssociationList.put("code", "0");
                    json_FeatureAssociationList.put("description", "");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                publishProgress(55);

                int list_size = 0;
                if (json_FeatureList != null)
                    try {
                        list_size = json_FeatureList.getJSONArray("feature").length();
                    } catch (JSONException e1) {
                        Tracer.e(mytag, e1.toString());
                    }
                Tracer.d(mytag, "Features list size = " + list_size);
                //correct a bug #2020 if device is empty.
                for (int i = 0; i < list_size; i++) {
                    try {

                        usage = json_FeatureList.getJSONArray("feature").getJSONObject(i)
                                .getJSONObject("device").getString("device_usage_id");
                    } catch (Exception e) {
                        usage = null;
                        // Cannot parse JSON Array or JSONObject
                        Tracer.e(mytag, "Exception processing Features list (" + i + ")");
                        Tracer.e(mytag, e.toString());
                    }
                    Tracer.i(mytag, "Features list processing usage = <" + usage + ">");

                    // Create a pseudo 'room' for each usage returned by Rinor
                    if (usage != null) {
                        if (!list_usage.contains(usage)) {
                            try {
                                if (json_FeatureList.getJSONArray("feature").length() > 0) {
                                    progress = 100 * i / json_FeatureList.getJSONArray("feature").length();
                                    publishProgress(progress);
                                    JSONObject room = new JSONObject();
                                    room.put("area_id", "1");
                                    room.put("description", "");

                                    room.put("area", area);
                                    room.put("id", j);
                                    j++;
                                    room.put("name", json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
                                    rooms.put(room);
                                    list_usage.add(json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id"));
                                }
                            } catch (JSONException e) {
                                Tracer.e(mytag, e.toString());
                            }
                        }
                        // And its associated widget
                        JSONObject Widget = new JSONObject();
                        try {
                            Widget.put("place_type", "room");
                            //#85 here, place_id was false.
                            Widget.put("place_id", numberofroom + list_usage.indexOf(
                                    json_FeatureList.getJSONArray("feature").getJSONObject(i).getJSONObject("device").getString("device_usage_id")) + 1); //id_rooms);
                            Widget.put("device_feature_id", json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
                            Widget.put("id", 50 + i);
                        } catch (JSONException e) {
                            Tracer.e(mytag, e.toString());
                        }
                        JSONObject device_feature = new JSONObject();
                        try {
                            device_feature.put("device_feature_model_id", json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_feature_model_id"));
                            device_feature.put("id", json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("id"));
                            device_feature.put("device_id", json_FeatureList.getJSONArray("feature").getJSONObject(i).getString("device_id"));
                        } catch (JSONException e) {
                            Tracer.e(mytag, e.toString());
                        }
                        try {
                            Widget.put("device_feature", device_feature);
                        } catch (JSONException e) {
                            Tracer.e(mytag, e.toString());
                        }
                        ListFeature.put(Widget);
                    }

                } // for loop on feature list...

                //Prepare list of rooms, and list of usable features
                try {
                    json_RoomList.putOpt("room", rooms);
                } catch (JSONException e) {
                    Tracer.e(mytag, e.toString());
                }
                try {
                    json_FeatureAssociationList.putOpt("feature_association", ListFeature);
                } catch (JSONException e) {
                    Tracer.e(mytag, e.toString());
                }

            } else if (Rinor_Api_Version >= 0.7f) {
                //TODO lot of work on this.
                // Function special Domogik 0.4
                //get value from rest.
                try {
                    String MQaddress = json_rinor.getJSONObject("mq").getString("ip");
                    String MQsubport = json_rinor.getJSONObject("mq").getString("sub_port");
                    String MQpubport = json_rinor.getJSONObject("mq").getString("pub_port");
                    String MQreq_repport = json_rinor.getJSONObject("mq").getString("req_rep_port");
                    // #103 if MQadress=localhost
                    if (MQaddress.equals("localhost") || MQaddress.equals("127.0.0.1") || MQaddress.equals("0.0.0.0")) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, R.string.mq_domogik_conf_localhost, Toast.LENGTH_LONG).show();
                            }
                        });
                        //MQaddress = ""; //TODO save it as empty or just tell user the MQ will not work?
                    } else if (MQaddress.equals("*")) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "MQ address in domogik config looks like a demo mode. It will not work correctly with Domodroid", Toast.LENGTH_LONG).show();
                            }
                        });
                        //MQaddress = ""; //TODO save it as empty or just tell user the MQ will not work?
                    }
                    prefUtils.SetMqAddress(MQaddress);
                    prefUtils.SetMqSubPort(MQsubport);
                    prefUtils.SetMqPubPort(MQpubport);
                    prefUtils.SetMqReqRepPort(MQreq_repport);
                    publishProgress(12);
                } catch (Exception e1) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.problem_with_mq_information, Toast.LENGTH_LONG).show();
                            Toast.makeText(activity, R.string.check_server_part_in_option, Toast.LENGTH_LONG).show();
                        }
                    });
                    Tracer.e(mytag, "ERROR getting MQ information");
                }
                json_FeatureList1 = Rest_com.connect_jsonarray(activity, Tracer, "device", 30000);
                if (json_FeatureList1 == null || json_FeatureList1.toString().equals("[]")) {
                    // Cannot connect to Rinor server.....
                    Tracer.e(mytag, "Cannot connect to to grab device list");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.problem_geting_device_information, Toast.LENGTH_LONG).show();
                            Toast.makeText(activity, R.string.check_server_part_in_option, Toast.LENGTH_LONG).show();
                        }
                    });
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "device");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                publishProgress(14);
                JSONObject Json_data_type = Rest_com.connect_jsonobject(activity, Tracer, "datatype", 30000);
                if (Json_data_type == null || (Json_data_type.toString().equals("{}"))) {
                    // Cannot get data_type from Rinor server.....
                    Tracer.e(mytag, "Cannot get data_type from Rinor server.....");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.problem_geting_datatype_information, Toast.LENGTH_LONG).show();
                            Toast.makeText(activity, R.string.check_server_part_in_option, Toast.LENGTH_LONG).show();
                        }
                    });
                    Bundle b = new Bundle();
                    //Notify error to parent Dialog
                    b.putString("message", "datatype");
                    Message msg = new Message();
                    msg.setData(b);
                    handler.sendMessage(msg);
                    return null;
                }
                publishProgress(25);
                Tracer.i(mytag, "connected to a 0.4 or more Domogik Version");

                //todo #75 ask user how he want the default Area to be organize
                //see https://github.com/domogik/domodroid/issues/75
                String device_sync_order = prefUtils.GetDeviceSyncOrder();
                Tracer.i(mytag, "Default area ordered by :" + device_sync_order);

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
                JSONArray icons = new JSONArray();
                JSONArray ListFeature = new JSONArray();
                //Create string
                String usage;
                //Create an ArrayList
                ArrayList<String> list_usage = new ArrayList<>();

                try {
                    json_AreaList.put("status", "OK");
                    json_AreaList.put("code", 0);
                    json_AreaList.put("description", "None");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                try {
                    map_area.put("description", "");
                    map_area.put("id", "1");
                    //todo #75 reorder for the moment it his done by name
                    switch (device_sync_order) {
                        case "device_name":
                            map_area.put("name", "Device Name");
                            break;
                        case "device_type":
                            map_area.put("name", "Device Type");
                            break;
                        case "plugin":
                            map_area.put("name", "Plugin");
                            break;
                        default:
                            map_area.put("name", "Usage");
                            break;
                    }
                    list.put(map_area);
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                try {
                    json_AreaList.put("area", list);
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                publishProgress(45);

                try {
                    json_RoomList.put("status", "OK");
                    json_RoomList.put("code", 0);
                    json_RoomList.put("description", "None");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }

                try {
                    json_IconList.put("status", "OK");
                    json_IconList.put("code", 0);
                    json_IconList.put("description", "None");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }

                try {
                    area.put("description", "");
                    area.put("id", "1");
                    area.put("name", "Device");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }

                int numberofroom = db.requestidlastRoom();
                int j = numberofroom + 1;
                int k = 50;
                try {
                    json_FeatureAssociationList.put("status", "OK");
                    json_FeatureAssociationList.put("code", "0");
                    json_FeatureAssociationList.put("description", "");
                } catch (JSONException e1) {
                    Tracer.e(mytag, e1.toString());
                }
                publishProgress(55);

                int list_size = 0;
                if (json_FeatureList1 != null) {
                    list_size = json_FeatureList1.length();
                    if (Rinor_Api_Version >= 0.8f) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date timestamplast_device_update;
                        Date timestamplast_update = new Date();
                        boolean newer = false;
                        try {
                            timestamplast_device_update = df.parse(last_device_update);
                        } catch (Exception e) {
                            Tracer.e(mytag, "No saved date or error parsing it");
                            timestamplast_device_update = new Date();
                        }
                        try {
                            //test info_changed:
                            for (int i = 0; i < json_FeatureList1.length(); i++) {
                                try {
                                    String last_update = json_FeatureList1.getJSONObject(i).getString("info_changed");
                                    timestamplast_update = df.parse(last_update);
                                    //compare to latest update
                                    if (timestamplast_update.compareTo(timestamplast_device_update) > 0) {
                                        newer = true;
                                        timestamplast_device_update = timestamplast_update;
                                        Tracer.v(mytag, "device info_changed at: " + timestamplast_update.toString());
                                    }
                                } catch (Exception E) {
                                    timestamplast_update = new Date();
                                    Tracer.d(mytag, "Exception info_changed:" + E);
                                }
                            }
                            if (newer) {
                                //store last update in prefs for next start
                                prefUtils.SetLastDeviceUpdate(df.format(timestamplast_device_update));
                            }
                        } catch (Exception e) {
                            Tracer.e(mytag, "Error trying to parse /device and info_changed");
                        }
                    }
                }
                Tracer.i(mytag, "Device list size = " + list_size);
                for (int i = 0; i < list_size; i++) {
                    progress = 55 + (35 * i / list_size);
                    publishProgress(progress);
                    int list_sensors = 0;
                    //List sensors for this device
                    try {
                        json_Sensors = json_FeatureList1.getJSONObject(i).getJSONObject("sensors");
                    } catch (JSONException e1) {
                        Tracer.e(mytag, e1.toString());
                    }
                    if (json_Sensors != null)
                        list_sensors = json_Sensors.length();
                    try {
                        Tracer.i(mytag, list_sensors + " sensors for device id " + json_FeatureList1.getJSONObject(i).getString("id"));
                    } catch (JSONException e1) {
                        Tracer.e(mytag, e1.toString());
                    }
                    JSONArray listsensor = json_Sensors.names();

                    //Sort list sensors by sensors id
                    List<Integer> sensoridlist = new ArrayList<Integer>();
                    if (list_sensors > 0) {
                        for (int y = 0; y < list_sensors; y++)
                            try {
                                sensoridlist.add(json_Sensors.getJSONObject(listsensor.getString(y)).getInt("id"));
                            } catch (JSONException e) {
                                Tracer.e(mytag, "sorting error" + e.toString());
                            }
                        Collections.sort(sensoridlist);
                    }
                    //List all sensors
                    for (int y = 0; y < list_sensors; y++) {
                        try {
                            //todo #75 reorder for the moment it his done by name
                            switch (device_sync_order) {
                                case "device_name":
                                    usage = json_FeatureList1.getJSONObject(i).getString("name");
                                    break;
                                case "device_type":
                                    usage = json_FeatureList1.getJSONObject(i).getString("device_type_id");
                                    break;
                                case "plugin":
                                    usage = json_FeatureList1.getJSONObject(i).getString("client_id");
                                    usage = usage.substring(usage.indexOf("-") + 1, usage.indexOf("."));
                                    usage = usage.substring(0, 1).toUpperCase() + usage.substring(1).toLowerCase();
                                    break;
                                default:
                                    usage = json_FeatureList1.getJSONObject(i).getString("name");
                                    break;
                            }
                        } catch (Exception e) {
                            usage = null;
                            // Cannot parse JSON Array or JSONObject
                            Tracer.e(mytag, "Exception processing sensor list (" + y + ")");
                        }
                        Tracer.i(mytag, "Features list processing usage = " + usage);

                        // Create a pseudo 'room' for each usage returned by Rinor
                        // prepare icon_table for room base on device_type_id in 0.4
                        if (usage != null) {
                            if (!list_usage.contains(usage)) {
                                if (json_Sensors.length() > 0) {
                                    JSONObject room = new JSONObject();
                                    JSONObject icon = new JSONObject();
                                    try {
                                        room.put("area_id", "1");
                                        room.put("description", "");
                                        room.put("area", area);
                                        room.put("id", j);
                                        room.put("name", usage);
                                        rooms.put(room);
                                    } catch (JSONException e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    try {
                                        icon.put("name", "room");
                                        icon.put("value", json_FeatureList1.getJSONObject(i).getString("device_type_id"));
                                        icon.put("reference", j);
                                    } catch (JSONException e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    icons.put(icon);
                                    try {
                                        list_usage.add(usage);
                                    } catch (Exception e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    j++;
                                }
                            }
                            // And its associated widget
                            JSONObject Widget = new JSONObject();
                            try {
                                Widget.put("place_type", "room");
                                //#85 here place_id was false
                                Widget.put("place_id", numberofroom + list_usage.indexOf(usage) + 1); //id_rooms);
                                Widget.put("device_feature_id", json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
                                Widget.put("id", k + sensoridlist.indexOf(json_Sensors.getJSONObject(listsensor.getString(y)).getInt("id")));
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            JSONObject device_feature = new JSONObject();
                            device_feature1 = new JSONObject();
                            String data_type = null;
                            try {
                                data_type = json_Sensors.getJSONObject(listsensor.getString(y)).getString("data_type");
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                device_feature.put("device_feature_model_id", data_type + "." + json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
                                device_feature.put("id", json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
                                device_feature.put("device_id", json_FeatureList1.getJSONObject(i).getString("id"));
                                Widget.put("device_feature", device_feature);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            ListFeature.put(Widget);
                            try {
                                json_FeatureAssociationList.put("feature_association", ListFeature);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                device_feature1.put("device_feature_model_id", data_type + "." + json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
                                device_feature1.put("id", json_Sensors.getJSONObject(listsensor.getString(y)).getString("id"));
                                device_feature1.put("device_id", json_FeatureList1.getJSONObject(i).getString("id"));
                                device_feature1.put("device_usage_id", json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
                                device_feature1.put("adress", json_Sensors.getJSONObject(listsensor.getString(y)).getString("name"));
                                device_feature1.put("device_type_id", json_FeatureList1.getJSONObject(i).getString("device_type_id"));
                                device_feature1.put("description", json_FeatureList1.getJSONObject(i).getString("description"));
                                device_feature1.put("name", json_FeatureList1.getJSONObject(i).getString("name"));
                                device_feature1.put("stat_key", json_Sensors.getJSONObject(listsensor.getString(y)).getString("reference"));
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            JSONObject parameters = new JSONObject();
                            String parent_type = null;
                            boolean parent_again = false;
                            String tempdata_type = data_type;
                            try {
                                //get value0 and value1 from labels
                                JSONObject labels = Json_data_type.getJSONObject(tempdata_type).getJSONObject("labels");
                                for (int length = 0; length < labels.length(); length++) {
                                    parameters.put("value" + labels.names().get(length), labels.getString((String) labels.names().get(length)));
                                }
                                Tracer.v(mytag, "dt_type: " + data_type + " as labels: " + labels.toString());
                            } catch (Exception e) {
                                Tracer.d(mytag, "NO labels for this dt_type: " + data_type);
                            }
                            try {
                                //get values
                                JSONObject values = Json_data_type.getJSONObject(tempdata_type).getJSONObject("values");
                                parameters.putOpt("values", values.toString());
                                Tracer.v(mytag, "dt_type: " + data_type + " as values: " + values.toString());
                            } catch (Exception e) {
                                Tracer.d(mytag, "NO values for this dt_type: " + data_type);
                            }
                            //For 0.4 make a loop until no more parent data_type
                            while (!parent_again) {
                                try {
                                    parent_type = Json_data_type.getJSONObject(tempdata_type).getString("parent");
                                    tempdata_type = parent_type;
                                } catch (JSONException e) {
                                    parent_type = tempdata_type;
                                    parent_again = true;
                                }
                            }
                            parent_type = parent_type.replace("DT_", "");
                            parent_type = parent_type.toLowerCase();
                            try {
                                device_feature1.put("value_type", parent_type);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                String unit = Json_data_type.getJSONObject(data_type).getString("unit");
                                if (!unit.equals(null) && !unit.equals("null"))
                                    parameters.put("unit", unit);
                            } catch (JSONException e) {
                                Tracer.d(mytag, "No unit for this one");
                            }
                            try {
                                device_feature1.put("parameters", parameters);
                                db.insertFeature_0_4(device_feature1);
                            } catch (JSONException e) {
                                Tracer.e(mytag, e.toString());
                            }

                        }
                    }
                    k = k + list_sensors;
                    //Create feature from commands
                    int list_commands = 0;
                    try {
                        json_Commands = json_FeatureList1.getJSONObject(i).getJSONObject("commands");
                    } catch (JSONException e1) {
                        Tracer.e(mytag, e1.toString());
                    }
                    if (json_Commands != null) {
                        Tracer.d(mytag, "Json list_command=" + json_Commands.toString());
                        list_commands = json_Commands.length();
                    }
                    try {
                        Tracer.d(mytag, list_commands + " commands for device id " + json_FeatureList1.getJSONObject(i).getString("id"));
                    } catch (JSONException e1) {
                        Tracer.e(mytag, e1.toString());
                    }
                    JSONArray listcommand = json_Commands.names();

                    //Sort list commands by commands id
                    List<Integer> commandidlist = new ArrayList<Integer>();
                    if (list_commands > 0) {
                        for (int y = 0; y < list_commands; y++)
                            try {
                                commandidlist.add(json_Commands.getJSONObject(listcommand.getString(y)).getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        Collections.sort(commandidlist);
                    }
                    //List all commands
                    for (int y = 0; y < list_commands; y++) {
                        try {
                            //todo #75 reorder for the moment it his done by name
                            switch (device_sync_order) {
                                case "device_name":
                                    usage = json_FeatureList1.getJSONObject(i).getString("name");
                                    break;
                                case "device_type":
                                    usage = json_FeatureList1.getJSONObject(i).getString("device_type_id");
                                    break;
                                case "plugin":
                                    usage = json_FeatureList1.getJSONObject(i).getString("client_id");
                                    usage = usage.substring(usage.indexOf("-") + 1, usage.indexOf("."));
                                    usage = usage.substring(0, 1).toUpperCase() + usage.substring(1).toLowerCase();
                                    break;
                                default:
                                    usage = json_FeatureList1.getJSONObject(i).getString("name");
                                    break;
                            }
                        } catch (Exception e) {
                            usage = null;
                            // Cannot parse JSON Array or JSONObject
                            Tracer.d(mytag, "Exception processing command list (" + y + ")");
                        }
                        Tracer.d(mytag, "Features list processing usage = " + usage);

                        // Create a pseudo 'room' for each usage returned by Rinor
                        // Prepare icon_table for room base on device_type_id in 0.4
                        if (usage != null) {
                            if (!list_usage.contains(usage)) {
                                if (json_Commands.length() > 0) {
                                    JSONObject room = new JSONObject();
                                    JSONObject icon = new JSONObject();
                                    try {
                                        room.put("area_id", "1");
                                        room.put("description", "");
                                        room.put("area", area);
                                        room.put("id", j);
                                        room.put("name", usage);
                                        rooms.put(room);
                                    } catch (JSONException e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    try {
                                        icon.put("name", "room");
                                        icon.put("value", json_FeatureList1.getJSONObject(i).getString("device_type_id"));
                                        icon.put("reference", j);
                                        icons.put(icon);
                                    } catch (JSONException e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    try {
                                        list_usage.add(usage);
                                    } catch (Exception e) {
                                        Tracer.e(mytag, e.toString());
                                    }
                                    j++;
                                }
                            }
                            // And its associated widget
                            JSONObject Widget = new JSONObject();
                            //TODO find a way to remove this limit of 50000 sensors
                            //It is used to have not the same id for a sensor and a commands
                            int tempid = 0;
                            try {
                                tempid = json_Commands.getJSONObject(listcommand.getString(y)).getInt("id");
                            } catch (NumberFormatException | JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            tempid = tempid + 50000;
                            try {
                                Widget.put("place_type", "room");
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                //#85 here place_id was false
                                Widget.put("place_id", numberofroom + list_usage.indexOf(usage) + 1);
                                Widget.put("device_feature_id", tempid);
                                Widget.put("id", k + commandidlist.indexOf(json_Commands.getJSONObject(listcommand.getString(y)).getString("id")));
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            String data_type = null;
                            try {
                                data_type = json_Commands.getJSONObject(listcommand.getString(y)).getJSONArray("parameters").getJSONObject(0).getString("data_type");
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            JSONObject device_feature = new JSONObject();
                            device_feature1 = new JSONObject();
                            try {
                                device_feature.put("device_feature_model_id", json_Commands.getJSONObject(json_Commands.names().getString(y)).getJSONArray("parameters").getJSONObject(0).getString("data_type")
                                        + "." + json_Commands.getJSONObject(listcommand.getString(y)).getString("name"));
                                device_feature.put("id", tempid);
                                device_feature.put("device_id", json_FeatureList1.getJSONObject(i).getString("id"));
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                Widget.put("device_feature", device_feature);
                                ListFeature.put(Widget);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                json_FeatureAssociationList.put("feature_association", ListFeature);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                device_feature1.put("device_feature_model_id", json_Commands.getJSONObject(json_Commands.names().getString(y)).getJSONArray("parameters").getJSONObject(0).getString("data_type")
                                        + "." + json_Commands.getJSONObject(listcommand.getString(y)).getString("name"));
                                device_feature1.put("id", tempid);
                                device_feature1.put("device_id", json_FeatureList1.getJSONObject(i).getString("id"));
                                device_feature1.put("device_usage_id", json_Commands.getJSONObject(listcommand.getString(y)).getString("name"));
                                device_feature1.put("adress", json_Commands.getJSONObject(listcommand.getString(y)).getString("name"));
                                device_feature1.put("device_type_id", json_FeatureList1.getJSONObject(i).getString("device_type_id"));
                                device_feature1.put("description", json_FeatureList1.getJSONObject(i).getString("description"));
                                device_feature1.put("name", json_FeatureList1.getJSONObject(i).getString("name"));
                                device_feature1.put("stat_key", json_Commands.getJSONObject(listcommand.getString(y)).getString("name"));
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            JSONObject parameters = new JSONObject();
                            String parent_type = null;
                            boolean parent_again = false;
                            String tempdata_type = data_type;
                            try {
                                //get value0 and value1 from labels
                                JSONObject labels = Json_data_type.getJSONObject(tempdata_type).getJSONObject("labels");
                                for (int length = 0; length < labels.length(); length++) {
                                    parameters.putOpt("value" + labels.names().get(length), labels.getString((String) labels.names().get(length)));
                                }
                                Tracer.i(mytag, "dt_type: " + data_type + " as labels: " + labels.toString());
                            } catch (Exception e) {
                                Tracer.d(mytag, "NO labels for this dt_type: " + data_type);
                            }
                            try {
                                //get values
                                JSONObject values = Json_data_type.getJSONObject(tempdata_type).getJSONObject("values");
                                parameters.putOpt("values", values.toString());
                                Tracer.v(mytag, "dt_type: " + data_type + " as values: " + values.toString());
                            } catch (Exception e) {
                                Tracer.d(mytag, "NO values for this dt_type: " + data_type);
                            }
                            //For 0.4 make a loop until no more parent data_type
                            while (!parent_again) {
                                try {
                                    parent_type = Json_data_type.getJSONObject(tempdata_type).getString("parent");
                                    tempdata_type = parent_type;
                                } catch (JSONException e) {
                                    parent_type = tempdata_type;
                                    parent_again = true;
                                }
                            }
                            parent_type = parent_type.replace("DT_", "");
                            parent_type = parent_type.toLowerCase();
                            try {
                                device_feature1.putOpt("value_type", parent_type);
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }

                            JSONArray list_command = json_Commands.names();
                            JSONArray command_parameter = new JSONArray();
                            try {
                                command_parameter = json_Commands.getJSONObject(json_Commands.names().getString(y)).getJSONArray("parameters");
                            } catch (JSONException e1) {
                                Tracer.e(mytag, e1.toString());
                            }
                            try {
                                //FOR 0.4 get other params
                                //this is just a try to get a binary switch working....
                                Tracer.d(mytag, "Json this id=" + json_Commands.getJSONObject(list_command.getString(y)).getString("id"));
                                String command_id = json_Commands.getJSONObject(list_command.getString(y)).getString("id");
                                parameters.putOpt("command_id", command_id);
                                parameters.putOpt("number_of_command_parameters", command_parameter.length());
                                for (int nb_parameters = 0; nb_parameters < command_parameter.length(); nb_parameters++) {
                                    String command_type = command_parameter.getJSONObject(nb_parameters).getString("key");
                                    if (command_type != null) {
                                        Tracer.d(mytag, "Json command_type=" + command_type);
                                        parameters.putOpt("command_type" + (nb_parameters + 1), command_type);
                                    }
                                    String command_data_type = command_parameter.getJSONObject(nb_parameters).getString("data_type");
                                    if (command_data_type != null) {
                                        Tracer.d(mytag, "Json command_data_type=" + command_type);
                                        parent_again = true;
                                        while (!parent_again) {
                                            try {
                                                parent_type = Json_data_type.getJSONObject(command_data_type).getString("parent");
                                                tempdata_type = parent_type;
                                            } catch (JSONException e) {
                                                parent_type = command_data_type;
                                                parent_again = true;
                                            }
                                        }
                                        parameters.putOpt("command_data_type" + (nb_parameters + 1), parent_type);
                                    }
                                }
                            } catch (JSONException e) {
                                Tracer.e(mytag, "error with json commands");
                            }
                            try {
                                device_feature1.putOpt("parameters", parameters);
                                db.insertFeature_0_4(device_feature1);
                            } catch (JSONException e) {
                                Tracer.e(mytag, e.toString());
                            }

                        }
                    }
                    k = k + list_commands;
                    // for loop on feature list...

                    //Prepare list of rooms, and list of usable features
                    try {
                        json_RoomList.putOpt("room", rooms);
                        json_IconList.putOpt("ui_config", icons);
                    } catch (JSONException e) {
                        Tracer.e(mytag, e.toString());
                    }

                }
                publishProgress(90);


            }
            //TODO #141 when available in rest
            /*if (Rinor_Api_Version > 0.9f) {
                try {
                    String External_port = json_rinor.getJSONObject("external").getString("external_ip");
                    String External_IP = json_rinor.getJSONObject("external").getString("external_port");
                    prefUtils.SetExternalRestIp( External_IP);
                    prefUtils.SetExternalRestPort(External_port);
                } catch (Exception e1) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "Could not get external IP PORT configuration", Toast.LENGTH_LONG).show();
                        }
                    });
                    Tracer.e(mytag, "ERROR getting external IP PORT information");
                }
            }*/

            // Common sequence for all versions sync

            // Insert results into local database
            // And sharedpref
            prefUtils.SetDomogikApiVersion(Rinor_Api_Version);
            prefUtils.SetDomogikVersion(domogik_Version);
            prefUtils.SetSyncCompleted(true);
            publishProgress(91);
            Tracer.v(mytag, "Updating database tables with new House configuration");
            try {
                db.insertArea(json_AreaList);
                prefUtils.SetArea(db.request_json_Area().toString());
            } catch (JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            publishProgress(92);
            try {
                db.insertRoom(json_RoomList);
                prefUtils.SetRoom(db.request_json_Room().toString());
            } catch (JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            publishProgress(93);
            if (Rinor_Api_Version >= 0.7f) {
                try {
                    db.insertIcon(json_IconList);
                    prefUtils.SetIconList(db.request_json_Icon().toString());
                } catch (JSONException e) {
                    Tracer.e(mytag, e.toString());
                }
            }
            publishProgress(94);
            if (Rinor_Api_Version <= 0.6f) {
                try {
                    db.insertFeature(json_FeatureList);
                    //No need of db request method as feature only comes from rest
                    // in fact the best way for feature is to rename or change description directly in domogik.
                    prefUtils.SetFeatureList(json_FeatureList.toString());
                } catch (JSONException e) {
                    Tracer.e(mytag, e.toString());
                }
            } else {
                //No need of db request method as feature only comes from rest
                prefUtils.SetFeatureList(json_FeatureList1.toString());
            }
            publishProgress(95);
            try {
                db.insertFeatureAssociation(json_FeatureAssociationList);
                prefUtils.SetFeatureListAssociation(db.request_json_Features_association().toString());
            } catch (JSONException e) {
                Tracer.e(mytag, e.toString());
            }
            publishProgress(96);
            if (Rinor_Api_Version <= 0.5f) {
                try {
                    db.insertIcon(json_IconList);
                    prefUtils.SetIconList(db.request_json_Icon().toString());
                } catch (JSONException e) {
                    Tracer.e(mytag, e.toString());
                }
                prefUtils.SetWidgetByUsage(false);
            } else if (Rinor_Api_Version >= 0.6f) {
                if (Rinor_Api_Version >= 0.7f)
                    prefUtils.SetAlternativeBinaryWidget(true);
                prefUtils.SetWidgetByUsage(by_usage);
            }
            publishProgress(97);
            //Clear possible feature association with deleted device
            db.CleanFeatures_association();
            publishProgress(98);
            //refresh cache address
            Cache_management.checkcache(Tracer, activity);
            need_refresh = true;    // To notify main activity that screen must be refreshed
            prefUtils.commit();

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
        }

    }
}

