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
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import misc.tracerengine;

  
public class Rest_com {
	private SharedPreferences params;
	private static String mytag = "Rest_com";
	
	private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
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
	public static JSONObject connect(String url,String login, String password)
    {
    	
    	tracerengine Tracer = null;
    	JSONObject json = null;
    		try {
    		DefaultHttpClient httpclient = new DefaultHttpClient();
    		httpclient.getCredentialsProvider().setCredentials( new AuthScope(null, -1),new UsernamePasswordCredentials(login+":"+password));
    		// TODO Set timeout
    		// this doesn't work
    		//httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
    		HttpGet httpget = new HttpGet(url);    		
    		HttpResponse response;
    		String result = null;
            response = httpclient.execute(httpget);
            if(response.getStatusLine().getStatusCode() == 200)
			{
            	HttpEntity entity = response.getEntity();
			    if (entity != null) {
			    	InputStream instream = entity.getContent();
			    	result= convertStreamToString(instream);
			    	json=new JSONObject(result);
			    	instream.close();
			    }
			}
            else
			{
            	Tracer.d(mytag, "Resource not available>");
			}
 
 
        } catch (HttpHostConnectException e) {
        	//e.printStackTrace();
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
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