package com.sensel.driverapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.ksoap2.serialization.PropertyInfo;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SaveMyAppsService extends android.app.Service
{
    private PowerManager.WakeLock mWakeLock;
    public static SaveMyAppsService instance;
    public  static String[] vehicle_speed=new String[6];
    Context context;
    public  static boolean allow=true;
    public  static boolean blocked=false;
    public  static boolean app_allow=true;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private SQLiteDatabase db;
    private SharedPreferences pref;
    static  String deviceid;
    int JRMSpotsRefreshRate=30*24*60*60;
    int runtimeRefreshRate=10*60;

    private ScheduledExecutorService scheduledExecutorServiceForJRMspots;
    private ScheduledFuture<?> futureTaskForJRMspots;
    private Runnable taskForJRMspots;

    private ScheduledExecutorService scheduledExecutorServiceForRuntime;
    private ScheduledFuture<?> futureTaskForRuntime;
    private Runnable taskForRuntime;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        /*if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "saveapp";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Service notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon).build();

            startForeground(1, notification);
        }*/
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "saveapp";
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service notification",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("Smart Driver Service")
                    .setContentText("Service running in the background")
                    .build();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION | ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
            } else {
                startForeground(1, notification);
            }
        }

        if(deviceid==null) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                deviceid = tm.getDeviceId();
            } else {
                @SuppressLint("HardwareIds")
                String android_id = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id != null) {
                    deviceid = android_id;
                }
            }
            LockScreenActivity.deviceid=deviceid;
        }
        scheduleMethod();
        pref = getApplicationContext().getSharedPreferences("totaljrm", MODE_PRIVATE);

        scheduledExecutorServiceForJRMspots = Executors.newScheduledThreadPool(5);

        taskForJRMspots = new Runnable()
        {
            @Override
            public void run()
            {
                try{
                    if(Gridmenu.points!=null && Gridmenu.points.length==8 && !Gridmenu.points[7].contains("NA")) {
                        db=openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                        db.execSQL("CREATE TABLE IF NOT EXISTS jrmspots(minlat DECIMAL(9,6),maxlat DECIMAL(9,6),minlng DECIMAL(9,6),maxlng DECIMAL(9,6),type VARCHAR);");
                        String jrmSpots=GetjrmSpots(deviceid);
                        if(jrmSpots.contains("#")) {
                            db.delete("jrmspots", null, null);
                            String insertQuery="INSERT INTO jrmspots(minlat ,maxlat ,minlng ,maxlng ,type) VALUES ";
                            String[] rows=jrmSpots.split("\\|");
                            for(int i=0;i<rows.length;i++){
                                String[] columns=rows[i].split("\\#");
                                insertQuery+="('"+columns[0]+"','"+columns[1]+"','"+columns[2]+"','"+columns[3]+"','"+columns[4]+"')";
                                if(i==rows.length-1)
                                    insertQuery += ";";
                                else
                                    insertQuery+=",";
                            }
                            db.execSQL(insertQuery);
                            if(JRMSpotsRefreshRate!=pref.getInt("JRMSpotsRefreshRate",JRMSpotsRefreshRate)) {
                                JRMSpotsRefreshRate = pref.getInt("JRMSpotsRefreshRate",JRMSpotsRefreshRate);
                                if (JRMSpotsRefreshRate > 0) {
                                    if (futureTaskForJRMspots != null) {
                                        futureTaskForJRMspots.cancel(true);
                                    }

                                    futureTaskForJRMspots = scheduledExecutorServiceForJRMspots.scheduleAtFixedRate(taskForJRMspots, 0, JRMSpotsRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                        }
                        else{
                            if(JRMSpotsRefreshRate!=1*24*60*60) {
                                JRMSpotsRefreshRate = 1*24*60*60;
                                if (JRMSpotsRefreshRate > 0) {
                                    if (futureTaskForJRMspots != null) {
                                        futureTaskForJRMspots.cancel(true);
                                    }

                                    futureTaskForJRMspots = scheduledExecutorServiceForJRMspots.scheduleAtFixedRate(taskForJRMspots, 0, JRMSpotsRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                        }
                        db.close();
                    }
                }
                catch (Exception e){
                    db.close();
                    e.printStackTrace();
                }
            }
        };

        if (JRMSpotsRefreshRate > 0) {
            if (futureTaskForJRMspots != null) {
                futureTaskForJRMspots.cancel(true);
            }

            futureTaskForJRMspots = scheduledExecutorServiceForJRMspots.scheduleAtFixedRate(taskForJRMspots, 0, JRMSpotsRefreshRate, TimeUnit.SECONDS);
        }

        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(Gridmenu.points!=null && Gridmenu.points.length==8 && !Gridmenu.points[7].contains("NA")) {
                                String limitations = GetLimitations(deviceid);
                                SharedPreferences.Editor editor = pref.edit();
                                if (limitations.contains("#")) {
                                    String[] limdata = limitations.split("#");
                                    String[] result = limdata[0].split(" ");
                                    int dayHr = Integer.parseInt(result[0].split("h")[0]);
                                    int dayMin = Integer.parseInt(result[2].split("min")[0]);
                                    if (dayMin >= 30)
                                        dayMin = dayMin - 30;
                                    else {
                                        dayHr--;
                                        dayMin = dayMin + 60 - 30;
                                    }
                                    editor.putInt("dayHr", dayHr);
                                    editor.putInt("dayMin", dayMin);
                                    result = limdata[5].split(" ");
                                    editor.putInt("tripHr", Integer.parseInt(result[0].split("h")[0]));
                                    editor.putInt("tripMin", Integer.parseInt(result[2].split("min")[0]));
                                    result = limdata[1].split(" ");
                                    result = result[0].split(":");
                                    editor.putInt("nightStart", (Integer.parseInt(result[0]) + 12) * 100 + Integer.parseInt(result[1]));
                                    result = limdata[2].split(" ");
                                    result = result[0].split(":");
                                    editor.putInt("nightEnd", (Integer.parseInt(result[0])) * 100 + Integer.parseInt(result[1]));
                                    result = limdata[3].split(" ");
                                    editor.putInt("minDrive", Integer.parseInt(result[0].split("h")[0]));
                                    result = limdata[5].split(" ");
                                    editor.putInt("maxDrive", Integer.parseInt(result[0].split("h")[0]));
                                    result = limdata[4].split(" ");
                                    int Rest = 0;
                                    if (Integer.valueOf(result[0].split("h")[0]) > 0)
                                        Rest = Integer.valueOf(result[0].split("h")[0]) * 60;
                                    Rest = Rest + (Integer.valueOf(result[2].split("min")[0]) - (Integer.valueOf(result[2].split("min")[0]) % 10));
                                    editor.putInt("minRest", Rest);
                                    result = limdata[6].split(" ");
                                    if (Integer.valueOf(result[0].split("h")[0]) > 0)
                                        Rest = Integer.valueOf(result[0].split("h")[0]) * 60;
                                    else
                                        Rest = 0;
                                    Rest = Rest + (Integer.valueOf(result[2].split("min")[0]) - (Integer.valueOf(result[2].split("min")[0]) % 10));
                                    editor.putInt("maxRest", Rest);
                                    result = limdata[7].split(",");
                                    //editor.putInt("maxSpeed", Integer.parseInt(limdata[7]));
                                    editor.putInt("maxSpeed", Integer.parseInt((result[0])));
                                    editor.putInt("JRMSpotsRefreshRate", Integer.parseInt(limdata[8]));
                                    editor.putInt("runtimeRefreshRate", Integer.parseInt(limdata[9]));
                                    editor.putInt("audioRefreshRate", Integer.parseInt(limdata[10]));
                                    editor.putInt("gpsupdaterate", Integer.parseInt(limdata[11]));
                                    editor.putInt("gpsdistanceinterval", Integer.parseInt(limdata[12]));
                                    editor.putInt("gpstimeinterval", Integer.parseInt(limdata[13]));
                                    editor.apply();
                                    if (limdata[14] != null)
                                        App_lock.applockPassword = limdata[14];

                                    if (runtimeRefreshRate != pref.getInt("runtimeRefreshRate", runtimeRefreshRate)) {
                                        runtimeRefreshRate = pref.getInt("runtimeRefreshRate", runtimeRefreshRate);
                                        if (runtimeRefreshRate > 0) {
                                            if (futureTaskForRuntime != null) {
                                                futureTaskForRuntime.cancel(true);
                                            }

                                            futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
                                        }
                                    }
                                    if (JRMSpotsRefreshRate != pref.getInt("JRMSpotsRefreshRate", JRMSpotsRefreshRate)) {
                                        JRMSpotsRefreshRate = pref.getInt("JRMSpotsRefreshRate", JRMSpotsRefreshRate);
                                        if (JRMSpotsRefreshRate > 0) {
                                            if (futureTaskForJRMspots != null) {
                                                futureTaskForJRMspots.cancel(true);
                                            }

                                            futureTaskForJRMspots = scheduledExecutorServiceForJRMspots.scheduleAtFixedRate(taskForJRMspots, 0, JRMSpotsRefreshRate, TimeUnit.SECONDS);
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        }, 0, 2*60, TimeUnit.SECONDS);

        scheduledExecutorServiceForRuntime = Executors.newScheduledThreadPool(5);

        taskForRuntime = new Runnable()
        {
            @Override
            public void run()
            {
                try{
                    if(Gridmenu.points!=null && Gridmenu.points.length==8 && !Gridmenu.points[7].contains("NA")) {
                        String runAndContinuousTime = GetRunAndContinuousTime(deviceid);
                        SharedPreferences.Editor editor = pref.edit();
                        if (runAndContinuousTime.contains("#")) {
                            editor.putString("runAndContinuousTime", runAndContinuousTime);
                            editor.apply();
                            if (runtimeRefreshRate != pref.getInt("runtimeRefreshRate", runtimeRefreshRate)) {
                                runtimeRefreshRate = pref.getInt("runtimeRefreshRate", runtimeRefreshRate);
                                if (runtimeRefreshRate > 0) {
                                    if (futureTaskForRuntime != null) {
                                        futureTaskForRuntime.cancel(true);
                                    }

                                    futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                        } else {
                            if (runtimeRefreshRate != 10*60) {
                                runtimeRefreshRate = 10*60;
                                if (runtimeRefreshRate > 0) {
                                    if (futureTaskForRuntime != null) {
                                        futureTaskForRuntime.cancel(true);
                                    }

                                    futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        };

        if (runtimeRefreshRate > 0) {
            if (futureTaskForRuntime != null) {
                futureTaskForRuntime.cancel(true);
            }

            futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
        }

        instance = this;
        context=getApplicationContext();
        return START_STICKY;
    }



    public String GetRunAndContinuousTime(String device_id) {
        String SOAP_ACTION = "http://tempuri.org/GetRunAndContinuousTime";
        String OPERATION_NAME = "GetRunAndContinuousTime";

        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public String GetjrmSpots(String device_id) {
        String SOAP_ACTION = "http://tempuri.org/GetjrmSpots";
        String OPERATION_NAME = "GetjrmSpots";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public String GetLimitations(String device_id) {
        String SOAP_ACTION = "http://tempuri.org/GetJrmLimitations";
        String OPERATION_NAME = "GetJrmLimitations";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    private void scheduleMethod() {
        // TODO Auto-generated method stub

        ScheduledExecutorService scheduler = Executors
                .newSingleThreadScheduledExecutor();
        final  PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        //  lastupdate_time =vehicle_speed[2];
        final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", Locale.ENGLISH);

       /* Calendar c = Calendar.getInstance();
        String formattedDate = format.format(c.getTime());
        if(vehicle_speed[2]==null)
        {
            lastupdate_time="10/21/2016 6:37:11 PM";
        }
        try {
            Date last_time= format.parse(lastupdate_time);
            Date current_time=format.parse(formattedDate);
            updategap = current_time.getTime() - last_time.getTime();

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            updategap=(1000 * 120);
        }*/
        scheduler.scheduleAtFixedRate(new Runnable() {

            @SuppressLint("InvalidWakeLockTag")
            @Override
            public void run() {
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && Gridmenu.points!=null && Gridmenu.points.length == 8 && !Gridmenu.points[7].contains("NA")) {
                    if(isNetworkAvailable() && vehicle_speed[3].contains("1")) {
                        if (Float.valueOf(vehicle_speed[1]) > 10) {
                            if (powerManager.isScreenOn()) {
                                if (LockScreenActivity.speed > 10) {
                                    Intent i = new Intent(context, LockScreenActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    LockScreenActivity.flag = false;
                                    if (!blocked) {
                                        ActivityLog("Mobile Locked");
                                        blocked = true;
                                    }
                                    context.startActivity(i);
                                } else {
                                    LockScreenActivity.flag = true;
                                    blocked = false;
                                }
                            } else {
                                mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                                mWakeLock.acquire(100000000);
                            }
                        } else {
                            LockScreenActivity.flag = true;
                            blocked = false;
                        }
                    }
                    else{
                        if(powerManager.isScreenOn()) {
                            if (LockScreenActivity.speed > 10) {
                                Intent i = new Intent(context, LockScreenActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                LockScreenActivity.flag = false;
                                if (!blocked) {
                                    ActivityLog("Mobile Locked");
                                    blocked = true;
                                }
                                context.startActivity(i);
                            } else {
                                LockScreenActivity.flag = true;
                                blocked = false;
                            }
                        }else {
                            mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "tag");
                            mWakeLock.acquire(100000000);
                        }
                    }
                }
                else
                {
                    LockScreenActivity.flag = true;
                    blocked = false;
                }
                }
        //}, 0, 1, TimeUnit.SECONDS); //for every second check
    }, 0, 5, TimeUnit.SECONDS); //for every second check
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void ActivityLog(final String activity)
    {

        GPSTracker gpsTracker=new GPSTracker(context);
        final String[] latlng= gpsTracker.getLocation().split(",");
        Thread activityThread=new Thread() {
            @Override
            public void run() {
                try {
                    WebserviceCall webserviceCall=new WebserviceCall();
                    webserviceCall.KeepActivityLog(deviceid,activity,latlng[0],latlng[1]);
                }
                catch(Exception e)
                {
                }
            }
        };
        activityThread.start();
    }
@Override
    public void onDestroy()
    {
        super.onDestroy();
        stopForeground(true);
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }

}