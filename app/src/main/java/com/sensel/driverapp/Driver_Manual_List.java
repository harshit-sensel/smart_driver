package com.sensel.driverapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by User on 28-12-2016.
 */

public class Driver_Manual_List extends AppCompatActivity {
    String[] lists={"General Safety Rules","Before Your Journey","Seeing Clearly","Good Reaction","Good Health","Incident_Accident","Loading Procedure","On the Way Enroute to Customers","Unloading Procedure"};
    ArrayAdapter<String> adapter;
    public static String languge="English-English";
    public static String page="General Safety Rules/General Safety Rules.html";

    public static long start=0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivermanual_list);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        final ProgressDialog progressDialog = new ProgressDialog(Driver_Manual_List.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Please wait...");
        progressDialog.show();
        ActivityLog("Driver_Manual_Start");
        Thread languageThread = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    int waited = 0;
                    while (waited < 500) {
                        sleep(100);
                        waited += 100;
                        if(waited==100)
                        {
                            WebserviceCall webserviceCall=new WebserviceCall();
                            String result=webserviceCall.GetDriver_Language(Gridmenu.device_id);
                            if((!result.contains("Exception")) && result.length()>2)
                            {
                                languge=result;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    progressDialog.dismiss();
                }
            }

        };
        languageThread.start();

     ListView listView=(ListView) findViewById(R.id.list);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,lists);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                String selectedFromList = (arg0.getItemAtPosition(arg2)).toString();
                Toast.makeText(getApplicationContext(),selectedFromList,Toast.LENGTH_SHORT).show();
                Intent i = new  Intent(getApplicationContext(), Driver_Manual.class);
                //  i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 page=selectedFromList;
                //languge="English-Tamil";
                ActivityLog(selectedFromList);
              getApplicationContext().startActivity(i);
            }
            });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ActivityLog("Driver_Manual_End");
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        ActivityLog("Driver_Manual_End");
        super.onBackPressed();

    }
}
