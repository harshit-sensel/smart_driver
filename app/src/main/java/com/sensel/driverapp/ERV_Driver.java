package com.sensel.driverapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Created by User on 06-01-2017.
 */

public class ERV_Driver extends AppCompatActivity{
    ArrayAdapter<String> adapter;
    public static boolean mSearchCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivermanual_list);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        final ProgressDialog progressDialog = new ProgressDialog(ERV_Driver.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Please wait...");
        progressDialog.show();

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
                           final String result=webserviceCall.GetTotalVehicles();
                            if((!result.contains("Exception")) && result.length()>2)
                            {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String[] lists=result.split("#");
                                        ListView listView=(ListView) findViewById(R.id.list);
                                        adapter=new ArrayAdapter<String>(ERV_Driver.this,android.R.layout.simple_list_item_1,lists);
                                        listView.setAdapter(adapter);

                                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                                String selectedFromList = (arg0.getItemAtPosition(arg2)).toString();
                                                Map.vehicle=selectedFromList;
                                                Map.Place="ERV";
                                                Map.title="Vehicle Position";
                                                if(isNetworkAvailable()) {
                                                    if (isLocationAvailable()) {
                                                        Intent intent = new Intent(getApplicationContext(), Map.class);
                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        getApplicationContext().startActivity(intent);
                                                    }
                                                    else
                                                    Toast.makeText(getApplicationContext(), "Enable GPS Location", Toast.LENGTH_SHORT).show();
                                                }
                                                else
                                                Toast.makeText(getApplicationContext(), "No internet", Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                    }
                                });

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


    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        final MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(this.getString(R.string.search));

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHintTextColor(getResources().getColor(R.color.white));
        }
        searchView.setOnQueryTextListener(onQuerySearchView);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_search:
                mSearchCheck = true;
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private SearchView.OnQueryTextListener onQuerySearchView = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            try {
                ERV_Driver.this.adapter.getFilter().filter(s);
            }
            catch (Exception e){}
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            try {
                if (mSearchCheck) {
                    ERV_Driver.this.adapter.getFilter().filter(s);
                }
            }
            catch (Exception e){}
            return false;
        }
    };
    public boolean isLocationAvailable() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return  true;
        } else {
            return false;
        }
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
}
