package com.sensel.driverapp;

import static android.app.PendingIntent.FLAG_MUTABLE;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sensel.driverapp.utility.Dbhelper;
import com.sensel.driverapp.utility.WorktimeBrodcast;
import com.sensel.driverapp.utility.WorktimeUploader;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class WrokStartandWorkEnd extends AppCompatActivity {
    Button startbtn,endbtn;
    TextView wrkhrtxt,wrkhrdescrxt;
    public  static Context context;
    String prefname,datekey,buttonkey;
    int workhrs,workingmins;
    public static Long totalmins;
    public static int hrs;
    public Timer timer;
    public static ProgressBar timeprogressBar;
    public static TextView remaiworktimetxt,remaiingtimetxtx;

    public static String Restresponse;
    Dbhelper DB;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wrok_startand_work_end);

        WorktimeUploader.IMEINO=getIMEI();

        remaiworktimetxt=findViewById(R.id.remaiworktime);
        timeprogressBar=findViewById(R.id.workprogressBar);
        remaiingtimetxtx=findViewById(R.id.remainingtime);


        prefname="WORKSTARTEND";
        datekey="enddate";
        buttonkey="workbtn";
        context=getApplicationContext();
        wrkhrtxt=findViewById(R.id.assignedhrs);
        try {
            workhrs=CustomAndroidGridViewAdapter.worktime/60;
            workingmins=CustomAndroidGridViewAdapter.worktime%60;
            wrkhrtxt.setText("Total assigned working hours="+String.valueOf(workhrs)+" hrs " +String.valueOf(workingmins)+" mins");
        }
        catch (Exception e){

        }









        startbtn=findViewById(R.id.startwork);
        endbtn=findViewById(R.id.endwork);

        SharedPreferences pref=context.getSharedPreferences(prefname, Activity.MODE_PRIVATE);
        String btnvalue=pref.getString(buttonkey,"");

        if(btnvalue==""||btnvalue.equals(null)||btnvalue.contains("start")){
            startbtn.setVisibility(View.VISIBLE);
            endbtn.setVisibility(View.GONE);
        }

        else{
            startbtn.setVisibility(View.GONE);
            endbtn.setVisibility(View.VISIBLE);
            localworktimer();
        }

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(WrokStartandWorkEnd.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_workstartconfirm);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                Button startwork=dialog.findViewById(R.id.d_startwork);
                TextView quest=dialog.findViewById(R.id.question);
                quest.setText("Want to start work?");
                startwork.setText("start");
                startwork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Toast.makeText(context,"clicked start work",Toast.LENGTH_LONG).show();
                        DB=new Dbhelper(getApplicationContext());
                        try{
                            //Toast.makeText(context,"entered try block",Toast.LENGTH_LONG).show();
                            Boolean checkinserted=DB.insertdata("startwork",getdevicetime(),"no");
                            if (!checkinserted){
                                //Toast.makeText(context,"entered checkinserted",Toast.LENGTH_LONG).show();
                                DB.insertdata("startwork",getdevicetime(),"no");
                            }
                            //Toast.makeText(context,"completed checkinserted",Toast.LENGTH_LONG).show();
                        }
                        catch (Exception e){

                            //loop the insert to db
                            Log.e("exception",e.toString());
                        }

                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefname,MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();




                        String currdate,logoffdate;
                        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

                        //getting current date
                        Date date = new Date();
                        currdate=dateFormat.format(date);

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(new Date());

                        cal.add(Calendar.MINUTE, CustomAndroidGridViewAdapter.worktime);
                        cal.getTime();
                        logoffdate=dateFormat.format(cal.getTime());

                        Log.e("logoffdate",logoffdate);

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        Date d1 = null;
                        Date d2 = null;
                        try {

                            d1=sdf.parse(logoffdate);

                            d2=sdf.parse(currdate);


                            long difference_In_Time = d1.getTime() - d2.getTime();



                            long difference_In_Minutes= (difference_In_Time  / 1000) / 60;

                            editor.putString(buttonkey, "stop");
                            editor.putString(datekey, logoffdate);
                            editor.putLong("minsdiffrence",difference_In_Minutes);
                            Boolean commit=editor.commit();
                            while (!commit){
                                commit=editor.commit();
                                Log.e("commt","called");
                            }

                            long time= System.currentTimeMillis();

                            if (d1 != null) {
                                setRepeataction(d1.getTime());
                            }

                            uploaderBroadcast(time);

                            Upload();
                            dialog.dismiss();
                        //    Toast.makeText(context,"Done",Toast.LENGTH_LONG).show();
                           // finish();
                        } catch (Exception e){
                            Log.e("WrokStartandWorkEnd", "Error in start work: ", e);
                            try {
                                if (dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                });

                dialog.show();
            }
        });

        endbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(WrokStartandWorkEnd.this);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.dialog_workstartconfirm);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                Button startwork=dialog.findViewById(R.id.d_startwork);
                TextView quest=dialog.findViewById(R.id.question);
                quest.setText("Want to end work?");

                startwork.setText("END");
                startwork.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        stoplararm();

                        DB=new Dbhelper(getApplicationContext());
                        try{
                            Boolean checkinserted=DB.insertdata("endwork",getdevicetime(),"no");
                            if (!checkinserted){
                                DB.insertdata("endwork",getdevicetime(),"no");
                            }
                        }
                        catch (Exception e){
                            try{
                                Boolean checkinserted=DB.insertdata("endwork",getdevicetime(),"no");
                                if (!checkinserted){
                                    DB.insertdata("endwork",getdevicetime(),"no");
                                }
                            }
                            catch (Exception exception){

                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        new ContextThemeWrapper(WrokStartandWorkEnd.this, android.R.style.Theme_Holo_Light_Dialog));
                                alertDialogBuilder.setIcon(R.drawable.error);
                                alertDialogBuilder.setTitle("Not Updated");
                                alertDialogBuilder.setMessage("Not updated try again")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok", (dialog, id) -> {
                                            dialog.cancel();
                                            finish();
                                            System.exit(0);
                                        });
                                AlertDialog alert = alertDialogBuilder.create();
                                alert.show();
                            }


                            Log.e("exception",e.toString());
                        }

                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        workProgessbarUpdator(0);
                        LockScreenActivity.workProgessbarUpdator(0);

                        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefname,MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString(buttonkey, "start");
                        editor.putString(datekey, "");
                        Boolean commit=editor.commit();
                        while (!commit){
                            commit=editor.commit();
                            Log.e("commt","called");
                        }

                        long time= System.currentTimeMillis();
                        uploaderBroadcast(time);
                        Upload();
                        dialog.dismiss();

                       // Toast.makeText(context,"Done",Toast.LENGTH_LONG).show();
                       // finish();






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

    private void setRepeataction(long triggerAtMillis) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, WorktimeBrodcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (alarmManager != null) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
            Log.e("alarm", "Work alarm set for " + triggerAtMillis);
        } catch (Exception e) {
            Log.e("alarm", "Error setting work alarm: " + e.getMessage());
        }
    }

    private void stoplararm(){
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 2,
                new Intent(this, WorktimeBrodcast.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && alarmIntent != null) {
            alarmManager.cancel(alarmIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(WorktimeBrodcast.NOTIFICATION_ID);
        }
    }


    private void uploaderBroadcast(long time){
       // Toast.makeText(context,"entered uploaderBroadcast",Toast.LENGTH_LONG).show();
        AlarmManager alarmManager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
        /*Intent intent=new Intent(this, WorktimeUploader.class);
        PendingIntent pendingIntent=PendingIntent.getBroadcast(this, 5,intent,PendingIntent.FLAG_MUTABLE);*/
        Intent notifyIntent = new Intent(this, WorktimeUploader.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                5,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                time,15*60*1000,
                pendingIntent);
        Log.e("uploader","set");

    }

   public static void stopUploadBroadcast(){
        PendingIntent alarmIntent;
        alarmIntent = PendingIntent.getBroadcast(context, 5,
                new Intent(context, WorktimeUploader.class),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager=(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(alarmIntent);


    }

    //getting device data and time
    public String getdevicetime(){
        String strlastupdate;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
        Date date = new Date();
        strlastupdate=dateFormat.format(date);
        return strlastupdate;
    }

    public String getIMEI() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            try {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String device_id = tm.getDeviceId();
                return device_id;
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), "Give READ_PHONE_STATE permission", Toast.LENGTH_LONG).show();
                return null;
            }
        }
        else{
            String android_id= Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id =android_id;
            }
            return device_id;
        }
    }

    public static void workProgessbarUpdator(int workdone) {

        try{

            Long l= new Long(CustomAndroidGridViewAdapter.worktime);
            int hrs=l.intValue();


            int hours=workdone/60;
            int minutes=workdone%60;

            remaiworktimetxt.setText(String.valueOf(hours)+":"+String.valueOf(minutes));

            remaiingtimetxtx.setText(String.valueOf(hours)+" hours "+String.valueOf(minutes)+" minutes REMAINING");

            if(workdone==0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    timeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }

            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    timeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                }
            }
