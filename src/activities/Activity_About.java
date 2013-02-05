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


import org.domogik.domodroid.R;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import misc.Tracer;

import android.widget.FrameLayout;
import android.widget.TextView;

public class Activity_About extends Activity{
    protected PowerManager.WakeLock mWakeLock;
    private String pn = "";
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pn = getPackageName();
		
		setContentView(R.layout.activity_help);	
		TextView versionText = (TextView) findViewById(R.id.versionText);
		if (versionText != null) {
			//set text in the activity_help versiontText textview
			//it's a concatenation of version from string.xml, the versionCode and versionName from AndroidManifest.xml
			String vcs = "??";
			String vns = getVersionName();
			int vc = getVersionCode();
			if(vc != -1)
				vcs=Integer.toString(vc);
			versionText.setText(getString(R.string.version)+"_"+vcs+"_"+vns+" ("+pn+")");
		}
		
		//titlebar
		final FrameLayout titlebar = (FrameLayout) findViewById(R.id.TitleBar);
		titlebar.setBackgroundDrawable(Gradients_Manager.LoadDrawable("title",40));
		
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
		Tracer.i("About","Package name = <"+pn+">");
		try {
			//get versionName from AndroidManifest.xml
			PackageInfo pi = getPackageManager().getPackageInfo( pn, 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Tracer.e("Activity_About", "Version name not found in package");
		}
		Tracer.i("About","Version name = <"+version+">");
		return version;
	}

	private int getVersionCode() {
		//set a fake code
		int version = -1;
		String pn = getPackageName();
		Tracer.i("About","Package name = <"+pn+">");
		try {
			//get versionCode from AndroidManifest.xml
			PackageInfo pi = getPackageManager().getPackageInfo(pn, 0);
			version = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			Tracer.e("Activity_About", "Version number not found in package");
		}
		Tracer.i("About","Version value = <"+version+">");
		return version;
	}
}