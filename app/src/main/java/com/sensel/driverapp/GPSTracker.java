package com.sensel.driverapp;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;


/**
 * Created by Mallikarjun on 22-10-2015.
 */

public class GPSTracker extends Service implements LocationListener {

    // Get Class Name
    private static String TAG = GPSTracker.class.getName();
    public static String LATLONG = "0,0";
    private final Context mContext;
    public static Activity mActivity;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    // flag for GPS Status
    boolean isGPSEnabled = false;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;

    Location location;
    double latitude;
    double longitude;

    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;

    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 5; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 20 sec

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;

    public GPSTracker(Context context) {
        this.mContext = context;

        getLocation();
    }

    /**
     * Try to get my current location by GPS or Network Provider
     */
    public String getLocation() {
        try {


            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            }
            else {
                // this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                                    android.Manifest.permission.ACCESS_FINE_LOCATION


                            }, 10);
                        } else
                            locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
                        Log.d("Network", "Network Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            if(ContextCompat.checkSelfPermission(GPSTracker.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(mActivity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);

                            }
                        }
                        else {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this, Looper.getMainLooper());
                            Log.d("GPS", "GPS Enabled");
                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
       // LATLONG=latitude+","+longitude;
        return latitude+","+longitude;
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.e("lat",LATLONG);
            LATLONG=latitude+","+longitude;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}