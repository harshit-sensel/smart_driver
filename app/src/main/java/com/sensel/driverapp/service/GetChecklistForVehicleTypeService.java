package com.sensel.driverapp.service;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.sensel.driverapp.Gridmenu;
import com.sensel.driverapp.R;
import com.sensel.driverapp.api.ServiceFactory;
import com.sensel.driverapp.api.VehicleApiService;
import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.db.model.VehicleTypeChecklist;
import com.sensel.driverapp.request.AppVersionRequest;
import com.sensel.driverapp.request.CheckListRequest;
import com.sensel.driverapp.response.AppVersionResponse;
import com.sensel.driverapp.response.GetCheckList;
import com.sensel.driverapp.utility.ApplicationConstants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.sensel.driverapp.utility.ApplicationConstants.BROADCAST_UPDATE_APP;

public class GetChecklistForVehicleTypeService extends IntentService {

    private static final String TAG = GetChecklistForVehicleTypeService.class.getName();
    private SharedPreferencesUtility sharedPreferencesUtility;

    public GetChecklistForVehicleTypeService() {
        super("GetChecklistForVehicleTypeService");
        sharedPreferencesUtility = new SharedPreferencesUtility(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createNotificationChannel();
        if (intent != null) {
            AppDatabase database = AppDatabase.getDatabase(this);
            getLoginConfig(database);
            //TODO checkAppVersion();
        }
    }

    private void checkAppVersion() {
        String baseURL = "https://ui.vehicle-tracking.co.in/";//https://fleetsmart3.ui.sensel.in/
        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                //sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
                baseURL);
        AppVersionRequest request = new AppVersionRequest();
        request.setPackageName(getPackageName());
        service.getAppCurrentVersion(request).enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(Call<AppVersionResponse> call, Response<AppVersionResponse> response) {
                if (response.code() == 200) {
                    AppVersionResponse appVersionResponse = response.body();
                    if (appVersionResponse.getPriority() == null || appVersionResponse.getPriority().isEmpty() || appVersionResponse.getStableVersion() == null || appVersionResponse.getStableVersion().isEmpty()) {
                        return;
                    }
                    int priority = Integer.parseInt(appVersionResponse.getPriority());
                    int stableVersion = Integer.parseInt(appVersionResponse.getStableVersion());
                    int versionCode = Integer.parseInt(appVersionResponse.getVersionCode());
                    //showUpdateAlert(priority, stableVersion, versionCode);

                    Intent intent = new Intent(BROADCAST_UPDATE_APP);
                    intent.putExtra("priority", priority);
                    intent.putExtra("stableVersion", stableVersion);
                    intent.putExtra("versionCode", versionCode);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure(Call<AppVersionResponse> call, Throwable t) {
                Log.d("Login Fail ", t.getMessage());
            }
        });
    }

    private void downloadChecklistForOfflineUse(AppDatabase database) {
        String[] option = {"Packed", "Rigid", "Truck+trailer"};
        for (int i = 0; i < option.length; i++) {
            getCheckListForVehicleType(option[i], database);
        }
    }

    private void getCheckListForVehicleType(String vehicleType, AppDatabase database) {
        showNotification();
        CheckListRequest checkListRequest = new CheckListRequest();
        checkListRequest.setSessionid(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));
        checkListRequest.setTruckType(vehicleType);

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        service.getChecklist(checkListRequest).enqueue(new Callback<GetCheckList>() {
            @Override
            public void onResponse(Call<GetCheckList> call, Response<GetCheckList> response) {
                switch (response.code()) {
                    case 200:
                        try {
                            VehicleTypeChecklist vehicleTypeChecklist = new VehicleTypeChecklist();
                            vehicleTypeChecklist.vehicleType = vehicleType;
                            vehicleTypeChecklist.checklistcount = response.body().getChecklistcount();
                            vehicleTypeChecklist.checklists = response.body().getChecklists();
                            //store in DB, this process needs to be done in background thread
                            PopulateVehicleTypeChecklistAsync task = new PopulateVehicleTypeChecklistAsync(database);
                            task.execute(vehicleTypeChecklist);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        removeNotification();
                        break;
                }
            }

            @Override
            public void onFailure(Call<GetCheckList> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                removeNotification();
            }
        });
    }

