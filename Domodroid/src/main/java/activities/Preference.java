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


import Abstract.common_method;
import misc.tracerengine;

import org.domogik.domodroid13.R;

import database.Cache_management;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

public class Preference extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    private Preference myself = null;

    private static tracerengine Tracer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preference, false);
        addPreferencesFromResource(R.xml.preference);
        Tracer = tracerengine.getInstance(PreferenceManager.getDefaultSharedPreferences(this), this);
        myself = this;
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
        //Create and correct rinor_Ip to add http:// on start or remove http:// to be used by mq and sync part
        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this);
        String temp = params.getString("rinorIP", "");
        SharedPreferences.Editor prefEditor;
        if (!temp.toUpperCase().startsWith("HTTP://")) {
            PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefEditor = params.edit();
            prefEditor.putString("rinor_IP", "http://" + temp);
            prefEditor.commit();
        } else if (temp.toUpperCase().startsWith("HTTP://")) {
            PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefEditor = params.edit();
            prefEditor.putString("rinorIP", temp.replace("http://", ""));
            prefEditor.commit();
        }
        //refresh URL address
        prefEditor = params.edit();
        String urlAccess = params.getString("rinor_IP", "1.1.1.1") + ":" + params.getString("rinorPort", "40405") + params.getString("rinorPath", "/");
        urlAccess = urlAccess.replaceAll("[\r\n]+", "");
        urlAccess = urlAccess.replaceAll(" ", "%20");
        String format_urlAccess;
        if (urlAccess.lastIndexOf("/") == urlAccess.length() - 1)
            format_urlAccess = urlAccess;
        else
            format_urlAccess = urlAccess.concat("/");
        prefEditor.putString("URL", format_urlAccess);
        prefEditor.commit();

        //Save to file
        String mytag = "Preference";
        common_method.save_params_to_file(Tracer, prefEditor, mytag, this);

        urlAccess = params.getString("URL", "1.1.1.1");
        //refresh cache address.
        Cache_management.checkcache(Tracer, myself);
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

} 