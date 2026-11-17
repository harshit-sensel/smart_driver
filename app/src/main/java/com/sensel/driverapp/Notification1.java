package com.sensel.driverapp;

/**
 * Created by User on 29-04-2016.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Mallikarjun on 21-01-2016.
 */

public class Notification1 extends AppCompatActivity {

    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    Context cont;
    public String result;
    public static String[] titles;
    private static String[] info;
    private static String[] date;
    List<RowItem> rowItems;
    TextView textView;
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification1);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        cont=this.getApplicationContext();
        rowItems = new ArrayList<RowItem>();
         textView = (TextView) findViewById(R.id.text);
         textView.setVisibility(View.GONE);
        final ProgressDialog progressDialog = new ProgressDialog(Notification1.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading ...  ");
        progressDialog.show();

        String device_id1="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            device_id1 = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id1 = android_id;
            }
        }
        final String device_id =device_id1;
        if(isNetworkAvailable()) {
                Thread splashThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int waited = 0;
                            while (waited < 1000) {
                                sleep(100);
                                waited += 100;
                                if (waited == 100) {
                                    result = CheckNotifications(device_id);
                                    createnotification();
                                    UpdateNotifiedDate(device_id);
                                }
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"OOPS Unable to contact server",Toast.LENGTH_SHORT).show();
                                }
                            });
                        } finally {
                            progressDialog.dismiss();
                        }
                    }

                };

                splashThread.start();
        }
        else {
            Toast.makeText(cont,"No internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //Create notification panel
    public  void createnotification()
    {
        try {
            final  JSONArray jArr = new JSONArray(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int j = 0; j <= jArr.length(); j++) {
                            try {
                                JSONObject data = jArr.getJSONObject(j);
                                String inputPattern = "MM/dd/yyyy h:mm:ss a";
                                String outputPattern = "dd-MMM-yyyy h:mm a";
                                SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
                                SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

                                Date date = null;
                                String str = null;

                                try {
                                    date = inputFormat.parse(data.getString("Date"));
                                    str = outputFormat.format(date);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                String subject = data.getString("Subject");
                                int id=R.drawable.bell;


                                RowItem item = new RowItem(id,subject, data.getString("Info"), str);
                                rowItems.add(item);
                                count++;

                            } catch (JSONException js) {
                                // Toast.makeText(cont,"Oops something went wrong, Try again",Toast.LENGTH_SHORT).show();
                            }
                        }
                        ListView listView = (ListView) findViewById(R.id.list);
                        NotificationAdapter adapter = new NotificationAdapter(Notification1.this,
                                R.layout.notificationlist, rowItems);
                        listView.setAdapter(adapter);
                    }

                });
        }
        catch (JSONException e){
           // Toast.makeText(cont,"Oops something went wrong, Try again",Toast.LENGTH_SHORT).show();
        }
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (count < 1)
                    textView.setVisibility(View.VISIBLE);
            }
        });
    }
    public String CheckNotifications(String device_id) {

        String SOAP_ACTION = "http://tempuri.org/CheckNotification";
        String OPERATION_NAME = "CheckNotification";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }

    public void UpdateNotifiedDate(String device_id) {

        String SOAP_ACTION = "http://tempuri.org/UpdateNotifiedDate";
        String OPERATION_NAME = "UpdateNotifiedDate";
        PropertyInfo[] piarray=new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0]=pi;
        WebserviceCall webserviceCall=new WebserviceCall();
        webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}