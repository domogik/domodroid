package activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.domogik.domodroid13.R;
import org.json.JSONException;
import org.json.JSONObject;

import Abstract.pref_utils;
import misc.tracerengine;


public class config_with_qrcode extends AppCompatActivity {
    private static String contents;
    private final String mytag = this.getClass().getName();
    private static tracerengine Tracer = null;
    private pref_utils prefUtils;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = new pref_utils(this);
        Tracer = tracerengine.getInstance(prefUtils.prefs, this);

        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");//for Qr code, its "QR_CODE_MODE" instead of "PRODUCT_MODE"
            intent.putExtra("SAVE_HISTORY", false);//this stops saving ur barcode in barcode scanner app's history
            startActivityForResult(intent, 0);

        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            showDialog(config_with_qrcode.this, getString(R.string.no_qrcode_scanner), getString(R.string.no_qrcode_question), getString(R.string.reloadOK), getString(R.string.reloadNO)).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                showDialog(config_with_qrcode.this, getString(R.string.qr_code_is_valid), contents, getString(R.string.continue1), getString(R.string.abort)).show();
                //Todo #153 if config works ask user is credentials (user/password if needed.
            } else if (resultCode == RESULT_CANCELED) {
                //showDialog(config_with_qrcode.this, "Qrcode results", "No results from qrcode scanner", "Yes", "No").show();
            }
        }
    }

    private AlertDialog showDialog(final Activity act, final CharSequence title, CharSequence message, CharSequence buttonYes, final CharSequence buttonNo) {
        final AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (title.equals(getString(R.string.no_qrcode_scanner))) {
                    Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    try {
                        act.startActivity(intent);
                    } catch (ActivityNotFoundException anfe) {
                        Tracer.e(mytag, "No market apps installed on this device: " + anfe.toString());
                        showDialog(config_with_qrcode.this, getString(R.string.no_market_apps), contents, getString(R.string.ok), getString(R.string.reloadNO)).show();
                    }
                } else if (title.equals(getString(R.string.qr_code_is_valid))) {
                    Tracer.d("preference", "We got a result from qrcode scanner:" + contents);
                    try {
                        JSONObject jsonresult = null;
                        jsonresult = new JSONObject(contents);
                        String admin_url = jsonresult.getString("admin_url");
                        String[] separated = admin_url.split("://");
                        String[] separatedbis = separated[1].split(":");
                        String admin_ip = separatedbis[0];
                        String rest_path = null;
                        String rest_port = null;
                        try {
                            String[] separatedter = separatedbis[1].split("/");
                            rest_port = separatedter[0];
                            rest_path = separatedter[1] + jsonresult.getString("rest_path");
                        } catch (ArrayIndexOutOfBoundsException exec) {
                            try {
                                rest_port = jsonresult.getString("rest_port");
                            } catch (Exception e) {
                                rest_port = "";
                            }
                            try {
                                rest_path = jsonresult.getString("rest_path");
                            } catch (Exception e) {
                                rest_path = "";
                            }
                        }
                        String mq_ip = jsonresult.getString("mq_ip");
                        //String rest_path = jsonresult.getString("rest_path");
                        String mq_port_sub = "40412";
                        try {
                            mq_port_sub = jsonresult.getString("mq_port_pub");
                        } catch (JSONException exec) {
                            try {
                                Tracer.e(mytag, "mq_port_pub not present in this qrcode");
                                mq_port_sub = jsonresult.getString("mq_port_pubsub");
                            } catch (JSONException exec2) {
                                Tracer.e(mytag, "mq_port_pubsub not present in this qrcode");
                            }
                        }
                        String mq_port_pub = "40411";
                        try {
                            mq_port_pub = jsonresult.getString("mq_port_pub");
                        } catch (JSONException exec) {
                            Tracer.e(mytag, "mq_port_pub not present in this qrcode");
                        }
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
                        String External_IP = "";
                        String External_port = "";
                        try {
                            External_IP = jsonresult.getString("u'external_ip'").replace("u'", "").replace("'", "");
                            External_port = jsonresult.getString("u'external_port'").replace("u'", "").replace("'", "");
                        } catch (Exception e1) {
                            Tracer.e(mytag, "ERROR getting external IP PORT information");
                        }
                        Boolean external_ssl = false;
                        try {
                            if (jsonresult.getString("u'external_ssl'").toLowerCase().equals("u'y'")) {
                                external_ssl = true;
                            }
                        } catch (Exception e1) {
                            Tracer.e(mytag, "ERROR getting external SSL information");
                        }
                        String butler_name = jsonresult.getString("butler_name").replace("u'", "").replace("'", "");
                        prefUtils.SetRestIp(rinor_IP);
                        prefUtils.SetRestPort(rest_port);
                        prefUtils.SetRestPath(rest_path);
                        prefUtils.SetRestSsl(SSL);
                        prefUtils.SetExternalRestSsl(external_ssl);
                        prefUtils.SetMqAddress(mq_ip);
                        prefUtils.SetMqSubPort(mq_port_sub);
                        prefUtils.SetMqPubPort(mq_port_pub);
                        prefUtils.SetMqReqRepPort(mq_port_req_rep);
                        prefUtils.SetButlerName(butler_name);
                        prefUtils.SetExternalRestIp(External_IP);
                        prefUtils.SetExternalRestPort(External_port);
                        config_with_qrcode.this.finish();

                    } catch (JSONException e) {
                        Tracer.e(mytag, "Error parsing answer of qrcode to json: " + e.toString());
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


}
