package activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

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
    private Handler handler;
    private String rinor_IP;
    private String rest_port;
    private String rest_path;
    private boolean SSL;
    private boolean external_ssl;
    private String mq_ip;
    private String mq_port_sub;
    private String mq_port_pub;
    private String mq_port_req_rep;
    private String butler_name;
    private String External_IP;
    private String External_port;

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
        handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                Tracer.e("QRcode receive message=", mesg.toString());
                if (mesg.what == 0) {
                    Tracer.e(mytag, "Quit Qrcode activity");
                    config_with_qrcode.this.finish();
                } else if (mesg.what == 1) {
                    Tracer.e(mytag, "Qrcode as been read");
                    try {
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
                        askquestion(config_with_qrcode.this, 0, 2, "Http auth", "Do you need to set a User/Password to contact domogik rest server", getString(R.string.reloadOK), getString(R.string.reloadNO), false).show();
                    } catch (Exception e) {
                        handler.sendEmptyMessage(20);
                    }
                } else if (mesg.what == 2) {
                    Tracer.e(mytag, "Says to set credentials");
                    askquestion(config_with_qrcode.this, 0, 3, "User", "type your Login", getString(R.string.continue1), getString(R.string.abort), true).show();
                } else if (mesg.what == 3) {
                    Tracer.e(mytag, "Entered a login");
                    if (mesg.obj != null) {
                        try {
                            Tracer.e(mytag, (String) mesg.obj);
                            prefUtils.SetHttpAuthLogin((String) mesg.obj);
                            askquestion(config_with_qrcode.this, 0, 4, "password", "type your password", getString(R.string.continue1), getString(R.string.abort), true).show();
                        } catch (Exception e) {
                            handler.sendEmptyMessage(21);
                        }
                    } else {
                        handler.sendEmptyMessage(21);
                    }
                } else if (mesg.what == 4) {
                    if (mesg.obj != null) {
                        try {
                            Tracer.e(mytag, (String) mesg.obj);
                            prefUtils.SetHttpAuthPassword((String) mesg.obj);
                            askquestion(config_with_qrcode.this, 0, 5, "Success", "All done", getString(R.string.continue1), getString(R.string.abort), false).show();
                        } catch (Exception e) {
                            handler.sendEmptyMessage(22);
                        }
                    } else {
                        handler.sendEmptyMessage(22);
                    }
                } else if (mesg.what == 5) {
                    //Tracer.e(mytag, mesg.obj.toString());
                    Tracer.e(mytag, "all done");
                    handler.sendEmptyMessage(0);
                    //let 20 value to intercept error
                } else if (mesg.what == 20) {
                    Tracer.e(mytag, "Can't save server information");
                    askquestion(config_with_qrcode.this, 0, 0, "Qrcode", "Can't save server information", getString(R.string.reloadOK), "", false).show();
                } else if (mesg.what == 21) {
                    Tracer.e(mytag, "Can't save login");
                    askquestion(config_with_qrcode.this, 0, 0, "User", "Can't save login", getString(R.string.reloadOK), "", false).show();
                } else if (mesg.what == 22) {
                    Tracer.e(mytag, "Can't save password");
                    askquestion(config_with_qrcode.this, 0, 0, "password", "Can't save password", getString(R.string.reloadOK), "", false).show();
                } else if (mesg.what == 23) {
                    Tracer.e(mytag, "Can't decode qrcode as json");
                    askquestion(config_with_qrcode.this, 0, 0, "Qrcode", "Error parsing answer of qrcode to json", getString(R.string.reloadOK), "", false).show();
                } else if (mesg.what == 24) {
                    Tracer.e(mytag, "No results from qrcode scanner");
                    askquestion(config_with_qrcode.this, 0, 0, "Qrcode results", "No results from qrcode scanner", getString(R.string.reloadOK), "", false).show();
                }

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                contents = data.getStringExtra("SCAN_RESULT"); //this is the result
                showDialog(config_with_qrcode.this, getString(R.string.qr_code_is_valid), contents, getString(R.string.continue1), getString(R.string.abort)).show();
            } else if (resultCode == RESULT_CANCELED) {
                handler.sendEmptyMessage(24);
            }
        }
    }

    private AlertDialog askquestion(final Activity act, final int msg0, final int msg1, CharSequence title, CharSequence message, CharSequence buttonYes, final CharSequence buttonNo, final boolean textinput) {
        //display an alertbox to remember user to set is password/login in options
        final AlertDialog.Builder alert = new AlertDialog.Builder(act);
        alert.setTitle(title);
        alert.setMessage(message);
        final EditText input = new EditText(getApplicationContext());
        // Set an EditText view to get user input
        if (textinput) {
            alert.setView(input);
        }
        alert.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                if (textinput) {
                    Message message1 = new Message();
                    message1.what = msg1;
                    Tracer.e(mytag, "input.getText=" + input.getText());
                    message1.obj = input.getText().toString();
                    handler.sendMessage(message1);
                } else handler.sendEmptyMessage(msg1);
            }
        });
        alert.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog_customname, int whichButton) {
                handler.sendEmptyMessage(msg0);
            }
        });
        return alert.show();
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
                        showDialog(config_with_qrcode.this, getString(R.string.no_market_apps), contents, "", getString(R.string.reloadOK)).show();
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
                        rest_path = null;
                        rest_port = null;
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
                        mq_ip = jsonresult.getString("mq_ip");
                        //String rest_path = jsonresult.getString("rest_path");
                        mq_port_sub = "40412";
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
                        mq_port_pub = "40411";
                        try {
                            mq_port_pub = jsonresult.getString("mq_port_pub");
                        } catch (JSONException exec) {
                            Tracer.e(mytag, "mq_port_pub not present in this qrcode");
                        }
                        mq_port_req_rep = jsonresult.getString("mq_port_req_rep");
                        rinor_IP = null;
                        if (separated[0].toLowerCase().equals("http")) {
                            rinor_IP = admin_ip.replace("http://", "");
                            SSL = false;
                        } else if (separated[0].toLowerCase().equals("https")) {
                            rinor_IP = admin_ip.replace("https://", "");
                            SSL = true;
                        }
                        External_IP = "";
                        External_port = "";
                        try {
                            External_IP = jsonresult.getString("u'external_ip'").replace("u'", "").replace("'", "");
                            External_port = jsonresult.getString("u'external_port'").replace("u'", "").replace("'", "");
                        } catch (Exception e1) {
                            Tracer.e(mytag, "ERROR getting external IP PORT information");
                        }
                        external_ssl = false;
                        try {
                            if (jsonresult.getString("u'external_ssl'").toLowerCase().equals("u'y'")) {
                                external_ssl = true;
                            }
                        } catch (Exception e1) {
                            Tracer.e(mytag, "ERROR getting external SSL information");
                        }
                        butler_name = jsonresult.getString("butler_name").replace("u'", "").replace("'", "");
                        handler.sendEmptyMessage(1);
                    } catch (JSONException e) {
                        Tracer.e(mytag, "Error parsing answer of qrcode to json: " + e.toString());
                        handler.sendEmptyMessage(23);
                    }
                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                handler.sendEmptyMessage(0);
            }
        });
        return downloadDialog.show();
    }


}
