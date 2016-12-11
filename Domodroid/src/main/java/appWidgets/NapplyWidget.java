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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import database.DmdContentProvider;

public class NapplyWidget extends AppWidgetProvider {
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
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.napply_widget_layout);
        views.setTextViewText(R.id.nap_time, "Allumer la lumière du salon");

        // Prepare intent to launch on widget click
        Intent intent = new Intent(context, NapplyWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setAction(ACTION_SHOW_NOTIFICATION);
        // Launch intent on widget click
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.napply_widget, pendingIntent);

        // Action open Domodroid
        Intent intent2 = new Intent(context, NapplyWidget.class);
        intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent2.setAction(ACTION_START_ACTIVITY);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, appWidgetId, intent2, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.nap_icon, pendingIntent2);

        Log.e("Napply", "appWidgetId N°" + appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Handle new messages
     */
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction() != null) {
            switch (intent.getAction()) {

                case ACTION_SHOW_NOTIFICATION:
                    showNotification(context);
                    Log.e("Napply", "ACTION_SHOW_NOTIFICATION");
                    break;
                case ACTION_START_ACTIVITY:
                    Bundle extras = intent.getExtras();
                    if (extras != null) {
                        try {
                            /*
                            int widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
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
                        Log.e("Napply", "ACTION_START_ACTIVITY");
                        break;
                    }
            }
        }
    }


    /**
     * Displays a notification message
     *
     * @param context
     */

    protected void showNotification(Context context) {
        CharSequence message = "Clique moi ! Clique moi ! Clique moi !";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {

        for (int i : appWidgetIds) {
            ContentValues values = new ContentValues();
            values.put("id", i);
            context.getContentResolver().insert(DmdContentProvider.CONTENT_URI_CLEAR_appswidgets_in_appswidgets, values);
        }
    }
}
