package com.sensel.driverapp;

/**
 * Created by Mallikarjun on 24-08-2015.
 */
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;


public class MyService extends Service {




    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    public int simple()
    {
        Toast.makeText(this, "Service running", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

}