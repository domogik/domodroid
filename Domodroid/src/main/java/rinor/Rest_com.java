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
 *
 *
 * A special thanks go to Chteuteu as he allow us to re-use parts of his
 * code from Munin-for-Android (https://github.com/chteuchteu/Munin-for-Android)
 *
 *
 */
package rinor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.domogik.domodroid13.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import applications.domodroid;
import misc.tracerengine;


public class Rest_com {
    private static final String mytag = "Rest_com";
    private static boolean alreadyTriedAuthenticating = false;

    @SuppressWarnings("null")
    public static JSONObject connect_jsonobject(Activity activity, tracerengine Tracer, String request, int timeout) {
        JSONObject json = null;
        try {
            json = new JSONObject(connect_string(activity, Tracer, request, timeout));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @SuppressWarnings("null")
    public static JSONArray connect_jsonarray(Activity activity, tracerengine Tracer, String request, int timeout) {
        JSONArray json = null;
        try {
            json = new JSONArray(connect_string(activity, Tracer, request, timeout));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static String connect_string(final Activity activity, tracerengine Tracer, String request, int timeout) {
        SharedPreferences SP_params = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        final String login = SP_params.getString("http_auth_username", "Anonymous");
        final String password = SP_params.getString("http_auth_password", "");
        Boolean SSL;

        String url;

        String result = "";
        //check if device is connected
        if (domodroid.instance.isConnected()) {
            if (domodroid.instance.on_preferred_Wifi) {
                //If connected to default SSID use local adress
                url = SP_params.getString("URL", "1.1.1.1") + request;
                SSL = SP_params.getBoolean("ssl_activate", false);
            } else {
                //If not connected to default SSID use external adress
                url = SP_params.getString("external_URL", "1.1.1.1") + request;
                SSL = SP_params.getBoolean("ssl_external_activate", false);
            }
            if (!SSL) {
                try {
                    // Set timeout
                    Tracer.d(mytag, "Url=" + url);
                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
                    HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                    DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
                    HttpGet httpget = new HttpGet(url);
                    httpget.addHeader("Authorization", "Basic " + Base64.encodeToString((login + ":" + password).getBytes(), Base64.NO_WRAP));
                    final HttpResponse response;
                    response = httpclient.execute(httpget);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            InputStream instream = entity.getContent();
                            result = Abstract.httpsUrl.convertStreamToString(instream);
                            instream.close();
                        }
                    } else if (response.getStatusLine().getStatusCode() == 204) {
                        //TODO need to adapt for 0.4 since rest answer now with standard code
                        //204,400,404 and else
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, R.string.Rest_204_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (response.getStatusLine().getStatusCode() == 301) {
                        //Todo handle redirection
                    } else {
                        Tracer.d(mytag, "Resource not available");
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, R.string.ressource_not_available + response.getStatusLine().getStatusCode(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (UnknownHostException e) {
                    Tracer.e(mytag, "Unable to resolve host");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.host_un_resolvable, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ConnectTimeoutException e) {
                    Tracer.e(mytag, "Timeout connecting to domogik");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.timout_rest, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (HttpHostConnectException e) {
                    Tracer.e(mytag, "Host connection exception");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.rest_host_connection_exception, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Tracer.e(mytag, e.toString());
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.rest_connection_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return result;
            } else {
                try {
                    Tracer.d(mytag, "Start https connection");
                    if (url.startsWith("http://")) {
                        url = url.replace("http://", "https://");
                    }
                    Tracer.d(mytag, "Url=" + url);
                    HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url, login, password);
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(login, password.toCharArray());
                        }
                    });
                    if (urlConnection.getResponseCode() == 200) {
                        InputStream instream = urlConnection.getInputStream();
                        result = Abstract.httpsUrl.convertStreamToString(instream);
                        instream.close();
                    } else if (urlConnection.getResponseCode() == 301) {
                        //Todo handle redirection
                    } else {
                        Tracer.e(mytag, "urlConnection.getResponseCode() == " + urlConnection.getResponseCode());
                    }

                } catch (UnknownHostException e) {
                    Tracer.e(mytag, "Unable to resolve host");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.host_un_resolvable, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ConnectTimeoutException e) {
                    Tracer.e(mytag, "Timeout connecting to domogik");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.timout_rest, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (HttpHostConnectException e) {
                    Tracer.e(mytag, "Host connection exception");
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.rest_host_connection_exception, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException e) {
                    Tracer.e(mytag, e.toString());
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.rest_io_connection_error, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Tracer.e(mytag, e.toString());
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, R.string.rest_connection_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                Tracer.d(mytag, "result https connection = " + result);
                return result;

            }
        } else {
            Tracer.e(mytag, "NO CONNECTION");
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, R.string.no_connection, Toast.LENGTH_LONG).show();
                }
            });
            return result;
        }
    }

}
/*Todo replace by a simpler method
// for now i get: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
      try {
            result = new CallUrl().execute(url, login, password, "3000", String.valueOf(SSL)).get();
        } catch (Exception e) {
            Tracer.e(mytag, e.toString());
        }
        return result;
  */