package activities;

import org.domogik.domodroid13.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Dialog_Map extends Dialog implements OnClickListener,OnSeekBarChangeListener {
	private Button cancelButton;
	private Button OKButton;
	private CheckBox checkbox_drag;
	private CheckBox checkbox_zoom;
	private CheckBox checkbox_hide; //Custom name option
	private CheckBox checkbox_menu_disable; //Custom name option
	private CheckBox checkbox_start_on_map; //option to start immediately on Map View
	private CheckBox map_autozoom; //if activate zoom will be auto
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
		checkbox_drag = (CheckBox)findViewById(R.id.map_checkbox_drag);
		checkbox_zoom = (CheckBox)findViewById(R.id.map_checkbox_zoom);
		checkbox_hide = (CheckBox)findViewById(R.id.map_checkbox_hide);		
		checkbox_menu_disable = (CheckBox)findViewById(R.id.map_checkbox_menu_disable);		
		checkbox_start_on_map = (CheckBox)findViewById(R.id.map_checkbox_start_on_map);		
		mProgressText3 = (TextView)findViewById(R.id.map_progress3);
		mSeekBar3=(SeekBar)findViewById(R.id.map_SeekBar3);
		mSeekBar3.setTag("seekbar3");
		mSeekBar3.setOnSeekBarChangeListener(this);
		map_autozoom = (CheckBox)findViewById(R.id.map_autozoom);
				
		get_params();	//retrieve actual settings and prepare to display them
		
	}

	public void get_params() {
		checkbox_drag.setChecked(params.getBoolean("DRAG", true));
		checkbox_zoom.setChecked(params.getBoolean("ZOOM", true));
		checkbox_hide.setChecked(params.getBoolean("HIDE", false));
		checkbox_menu_disable.setChecked(params.getBoolean("map_menu_disable", false));
		checkbox_start_on_map.setChecked(params.getBoolean("START_ON_MAP", false));
		mSeekBar3.setProgress(params.getInt("SIZE", 800)-sizeOffset);
		map_autozoom.setChecked(params.getBoolean("map_autozoom",false));
		
		
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("map_cancel"))
			dismiss();
		else if (tag.equals("map_ok")) {
			//Something has been changed : store values in params
			prefEditor=params.edit();
			prefEditor.putBoolean("DRAG", checkbox_drag.isChecked());
			prefEditor.putBoolean("ZOOM", checkbox_zoom.isChecked());
			//Custom name option
			prefEditor.putBoolean("HIDE", checkbox_hide.isChecked());
			prefEditor.putBoolean("map_menu_disable", checkbox_menu_disable.isChecked());
			prefEditor.putBoolean("START_ON_MAP", checkbox_start_on_map.isChecked());
			prefEditor.putInt("SIZE", mSeekBar3.getProgress()+sizeOffset);
			prefEditor.putBoolean("map_autozoom", map_autozoom.isChecked());
			
			prefEditor.commit();
			
			if(checkbox_start_on_map.isChecked()) {
				Tracer.Map_as_main = true;
			} else {
				Tracer.Map_as_main = false;
			}
			
			dismiss();
		}
	}
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		if(seekBar.getTag().equals("seekbar3")) {
			int	value = progress;
			if(value < sizeOffset) {
				value = sizeOffset;
				seekBar.setProgress(value);
			}
			//Tracer.e("Dialog_Map", "Updating size : "+value+" "+context.getString(R.string.pixels));
			mProgressText3.setText((value)+" "+context.getString(R.string.pixels));
		}
	}
	public void onStartTrackingTouch(SeekBar seekBar) {
	}


	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	
	
	
	
}

