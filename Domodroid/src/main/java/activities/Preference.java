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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.domogik.domodroid13.R;

import java.util.List;

import Abstract.pref_utils;
import database.Cache_management;
import misc.tracerengine;

public class Preference extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    private static Preference myself = null;
    private final String mytag = this.getClass().getName();
    private tracerengine Tracer = null;
    private String action;
    private WifiManager mWifiManager;
    private CharSequence[] entries = null;
    private ListPreference prefered_wifi_ssid;
    private pref_utils prefUtils;


    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        AppBarLayout bar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
            root.addView(bar, 0);
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            bar = (AppBarLayout) LayoutInflater.from(this).inflate(R.layout.toolbar_settings, root, false);
            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            } else {
                height = bar.getHeight();
            }
            content.setPadding(0, height, 0, 0);
            root.addView(content);
            root.addView(bar);
        }
        Toolbar Tbar = (Toolbar) bar.getChildAt(0);
        Tbar.setNavigationOnClickListener(new View.OnClickListener() {
                                              public void onClick(View v) {
                                                  finish();
                                              }
                                          }
        );


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracer = tracerengine.getInstance(PreferenceManager.getDefaultSharedPreferences(this), this);
        myself = this;
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        prefUtils = new pref_utils();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPreferenceScreen(null);
        action = getIntent().getAction();
        if (action != null && action.equals("preferences_server")) {
            addPreferencesFromResource(R.xml.preferences_server);
            registerReceiver(mWifiScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            mWifiManager.startScan();
            prefered_wifi_ssid = (ListPreference) findPreference("prefered_wifi_ssid");

            entries = new String[1];
            entries[0] = getString(R.string.wait_wifi_scan_result);
            prefered_wifi_ssid.setEntries(entries);
            prefered_wifi_ssid.setEntryValues(entries);

        } else if (action != null && action.equals("preferences_mq")) {
            addPreferencesFromResource(R.xml.preferences_mq);
        } else if (action != null && action.equals("preferences_widget")) {
            addPreferencesFromResource(R.xml.preferences_widget);
        } else if (action != null && action.equals("preferences_map")) {
            addPreferencesFromResource(R.xml.preferences_map);
        } else if (action != null && action.equals("preferences_house")) {
            addPreferencesFromResource(R.xml.preferences_house);
        } else if (action != null && action.equals("preferences_butler")) {
            addPreferencesFromResource(R.xml.preferences_butler);
        } else if (action != null && action.equals("preferences_debug")) {
            addPreferencesFromResource(R.xml.preferences_debug);
        } else {
            addPreferencesFromResource(R.xml.preference);
        }

        /// Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
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
        if (key.equals("load_area_at_start")) {
            prefUtils.SetWidgetByUsage(true);
        }
        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }


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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Create and correct rinor_Ip to add http:// on start or remove http:// to be used by mq and sync part
        String temp = prefUtils.GetRestIp();
        Boolean SSL = prefUtils.GetRestSsl();
        if (!temp.toLowerCase().startsWith("http://") && !temp.toLowerCase().startsWith("https://")) {
            if (SSL) {
                prefUtils.SetRestIp("https://" + temp);
            } else {
                prefUtils.SetRestIp("http://" + temp);
            }
        } else if (temp.toLowerCase().startsWith("http://") || temp.toLowerCase().startsWith("https://")) {
            temp = temp.replace("https://", "");
            temp = temp.replace("http://", "");
            if (SSL) {
                prefUtils.SetRestIp("https://" + temp);
            } else {
                prefUtils.SetRestIp("http://" + temp);
            }
        }
        String extrenal_temp = prefUtils.GetExternalRestIp();
        Boolean ExternalSSL = prefUtils.GetExternalRestSsl();
        if (!extrenal_temp.toLowerCase().startsWith("http://") && !extrenal_temp.toLowerCase().startsWith("https://")) {
            if (ExternalSSL) {
                prefUtils.SetExternalRestIp("https://" + extrenal_temp);
            } else {
                prefUtils.SetExternalRestIp("http://" + extrenal_temp);
            }
        } else if (extrenal_temp.toLowerCase().startsWith("http://") || extrenal_temp.toLowerCase().startsWith("https://")) {
            extrenal_temp = extrenal_temp.replace("https://", "");
            extrenal_temp = extrenal_temp.replace("http://", "");
            if (ExternalSSL) {
                prefUtils.SetExternalRestIp("https://" + extrenal_temp);
            } else {
                prefUtils.SetExternalRestIp("http://" + extrenal_temp);
            }
        }

        //refresh URL address
        String urlAccess = prefUtils.GetRestIp() + ":" + prefUtils.GetRestPort() + prefUtils.GetRestPath();
        urlAccess = urlAccess.replaceAll("[\r\n]+", "");
        urlAccess = urlAccess.replaceAll(" ", "%20");
        String format_urlAccess;
        if (urlAccess.lastIndexOf("/") == urlAccess.length() - 1)
            format_urlAccess = urlAccess;
        else
            format_urlAccess = urlAccess.concat("/");
        prefUtils.SetUrl(format_urlAccess);

        String external_urlAccess = prefUtils.GetExternalRestIp() + ":" + prefUtils.GetExternalRestPort() + prefUtils.GetRestPath();
        external_urlAccess = external_urlAccess.replaceAll("[\r\n]+", "");
        external_urlAccess = external_urlAccess.replaceAll(" ", "%20");
        String external_format_urlAccess;
        if (external_urlAccess.lastIndexOf("/") == external_urlAccess.length() - 1)
            external_format_urlAccess = external_urlAccess;
        else
            external_format_urlAccess = external_urlAccess.concat("/");
        prefUtils.SetExternalUrl(external_format_urlAccess);

        //Save to file
        String mytag = "Preference";
        prefUtils.save_params_to_file(Tracer, mytag, this);

        //refresh cache address.
        Cache_management.checkcache(Tracer, this);
        Tracer.d(mytag, "End destroy activity");
        try {
            //because if not registered it crash.
            if (mWifiScanReceiver != null)
                unregisterReceiver(mWifiScanReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> mScanResults = mWifiManager.getScanResults();
                entries = new String[mScanResults.size()];
                for (int i = 0; i < mScanResults.size(); i++) {
                    entries[i] = (mScanResults.get(i)).SSID;
                }
                prefered_wifi_ssid.setEntries(entries);
                prefered_wifi_ssid.setEntryValues(entries);
            }
        }
    };
}