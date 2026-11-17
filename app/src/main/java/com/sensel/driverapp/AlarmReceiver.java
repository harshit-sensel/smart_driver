package com.sensel.driverapp;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mallikarjun on 14-01-2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public String SOAP_ACTION = "http://tempuri.org/SendSMS";
    public String OPERATION_NAME = "SendSMS";
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    Context cont;
    public String result;
    public String latlng = "0,0";
    public static int count = 0;
    public static String latest_notification="";
    private static int previouscount = 0;
    public static int NOTIFICATION_ID = 1;
    final static String GROUP_KEY_EMAILS = "group_key_emails";
    public static int poscount = 0;
    private static int priority_count=0;
    Timer timer;
    TimerTask timerTask;
    String device_id="0";

    @Override
    public void onReceive(final Context context, Intent intent) {
        poscount++;
        result = "0";
        cont = context;
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                == PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                TelephonyManager tm = (TelephonyManager) cont.getSystemService(Context.TELEPHONY_SERVICE);
                device_id = tm.getDeviceId();
            }
            else{
                String android_id= Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id != null) {
                    device_id =android_id;
                }
            }
        }

        GPSTracker gpsTracker = new GPSTracker(cont);
        latlng = gpsTracker.getLocation();
        //get and send location information
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < 500) {
                        sleep(100);
                        waited += 100;
                        if (waited == 100) {
                            if (poscount == 5) {
                                /*insert position data is stopped because from dashboard gps data is sending*/
                                //InsertPositionData(device_id);
                                poscount = 0;
                            }
                            //make screen of
                              //  context.stopService(new Intent(context,SaveMyAppsService.class));
                             //   context.startService(new Intent(context, SaveMyAppsService.class));
                            //

                            result = CheckNotifications(device_id);
                            String res=CheckVehicleSpeed(device_id);
                            if(!res.contains("Exception")) {
                                SaveMyAppsService.vehicle_speed = res.split(",");
                            }
                            else {
                                SaveMyAppsService.allow=true;
                            }
                            try {
                                JSONArray jArr = new JSONArray(result);
                                PriorityNotification.message="";
                                JSONObject d = jArr.getJSONObject(0);
                                count = Integer.parseInt(d.getString("count"));
                                latest_notification=d.getString("Subject");
                                if (count > 0 && previouscount != count) {
                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(cont);
                                    Intent nid = new Intent(cont, Notification1.class);
                                    PendingIntent ci = PendingIntent.getActivity(cont, NOTIFICATION_ID, nid, PendingIntent.FLAG_UPDATE_CURRENT);

                                    NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(cont);
                                    summaryNotification.setContentTitle("Smart_Driver app");
                                    summaryNotification.setContentText(count + "  new notification(s)");
                                    summaryNotification.setSmallIcon(R.mipmap.icon);
                                    summaryNotification.setTicker("New Notification(s) from Smart_Driver");
                                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                                    for (int j = 0; j < jArr.length(); j++) {
                                        JSONObject data = jArr.getJSONObject(j);
                                        if (Integer.parseInt(data.getString("Isnotified"))==0) {
                                            style.addLine(data.getString("Subject") + " : " + data.getString("Info"));
                                            summaryNotification.setGroup(GROUP_KEY_EMAILS);

                                            //check for priority notification
                                            if(Integer.parseInt(data.getString("Priority"))>0)
                                            {
                                                if(priority_count==0) {
                                                    PriorityNotification.message = data.getString("Subject") + " : " + data.getString("Info");
                                                    priority_count++;
                                                }
                                                else {
                                                    PriorityNotification.message = PriorityNotification.message + "<br><br>" + data.getString("Subject") + " : " + data.getString("Info");
                                                    priority_count++;
                                                }

                                            }
                                        }
                                    }
                                    style.setSummaryText(count + "  new notification(s)");
                                    summaryNotification.setStyle(style);
                                    summaryNotification.setGroupSummary(true);
                                    //Different sound for different notification
                                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                    Uri max_driving =Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.max_driving);//Here is FILE_NAME is the name of file that you want to play
                                    Uri night_driving =Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.night_driving);//Here is FILE_NAME is the name of file that you want to play
                                    Uri over_speed =Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.over_speed);//Here is FILE_NAME is the name of file that you want to play
                                    Uri continues_driving =Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.continues_driving);
                                   if(latest_notification.equals("Maximum driving"))
                                    summaryNotification.setSound(max_driving);
                                   else if(latest_notification.equals("NIGHT DRIVING Alert"))
                                       summaryNotification.setSound(night_driving);
                                    else if(latest_notification.equals("OVERSPEEDING Alert"))
                                       summaryNotification.setSound(over_speed);
                                   else if(latest_notification.equals("CONTINUOUS DRIVING Alert"))
                                       summaryNotification.setSound(continues_driving);
                                    else
                                       summaryNotification.setSound(alarmSound);
                                    summaryNotification.setContentIntent(ci);
                                    summaryNotification.setAutoCancel(true);

                                    Notification notification = summaryNotification.build();
                                    notificationManager.notify(NOTIFICATION_ID, notification);
                                    previouscount = count;
                                }


                            } catch (Exception e) {
                            }

                        }
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                     if(priority_count>0)
                     {
                         try {
                             Intent i = new Intent(cont, PriorityNotification.class);
                             i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                             cont.startActivity(i);
                         }catch (Exception e){}
                     }
                    priority_count=0;
                }
            }

        };
            if(isNetworkAvailable())
            {
                splashThread.start();
            }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) cont.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public String InsertPositionData(String device_id) {

        String lat = latlng.substring(0, latlng.indexOf(","));
        String lng = latlng.substring(latlng.indexOf(",") + 1);
        SOAP_ACTION = "http://tempuri.org/PositionData";
        String OPERATION_NAME = "PositionData";
        PropertyInfo[] arraypi=new PropertyInfo[3];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        arraypi[0]=pi;
        pi = new PropertyInfo();
        pi.setName("lat");
        pi.setValue(lat);
        pi.setType(String.class);
        arraypi[1]=pi;
        pi = new PropertyInfo();
        pi.setName("lng");
        pi.setValue(lng);
        pi.setType(String.class);
        arraypi[2]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,arraypi,SOAP_ACTION);
    }


    public String CheckNotifications(String device_id) {
        SOAP_ACTION = "http://tempuri.org/CheckNotification";
        OPERATION_NAME = "CheckNotification";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public String CheckVehicleSpeed(String device_id) {

        SOAP_ACTION = "http://tempuri.org/GetVehicleSpeedByIMEI";
        OPERATION_NAME = "GetVehicleSpeedByIMEI";
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



