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
import android.widget.ListView;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import database.DmdContentProvider;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private SharedPreferences SP_params;

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

        //final AlertDialog dialog = new AlertDialog.Builder(context)
         //       .setView(dialogView)
         //       .show();
        final ListView lv1 = (ListView) dialogView.findViewById(R.id.listview1); // nodes
        final ListView lv2 = (ListView) dialogView.findViewById(R.id.listview2); // plugins
        final ListView lv3 = (ListView) dialogView.findViewById(R.id.listview3); // period
        if (SP_params.getBoolean("SYNC", false)) {
            Log.e("Napply", "SP_params.getBoolean,true");
            Toast.makeText(this, getString(R.string.not_sync), Toast.LENGTH_SHORT).show();
            //dialog.dismiss();
            finish();
        }else {
            Log.e("Napply", "SP_params.getBoolean,false");
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
        NapplyWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId, true);
        Log.e("Napply", "configureWidget N°:" + mAppWidgetId);

        ContentValues values = new ContentValues();
        values.put("widget_id", mAppWidgetId);
        context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_INSERT_appswidgets_in_appswidgets, values);

    }
}