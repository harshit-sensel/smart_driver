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
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.sensel.driverapp.CustomAndroidGridViewAdapter;
import com.sensel.driverapp.LockScreenActivity;
import com.sensel.driverapp.R;
import com.sensel.driverapp.RestTime;
import com.sensel.driverapp.WrokStartandWorkEnd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.FLAG_MUTABLE;
import static android.content.Context.MODE_PRIVATE;

public class WorktimeBrodcast extends BroadcastReceiver {
    private static final String CHANNEL_ID = "channel_0111";
    public static final int NOTIFICATION_ID = 1001;
    public static Long totalmins;
    public static int hrs;
    public static int mincounter;

    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("WorktimeBrodcast", "onReceive called");
        mContext = context;

        try {
            SharedPreferences prefs = context.getSharedPreferences("WORKSTARTEND", MODE_PRIVATE);
            String buttonState = prefs.getString("workbtn", "start");
            String endtime = prefs.getString("enddate", "");

            // If work is not currently active, do nothing
            if (!"stop".equalsIgnoreCase(buttonState) || endtime == null || endtime.trim().isEmpty()) {
                Log.d("WorktimeBrodcast", "Work is not active or enddate empty, ignoring.");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date endDate = sdf.parse(endtime);
            Date now = new Date();

            // If phone rebooted and endtime has not arrived yet, reschedule the alarm
            if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
                if (endDate != null && endDate.after(now)) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent workIntent = new Intent(context, WorktimeBrodcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            2,
                            workIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );
                    if (alarmManager != null) {
                        try {
                            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endDate.getTime(), pendingIntent);
                        } catch (Exception e) {
                            Log.e("WorktimeBrodcast", "Error rescheduling alarm: " + e.getMessage());
                        }
                    }
                    Log.d("WorktimeBrodcast", "Rescheduled alarm after reboot for " + endtime);
                    return;
                }
            }

            // End time has arrived: show clean system notification
            createNotificationChannel();

            Intent nintent = new Intent(mContext, WrokStartandWorkEnd.class);
            nintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    mContext,
                    0,
                    nintent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.icon)
                    .setContentTitle("Work Shift Completed")
                    .setContentText("Your work shift has completed. Tap to end work.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        } catch (Exception e) {
            Log.e("WorktimeBrodcast", "Error in onReceive: " + e.toString());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Work Shift Alert",
                    importance);
            channel.setDescription("Alert when work shift ends");
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
