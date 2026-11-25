package com.sensel.driverapp.utility;

import android.app.AlarmManager;
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
    private static final String CHANNEL_ID = "channel_0112";
    public static final int NOTIFICATION_ID = 1002;
    public static Long totalmins;
    public static int hrs;
    public static int mincounter;
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("RestTimeBroadcast", "onReceive called");
        mContext = context;

        try {
            SharedPreferences prefs = context.getSharedPreferences("RESTSTARTEND", MODE_PRIVATE);
            String buttonState = prefs.getString("restbtn", "start");
            String endtime = prefs.getString("enddate", "");

            // If rest is not currently active, do nothing
            if (!"stop".equalsIgnoreCase(buttonState) || endtime == null || endtime.trim().isEmpty()) {
                Log.d("RestTimeBroadcast", "Rest is not active or enddate empty, ignoring.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date endDate = sdf.parse(endtime);
            Date now = new Date();

            // If phone rebooted and endtime has not arrived yet, reschedule the alarm
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                if (endDate != null && endDate.after(now)) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent restIntent = new Intent(context, RestTimeBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            3,
                            restIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    if (alarmManager != null) {
                        try {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endDate.getTime(), pendingIntent);
                        } catch (Exception e) {
                            Log.e("RestTimeBroadcast", "Error rescheduling alarm: " + e.getMessage());
                        }
                    }
                    Log.d("RestTimeBroadcast", "Rescheduled alarm after reboot for " + endtime);
                    return;
                }
            }

            // End time has arrived: show clean system notification
            createNotificationChannel();

            Intent nintent = new Intent(mContext, RestTime.class);
            nintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    nintent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("Rest Period Completed")
                    .setContentText("Your 9-hour rest period is complete. Tap to end rest.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            Log.e("RestTimeBroadcast", "Error in onReceive: " + e.toString());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Rest Alert",
                    importance);
            channel.setDescription("Alert when 9 hrs rest is done");
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
