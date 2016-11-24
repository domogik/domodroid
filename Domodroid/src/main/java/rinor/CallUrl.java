package rinor;

import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.domogik.domodroid13.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;

import javax.net.ssl.HttpsURLConnection;

import static activities.Activity_Main.context;

/**
 * Created by fritz on 07/09/15.
 * Call with url,login,password,timeout
 * all those parameters should be String
 */
public class CallUrl extends AsyncTask<String, Void, String> {
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
        if (!SSL) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
            HttpConnectionParams.setSoTimeout(httpParameters, timeout);
            DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
            httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
            HttpResponse response;
            String responseString = "";
            try {
                response = httpclient.execute(new HttpGet(url));
                StatusLine statusLine = response.getStatusLine();
                stats_com.add(Stats_Com.EVENTS_SEND, httpclient.getRequestInterceptorCount());
                stats_com.add(Stats_Com.EVENTS_RCV, httpclient.getResponseInterceptorCount());
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else {
                    //Closes the connection.
                    try {
                        response.getEntity().getContent().close();
                    } catch (Exception e1) {
                        //TODO Handle problems..
                    }
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                responseString = "ERROR";
            } catch (IOException e) {
                e.printStackTrace();
                //TODO Handle problems..
                if (e.getMessage().equals("NOT FOUND")) {
                    responseString = "ERROR";
                }
            }
            return responseString;
        } else {
            String responseMessage = "";
            try {
                if (url.startsWith("http://")) {
                    url = url.replace("http://", "https://");
                }
                final HttpsURLConnection urlConnection = Abstract.httpsUrl.setUpHttpsConnection(url);
                Authenticator.setDefault(new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(login, password.toCharArray());
                    }
                });
                String result = null;
                InputStream instream = urlConnection.getInputStream();
                // Read response headers
                int responseCode = urlConnection.getResponseCode();
                responseMessage = urlConnection.getResponseMessage();
                result = Abstract.httpsUrl.convertStreamToString(instream);
                stats_com.add(Stats_Com.EVENTS_SEND, urlConnection.getContentLength());
                stats_com.add(Stats_Com.EVENTS_RCV, responseMessage.length());
                instream.close();
                //} catch (HttpHostConnectException e) {
                //    e.printStackTrace();
            } catch (java.net.SocketTimeoutException e) {
                e.printStackTrace();
                responseMessage = "ERROR";
            } catch (IOException e) {
                //TODO Handle problems..
                e.printStackTrace();
                if (e.getMessage().equals("NOT FOUND")) {
                    responseMessage = "ERROR";
                }
            }
            return responseMessage;
        }
    }
/*
    @Override
    protected void onPreExecute() {
        // This method will called during doInBackground is in process
        // Here you can for example show a ProgressDialog
    }

    */
    protected void onPostExecute(String string) {
        // onPostExecute is called when doInBackground finished
        // Here you can for example fill your Listview with the content loaded in doInBackground method
        if (string.equals("ERROR")) {
            Toast.makeText(context, R.string.rinor_command_exception, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.command_sent, Toast.LENGTH_SHORT).show();
        }


    }
}
