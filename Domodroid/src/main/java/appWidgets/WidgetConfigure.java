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

import org.domogik.domodroid13.R;

import java.util.ArrayList;
import java.util.HashMap;

import Entity.Entity_Feature;
import database.DmdContentProvider;
import database.WidgetUpdate;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SharedPreferences SP_params;
    private Entity_Feature[] listFeature;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SP_params = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // If the user closes window, don't create the widget
        setResult(RESULT_CANCELED);

        // Find widget id from launching intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Log.e("Napply", "intent");
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            Log.e("Napply", "extras");

        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e("Napply", "Configuration Activity: no appwidget id provided");
            finish();
        }

        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View dialogView = mInflater.inflate(R.layout.widget_configuration, null);

        final Context context = new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .show();

        final ListView lv1 = (ListView) dialogView.findViewById(R.id.listview1); // nodes
        final ListView lv2 = (ListView) dialogView.findViewById(R.id.listview2); // plugins
        final ListView lv3 = (ListView) dialogView.findViewById(R.id.listview3); // period

        //Check if Domodroid is sync with a server
        if (!SP_params.getBoolean("SYNC", false)) {
            Log.e("Napply", "SP_params.getBoolean,false");
            Toast.makeText(this, getString(R.string.not_sync), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish();
        } else {
            Log.e("Napply", "SP_params.getBoolean,false");
        }
        try {
            listFeature = WidgetUpdate.requestFeatures();
            Log.e("Napply", "Listfeature size is " + String.valueOf(listFeature.length));
        } catch (Exception e) {
            Log.e("Napply Error", e.toString());
            e.printStackTrace();
        }
        ArrayList<HashMap<String, String>> listItem1 = new ArrayList<>();

        for (Entity_Feature aListFeature : listFeature) {
            if (aListFeature.getParameters().contains("command_id")) {
                Log.e("Napply", "this feature:" + aListFeature.getName() + " , " + aListFeature.getDevice_usage_id() + " is a command");
            } else {
                Log.e("Napply", "this feature:" + aListFeature.getName() + " , " + aListFeature.getDevice_usage_id() + " is a sensor");
            }
            if (aListFeature != null) {
                HashMap map = new HashMap<>();
                map.put("name", aListFeature.getName());
                if (aListFeature.getParameters().contains("command")) {
                    map.put("type", getString(R.string.command) + "-" + aListFeature.getValue_type());
                } else {
                    map.put("type", aListFeature.getValue_type());
                }
                try {
                    //map.put("state_key", getResources().getString(translate.do_translate(getApplicationContext(), Tracer, feature.getState_key())));
                    map.put("state_key", aListFeature.getState_key());
                } catch (Exception e) {
                    map.put("state_key", aListFeature.getState_key());
                }
                map.put("icon", Integer.toString(aListFeature.getRessources()));
                listItem1.add(map);
            }
            if ((listItem1 != null) && (listItem1.size() > 0)) {
                SimpleAdapter adapter_feature = new SimpleAdapter(getBaseContext(), listItem1,
                        R.layout.item_feature_list_add_feature_map, new String[]{"name", "type", "state_key", "icon"}, new int[]{R.id.name, R.id.description, R.id.state_key, R.id.icon});
                lv1.setAdapter(adapter_feature);
                lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
/*                        if (position < listFeature.length) {
                            // It's a feature element
                            mapView.map_id = -1;
                            mapView.temp_id = listFeature[position].getId();
                        } else {
                            //It's a map switch element
                            mapView.temp_id = -1;
                            mapView.map_id = (position - listFeature.length) + 99999;
                            Tracer.v(mytag, "map_id = <" + mapView.map_id + "> , map selected <" + list_usable_files.elementAt(mapView.map_id - 99999) + ">");
                        }
                        mapView.setAddMode(true);
                        dialog_feature.dismiss();
*/
                    }
                });
            }

            lv1.setVisibility(View.VISIBLE);
            lv1.requestFocus();

        }
        configureWidget(getApplicationContext());
        Log.e("Napply", "configureWidget");
        // Make sure we pass back the original appWidgetId before closing the activity
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        Log.e("Napply", "resultValue");
        finish();
    }

    /**
     * Configures the created widget
     *
     * @param context
     */
    public void configureWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        AppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, true);
        Log.e("Napply", "configureWidget N°:" + mAppWidgetId);

        ContentValues values = new ContentValues();
        values.put("widget_id", mAppWidgetId);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_appswidgets_in_appswidgets, values);

    }
}