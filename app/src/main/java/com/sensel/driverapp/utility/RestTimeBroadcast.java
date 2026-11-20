package com.sensel.driverapp.utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sensel.driverapp.LockScreenActivity;
import com.sensel.driverapp.R;
import com.sensel.driverapp.RestTime;
import com.sensel.driverapp.WrokStartandWorkEnd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.content.Context.MODE_PRIVATE;

public class RestTimeBroadcast extends BroadcastReceiver {
    private static final String CHANNEL_ID ="channel_0112";
    public static Long totalmins;
    public static int hrs;
    public static int mincounter;
    Context mContext;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("brodcast","called");

        mContext=context;
        try {
            String currdate;

            //getting current date
            DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();
            currdate=dateFormat.format(date);
            SharedPreferences prefs = context.getSharedPreferences("RESTSTARTEND", MODE_PRIVATE);
            String endtime = prefs.getString("enddate", "");
            totalmins=prefs.getLong("minsdiffrence",0);

            Log.e("total",String.valueOf(totalmins));


            if(endtime.compareTo(currdate)>0){
                mincounter++;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date d1 = sdf.parse(endtime);
                Date d2 = sdf.parse(currdate);

                long difference_In_Time = d1.getTime() - d2.getTime();
                long difference_In_Minutes = (difference_In_Time  / 1000) / 60;

                Log.e("d",String.valueOf(difference_In_Minutes));


                Long l= new Long(difference_In_Minutes);
                hrs=l.intValue();
//                RestTime.restProgessbarUpdator(hrs);







            }

            else{
//                RestTime.restProgessbarUpdator(0);

                createNotificationChannel();
                Intent nintent = new Intent(mContext, RestTime.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, nintent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.icon)
                        .setContentTitle("Rest time ends")
                        .setContentText("Click to close the rest")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        // Set the intent that will fire when the user taps the notification
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

                notificationManager.notify(2, builder.build());



            }

        }
        catch (Exception e){

            Log.e("ex",e.toString());

        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Rest",
                    importance);
            channel.setDescription("9 hrs rest done");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
