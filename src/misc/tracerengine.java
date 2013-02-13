package misc;

import java.io.FileWriter;
import java.io.IOException;
import android.content.SharedPreferences;
import android.util.Log;

public class tracerengine {
	private static Boolean	to_Android = true;
	private static Boolean	to_txtFile = false;
	private static Boolean	to_screen = false;
	private static String logpath = null;
	private static String logname = null;
	
	private static Boolean	txtappend = false;		//append by default
	private static SharedPreferences settings = null;
	private static SharedPreferences.Editor prefEditor;
	
	private static FileWriter txtFile = null;
	
	public tracerengine(SharedPreferences params){
		settings = params;
		set_profile(params);
	}
	public void close() {
		if(txtFile != null) {
			try {
				txtFile.close();
			} catch (Exception e) {}
			
			txtFile = null;
		}
	}
	public void d(String tag, String msg) {
		choose_log(0,tag,msg);
	}
	public void e(String tag, String msg) {
		choose_log(1,tag,msg);
	}
	public void i(String tag, String msg) {
		choose_log(2,tag,msg);
	}
	public void v(String tag, String msg) {
		choose_log(3,tag,msg);
	}
	public void w(String tag, String msg) {
		choose_log(4,tag,msg);
	}
	/*
	 * Configure Tracer profile
	 */
	public void set_profile( SharedPreferences params) {
		settings = params;
		get_settings();
		
	}
	public void refresh_settings() {
		if(settings != null ) {
			logpath=settings.getString("LOGPATH", "");
			logname=settings.getString("LOGNAME", "");
			to_Android = settings.getBoolean("SYSTEMLOG", false);
			to_txtFile = settings.getBoolean("TEXTLOG", false);
			to_screen = settings.getBoolean("SCREENLOG", false);
			txtappend = settings.getBoolean("LOGAPPEND", false);
			
			
		}
		
	}
	private void get_settings() {
		if(settings != null) {
			Boolean changed = settings.getBoolean("LOGCHANGED", true);
			if(changed) {
				refresh_settings();
				if(txtFile != null) {
					txtFile = null;	//close object
				}
				if(to_txtFile) {
					if(logname.equals("")) {
						// If no filename given, no log to file, nor in append !
						to_txtFile = false;
						txtappend=false;
						
					} else {
						// file path given : try to open it....
						try {
							txtFile = new FileWriter(logpath+logname, txtappend);
							txtlog(2," ","Starting log session");
						} catch (Exception e) {
							txtFile = null;
							to_txtFile = false;
							txtappend=false;
						}
						
					}
				} 
				prefEditor=settings.edit();
				prefEditor.putBoolean("LOGCHANGED", false);
				prefEditor.putBoolean("TEXTLOG", to_txtFile);	//In case open fails.... don't retry till next change !
				prefEditor.commit();
				
			}
			// Nothing changed
		}
	}
	/*
	 * all modes use this common method, to decide wich kind of logging is configured
	 */
	private void choose_log(int type, String tag, String msg) {
		
		// if needed, log to Android
		if(to_Android)
			syslog(type,tag,msg);
		// if needed, log to text file
		if(to_txtFile)
			txtlog(type,tag,msg);
		if(to_screen) {
			screenlog(type,tag,msg);
		}
	}
	/*
	 * TODO Method writing messages to screen view
	 */
	private void screenlog(int type,String tag,String msg) {
		
	}
	/*
	 * Method writing messages to text file
	 */
	private void txtlog(int type,String tag,String msg) {
		if(txtFile != null) {
			String typeC = " ";
			String dateS = " ";
			String tagS = tag;
			switch (type) {
			case 0:
				typeC = "D";
				break;
			case 1:
				typeC = "E";
				break;
			case 2:
				typeC = "I";
				break;
			case 3:
				typeC = "V";
				break;
			case 4:
				typeC = "W";
				break;
			}
			try {
				String line = typeC+" | "+dateS+" | "+tagS+" | "+msg;
				//Log.w("Tracerengine",line);
				txtFile.write(line+"\n");
				txtFile.flush();
			} catch (IOException i) {
				txtFile = null;		//Abort log to text file in future
				to_txtFile = false;
			}
		}
	}
	/*
	 * method sending messages to system log ( for Eclipse, and CatLog )
	 */
	private void syslog(int type, String tag, String msg) {
		switch (type) {
		case 0:
			Log.d(tag,msg);
			break;
		case 1:
			Log.e(tag,msg);
			break;
		case 2:
			Log.i(tag,msg);
			break;
		case 3:
			Log.v(tag,msg);
			break;
		case 4:
			Log.w(tag,msg);
			break;
		}
	}
}
