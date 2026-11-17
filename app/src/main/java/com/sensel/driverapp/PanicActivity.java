package com.sensel.driverapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.ksoap2.serialization.PropertyInfo;

public class PanicActivity extends AppCompatActivity {

    LinearLayout paniclayout;
    Button panicbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panic);

        panicbutton= (Button) findViewById(R.id.panicbtn);

        paniclayout = (LinearLayout) findViewById(R.id.panic_full_layout);
        final int from = ContextCompat.getColor(this, R.color.panicred);
        final int to   = ContextCompat.getColor(this, R.color.white);

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(from, to);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                paniclayout.setBackgroundColor((Integer)valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();



        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(2000);
        }


        panicbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkAvailable()) {
                    final ProgressDialog progressDialog = new ProgressDialog(PanicActivity.this);
                    progressDialog.setMessage("Loading please wait ...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    Thread splashThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                int waited = 0;
                                while (waited < 1000) {
                                    sleep(100);
                                    waited += 100;
                                    if (waited == 100) {
                                        String result=InsertNotification(Gridmenu.device_id);
                                        if(result.contains("Exception"))
                                        {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PanicActivity.this);
                                                    alertDialogBuilder.setIcon(R.drawable.error);
                                                    alertDialogBuilder.setTitle("Error ");
                                                    alertDialogBuilder.setMessage("Server not responding, try later")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Ok",
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {
                                                                            dialog.cancel();
                                                                        }
                                                                    });
                                                    AlertDialog alert = alertDialogBuilder.create();
                                                    alert.show();
                                                }
                                            });
                                        }
                                        else
                                        {
                                            runOnUiThread(new Runnable() {
                                                public void run() {
                                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PanicActivity.this);
                                                    alertDialogBuilder.setIcon(R.drawable.success);
                                                    alertDialogBuilder.setTitle("Success ");
                                                    alertDialogBuilder.setMessage("Reported successfully")
                                                            .setCancelable(false)
                                                            .setPositiveButton("Ok",
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {
                                                                            dialog.cancel();
                                                                            finish();
                                                                        }
                                                                    });
                                                    AlertDialog alert = alertDialogBuilder.create();
                                                    alert.show();
                                                }
                                            });
                                        }


                                    }
                                }
                            } catch (Exception e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Something went wrong, try again",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } finally {
                                progressDialog.dismiss();
                            }
                        }

                    };

                    splashThread.start();
                }

            }
        });

    }



    public String InsertNotification(String device_id) {

        String SOAP_ACTION = "http://tempuri.org/addTransporterNotifications";
        String OPERATION_NAME = "addTransporterNotifications";
        PropertyInfo[] piarray=new PropertyInfo[2];

        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;



        pi = new PropertyInfo();
        pi.setName("message");
        pi.setValue("Panic button pressed");
        pi.setType(String.class);
        piarray[1]=pi;

        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finish();
    }
}