package Abstract;

import android.content.Context;
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

public class getlastvalues {

    public static void getlastvalue(Context context, tracerengine Tracer, float api_version, ArrayList listItem, ListView listeChoices, String mytag, int dev_id,
                                    String url, String state_key, int nb_item_for_history, String login, String password, Boolean SSL, int id) {
        JSONObject json_LastValues = null;
        JSONArray itemArray = null;
        listeChoices = new ListView(context);
        listItem = new ArrayList<>();
        try {
            if (api_version <= 0.6f) {
                Tracer.i(mytag, "UpdateThread (" + dev_id + ") : " + url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/");
                json_LastValues = Rest_com.connect_jsonobject(Tracer, url + "stats/" + dev_id + "/" + state_key + "/last/" + nb_item_for_history + "/", login, password, 30000, SSL);
            } else if (api_version >= 0.7f) {
                Tracer.i(mytag, "UpdateThread (" + id + ") : " + url + "sensorhistory/id/" + id + "/last/5");
                //Don't forget old "dev_id"+"state_key" is replaced by "id"
                JSONArray json_LastValues_0_4 = Rest_com.connect_jsonarray(Tracer, url + "sensorhistory/id/" + id + "/last/" + nb_item_for_history + "", login, password, 30000, SSL);
                json_LastValues = new JSONObject();
                json_LastValues.put("stats", json_LastValues_0_4);

            }
            itemArray = json_LastValues.getJSONArray("stats");
            if (api_version <= 0.6f) {
                for (int i = itemArray.length(); i >= 0; i--) {
                    try {
                        HashMap<String, String> map = new HashMap<>();
                        try {
                            map.put("TV_Value", context.getString(translate.do_translate(context, Tracer, itemArray.getJSONObject(i).getString("TV_Value"))));
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
                            map.put("TV_Value", context.getString(translate.do_translate(context, Tracer, itemArray.getJSONObject(i).getString("value_str"))));
                        } catch (Exception e1) {
                            map.put("TV_Value", itemArray.getJSONObject(i).getString("value_str"));
                        }
                        if (api_version == 0.7f) {
                            map.put("date", itemArray.getJSONObject(i).getString("date"));
                        } else if (api_version >= 0.8f) {
                            String currenTimestamp = String.valueOf((long) (itemArray.getJSONObject(i).getInt("timestamp")) * 1000);
                            map.put("date", display_sensor_info.timestamp_convertion(currenTimestamp, context));
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

        SimpleAdapter adapter_feature = new SimpleAdapter(context, listItem,
                R.layout.item_history_in_graphical_history, new String[]{"TV_Value", "date"}, new int[]{R.id.value, R.id.date});
        listeChoices.setAdapter(adapter_feature);
        listeChoices.setScrollingCacheEnabled(false);
    }
}
