package com.sensel.driverapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.ksoap2.serialization.PropertyInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.res.Configuration;

/**
 * Created by User on 09-01-2017.
 */

public class DriverSelfAppraisal extends AppCompatActivity {
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    int checklist=0;int rest=0;int overspeed=0,jrmoverspeed=0,nightdriving=0,contdriving=0,maxdriving=0,authorizedroute=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ✅ Apply language before loading layout
        String lang = getSharedPreferences("lang_setting", MODE_PRIVATE).getString("my_lang", "en");
        String langCode = getLangCodeFromServerValue(lang);
        setLocate(langCode); // This must be called BEFORE setContentView()
        setContentView(R.layout.activity_driver_appraisal);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
    public String getLangCodeFromServerValue(String serverValue) {
        if (serverValue == null) return "en";
        switch (serverValue.toLowerCase().trim()) {
            case "english":
            case "english-english":
            case "en":
                return "en";
            case "english-hindi":
            case "hindi":
            case "hi":
                return "hi";
            case "english-tamil":
            case "tamil":
            case "ta":
                return "ta";
            case "english-malayalam":
            case "malayalam":
            case "ml":
                return "ml";
            case "engilsh-kannada":
            case "english-kannada":
            case "kannada":
            case "kn":
                return "kn";
            case "english-telugu":
            case "telugu":
            case "te":
                return "te";
            default:
                return "en"; // fallback
        }
    }

    public void setLocate(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_add:
                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rb1);
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                View radioButton = radioGroup.findViewById(radioButtonID);
                int idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    checklist = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb2);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    rest = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb3);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    overspeed = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb4);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    jrmoverspeed = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb5);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    nightdriving = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb6);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    contdriving = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb7);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    maxdriving = 1;

                radioGroup = (RadioGroup) findViewById(R.id.rb8);
                radioButtonID = radioGroup.getCheckedRadioButtonId();
                if (radioButtonID == -1) {
                    alert("Before submit you need to check all points");
                    break;
                }
                radioButton = radioGroup.findViewById(radioButtonID);
                idx = radioGroup.indexOfChild(radioButton);
                if (idx == 0)
                    authorizedroute = 1;

                if (isNetworkAvailable()) {
                    final ProgressDialog progressDialog = new ProgressDialog(DriverSelfAppraisal.this);
                    progressDialog.setMessage("Loading please wait ...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    Thread backThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                String result = InsertAppraisal(Gridmenu.device_id, checklist, rest, overspeed,jrmoverspeed,nightdriving,contdriving,maxdriving,authorizedroute);
                                if (result.contains("Exception")) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                    new ContextThemeWrapper(DriverSelfAppraisal.this, android.R.style.Theme_Holo_Light_Dialog));
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
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            String currentDateTime = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                                    .format(new Date());
                                            String message = "Reported successfully\nTime: " + currentDateTime;
                                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                    new ContextThemeWrapper(DriverSelfAppraisal.this, android.R.style.Theme_Holo_Light_Dialog));
                                            alertDialogBuilder.setIcon(R.drawable.success);
                                            alertDialogBuilder.setTitle("Success ");
                                            //alertDialogBuilder.setMessage("Reported successfully")
                                            alertDialogBuilder.setMessage(message)
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                    Intent intent = new Intent(getApplicationContext(), Map.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    Map.Place = "Violations";
                                                                    Map.title = "Your Violations";
                                                                    // ActivityLog("non dc route");
                                                                    getApplicationContext().startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                            AlertDialog alert = alertDialogBuilder.create();
                                            alert.show();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                            } finally {
                                progressDialog.dismiss();
                            }
                        }
                    };
                    backThread.start();
                } else {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);

    };

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public String InsertAppraisal(String device_id,int checklist,int rest,int overspeed,int jrmoverspeed,int nightdriving,int contdriving,int maxdriving,int authorizedroute) {

        String SOAP_ACTION = "http://tempuri.org/InsertDriverSelfAppraisalNew1";
        String OPERATION_NAME = "InsertDriverSelfAppraisalNew1";

        PropertyInfo[] propertyInfos=new PropertyInfo[9];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        propertyInfos[0]=pi;

        pi = new PropertyInfo();
        pi.setName("checklist");
        pi.setValue(checklist);
        pi.setType(Integer.class);
        propertyInfos[1]=pi;

        pi = new PropertyInfo();
        pi.setName("restTaken");
        pi.setValue(rest);
        pi.setType(Integer.class);
        propertyInfos[2]=pi;

        pi = new PropertyInfo();
        pi.setName("overspeed");
        pi.setValue(overspeed);
        pi.setType(Integer.class);
        propertyInfos[3]=pi;

        pi = new PropertyInfo();
        pi.setName("jrmOverspeed");
        pi.setValue(jrmoverspeed);
        pi.setType(Integer.class);
        propertyInfos[4]=pi;

        pi = new PropertyInfo();
        pi.setName("nightDriving");
        pi.setValue(nightdriving);
        pi.setType(Integer.class);
        propertyInfos[5]=pi;

        pi = new PropertyInfo();
        pi.setName("continuesDriving");
        pi.setValue(contdriving);
        pi.setType(Integer.class);
        propertyInfos[6]=pi;


        pi = new PropertyInfo();
        pi.setName("maxDriving");
        pi.setValue(maxdriving);
        pi.setType(Integer.class);
        propertyInfos[7]=pi;

        pi = new PropertyInfo();
        pi.setName("AuthorizedRoute");
        pi.setValue(authorizedroute);
        pi.setType(Integer.class);
        propertyInfos[8]=pi;

        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,propertyInfos,SOAP_ACTION);
        //return  webserviceCall.callWebservice(Config.DOMAINURL4,Config.DOMAINURL2,OPERATION_NAME,propertyInfos,SOAP_ACTION);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void  alert(String message)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(DriverSelfAppraisal.this, android.R.style.Theme_Holo_Light_Dialog));
        alertDialogBuilder.setIcon(android.R.drawable.ic_menu_info_details);
        alertDialogBuilder.setTitle("Self Appraisal ");
        alertDialogBuilder.setMessage(message)
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
 }
