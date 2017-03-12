package activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import Abstract.pref_utils;
import applications.domodroid;

/**
 * Created by tiki on 24/12/2016.
 */

public class webview_domogik_admin extends Activity {

    private WebView webView;
    private WebView myWebView;
    private pref_utils prefUtils;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new pref_utils();

        setContentView(R.layout.domogik_admin_webview);
        myWebView = (WebView) findViewById(R.id.webview);

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        myWebView.setWebViewClient(new MyWebViewClient());
        if (domodroid.instance.isConnected()) {
            String url;
            String port;
            Boolean SSL = false;

            if (domodroid.instance.on_preferred_Wifi) {
                //If connected to default SSID use local adress
                url = prefUtils.GetRestIp();
                port = prefUtils.GetRestPort();
                //SSL = prefUtils.GetRestSsl();
            } else {
                //If not connected to default SSID use external adress
                url = prefUtils.GetExternalRestIp();
                port = prefUtils.GetExternalRestPort();
                //SSL = prefUtils.GetExternalRestSsl();
            }
            /*if (!SSL) {
                myWebView.loadUrl(url + ":" + port);
            } else {
                myWebView.loadUrl("https://" + url + ":" + port);
            }*/
            myWebView.loadUrl(url + ":" + port);
        } else {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_LONG).show();
            this.finish();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {
        //Allow to open webview even if untrusted SSL cert
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            if (!prefUtils.GetSslTrusted()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(myWebView.getContext());
                int message;
                switch (error.getPrimaryError()) {
                    case SslError.SSL_DATE_INVALID:
                        message = R.string.notification_error_ssl_date_invalid;
                        break;
                    case SslError.SSL_EXPIRED:
                        message = R.string.notification_error_ssl_expired;
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = R.string.notification_error_ssl_idmismatch;
                        break;
                    case SslError.SSL_INVALID:
                        message = R.string.notification_error_ssl_invalid;
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = R.string.notification_error_ssl_not_yet_valid;
                        break;
                    case SslError.SSL_UNTRUSTED:
                        message = R.string.notification_error_ssl_untrusted;
                        break;
                    default:
                        message = R.string.notification_error_ssl_cert_invalid;
                }
                builder.setMessage(message);
                builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefUtils.SetSslTrusted(true);
                        handler.proceed();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.cancel();
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                handler.proceed();
            }
        }
    }
}