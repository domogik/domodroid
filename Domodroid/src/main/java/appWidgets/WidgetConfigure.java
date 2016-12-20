package appWidgets;

/**
 * Created by tiki on 11/12/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.domogik.domodroid13.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Abstract.translate;
import Entity.Entity_Feature;
import database.DmdContentProvider;
import database.WidgetUpdate;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int device_feature_id_sensor = 0;
    private int device_feature_id_command = 0;
    private SharedPreferences SP_params;
    private Entity_Feature[] listFeature;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP_params = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.widget_configuration);

        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        // Find widget id from launching intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.e("WidgetConfigure", "intent");
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.e("WidgetConfigure", "as extras");
        }

        // If this activity was started with an intent without an app widget ID,
        // finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("WidgetConfigure", "Configuration Activity: no appwidget id provided");
            finish();
        } else {
            Log.e("WidgetConfigure", "Configuration Activity: appwidget has been provided");
        }
        initListViews();
    }

    public void initListViews() {
        Log.e("WidgetConfigure", "initListViews");

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Context context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light);

        final ListView lv1 = (ListView) findViewById(R.id.listview1); // period
        final ListView lv2 = (ListView) findViewById(R.id.listview2); // nodes
        final ListView lv3 = (ListView) findViewById(R.id.listview3); // nodes

        //Check if Domodroid is sync with a server
        if (!SP_params.getBoolean("SYNC", false)) {
            Log.e("WidgetConfigure", "SP_params.getBoolean,false");
            Toast.makeText(this, getString(R.string.not_sync), Toast.LENGTH_SHORT).show();
            //dialog.dismiss();
            finish();
        } else {
            Log.e("WidgetConfigure", "SP_params.getBoolean,false");
        }
        try {
            listFeature = WidgetUpdate.requestFeatures();
            Log.e("WidgetConfigure", "Listfeature size is " + String.valueOf(listFeature.length));
        } catch (Exception e) {
            Toast.makeText(this, "Domodroid is not lunch!", Toast.LENGTH_SHORT).show();
            Log.e("WidgetConfigure Error", e.toString());
            e.printStackTrace();
            //dialog.dismiss();
            finish();
        }
        ArrayList<HashMap<String, String>> listItem_sensors = new ArrayList<>();
        ArrayList<HashMap<String, String>> listItem_commands = new ArrayList<>();

        if (!(listFeature == null)) {
            for (Entity_Feature aListFeature : listFeature) {
                if (aListFeature.getParameters().contains("command_id")) {
                    Log.e("WidgetConfigure", "this feature:" + aListFeature.getName() + " , " + aListFeature.getDevice_usage_id() + " is a command");
                    // Populate lv2 listItem_sensors
                    if (aListFeature != null) {
                        HashMap map = new HashMap<>();
                        map.put("name", aListFeature.getName());
                        //map.put("type", getString(R.string.command) + "-" + aListFeature.getValue_type());
                        map.put("type", getString(R.string.command) + "-" + aListFeature.getValue_type());
                        try {
                            map.put("state_key", getResources().getString(translate.do_translate(getApplicationContext(), null, aListFeature.getState_key())));
                            //map.put("state_key", aListFeature.getState_key());
                        } catch (Exception e) {
                            map.put("state_key", aListFeature.getState_key());
                        }
                        map.put("icon", Integer.toString(aListFeature.getRessources()));
                        map.put("feature_id", aListFeature.getId());
                        listItem_commands.add(map);
                    }
                } else {
                    // Populate lv2 listItem_sensors
                    if (aListFeature != null) {
                        HashMap map = new HashMap<>();
                        map.put("name", aListFeature.getName());
                        map.put("type", aListFeature.getValue_type());
                        try {
                            map.put("state_key", getResources().getString(translate.do_translate(getApplicationContext(), null, aListFeature.getState_key())));
                            //map.put("state_key", aListFeature.getState_key());
                        } catch (Exception e) {
                            map.put("state_key", aListFeature.getState_key());
                        }
                        map.put("icon", Integer.toString(aListFeature.getRessources()));
                        map.put("feature_id", aListFeature.getId());
                        listItem_sensors.add(map);
                    }
                    Log.e("WidgetConfigure", "this feature:" + aListFeature.getName() + " , " + aListFeature.getDevice_usage_id() + " is a sensor");
                }

            }
            final SimpleAdapter adapter_feature_sensors = new SimpleAdapter(getBaseContext(), listItem_sensors,
                    R.layout.item_feature_list_add_feature_map, new String[]{"name", "type", "state_key", "icon"}, new int[]{R.id.name, R.id.description, R.id.state_key, R.id.icon});
            final SimpleAdapter adapter_feature_commands = new SimpleAdapter(getBaseContext(), listItem_commands,
                    R.layout.item_feature_list_add_feature_map, new String[]{"name", "type", "state_key", "icon"}, new int[]{R.id.name, R.id.description, R.id.state_key, R.id.icon});

            // Populate lv1
            ArrayList<HashMap<String, String>> list1 = new ArrayList<>();
            String[] periods = {"sensor", "command", "both"};
            for (String str : periods) {
                HashMap<String, String> item1 = new HashMap<>();
                item1.put("line1", str);
                list1.add(item1);
            }
            final SimpleAdapter sa2 = new SimpleAdapter(context, list1, R.layout.widget_periodselection,
                    new String[]{"line1"}, new int[]{R.id.line_a});
            if ((listItem_sensors != null) && (listItem_sensors.size() > 0)) {
                lv1.setAdapter(sa2);
                lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.e("WidgetConfigure", "onItemClick : " + position + "position at " + id);

                        lv2.setAdapter(adapter_feature_sensors);
                        lv2.setOnItemClickListener(new OnItemClickListener() {
                            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                                Log.e("WidgetConfigure", "onItemClick : " + position + "position at " + arg);
                                HashMap<String, Object> obj = (HashMap<String, Object>) adapter_feature_sensors.getItem(position);
                                int name = (int) obj.get("feature_id");
                                Log.e("WidgetConfigure", "feature_sensors=" + name);
                                device_feature_id_sensor = name;

                                lv2.setVisibility(View.GONE);

                                lv3.setAdapter(adapter_feature_commands);
                                lv3.setOnItemClickListener(new OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                                        Log.e("WidgetConfigure", "onItemClick : " + position + "position at " + arg);
                                        HashMap<String, Object> obj = (HashMap<String, Object>) adapter_feature_commands.getItem(position);
                                        int name = (int) obj.get("feature_id");
                                        Log.e("WidgetConfigure", "feature_commands=" + name);
                                        device_feature_id_command = name;

                                        LinearLayout ll = (LinearLayout) findViewById(R.id.final_instructions);

                                        lv3.setVisibility(View.GONE);
                                        ll.setVisibility(View.VISIBLE);
                                        ll.requestFocus();
                                        Button btn = (Button) findViewById(R.id.save);

                                        btn.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {

                                                configureWidget(getApplicationContext());
                                                Log.e("WidgetConfigure", "configureWidget");
                                                // Make sure we pass back the original appWidgetId before closing the activity
                                                Intent resultValue = new Intent();
                                                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                                                setResult(RESULT_OK, resultValue);
                                                Log.e("WidgetConfigure", "resultValue");
                                                //dialog.dismiss();
                                                finish();
                                            }
                                        });
                                    }
                                });

                                lv3.setVisibility(View.VISIBLE);
                                lv3.requestFocus();
                            }
                        });

                        lv1.setVisibility(View.GONE);

                        lv2.setVisibility(View.VISIBLE);
                        lv2.requestFocus();
                    }
                });

                lv1.setVisibility(View.VISIBLE);
                lv1.requestFocus();

            } else {
                //dialog.dismiss();
                finish();
            }

        }
    }

    /**
     * Configures the created widget
     *
     * @param context
     */

    public void configureWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        AppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, true);
        Log.e("WidgetConfigure", "configureWidget N°:" + mAppWidgetId);
        Log.e("WidgetConfigure", "device_feature_id_sensor N°:" + device_feature_id_sensor);
        Log.e("WidgetConfigure", "device_feature_id_command N°:" + device_feature_id_command);
        ContentValues values = new ContentValues();
        values.put("widget_id", mAppWidgetId);
        values.put("device_feature_id_sensor", device_feature_id_sensor);
        values.put("device_feature_id_command", device_feature_id_command);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_appswidgets_in_appswidgets, values);

    }
}