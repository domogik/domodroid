package rinor;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import Event.ConnectivityChangeEvent;
import applications.domodroid;

/**
 * Created by fritz on 07/09/15.
 * Call with url,login,password,timeout
 * all those parameters should be String
 */
class CallUrl extends AsyncTask<String, Void, String> {
    private final String mytag = this.getClass().getName();
    private boolean alreadyTriedAuthenticating = false;
    private Stats_Com stats_com = Stats_Com.getInstance();

    @Override
    protected String doInBackground(String... uri) {
        if (stats_com == null)
            stats_com = Stats_Com.getInstance();
        stats_com.wakeup();
        // TODO : use non deprecated functions
        String url = uri[0];
        final String login = uri[1];
        final String password = uri[2];
        int timeout = Integer.parseInt(uri[3]);
        Boolean SSL = Boolean.valueOf(uri[4]);
        String result = "";
        if (domodroid.instance.isConnected()) {
            String responseString = "ERROR";
            if (!SSL) {
                try {
                    // Set timeout
                    HttpParams httpParameters = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
                    HttpConnectionParams.setSoTimeout(httpParameters, timeout);
                    DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
                    httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
                    Log.e("connect_string", "url=" + url.toString());
                    HttpGet httpget = new HttpGet(url);
                    httpget.addHeader("Authorization", "Basic " + Base64.encodeToString((login + ":" + password).getBytes(), Base64.NO_WRAP));
                    final HttpResponse response;
                    response = httpclient.execute(httpget);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        result = "200";
                    } else if (response.getStatusLine().getStatusCode() == 204) {
                        //TODO need to adapt for 0.4 since rest answer now with standard code
                        //204,400,404 and else
                        result = "204";
                    } else {
                        result = String.valueOf(response.getStatusLine().getStatusCode());
                    }
                } catch (UnknownHostException e) {
                    result = "UnknownHostException";
                } catch (ConnectTimeoutException e) {
                    result = "ConnectTimeoutException";
                } catch (HttpHostConnectException e) {
                    result = "HttpHostConnectException";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            } else {
                try {
                    if (url.startsWith("http://")) {
                        url = url.replace("http://", "https://");
                    }
                    HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url, login, password);
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(login, password.toCharArray());
                        }
                    });
                    InputStream instream = urlConnection.getInputStream();
                    // Read response headers
                    result = String.valueOf(urlConnection.getResponseCode());
                    instream.close();
                } catch (UnknownHostException e) {
                    result = "UnknownHostException";
                } catch (ConnectTimeoutException e) {
                    result = "ConnectTimeoutException";
                } catch (HttpHostConnectException e) {
                    result = "HttpHostConnectException";
                } catch (IOException e) {
                    result = "IOException";
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;

            }
        }
        return "NO CONNECTION";
    }

    @Override
    protected void onPreExecute() {
        // This method will called during doInBackground is in process
        // Here you can for example show a ProgressDialog
        Toast.makeText(domodroid.GetInstance(), R.string.command_sending, Toast.LENGTH_SHORT).show();
    }

    protected void onPostExecute(String string) {
        Log.e(mytag, "Response from rest is: " + string);
        // onPostExecute is called when doInBackground finished
        // switch send command answer
        switch (string) {
            case "ERROR":
                Toast.makeText(domodroid.GetInstance(), R.string.rinor_command_exception, Toast.LENGTH_LONG).show();
                Toast.makeText(domodroid.GetInstance(), R.string.rest_connection_error, Toast.LENGTH_LONG).show();
                break;
            case "NO CONNECTION":
                Toast.makeText(domodroid.GetInstance(), R.string.no_connection_send_command, Toast.LENGTH_LONG).show();
                break;
            case "UnknownHostException":
                Toast.makeText(domodroid.GetInstance(), R.string.host_un_resolvable, Toast.LENGTH_LONG).show();
                break;
            case "ConnectTimeoutException":
                Toast.makeText(domodroid.GetInstance(), R.string.timout_rest, Toast.LENGTH_LONG).show();
                break;
            case "HttpHostConnectException":
                Toast.makeText(domodroid.GetInstance(), R.string.rest_host_connection_exception, Toast.LENGTH_LONG).show();
                break;
            case "IOException":
                Toast.makeText(domodroid.GetInstance(), R.string.rest_io_connection_error, Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(domodroid.GetInstance(), R.string.command_sent, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
