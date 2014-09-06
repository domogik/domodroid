package activities;

import java.util.ArrayList;
import org.domogik.domodroid13.R;
import rinor.Rest_com;
import database.DomodroidDB;
import org.json.JSONArray;
import org.json.JSONObject;
import widgets.Entity_Feature;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class Dialog_Debug extends Dialog implements OnClickListener {
	private Button cancelButton;
	private Button OKButton;
	private CheckBox CB_Debug = null;
	private CheckBox CB_Error = null;
	private CheckBox CB_Info = null;
	private CheckBox CB_Verbose = null;
	private CheckBox CB_Warning = null;
	private CheckBox CB_Syslog = null;
	private CheckBox CB_Screen = null;
	private CheckBox CB_Txtfile = null;
	private TextView db_logpath = null;
	private TextView db_logname = null;
	private CheckBox debugcheckbox; //Debug option
	
	
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;

	public Dialog_Debug(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_debug);
		//Get handlers on screen objects....
		cancelButton = (Button) findViewById(R.id.debug_Cancel);
		cancelButton.setTag("debug_cancel");
		cancelButton.setOnClickListener(this);
		OKButton = (Button) findViewById(R.id.debug_OK);
		OKButton.setTag("debug_ok");
		OKButton.setOnClickListener(this);
		CB_Debug = (CheckBox) findViewById(R.id.checkBox_debug);
		CB_Error = (CheckBox) findViewById(R.id.checkBox_error);
		CB_Info = (CheckBox) findViewById(R.id.checkBox_info);
		CB_Verbose = (CheckBox) findViewById(R.id.checkBox_verbose);
		CB_Warning = (CheckBox) findViewById(R.id.checkBox_warning);
		CB_Syslog =  (CheckBox) findViewById(R.id.checkBox_syslog);
		CB_Screen =  (CheckBox) findViewById(R.id.checkBox_screen);
		CB_Txtfile = (CheckBox) findViewById(R.id.checkBox_file);
		db_logpath =  (TextView) findViewById(R.id.dirpath);
		db_logname =  (TextView) findViewById(R.id.filename);
		//Debug option
		debugcheckbox = (CheckBox)findViewById(R.id.debugcheckbox);
				
		get_params();	//retrieve actual settings and prepare to display them
		
	}

	public void get_params() {
		CB_Debug.setChecked(params.getBoolean("LOG_DEBUG", true));
		CB_Error.setChecked(params.getBoolean("LOG_ERROR", true));
		CB_Info.setChecked(params.getBoolean("LOG_INFO", true));
		CB_Verbose.setChecked(params.getBoolean("LOG_VERBOSE", true));
		CB_Warning.setChecked(params.getBoolean("LOG_WARNING", true));
		
		CB_Syslog.setChecked(params.getBoolean("SYSTEMLOG", true));
		CB_Screen.setChecked(params.getBoolean("SCREENLOG", true));
		CB_Txtfile.setChecked(params.getBoolean("TEXTLOG", true));
		
		db_logpath.setText(params.getString("LOGPATH", ""));
		db_logname.setText(params.getString("LOGNAME", ""));
		//Debug option
		debugcheckbox.setChecked(params.getBoolean("DEV",false));
				
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("debug_cancel"))
			dismiss();
		else if (tag.equals("debug_ok")) {
			//Something has been changed : store values in params
			prefEditor=params.edit();
			prefEditor.putString("LOGPATH",db_logpath.getText().toString());
			prefEditor.putString("LOGNAME",db_logname.getText().toString());
			
			prefEditor.putBoolean("SYSTEMLOG", CB_Syslog.isChecked());
			prefEditor.putBoolean("TEXTLOG", CB_Txtfile.isChecked());
			prefEditor.putBoolean("SCREENLOG", CB_Screen.isChecked());
			
			prefEditor.putBoolean("LOG_DEBUG", CB_Debug.isChecked());
			prefEditor.putBoolean("LOG_ERROR", CB_Error.isChecked());
			prefEditor.putBoolean("LOG_INFO" , CB_Info.isChecked());
			prefEditor.putBoolean("LOG_VERBOSE", CB_Verbose.isChecked());
			prefEditor.putBoolean("LOG_WARNING" , CB_Warning.isChecked());
			
			prefEditor.putBoolean("LOGCHANGED", true);
			prefEditor.putBoolean("LOGAPPEND", false);
			//Debug option
			prefEditor.putBoolean("DEV", debugcheckbox.isChecked());
			prefEditor.commit();
			// And force tracer to consider them
			Tracer.set_profile(params);
			dismiss();
		}
	}
	
	
	
	
}

