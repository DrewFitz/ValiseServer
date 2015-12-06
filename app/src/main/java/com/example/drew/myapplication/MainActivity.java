package com.example.drew.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Format;
import java.util.Formatter;
import java.util.List;

public class MainActivity extends Activity implements DebugListener {

    private TextView logView;
    private TextView pathView;
    private TextView ipView;

    private final String PREFS_NAME = "com.titen.server.preferences";
    private final String PATH_KEY = "serverPath";
    private final String PORT_KEY = "serverPort";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Valise");

        logView = (TextView) findViewById(R.id.serverLogTextView);
        pathView = (TextView) findViewById(R.id.filePathText);
        DebugBroadcaster.addListener(this);



        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        String path = prefs.getString(PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
        pathView.setText(path);

    }

    @Override
    protected void onDestroy() {
        DebugBroadcaster.removeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(this, ServerSettings.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = data.getStringExtra("Path");
        if (path == null) return;

        pathView.setText(path);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PATH_KEY, path);
        editor.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();

        pathView.setText(getSharedPreferences(PREFS_NAME,0).getString(PATH_KEY, "No path selected"));
    }

    public void choosePathEvent(View v) {
        Intent intent = new Intent(this, FileBrowser.class);
        startActivityForResult(intent, 0);
    }

    public void startServerEvent(View v) {
        Intent intent = new Intent(this, ServerBackgroundService.class);
        intent.putExtra("rootPath", getSharedPreferences(PREFS_NAME,0).getString(PATH_KEY, null));
        intent.putExtra("port", getSharedPreferences(PREFS_NAME,0).getString(PORT_KEY, "8080"));
        startService(intent);
    }

    public void stopServerEvent(View v) {
        Intent intent = new Intent(this, ServerBackgroundService.class);
        stopService(intent);
    }

    @Override
    public void debugMessage(String message) {
        final String m = message;
        if (logView != null) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logView.append(m + "\n");
                }
            });
        }
    }
}
