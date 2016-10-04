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


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.common_method;
import database.Cache_management;
import misc.tracerengine;

public class Preference extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    private Preference myself = null;
    private final String mytag = this.getClass().getName();
    private static tracerengine Tracer = null;
    private static String contents;
    private String action;

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
        action = getIntent().getAction();
        if (action != null && action.equals("preferences_server")) {
            addPreferencesFromResource(R.xml.preferences_server);
        } else if (action != null && action.equals("scan_qrcode")) {
            try {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
                intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
                startActivityForResult(intent, 0);

            } catch (ActivityNotFoundException anfe) {
                //on catch, show the download dialog
                showDialog(Preference.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
            }
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
        if (action == null || !action.equals("scan_qrcode")) {
            // show the current value in the settings screen
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                initSummary(getPreferenceScreen().getPreference(i));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (action == null || !action.equals("scan_qrcode")) {
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (action == null || !action.equals("scan_qrcode")) {
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Create and correct rinor_Ip to add http:// on start or remove http:// to be used by mq and sync part
        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(this);
        String temp = params.getString("rinorIP", "");
        Boolean SSL = params.getBoolean("ssl_activate", false);
        SharedPreferences.Editor prefEditor;
        if (!temp.toLowerCase().startsWith("http://") && !temp.toLowerCase().startsWith("https://")) {
            PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefEditor = params.edit();
            if (SSL) {
                prefEditor.putString("rinor_IP", "https://" + temp);
            } else {
                prefEditor.putString("rinor_IP", "http://" + temp);
            }
            prefEditor.commit();
        } else if (temp.toLowerCase().startsWith("http://") || temp.toLowerCase().startsWith("https://")) {
            PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefEditor = params.edit();
            if (SSL) {
                prefEditor.putString("rinor_IP", temp.replace("https://", ""));
            } else {
                prefEditor.putString("rinor_IP", temp.replace("http://", ""));
            }
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
            //Add to avoid password in clear in this view
            if (!preference.getKey().equals("http_auth_password"))
                preference.setSummary(editTextPref.getText());
        }
    }

    private AlertDialog showDialog(final Activity act, final CharSequence title, CharSequence message, CharSequence buttonYes, final CharSequence buttonNo) {
        final AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (title.equals("No Scanner Found")) {
                    Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        act.startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {

                    }
                } else if (title.equals("Qrcode is valid")) {
                    Tracer.d("preference", "We got a recult from qrcode scanner:" + contents);
                    try {
                        JSONObject jsonreult = new JSONObject(contents);
                        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(Preference.this);
                        String admin_url = jsonreult.getString("admin_url");
                        int rest_port = jsonreult.getInt("rest_port");
                        String mq_ip = jsonreult.getString("mq_ip");
                        String rest_path = jsonreult.getString("rest_path");
                        int mq_port_pubsub = jsonreult.getInt("mq_port_pubsub");
                        int mq_port_req_rep = jsonreult.getInt("mq_port_req_rep");
                        SharedPreferences.Editor prefEditor;
                        PreferenceManager.getDefaultSharedPreferences(Preference.this).edit();
                        prefEditor = params.edit();
                        if (admin_url.toLowerCase().startsWith("http://")) {
                            prefEditor.putString("rinor_IP", admin_url.replace("http://", ""));
                            prefEditor.putBoolean("ssl_activate", false);
                        } else if (admin_url.toLowerCase().startsWith("https://")) {
                            prefEditor.putString("rinor_IP", admin_url.replace("https://", ""));
                            prefEditor.putBoolean("ssl_activate", true);
                        }
                        prefEditor.putInt("rinorPort", rest_port);
                        prefEditor.putString("rinorPath", rest_path);
                        prefEditor.putString("MQaddress", mq_ip);
                        prefEditor.putInt("MQsubport", mq_port_pubsub);
                        prefEditor.putInt("MQpubport", mq_port_req_rep);

                        prefEditor.commit();
                    } catch (JSONException e) {
                        Tracer.e(mytag, "Error parsing answer of qrode to json: " + e.toString());
                    }
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                showDialog(Preference.this, "Qrcode is valid", contents, "OK", "No").show();
            } else if (resultCode == RESULT_CANCELED) {
                showDialog(Preference.this, "Qrcode results", "No results from qrcode scanner", "Yes", "No").show();
            }
        }
    }
}