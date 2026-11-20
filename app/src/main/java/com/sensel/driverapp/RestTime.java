package com.sensel.driverapp;

import static android.app.PendingIntent.FLAG_MUTABLE;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sensel.driverapp.utility.RestTimeBroadcast;
import com.sensel.driverapp.utility.WorktimeBrodcast;
import com.sensel.driverapp.utility.WorktimeUploader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RestTime extends AppCompatActivity {
    Button startbtn,endbtn;
    TextView wrkhrtxt,wrkhrdescrxt;
    Context context;
    public static String prefname="RESTSTARTEND";
    public static String datekey="enddate";
    public static String buttonkey="restbtn";
    public static ProgressBar resttimeprogressBar;
    public static TextView texttime;
    public Timer timer;
    public static Long totalmins;
    public static int hrs;
    public static int resttime=540;//9hrs default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_time);

        context=getApplicationContext();
        resttimeprogressBar=findViewById(R.id.resttimeprogress);
        startbtn=findViewById(R.id.startrest);
        endbtn=findViewById(R.id.endrest);
        texttime=findViewById(R.id.resttimer);




        SharedPreferences pref=context.getSharedPreferences(prefname, Activity.MODE_PRIVATE);
        String btnvalue=pref.getString(buttonkey,"");

        if(btnvalue==""||btnvalue.equals(null)||btnvalue.contains("start")){
            startbtn.setVisibility(View.VISIBLE);
            endbtn.setVisibility(View.GONE);
        }

        else{
            startbtn.setVisibility(View.GONE);
            endbtn.setVisibility(View.VISIBLE);
            countdowntimer();
        }


        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(RestTime.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_workstartconfirm);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                Button startwork=dialog.findViewById(R.id.d_startwork);
                TextView quest=dialog.findViewById(R.id.question);
                quest.setText("Want to start rest?");
                startwork.setText("start");
                startwork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefname,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();


                        String currdate,logoffdate;
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                        //getting current date
                        Date date = new Date();
                        currdate=dateFormat.format(date);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());
                        cal.add(Calendar.MINUTE,resttime);
                        cal.getTime();
                        logoffdate=dateFormat.format(cal.getTime());

                        Log.e("logoffdate",logoffdate);

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        Date d1 = null;
                        Date d2 = null;
                        try {
                            d1 = sdf.parse(logoffdate);
                            d2 = sdf.parse(currdate);


                            long difference_In_Time = d1.getTime() - d2.getTime();
                            long difference_In_Minutes = (difference_In_Time  / 1000) / 60;

                            Long l= new Long(difference_In_Minutes);

                            RestTimeBroadcast.hrs=l.intValue();

                            editor.putString(buttonkey, "stop");
                            editor.putString(datekey, logoffdate);
                            editor.putLong("minsdiffrence",difference_In_Minutes);


                            Boolean commit=editor.commit();
                            while (!commit){
                                commit=editor.commit();
                                Log.e("commt","called");
                            }

                            Log.e("difference_In_Time",String.valueOf(difference_In_Time));

                            long time= System.currentTimeMillis();
                            //setRepeataction(time);

                            dialog.dismiss();
                            finish();
                            Toast.makeText(context,"Done",Toast.LENGTH_LONG).show();

                        }


                        catch (Exception e){ }


                    }
                });

                dialog.show();
            }
        });

        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(RestTime.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_workstartconfirm);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                Button startwork=dialog.findViewById(R.id.d_startwork);
                TextView quest=dialog.findViewById(R.id.question);
                quest.setText("Want to end rest?");

                startwork.setText("END");
                startwork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stoplararm();


                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefname,MODE_PRIVATE);


                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(buttonkey, "start");
                        editor.putString(datekey, "");
                        editor.apply();

                        dialog.dismiss();
                        finish();

                    }
                });

                dialog.show();
            }
        });


        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void restProgessbarUpdator(int workdone) {

        try{

            int hours=workdone/60;
            int minutes=workdone%60;

            Log.e("hrs",String.valueOf(hours));

            texttime.setText(String.valueOf(hours)+":"+String.valueOf(minutes));



            Long l= new Long(resttime);
            int hrs=l.intValue();
            if(workdone==0){
                resttimeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));

            }
            else {
                resttimeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            }
            resttimeprogressBar.setMax(hrs);
            resttimeprogressBar.setSecondaryProgress(hrs-workdone);


        }
        catch (Exception e){

        }

    }

    private void setRepeataction(long time) {

        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        /*Intent intent=new Intent(this, RestTimeBroadcast.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 3,intent,PendingIntent.FLAG_MUTABLE);*/
        Intent notifyIntent = new Intent(this, RestTimeBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                3,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                time,1*60*1000,
                pendingIntent);
        Log.e("alarm","set");
    }


    private void stoplararm(){
        PendingIntent alarmIntent;
        /*alarmIntent = PendingIntent.getBroadcast(this, 3,
                new Intent(this, RestTimeBroadcast.class),
                PendingIntent.FLAG_CANCEL_CURRENT);*/
        alarmIntent = PendingIntent.getBroadcast(this, 3,
                new Intent(this, RestTimeBroadcast.class),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);
    }

    private void countdowntimer(){
        SharedPreferences prefs = context.getSharedPreferences("RESTSTARTEND", MODE_PRIVATE);
        String endtime = prefs.getString("enddate", "");
        totalmins=prefs.getLong("minsdiffrence",0);

        Log.e("total",String.valueOf(totalmins));
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try{
                    String currdate;

                    //getting current date
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    Date date = new Date();
                    currdate=dateFormat.format(date);



                    if(endtime.compareTo(currdate)>0){
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        Date d1 = sdf.parse(endtime);
                        Date d2 = sdf.parse(currdate);

                        long difference_In_Time = d1.getTime() - d2.getTime();
                        long difference_In_Minutes = (difference_In_Time  / 1000) / 60;

                        //Log.e("diffrence",String.valueOf(difference_In_Minutes));


                        Long l= new Long(difference_In_Minutes);
                        hrs=l.intValue();
                        restProgessbarUpdator(hrs);


                    }

                    else{
                        restProgessbarUpdator(0);
                    }



                }
                catch (Exception e){
                    Log.e("exception",e.toString());
                }
            }
        }, 0,60000);
    }
}