package com.sensel.driverapp.service;

import static com.sensel.driverapp.utility.ApplicationConstants.BROADCAST_UPDATE_SCREEN;
import static com.sensel.driverapp.utility.ApplicationConstants.CONST_CONFIG_REGULAR;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.content.Context;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.sensel.driverapp.Gridmenu;
import com.sensel.driverapp.R;
import com.sensel.driverapp.api.ServiceFactory;
import com.sensel.driverapp.api.VehicleApiService;
import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.SharedPreferencesUtility;
import com.sensel.driverapp.db.model.VehicleVetting;
import com.sensel.driverapp.request.SaveRequest;
import com.sensel.driverapp.response.GetInsertResponse;
import com.sensel.driverapp.response.ImageDetails;
import com.sensel.driverapp.response.MultipleImageDetailsWrapper;
import com.sensel.driverapp.utility.CheckNetwork;
import com.sensel.driverapp.utility.CustomVolleyRequestQueue;
import com.sensel.driverapp.utility.ImageUploadWithVolley;
import com.sensel.driverapp.utility.ToastHelper;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SyncWorker extends Worker {

    private Context context;
    private int uploadDataTaskIndex = -1;
    private LinkedList<Object> uploadDataTaskList;
    private static String baseURL;
    private String CHANNEL_ID = "VETTING_CHANNEL";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private final int NOTIFICATION_ID = 1236;

    private static final String TAG = SyncIntentService.class.getSimpleName();
    private RequestQueue volleyRequestQueue;
    private SharedPreferencesUtility sharedPreferencesUtility;


    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }
    @NonNull
    @Override
    public Result doWork() {
        Log.e(TAG, "SyncTaskService called");
        if (CheckNetwork.isNetworkAvailable(context)) {
            createNotificationChannel();
            //volleyRequestQueue = Volley.newRequestQueue(this);
            volleyRequestQueue = CustomVolleyRequestQueue.getInstance(this.getApplicationContext()).getRequestQueue();
            sharedPreferencesUtility = new SharedPreferencesUtility(context);
            // Your background work logic goes here
            uploadDataTaskIndex = -1; //starts with -1 because its getting updated whenever called executeNextRequest method
            uploadDataTaskList = new LinkedList<Object>();
            baseURL = sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL) + "SenselRestService.svc/rest/v3/";
            AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
            List<VehicleVetting> vettingList = database.vehicleVettingModel().loadAllVetting();
            //store vetting list to server
            for (VehicleVetting vehicleVetting : vettingList) {
                //Log.e(TAG, "Creating request queue " + uploadDataTaskIndex);
                createRequestQueue(vehicleVetting);
            }

            Log.e(TAG, "execution started - " + uploadDataTaskIndex);
            executeNextRequest();
            // ...
        }
        else {
            Log.e(TAG, "Please check internet connection");
        }
        return Result.success();
    }
    private void executeNextRequest() {

        uploadDataTaskIndex++; //update uploadDataTaskIndex to process next request

        if (uploadDataTaskIndex <= uploadDataTaskList.size()) {
            showNotification();
        } else {
            Log.e(TAG, "Else uploadDataTaskIndex > size " + uploadDataTaskIndex);
        }

        if (uploadDataTaskIndex >= 0 && uploadDataTaskIndex < uploadDataTaskList.size()) {
            Object object = uploadDataTaskList.get(uploadDataTaskIndex);
            if (object instanceof ImageDetails) {
                Log.e(TAG, "uploading file " + uploadDataTaskIndex);
                ImageDetails imageDetails = (ImageDetails) object;
                uploadImage(imageDetails, imageDetails.getSessionid());
            } else if (object instanceof SaveRequest) {
                Log.e(TAG, "Save Request data " + uploadDataTaskIndex);
                saveCheckList((SaveRequest) object);
            }
        } else {
            // tasks done
            Log.e(TAG, "uploading tasks done " + uploadDataTaskIndex);
            setBackupCompleted();
            removeNotification();
            notifyActivity();
            //notify to UI refresh the UI
        }
    }

    private void createRequestQueue(VehicleVetting vehicleVetting) {
        Long dbRowId = vehicleVetting.id;

        for (MultipleImageDetailsWrapper imageDetailsWrapper : vehicleVetting.imageList) {
            //imageDetails.setDbRowId(dbRowId); // for now its not required while uploading files
            uploadDataTaskList.addAll(imageDetailsWrapper.getImages());
        }

        SaveRequest saveRequest = new SaveRequest();
        saveRequest.setDbRowId(dbRowId); // use this id to update record in DB
        saveRequest.setStaticDetails(vehicleVetting.staticDetails);
        saveRequest.setChecklistValues(vehicleVetting.checklistValues);
        saveRequest.setFieldValues(vehicleVetting.fieldValues);
        saveRequest.setSessionid(vehicleVetting.sessionid);
        uploadDataTaskList.add(saveRequest);
    }
    private void showNotification() {
        // Instantiate a Builder object.
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle("Syncing vetting data to server, network operation is in progress...")
                .setContentText("Syncing Data")
                .setSmallIcon(R.mipmap.icon)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        // Creates an Intent for the Activity
        //Intent notifyIntent = new Intent(this, MainActivity.class);
        /*Intent notifyIntent = new Intent();
        // Sets the Activity to start in a new, empty task
        // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        //PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_MUTABLE);*/
        Intent notifyIntent = new Intent(context, SyncWorker.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Creates the PendingIntent with FLAG_IMMUTABLE
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                context,
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
        //mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager =(NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds an anonymous Notification object from the builder, and
        // passes it to the NotificationManager
        builder.setProgress(0, 0, true);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setBackupCompleted() {
        if (builder != null && mNotificationManager != null) {
            builder.setContentTitle("Sync Complete")
                    .setContentText("Local vetting records synced with server...")
                    // Removes the progress bar
                    .setProgress(0, 0, false);
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.e(TAG, "Sync Completed");
        }
    }

    private void removeNotification() {
        if (builder != null && mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
    private void notifyActivity() {
        Intent intent = new Intent(BROADCAST_UPDATE_SCREEN);
        //intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    private void uploadImage(ImageDetails imageDetails, String sessionId) {
        try {
            File uploadFile = new File(imageDetails.getFinalFile());
            if (!uploadFile.exists()) {
                executeNextRequest();
                return;
            }

            StringBuffer methodUrl = new StringBuffer(baseURL + "UploadFile" + "?sessionid=" + sessionId + "&fileName=" + imageDetails.getFileName());
            ImageUploadWithVolley imageUploadReq = new ImageUploadWithVolley(methodUrl.toString(),
                    error -> {
                        Log.e(TAG, "volley onErrorResponse: ", error);
                        executeNextRequest();
                    },
                    response -> {
                        Log.e(TAG, "volley onResponse " + response);
                        executeNextRequest();
                    }, uploadFile);
            RetryPolicy policy = new DefaultRetryPolicy(20000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            imageUploadReq.setRetryPolicy(policy);
            volleyRequestQueue.add(imageUploadReq);
            setBackupCompleted();
        } catch (Exception e) {
            Log.e(TAG, "volley error " + e.getMessage());
            executeNextRequest();
        }

    }

    private void saveCheckList(SaveRequest saveRequest) {
        VehicleApiService service = ServiceFactory.createRetrofitService(VehicleApiService.class,
                sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.BASE_URL));
        //for non vetting configurable apps, inspector name contains app name so check if app
        if (saveRequest.getStaticDetails().getAppName().equals(CONST_CONFIG_REGULAR)) {
            service.saveCompleteData(saveRequest).enqueue(new Callback<GetInsertResponse>() {
                @Override
                public void onResponse(@NonNull Call<GetInsertResponse> call, @NonNull Response<GetInsertResponse> response) {
                    if (response.code() == 200) {
                        Log.d(TAG, "CheckList Response " + response.body().getResult());
                        //update the record as synced & remove the files which are upload to server
                        updateRecordInDB(saveRequest.getDbRowId());
                        executeNextRequest();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetInsertResponse> call, @NonNull Throwable t) {
                    Log.d(TAG, "CheckList Request Failed " + t.getMessage());
                    executeNextRequest();
                }
            });
        } else {//call for saving configuration app data
            service.saveVetDetails(saveRequest).enqueue(new Callback<GetInsertResponse>() {
                @Override
                public void onResponse(@NonNull Call<GetInsertResponse> call, @NonNull Response<GetInsertResponse> response) {
                    if (response.code() == 200) {
                        Log.d(TAG, "CheckList Response " + response.body().getResult());
                        //update the record as synced & remove the files which are upload to server
                        updateRecordInDB(saveRequest.getDbRowId());
                        executeNextRequest();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetInsertResponse> call, @NonNull Throwable t) {
                    Log.d(TAG, "CheckList Request Failed " + t.getMessage());
                    executeNextRequest();
                }
            });
        }

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name);
            String description = getApplicationContext().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            //NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(channel);
        }
    }
    private void updateRecordInDB(Long dbRowId) {
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        //get the record
        VehicleVetting vehicleVetting = database.vehicleVettingModel().loadVettingById(dbRowId);
        //delete the record
        database.vehicleVettingModel().deleteVettingById(dbRowId);
        if (vehicleVetting != null && vehicleVetting.imageList != null) { //delete the files
            for (MultipleImageDetailsWrapper imageDetailsWrapper : vehicleVetting.imageList) {
                for (ImageDetails imageDetails : imageDetailsWrapper.getImages()) {
                    try {
                        if (imageDetails.getFinalFile() != null) {
                            File uploadFile = new File(imageDetails.getFinalFile());
                            if (uploadFile.exists()) {
                                uploadFile.delete();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        }
    }
}