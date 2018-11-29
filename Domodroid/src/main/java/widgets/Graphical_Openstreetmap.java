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
package widgets;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import org.domogik.domodroid13.BuildConfig;
import org.domogik.domodroid13.R;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import java.util.ArrayList;
import java.util.HashMap;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import Event.Entity_client_event_value;
import database.WidgetUpdate;
import misc.tracerengine;
import rinor.Rest_com;

import static activities.Activity_Main.SV_Main_ScrollView;

public class Graphical_Openstreetmap extends Basic_Graphical_widget implements OnClickListener {


    private ArrayList<HashMap<String, String>> listItem;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private TextView state;
    private static String mytag;

    private int nb_item_for_history;
    public FrameLayout container = null;
    private FrameLayout myself = null;

    private Animation animation;
    private final Entity_Feature feature;
    private String state_key;
    private final int session_type;
    private boolean isopen = false;
    private Float Float_graph_size;
    private TextView state_key_view;
    private String stateS;

    private String test_unite;
    private String Value_timestamp;
    private String status;
    private int dev_id;
    private org.osmdroid.views.MapView osmMapview;
    private OverlayItem myLocationOverlayItem;
    private PathOverlay myPath;

    public Graphical_Openstreetmap(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Feature feature) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Openstreetmap(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Map feature_map) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        String parameters = feature.getParameters();
        this.state_key = feature.getState_key();
        this.isopen = false;
        int graphics_height_size = prefUtils.GetWidgetGraphSize();
        this.Float_graph_size = Float.valueOf(graphics_height_size);
        try {
            this.stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            this.stateS = state_key;
        }
        if (api_version <= 0.6f) {
            this.dev_id = feature.getDevId();
        } else if (api_version >= 0.7f) {
            this.dev_id = feature.getId();
            this.state_key = ""; //for entity_client
        }
        try {
            String params_nb_item_for_history = prefUtils.GetWidgetHistoryLength();
            //this.nb_item_for_history = Integer.valueOf(params_nb_item_for_history);
            this.nb_item_for_history = 50;
        } catch (Exception e) {
            Tracer.e(mytag, "Error getting number of item to display");
            this.nb_item_for_history = 5;
        }
        myself = this;
        mytag = "Graphical_Openstreetmap(" + dev_id + ")";

        if (stateS.equals("null"))
            stateS = state_key;
        test_unite = "";
        try {
            //Basilic add, number feature has a unit parameter
            JSONObject jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
            test_unite = jparam.getString("unit");
        } catch (JSONException jsonerror) {
            Tracer.i(mytag, "No unit for this feature");
        }
        setOnClickListener(this);

        //state key
        state_key_view = new TextView(activity);
        state_key_view.setText(stateS);
        state_key_view.setTextColor(Color.parseColor("#333333"));

