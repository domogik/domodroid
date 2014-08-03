package activities;

import org.domogik.domodroid.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import misc.tracerengine;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Dialog_Usage extends Dialog implements OnClickListener {
	private Button cancelButton;
	private Button OKButton;
	private CheckBox WIDGET_CHOICEcheckbox; //Debug option
	private CheckBox Graph_CHOICEcheckbox; //Debug option
	private CheckBox twocollandscapecheckbox; //if activate 2col will be forbid
	private CheckBox twocolportraitcheckbox; //if activate 2col will be forbid
	private CheckBox byusagecheckbox;
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;
	
	public Dialog_Usage(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_usage);
		//Get handlers on screen objects....
		cancelButton = (Button) findViewById(R.id.usage_Cancel);
		cancelButton.setTag("usage_cancel");
		cancelButton.setOnClickListener(this);
		OKButton = (Button) findViewById(R.id.usage_OK);
		OKButton.setTag("usage_ok");
		OKButton.setOnClickListener(this);
		WIDGET_CHOICEcheckbox = (CheckBox)findViewById(R.id.WIDGET_CHOICEcheckbox);
		Graph_CHOICEcheckbox = (CheckBox)findViewById(R.id.Graph_CHOICEcheckbox);
		twocollandscapecheckbox = (CheckBox)findViewById(R.id.twocollandscapecheckbox);
		twocolportraitcheckbox = (CheckBox)findViewById(R.id.twocolportraitcheckbox);
		byusagecheckbox =(CheckBox)findViewById(R.id.usage_byusagecheckbox);
				
		get_params();	//retrieve actual settings and prepare to display them
		
	}

	public void get_params() {
		WIDGET_CHOICEcheckbox.setChecked(params.getBoolean("WIDGET_CHOICE",false));
		Graph_CHOICEcheckbox.setChecked(params.getBoolean("Graph_CHOICE",false));
		twocollandscapecheckbox.setChecked(params.getBoolean("twocol_lanscape",false));
		twocolportraitcheckbox.setChecked(params.getBoolean("twocol_portrait",false));
		byusagecheckbox.setChecked(params.getBoolean("BY_USAGE",false));
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("usage_cancel"))
			dismiss();
		else if (tag.equals("usage_ok")) {
			prefEditor=params.edit();
			try{
				//Something has been changed : store values in params
				prefEditor=params.edit();
				prefEditor.putBoolean("WIDGET_CHOICE", WIDGET_CHOICEcheckbox.isChecked());
				prefEditor.putBoolean("Graph_CHOICE", Graph_CHOICEcheckbox.isChecked());
				prefEditor.putBoolean("twocol_lanscape", twocollandscapecheckbox.isChecked());
				prefEditor.putBoolean("twocol_portrait", twocolportraitcheckbox.isChecked());
				prefEditor.putBoolean("BY_USAGE", byusagecheckbox.isChecked());
				prefEditor.commit();
				
			} catch(Exception e){}
			
			prefEditor.commit();
			
			
			dismiss();
		}
	}
		
}

