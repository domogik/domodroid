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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Dialog_Server extends Dialog implements OnClickListener,OnSeekBarChangeListener {
	private Button cancelButton;
	private Button OKButton;
	private EditText localIP;
	private EditText localPORT;
	private String format_urlAccess;
	public static String urlAccess;
	private TextView mProgressText1;
	private SeekBar mSeekBar1;
	private EditText http_auth_username;
	private EditText http_auth_password;
	
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private Activity context;
	private tracerengine Tracer = null;
	private int secondeOffset = 5;
	
	public Dialog_Server(tracerengine Trac, SharedPreferences params, Activity context) {
		super(context);
		this.context = context;
		this.params = params;
		this.Tracer = Trac;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_server);
		//Get handlers on screen objects....
		cancelButton = (Button) findViewById(R.id.server_Cancel);
		cancelButton.setTag("server_cancel");
		cancelButton.setOnClickListener(this);
		OKButton = (Button) findViewById(R.id.server_OK);
		OKButton.setTag("server_ok");
		OKButton.setOnClickListener(this);
		localIP = (EditText)findViewById(R.id.server_localIP);
		localPORT = (EditText)findViewById(R.id.server_localPORT);
		http_auth_username = (EditText)findViewById(R.id.server_http_user);
		http_auth_password = (EditText)findViewById(R.id.server_http_password);
		mProgressText1 = (TextView)findViewById(R.id.server_progress1);
		mSeekBar1=(SeekBar)findViewById(R.id.server_SeekBar1);
		mSeekBar1.setOnSeekBarChangeListener(this);

				
		get_params();	//retrieve actual settings and prepare to display them
		
	}

	public void get_params() {
		localIP.setText(params.getString("rinorIP",null));
		localPORT.setText(params.getString("rinorPort","40405"));
		mSeekBar1.setProgress(params.getInt("UPDATE_TIMER", 300)-secondeOffset);
		http_auth_username.setText(params.getString("http_auth_username",null));
		http_auth_password.setText(params.getString("http_auth_password",null));
		
	}
	
	public void onClick(View v) {
		String tag = v.getTag().toString();
		if (tag.equals("server_cancel"))
			dismiss();
		else if (tag.equals("server_ok")) {
			prefEditor=params.edit();
			try{
				//Something has been changed : store values in params
				prefEditor=params.edit();
				if (localIP.getText().toString().equals("")){
					//TODO do something if it's null			
				}
				//Change rinor ip to add http:// if not 
				if (!localIP.getText().toString().startsWith("http://")){
					localIP.setText("http://"+localIP.getText().toString());
				}
				prefEditor.putString("rinorIP",localIP.getText().toString());
				prefEditor.putString("rinorPort",localPORT.getText().toString());
				prefEditor.putString("http_auth_username",http_auth_username.getText().toString());
				prefEditor.putString("http_auth_password",http_auth_password.getText().toString());
				int period = mSeekBar1.getProgress();
				if(period < secondeOffset)
					period = secondeOffset;
				prefEditor.putInt("UPDATE_TIMER", period);
				urlAccess = localIP.getText().toString()+":"+localPORT.getText().toString();
				urlAccess = urlAccess.replaceAll("[\r\n]+", "");
				//Try to solve #1623
				urlAccess = urlAccess.replaceAll(" ", "%20");
				//add a '/' at the end of the IP address
				if(urlAccess.lastIndexOf("/")==localIP.getText().toString().length()-1) 
					format_urlAccess = urlAccess;
				else 
					format_urlAccess = urlAccess.concat("/");
				prefEditor.putString("URL",format_urlAccess);
				prefEditor.commit();
				
			} catch(Exception e){}
			
			prefEditor.commit();
			
			
			dismiss();
		}
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
		int	value = progress;
		if(seekBar.getId()==R.id.server_SeekBar1) {
			int	value1 = progress;
			if(value1 < secondeOffset) {
				value1 = secondeOffset;
				seekBar.setProgress(value1);
			}
			mProgressText1.setText((value1)+" "+context.getString(R.string.seconds));
		}
		/* else if(seekBar.getId()==R.id.SeekBar2) {
			mProgressText2.setText( (Integer.toString(progress+dayOffset))+ getText(R.string.network_Text11a));
		}*/else{
			
		}
	}
		
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	
}