        //TV_Value
        TV_Value = new TextView(activity);
        TV_Value.setTextSize(28);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            TV_Value.setTextIsSelectable(true);
            TV_Value.setOnClickListener(this);
        }
        TV_Value.setTextColor(Color.BLACK);
        TV_Value.setGravity(Gravity.RIGHT);

        TV_Timestamp = new RelativeTimeTextView(activity, null);
        TV_Timestamp.setTextSize(10);
        TV_Timestamp.setTextColor(Color.BLUE);
        TV_Timestamp.setGravity(Gravity.RIGHT);

        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(1000);

        super.LL_featurePan.addView(TV_Value);
        super.LL_featurePan.addView(TV_Timestamp);
        super.LL_infoPan.addView(state_key_view);

        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
         *
         */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            session = new Entity_client(dev_id, state_key, mytag, session_type);
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    status = session.getValue();
                    Value_timestamp = session.getTimestamp();
                    update_display();
                    //register eventbus for new value
                    EventBus.getDefault().register(this);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
    }

    /**
     * @param event an Entity_client_event_value from EventBus when a new value is received from widgetupdate.
     */
    @Subscribe
    public void onEvent(Entity_client_event_value event) {
        // your implementation
        Tracer.d(mytag, "Receive event from Eventbus" + event.Entity_client_event_get_id() + " With value" + event.Entity_client_event_get_val());
        if (event.Entity_client_event_get_id() == dev_id) {
            status = event.Entity_client_event_get_val();
            Value_timestamp = event.Entity_client_event_get_timestamp();
            update_display();
            update_map_position();
        }
    }

    /**
     * Update the current widget information at creation
     * or when an eventbus is receive
     */
    private void update_display() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Tracer.d(mytag, "update_display id:" + dev_id + " <" + status + "> at " + Value_timestamp);
                TV_Value.setAnimation(animation);

                Long Value_timestamplong;
                Value_timestamplong = Long.valueOf(Value_timestamp) * 1000;
                final String uri = "geo:" + status + "?q=" + status + "(" + name + "-" + state_key + ")";
                TV_Value.setOnClickListener(new OnClickListener() {
                                                public void onClick(View v) {
                                                    try {
                                                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                                        activity.startActivity(unrestrictedIntent);
                                                    } catch (ActivityNotFoundException innerEx) {
                                                        Toast.makeText(activity, R.string.missing_maps_applications, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            }

                );
                display_sensor_info.display(Tracer, status, Value_timestamplong, mytag, feature.getParameters(), TV_Value, TV_Timestamp, activity, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);
                //TV_Value.setTypeface(typefaceawesome, Typeface.NORMAL);
                //TV_Value.setText(new_val + " \uF064");
                //To have the icon colored as it has no state
                change_this_icon(2);

            }
        });
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
    }

    private void update_map_position() {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = Float_graph_size * activity.getResources().getDisplayMetrics().density + 0.5f;
        int sizeint = (int) size;
        int currentint = LL_background.getHeight();
        if (!this.isopen) {
            //if not open extend layout size to handle map view
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, currentint + sizeint));
        }
        try {
            LL_background.removeView(osmMapview);
        } catch (Exception e) {
            //to avoid #135
        }
        Tracer.d(mytag, "display_position_on_map");
        display_position_on_map();
        LL_background.addView(osmMapview);
        osmMapview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SV_Main_ScrollView.requestDisallowInterceptTouchEvent(true);
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                        SV_Main_ScrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return true;
            }
        });
        //display_history_position_on_map sync = new display_history_position_on_map();
        //sync.execute();
        this.isopen = true;
    }

    private void display_position_on_map() {
        Float lat = Float.parseFloat("0");
        Float lon = Float.parseFloat("0");
        if (TV_Value.getText().toString() != null && TV_Value.getText().toString() != "") {
            //Get value display to set lat/lon of current position
            String[] position = TV_Value.getText().toString().split(",");
            lat = Float.parseFloat(position[0]);
            lon = Float.parseFloat(position[1]);
        }
        osmMapview = new org.osmdroid.views.MapView(activity);
        //important! set your user agent to prevent getting banned from the osm servers
        org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants.setUserAgentValue(BuildConfig.APPLICATION_ID);
        osmMapview.setTileSource(TileSourceFactory.MAPNIK);

        //Then we add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
        osmMapview.setBuiltInZoomControls(true);
        osmMapview.setMultiTouchControls(true);

        //We can move the map on a default view point. For this, we need access to the map controller:
        IMapController mapController = osmMapview.getController();
        mapController.setZoom(15);
        GeoPoint startPoint = new GeoPoint(lat, lon);
        //Center on position
        mapController.setCenter(startPoint);
        //Add a marker on position
        myLocationOverlayItem = new OverlayItem("", "", startPoint);
        Drawable myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.marker_default);
        myLocationOverlayItem.setMarker(myCurrentLocationMarker);
        final ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(myLocationOverlayItem);
        ItemizedIconOverlay currentLocationOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, activity);
        osmMapview.getOverlays().add(currentLocationOverlay);
    }

    private class display_history_position_on_map extends AsyncTask<Void, Integer, Void> {
        //todo add a way to handle history position to display them on Openstreetmap

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(activity, R.string.loading_data_from_rest, Toast.LENGTH_SHORT).show();
        }

        protected void onPostExecute(Void result) {
            osmMapview.getOverlays().add(myPath);
            //osmMapview.invalidate();
            Tracer.d(mytag, "OnPostExecute");
        }


        protected Void doInBackground(Void... params) {
            JSONObject json_LastValues = null;
            JSONArray itemArray;
            try {
                if (api_version <= 0.6f) {
                    Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                    json_LastValues = Rest_com.connect_jsonobject(activity, Tracer, "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", 30000);
                } else if (api_version >= 0.7f) {
                    Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + "sensorhistory/id/" + dev_id + "/last/" + nb_item_for_history);
                    //Don't forget old "dev_id"+"state_key" is replaced by "id"
                    JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(activity, Tracer, "sensorhistory/id/" + dev_id + "/last/" + nb_item_for_history + "", 30000);
                    json_LastValues = new JSONObject();
                    json_LastValues.put("stats", json_LastValues_0_4);
                    Tracer.d(mytag, "json value GPS position" + json_LastValues.toString());

                }
                itemArray = json_LastValues.getJSONArray("stats");
                if (api_version <= 0.6f) {
                    for (int i = itemArray.length(); i >= 0; i--) {
                        try {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("TV_Value", itemArray.getJSONObject(i).getString("TV_Value"));
                            //map.put("date", itemArray.getJSONObject(i).getString("date"));
                            listItem.add(map);
                            Tracer.d(mytag, map.toString());
                        } catch (Exception e) {
                            Tracer.e(mytag, "Error getting json TV_Value");
                        }
                    }
                } else if (api_version >= 0.7f) {
                    for (int i = 0; i < itemArray.length(); i++) {
                        try {
                            HashMap<String, String> map = new HashMap<>();
                            map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                            /*if (api_version == 0.7f) {
                                map.put("date", itemArray.getJSONObject(i).getString("date"));
                            } else if (api_version >= 0.8f) {
                                String currenTimestamp = String.valueOf((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                                map.put("date", display_sensor_info.timestamp_convertion(currenTimestamp, activity));
                            }*/
                            listItem.add(map);
                            Tracer.d(mytag, map.toString());
                        } catch (Exception e) {
                            Tracer.e(mytag, "Error getting json TV_Value");
                        }
                    }
                }
                myPath = new PathOverlay(Color.RED, activity);
                for (int j = 0; j < listItem.size(); j++) {
                    String[] position = listItem.get(j).get("TV_Value").split(",");
                    Float lat = Float.parseFloat(position[0]);
                    Float lon = Float.parseFloat(position[1]);
                    GeoPoint gPt0 = new GeoPoint(lat, lon);
                    myPath.addPoint(gPt0);
                }
            } catch (JSONException e) {
                //return null;
                Tracer.e(mytag, "Error fetching json object");
            }
            return null;
        }
    }

    public void onClick(View arg0) {
        listItem = new ArrayList<>();
        if (!isopen) {
            Tracer.d(mytag, "on click");
            try {
                LL_background.removeView(osmMapview);
                Tracer.d(mytag, "removeView(osmMapview)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            update_map_position();
        } else {
            this.isopen = false;
            LL_background.removeView(osmMapview);
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }


}



