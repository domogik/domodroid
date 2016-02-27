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
package rinor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;

import misc.tracerengine;


public class Rest_com {
    private static final String mytag = "Rest_com";
    private SharedPreferences params;

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("null")
    public static JSONObject connect_jsonobject(String url, String login, String password, int timeout) {

        tracerengine Tracer = null;
        JSONObject json = null;
        try {
            // Set timeout
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));

            HttpGet httpget = new HttpGet(url);
            HttpResponse response;
            String result = null;
            response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    result = convertStreamToString(instream);
                    json = new JSONObject(result);
                    instream.close();
                }
            } else if (response.getStatusLine().getStatusCode() == 204) {
                //TODO need to adapt for 0.4 since rest answer now with standard code
                //204,400,404 and else
                json = new JSONObject();
                json.put("status", "204 NO CONTENT");
            } else {
                Tracer.d(mytag, "Resource not available>");
            }
        } catch (HttpHostConnectException | ClientProtocolException e) {
            Tracer.e(mytag, e.toString());
        } catch (IOException | JSONException e) {
            Tracer.e(mytag, e.toString());
        } catch (Exception e){
            Tracer.e(mytag, e.toString());
        }

        return json;
    }

    @SuppressWarnings("null")
    public static JSONArray connect_jsonarray(String url, String login, String password, int timeout) {

        tracerengine Tracer = null;
        JSONArray json = null;
        try {
            // Set timeout
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
            HttpGet httpget = new HttpGet(url);
            HttpResponse response;
            String result = null;
            response = httpclient.execute(httpget);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    result = convertStreamToString(instream);
                    json = new JSONArray(result);
                    instream.close();
                }
            } else {
                Tracer.d(mytag, "Resource not available>");
            }


        } catch (HttpHostConnectException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void setParams(SharedPreferences params) {
        this.params = params;
    }
}