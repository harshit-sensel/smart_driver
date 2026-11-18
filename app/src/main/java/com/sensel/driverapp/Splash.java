package com.sensel.driverapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.sensel.driverapp.db.SharedPreferencesUtility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Mallikarjun on 30-09-2015.
 */
public class Splash extends Activity {
    /**
     * Called when the activity is first created.
     */
    public static String isAuthorised = "false";
    public static String isBirthday = "false";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_LOCATION = 101;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 102;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 103;
    static long time_span = 0;
    Animation topAnim, bottomAnim;
    public static  String SESSIONID="Error";
    public static  String ACCOUNTID="Error";
    private SharedPreferencesUtility sharedPreferencesUtility;
    static String deviceid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        ImageView mainicon = (ImageView) findViewById(R.id.main_icon);



        sharedPreferencesUtility = new SharedPreferencesUtility(this);
        setInfo();


        if (!isNetworkAvailable()) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(Splash.this, android.R.style.Theme_Holo_Light_Dialog));
            alertDialogBuilder.setIcon(R.drawable.error);
            alertDialogBuilder.setTitle("No Network connection");
            alertDialogBuilder.setMessage("Check your internet connections")
                    .setCancelable(false)
                    .setPositiveButton("Ok", (dialog, id) -> {
                        dialog.cancel();
                        finish();
                        System.exit(0);
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
            return;
        }


        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyDriver", MODE_PRIVATE);
        String valid_device = pref.getString("device", null);
        String birthday = pref.getString("birthday", null);
        long lastverified = pref.getLong("lastverified", 0);




        if (ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Splash.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else if (ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Splash.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_LOCATION);
        }
        /*else if (ContextCompat.checkSelfPermission(Splash.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Splash.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
        }*/
        else {
            if (valid_device != null && birthday != null && lastverified > 0) {
                time_span = currentTimeMillis() / 1000 - lastverified;
                isAuthorised = "true";
                isBirthday = birthday;
            }
            deviceid = getIMEI();

            Log.e("IMEI",deviceid);
            Gridmenu.device_id = deviceid;



            Thread splashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        WebserviceCall webserviceCall = new WebserviceCall();
                        String appdata = webserviceCall.GetAppVersion(getApplicationContext().getPackageName());

//                        String sessionid =webserviceCall.Getsessionid(deviceid);
//
//                        Log.e("session",sessionid);
//
//                        try {
//
//                            JSONObject obj = new JSONObject(sessionid);
//                            if(obj.getString("session").contains("Error")|| obj.getString("account").contains("Error")){
//                                //sessionErroralert("Error in session or account ");
//                            }
//                            else {
//                                SESSIONID=obj.getString("session");
//                                ACCOUNTID=obj.getString("account");
//                            }
//                        }
//                        catch (Exception e){
//                            sessionErroralert(e.toString());
//                        }

                        if (appdata != null) {
                            try {
                                JSONArray array = new JSONArray(appdata);
                                JSONObject data = new JSONObject(array.get(0).toString());
                                if (data.getString("DomainUrl").contains("http")) {
                                    Config.DOMAINURL1 = data.getString("DomainUrl");
                                    if (data.getString("DomainUrl").contains("https://"))
                                        Config.DOMAINURL2 = data.getString("DomainUrl").replace("https://", "http://");
                                    else
                                        Config.DOMAINURL2 = data.getString("DomainUrl").replace("http://", "https://");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if (time_span == 0 || time_span > 3600) {
                            isAuthorised = webserviceCall.CheckIMEI(deviceid);
                            //String session=webserviceCall.Getsessionid(deviceid);
                            Log.e("session",isAuthorised);
                            if (isAuthorised.equals("true")) {
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("device", "true");
                                editor.putString("birthday", "false");
                                editor.putLong("lastverified", currentTimeMillis() / 1000);
                                isBirthday = webserviceCall.IsBirthday(deviceid);
                                if (isBirthday != null) {
                                    isBirthday = isBirthday.trim().toLowerCase();
                                }
                                if ("true".equalsIgnoreCase(isBirthday))
                                    editor.putString("birthday", "true");
                                editor.apply();
                                String driver_incentives = webserviceCall.GetDriver_Incentives(deviceid);
                                Gridmenu.points = driver_incentives.replace("\"", " ").split(",");
                                Transporter_Incentive.result = webserviceCall.GetTransporter_Incentives(deviceid);
                                Best_Driver.result = webserviceCall.BestDriver_Incentives(deviceid);
                            }
                        } else {
                            String driver_incentives = webserviceCall.GetDriver_Incentives(deviceid);
                            Gridmenu.points = driver_incentives.replace("\"", " ").split(",");
                            Transporter_Incentive.result = webserviceCall.GetTransporter_Incentives(deviceid);
                            Best_Driver.result = webserviceCall.BestDriver_Incentives(deviceid);
                        }
                    } catch (Exception e) {
                        // do nothing
                    } finally {
                        if (!isAuthorised.equals("true")) {
                            Intent i = new Intent(Splash.this, Error.class);
                            Error.error_text = "UnAuthorised device, Contact TOTAL ENERGIES to register your IMEI "+"("+deviceid+")";
                            if (isAuthorised.contains("Exception"))
                                Error.error_text = "Server not responding,Please try again later";
                            startActivity(i);
                            finish();
                        }

//                        else if(SESSIONID.contains("Error")||ACCOUNTID.contains("Error")){
//                            sessionErroralert("Error in session or account ");
//                        }
                        else {
                            WebserviceCall webserviceCall = new WebserviceCall();
                            String sessionid = webserviceCall.Getsessionid(deviceid);

                            Log.e("session", sessionid);

                            try {
                                JSONObject obj = new JSONObject(sessionid);
                                if (obj.getString("session").contains("Error") || obj.getString("account").contains("Error")) {
                                    sessionErroralert("Error in session or account ");
                                } else {
                                    SESSIONID = obj.getString("session");
                                    ACCOUNTID = obj.getString("account");
                                    setupPreferenceValues();

                                    Intent i = new Intent(Splash.this, Gridmenu.class);
                                    if ("true".equalsIgnoreCase(isBirthday)) {
                                        i = new Intent(Splash.this, Map.class);
                                    }
                                    startActivity(i);
                                    finish();
                                }
                            } catch (Exception e) {
                                sessionErroralert(e.toString());
                            }
                        }
                    }
                }

            };
            splashThread.start();
        }
    }

    private void sessionErroralert(String errormessage) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog ad = new AlertDialog.Builder(Splash.this).create();
                ad.setTitle("Error");
                ad.setCancelable(false);
                ad.setMessage(deviceid+"\n"+errormessage);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        System.exit(0);
                    }
                });
                ad.show();
            }
        });



    }

    //TODO update data here
    private void setupPreferenceValues() {
        //"c52d1b490c9ac9432532c4ffe7364155b171387c"
        //sharedPreferencesUtility.saveBooleanPreferences(SharedPreferencesUtility.LOGGED_IN, true);
        sharedPreferencesUtility.saveStringPreferences(SharedPreferencesUtility.SESSION_ID, SESSIONID);
        sharedPreferencesUtility.saveStringPreferences(SharedPreferencesUtility.BASE_URL, "https://fleetsmart3.ui.sensel.in/");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Splash.this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(Splash.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_READ_LOCATION);


                    } else
                        main();

                } else {
                    final AlertDialog ad = new AlertDialog.Builder(Splash.this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("PHONE STATE permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
                    ad.show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    main();

                } else {
                    final AlertDialog ad = new AlertDialog.Builder(Splash.this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Location permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
                    ad.show();
                }
                return;
            }
            /*case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    main();
                } else {
                    final AlertDialog ad = new AlertDialog.Builder(Splash.this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Storage permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                            System.exit(0);
                        }
                    });
                    ad.show();
                }
                return;
            }*/
            // other 'case' lines to check for other
            // permissions this app might request
        }



    }

    private void blink() {
        TextView myText = (TextView) findViewById(R.id.logo);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.anim_bottom_to_top);
        myText.setAnimation(topAnim);
    }

    /******* For getting IMEI number of device *******/
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
            @SuppressLint("HardwareIds")
            String android_id= Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id =android_id;
            }
            return device_id;
        }
    }

    //Check internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void getdata(final String device_id) {
        if (device_id == null) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                Toast.makeText(getApplicationContext(), "Give READ_PHONE_STATE Permission", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < 400) {
                        sleep(100);
                        waited += 100;
                        if (waited == 100) {
                            WebserviceCall webserviceCall = new WebserviceCall();
                            String driver_incentives = webserviceCall.GetDriver_Incentives(device_id);
                            Gridmenu.points = driver_incentives.replace("\"", " ").split(",");
                            Transporter_Incentive.result = webserviceCall.GetTransporter_Incentives(device_id);
                            Best_Driver.result = webserviceCall.BestDriver_Incentives(device_id);
                            waited = 600;
                        }

                    }
                } catch (Exception e) {
                } finally {
                    if (Gridmenu.points == null) {
                        Intent i = new Intent(Splash.this, Error.class);
                        Error.error_text = "Server not responding, try again later";
                        startActivity(i);
                        finish();
                    } else if (Gridmenu.points[0].contains("Exception")) {

                        Intent i = new Intent(Splash.this, Error.class);
                        Error.error_text = "Server time out error, try again later";
                        startActivity(i);
                        finish();
                    } else if (!isAuthorised.equals("true")) {
                        Intent i = new Intent(Splash.this, Error.class);
                        Error.error_text = "UnAuthorised device, please register your IMEI ("+device_id+")";
                        if (isAuthorised.contains("Exception"))
                            Error.error_text = "Server not responding, try again later";
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(Splash.this, Gridmenu.class);
                        if ("true".equalsIgnoreCase(isBirthday))
                            i = new Intent(Splash.this, Map.class);
                        startActivity(i);
                        finish();
                    }
                }
            }

        };
        splashThread.start();
    }

    private void main() {
        final SharedPreferences pref = getApplicationContext().getSharedPreferences("MyDriver", MODE_PRIVATE);
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < 500) {
                        sleep(100);
                        waited += 100;
                        if (waited == 100) {
                            String deviceid = getIMEI();
                            Gridmenu.device_id = deviceid;
                            WebserviceCall webserviceCall = new WebserviceCall();
                            isAuthorised = webserviceCall.CheckIMEI(deviceid);
                            if (isAuthorised.equals("true")) {
                                SharedPreferences.Editor editor = pref.edit();
                                editor.putString("device", "true");
                                editor.putString("birthday", "false");
                                editor.putLong("lastverified", currentTimeMillis() / 1000);
                                isBirthday = webserviceCall.IsBirthday(deviceid);
                                if (isBirthday != null) {
                                    isBirthday = isBirthday.trim().toLowerCase();
                                }
                                if ("true".equalsIgnoreCase(isBirthday))
                                    editor.putString("birthday", "true");
                                editor.apply();
                                //show alert based on priority
                                String driver_incentives = webserviceCall.GetDriver_Incentives(deviceid);
                                Gridmenu.points = driver_incentives.replace("\"", " ").split(",");
                                Transporter_Incentive.result = webserviceCall.GetTransporter_Incentives(deviceid);
                                Best_Driver.result = webserviceCall.BestDriver_Incentives(deviceid);
                            }
                            // getdata(deviceid);
                        }

                    }
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    if (!isAuthorised.equals("true")) {
                        Intent i = new Intent(Splash.this, Error.class);
                        Error.error_text = "UnAuthorised device, please register your IMEI ("+Gridmenu.device_id+")";
                        if (isAuthorised.contains("Exception"))
                            Error.error_text = "Server not responding, try again later";
                        startActivity(i);
                        finish();
                    } else {
                        WebserviceCall webserviceCall = new WebserviceCall();
                        String sessionid =webserviceCall.Getsessionid(deviceid);

                        Log.e("session1",sessionid);

                        try {

                            JSONObject obj = new JSONObject(sessionid);
                            if(obj.getString("session").contains("Error")|| obj.getString("account").contains("Error")){
                                sessionErroralert("Error in session or account ");
                            }
                            else {
                                SESSIONID=obj.getString("session");
                                ACCOUNTID=obj.getString("account");
                                Intent i = new Intent(Splash.this, Gridmenu.class);
                                if ("true".equalsIgnoreCase(isBirthday))
                                    i = new Intent(Splash.this, Map.class);
                                startActivity(i);
                                finish();
                            }
                        }
                        catch (Exception e){
                            sessionErroralert(e.toString());
                        }

                    }
                }
            }

        };
        splashThread.start();
    }

    private void setInfo() {
        try {
            TextView textViewVersion = findViewById(R.id.textViewVersion);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textViewVersion.setText("Version Name : " + pInfo.versionName + " | Version Code : " + pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}