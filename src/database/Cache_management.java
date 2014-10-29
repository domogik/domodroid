package database;

import misc.tracerengine;
import widgets.Entity_Feature;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Cache_management {
	public static String mytag="Cache_management";
	private static tracerengine Tracer = null;
	private static SharedPreferences sharedparams;
	private static Activity context;
	private static float api_version;
		
	public static void checkcache(tracerengine Trac, Activity Context){
		Tracer = Trac;
		context = Context;
		sharedparams= PreferenceManager.getDefaultSharedPreferences(context);
		api_version=sharedparams.getFloat("API_VERSION", 0);
		
		if (api_version<=0.6f){ 
			DomodroidDB db = new DomodroidDB(Tracer, context);
			int[] listFeature_Association = db.requestAllFeatures_association();
			Entity_Feature[] listFeature = db.requestFeatures();
			String urlUpdate = sharedparams.getString("URL","1.1.1.1")+"stats/multi/";
			Tracer.i(mytag, "urlupdate= "+urlUpdate);
			int compteur=0;
			for (Entity_Feature feature : listFeature) {
				for (int i=0;i<listFeature_Association.length;i++) {
					if (feature.getId()==listFeature_Association[i]){
						if (!feature.getState_key().equals("")){
							urlUpdate = urlUpdate.concat(feature.getDevId()+"/"+feature.getState_key()+"/");
							compteur=compteur+1;
						}
					}
					
				}			
			}
			Tracer.v(mytag,"prepare UPDATE_URL items="+String.valueOf(compteur));
			Tracer.i(mytag, "urlupdate= "+urlUpdate);
			SharedPreferences.Editor prefEditor=sharedparams.edit();
			prefEditor.putString("UPDATE_URL", urlUpdate);
			//need_refresh = true;	// To notify main activity that screen must be refreshed
			prefEditor.commit();
			//TODO restart the cacheengine.
			//Empty it then refill it with right value
		}
	}
}
