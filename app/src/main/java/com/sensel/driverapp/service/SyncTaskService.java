package com.sensel.driverapp.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sensel.driverapp.utility.CheckNetwork;

import static android.content.Intent.ACTION_SYNC;

public class SyncTaskService extends GcmTaskService {

    public static String TAG = SyncTaskService.class.getName();

    @Override
    public int onRunTask(TaskParams taskParams) {
        Log.e(TAG, "SyncTaskService called");
        if (CheckNetwork.isNetworkAvailable(this)) {
            Intent intent = new Intent(this, SyncIntentService.class);
            //intent.putExtra(CONST_DATA, vehicleVetting); //cause error
            intent.setAction(ACTION_SYNC);
            startService(intent);
        } else {
            Log.e(TAG, "Please check internet connection");
        }
        return GcmNetworkManager.RESULT_SUCCESS;
    }

}
