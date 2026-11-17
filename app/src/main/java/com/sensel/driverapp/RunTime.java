package com.sensel.driverapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.TextView;

import org.ksoap2.serialization.PropertyInfo;

/**
 * Created by User on 27-12-2016.
 */

public class RunTime  extends AppCompatActivity {
    private SpeedometerView speedometer;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    int hr=0,min=0;
    int dayHr=9;
    int dayMin=30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_runtime);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        // Customize SpeedometerView
        speedometer = (SpeedometerView) findViewById(R.id.speedometer);


        // Add label converter
        speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });

        // configure value range and ticks
        speedometer.setMaxSpeed(12);
        speedometer.setMajorTickStep(1);
        speedometer.setMinorTicks(1);
       // speedometer.setSpeed(0,100,10);
        // Configure value range colors
        speedometer.addColoredRange(0, 8, Color.GREEN);
        speedometer.addColoredRange(8, 10, Color.YELLOW);
        speedometer.addColoredRange(10, 12, Color.RED);

        final TextView vehicle=(TextView) findViewById(R.id.vehicle);
        final TextView runtime=(TextView) findViewById(R.id.runtime);
        final TextView remainingtime=(TextView) findViewById(R.id.remainingtime);
        final TextView updatetime=(TextView) findViewById(R.id.updatetime);
        final ProgressDialog progressDialog = new ProgressDialog(RunTime.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Please wait...");
        progressDialog.show();
        SharedPreferences pref = getApplicationContext().getSharedPreferences("totaljrm", MODE_PRIVATE);
        dayHr = pref.getInt("dayHr",dayHr);
        dayMin = pref.getInt("dayMin",dayMin);

        Thread runtimeThread = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    int waited = 0;
                    while (waited < 800) {
                        sleep(100);
                        waited += 100;
                        if(waited==100)
                        {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    SharedPreferences pref = getApplicationContext().getSharedPreferences("totaljrm", MODE_PRIVATE);
                                    String deviceid="";
                                    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                                        deviceid = tm.getDeviceId();
                                    } else {
                                        String android_id = Settings.Secure.getString(getContentResolver(),
                                                Settings.Secure.ANDROID_ID);
                                        if (android_id != null) {
                                            deviceid = android_id;
                                        }
                                    }
                                    String runAndContinuousTime = GetRunAndContinuousTime(deviceid);
                                    SharedPreferences.Editor editor = pref.edit();
                                    if (runAndContinuousTime.contains("#")) {
                                        editor.putString("runAndContinuousTime", runAndContinuousTime);
                                        editor.commit();
                                    }
                                    String run_time = pref.getString("runAndContinuousTime","0h  0min#01-01-2017 00:00:01 AM#0h  0min#0#VehicleId");
                                    if(run_time.contains("#"))
                                    {
                                        final String[] data=run_time.split("#");
                                        String[] res = data[0].split(" ");
                                        String[] h = res[0].split("h");
                                        String[] m = res[2].split("m");
                                        final  int dec_h=Integer.parseInt(h[0]);
                                        double decimal_min=Integer.parseInt(m[0])/60.0;
                                        final double dec=dec_h+decimal_min;

                                        if ((Integer.parseInt(h[0]) < dayHr)||(Integer.parseInt(h[0]) == dayHr && Integer.parseInt(m[0]) < dayMin)) {
                                            if(dayMin>=Integer.parseInt(m[0])) {
                                                min = dayMin - Integer.parseInt(m[0]);
                                                hr = dayHr - Integer.parseInt(h[0]);
                                            }
                                            else{
                                                min=dayMin+60 - Integer.parseInt(m[0]);
                                                hr=dayHr-1-Integer.parseInt(h[0]);
                                            }
                                        } else {
                                            hr = 0;
                                            min = 00;
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                vehicle.setText(data[4]);
                                                updatetime.setText(data[1]);
                                                runtime.setText(data[0]);
                                                remainingtime.setText(hr +"h "+min+"min");
                                                if(dec>0)
                                                    speedometer.setSpeed(dec,dec_h*1000,1000);
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    progressDialog.dismiss();
                }
            }

        };
        runtimeThread.start();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
