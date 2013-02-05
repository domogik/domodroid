package misc;

import java.io.File;
import android.util.Log;

public class Tracer {
	// Following booleans define which kind of log is configured
	private static Boolean	to_Android = true;
	private static Boolean	to_txtFile = false;
	private static String filename = null;
	private static Boolean	filemode = false;		//append by default
	
	private File txtFile = null;
	
	public static void d(String tag, String msg) {
		choose_log(0,tag,msg);
	}
	public static void e(String tag, String msg) {
		choose_log(1,tag,msg);
	}
	public static void i(String tag, String msg) {
		choose_log(2,tag,msg);
	}
	public static void v(String tag, String msg) {
		choose_log(3,tag,msg);
	}
	public static void w(String tag, String msg) {
		choose_log(4,tag,msg);
	}
	/*
	 * Configure Tracer profile
	 * if filemode == true : create a new txt file (filepath required)
	 * if filemode == false: write txt file in append mode if filepath exists
	 */
	public void set_profile( Boolean system, Boolean txt, String filepath, Boolean filemode) {
		
		this.filename=filepath;
		this.to_Android = system;
		this.to_txtFile = txt;
		this.filemode = filemode;
		if(filename == null) {
			// If no filename given, no log to file, nor in append !
			to_txtFile = false;
			this.filemode=false;
			if(txtFile != null) {
				txtFile = null;	//close object
			}
		} else {
			// TODO filepath given : try to open it....
			
		}
		
	}
	/*
	 * all modes use this common method, to decide wich kind of logging is configured
	 */
	private static void choose_log(int type, String tag, String msg) {
		
		// if needed, log to Android
		if(to_Android)
			syslog(type,tag,msg);
	}
	/*
	 * method sending messages to system log ( for Eclipse, and CatLog )
	 */
	private static void syslog(int type, String tag, String msg) {
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
