package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Created by User on 24-11-2016.
 */

public class Driver_Feedback extends AppCompatActivity {

    int Cooperative=0;
    int Attitude=0;
    int Cleanness=0;
    int Wearing_PPE=0;
    int Unloading_procedure=0;
    String result;

    int r1=0;
    int r2=0;
    int r3=0;
    int r4=0;
    int r5=0;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivers_feedback);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Checked change Listener for RadioGroup 1
        RadioGroup cooperative = (RadioGroup) findViewById(R.id.radioGroup1);
        RadioGroup attitude=(RadioGroup) findViewById(R.id.radioGroup2);
        RadioGroup cleanness=(RadioGroup) findViewById(R.id.radioGroup3);
        RadioGroup PPE=(RadioGroup) findViewById(R.id.radioGroup4);
        RadioGroup unloading_procedure=(RadioGroup) findViewById(R.id.radioGroup5);


        cooperative.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {

                if(r1==0)
                    r1=checkedId;
                else
                {
                    RadioButton pbtn=(RadioButton) findViewById(r1);
                    pbtn.setAlpha(0.4f);
                    r1=checkedId;
                }
                RadioButton button=(RadioButton) findViewById(checkedId);

                switch (checkedId)
                {
                    case R.id.radio1:
                        button.setAlpha(1);
                        Cooperative=1;
                        break;
                    case R.id.radio2:
                        button.setAlpha(1);
                        Cooperative=2;
                        break;
                    case R.id.radio3:
                        button.setAlpha(1);
                        Cooperative=3;
                        break;
                    case R.id.radio4:
                        button.setAlpha(1);
                        Cooperative=4;
                        break;
                    default:
                        break;
                }
            }
        });

        attitude.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {

                if(r2==0)
                    r2=checkedId;
                else
                {
                    RadioButton pbtn=(RadioButton) findViewById(r2);
                    pbtn.setAlpha(0.4f);
                    r2=checkedId;
                }
                RadioButton button=(RadioButton) findViewById(checkedId);
                switch (checkedId)
                {
                    case R.id.radio5:
                        button.setAlpha(1);
                        Attitude=1;
                        break;
                    case R.id.radio6:
                        button.setAlpha(1);
                        Attitude=2;
                        break;
                    case R.id.radio7:
                        button.setAlpha(1);
                        Attitude=3;
                        break;
                    case R.id.radio8:
                        button.setAlpha(1);
                        Attitude=4;
                        break;
                    default:
                        break;
                }
            }
        });

        cleanness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(r3==0)
                    r3=checkedId;
                else
                {
                    RadioButton pbtn=(RadioButton) findViewById(r3);
                    pbtn.setAlpha(0.4f);
                    r3=checkedId;
                }
                RadioButton button=(RadioButton) findViewById(checkedId);

                switch (checkedId)
                {
                    case R.id.radio9:
                        button.setAlpha(1);
                        Cleanness=1;
                        break;
                    case R.id.radio10:
                        button.setAlpha(1);
                        Cleanness=2;
                        break;
                    case R.id.radio11:
                        button.setAlpha(1);
                        Cleanness=3;
                        break;
                    case R.id.radio12:
                        button.setAlpha(1);
                        Cleanness=4;
                        break;
                    default:
                        break;
                }
            }
        });

        PPE.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(r4==0)
                    r4=checkedId;
                else
                {
                    RadioButton pbtn=(RadioButton) findViewById(r4);
                    pbtn.setAlpha(0.4f);
                    r4=checkedId;
                }
                RadioButton button=(RadioButton) findViewById(checkedId);
                switch (checkedId)
                {
                    case R.id.radio13:
                        button.setAlpha(1);
                        Wearing_PPE=1;
                        break;
                    case R.id.radio14:
                        button.setAlpha(1);
                        Wearing_PPE=2;
                        break;
                    case R.id.radio15:
                        button.setAlpha(1);
                        Wearing_PPE=3;
                        break;
                    case R.id.radio16:
                        button.setAlpha(1);
                        Wearing_PPE=4;
                        break;
                    default:
                        break;
                }
            }
        });

        unloading_procedure.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(r5==0)
                    r5=checkedId;
                else
                {
                    RadioButton pbtn=(RadioButton) findViewById(r5);
                    pbtn.setAlpha(0.4f);
                    r5=checkedId;
                }
                RadioButton button=(RadioButton) findViewById(checkedId);
                switch (checkedId)
                {
                    case R.id.radio17:
                        button.setAlpha(1);
                        Unloading_procedure=1;
                        break;
                    case R.id.radio18:
                        button.setAlpha(1);
                        Unloading_procedure=2;
                        break;
                    case R.id.radio19:
                        button.setAlpha(1);
                        Unloading_procedure=3;
                        break;
                    case R.id.radio20:
                        button.setAlpha(1);
                        Unloading_procedure=4;
                        break;
                    default:
                        break;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver__incentives, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public  void submitfeedback(View view) {
        final EditText customercode = (EditText) findViewById(R.id.customercode);
        final EditText comment = (EditText) findViewById(R.id.comment);
        final String customer = customercode.getText().toString();
        if (customer.length() < 1) {
            Toast.makeText(getApplicationContext(), "Enter customer code", Toast.LENGTH_LONG).show();
            return;
        } else if (Attitude == 0 || Cleanness == 0 || Cooperative == 0 || Wearing_PPE == 0 || Unloading_procedure == 0) {
            Toast.makeText(getApplicationContext(), "Give feedback for all sections", Toast.LENGTH_LONG).show();
            return;
        }
        String device_id1="";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            device_id1 = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id1 = android_id;
            }
        }
        final String device_id = device_id1;
        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        final String latlng = gpsTracker.getLocation();

        final ProgressDialog progressDialog = new ProgressDialog(Driver_Feedback.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Thread back_Thread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < 1000) {
                        sleep(100);
                        waited += 100;
                        if (waited == 100) {
                            progressDialog.show();
                            WebserviceCall ws = new WebserviceCall();
                            result = ws.InsertDriverFeedback(device_id, customer, latlng, String.valueOf(Cooperative), String.valueOf(Attitude), String.valueOf(Cleanness), String.valueOf(Wearing_PPE), String.valueOf(Unloading_procedure), comment.getText().toString());
                        }

                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    progressDialog.dismiss();
                    if(result.equals("1")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                comment.setText("");
                                customercode.setText("");
                                final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                        new ContextThemeWrapper(Driver_Feedback.this, android.R.style.Theme_Holo_Light_Dialog));
                                alertDialogBuilder.setIcon(R.drawable.success);
                                alertDialogBuilder.setTitle("Thank You ");
                                alertDialogBuilder.setMessage("Feedback Submitted.")
                                        .setCancelable(false)
                                        .setPositiveButton("Ok",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        finish();
                                                        dialog.cancel();
                                                    }
                                                });
                                AlertDialog alert = alertDialogBuilder.create();
                                alert.show();
                            }
                        });
                    }
                    else if(result.equals("Customer code is wrong"))
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                        new ContextThemeWrapper(Driver_Feedback.this, android.R.style.Theme_Holo_Light_Dialog));
                                alertDialogBuilder.setIcon(R.drawable.error);
                                alertDialogBuilder.setTitle("Wrong Input");
                                alertDialogBuilder.setMessage("Enter current trip customer code")
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
                    else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final AlertDialog.Builder alertDialogBuilder =  new AlertDialog.Builder(
                                        new ContextThemeWrapper(Driver_Feedback.this, android.R.style.Theme_Holo_Light_Dialog));
                                alertDialogBuilder.setIcon(R.drawable.error);
                                alertDialogBuilder.setTitle("Not in trip");
                                alertDialogBuilder.setMessage("During trip only, you can give feedback")
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
                }
            }

        };
        if(isNetworkAvailable())
        back_Thread.start();
        else
            Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_SHORT).show();
    }

    //Check internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
