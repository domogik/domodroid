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


import org.domogik.domodroid13.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;
 
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;

public class Preference extends PreferenceActivity implements
    OnSharedPreferenceChangeListener {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preference);
    
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
      preference.setSummary(editTextPref.getText());
    }
  }
} 