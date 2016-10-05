package activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import misc.tracerengine;


public class config_with_qrcode extends AppCompatActivity {
    private static String contents;
    private final String mytag = this.getClass().getName();
    private static tracerengine Tracer = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracer = tracerengine.getInstance(PreferenceManager.getDefaultSharedPreferences(this), this);

        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
            intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
            startActivityForResult(intent, 0);

        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(config_with_qrcode.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
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
                        JSONObject jsonresult = null;
                        jsonresult = new JSONObject(contents);
                        SharedPreferences params = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor prefEditor;
                        String admin_url = jsonresult.getString("admin_url");
                        String[] separated = admin_url.split("://");
                        String[] separatedbis = separated[1].split(":");
                        String admin_ip = separatedbis[0];
                        String[] separatedter = separatedbis[1].split("/");
                        String rest_port = separatedter[0];
                        String rest_path = null;
                        try {
                            rest_path = separatedter[1] + jsonresult.getString("rest_path");
                        } catch (ArrayIndexOutOfBoundsException exec) {
                            rest_path = jsonresult.getString("rest_path");
                        }
                        //String rest_port = jsonresult.getString("rest_port");
                        String mq_ip = jsonresult.getString("mq_ip");
                        //String rest_path = jsonresult.getString("rest_path");
                        String mq_port_pubsub = jsonresult.getString("mq_port_pubsub");
                        String mq_port_req_rep = jsonresult.getString("mq_port_req_rep");
                        String rinor_IP = null;
                        Boolean SSL = null;
                        if (separated[0].toLowerCase().equals("http")) {
                            rinor_IP = admin_ip.replace("http://", "");
                            SSL = false;
                        } else if (separated[0].toLowerCase().equals("https")) {
                            rinor_IP = admin_ip.replace("https://", "");
                            SSL = true;
                        }
                        prefEditor = params.edit();
                        prefEditor.putString("rinor_IP", rinor_IP);
                        prefEditor.putString("rinorPort", rest_port);
                        prefEditor.putString("rinorPath", rest_path);
                        prefEditor.putBoolean("ssl_activate", SSL);
                        prefEditor.putString("MQaddress", mq_ip);
                        prefEditor.putString("MQsubport", mq_port_pubsub);
                        prefEditor.putString("MQpubport", mq_port_req_rep);
                        prefEditor.commit();

                        Tracer.e(mytag, "rinor_IP: " + admin_ip);
                        Tracer.e(mytag, "rinor_IP: " + params.getString("rinor_IP", "1.1.1.1"));

                        Tracer.e(mytag, "admin_url: " + admin_url);
                        Tracer.e(mytag, "rest_port: " + rest_port);
                        Tracer.e(mytag, "mq_ip: " + mq_ip);
                        Tracer.e(mytag, "rest_path: " + rest_path);
                        Tracer.e(mytag, "mq_port_pubsub: " + mq_port_pubsub);
                        Tracer.e(mytag, "mq_port_req_rep: " + mq_port_req_rep);

                        config_with_qrcode.this.finish();

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
                showDialog(config_with_qrcode.this, "Qrcode is valid", contents, "OK", "No").show();
            } else if (resultCode == RESULT_CANCELED) {
                showDialog(config_with_qrcode.this, "Qrcode results", "No results from qrcode scanner", "Yes", "No").show();
            }
        }
    }
}
