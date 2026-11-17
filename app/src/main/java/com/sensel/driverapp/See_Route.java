package com.sensel.driverapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by User on 25-01-2017.
 */

public class See_Route extends AppCompatActivity {

    static String values="";
    public  static String device_id;
    ArrayAdapter<String> adapter;
    AutoCompleteTextView source,destination;
    ProgressDialog progressDialog;
    static ArrayList<String> sources=new ArrayList<String>();
    static ArrayList<String> destinations=new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jrm_route_input);
        if(device_id==null) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                device_id = tm.getDeviceId();
            } else {
                String android_id = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id != null) {
                    device_id = android_id;
                }
            }
        }
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        destination = (AutoCompleteTextView) findViewById(R.id.destination);
        source = (AutoCompleteTextView) findViewById(R.id.source);
        if(destinations.size()==0 || sources.size()==0) {
            progressDialog = new ProgressDialog(See_Route.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Please wait...");
            progressDialog.show();
            GetSourceDestinations(device_id);

        }
        else
        {
            adapter = new ArrayAdapter<String>(See_Route.this,
                    R.layout.mylist, sources);
            source.setAdapter(adapter);

            adapter = new ArrayAdapter<String>(See_Route.this,
                    R.layout.mylist, destinations);
            destination.setAdapter(adapter);
        }
        destination.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                destination.showDropDown();//Show full list of dest
            }
        });
        source.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                source.showDropDown();//Show full list of sources
            }
        });
        Button button=(Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //call map with source destination
                if (isNetworkAvailable()) {
                    if(isLocationAvailable()) {
                        if(source.getText().length()==0)
                        {
                            Toast.makeText(getApplicationContext(),"Please select source",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(sources.indexOf(source.getText().toString()) < 0)
                        {
                            Toast.makeText(getApplicationContext(),"Please select valid source",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(destination.getText().length()==0)
                        {
                            Toast.makeText(getApplicationContext(),"Please select destination",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(destinations.indexOf(destination.getText().toString()) < 0)
                        {
                            Toast.makeText(getApplicationContext(),"Please select valid destination",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            Map.Place = "NonDCRoute";
                            Map.title = "JRM Route";
                            Map.route = source.getText().toString()+"," + destination.getText().toString();

                            Intent intent = new Intent(getApplicationContext(), Map.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            // ActivityLog("non dc route");
                            getApplicationContext().startActivity(intent);
                        }
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Enable GPS",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
            }
        });
    }

   /* @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    void GetSourceDestinations(final String device_id)
    {
        Thread background=new Thread() {
            @Override
            public void run() {
                WebserviceCall webserviceCall=new WebserviceCall();
                String result=webserviceCall.GetDestinations(device_id);
                try {
                    final JSONArray jArr = new JSONArray(result);
                    for (int j = 0; j < jArr.length(); j++) {
                        try {
                            JSONObject data = new JSONObject(jArr.get(j).toString());
                            if(!data.isNull("Source"))
                            {
                                sources.add(data.getString("Source"));
                            }
                            else
                            {
                                destinations.add(data.getString("Destination"));
                            }
                        } catch (JSONException js) {

                        }
                    }

                } catch (JSONException e) {
                }
                finally {
                    progressDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //set sdapter
                            if(sources.size()>0) {
                                adapter = new ArrayAdapter<String>(See_Route.this,
                                        R.layout.mylist, sources);
                                source.setAdapter(adapter);
                            }
                            if(destinations.size()>0)
                            {
                                adapter = new ArrayAdapter<String>(See_Route.this,
                                        R.layout.mylist, destinations);
                                destination.setAdapter(adapter);
                            }
                        }
                    });
                }
            }
        };
        background.start();
    }

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isLocationAvailable() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return  true;
        } else {
            return false;
        }
    }

    private void ActivityLog(final String activity)
    {
        GPSTracker gpsTracker=new GPSTracker(getApplicationContext());
        final String[] latlng= gpsTracker.getLocation().split(",");
        Thread activityThread=new Thread() {
            @Override
            public void run() {
                try {
                    WebserviceCall webserviceCall=new WebserviceCall();
                    webserviceCall.KeepActivityLog(Gridmenu.device_id,activity,latlng[0],latlng[1]);
                }
                catch(Exception e)
                {
                }
            }
        };
        activityThread.start();
    }
}
