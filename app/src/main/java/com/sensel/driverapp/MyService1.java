package com.sensel.driverapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


/**
 * Created by User on 17-01-2017.
 */
public class MyService1 extends Service
{
    private static Timer timer = new Timer();
    public Boolean userAuth = false;
    private Context ctx;
    static String currentApp="";
    static String prvapp="";
    static ArrayList packages = new ArrayList();
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

//    public void onCreate()
//    {
//        super.onCreate();
//        ctx = this;
//        startService();
//    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ctx = this;
        startService();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "myservice";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Service notification",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon).build();

            //startForeground(1, notification);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
                startForeground(1, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC); // only if needed
            } else {
                startForeground(1, notification);
            }
        }
        return START_STICKY;
    }

    private void startService()
    {
        timer.scheduleAtFixedRate(new mainTask(), 0, 500);
        timer.scheduleAtFixedRate(new getLockedAppTask(), 0, 1000*60*10);//Every 10 min
    }

    private class mainTask extends TimerTask
    {
        public void run()
        {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 5, time);
                if (appList != null && appList.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                    for (UsageStats usageStats : appList) {
                        mySortedMap.put(usageStats.getLastTimeUsed(),
                                usageStats);
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
                else{
                   // startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            } else {
                ActivityManager am = (ActivityManager) getBaseContext().getSystemService(ACTIVITY_SERVICE);
                currentApp = am.getRunningTasks(1).get(0).topActivity .getPackageName();
            }
            for (int i=0;i<packages.size();i++) {
                if((currentApp.trim().equals(packages.get(i).toString().trim()) && !App_lock.isopened))
                {
                    Intent intent = new Intent(ctx, App_lock.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(intent);
                    break;
                }
            }

            if(!prvapp.equals(currentApp) && !currentApp.equals("com.sensel.driverapp"))
            {
                prvapp=currentApp;
                App_lock.isopened=false;
            }
        }
    }

    private class getLockedAppTask extends TimerTask
    {
        public void run()
        {
            Thread backThread=new Thread() {
                @Override
                public void run() {
                    try {
                     String result=getLockedApps(Gridmenu.device_id);
                        final JSONArray jArr = new JSONArray(result);
                        packages.clear();
                        for (int j = 0; j < jArr.length(); j++) {
                            try {
                                JSONObject data = jArr.getJSONObject(j);
                                packages.add(data.getString("PackageName"));

                            } catch (JSONException js) {

                            }
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
            };
            backThread.start();
        }

    }


@Override
    public void onDestroy()
    {
        super.onDestroy();
        stopForeground(true);
        Toast.makeText(this, "Service Stopped ...", Toast.LENGTH_SHORT).show();
    }

    public  String getLockedApps(String device_id)
    {
        String SOAP_ACTION = "http://tempuri.org/GetLockedApps";
        String OPERATION_NAME = "GetLockedApps";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }
}