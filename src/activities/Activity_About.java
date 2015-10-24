/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * This code to get version number and name is adapt from 
 * http://ballardhack.wordpress.com/2010/09/28/subversion-revision-in-android-app-version-with-eclipse/
 */
package activities;


import misc.changelog;

import org.domogik.domodroid13.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class Activity_About extends Activity implements OnClickListener{
    protected PowerManager.WakeLock mWakeLock;
    private String pn = "";
    private String mytag="Activity_About";
    private Button showchangelog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pn = getPackageName();
		
		setContentView(R.layout.activity_about);
		//display domogik version
		TextView TV_domogikversionText = (TextView) findViewById(R.id.domogikversionText);
		
		SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(this);
		
		TV_domogikversionText.setText("Domogik version: "+SP_params.getString("DOMOGIK-VERSION", ""));
		//display domodroid version
		TextView TV_versionText = (TextView) findViewById(R.id.versionText);
		if (TV_versionText != null) {
			//set text in the activity_help versiontText textview
			//it's a concatenation of version from string.xml, the versionCode and versionName from AndroidManifest.xml
			String vcs = "??";
			String vns = getVersionName();
			int vc = getVersionCode();
			if(vc != -1)
				vcs=Integer.toString(vc);
			TV_versionText.setText(pn+" "+vns+" "+getString(R.string.version)+"_"+vcs);
		}
		
		//titlebar
		final FrameLayout FL_titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		FL_titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));
		showchangelog = (Button) findViewById(R.id.showchangelog);
		showchangelog.setTag("showchangelog");
		showchangelog.setOnClickListener(this);
		
		//power management
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
    }
	
	@Override
    public void onDestroy() {
            this.mWakeLock.release();
            super.onDestroy();
	}
	
	private String getVersionName() {
		//set a fake version
		String version = "??";
		try {
			//get versionName from AndroidManifest.xml
			PackageInfo pi = getPackageManager().getPackageInfo( pn, 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			//Tracer.e(mytag, "Version name not found in package");
		}
		return version;
	}

	private int getVersionCode() {
		//set a fake code
		int version = -1;
		String pn = getPackageName();
		try {
			//get versionCode from AndroidManifest.xml
			PackageInfo pi = getPackageManager().getPackageInfo(pn, 0);
			version = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			//Tracer.e(mytag, "Version number not found in package");
		}
		return version;
	}

	public void onClick(View v) {
		changelog changelog = new changelog(this);
		/** When OK Button is clicked, dismiss the dialog */
		if (v == showchangelog)
			changelog.getFullLogDialog().show();
	}
}