    private void getLoginConfig(AppDatabase database) {
        showNotification();
        CheckListRequest checkListRequest = new CheckListRequest();
        checkListRequest.setSessionid(sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.SESSION_ID));

        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class, sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        service.getLoginConfig(checkListRequest).enqueue(new Callback<List<LoginConfigs>>() {
            @Override
            public void onResponse(Call<List<LoginConfigs>> call, Response<List<LoginConfigs>> response) {
                switch (response.code()) {
                    case 200:
                        try {
                            List<LoginConfigs> loginConfigs = response.body();
                            //store in DB, this process needs to be done in background thread
                            //clear all before inserting new one
                            if (loginConfigs != null) {
                                database.loginConfigModel().deleteAll();
                            }

                            if (loginConfigs != null && loginConfigs.size() > 0) {
                                SaveLoginConfigListTask task = new SaveLoginConfigListTask(database, loginConfigs);
                                task.execute();

                                //download the checklist for offline use only when Vetting App is present in configurations
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    if (loginConfigs.stream().anyMatch(config -> config.getName().equals(ApplicationConstants.CONST_CONFIG_REGULAR))) {
                                        downloadChecklistForOfflineUse(database);
                                    }
                                } else {
                                    for (LoginConfigs config : loginConfigs) {
                                        if (config.getName().equals(ApplicationConstants.CONST_CONFIG_REGULAR)) {
                                            downloadChecklistForOfflineUse(database);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        removeNotification();
                        break;
                }
            }

            @Override
            public void onFailure(Call<List<LoginConfigs>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
                removeNotification();
            }
        });
    }

    private static class PopulateVehicleTypeChecklistAsync extends AsyncTask<VehicleTypeChecklist, Void, Void> {

        private final AppDatabase database;

        PopulateVehicleTypeChecklistAsync(AppDatabase db) {
            database = db;
        }

        @Override
        protected Void doInBackground(VehicleTypeChecklist... vehicleTypeChecklists) {
            database.vehicleTypeChecklistModel().insertVehicleTypeChecklist(vehicleTypeChecklists[0]);
            return null;
        }
    }

    private static class SaveLoginConfigListTask extends AsyncTask<Void, Void, Void> {

        private final AppDatabase database;
        private List<LoginConfigs> loginConfigs;

        SaveLoginConfigListTask(AppDatabase db, List<LoginConfigs> loginConfigs) {
            database = db;
            this.loginConfigs = loginConfigs;
        }

        @Override
        protected Void doInBackground(Void... loginConfigsList) {
            //save the list directly in DB
            for (LoginConfigs loginConfigs : loginConfigs) {
                database.loginConfigModel().insertConfig(loginConfigs);
            }
            return null;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String CHANNEL_ID = "VETTING_CHANNEL";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private final int NOTIFICATION_ID = 1237;

    private void showNotification() {
        // Instantiate a Builder object.
        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("Downloading vehicle type checklists from server, network operation is in progress...")
                .setContentText("Syncing Data")
                .setSmallIcon(R.mipmap.icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        // Creates an Intent for the Activity
        //Intent notifyIntent = new Intent(this, MainActivity.class);
        /*Intent notifyIntent = new Intent();
        // Sets the Activity to start in a new, empty task
        // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        //PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_MUTABLE);*/
        Intent notifyIntent = new Intent(this, Gridmenu.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Creates the PendingIntent with FLAG_IMMUTABLE
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this,
                0,
                notifyIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // set the sound
        //builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setDefaults(0);
        // Puts the PendingIntent into the notification builder
        builder.setContentIntent(notifyPendingIntent);
        // Notifications are issued by sending them to the
        // NotificationManager system service.
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds an anonymous Notification object from the builder, and
        // passes it to the NotificationManager
        builder.setProgress(0, 0, true);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        if (builder != null && mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

}
