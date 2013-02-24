package activities;

import java.util.ArrayList;
import org.domogik.domodroid.R;
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
import android.widget.SeekBar;
import android.widget.TextView;

public class Dialog_Map extends Dialog implements OnClickListener {
	private Button cancelButton;
	private Button OKButton;
	private CheckBox checkbox3;
	private CheckBox checkbox4;
	private CheckBox hidecheckbox; //Custom name option
	private TextView mProgressText3;
	private SeekBar mSeekBar3;
	private int sizeOffset = 300;
	
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;

	public Dialog_Map(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_map);
		//Get handlers on screen objects....
		cancelButton = (Button) findViewById(R.id.map_Cancel);
		cancelButton.setTag("map_cancel");
		cancelButton.setOnClickListener(this);
		OKButton = (Button) findViewById(R.id.map_OK);
		OKButton.setTag("map_ok");
		OKButton.setOnClickListener(this);
		//Hide name option
		checkbox3 = (CheckBox)findViewById(R.id.checkbox3);
		checkbox4 = (CheckBox)findViewById(R.id.checkbox4);
		hidecheckbox = (CheckBox)findViewById(R.id.hidecheckbox);		
		mProgressText3 = (TextView)findViewById(R.id.progress3);
		mSeekBar3=(SeekBar)findViewById(R.id.SeekBar3);
				
		get_params();	//retrieve actual settings and prepare to display them
		
	}

	public void get_params() {
		checkbox3.setChecked(params.getBoolean("DRAG", true));
		checkbox4.setChecked(params.getBoolean("ZOOM", true));
		hidecheckbox.setChecked(params.getBoolean("HIDE", true));
		mSeekBar3.setProgress(params.getInt("SIZE", 800)-sizeOffset);
		
		
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("map_cancel"))
			dismiss();
		else if (tag.equals("map_ok")) {
			//Something has been changed : store values in params
			prefEditor=params.edit();
			prefEditor.putBoolean("DRAG", checkbox3.isChecked());
			prefEditor.putBoolean("ZOOM", checkbox4.isChecked());
			//Custom name option
			prefEditor.putBoolean("HIDE", hidecheckbox.isChecked());
			prefEditor.putInt("SIZE", mSeekBar3.getProgress()+sizeOffset);
			
			prefEditor.commit();
			// And force tracer to consider them
			Tracer.set_profile(params);
			dismiss();
		}
	}
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		mProgressText3.setText((progress+sizeOffset)+" px");	
	}
	
	
	
	
}

