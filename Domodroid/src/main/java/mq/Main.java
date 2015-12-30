package mq;

import org.domogik.domodroid13.R;

import activities.Preference;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

// TTS
import android.speech.tts.TextToSpeech;


import org.json.JSONException;
import org.json.JSONObject;

//TODO add Tracer engine to log message

public class Main extends Activity {
    private Intent zmqService;
    private ZMQSubMessageReceiver recv = null;
    private boolean recvIsRegistered = false;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    // Config
    public SharedPreferences SP;
    // TTS
    private final int CHECK_CODE = 0x1;     // TODO : rename ?
    private Speaker speaker;

    // Chat history
    ListView chatHistory;
    ArrayList<String> chatHistoryList;
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);

        // Config
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        zmqService = new Intent(this, ZMQService.class);
        startService(zmqService);
        this.recv = new ZMQSubMessageReceiver(this);

        // TTS
        checkTTS();

        // ChatHistory
        chatHistory = (ListView) findViewById(R.id.listView);
        chatHistoryList = new ArrayList<String>();
        //chatHistoryList.add("Butler > Welcome");
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.list_view_theme, chatHistoryList);
        /*
        ArrayAdapter<String> arrayAdapter =

                new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, animalsNameList);
        // Set The Adapter
		 */
        chatHistory.setAdapter(arrayAdapter);


    }

    // TTS
    private void checkTTS() {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    protected void onDestroy() {
        stopService(zmqService);

        // TTS
        speaker.destroy();

        super.onDestroy();
    }

    public void showConfig(View view) {
    }

    /**
     * Showing google speech input dialog
     */
    public void promptSpeechInput(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Speech not supported", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    // Toast.makeText(getApplicationContext(), result.get(0), Toast.LENGTH_SHORT).show();
                    String yourName = SP.getString("dmg_your_name", getString(R.string.you_default_name));
                    chatHistoryList.add(yourName + " > " + result.get(0));
                    arrayAdapter.notifyDataSetChanged();
                    chatHistory.setSelection(arrayAdapter.getCount() - 1);

                    ZMQPubMessage pub = new ZMQPubMessage();
                    String ip = SP.getString("MQaddress", "");    // TODO : use a R. for the default value
                    String port = SP.getString("MQpubport", "40411");    // TODO : use a R. for the default value
                    String pub_url = "tcp://" + ip + ":" + port;
                    Log.d(this.getClass().getSimpleName(), "Pub address : " + pub_url);
                    pub.execute(pub_url, "interface.input", result.get(0));
                    pub = null;
                }
                break;
            }
            // TTS
            case CHECK_CODE: {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    speaker = new Speaker(this);
                    speaker.allow(true);
                } else {
                    Intent install = new Intent();
                    install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(install);
                }
                break;
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //switch (item.getItemId()) {
        //    case R.id.preferences:
        //Toast.makeText(this, "ADD!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, Preference.class);
        startActivity(i);
        return true;
        //    default:
        //        return super.onOptionsItemSelected(item);
        //}
    }

    public class ZMQSubMessageReceiver extends BroadcastReceiver {
        private final Context context;

        public ZMQSubMessageReceiver(Context context) {
            this.context = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(this.getClass().getSimpleName(), "here");
            ZMQMessage message = intent.getParcelableExtra("message");
            String response = "";
            if (message != null) {
                Log.d(this.getClass().getSimpleName(), "JSON received : " + message.getMessage());
                try {
                    JSONObject jsonData = new JSONObject(message.getMessage());
                    response = (String) jsonData.get("text");
                } catch (JSONException e) {
                    Log.e(this.getClass().getSimpleName(), "Error while decoding json: ", e);
                    Toast.makeText(this.context, "Error while decoding json from the butler", Toast.LENGTH_SHORT).show();
                }
                //Toast.makeText(this.context, message.getMessage(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(this.context, response, Toast.LENGTH_SHORT).show();
                speaker.speak(response);
                String butlerName = SP.getString("dmg_butler_name", getString(R.string.butler_default_name));
                chatHistoryList.add(butlerName + " > " + response);
                arrayAdapter.notifyDataSetChanged();
                chatHistory.setSelection(arrayAdapter.getCount() - 1);

            } else {
                Log.d(this.getClass().getSimpleName(), "Empty");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!recvIsRegistered) {
            registerReceiver(recv, new IntentFilter("domogik.domodroid.MESSAGE_RECV"));
            recvIsRegistered = true;
        }
    }

    @Override
    public void onPause() {
        if (recvIsRegistered) {
            unregisterReceiver(recv);
            recvIsRegistered = false;
        }
        super.onPause();
    }
}
