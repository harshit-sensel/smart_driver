package com.sensel.driverapp.application;

import android.content.Context;
import android.os.StrictMode;

import androidx.multidex.MultiDexApplication;

import com.sensel.driverapp.db.SharedPreferencesUtility;


public class VehicleApplication extends MultiDexApplication {

    private Context context;
    private static VehicleApplication vehicleApplication;
    private SharedPreferencesUtility sharedPreferencesUtility;

    public VehicleApplication() {
        vehicleApplication = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        sharedPreferencesUtility = new SharedPreferencesUtility(context);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


    }

    public static VehicleApplication getElectionTrackersApplication() {
        return vehicleApplication;
    }

    public static void setElectionTrackersApplication(VehicleApplication vehicleApplication) {
        VehicleApplication.vehicleApplication = vehicleApplication;
    }

//    public  String gettoken(){
//        return sharedPreferencesUtility.getStringPreferences(SharedPreferencesUtility.TOKEN);
//    }

    public Context getContext() {
        return context;
    }

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("font/Averta-Regular.otf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );

}
