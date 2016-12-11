package appWidgets;

/**
 * Created by tiki on 11/12/2016.
 */

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    /**
     * Called when the activity is created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    }
}