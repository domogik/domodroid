package logging;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by tiki on 08/01/2017.
 */

public class LogActivity extends AppCompatActivity {

    private TextView textView;
    private final String fileName = Environment.getExternalStorageDirectory() + "/domodroid/.log/Domodroid.txt";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTextView();
        }
    };
    private Intent serviceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceIntent = new Intent(getApplicationContext(), LogService.class);
        setContentView(R.layout.log);
        textView = (TextView) findViewById(R.id.textView1);
        setTextView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startService(serviceIntent);
            registerReceiver(broadcastReceiver, new IntentFilter(LogService.BROADCAST_FILE_LOG_UPDATE));
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload:
                Toast.makeText(getApplicationContext(), R.string.reload, Toast.LENGTH_LONG).show();
                setTextView();
                break;
            case R.id.menu_empty:
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.erase_log_file).setMessage(R.string.sure_question)
                        .setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(fileName);
                                file.delete();
                                try {
                                    file.createNewFile();
                                    unregisterReceiver(broadcastReceiver);
                                    stopService(serviceIntent);
                                    startService(serviceIntent);
                                    registerReceiver(broadcastReceiver, new IntentFilter(
                                            LogService.BROADCAST_FILE_LOG_UPDATE));

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                setTextView();
                            }

                        }).setNegativeButton(R.string.reloadNO, null).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextView() {
        try {
            FileReader fileReader = new FileReader(new File(fileName));
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = "";
            StringBuilder builder = new StringBuilder("");
            while ((line = bufferedReader.readLine()) != null) {
                builder.insert(0, line + "\n");
            }
            textView.setText(builder.toString());
            bufferedReader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
