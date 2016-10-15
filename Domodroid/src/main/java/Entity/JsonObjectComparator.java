package Entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JsonObjectComparator {

    private static final int TYPE_STRING = 1;
    private static final int TYPE_INT = 2;
    private static final int TYPE_DUBLE = 3;


    public static JSONArray sort(JSONArray array, Comparator c) {
        List asList = new ArrayList(array.length());
        for (int i = 0; i < array.length(); i++) {
            asList.add(array.opt(i));
        }
        Collections.sort(asList, c);
        JSONArray res = new JSONArray();
        for (Object o : asList) {
            res.put(o);
        }
        return res;
    }

    public static Comparator getComparator(final String tagJSON, final int type) {
        Comparator c = new Comparator() {
            public int compare(Object a, Object b) {
                try {
                    JSONObject ja = (JSONObject) a;
                    JSONObject jb = (JSONObject) b;
                    switch (type) {
                        case TYPE_STRING:// String
                            return ja.optString(tagJSON, "")
                                    .toLowerCase()
                                    .compareTo(jb.optString(tagJSON, "").toLowerCase());
                        case TYPE_INT:// int
                            int valA = ja.getInt(tagJSON);
                            int valB = jb.getInt(tagJSON);
                            if (valA > valB)
                                return 1;
                            if (valA < valB)
                                return -1;

                        case TYPE_DUBLE:// double
                            String v1 = ja.getString(tagJSON).replace(",", ".");
                            String v2 = jb.getString(tagJSON).replace(",", ".");

                            double valAd = new Double(v1);// ja.getDouble(tagJSON);
                            double valBd = new Double(v2);//  jb.getDouble(tagJSON);
                            if (valAd > valBd)
                                return 1;
                            if (valAd < valBd)
                                return -1;

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };

        return c;
    }
}