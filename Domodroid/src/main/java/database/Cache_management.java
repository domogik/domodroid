package database;

import android.app.Activity;

import Abstract.pref_utils;
import Entity.Entity_Feature;
import misc.tracerengine;

public class Cache_management {
    private static final String mytag = "Cache_management";
    private static pref_utils prefUtils;

    public static void checkcache(tracerengine Trac, Activity activity) {
        // Change UrlAccess to make cache more light.
        // 1st need to change when this urlupdate his create.
        // 2nd need to check if this entity_feature exist somewhere (in feature_map or feature_assotiation)
        // 3rd add it in path only if it is the case.
        // So when a user will remove it from association or map it will be removed from cache
        // And when it will be add, it will get back in cache.
        prefUtils = new pref_utils(activity.getApplicationContext());

        float api_version = prefUtils.prefs.getFloat("API_VERSION", 0);
        String urlUpdate = "";
        if (api_version != 0) {
            if (api_version <= 0.6f) {
                DomodroidDB db = new DomodroidDB(Trac, activity);
                int[] listFeature_Association = db.requestAllFeatures_association();
                Entity_Feature[] listFeature = db.requestFeatures();
                urlUpdate = prefUtils.prefs.getString("URL", "1.1.1.1") + "stats/multi/";
                Trac.i(mytag, "urlupdate= " + urlUpdate);
                int compteur = 0;
                for (Entity_Feature feature : listFeature) {
                    for (int aListFeature_Association : listFeature_Association) {
                        if (feature.getId() == aListFeature_Association) {
                            if (!feature.getState_key().equals("")) {
                                urlUpdate = urlUpdate.concat(feature.getDevId() + "/" + feature.getState_key() + "/");
                                compteur = compteur + 1;
                            }
                        }

                    }
                }
                Trac.v(mytag, "prepare UPDATE_URL items=" + String.valueOf(compteur));
                Trac.i(mytag, "urlupdate= " + urlUpdate);
                prefUtils.editor.putString("UPDATE_URL", urlUpdate);
                //need_refresh = true;	// To notify main activity that screen must be refreshed
                prefUtils.editor.commit();
                //TODO restart the cache-engine.
                //Empty it then refill it with right value
                WidgetUpdate WU_widgetUpdate = WidgetUpdate.getInstance();
                if (WU_widgetUpdate != null) {
                    WU_widgetUpdate.refreshNow();
                    Trac.d(mytag, "launching a widget update refresh");
                } else {
                    WU_widgetUpdate.init(Trac, activity);
                    Trac.d(mytag, "launching a widget update init");
                }
            } else if (api_version >= 0.7f) {
                if (api_version >= 0.7f)
                    urlUpdate = prefUtils.prefs.getString("URL", "1.1.1.1") + "sensor/";
                //todo for #124 but later
                /*
                if (api_version >= 0.9f)
                    urlUpdate = sharedparams.getString("URL", "1.1.1.1") + "sensor/since/";
                */
                prefUtils.editor.putString("UPDATE_URL", urlUpdate);
                //need_refresh = true;	// To notify main activity that screen must be refreshed
                prefUtils.editor.commit();
            }
        }

        Trac.v(mytag, "UPDATE_URL = " + urlUpdate);

    }
}