package com.sensel.driverapp;

/**
 * Created by User on 20-10-2016.
 */

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.sensel.driverapp.objectclass.LockScreenParkingSpots;
import com.sensel.driverapp.utility.JourneyPlanDB;

import org.ksoap2.serialization.PropertyInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



public class LockScreenActivity extends Activity {
    WebView myWebView;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    int hr = 0, min = 0, chr = 0, cmin = 0, dec_h = 0, decimal_min = 0, cdec_h = 0, cdecimal_min = 0;
    private SpeedometerView speedometer;
    int cnt = 0;
    int[] lastspeed = new int[10];//this number is how many last speed points to store to check rest
    ProgressBar cirprogressBar;
    private TextView timetext;
    ProgressBar conprogressBar;
    public static ProgressBar timeprogressBar;
    private TextView contimetext;
    private TextView daydriventime;
    private TextView tripdriventime;
    private TextView resttime;
    private TextView parklocation;
    public static TextView remaiworktimetxt;
    public static String deviceid;
    int blink = 0;
    int rest = 0;
    boolean restaudio = true;
    boolean nightaudio = true;
    boolean blinkstatus = false;
    private static final int MY_PERMISSIONS_REQUEST = 99;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 101;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 102;
    public static boolean flag = false;
    public static boolean isdashboard = false;
    private TextView vehicle;
    int dayHr = 9;
    int dayMin = 30;
    int tripHr = 4;
    int tripMin = 30;
    int nightStart = 2000;//This is 8pm night
    int nightEnd = 800;//This is 5 am morning

    boolean nightdrivingallowed = false, maxdrivingaudio = true, condrivingaudio = true;

    int minDrive = 2, avgDrive = 3, maxDrive = 4, minRest = 20, avgRest = 30, maxRest = 40;
    public static int speed = 100000, constmaxspeed = 60;
    double lat, lng;
    long locationchangedtime = 0, lastlocationtime = 0;
    long lastspeedaudio = 0;
    SharedPreferences pref;
    TextView speedval;
    TextView maxspeedval;
    String lastruntime = "lastruntime";
    LocationManager locationManager;
    LocationListener locationListener;
    int gpsupdaterate = 10, gpstimeinterval = 0, gpsdistanceinterval = 5;
    private ScheduledExecutorService scheduledExecutorServiceForRuntime;
    private ScheduledFuture<?> futureTaskForRuntime;
    private Runnable taskForRuntime;
    private ScheduledExecutorService scheduledExecutorServiceForAudio;
    private ScheduledFuture<?> futureTaskForAudio;
    private Runnable taskForAudio;
    int runtimeRefreshRate = 5 * 60;
    int audioRefreshRate = 30 * 24 * 60 * 60;
    String language = "English-English";
    String[] gpsdateArray;
    String gpsdata;
    String[] audiodateArray;
    String audiodata;
    Float[] speedArray = new Float[5];//this number is how many last speed points to store to get accuracy
    int speedcount = 0;
    LocationProvider high;
    int alertcount = 0;

    public static Long totalmins;
    public static int hrs;
    public Timer timer;
    private Timer prefRefreshTimer;
    private Timer audioInsertTimer;
    private Timer hazardTimer;
    private Timer restTimer;


    //jrp variables
    ArrayList<LockScreenParkingSpots> lockScreenParkingSpots;
    JourneyPlanDB journeyPlanDB;

    private ArrayList<LatLng> latLngArrayList;
    long lastAnouncedtime = System.currentTimeMillis();

    int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.lockscreen);
        //get all selected parking spot
        parklocation = (TextView) findViewById(R.id.parklocation);
        lockScreenParkingSpots = new ArrayList<>();
        latLngArrayList = new ArrayList<>();

        journeyPlanDB = new JourneyPlanDB(getApplicationContext());

        Cursor cursor = journeyPlanDB.getdata();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                LockScreenParkingSpots parkingSpots = new LockScreenParkingSpots(cursor.getInt(0), cursor.getString(1), cursor.getString(5), cursor.getString(6));
                lockScreenParkingSpots.add(parkingSpots);
            }
        }

        cursor = journeyPlanDB.getLatLong();

        /*WebSettings settings = myWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        //settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        JavaScriptInterface jsInterface = new JavaScriptInterface(this, myWebView, getApplicationContext());
        myWebView.addJavascriptInterface(jsInterface, "JSInterface");*/

//        if (cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));
//                latLngArrayList.add(temp);
//            }
//        }


