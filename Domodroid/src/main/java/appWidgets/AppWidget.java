package appWidgets;

/**
 * Created by tiki on 11/12/2016.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import database.DmdContentProvider;

public class AppWidget extends AppWidgetProvider {
    public static final String ACTION_SHOW_NOTIFICATION = "org.domodroid13.appwidgets.SHOW_NOTIFICATION";
    private static final String ACTION_START_ACTIVITY = "org.domodroid13.appwidgets.START_ACTIVITY";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context, AppWidgetManager.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        // Perform this loop procedure for each App GraphWidget that belongs to this provider
        for (Integer i : allWidgetIds)
            updateAppWidget(context, appWidgetManager, i, false);
    }

    /**
     * Update the widget
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, boolean forceUpdate) {

        // Prepare widget views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget_layout);

        views.setTextViewText(R.id.nap_time, "Allumer la lumière du salon");

        // Prepare intent to launch on widget click
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra("id", appWidgetId);
        // Launch intent on widget text click
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nap_time, pendingIntent);

        // Action open Domodroid
        Intent intent2 = new Intent(context, AppWidget.class);
        intent2.setAction(ACTION_START_ACTIVITY);
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Launch intent on widget icon click
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nap_icon, pendingIntent2);

        Log.e("AppWidget", "appWidgetId N°" + appWidgetId);
/*
Todo get feature from sensor and command for this appswidgets
        Cursor sensor = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_appswidgets_sensor, null, null, null, null);
        Cursor command = context.getContentResolver().query(DmdContentProvider.CONTENT_URI_REQUEST_FEATURE_appswidgets_command, null, null, null, null);
        String sensor_id = sensor.getString(0);
        Log.e("AppWidget", "sensor_id N°" + sensor_id);
        String command_id = command.getString(0);
        Log.e("AppWidget", "command_id N°" + command_id);
*/
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Handle new messages
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null) {
            Bundle extras;
            extras = intent.getExtras();
            int widgetId;
            if (extras != null) {
                switch (intent.getAction()) {
                    case ACTION_SHOW_NOTIFICATION:
                        try {
                            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                            Log.e("AppWidget", "Id= " + widgetId);
                            showNotification(context, widgetId);
                            Log.e("AppWidget", "ACTION_SHOW_NOTIFICATION");
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    case ACTION_START_ACTIVITY:
                        try {
                            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                            Log.e("AppWidget", "Id= " + widgetId);
                            /*
                            graphWidget = sqlite.dbHlpr.getGraphWidget(widgetId);
                            Intent intent2 = new Intent(context, Activity_GraphView.class);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent2.putExtra("node", graphWidget.getPlugin().getInstalledOn().getUrl());
                            intent2.putExtra("plugin", graphWidget.getPlugin().getName());
                            intent2.putExtra("period", graphWidget.getPeriod());
                            context.startActivity(intent2);
                             */
                        } catch (NullPointerException ex) {
                            ex.printStackTrace();
                        }
                        Log.e("AppWidget", "ACTION_START_ACTIVITY");
                        break;
                }
            }
        }
    }


    /**
     * Displays a notification message
     *
     * @param context
     * @param widgetId
     */

    protected void showNotification(Context context, int widgetId) {
        Log.e("AppWidget", "Widgets selectionner " + widgetId);

        CharSequence message = "Widgets selectionner: " + widgetId;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        for (int i : appWidgetIds) {
            ContentValues values = new ContentValues();
            values.put("widget_id", i);
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_appswidgets_in_appswidgets, values);
        }
    }

}
