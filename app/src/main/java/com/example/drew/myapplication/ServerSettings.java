package com.example.drew.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class ServerSettings extends Activity {

    private final String PREFS_NAME = "com.titen.server.preferences";
    private final String PATH_KEY = "serverPath";
    private final String PORT_KEY = "serverPort";

    private TextView pathView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_settings);

        final TextView portText = (TextView) findViewById(R.id.portNumberText);

        pathView = (TextView) findViewById(R.id.rootPathText);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        String path = prefs.getString(PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
        pathView.setText(path);

        portText.setText(getSharedPreferences(PREFS_NAME, 0).getString(PORT_KEY, "8080"));

        portText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null
                                && event.getAction() == KeyEvent.ACTION_DOWN
                                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && !event.isShiftPressed())) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    String port = portText.getText().toString();

                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PORT_KEY, port);
                    editor.commit();

                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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

    public void choosePathEvent(View v) {
        Intent intent = new Intent(this, FileBrowser.class);
        startActivityForResult(intent, 0);
    }

}
