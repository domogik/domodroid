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

package activities;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import misc.tracerengine;

import org.domogik.domodroid13.R;

import database.Cache_management;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class Preference extends PreferenceActivity implements
OnSharedPreferenceChangeListener {
	private Preference myself = null;

	private static tracerengine Tracer = null;
	private File backupprefs = new File(Environment.getExternalStorageDirectory()+"/domodroid/.conf/settings");
	private SharedPreferences.Editor prefEditor;
	private SharedPreferences params;
	private String mytag="Preference";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.preference, false);
		addPreferencesFromResource(R.xml.preference);
		Tracer= tracerengine.getInstance(PreferenceManager.getDefaultSharedPreferences(this));
		myself=this;
		// show the current value in the settings screen
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePreferences(findPreference(key));
		//Save to file
		if(backupprefs != null)
			saveSharedPreferencesToFile(backupprefs);	// Store settings to SDcard

		//Create and correct rinor_Ip to add http:// on start or remove http:// to be used by mq and sync part
		params = PreferenceManager.getDefaultSharedPreferences(this);
		String temp = params.getString("rinorIP", "");
		if (!temp.toUpperCase().startsWith("HTTP://")){
			PreferenceManager.getDefaultSharedPreferences(this).edit();
			prefEditor=params.edit();
			prefEditor.putString("rinor_IP", "http://"+temp);
			prefEditor.commit();
		}else if (temp.toUpperCase().startsWith("HTTP://")){
			PreferenceManager.getDefaultSharedPreferences(this).edit();
			prefEditor=params.edit();
			prefEditor.putString("rinorIP", temp.replace("http://", ""));
			prefEditor.commit();
		}
		//refresh URL address
		prefEditor=params.edit();
		String urlAccess = params.getString("rinor_IP","1.1.1.1")+":"+params.getString("rinorPort","40405")+params.getString("rinorPath","/");
		urlAccess = urlAccess.replaceAll("[\r\n]+", "");
		urlAccess = urlAccess.replaceAll(" ", "%20");
		String format_urlAccess;
		if(urlAccess.lastIndexOf("/")==urlAccess.toString().length()-1)
			format_urlAccess = urlAccess;
		else
			format_urlAccess = urlAccess.concat("/");
		prefEditor.putString("URL",format_urlAccess);
		prefEditor.commit();
		urlAccess = params.getString("URL","1.1.1.1");
		//refresh cache address.
		Cache_management.checkcache(Tracer,myself);
		//this.onContentChanged();

	}

	private void initSummary(android.preference.Preference preference) {
		if (preference instanceof PreferenceCategory) {
			PreferenceCategory cat = (PreferenceCategory) preference;
			for (int i = 0; i < cat.getPreferenceCount(); i++) {
				initSummary(cat.getPreference(i));
			}
		} else {
			updatePreferences(preference);
		}
	}

	private void updatePreferences(android.preference.Preference preference) {
		if (preference instanceof EditTextPreference) {
			EditTextPreference editTextPref = (EditTextPreference) preference;
			//Add to avoid password in clear in this view
			if (!preference.getKey().equals("http_auth_password"))
				preference.setSummary(editTextPref.getText());
		}
	}
	private boolean saveSharedPreferencesToFile(File dst) {
		boolean res = false;
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(dst));
			output.writeObject(PreferenceManager.getDefaultSharedPreferences(this).getAll());
			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}
} 