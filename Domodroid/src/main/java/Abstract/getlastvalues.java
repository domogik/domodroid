package Abstract;

import android.app.Activity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import misc.tracerengine;
import rinor.Rest_com;

/**
 * Created by tiki on 04/12/2016.
 */

class getlastvalues {

    public static void getlastvalue(Activity activity, tracerengine Tracer, float api_version, String mytag, int dev_id,
                                    String state_key, int nb_item_for_history, int id) {
        JSONObject json_LastValues = null;
        JSONArray itemArray = null;
        ListView listeChoices = new ListView(activity);
        ArrayList listItem = new ArrayList<>();
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                json_LastValues = Rest_com.connect_jsonobject(activity, Tracer, "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", 30000);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + "sensorhistory/id/" + id + "/last/5");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(activity, Tracer, "sensorhistory/id/" + id + "/last/" + nb_item_for_history + "", 30000);
                json_LastValues = new JSONObject();
                json_LastValues.put("stats", json_LastValues_0_4);

            }
            itemArray = json_LastValues.getJSONArray("stats");
            if (api_version <= 0.6f) {
                for (int i = itemArray.length(); i >= 0; i--) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        try {
                            map.put("TV_Value", activity.getString(translate.do_translate(activity, Tracer, itemArray.getJSONObject(i).getString("TV_Value"))));
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
                            map.put("TV_Value", activity.getString(translate.do_translate(activity, Tracer, itemArray.getJSONObject(i).getString("value_str"))));
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

        SimpleAdapter adapter_feature = new SimpleAdapter(activity, listItem,
                R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
        listeChoices.setAdapter(adapter_feature);
        listeChoices.setScrollingCacheEnabled(false);
    }
}
