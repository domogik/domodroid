package map;

import org.domogik.domodroid13.R;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class Dialog_Help extends Dialog implements OnClickListener {
	private final Button okButton;

	public Dialog_Help(Context context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.dialog_help);
		okButton = (Button) findViewById(R.id.OkButton);
		okButton.setOnClickListener(this);

	}

	
	public void onClick(View v) {
		/** When OK Button is clicked, dismiss the dialog */
		if (v == okButton)
			dismiss();
	}
}