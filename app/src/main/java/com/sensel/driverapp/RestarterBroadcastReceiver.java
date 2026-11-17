package com.sensel.driverapp;

/**
 * Created by User on 28-10-2016.
 */
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class RestarterBroadcastReceiver extends BroadcastReceiver {
    Context mcontext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mcontext=context;

//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(new Intent(context,SaveMyAppsService.class));
//            context.startForegroundService(new Intent(context,MyService1.class));
//        } else {
//            context.startService(new Intent(context, SaveMyAppsService.class)); //for locking while driving
//            context.startService(new Intent(context, MyService1.class));//for app lock for selected apps
//        }



            if(!isMyServiceRunning(SaveMyAppsService.class,mcontext)){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mcontext.startForegroundService(new Intent(mcontext, SaveMyAppsService.class));
                } else {
                    mcontext.startService(new Intent(mcontext, SaveMyAppsService.class));
                }
            }

        if(!isMyServiceRunning(SaveMyAppsService.class,mcontext)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context,MyService1.class));
            } else {
                context.startService(new Intent(context, MyService1.class));
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass,Context context) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.e("Service already","running");
                return true;
            }
        }
        Log.e("Service not","running");
        return false;
    }
}