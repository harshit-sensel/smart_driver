package com.sensel.driverapp;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_MUTABLE;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sensel.driverapp.utility.CheckNetwork;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.sensel.driverapp.utility.ApplicationConstants.BROADCAST_UPDATE_APP;

/**
 * Created by User on 28-12-2016.
 */

public class Gridmenu extends AppCompatActivity {
    private static final String TAG = Gridmenu.class.getName();
    private static final int RC_PLAY_SERVICES = 123;

    public static String LANGUGE;

    ExpandableHeightGridView gridView;
    LinearLayout linearLayout;
    //important global variables
    public static String device_id;
    public static String vehicleid;
    static boolean is_ERV = false;
    public static String[] points;
    public static String lang;

    public static Context context;
    public static String LATLONG;


    public static String[] gridViewStrings;

    public static Integer[] gridViewImages;
    ProgressDialog progressDialog;
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LANGUGE = loadLocate();

        GPSTracker gpsTracker = new GPSTracker(getApplicationContext());
        gpsTracker.getLocation();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.e("lat", location.getProvider());
                            LATLONG = String.valueOf(location.getLatitude() + "," + String.valueOf(location.getLongitude()));
                        }
                    }
                });
        context = getApplicationContext();


        gridViewStrings = new String[]{
                "Daily Checklist",
                "JRM Route",
                "Vehicle Runtime",
                "Unload Quantity",
                "Incentive Points",
                "Driver Manual",
                "Customer Feedback",
                "See JRM Route",
                "Notifications",
                "Send Notification",
                "Deviation Report",
                "Self Appraisal",
                "Emergency Vehicle",
                "Hospital",
                "Ambulance",
                "Fire Brigade",
                "Puncture Shop",
                "Motor Garage",
                "Police",
                "Cranes",
                "Dashboard",
                //"ZERO Fatality",
                "Start & End Work",
                "Rest Timer",
                "Panic button",
                "Journey Plan",
        };


        gridViewImages = new Integer[]{
                R.drawable.checklist,
                R.drawable.route,
                R.drawable.runtime,
                R.drawable.unload,
                R.drawable.incentive,
                R.drawable.manual,
                R.drawable.feedback,
                R.drawable.see_route,
                R.drawable.notification,
                R.drawable.send_notification,
                R.drawable.deviation,
                R.drawable.appraisal,
                R.drawable.emergency,
                R.drawable.hospital,
                R.drawable.ambulance,
                R.drawable.fire,
                R.drawable.puncture,
                R.drawable.garage,
                R.drawable.police,
                R.drawable.crane,
                R.drawable.dashboard,
                //R.drawable.checklist_icon,
                R.drawable.worktime,
                R.drawable.sleeptime,
                R.drawable.ic_error,
                R.drawable.worktime,
        };


        Log.e(" LANGUGE", LANGUGE);
        // LANGUGE="en";
        setContentView(R.layout.gridmenu);
        ActionBar actionBar = getSupportActionBar();

        lang = whichlanguge(LANGUGE);
        setLocate(LANGUGE);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        getSupportActionBar().setIcon(R.mipmap.icon);
        getSupportActionBar().setSubtitle(lang);


        progressDialog = new ProgressDialog(Gridmenu.this);
        gridView = (ExpandableHeightGridView) findViewById(R.id.grid);
        linearLayout = (LinearLayout) findViewById(R.id.linear);
        gridView.setExpanded(true);

        if (device_id == null) {
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                device_id = tm.getDeviceId();
            } else {
                String android_id = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id != null) {
                    device_id = android_id;
                }
            }
        }

        Log.e("device", device_id);
        //check locate vehicle option for emergency vehicle drivers
        if (points != null) {
            if (points.length == 8) {

                progressDialog.setCancelable(false);
                progressDialog.setMessage("Loading Please wait...");
                progressDialog.show();
                vehicleid = points[7];
                isERV();
            } else
                gridView.setAdapter(new CustomAndroidGridViewAdapter(Gridmenu.this, getApplicationContext(), gridViewStrings, gridViewImages));
        } else
            gridView.setAdapter(new CustomAndroidGridViewAdapter(Gridmenu.this, getApplicationContext(), gridViewStrings, gridViewImages));

        //Start Notification service


        Calendar cal = Calendar.getInstance();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 60000, pendingIntent);

        GPSTracker.mActivity = Gridmenu.this;
        AlarmManager service = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent intent1 = new Intent(this, RestarterBroadcastReceiver.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);
        service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10000, pendingIntent1);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, SaveMyAppsService.class));
            } else {
                startService(new Intent(this, SaveMyAppsService.class));
            }
        } catch (Exception e) {
            Log.e("Gridmenu", "Error starting service: " + e.getMessage());
        }


        //Lock mobile while driving service

        /*Thread t = new Thread() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public void run() {
                UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
                long time = System.currentTimeMillis();
                List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                        time - 1000 * 5, time);
                if (appList == null || appList.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    new ContextThemeWrapper(Gridmenu.this, android.R.style.Theme_Holo_Light_Dialog));
                            alertDialogBuilder.setTitle("Usage Stats Permission Need");
                            alertDialogBuilder.setMessage("Allow SmartDriver to access usage stats?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                                }
                                            });
                            alertDialogBuilder.setNegativeButton("No",
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
        };
        t.start();*/


        new Thread(new Runnable() {
            @Override
            public void run() {
                WebserviceCall webserviceCall = new WebserviceCall();
                String appdata = webserviceCall.GetAppVersion(getApplicationContext().getPackageName());
                if (appdata != null) {
                    try {
                        JSONArray array = new JSONArray(appdata);
                        JSONObject data = new JSONObject(array.get(0).toString());
                        String _version = data.getString("VersionCode");
                        if (Integer.parseInt(_version) > BuildConfig.VERSION_CODE)
                            showUpdateAlert(Integer.parseInt(data.getString("Priority")), Integer.parseInt(data.getString("StableVersion")));
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
            }
        }).start();

        checkPlayServicesAvailable();
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_langage, menu);

        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.language:
//                showlanguagechange();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void showlanguagechange() {
        LanguageSet languageSet = new LanguageSet();
        final String[] listitem = {"English", "ಕನ್ನಡ", "हिंदी", "മലയാളം", "தமிழ்"};
        androidx.appcompat.app.AlertDialog.Builder mBuilder = new androidx.appcompat.app.AlertDialog.Builder(Gridmenu.this);
        mBuilder.setTitle("Choose langage");
        mBuilder.setSingleChoiceItems(listitem, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    languageSet.setLocate("en", getBaseContext(), getApplicationContext());
                    recreate();
                } else if (which == 1) {
                    languageSet.setLocate("ml", getBaseContext(), getApplicationContext());
                    recreate();
                } else if (which == 2) {
                    languageSet.setLocate("ta", getBaseContext(), getApplicationContext());
                    recreate();
                } else if (which == 3) {
                    languageSet.setLocate("hi", getBaseContext(), getApplicationContext());
                    recreate();
                } else if (which == 4) {
                    languageSet.setLocate("en", getBaseContext(), getApplicationContext());
                    recreate();
                }
                dialog.dismiss();
            }

        });
        androidx.appcompat.app.AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    public void setLocate(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("lang_setting", MODE_PRIVATE).edit();
        editor.putString("my_lang", lang);
        editor.apply();
    }

    public String loadLocate() {
        SharedPreferences pref = getSharedPreferences("lang_setting", Activity.MODE_PRIVATE);
        String language = pref.getString("my_lang", "en");
        Log.e("lang", language);
        if (language == "") {
            language = "en";
        }
        return language;
    }

    public String whichlanguge(String wlang) {

        String language = wlang;
        switch (language) {
            case "en":
                language = "English";
                break;
            case "ml":
                language = "മലയാളം";
                break;
            case "kn":
                language = "ಕನ್ನಡ";
                break;
            case "ta":
                language = "தமிழ்";
                break;
            case "hi":
                language = "हिंदी";
                break;
            default:
                language = "English";
                break;

        }
        return language;
    }

    private void checkPlayServicesAvailable() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to resolve the error.
                availability.getErrorDialog(this, resultCode, RC_PLAY_SERVICES).show();
            } else {
                // Unresolvable error
                Toast.makeText(this, "Google Play Services error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        // Unregister since the activity is paused.
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mMessageReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter(BROADCAST_UPDATE_APP));
            LANGUGE = loadLocate();
            setLocate(LANGUGE);
            Log.e(" LANGUGE", LANGUGE);
            // LANGUGE="en";

            lang = whichlanguge(LANGUGE);
            getSupportActionBar().setSubtitle(lang);

        }
        super.onResume();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                int priority = intent.getIntExtra("priority", 0);
                int stableVersion = intent.getIntExtra("stableVersion", 0);
                int versionCode = intent.getIntExtra("versionCode", 0);
                if (priority != 0 && stableVersion != 0 && versionCode != 0) {
                    showUpdateAlert(priority, stableVersion, versionCode);
                }
            }
        }
    };

    //If user app version < stable version, there should be update only no cancel. We should not allow user to use app.
    //If user app version < Version Code and Priority ==1, User should get pop up, new version available, user can cancel and use app normally.
    //if user app version<Version Code and Priority == 2, User must update the app, no cancel and we should not allow user to use app.
    private void showUpdateAlert(final int priority, final int stableVersion, int versionCode) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setIcon(R.mipmap.icon);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle("New Version available");
        alertDialogBuilder.setMessage("Please update the app to new version");
        if (BuildConfig.VERSION_CODE < stableVersion) {
            alertDialogBuilder.setPositiveButton("Update",
                    (dialog, id) -> {
                        openPlayStore();
                        dialog.dismiss();
                    });
            alertDialogBuilder.create().show();
        } else if (BuildConfig.VERSION_CODE < versionCode && priority == 1) {
            alertDialogBuilder.setPositiveButton("Update",
                    (dialog, id) -> {
                        openPlayStore();
                        dialog.dismiss();
                    });
            alertDialogBuilder.setNegativeButton("Update Later", (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialogBuilder.create().show();
        } else if (BuildConfig.VERSION_CODE < versionCode && priority == 2) {
            alertDialogBuilder.setPositiveButton("Update",
                    (dialog, id) -> {
                        openPlayStore();
                        dialog.dismiss();
                    });
            alertDialogBuilder.create().show();
        }

    }

    private void openPlayStore() {
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            finish();
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void showUpdateAlert(final int priority, final int stableVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.VERSION_CODE < stableVersion) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            new ContextThemeWrapper(Gridmenu.this, android.R.style.Theme_Holo_Light_Dialog));
                    alertDialogBuilder.setTitle("You are using old version");
                    alertDialogBuilder.setIcon(R.drawable.error);
                    alertDialogBuilder.setMessage("Please update the app to new version")
                            .setCancelable(false)
                            .setPositiveButton("Update",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                                System.exit(0);
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                            finish();
                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else if (priority == 1) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            new ContextThemeWrapper(Gridmenu.this, android.R.style.Theme_Holo_Light_Dialog));
                    alertDialogBuilder.setIcon(R.drawable.error);
                    alertDialogBuilder.setTitle("New Version available");
                    alertDialogBuilder.setMessage("Please update the app to new version")
                            .setCancelable(false)
                            .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton("Update",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                            finish();
                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                } else if (priority == 2) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            new ContextThemeWrapper(Gridmenu.this, android.R.style.Theme_Holo_Light_Dialog));
                    alertDialogBuilder.setIcon(R.drawable.error);
                    alertDialogBuilder.setTitle("New Version available");
                    alertDialogBuilder.setMessage("Please update the app to new version")
                            .setCancelable(false)
                            .setPositiveButton("Update",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                            finish();
                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(Gridmenu.this, android.R.style.Theme_Holo_Light_Dialog));
        alertDialogBuilder.setTitle("Close Application");
        alertDialogBuilder.setMessage("Are you sure you want to close app?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                dialog.cancel();
                                finish();
                                Gridmenu.this.moveTaskToBack(true);
                                System.exit(0);
                            }
                        });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    void isERV() {
        Thread background = new Thread() {
            @Override
            public void run() {
                WebserviceCall webserviceCall = new WebserviceCall();
                String result = webserviceCall.GetEmergencyVehicle(device_id);
                try {
                    final JSONArray jArr = new JSONArray(result);
                    for (int j = 0; j < jArr.length(); j++) {
                        try {
                            JSONObject data = jArr.getJSONObject(j);
                            if (data.getString("ID").equals(vehicleid)) {
                                is_ERV = true;
                                break;
                            }

                        } catch (JSONException js) {

                        }
                    }

                } catch (JSONException e) {
                } finally {
                    if (is_ERV) {
                        gridViewStrings = Arrays.copyOf(gridViewStrings, gridViewStrings.length + 1);
                        gridViewStrings[gridViewStrings.length - 1] = "Locate Vehicle";
                        gridViewImages = Arrays.copyOf(gridViewImages, gridViewImages.length + 1);
                        gridViewImages[gridViewImages.length - 1] = R.drawable.locate_vehicle;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridView.setAdapter(new CustomAndroidGridViewAdapter(Gridmenu.this, getApplicationContext(), gridViewStrings, gridViewImages));
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        };
        background.start();
    }
}
