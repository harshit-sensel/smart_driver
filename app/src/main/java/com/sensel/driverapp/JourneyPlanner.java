package com.sensel.driverapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.sensel.driverapp.objectclass.RootWeather;
import com.sensel.driverapp.objectclass.weather.City;
import com.sensel.driverapp.objectclass.weather.Clouds;
import com.sensel.driverapp.objectclass.weather.Coord;
import com.sensel.driverapp.objectclass.weather.Sys;
import com.sensel.driverapp.objectclass.weather.WList;
import com.sensel.driverapp.objectclass.weather.Weather;
import com.sensel.driverapp.objectclass.weather.WeatherObject;
import com.sensel.driverapp.objectclass.weather.Wind;
import com.sensel.driverapp.service.Download_LatLong;
import com.sensel.driverapp.utility.JourneyPlanDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class JourneyPlanner extends AppCompatActivity implements OnMapReadyCallback {

    TextInputLayout txt_startdate;
    TextView txt_vehicle, txt_driver, txt_start, txt_end, txt_status;
    Button submit;
    FragmentManager fragmentManager;
    Activity activity;
    ProgressDialog progress;
    String isDateseditable = "no", prev_tripId = "";

    public static boolean TRIP_EDITABLE = false;
    public static Date START_DATE;
    public static String START_LOC, END_LOC, NIGHT_START_TIME, NIGHT_END_TIME, TRIP_ID = "nill", TRIP_TYPE, ROUTE_ID = "nill", SRC_LATLANG = "00,00", DEST_LATLANG = "00,00", PLAN_STATUS;
    public static Long CONT_DRIVING, MAX_DRIVING, SHORT_REST, LONG_REST;
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");

    SimpleDateFormat nightFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
    Date selectDate = null;
    public static final String PREFERENCES = "journeyplan";
    public static final String PREFKEY = "tripid";
    public static final String PREF_LATLONG = "srcdest";
    SharedPreferences sharedpreferences;

    Date d1 = null, d2 = null;

    //map variable
    private GoogleMap mMap;
    private ArrayList<LatLng> latLngArrayList;
    private JourneyPlanDB journeyPlanDB;
    private Cursor cursor;
    private RequestQueue mQueue;

    private String polylineColor = "nill";

    int count, count1 = 0;
    Marker mPositionMarker;
    public static final String REQUEST_TAG = "WEATHERAPIREQUEST1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply language before loading layout
        String lang = getSharedPreferences("lang_setting", MODE_PRIVATE).getString("my_lang", "en");
        String langCode = getLangCodeFromServerValue(lang);
        setLocate(langCode);

        setContentView(R.layout.activity_journey_planner);

        JourneyPlanDB journeyPlanDB = new JourneyPlanDB(getApplicationContext());
        submit = (Button) findViewById(R.id.next);
        txt_startdate = (TextInputLayout) findViewById(R.id.startdate);
        //txt_starttime = (TextInputLayout) findViewById(R.id.starttime);
        txt_driver = (TextView) findViewById(R.id.driver);
        txt_start = (TextView) findViewById(R.id.startlocation);
        txt_end = (TextView) findViewById(R.id.endlocation);
        txt_vehicle = (TextView) findViewById(R.id.vehicle);
        txt_status = (TextView) findViewById(R.id.txt_status);


        NIGHT_START_TIME = "08:00 pm";
        NIGHT_END_TIME = "06:00 am";

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        activity = this;
        fragmentManager = getSupportFragmentManager();


        sharedpreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        prev_tripId = sharedpreferences.getString(PREFKEY, "nill");

        String templatlon[] = sharedpreferences.getString(PREF_LATLONG, "00,00##00,00").split("##");
        SRC_LATLANG = templatlon[0];
        DEST_LATLANG = templatlon[1];

        Log.e(SRC_LATLANG, DEST_LATLANG);


        progress = ProgressDialog.show(JourneyPlanner.this, null, null, true);
        progress.setContentView(R.layout.progressbar);
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


        getTripdetails();


//
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
//                Request.Method.POST,
//                "https://api.json-generator.com/templates/YwURTZynpVND/data?access_token=epqpuw0ry1jhhfigqs35osbqm7a0hx5cvcl3tlqs",
//                null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        try {
//                            String result = response.getString("isTrip");
//                            if (result.contains("yes")) {
//                                getTripdetails();
//                            } else {
//                                progress.dismiss();
//                                Error_alert("No trip assigned", "Trip not found");
//                            }
//                        } catch (JSONException e) {
//                            progress.dismiss();
//                            Error_alert(e.toString(), "Error");
//                            e.printStackTrace();
//                        }
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        progress.dismiss();
//                        Error_alert(error.toString(), "Error");
//
//                    }
//                }
//        );
//
//        requestQueue.add(jsonObjectRequest);


        //get the current date to set date picker
        Calendar calendar = Calendar.getInstance();

        txt_startdate.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDateseditable.contains("Yes")) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(JourneyPlanner.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.set(year, monthOfYear, dayOfMonth);
                                    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                                    String dateString = format.format(calendar.getTime());


                                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                                            activity,
                                            new TimePickerDialog.OnTimeSetListener() {
                                                @Override
                                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                    Calendar calendar = Calendar.getInstance();
                                                    calendar.set(0, 0, 0, hourOfDay, minute);
                                                    txt_startdate.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                    try {
                                                        selectDate = sdf.parse(txt_startdate.getEditText().getText().toString());
                                                        Date currDate = new Date();
                                                        if (selectDate.after(currDate)) {
                                                            START_DATE = selectDate;
                                                            txt_startdate.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                            txt_startdate.setError("");

                                                        } else {
                                                            txt_startdate.getEditText().setText("");
                                                            txt_startdate.setError(getString(R.string.greaterthancurrenttime));
                                                        }
                                                    } catch (ParseException e) {
                                                        Error_alert(e.toString(), "Error");
                                                    }


                                                }
                                            }, 12, 00, false
                                    );

                                    timePickerDialog.show();
                                }
                            }, calendar.get(Calendar.YEAR), Calendar.MONTH, Calendar.DAY_OF_MONTH);

                    datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                    datePickerDialog.show();
                } else {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), getString(R.string.cannotedited), Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (txt_startdate.getEditText().getText().toString().length() < 5) {
                    Toast.makeText(getApplicationContext(), getString(R.string.selectvaliddate), Toast.LENGTH_SHORT).show();

                } else {
                    Intent intent = new Intent(getApplicationContext(), ParkingSpotSelect.class);
                    startActivity(intent);

                }


            }
        });


    }