//           timeprogressBar.setMax(hrs);
            timeprogressBar.setMax(hrs);
            timeprogressBar.setSecondaryProgress(hrs-workdone);


        }
        catch (Exception e){

        }



    }


    private void Upload() {
        ProgressDialog nDialog = new ProgressDialog(WrokStartandWorkEnd.this);
        uploadInternal(nDialog);
    }

    private void uploadInternal(final ProgressDialog nDialog) {
        final Dbhelper database = new Dbhelper(getApplicationContext());
        Cursor res = database.getdata();
        if (res != null && res.getCount() > 0) {
            if (!nDialog.isShowing() && !isFinishing()) {
                nDialog.setMessage("Uploading");
                nDialog.setTitle("Please wait");
                nDialog.setIndeterminate(false);
                nDialog.setCancelable(false);
                nDialog.show();
            }
            try {
                String status = "";
                String time = "";
                while (res.moveToNext()) {
                    status = res.getString(0);
                    time = res.getString(1);
                }
                res.close();

                JSONObject mainObj = new JSONObject();
                mainObj.put("IMEI", getIMEI());

                String uploadURL = "";
                if (status.contains("startwork")) {
                    mainObj.put("startTime", time);
                    uploadURL = Config.DOMAINURL1 + "/SenselRestService.svc/rest/v3/PostWorkStartData";
                } else {
                    mainObj.put("endTime", time);
                    uploadURL = Config.DOMAINURL1 + "/SenselRestService.svc/rest/v3/PostWorkEndData";
                }

                Log.e("object", mainObj.toString());

                RequestQueue requestQueue = Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        uploadURL,
                        mainObj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("response", response.toString());
                                try {
                                    String name = response.getString("result");
                                    if (name.contains("Success") || name.contains("Vehicle not assigned")) {
                                        database.updateyes();
                                        database.deletedata();
                                        // Sequentially upload next pending row if any
                                        uploadInternal(nDialog);
                                        return;
                                    }
                                } catch (JSONException e) {
                                    Log.e("ex", e.toString());
                                }
                                if (nDialog.isShowing() && !isFinishing()) {
                                    nDialog.dismiss();
                                }
                                finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (nDialog.isShowing() && !isFinishing()) {
                                    nDialog.dismiss();
                                }
                                finish();
                                Log.e("error", error.toString());
                            }
                        }
                );

                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        15000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                ));

                requestQueue.add(jsonObjectRequest);

            } catch (Exception e) {
                database.updateno();
                if (nDialog.isShowing() && !isFinishing()) {
                    nDialog.dismiss();
                }
                finish();
                Log.e("ex", e.toString());
            }
        } else {
            if (res != null) {
                res.close();
            }
            if (nDialog.isShowing() && !isFinishing()) {
                nDialog.dismiss();
            }
            finish();
        }
    }



    public void localworktimer(){
        SharedPreferences prefs = context.getSharedPreferences("WORKSTARTEND", MODE_PRIVATE);
        String endtime = prefs.getString("enddate", "");
        totalmins=prefs.getLong("minsdiffrence",0);


            //getting current date



            //Log.e("total",String.valueOf(totalmins));

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

        try {
            if (endtime != null && !endtime.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Date d1 = sdf.parse(endtime);
                Date d2 = new Date();

                if (d1.after(d2)) {
                    long difference_In_Time = d1.getTime() - d2.getTime();
                    long difference_In_Minutes = (difference_In_Time  / 1000) / 60;

                    //Log.e("diffrence",String.valueOf(difference_In_Minutes));

                    Long l= new Long(difference_In_Minutes);
                    hrs=l.intValue();
                    LockScreenActivity.workProgessbarUpdator(hrs);
                    workProgessbarUpdator(hrs);
                }
                else {
                    LockScreenActivity.workProgessbarUpdator(0);
                    workProgessbarUpdator(0);
                }
            } else {
                LockScreenActivity.workProgessbarUpdator(0);
                workProgessbarUpdator(0);
            }
        }
        catch (Exception e){

            Log.e("ex",e.toString());

        }

            }
        }, 0,60000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}