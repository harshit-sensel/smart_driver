package com.sensel.driverapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sensel.driverapp.JourneyPlanner;
import com.sensel.driverapp.R;
import com.sensel.driverapp.objectclass.Object_LatLong;
import com.sensel.driverapp.utility.JourneyPlanDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Download_LatLong extends Service {

    public static final String CHANNEL_ID = "DownloadLATLONG";

    ArrayList<Object_LatLong> latLongArrayList;
    JourneyPlanDB journeyPlanDB;

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        latLongArrayList = new ArrayList<>();
        journeyPlanDB =new JourneyPlanDB(getApplicationContext());
        createNotificationChannel();

        SharedPreferences sharedpreferences = getSharedPreferences(JourneyPlanner.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();



        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading route details")
                .setSmallIcon(R.drawable.ic__totalgaz)
                .build();
        //startForeground(1, notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            startForeground(1, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, notification);
        }

        //downloading latlong
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("RouteId", JourneyPlanner.ROUTE_ID);
            jsonObject.put("TripType", JourneyPlanner.TRIP_TYPE);
            jsonObject.put("tripid", JourneyPlanner.TRIP_ID);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/GetJMPTripJRMRouteDetails",
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            journeyPlanDB.deleteLatLongs();
                            journeyPlanDB.deletedata();

                            Log.e("RESPONSE", response.toString());
                            try {
                                JSONArray jsonArray = response.getJSONArray("JMPRouteDetails");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Object_LatLong temp = new Object_LatLong(object.getString("latitude"), object.getString("longitude"),object.getString("colorname"),object.getString("colorcode"));
                                    latLongArrayList.add(temp);
                                }

                                JourneyPlanner.SRC_LATLANG=latLongArrayList.get(0).getLatitude()+"," +latLongArrayList.get(0).getLongitude();
                                JourneyPlanner.DEST_LATLANG=latLongArrayList.get(latLongArrayList.size()-1).getLatitude()+","+latLongArrayList.get(latLongArrayList.size()-1).getLongitude();
                                Log.e(JourneyPlanner.SRC_LATLANG,JourneyPlanner.DEST_LATLANG);
                                editor.putString(JourneyPlanner.PREF_LATLONG, JourneyPlanner.SRC_LATLANG +"##"+JourneyPlanner.DEST_LATLANG);
                                editor.commit();

                                journeyPlanDB.insertLatLongs(latLongArrayList);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            stopSelf();
                            stopForeground(true);

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ERROR", error.toString());
                            stopSelf();
                            stopForeground(true);

                        }
                    }
            );
            requestQueue.add(jsonObjectRequest);

        } catch (JSONException e) {
            stopSelf();
            stopForeground(true);
            e.printStackTrace();
        }




        //do heavy work on a background thread
        //stopSelf();


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Downloading route details",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