//    private void ShowJRMRoute(String start, String end) {
//        String route = start + "," + end;
//        //for showing jrm route
//        WebView mywebview = (WebView) findViewById(R.id.journey_webview);
//
//        mywebview.setWebViewClient(
//                new WebViewClient() {
//                    @Override
//                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                        view.loadUrl(url);
//                        return true;
//                    }
//                });
//        mywebview.setWebChromeClient(new WebChromeClient() {
//            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
//                callback.invoke(origin, true, false);
//            }
//        });
//        mywebview.setWebViewClient(new WebViewClient() {
//
//            public void onPageFinished(WebView view, String url) {
//                mywebview.loadUrl("javascript:getRoutejourneyPlanner('" + route + "')");
//            }
//        });
//
//        WebSettings settings = mywebview.getSettings();
//        settings.setJavaScriptEnabled(true);
//        settings.setDomStorageEnabled(true);
//        settings.setAllowFileAccess(true);
//        settings.setAppCacheEnabled(true);
//
//        JavaScriptInterface jsInterface = new JavaScriptInterface(this, mywebview, getApplicationContext());
//        mywebview.addJavascriptInterface(jsInterface, "JSInterface");
//
//        mywebview.loadUrl("file:///android_asset/map.html");
//    }

    private void getTripdetails() {


        JSONObject jsonObject = new JSONObject();
        try {

            jsonObject.put("IMEI", Gridmenu.device_id);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/GetJMPTripsDetails",
                    jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("RESP", response.toString());
                            progress.dismiss();
                            try {
                                TRIP_ID = response.getString("TripId");

                                if (response.has("Language")) {
                                    String tripLang = response.getString("Language");
                                    String tripLangCode = getLangCodeFromServerValue(tripLang);
                                    SharedPreferences.Editor langEditor = getSharedPreferences("lang_setting", MODE_PRIVATE).edit();
                                    langEditor.putString("my_lang", tripLangCode);
                                    langEditor.apply();
                                }


                                if (!TRIP_ID.contains("No")) {
                                    TRIP_TYPE = response.getString("TripType");

                                    if (!prev_tripId.equals(TRIP_ID + TRIP_TYPE)) {
                                        Log.e(prev_tripId, TRIP_ID + TRIP_TYPE);

                                        Intent serviceIntent = new Intent(getApplicationContext(), Download_LatLong.class);
                                        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString(PREFKEY, TRIP_ID + TRIP_TYPE);
                                        editor.commit();
                                    }


                                    NIGHT_START_TIME = response.getString("NightDrivingStart");
                                    NIGHT_END_TIME = response.getString("NightDrivingEnd");
                                    END_LOC = response.getString("EndtLocation");
                                    START_LOC = response.getString("StartLocation");
                                    MAX_DRIVING = response.getLong("MaxDrivingDuration");
                                    CONT_DRIVING = response.getLong("ContinuousDrivingDuration");
                                    SHORT_REST = response.getLong("RestTimeDuration");
                                    ROUTE_ID = response.getString("RouteId");


                                    LONG_REST = 540L;

//
//                                    d1 = nightFormat.parse(NIGHT_START_TIME);
//                                    d2 = nightFormat.parse(NIGHT_END_TIME);
//
//                                    long diff = d1.getTime() - d2.getTime();
//                                    Log.e("MILLI", String.valueOf(diff));
//                                    LONG_REST = (diff / 1000) / 60;
//                                    Log.e("MIN",String.valueOf(LONG_REST));
                                    //LONG_REST = TimeUnit.MILLISECONDS.toMinutes(diff);

                                    isDateseditable = response.getString("IsStartTimeEditable");

                                    if (response.getString("IsTripEditable").equals("Yes")) {
                                        TRIP_EDITABLE = true;
                                    }

                                    if (!response.getString("TripStart").equals("")) {

                                        try {

                                            START_DATE = nightFormat.parse(response.getString("TripStart"));
                                            selectDate = START_DATE;
                                            txt_startdate.getEditText().setText(nightFormat.format(START_DATE));
                                        } catch (Exception e) {
                                            Error_alert(e.toString(), "Error");
                                        }
                                    } else {
                                        txt_startdate.getEditText().setText("");
                                    }

                                    //     ShowJRMRoute(START_LOC, END_LOC);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {

                                                txt_driver.setText(response.getString("Driver"));
                                                txt_vehicle.setText(response.getString("Vehicle"));
                                                txt_start.setText(START_LOC);
                                                txt_end.setText(END_LOC);

                                                PLAN_STATUS = response.getString("TripPlanStatus");

                                                if (PLAN_STATUS.equals("")) {

                                                    txt_status.setText("No plan available");
                                                    txt_status.setTextColor(Color.BLUE);
                                                } else if (PLAN_STATUS.equals("Pending")) {

                                                    txt_status.setText("Plan pending for approval");
                                                    txt_status.setTextColor(Color.parseColor("#FF0000"));
                                                } else if (PLAN_STATUS.equals("Rejected")) {

                                                    journeyPlanDB.deletedata();
                                                    txt_status.setText("Plan rejected");
                                                    txt_status.setTextColor(Color.parseColor("#80AA0000"));
                                                } else if (PLAN_STATUS.equals("Approved")) {

                                                    txt_status.setText("Plan Approved");
                                                    txt_status.setTextColor(Color.parseColor("#FF145C03"));
                                                } else {
                                                    txt_status.setText(PLAN_STATUS);

                                                }


                                            } catch (JSONException e) {
                                                Error_alert(e.toString(), "Error");
                                                e.printStackTrace();
                                            }

                                        }
                                    });
                                } else {

                                    journeyPlanDB.deleteLatLongs();
                                    journeyPlanDB.deletedata();
                                    Error_alert(getString(R.string.notripassigned), getString(R.string.notrip));
                                }


                            } catch (JSONException e) {
                                progress.dismiss();
                                Error_alert(e.toString(), "Error");
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progress.dismiss();
                            Error_alert(error.toString(), "Error");

                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);


        } catch (Exception e) {
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
                return "en";
        }
    }

    public void setLocate(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }

    private void Error_alert(String errormessage, String title) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final AlertDialog ad = new AlertDialog.Builder(JourneyPlanner.this).create();
                ad.setTitle(title);
                ad.setCancelable(false);
                ad.setMessage(errormessage);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                ad.show();
            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        journeyPlanDB = new JourneyPlanDB(getApplicationContext());
        latLngArrayList = new ArrayList<>();


        count = 1;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cursor = journeyPlanDB.getLatLong();

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {

                        if (cursor.isFirst()) {
                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));

                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(temp)
                                            .title(JourneyPlanner.START_LOC)
                                            .snippet(" ")
                                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_round_greenloc)));

                        }
                        if (cursor.isLast()) {
                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));

                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(temp)
                                            .title(JourneyPlanner.END_LOC)
                                            .snippet(" ")
                                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_flag)));

                        }

                        if (count % 100 == 0) {
                            startParsingTask(cursor.getString(0), cursor.getString(1));
                        }
                        count++;
                        if (polylineColor.equals(cursor.getString(2))) {
                            polylineColor = cursor.getString(2);


                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));
                            latLngArrayList.add(temp);
                        } else {
                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));
                            latLngArrayList.add(temp);
                            drawPolyline(polylineColor, latLngArrayList);
                            polylineColor = cursor.getString(2);
                            latLngArrayList.clear();
                        }

                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngArrayList.get(0), 10));
                    drawPolyline(polylineColor, latLngArrayList);
                }
            }
        });


    }


    private void drawPolyline(String polylineColor, ArrayList<LatLng> latLngArrayList) {
        PolylineOptions polyOptions;
        polyOptions = new PolylineOptions();
        int color;
        color = Color.BLUE;

        if (polylineColor.equals("Red")) {
            color = Color.RED;
        } else if (polylineColor.equals("Blue")) {
            color = Color.BLUE;
        } else if (polylineColor.equals("Yellow")) {
            color = Color.YELLOW;
        } else if (polylineColor.equals("Green")) {
            color = Color.GREEN;
        } else {
            color = Color.BLUE;
        }

        polyOptions.color(color);

        polyOptions.width(20);
        polyOptions.addAll(latLngArrayList);
        mMap.addPolyline(polyOptions);
    }


    public void startParsingTask(String lat, String lon) {

        mQueue = Volley.newRequestQueue(this);
        String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&exclude=current,minutely,hourly&units=metric&appid=4b406562a5c50b1f44bcfbbeb35795af";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            ArrayList<WList> list;
                            list = new ArrayList<>();

                            JSONArray j_lst = response.getJSONArray("list");
                            JSONObject j_city = response.getJSONObject("city");
                            JSONObject j_coord = j_city.getJSONObject("coord");

                            for (int i = 0; i < j_lst.length(); i++) {
                                JSONObject jsonObject = j_lst.getJSONObject(i);
                                JSONObject j_main = jsonObject.getJSONObject("main");


                                WeatherObject weatherObject = new WeatherObject(j_main.getDouble("temp"), j_main.getDouble("feels_like"), j_main.getDouble("temp_min"), j_main.getDouble("temp_max"),
                                        j_main.getInt("pressure"), j_main.getInt("sea_level"), j_main.getInt("grnd_level"), j_main.getInt("humidity"),
                                        j_main.getDouble("temp_kf"));

                                JSONArray jarray_weather = jsonObject.getJSONArray("weather");

                                ArrayList<Weather> weatherArrayList = new ArrayList<>();

                                for (int j = 0; j < jarray_weather.length(); j++) {
                                    JSONObject j_weather = jarray_weather.getJSONObject(j);

                                    Weather weather = new Weather(j_weather.getInt("id"), j_weather.getString("main"), j_weather.getString("description"), j_weather.getString("icon"));
                                    weatherArrayList.add(weather);
                                }

                                JSONObject j_clouds = jsonObject.getJSONObject("clouds");

                                Clouds clouds = new Clouds(j_clouds.getInt("all"));

                                JSONObject j_winds = jsonObject.getJSONObject("wind");

                                Wind wind = new Wind(j_winds.getDouble("speed"), j_winds.getInt("deg"), j_winds.getDouble("gust"));

                                JSONObject j_sys = jsonObject.getJSONObject("sys");

                                Sys sys = new Sys(j_sys.getString("pod"));


                                WList wList = new WList(jsonObject.getInt("dt"), weatherObject, weatherArrayList, clouds, wind, jsonObject.getInt("visibility"), jsonObject.getDouble("pop"), sys, jsonObject.getString("dt_txt"));
                                list.add(wList);
                            }


                            Coord coord = new Coord(j_coord.getDouble("lat"), j_coord.getDouble("lon"));

                            LatLng latLng = new LatLng(coord.lat, coord.lon);
                            String lat[] = Gridmenu.LATLONG.split(",");
                            LatLng userlatlong = new LatLng(Double.valueOf(lat[0]), Double.valueOf(lat[1]));


                            City city = new City(j_city.getInt("id"), j_city.getString("name"), coord, j_city.getString("country"),
                                    j_city.getInt("population"), j_city.getInt("timezone"), j_city.getInt("sunrise"), j_city.getInt("sunset"));


                            RootWeather rootWeather = new RootWeather(response.getString("cod"), response.getInt("message"), response.getInt("cnt"), list, city);

                            ArrayList<Date> dateArrayList = new ArrayList<>();
                            Log.e("test", "3");
                            for (WList l : list) {
                                dateArrayList.add(MapsActivity.sdf.parse(l.dt_txt));
                            }

                            Log.e("test", "1");


                            Date nextdate = calculateDistanceInKilometer(userlatlong, latLng);

                            Log.e("test", "2");

                            int index = getNearestDate(dateArrayList, nextdate);
                            Log.e("NEARDATE", String.valueOf(index));

                            int resid = R.drawable.ic_w_clearsky;
                            Log.e("WEATHER", rootWeather.getList().get(0).weather.get(0).description);

                            if (rootWeather.getList().get(index).weather.get(0).description.contains("clear sky")) {
                                resid = R.drawable.ic_w_clearsky;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("few clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(0).weather.get(0).description.contains("scattered clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("broken clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("shower rain")) {
                                resid = R.drawable.ic_w_rain1;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("rain")) {
                                resid = R.drawable.ic_w_rain1;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("thunderstorm")) {
                                resid = R.drawable.ic_w_thunderstorm;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("snow")) {
                                resid = R.drawable.ic_w_snowflake;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("mist")) {
                                resid = R.drawable.ic_w_snowflake;
                            }

                            String weather = rootWeather.getList().get(index).weather.get(0).description
                                    + "\nFeels like " + rootWeather.getList().get(index).weatherObject.feels_like
                                    + "\nTemperature " + rootWeather.getList().get(index).weatherObject.temp
                                    + "\ntemp min " + rootWeather.getList().get(index).weatherObject.temp_min
                                    + "\ntemp max" + rootWeather.getList().get(index).weatherObject.temp_max;


                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(latLng)
                                            .title(rootWeather.getList().get(index).weather.get(0).main)
                                            .snippet(weather)
                                            .icon(bitmapDescriptorFromVector(getApplicationContext(), resid)));

                        } catch (Exception e) {

                            Log.e("ERROR", e.toString());

                        }


                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonObjectRequest.setTag(REQUEST_TAG);
        mQueue.add(jsonObjectRequest);

        mMap.setInfoWindowAdapter(new JourneyPlanner.MyInfoWindowAdapter());


    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static int getNearestDate(ArrayList<Date> dates, Date currentDate) {
        int index = 0, i = 0;
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {

            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                index = i;

                minDiff = diff;
                minDate = date;
            }
            i++;
        }
        return index;
    }


    public Date calculateDistanceInKilometer(LatLng source, LatLng destination) {
        long currenttime = System.currentTimeMillis();


        double AVERAGE_RADIUS_OF_EARTH_KM = 6371;


        double parkLat = 0, parkLng = 0, latDistance, lngDistance, a, c;
        int distance = 0;
        int tempdistance = 9999;

        parkLat = destination.latitude;
        parkLng = destination.longitude;

        latDistance = Math.toRadians(source.latitude - parkLat);
        lngDistance = Math.toRadians(source.longitude - parkLng);

        a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(source.latitude)) * Math.cos(Math.toRadians(parkLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
        Log.e("DISTANCE:", String.valueOf(distance));

        int timeoftravel = (int) distance / 45;


        Date date = new Date();
        date.setTime(date.getTime() + timeoftravel * 3600000);


        return date;


    }

    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.map_info_window, null);
        }


        @Override
        public View getInfoWindow(Marker marker) {
            TextView tvInfo = ((TextView) myContentsView.findViewById(R.id.mapinfo));
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.parkingname));
            tvInfo.setText(marker.getSnippet());
            tvTitle.setText(marker.getTitle());

            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }
}