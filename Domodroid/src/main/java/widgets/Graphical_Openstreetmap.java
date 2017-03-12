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
import android.os.Handler;
import android.os.Message;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import Abstract.display_sensor_info;
import Abstract.translate;
import Entity.Entity_Feature;
import Entity.Entity_Map;
import Entity.Entity_client;
import database.WidgetUpdate;
import misc.tracerengine;

import static activities.Activity_Main.SV_Main_ScrollView;

public class Graphical_Openstreetmap extends Basic_Graphical_widget implements OnClickListener {


    private org.osmdroid.views.MapView osmMapview;
    private ArrayList<HashMap<String, String>> listItem;
    private TextView TV_Value;
    private RelativeTimeTextView TV_Timestamp;
    private TextView state;
    private int id;
    private static String mytag;
    private Message msg;

    public FrameLayout container = null;
    private FrameLayout myself = null;

    private Boolean realtime = false;
    private Animation animation;
    private final Entity_Feature feature;
    private String state_key;
    private int dev_id;
    private final int session_type;
    private boolean isopen = false;
    private Float Float_graph_size;
    private TextView state_key_view;
    private String stateS;

    private String test_unite;

    public Graphical_Openstreetmap(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Feature feature, Handler handler) {
        super(activity, Trac, feature.getId(), feature.getDescription(), feature.getState_key(), feature.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature;
        this.session_type = session_type;
        onCreate();
    }

    public Graphical_Openstreetmap(tracerengine Trac,
                                   final Activity activity, int widgetSize, int session_type, int place_id, String place_type,
                                   final Entity_Map feature_map, Handler handler) {
        super(activity, Trac, feature_map.getId(), feature_map.getDescription(), feature_map.getState_key(), feature_map.getIcon_name(), widgetSize, place_id, place_type, mytag, handler);
        this.feature = feature_map;
        this.session_type = session_type;
        onCreate();
    }

    private void onCreate() {
        String parameters = feature.getParameters();
        this.dev_id = feature.getDevId();
        this.state_key = feature.getState_key();
        this.id = feature.getId();
        this.isopen = false;
        int graphics_height_size = prefUtils.GetWidgetGraphSize();
        this.Float_graph_size = Float.valueOf(graphics_height_size);

        myself = this;
        mytag = "Graphical_History(" + dev_id + ")";
        try {
            stateS = getResources().getString(translate.do_translate(getContext(), Tracer, state_key));
        } catch (Exception e) {
            stateS = state_key;
        }
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

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String status;
                if (msg.what == 9999) {
                    if (session == null)
                        return true;
                    String new_val = session.getValue();
                    String Value_timestamp = session.getTimestamp();
                    Tracer.d(mytag, "Handler receives a new TV_Value <" + new_val + "> at " + Value_timestamp);
                    TV_Value.setAnimation(animation);

                    Long Value_timestamplong = null;
                    Value_timestamplong = Long.valueOf(Value_timestamp) * 1000;
                    final String uri = String.format(Locale.ENGLISH, "geo:" + new_val + "?q=" + new_val + "(" + name + "-" + state_key + ")");
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
                    display_sensor_info.display(Tracer, new_val, Value_timestamplong, mytag, feature.getParameters(), TV_Value, TV_Timestamp, activity, LL_featurePan, typefaceweather, typefaceawesome, state_key, state_key_view, stateS, test_unite);
                    //TV_Value.setTypeface(typefaceawesome, Typeface.NORMAL);
                    //TV_Value.setText(new_val + " \uF064");
                    //To have the icon colored as it has no state
                    change_this_icon(2);

                } else if (msg.what == 9998) {
                    // state_engine send us a signal to notify it'll die !
                    Tracer.d(mytag, "state engine disappeared ===> Harakiri !");
                    session = null;
                    realtime = false;
                    removeView(LL_background);
                    myself.setVisibility(GONE);
                    if (container != null) {
                        container.removeView(myself);
                        container.recomputeViewAttributes(myself);
                    }
                    try {
                        finalize();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }    //kill the handler thread itself
                }
                return true;
            }
        });
        //================================================================================
        /*
         * New mechanism to be notified by widgetupdate engine when our TV_Value is changed
		 * 
		 */
        WidgetUpdate cache_engine = WidgetUpdate.getInstance();
        if (cache_engine != null) {
            if (api_version <= 0.6f) {
                session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
            } else if (api_version >= 0.7f) {
                session = new Entity_client(id, "", mytag, handler, session_type);
            }
            try {
                if (Tracer.get_engine().subscribe(session)) {
                    realtime = true;        //we're connected to engine
                    //each time our TV_Value change, the engine will call handler
                    handler.sendEmptyMessage(9999);    //Force to consider current TV_Value in session
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //================================================================================
        //updateTimer();	//Don't use anymore cyclic refresh....
    }


    @Override
    protected void onWindowVisibilityChanged(int visibility) {

    }

    private void display_position_on_map() {
        //Get value display to set lat/lon of current position
        String[] position = TV_Value.getText().toString().split(",");
        Float lat = Float.parseFloat(position[0]);
        Float lon = Float.parseFloat(position[1]);

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
        OverlayItem myLocationOverlayItem = new OverlayItem("", "", startPoint);
        Drawable myCurrentLocationMarker = this.getResources().getDrawable(R.drawable.marker_default);
        myLocationOverlayItem.setMarker(myCurrentLocationMarker);
        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(myLocationOverlayItem);
        ItemizedIconOverlay currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, activity);
        this.osmMapview.getOverlays().add(currentLocationOverlay);
    }

    private void display_history_position_on_map() {
        //todo add a way to handle history position to display them on Openstreetmap
        /*
        JSONObject json_LastValues = null;
        JSONArray itemArray = null;

        listItem = new ArrayList<>();
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                json_LastValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", login, password, 10000, SSL);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/last/5");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(Tracer, url + "sensorhistory/id/" + id + "/last/" + nb_item_for_history + "", login, password, 10000, SSL);
                json_LastValues = new JSONObject();
                json_LastValues.put("stats", json_LastValues_0_4);

            }
            itemArray = json_LastValues.getJSONArray("stats");
            if (api_version <= 0.6f) {
                for (int i = itemArray.length(); i >= 0; i--) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        try {
                            map.put("TV_Value", activity.getString(translate.do_translate(getactivity(), Tracer, itemArray.getJSONObject(i).getString("TV_Value"))));
                        } catch (Exception e1) {
                            map.put("TV_Value", itemArray.getJSONObject(i).getString("TV_Value"));
                        }
                        map.put("date", itemArray.getJSONObject(i).getString("date"));
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
                        try {
                            map.put("TV_Value", activity.getString(translate.do_translate(getactivity(), Tracer, itemArray.getJSONObject(i).getString("value_str"))));
                        } catch (Exception e1) {
                            map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                        }
                        if (api_version == 0.7f) {
                            map.put("date", itemArray.getJSONObject(i).getString("date"));
                        } else if (api_version >= 0.8f) {
                            String currenTimestamp = String.valueOf((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                            map.put("date", display_sensor_info.timestamp_convertion(currenTimestamp, activity));
                        }
                        listItem.add(map);
                        Tracer.d(mytag, map.toString());
                    } catch (Exception e) {
                        Tracer.e(mytag, "Error getting json TV_Value");
                    }
                }
            }

        } catch (Exception e) {
            //return null;
            Tracer.e(mytag, "Error fetching json object");
        }

        SimpleAdapter adapter_feature = new SimpleAdapter(this.activity, listItem,
                R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
        listeChoices.setAdapter(adapter_feature);
        listeChoices.setScrollingCacheEnabled(false);
        */
    }

    public void onClick(View arg0) {
        //Done correct 350px because it's the source of http://tracker.domogik.org/issues/1804
        float size = Float_graph_size * activity.getResources().getDisplayMetrics().density + 0.5f;
        int sizeint = (int) size;
        int currentint = LL_background.getHeight();
        if (!isopen) {
            Tracer.d(mytag, "on click");
            try {
                LL_background.removeView(osmMapview);
                Tracer.d(mytag, "removeView(osmMapview)");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Tracer.d(mytag, "display_position_on_map");
            display_position_on_map();
            Tracer.d(mytag, "addView(osmMapview)");
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, currentint + sizeint));
            try {
                LL_background.removeView(osmMapview);
            } catch (Exception e) {
                //to avoid #135
            }
            LL_background.addView(osmMapview);
            this.osmMapview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    SV_Main_ScrollView.requestDisallowInterceptTouchEvent(true);
                    int action = event.getActionMasked();
                    switch (action) {
                        case MotionEvent.ACTION_UP:
                            SV_Main_ScrollView.requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return false;
                }
            });
            this.isopen = true;

        } else {
            this.isopen = false;
            LL_background.removeView(osmMapview);
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

    }


}



