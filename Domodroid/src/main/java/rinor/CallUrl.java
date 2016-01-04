package rinor;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by fritz on 07/09/15.
 * Call with url,login,password,timeout
 * all those parameters should be String
 */
public class CallUrl extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... uri) {
        // TODO : use non deprecated functions
        String url = uri[0];
        String login = uri[1];
        String password = uri[2];
        int timeout = Integer.parseInt(uri[3]);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeout);
        HttpConnectionParams.setSoTimeout(httpParameters, timeout);
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(login + ":" + password));
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
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
        } catch (IOException e) {
            //TODO Handle problems..
            // Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
            // Toast.makeText(context, "Rinor exception sending command", Toast.LENGTH_LONG).show();
        }
        return responseString;
    }
/*
    @Override
    protected void onPreExecute() {
        // This method will called during doInBackground is in process
        // Here you can for example show a ProgressDialog
    }

    @Override
    protected void onPostExecute(Long result) {
        // onPostExecute is called when doInBackground finished
        // Here you can for example fill your Listview with the content loaded in doInBackground method

    }
*/
}
