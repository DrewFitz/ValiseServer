package com.example.drew.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServerBackgroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) return super.onStartCommand(intent, flags, startId);

        String path = intent.getStringExtra("rootPath");
        String port = intent.getStringExtra("port");

        assert path != null;
        assert port != null;

        HTTPServer.startServer(Integer.parseInt(port), path);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        HTTPServer.stopServer();
        super.onDestroy();
    }
}