//        final Handler handler = new Handler();
//        Runnable task;
//        task = new Runnable() {
//            @Override
//            public void run() {
//                calculateDistanceInKilometer(latLngArrayList.get(count).latitude, latLngArrayList.get(count).longitude);
//                count++;
//                handler.postDelayed(this, 1000);
//            }
//
//        };
//        handler.post(task);


        vehicle = (TextView) findViewById(R.id.vehicle);
        speedval = (TextView) findViewById(R.id.speedval);
        Typeface myTypeface = Typeface.createFromAsset(getAssets(),
                "modern_lcd-7.ttf");
        speedval.setTypeface(myTypeface);
        speedval.setText("0");
        maxspeedval = (TextView) findViewById(R.id.maxspeedval);
        maxspeedval.setTypeface(myTypeface);
        maxspeedval.setText("60");
        cirprogressBar = (ProgressBar) findViewById(R.id.cirprogressBar);
        timetext = (TextView) findViewById(R.id.textpro);
        conprogressBar = (ProgressBar) findViewById(R.id.conprogressBar);
        contimetext = (TextView) findViewById(R.id.contextpro);
        daydriventime = (TextView) findViewById(R.id.daydriventime);
        tripdriventime = (TextView) findViewById(R.id.tripdriventime);
        //speedline=(ImageView)findViewById(R.id.speedline);
        resttime = (TextView) findViewById(R.id.resttime);
        // Customize SpeedometerView
        speedometer = (SpeedometerView) findViewById(R.id.speedometer);
        remaiworktimetxt = findViewById(R.id.remaiworktime);

        timeprogressBar = findViewById(R.id.workprogressBar);
        //     workProgessbarUpdator(WorktimeBrodcast.hrs);


        // Add label converter
        speedometer.setLabelConverter(new SpeedometerView.LabelConverter() {
            @Override
            public String getLabelFor(double progress, double maxProgress) {
                return String.valueOf((int) Math.round(progress));
            }
        });
        if (deviceid == null) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                deviceid = tm.getDeviceId();
            } else {
                String android_id = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                if (android_id != null) {
                    deviceid = android_id;
                }
            }
        }
        // configure value range and ticks
        speedometer.setMaxSpeed(120);
        speedometer.setMajorTickStep(10);
        speedometer.setMinorTicks(1);
        // speedometer.setSpeed(0,100,10);
        // Configure value range colors
        speedometer.addColoredRange(0, 50, Color.GREEN);
        speedometer.addColoredRange(50, 80, Color.YELLOW);
        speedometer.addColoredRange(80, 120, Color.RED);
        pref = getApplicationContext().getSharedPreferences("totaljrm", MODE_PRIVATE);
        // 1. Immediately populate UI on Frame 1 from local storage (Zero delay!)
        String cachedRunTime = pref.getString("runAndContinuousTime", "");
        if (cachedRunTime != null && cachedRunTime.contains("#") && !cachedRunTime.contains("VehicleId")) {
            updateRuntimeUI(cachedRunTime);
        } else {
            if (Gridmenu.points != null && Gridmenu.points.length == 8 && !Gridmenu.points[7].contains("NA") && !Gridmenu.points[7].trim().isEmpty()) {
                vehicle.setText(Gridmenu.points[7].trim());
            } else if (Gridmenu.vehicleid != null && !Gridmenu.vehicleid.trim().isEmpty()) {
                vehicle.setText(Gridmenu.vehicleid.trim());
            }
        }

        // 2. Fetch fresh runtime in background immediately when screen opens
        if (isNetworkAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String runAndContinuousTime = GetRunAndContinuousTime(deviceid);
                        if (runAndContinuousTime != null && runAndContinuousTime.contains("#") && !runAndContinuousTime.contains("Exception") && !runAndContinuousTime.contains("No updates")) {
                            pref.edit().putString("runAndContinuousTime", runAndContinuousTime).apply();
                            updateRuntimeUI(runAndContinuousTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        // 3. Stop any existing timers before starting fresh lifecycle-managed timers
        stopDashboardTimers();

        prefRefreshTimer = new Timer();
        prefRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            dayHr = pref.getInt("dayHr", dayHr);
                            dayMin = pref.getInt("dayMin", dayMin);
                            tripHr = pref.getInt("tripHr", tripHr);
                            tripMin = pref.getInt("tripMin", tripMin);
                            nightStart = pref.getInt("nightStart", nightStart);
                            nightEnd = pref.getInt("nightEnd", nightEnd);
                            minDrive = pref.getInt("minDrive", minDrive);
                            maxDrive = pref.getInt("maxDrive", maxDrive);
                            avgDrive = (minDrive + maxDrive) / 2;
                            minRest = pref.getInt("minRest", minRest);
                            maxRest = pref.getInt("maxRest", maxRest);
                            avgRest = (minRest + maxRest) / 2;
                            constmaxspeed = pref.getInt("maxSpeed", constmaxspeed);
                            if (runtimeRefreshRate != pref.getInt("runtimeRefreshRate", runtimeRefreshRate) / 2) {
                                runtimeRefreshRate = pref.getInt("runtimeRefreshRate", runtimeRefreshRate) / 2;
                                if (runtimeRefreshRate > 0) {
                                    if (futureTaskForRuntime != null) {
                                        futureTaskForRuntime.cancel(true);
                                    }

                                    futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                            if (audioRefreshRate != pref.getInt("audioRefreshRate", audioRefreshRate)) {
                                audioRefreshRate = pref.getInt("audioRefreshRate", audioRefreshRate);
                                if (audioRefreshRate > 0) {
                                    if (futureTaskForAudio != null) {
                                        futureTaskForAudio.cancel(true);
                                    }
                                    futureTaskForAudio = scheduledExecutorServiceForAudio.scheduleAtFixedRate(taskForAudio, 0, audioRefreshRate, TimeUnit.SECONDS);
                                }
                            }
                            gpsupdaterate = pref.getInt("gpsupdaterate", gpsupdaterate);
                            if (gpsdistanceinterval != pref.getInt("gpsdistanceinterval", gpsdistanceinterval) || gpstimeinterval != pref.getInt("gpstimeinterval", gpstimeinterval)) {
                                gpsdistanceinterval = pref.getInt("gpsdistanceinterval", gpsdistanceinterval);
                                gpstimeinterval = pref.getInt("gpstimeinterval", gpstimeinterval);
                                if (ContextCompat.checkSelfPermission(LockScreenActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(LockScreenActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                if (locationManager != null && locationListener != null) {
                                    locationManager.removeUpdates(locationListener);
                                }
                                high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), true));
                                locationManager.requestLocationUpdates(high.getName(), gpstimeinterval, gpsdistanceinterval, locationListener);
                            }
                        }
                    });
                }
            }, 0, 2 * 60 * 1000);

            audioInsertTimer = new Timer();
            audioInsertTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            insertaudio();
                        }
                    });
                }
            }, 0, 10 * 60 * 1000);

            scheduledExecutorServiceForAudio = Executors.newScheduledThreadPool(5);

            taskForAudio = new Runnable() {
                @Override
                public void run() {
                    try {
                        runOnUiThread(new Runnable() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void run() {
                                if (Build.VERSION.SDK_INT <= 32) {
                                    if (ContextCompat.checkSelfPermission(LockScreenActivity.this,
                                            Manifest.permission.CAMERA)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(LockScreenActivity.this,
                                                new String[]{Manifest.permission.CAMERA},
                                                MY_PERMISSIONS_REQUEST_CAMERA);
                                    } else if (ContextCompat.checkSelfPermission(LockScreenActivity.this,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(LockScreenActivity.this,
                                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                MY_PERMISSIONS_REQUEST_STORAGE);
                                    } else if (ContextCompat.checkSelfPermission(LockScreenActivity.this,
                                            Manifest.permission.READ_EXTERNAL_STORAGE)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(LockScreenActivity.this,
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                MY_PERMISSIONS_REQUEST_STORAGE);
                                    } else {
                                        if (Gridmenu.points != null && Gridmenu.points.length == 8 && !Gridmenu.points[7].contains("NA")) {
                                            long lastaudiodownloaded = pref.getLong("lastaudiodownloaded", 0);
                                            if ((int) ((System.currentTimeMillis() - lastaudiodownloaded) / (60 * 60 * 1000)) >= 1) {
                                                Thread languageThread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            int waited = 0;
                                                            while (waited < 500) {
                                                                sleep(100);
                                                                waited += 100;
                                                                if (waited == 100) {
                                                                    WebserviceCall webserviceCall = new WebserviceCall();
                                                                    String result = webserviceCall.GetDriver_Language(deviceid);
                                                                    if ((!result.contains("Exception")) && result.length() > 2) {
                                                                        if (language != result) {
                                                                            language = result;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        } catch (InterruptedException e) {
                                                            // do nothing
                                                        }
                                                    }

                                                };
                                                languageThread.start();
                                                new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_continuous_driving_voice.mp3");
                                                new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_slowdown_voice.mp3");
                                                new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_max_driving_voice.mp3");
                                                new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_night_driving_voice.mp3");
                                                SharedPreferences.Editor editor = pref.edit();
                                                editor.putLong("lastaudiodownloaded", System.currentTimeMillis());
                                                editor.apply();
                                            }
                                        }
                                    }
                                }

                                /*else if (Build.VERSION.SDK_INT >= 32)
                                {
                                    if(ContextCompat.checkSelfPermission(LockScreenActivity.this,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {

                                    ActivityCompat.requestPermissions(LockScreenActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            MY_PERMISSIONS_REQUEST_STORAGE);
                                }
                                    if(ContextCompat.checkSelfPermission(LockScreenActivity.this,
                                            Manifest.permission.READ_MEDIA_IMAGES)
                                            != PackageManager.PERMISSION_GRANTED) {

                                        ActivityCompat.requestPermissions(LockScreenActivity.this,
                                                new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                                MY_PERMISSIONS_REQUEST_STORAGE);
                                    }
                                }*/
                                else {
                                    if (Gridmenu.points != null && Gridmenu.points.length == 8 && !Gridmenu.points[7].contains("NA")) {
                                        long lastaudiodownloaded = pref.getLong("lastaudiodownloaded", 0);
                                        if ((int) ((System.currentTimeMillis() - lastaudiodownloaded) / (60 * 60 * 1000)) >= 1) {
                                            Thread languageThread = new Thread() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        int waited = 0;
                                                        while (waited < 500) {
                                                            sleep(100);
                                                            waited += 100;
                                                            if (waited == 100) {
                                                                WebserviceCall webserviceCall = new WebserviceCall();
                                                                String result = webserviceCall.GetDriver_Language(deviceid);
                                                                if ((!result.contains("Exception")) && result.length() > 2) {
                                                                    if (language != result) {
                                                                        language = result;
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } catch (InterruptedException e) {
                                                        // do nothing
                                                    }
                                                }

                                            };
                                            languageThread.start();
                                            new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_continuous_driving_voice.mp3");
                                            new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_slowdown_voice.mp3");
                                            new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_max_driving_voice.mp3");
                                            new DownloadFileFromURL(LockScreenActivity.this).execute(Config.DOMAINURL1 + "/Uploads/TotalGasAudioFiles/" + language + "_night_driving_voice.mp3");
                                            SharedPreferences.Editor editor = pref.edit();
                                            editor.putLong("lastaudiodownloaded", System.currentTimeMillis());
                                            editor.apply();
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            if (audioRefreshRate > 0) {
                if (futureTaskForAudio != null) {
                    futureTaskForAudio.cancel(true);
                }
                futureTaskForAudio = scheduledExecutorServiceForAudio.scheduleAtFixedRate(taskForAudio, 0, audioRefreshRate, TimeUnit.SECONDS);
            }

            scheduledExecutorServiceForRuntime = Executors.newScheduledThreadPool(5);

            taskForRuntime = new Runnable() {
                @Override
                public void run() {
                    try {
                        isLocationAvailable();
                        if (isNetworkAvailable()) {
                            String fresh = GetRunAndContinuousTime(deviceid);
                            if (fresh != null && fresh.contains("#") && !fresh.contains("Exception") && !fresh.contains("No updates")) {
                                pref.edit().putString("runAndContinuousTime", fresh).apply();
                                updateRuntimeUI(fresh);
                                return;
                            }
                        }
                        String run_time = pref.getString("runAndContinuousTime", "");
                        if (run_time != null && run_time.contains("#")) {
                            updateRuntimeUI(run_time);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            if (runtimeRefreshRate > 0) {
                if (futureTaskForRuntime != null) {
                    futureTaskForRuntime.cancel(true);
                }

                futureTaskForRuntime = scheduledExecutorServiceForRuntime.scheduleAtFixedRate(taskForRuntime, 0, runtimeRefreshRate, TimeUnit.SECONDS);
            }

            hazardTimer = new Timer();
            hazardTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (!isGPSAvailable()) {
                                alertcount++;
                                if (alertcount == 100) {
                                    Toast.makeText(getApplicationContext(), "Enable GPS", Toast.LENGTH_SHORT).show();
                                    alertcount = 0;
                                }
                            } else if (!isNetworkAvailable()) {
                                alertcount++;
                                if (alertcount == 100) {
                                    Toast.makeText(getApplicationContext(), "Enable data", Toast.LENGTH_SHORT).show();
                                    alertcount = 0;
                                }
                            } else {
                                boolean parking = false;
                                int maxSpeed = constmaxspeed;
                                SQLiteDatabase db = null;
                                Cursor cursor = null;
                                nightdrivingallowed = false;
                                try {
                                    db = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                    db.execSQL("CREATE TABLE IF NOT EXISTS jrmspots(minlat DECIMAL(9,6),maxlat DECIMAL(9,6),minlng DECIMAL(9,6),maxlng DECIMAL(9,6),type VARCHAR);");
                                    String query = "SELECT DISTINCT type FROM jrmspots WHERE minlat<'" + lat + "' and maxlat>'" + lat + "' and minlng<'" + lng + "' and maxlng>'" + lng + "'";
                                    cursor = db.rawQuery(query, null);
                                    if (cursor != null && cursor.getCount() > 0) {
                                        cursor.moveToFirst();
                                        do {
                                            if (cursor.getString(0).contains("Accident")) {
                                                maxSpeed = 25;
                                            } else if ((cursor.getString(0).contains("Blind")) && maxSpeed != 25) {
                                                maxSpeed = 35;
                                            } else if ((cursor.getString(0).contains("Parking")) && maxSpeed != 25 && maxSpeed != 35) {
                                                parking = true;
                                            }
                                            if (cursor.getString(0).contains("night"))
                                                nightdrivingallowed = true;
                                        } while (cursor.moveToNext());
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (cursor != null) {
                                        try { cursor.close(); } catch (Exception ignored) {}
                                    }
                                    if (db != null) {
                                        try { db.close(); } catch (Exception ignored) {}
                                    }
                                    if (cnt >= lastspeed.length)
                                        cnt = 0;
                                    if (speed > maxSpeed) {
                                        if (!blinkstatus) {
                                            blinkstatus = true;
                                            speedblink();
                                        }
                                    } else {
                                        blinkstatus = false;
                                    }
                                    if (maxSpeed == 25) {
                                        speedometer.setBackgroundColor(Color.RED);
                                    } else if (maxSpeed == 35) {
                                        speedometer.setBackgroundColor(Color.YELLOW);
                                    } else if (parking) {
                                        speedometer.setBackgroundColor(Color.GREEN);
                                    } else {
                                        speedometer.setBackgroundColor(Color.WHITE);
                                    }


                                    // maxspeedval=(TextView) findViewById(R.id.maxspeedval);
                                    Typeface myTypeface = Typeface.createFromAsset(getAssets(),
                                            "modern_lcd-7.ttf");
                                    maxspeedval.setTypeface(myTypeface);
                                    maxspeedval.setText(Integer.toString(maxSpeed));

                                    lastspeed[cnt] = speed;
                                    cnt++;
                                    db.close();
                                }
                            }
                        }
                    });
                }
            }, 0, 3000);//put here time 1000 milliseconds=1 second

            restTimer = new Timer();
            restTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                            boolean realruntime = true;
                            for (int i = 0; i < lastspeed.length; i++) {
                                if (lastspeed[i] <= 1) {
                                    realruntime = false;
                                    break;
                                }
                            }
                            if (realruntime) {
                                if (min == 0) {
                                    if (hr != 0) {
                                        hr--;
                                        min = 59;
                                    }
                                } else
                                    min--;
                                if (min < 10)
                                    timetext.setText(hr + ":0" + min);
                                else
                                    timetext.setText(hr + ":" + min);

                                int progress = dec_h * 100 + ((decimal_min * 100) / 60);
                                int progresspercent = (progress * 100) / (dayHr * 100 + (dayMin * 100) / 60);
                                if (progresspercent > 100)
                                    progresspercent = 100;

                                if (progresspercent < 80)
                                    cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                                else if (progresspercent < 90)
                                    cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(243, 156, 18)));
                                else
                                    cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));

                                cirprogressBar.setProgress(progresspercent);
                                if (cmin == 0) {
                                    if (chr != 0) {
                                        chr--;
                                        cmin = 59;
                                    }
                                } else
                                    cmin--;
                                if (cmin < 10)
                                    contimetext.setText(chr + ":0" + cmin);
                                else
                                    contimetext.setText(chr + ":" + cmin);

                                if (decimal_min == 59) {
                                    decimal_min = 00;
                                    dec_h++;
                                } else
                                    decimal_min++;
                                if (decimal_min < 10)
                                    daydriventime.setText("Day : " + dec_h + ":0" + decimal_min);
                                else
                                    daydriventime.setText("Day : " + dec_h + ":" + decimal_min);

                                if (cdecimal_min == 59) {
                                    cdecimal_min = 00;
                                    cdec_h++;
                                } else
                                    cdecimal_min++;
                                if (cdecimal_min < 10)
                                    tripdriventime.setText("Trip : " + cdec_h + ":0" + cdecimal_min);
                                else
                                    tripdriventime.setText("Trip : " + cdec_h + ":" + cdecimal_min);

                                int cprogress = cdec_h * 100 + ((cdecimal_min * 100) / 60);
                                int cprogresspercent = (cprogress * 100) / (tripHr * 100 + (tripMin * 100) / 60);
                                if (cprogresspercent > 100)
                                    cprogresspercent = 100;

                                if (cprogresspercent < 80)
                                    conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                                else if (progresspercent < 90)
                                    conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(243, 156, 18)));
                                else
                                    conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                                conprogressBar.setProgress(cprogresspercent);
                                if ((cdec_h == tripHr && cdecimal_min >= tripMin) || cdec_h > tripHr) {
                                    resttime.setText("Plan to take rest");
                                    condrivingaudio = false;
                                } else {
                                    resttime.setText("Vehicle is moving");
                                }
                                rest = 0;
                                restaudio = true;
                            } else {
                                maxdrivingaudio = true;
                                condrivingaudio = true;
                                nightaudio = true;
                                if (restaudio && ((cdec_h < (minDrive + 1) && rest > minRest) || (cdec_h < (avgDrive + 1) && rest > avgRest) || (cdec_h > maxDrive && rest > maxRest))) {
                                    resttime.setText("Enough rest taken..");
                                    restaudio = false;
                                } else {
                                    if (rest > 60) {
                                        int hour = rest / 60;
                                        int minutes = rest % 60;
                                        resttime.setText("Rest time : " + hour + "hr " + minutes + "min");
                                    } else
                                        resttime.setText("Rest time : " + rest + "min");
                                }
                                rest++;
                            }
                        }
                    });
                }
            }, 0, 60000);

            Getspeed();
            speed = 0;

        localworktimer();
    }

    private void VoiceAlertForParking(Context mContext, int limit) {
    }

    public static void workProgessbarUpdator(int workdone) {

        try {

            Log.e("workdone", String.valueOf(workdone));

            Long l = new Long(CustomAndroidGridViewAdapter.worktime);
            int hrs = l.intValue();

            int hours = workdone / 60;
            int minutes = workdone % 60;

            remaiworktimetxt.setText(String.valueOf(hours) + ":" + String.valueOf(minutes));

            if (workdone == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    timeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }

            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    timeprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                }
            }
            timeprogressBar.setMax(hrs);
            timeprogressBar.setSecondaryProgress(hrs - workdone);


        } catch (Exception e) {

        }


    }

    public void updateRuntimeUI(String run_time) {
        if (run_time == null || !run_time.contains("#")) {
            if (Gridmenu.points != null && Gridmenu.points.length == 8) {
                if (vehicle != null && !Gridmenu.points[7].contains("NA") && !Gridmenu.points[7].trim().isEmpty()) {
                    runOnUiThread(() -> vehicle.setText(Gridmenu.points[7].trim()));
                }
            } else if (Gridmenu.vehicleid != null && !Gridmenu.vehicleid.trim().isEmpty() && vehicle != null) {
                runOnUiThread(() -> vehicle.setText(Gridmenu.vehicleid.trim()));
            }
            return;
        }

        try {
            final String[] data = run_time.split("#");
            if (data.length < 5) return;

            // Day driving time: e.g. "2h  22min" or "2h 22min"
            String[] res = data[0].trim().split("\\s+");
            if (res.length >= 2) {
                String[] h = res[0].split("h");
                String[] m = res[1].split("m");
                dec_h = Integer.parseInt(h[0].trim());
                decimal_min = Integer.parseInt(m[0].trim());
            }

            if ((dec_h < dayHr) || (dec_h == dayHr && decimal_min < dayMin)) {
                if (dayMin >= decimal_min) {
                    min = dayMin - decimal_min;
                    hr = dayHr - dec_h;
                } else {
                    min = dayMin + 60 - decimal_min;
                    hr = dayHr - 1 - dec_h;
                }
            } else {
                hr = 0;
                min = 0;
            }

            // Trip driving time: e.g. "0h  16min" or "0h 16min"
            res = data[2].trim().split("\\s+");
            if (res.length >= 2) {
                String[] h = res[0].split("h");
                String[] m = res[1].split("m");
                cdec_h = Integer.parseInt(h[0].trim());
                cdecimal_min = Integer.parseInt(m[0].trim());
            }

            if ((cdec_h < tripHr) || (cdec_h == tripHr && cdecimal_min < tripMin)) {
                if (tripMin >= cdecimal_min) {
                    cmin = tripMin - cdecimal_min;
                    chr = tripHr - cdec_h;
                } else {
                    cmin = tripMin + 60 - cdecimal_min;
                    chr = tripHr - 1 - cdec_h;
                }
            } else {
                chr = 0;
                cmin = 0;
            }

            try {
                rest = Integer.parseInt(data[3].trim());
            } catch (Exception ignored) {}

            runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    if (vehicle != null && data.length >= 5 && !data[4].trim().isEmpty() && !data[4].contains("VehicleId")) {
                        vehicle.setText(data[4].trim());
                    } else if (Gridmenu.points != null && Gridmenu.points.length == 8 && !Gridmenu.points[7].contains("NA") && !Gridmenu.points[7].trim().isEmpty()) {
                        if (vehicle != null) vehicle.setText(Gridmenu.points[7].trim());
                    } else if (Gridmenu.vehicleid != null && !Gridmenu.vehicleid.trim().isEmpty() && vehicle != null) {
                        vehicle.setText(Gridmenu.vehicleid.trim());
                    }

                    int progress = dec_h * 100 + ((decimal_min * 100) / 60);
                    int progresspercent = (progress * 100) / (dayHr * 100 + (dayMin * 100) / 60);
                    if (progresspercent > 100) progresspercent = 100;

                    if (cirprogressBar != null) {
                        if (progresspercent < 80)
                            cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        else if (progresspercent < 90)
                            cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(243, 156, 18)));
                        else
                            cirprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        cirprogressBar.setProgress(progresspercent);
                    }

                    if (timetext != null) {
                        if (min < 10)
                            timetext.setText(hr + ":0" + min);
                        else
                            timetext.setText(hr + ":" + min);
                    }

                    if (daydriventime != null) {
                        if (decimal_min < 10)
                            daydriventime.setText("Day : " + dec_h + ":0" + decimal_min);
                        else
                            daydriventime.setText("Day : " + dec_h + ":" + decimal_min);
                    }

                    int cprogress = cdec_h * 100 + ((cdecimal_min * 100) / 60);
                    int cprogresspercent = (cprogress * 100) / (tripHr * 100 + (tripMin * 100) / 60);
                    if (cprogresspercent > 100) cprogresspercent = 100;

                    if (conprogressBar != null) {
                        if (cprogresspercent < 80)
                            conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        else if (cprogresspercent < 90)
                            conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(243, 156, 18)));
                        else
                            conprogressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                        conprogressBar.setProgress(cprogresspercent);
                    }

                    if (contimetext != null) {
                        if (cmin < 10)
                            contimetext.setText(chr + ":0" + cmin);
                        else
                            contimetext.setText(chr + ":" + cmin);
                    }

                    if (tripdriventime != null) {
                        if (cdecimal_min < 10)
                            tripdriventime.setText("Trip : " + cdec_h + ":0" + cdecimal_min);
                        else
                            tripdriventime.setText("Trip : " + cdec_h + ":" + cdecimal_min);
                    }

                    if (resttime != null) {
                        if (rest >= 60) {
                            int hour = rest / 60;
                            int minutes = rest % 60;
                            resttime.setText("Rest time : " + hour + "hr " + minutes + "min");
                        } else {
                            resttime.setText("Rest time : " + rest + "min");
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopDashboardTimers() {
        if (prefRefreshTimer != null) {
            try { prefRefreshTimer.cancel(); } catch (Exception ignored) {}
            prefRefreshTimer = null;
        }
        if (audioInsertTimer != null) {
            try { audioInsertTimer.cancel(); } catch (Exception ignored) {}
            audioInsertTimer = null;
        }
        if (hazardTimer != null) {
            try { hazardTimer.cancel(); } catch (Exception ignored) {}
            hazardTimer = null;
        }
        if (restTimer != null) {
            try { restTimer.cancel(); } catch (Exception ignored) {}
            restTimer = null;
        }
        if (futureTaskForRuntime != null) {
            try { futureTaskForRuntime.cancel(true); } catch (Exception ignored) {}
            futureTaskForRuntime = null;
        }
        if (futureTaskForAudio != null) {
            try { futureTaskForAudio.cancel(true); } catch (Exception ignored) {}
            futureTaskForAudio = null;
        }
        if (scheduledExecutorServiceForRuntime != null) {
            try { scheduledExecutorServiceForRuntime.shutdownNow(); } catch (Exception ignored) {}
            scheduledExecutorServiceForRuntime = null;
        }
        if (scheduledExecutorServiceForAudio != null) {
            try { scheduledExecutorServiceForAudio.shutdownNow(); } catch (Exception ignored) {}
            scheduledExecutorServiceForAudio = null;
        }
    }

    public void localworktimer() {

        try {
            SharedPreferences prefs = getApplicationContext().getSharedPreferences("WORKSTARTEND", MODE_PRIVATE);
            String endtime = prefs.getString("enddate", "");
            totalmins = prefs.getLong("minsdiffrence", 0);
            String btnvalue = prefs.getString("workbtn", "");


            //getting current date


            //Log.e("total",String.valueOf(totalmins));
            if (btnvalue.contains("stop")) {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        try {
                            if (endtime != null && !endtime.isEmpty()) {
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                                Date d1 = sdf.parse(endtime);
                                Date d2 = new Date();

                                if (d1.after(d2)) {
                                    long difference_In_Time = d1.getTime() - d2.getTime();
                                    long difference_In_Minutes = (difference_In_Time / 1000) / 60;

                                    Log.e("d", String.valueOf(difference_In_Minutes));

                                    Long l = new Long(difference_In_Minutes);
                                    hrs = l.intValue();
                                    LockScreenActivity.workProgessbarUpdator(hrs);
                                    workProgessbarUpdator(hrs);
                                } else {
                                    LockScreenActivity.workProgessbarUpdator(0);
                                    workProgessbarUpdator(0);
                                }
                            } else {
                                LockScreenActivity.workProgessbarUpdator(0);
                                workProgessbarUpdator(0);
                            }
                        } catch (Exception e) {

                            Log.e("ex", e.toString());

                        }

                    }
                }, 0, 60000);
            } else {

                Log.e("ca", "lled");


                workProgessbarUpdator(720);
            }

        } catch (Exception e) {

        }


    }

    public String GetRunAndContinuousTime(String device_id) {
        String SOAP_ACTION = "http://tempuri.org/GetRunAndContinuousTime";
        String OPERATION_NAME = "GetRunAndContinuousTime";

        PropertyInfo[] piarray = new PropertyInfo[1];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0] = pi;
        WebserviceCall webserviceCall = new WebserviceCall();
        return webserviceCall.callWebservice(Config.DOMAINURL1, Config.DOMAINURL2, OPERATION_NAME, piarray, SOAP_ACTION);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pref != null) {
            String cachedRunTime = pref.getString("runAndContinuousTime", "");
            if (cachedRunTime != null && cachedRunTime.contains("#") && !cachedRunTime.contains("VehicleId")) {
                updateRuntimeUI(cachedRunTime);
            }
        }
        localworktimer();
        //This audio manager is to increase phone volume to maximum
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    public void speedblink() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (blinkstatus == true) {
                            if ((blink % 2) == 0)
                                vehicle.setBackgroundColor(Color.RED);
                            else if ((blink % 2) == 1)
                                vehicle.setBackgroundColor(Color.WHITE);
                            blink++;
                        } else {
                            blink = 0;
                            vehicle.setBackgroundColor(Color.WHITE);
                            cancel();
                        }
                    }
                });
            }
        }, 0, 500);
    }

    public boolean isGPSAvailable() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;
        else
            return false;
    }

    public void isLocationAvailable() {
        if ((ContextCompat.checkSelfPermission(LockScreenActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(LockScreenActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(LockScreenActivity.this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Location permission is needed.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else
                {

                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("STORAGE permission is mandatory to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (audioRefreshRate > 0) {
                                if (futureTaskForAudio != null) {
                                    futureTaskForAudio.cancel(true);
                                }
                                futureTaskForAudio = scheduledExecutorServiceForAudio.scheduleAtFixedRate(taskForAudio, 0, audioRefreshRate, TimeUnit.SECONDS);
                            }
                        }
                    });
                    ad.show();

                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed() {
        if (speed < constmaxspeed) {
            stopDashboardTimers();
            finish();
        }
    }

    @Override
    public void onDestroy() {
        stopDashboardTimers();
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ignored) {}
        }
        if (!isdashboard && !flag) {
            Intent i = new Intent(getApplicationContext(), LockScreenActivity.class);
            //  i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(i);
        }
        isdashboard = false;
        super.onDestroy();
    }


    public void calculateDistanceInKilometer(double userLat, double userLng) {
        long currenttime = System.currentTimeMillis();
        double AVERAGE_RADIUS_OF_EARTH_KM = 6371;

        double parkLat = 0, parkLng = 0, latDistance, lngDistance, a, c;
        int distance = 0;
        int tempdistance = 9999;

        for (int i = 0; i < lockScreenParkingSpots.size(); i++) {
            String latlong[] = lockScreenParkingSpots.get(i).getLatLong().split(",");
            parkLat = Double.valueOf(latlong[0]);
            parkLng = Double.valueOf(latlong[1]);

            latDistance = Math.toRadians(userLat - parkLat);
            lngDistance = Math.toRadians(userLng - parkLng);

            a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(parkLat))
                    * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

            c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            distance = (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
            Log.e("DISTANCE:", String.valueOf(distance));
            Log.e(lockScreenParkingSpots.get(i).getLatLong(), String.valueOf(userLat) + "," + String.valueOf(userLng));

            if (tempdistance > distance) {

                if (lockScreenParkingSpots.get(i).getIsComplete().contains("No")) {


                    tempdistance = distance;
                    try {
                        parklocation.setText("NEXT PARKING LOCATION :" + lockScreenParkingSpots.get(i).getParkingName() + "\n" + String.valueOf(distance) + " KM");

                        if (currenttime - lastAnouncedtime > 30000) {
                            Log.e("1093", "__");
                            lastAnouncedtime = currenttime;
                            if (distance < 15 && distance >= 10) {
                                parklocation.setTextColor(Color.RED);
                                VoiceAlertForParking(getApplicationContext(), 15);
                                Log.e("15-10", lockScreenParkingSpots.get(i).getParkingName());
                            } else if (distance < 10 && distance >= 3) {
                                VoiceAlertForParking(getApplicationContext(), 10);
                                parklocation.setTextColor(Color.BLUE);
                                Log.e("10-03", lockScreenParkingSpots.get(i).getParkingName());
                            } else if (distance < 3 && distance > 1) {
                                VoiceAlertForParking(getApplicationContext(), 3);
                                Log.e("3-0", lockScreenParkingSpots.get(i).getParkingName());
                                parklocation.setTextColor(Color.GREEN);
                            } else if (distance <= 1) {
                                VoiceAlertForParking(getApplicationContext(), 3);
                                Log.e("1-0", lockScreenParkingSpots.get(i).getParkingName());
                                parklocation.setTextColor(Color.GREEN);

                                journeyPlanDB.updateParkingDone(lockScreenParkingSpots.get(i).getParkingName(), lockScreenParkingSpots.get(i).getId());
                                Cursor cursor = journeyPlanDB.getdata();
                                lockScreenParkingSpots.clear();
                                if (cursor.getCount() > 0) {
                                    while (cursor.moveToNext()) {
                                        LockScreenParkingSpots parkingSpots = new LockScreenParkingSpots(cursor.getInt(0), cursor.getString(1), cursor.getString(5), cursor.getString(6));
                                        lockScreenParkingSpots.add(parkingSpots);
                                    }
                                }
                                parklocation.setTextColor(Color.BLACK);

                            }

                        }

                    } catch (Exception e) {
                        Log.e("EXEEPTION", e.toString());
                    }


                }
            }

        }


    }


    public void Getspeed() {
        // Acquire a reference to the system  Location Manager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if(isGPSAvailable()) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            high = locationManager.getProvider(locationManager.getBestProvider(createFineCriteria(), true));
            // Define a listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    lat = Double.parseDouble(new DecimalFormat("##.######").format(location.getLatitude()));
                    lng = Double.parseDouble(new DecimalFormat("##.######").format(location.getLongitude()));
                    try {
                        calculateDistanceInKilometer(lat, lng);
                    } catch (Exception e) {
                    }


                    Float mspeed = (location.getSpeed() * 3600) / 1000; //miles/sec to Km/hr
                    if (mspeed > 15)
                        mspeed = mspeed + 2.0f;
                    if (speedcount >= speedArray.length)
                        speedcount = 0;
                    if (mspeed < 120 && location.getAccuracy() < 20) {
                        speedArray[speedcount] = mspeed;
                        boolean speedFlag = true;
                        if (speedArray[speedArray.length - 1] != null) {
                            for (int i = 0; i < speedArray.length; i++) {
                                if (speedArray[i] < 1) {
                                    speedFlag = false;
                                    break;
                                }
                            }
                        }
                        speedcount++;
                        speed = 0;
                        if (speedFlag)
                            speed = Math.round(mspeed);

                        speedometer.setSpeed(speed);
                        //  speedval=(TextView) findViewById(R.id.speedval);
                        Typeface myTypeface = Typeface.createFromAsset(getAssets(),
                                "modern_lcd-7.ttf");
                        speedval.setTypeface(myTypeface);
                        speedval.setText(Integer.toString(speed));
                        locationchangedtime = location.getTime();
                        if ((int) ((locationchangedtime - lastlocationtime) / 1000) >= gpsupdaterate) {
                            lastlocationtime = locationchangedtime;
                            DecimalFormat df = new DecimalFormat("#.##");
                            SQLiteDatabase db = null;
                            try {
                                db = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                db.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(lat DECIMAL(9,6),lng DECIMAL(9,6),speed DECIMAL(5,2),timestamp DATETIME);");
                                String Query = "INSERT INTO gpsdata(lat ,lng , speed, timestamp) VALUES ('" + lat + "','" + lng + "','" + df.format(mspeed) + "','" + DateFormat.format("yyyy-MM-dd H:mm:ss", new Date(locationchangedtime)).toString() + "');";
                                db.execSQL(Query);
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (db != null) {
                                    try { db.close(); } catch (Exception ignored) {}
                                }
                            }
                            if (isNetworkAvailable()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        SQLiteDatabase selectDb = null;
                                        Cursor cursor = null;
                                        try {
                                            selectDb = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                            String query = "SELECT * FROM gpsdata order by timestamp desc limit 100";
                                            cursor = selectDb.rawQuery(query, null);
                                            gpsdata = "";
                                            gpsdateArray = new String[100];
                                            if (cursor != null && cursor.getCount() > 0) {
                                                cursor.moveToFirst();
                                                int i = 0;
                                                do {
                                                    gpsdateArray[i] = cursor.getString(3);
                                                    gpsdata = gpsdata + cursor.getString(0) + "#" + cursor.getString(1) + "#" + cursor.getString(2) + "#" + cursor.getString(3) + "|";
                                                    i++;
                                                } while (cursor.moveToNext());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            if (cursor != null) {
                                                try { cursor.close(); } catch (Exception ignored) {}
                                            }
                                            if (selectDb != null) {
                                                try { selectDb.close(); } catch (Exception ignored) {}
                                            }
                                        }
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    String result = insertMobileGpsData(deviceid, gpsdata);
                                                    if (result.contains("Data Inserted successfully")) {
                                                        String data = "delete from gpsdata where timestamp in (";
                                                        for (int i = 0; i < gpsdateArray.length; i++) {
                                                            if (gpsdateArray[i] != null) {
                                                                if (i != 0)
                                                                    data = data + ",";
                                                                data = data + "'" + gpsdateArray[i] + "'";
                                                            } else
                                                                break;
                                                        }
                                                        data = data + ");";
                                                        SQLiteDatabase deleteDb = null;
                                                        try {
                                                            deleteDb = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                                            deleteDb.execSQL(data);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        } finally {
                                                            if (deleteDb != null) {
                                                                try { deleteDb.close(); } catch (Exception ignored) {}
                                                            }
                                                        }
                                                    }
                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                        }).start();
                                    }
                                });
                            }
                        }
                    }
                    if (flag && !isdashboard) {
                        onBackPressed();
                    }
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                public void onProviderEnabled(String provider) {

                }

                public void onProviderDisabled(String provider) {

                }
            };
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(high.getName(), gpstimeinterval, gpsdistanceinterval, locationListener);
        }
        else
        {
            Toast.makeText(this, "Enable GPS", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, Gridmenu.class);
            startActivity(i);
        }
    }

    public void insertAudioLog(String type) {
        Calendar c = Calendar.getInstance();
        SQLiteDatabase db = null;
        try {
            db = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS insertAudioLog(lat DECIMAL(9,6),lng DECIMAL(9,6),type VARCHAR,timestamp DATETIME);");
            String Query = "INSERT INTO insertAudioLog(lat,lng ,type , timestamp) VALUES ('" + lat + "','" + lng + "','" + type + "','" + DateFormat.format("yyyy-MM-dd H:mm:ss", new Date(c.getTimeInMillis())).toString() + "');";
            db.execSQL(Query);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try { db.close(); } catch (Exception ignored) {}
            }
        }
        insertaudio();
    }

    public void insertaudio() {
        if (isNetworkAvailable()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SQLiteDatabase db = null;
                    Cursor cursor = null;
                    try {
                        db = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                        db.execSQL("CREATE TABLE IF NOT EXISTS insertAudioLog(lat DECIMAL(9,6),lng DECIMAL(9,6),type VARCHAR,timestamp DATETIME);");
                        String query = "SELECT distinct timestamp,lat,lng,type FROM insertAudioLog order by timestamp desc limit 100";
                        cursor = db.rawQuery(query, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            audiodata = "";
                            audiodateArray = new String[100];
                            cursor.moveToFirst();
                            int i = 0;
                            do {
                                audiodateArray[i] = cursor.getString(0);
                                i++;
                                audiodata += cursor.getString(0) + "@&" + cursor.getString(1) + "@&" + cursor.getString(2) + "@&" + cursor.getString(3) + "&@";
                            } while (cursor.moveToNext());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String result = insertAudioLogDb(audiodata);
                                        if (result.contains("Data Inserted successfully")) {
                                            String data = "delete from insertAudioLog where timestamp in (";
                                            for (int i = 0; i < audiodateArray.length; i++) {
                                                if (audiodateArray[i] != null) {
                                                    if (i != 0)
                                                        data = data + ",";
                                                    data = data + "'" + audiodateArray[i] + "'";
                                                } else
                                                    break;
                                            }
                                            data = data + ");";
                                            SQLiteDatabase deleteDb = null;
                                            try {
                                                deleteDb = openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                                deleteDb.execSQL(data);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                if (deleteDb != null) {
                                                    try { deleteDb.close(); } catch (Exception ignored) {}
                                                }
                                            }
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (cursor != null) {
                            try { cursor.close(); } catch (Exception ignored) {}
                        }
                        if (db != null) {
                            try { db.close(); } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
    }

    /**
     * this criteria needs high accuracy, high power, and cost
     */
    public static Criteria createFineCriteria() {

        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(false);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;

    }

    public String insertAudioLogDb(String logData) {
        String SOAP_ACTION = "http://tempuri.org/insertlogAudioData";
        String OPERATION_NAME = "insertlogAudioData";
        PropertyInfo[] piarray = new PropertyInfo[2];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("imei");
        pi.setValue(deviceid);
        pi.setType(String.class);
        piarray[0] = pi;

        pi = new PropertyInfo();
        pi.setName("logData");
        pi.setValue(logData);
        pi.setType(String.class);
        piarray[1] = pi;

        WebserviceCall webserviceCall = new WebserviceCall();
        return webserviceCall.callWebservice(Config.DOMAINURL1, Config.DOMAINURL2, OPERATION_NAME, piarray, SOAP_ACTION);
    }

    public String insertMobileGpsData(String device_id, String data) {

        String SOAP_ACTION = "http://tempuri.org/insertMobileGpsData";
        String OPERATION_NAME = "insertMobileGpsData";
        PropertyInfo[] piarray = new PropertyInfo[2];
        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[0] = pi;

        pi = new PropertyInfo();
        pi.setName("data");
        pi.setValue(data);
        pi.setType(String.class);
        piarray[1] = pi;

        WebserviceCall webserviceCall = new WebserviceCall();
        return webserviceCall.callWebservice(Config.DOMAINURL1, Config.DOMAINURL2, OPERATION_NAME, piarray, SOAP_ACTION);
    }
}

class DownloadFileFromURL extends AsyncTask<String, String, String> {
    private Context context;

    public DownloadFileFromURL() {
    }

    public DownloadFileFromURL(Context context) {
        this.context = context;
    }

    /**
     * Before starting background thread
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        System.out.println("Starting download");
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected String doInBackground(String... f_url) {
        int count;
        try {
            String filename = f_url[0].substring(f_url[0].lastIndexOf('/') + 1);
            filename = filename.substring(filename.indexOf('_') + 1);
            File baseDir = context != null ? context.getExternalFilesDir(null) : null;
            File dir = new File(baseDir != null ? baseDir : Environment.getExternalStorageDirectory(), "driverapp");
            File file = new File(dir, filename);
            System.out.println("Downloading");
            URL url = new URL(f_url[0]);

            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream to write file
            if (!file.exists() || lenghtOfFile != file.length()) {
                if (!dir.exists())
                    dir.mkdirs();
                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;

                    // writing data to file
                    output.write(data, 0, count);

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
            }
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }
    /**
     * After completing background task
     **/
    @Override
    protected void onPostExecute(String file_url) {
        System.out.println("Downloaded");

    }

}