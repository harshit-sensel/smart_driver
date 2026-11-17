package com.sensel.driverapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.sensel.driverapp.adapter.ParkingSpotAdaptor;
import com.sensel.driverapp.adapter.SelectedParkingSpotAdaptor;
import com.sensel.driverapp.objectclass.ParkingSpots;
import com.sensel.driverapp.objectclass.SelectedParingSpotObject;
import com.sensel.driverapp.utility.JourneyPlanDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ParkingSpotSelect extends AppCompatActivity {
    private Button finish_button, confirmbutton, closebutton,btn_view;
    private TextView txt_4hrs, txt_10hrs, txt_spotname;
    private FloatingActionButton addbutton;
    private ParkingSpotAdaptor adaptor;
    private Dialog confirm_dialog;
    private ListView l;
    public static Activity activity;
    public static BottomSheetDialog bottomSheetDialog, bottomMapsheet;
    public TextInputLayout resttime, E_arrivalDateTime;
    public static FragmentManager fragmentManager;
    public static ArrayList<SelectedParingSpotObject> spotObjectArrayList;
    public static ImageView placeholder;
    public RecyclerView recyclerViewparking;
    public static SelectedParkingSpotAdaptor parkingadaptor;
    public static ArrayList<ParkingSpots> parkingSpotsArrayList;
    ProgressDialog progress;
    JourneyPlanDB journeyPlanDB;

    public static SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");

    Date prev_date = JourneyPlanner.START_DATE, nightStart, nightEnd, selectArrival;
    SimpleDateFormat nightFormat = new SimpleDateFormat("hh:mm:ss aa");

    Date prev_DepartDate, nine_hrsDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_spot_select);
        addbutton = (FloatingActionButton) findViewById(R.id.add);
        placeholder = (ImageView) findViewById(R.id.placeholder);
        finish_button = findViewById(R.id.finish);
        btn_view=findViewById(R.id.btn_viewplan);
//        btn_view_plan = (Button) findViewById(R.id.viewplan);
//
//        btn_view_plan.setVisibility(View.GONE);

        prev_DepartDate = JourneyPlanner.START_DATE;
        prev_DepartDate = JourneyPlanner.START_DATE;

        btn_view.setVisibility(View.GONE);



        journeyPlanDB = new JourneyPlanDB(getApplicationContext());


        try {
            nightStart = nightFormat.parse(JourneyPlanner.NIGHT_START_TIME);
            nightEnd = nightFormat.parse(JourneyPlanner.NIGHT_END_TIME);
        } catch (ParseException e) {
            Log.e("e", e.toString());
            e.printStackTrace();
        }

        if(JourneyPlanner.PLAN_STATUS.equals("Approved")){
            finish_button.setVisibility(View.GONE);
            addbutton.setVisibility(View.GONE);
        }


        parkingSpotsArrayList = new ArrayList<>();

        activity = this;
        fragmentManager = getSupportFragmentManager();

        spotObjectArrayList = new ArrayList<>();


        recyclerViewparking = (RecyclerView) findViewById(R.id.selectedspots);
        recyclerViewparking.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        parkingadaptor = new SelectedParkingSpotAdaptor(spotObjectArrayList);
        recyclerViewparking.setAdapter(parkingadaptor);

        adaptor = new ParkingSpotAdaptor(parkingSpotsArrayList);

        // updateSelectedList();
        getAddedParkingSpot();
        getParkingSpots();

        addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showConfirmDialog();
                showBottomSheetParkingspot(v);
            }
        });

        btn_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });


        finish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (spotObjectArrayList.toString().contains(JourneyPlanner.END_LOC + " DESTINATION") || spotObjectArrayList.toString().contains(JourneyPlanner.START_LOC + " DESTINATION")) {

//                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
//                    startActivity(intent);
                    uploadParkingSpots();


                } else {
                    Toast.makeText(getApplicationContext(), activity.getString(R.string.selectendlocation), Toast.LENGTH_SHORT).show();
                }


                //showRoutePlay(v, details);

            }
        });

    }

    private void uploadParkingSpots() {

        progress = ProgressDialog.show(ParkingSpotSelect.this, null, null, true);
        progress.setContentView(R.layout.progressbar);
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject upload = new JSONObject();

        JSONArray data = new JSONArray();

        try {
            for (int i = 0; i < spotObjectArrayList.size(); i++) {


                JSONObject jsonObject = new JSONObject();
                String latlong[];
                latlong = spotObjectArrayList.get(i).getLatLan().split(",");
                Log.e("latlong", spotObjectArrayList.get(i).getLatLan());

                jsonObject.put("Location", spotObjectArrayList.get(i).getParkingName());
                jsonObject.put("Latitude", latlong[0]);
                jsonObject.put("Longitude", latlong[1]);
                jsonObject.put("EntryETA", sdf.format(spotObjectArrayList.get(i).getEstimatedArrival()));
                jsonObject.put("EntryATA", spotObjectArrayList.get(i).getEntryATA());
                jsonObject.put("ExitETA", sdf.format(spotObjectArrayList.get(i).getEstimatedLeavingTime()));
                jsonObject.put("ExitATA", spotObjectArrayList.get(i).getEXITATA());
                jsonObject.put("EstRestDuration", spotObjectArrayList.get(i).getRestTime());
                jsonObject.put("ActRestDuration", spotObjectArrayList.get(i).getActualRestTime());
                jsonObject.put("IsCompleted", spotObjectArrayList.get(i).getisEditable());

                data.put(jsonObject);


            }

            upload.put("tripid", JourneyPlanner.TRIP_ID);
            upload.put("triptype", JourneyPlanner.TRIP_TYPE);
            upload.put("tripstarttime", sdf.format(JourneyPlanner.START_DATE));
            upload.put("addparkingspots", data);

            Log.e("UPLOAD", upload.toString());


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/InsertJMPParkingLocationDetails",
                    upload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("RESP", response.toString());
                            try {
                                if (response.getString("statuscode").contains("200")) {

                                    journeyPlanDB.deletedata();
                                    journeyPlanDB.insertdata(spotObjectArrayList);
                                    progress.dismiss();
                                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                    startActivity(intent);
                                } else if (response.getString("statuscode").contains("500")) {
                                    progress.dismiss();
                                    Error_alert(response.getString("result"), response.getString("statuscode"));
                                } else {
                                    progress.dismiss();
                                    Error_alert(response.getString("result"), response.getString("statuscode"));
                                }
                            } catch (Exception e) {
                                progress.dismiss();
                                Error_alert(e.toString(), "Error");
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
            Error_alert(e.toString(), "Error");
        }

        journeyPlanDB.deletedata();
        journeyPlanDB.insertdata(spotObjectArrayList);


    }


    private void showRoutePlay(View v, String details) {
        Log.e("details", details);

        bottomSheetDialog = new BottomSheetDialog(v.getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_route_show);

        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bottomSheetDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        WebView webView = bottomSheetDialog.findViewById(R.id.mapwebview);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {


                webView.loadUrl("javascript:multipleParkingShow('" + details + "')");
                Log.e("called", "called");


            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        //settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        JavaScriptInterface jsInterface = new JavaScriptInterface(ParkingSpotSelect.activity, webView, activity.getApplicationContext());
        webView.addJavascriptInterface(jsInterface, "JSInterface");

        webView.loadUrl("file:///android_asset/map.html");

        bottomSheetDialog.show();
    }


    public void updateSelectedList() {
        Log.e("called", "updateSelectedList()");
        if (spotObjectArrayList.size() > 0) {
            placeholder.setVisibility(View.GONE);

        }


    }


    private void showBottomSheetParkingspot(View v) {

        bottomSheetDialog = new BottomSheetDialog(v.getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_parkinglist);

        RecyclerView recyclerView = (RecyclerView) bottomSheetDialog.findViewById(R.id.recyclerView_parkingspot);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity.getApplicationContext()));
        recyclerView.setAdapter(adaptor);
        bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        bottomSheetDialog.show();
    }

    public void showConfirmDialog(String spotName, String details) {
        long runtimeHrs=0;
        long totalRest=0;
        long totalruntime=0;
        bottomSheetDialog.dismiss();
        confirm_dialog = new Dialog(activity);
        confirm_dialog.setContentView(R.layout.dialog_spot_confirm);

        resttime = (TextInputLayout) confirm_dialog.findViewById(R.id.resttime);

        E_arrivalDateTime = (TextInputLayout) confirm_dialog.findViewById(R.id.arrivaldate);


        if (spotName.equals(JourneyPlanner.END_LOC + " DESTINATION") || spotName.equals(JourneyPlanner.START_LOC + " DESTINATION")) {
            resttime.setVisibility(View.GONE);
        }


        txt_4hrs = confirm_dialog.findViewById(R.id.next4hr);
        txt_10hrs = confirm_dialog.findViewById(R.id.next10hr);
        txt_spotname = confirm_dialog.findViewById(R.id.spotname);

        confirmbutton = confirm_dialog.findViewById(R.id.confirm);
        closebutton = confirm_dialog.findViewById(R.id.close);


        try {
            nightStart = nightFormat.parse(JourneyPlanner.NIGHT_START_TIME);
            nightEnd = nightFormat.parse(JourneyPlanner.NIGHT_END_TIME);
        } catch (ParseException e) {
            Log.e("e", e.toString());
            e.printStackTrace();
        }


        prev_DepartDate = JourneyPlanner.START_DATE;
        nine_hrsDate = JourneyPlanner.START_DATE;

        Log.e("LONGREST", String.valueOf(JourneyPlanner.LONG_REST));

        if (spotObjectArrayList.size() > 0) {
            prev_DepartDate = spotObjectArrayList.get(spotObjectArrayList.size() - 1).getEstimatedLeavingTime();
            for (int i = spotObjectArrayList.size() - 1; i >= 0; i--) {
                if (spotObjectArrayList.get(i).getRestTime() >= JourneyPlanner.LONG_REST) {
                    nine_hrsDate = spotObjectArrayList.get(i).getEstimatedLeavingTime();
                    totalRest= (long) (totalRest+spotObjectArrayList.get(i).getRestTime());
                    totalruntime = 0;
                    break;
                } else {
                    nine_hrsDate = JourneyPlanner.START_DATE;
                    totalRest=0;
                    if(i == 0)
                    {
                        totalruntime += (spotObjectArrayList.get(i).getEstimatedArrival().getTime() - JourneyPlanner.START_DATE.getTime());
                    }
                    else
                    {
                        totalruntime += (spotObjectArrayList.get(i).getEstimatedArrival().getTime() - spotObjectArrayList.get(i-1).getEstimatedLeavingTime().getTime());
                    }
                }


            }

        }

        Calendar nineHRS = Calendar.getInstance();
        nineHRS.setTime(nine_hrsDate);
        nineHRS.add(Calendar.MINUTE, JourneyPlanner.LONG_REST.intValue());

        Calendar fourHRS = Calendar.getInstance();
        fourHRS.setTime(prev_DepartDate);
        fourHRS.add(Calendar.MINUTE, JourneyPlanner.CONT_DRIVING.intValue());

        long diff = nine_hrsDate.getTime() - JourneyPlanner.START_DATE.getTime();

        runtimeHrs = TimeUnit.MILLISECONDS.toHours(diff);
        runtimeHrs=runtimeHrs-totalRest;

        txt_10hrs.setText("Total runtime :" + TimeUnit.MILLISECONDS.toHours(totalruntime)+" hours " + (TimeUnit.MILLISECONDS.toMinutes(totalruntime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalruntime))) + " minutes");
        txt_4hrs.setText("Next 30 min rest :" + DateFormat.format("dd-MM-yyyy hh:mm aa", fourHRS));
        txt_spotname.setText(spotName);


        try {
            nightStart = nightFormat.parse(JourneyPlanner.NIGHT_START_TIME);
            nightEnd = nightFormat.parse(JourneyPlanner.NIGHT_END_TIME);
        } catch (ParseException e) {
            Log.e("e", e.toString());
            e.printStackTrace();
        }


        //arrivaltime = (TextInputLayout) confirm_dialog.findViewById(R.id.reachtime);


        long finalRuntimeHrs = totalruntime;
        E_arrivalDateTime.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(prev_DepartDate);

                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                                String dateString = format.format(calendar.getTime());
                                //E_arrivalDateTime.getEditText().setText(dateString);

                                TimePickerDialog timePickerDialog = new TimePickerDialog(
                                        activity,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.set(0, 0, 0, hourOfDay, minute);
                                                Log.e("time", dateString + DateFormat.format("hh:mm aa", calendar));
                                                Date arrival, arriavalTime;


                                                try {
                                                    arriavalTime = nightFormat.parse(DateFormat.format("hh:mm:ss aa", calendar).toString());
                                                    arrival = sdf.parse(dateString + " " + DateFormat.format("hh:mm aa", calendar));

                                                    if (arrival.after(prev_DepartDate)) {

                                                        // Log.e("new error", validateDate(arrival, prev_DepartDate, nine_hrsDate));
                                                        String error = validateDate(arrival, prev_DepartDate, nine_hrsDate, finalRuntimeHrs);
                                                        if (error.equals("none")) {
                                                            if (arriavalTime.after(nightStart) || arriavalTime.before(nightEnd)) {
                                                                String description = nightFormat.format(nightStart) + " to " + nightFormat.format(nightEnd) + activity.getString(R.string.nightdescription);
                                                                showDialog(activity.getString(R.string.nightdrivealert), description);
                                                                E_arrivalDateTime.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                                E_arrivalDateTime.setError("");
                                                            } else {
                                                                E_arrivalDateTime.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                                E_arrivalDateTime.setError("");
                                                            }
                                                            selectArrival = sdf.parse(dateString + " " + DateFormat.format("hh:mm aa", calendar));

                                                        } else {
                                                            E_arrivalDateTime.setError(error);
                                                            E_arrivalDateTime.getEditText().setText("");
                                                        }

                                                    } else {
                                                        Log.e("4", "log");
                                                        E_arrivalDateTime.getEditText().setText("");
                                                        E_arrivalDateTime.setError(activity.getString(R.string.selectafterprevtime));
                                                    }
                                                } catch (ParseException e) {
                                                    Log.e("1", e.toString());
                                                    e.printStackTrace();
                                                }


                                            }
                                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                                );

                                timePickerDialog.show();
                            }
                        }, calendar.get(Calendar.YEAR), Calendar.MONTH, Calendar.DAY_OF_MONTH);

                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();


            }
        });

        resttime.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (E_arrivalDateTime.getEditText().getText().toString().length() < 5) {
                    E_arrivalDateTime.setError(activity.getString(R.string.selectarrivaltime));
                    E_arrivalDateTime.getEditText().setText("");
                    return;
                }


                Calendar calendar = Calendar.getInstance();
                calendar.setTime(selectArrival);
                calendar.add(Calendar.MINUTE, JourneyPlanner.SHORT_REST.intValue());

                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.set(year, monthOfYear, dayOfMonth);
                                @SuppressLint("SimpleDateFormat")
                                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                                String dateString = format.format(calendar.getTime());
                                //E_arrivalDateTime.getEditText().setText(dateString);

                                TimePickerDialog timePickerDialog = new TimePickerDialog(
                                        activity,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.set(0, 0, 0, hourOfDay, minute);
                                                Log.e("time", dateString + DateFormat.format("hh:mm aa", calendar));

                                                Date arrival, leaving;

                                                try {
                                                    arrival = sdf.parse(E_arrivalDateTime.getEditText().getText().toString());
                                                    leaving = sdf.parse(dateString + " " + DateFormat.format("hh:mm aa", calendar));

                                                    if (arrival.after(leaving)) {
                                                        resttime.setError(activity.getString(R.string.shouldafterarrival));
                                                        resttime.getEditText().setText("");
                                                        return;
                                                    } else {

                                                        long difference_In_Time = leaving.getTime() - arrival.getTime();
                                                        long difference_In_Minutes = (difference_In_Time / 1000) / 60;

                                                        long difference_InBetween_ParkingRun_Time = arrival.getTime() - prev_DepartDate.getTime();
                                                        long totalfinalruntime = ((finalRuntimeHrs + difference_InBetween_ParkingRun_Time) / 1000) / 60;

                                                        if (totalfinalruntime == JourneyPlanner.MAX_DRIVING) {
                                                            if (difference_In_Minutes < JourneyPlanner.LONG_REST) {
                                                                resttime.getEditText().setText("");
                                                                resttime.setError("Need to take 9hrs rest");
                                                                return;

                                                            } else {
                                                                resttime.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                                resttime.setError("");
                                                            }
                                                        }
                                                        else
                                                        {
                                                            if (difference_In_Minutes < JourneyPlanner.SHORT_REST) {
                                                                resttime.getEditText().setText("");
                                                                resttime.setError(activity.getString(R.string.min30restrequired));
                                                                return;

                                                            } else {
                                                                resttime.getEditText().setText(dateString + " " + DateFormat.format("hh:mm aa", calendar));
                                                                resttime.setError("");
                                                            }
                                                        }







                                                    }

                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                                );

                                timePickerDialog.show();
                            }
                        }, calendar.get(Calendar.YEAR), Calendar.MONTH, Calendar.DAY_OF_MONTH);

                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                datePickerDialog.show();

            }
        });


        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm_dialog.dismiss();
            }
        });
        confirmbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String EstDateTime = E_arrivalDateTime.getEditText().getText().toString();
                String RestTime = resttime.getEditText().getText().toString();

                Date arrival, leaving;
                String validation;
                try {
                    if (spotName.equals(JourneyPlanner.END_LOC + " DESTINATION") || spotName.equals(JourneyPlanner.START_LOC + " DESTINATION")) {
                        arrival = sdf.parse(E_arrivalDateTime.getEditText().getText().toString());
                        if (E_arrivalDateTime.getEditText().getText().toString().length() < 5) {
                            E_arrivalDateTime.setError(activity.getString(R.string.selectarrivaltime));
                            E_arrivalDateTime.getEditText().setText("");
                            return;
                        } else {


                            SelectedParingSpotObject temp = new SelectedParingSpotObject(ParkingSpotAdaptor.selectedParking.getSpotName(), arrival, arrival, "Not arrived", "false", ParkingSpotAdaptor.selectedParking.getLatlan(), 00, 00, "", "", "No");
                            spotObjectArrayList.add(temp);
                            parkingadaptor.notifyDataSetChanged();
                            updateSelectedList();
                            confirm_dialog.dismiss();
                        }
                    } else {
                        arrival = sdf.parse(E_arrivalDateTime.getEditText().getText().toString());
                        leaving = sdf.parse(resttime.getEditText().getText().toString());
                        if (E_arrivalDateTime.getEditText().getText().toString().length() < 5) {
                            E_arrivalDateTime.setError(activity.getString(R.string.selectarrival));
                            E_arrivalDateTime.getEditText().setText("");
                            return;
                        } else if (resttime.getEditText().getText().toString().length() < 5) {
                            resttime.getEditText().setText("");
                            resttime.setError(activity.getString(R.string.enterrestdate));
                            return;
                        } else {
                            long restDuration;
                            restDuration = leaving.getTime() - arrival.getTime();
                            restDuration = (restDuration / 1000) / 60;
                            //String parkingName, Date estimatedArrival, Date estimatedLeavingTime, String actualArrival, String isEditable, String latLan, float restTime, float actualRestTime, String entryATA, String EXITATA, String isCompleted
                            SelectedParingSpotObject temp = new SelectedParingSpotObject(ParkingSpotAdaptor.selectedParking.getSpotName(), arrival, leaving, "Not arrived", "false", ParkingSpotAdaptor.selectedParking.getLatlan(), restDuration, 00, "", "", "No");
                            spotObjectArrayList.add(temp);
                            parkingadaptor.notifyDataSetChanged();
                            updateSelectedList();
                            confirm_dialog.dismiss();
                        }

                    }


                } catch (ParseException e) {
                    showDialog("Error", e.toString());
                    e.printStackTrace();
                }


            }
        });

        WebView webView = confirm_dialog.findViewById(R.id.mapwebview);
        webView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {


                webView.loadUrl("javascript:confirmSingleSpotView('" + details + "')");
                Log.e("called", "called");


            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        //settings.setAppCacheEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        JavaScriptInterface jsInterface = new JavaScriptInterface(ParkingSpotSelect.activity, webView, activity.getApplicationContext());
        webView.addJavascriptInterface(jsInterface, "JSInterface");

        webView.loadUrl("file:///android_asset/map.html");
        confirm_dialog.setCanceledOnTouchOutside(false);
        confirm_dialog.setCancelable(false);
        confirm_dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        confirm_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        confirm_dialog.getWindow().setGravity(Gravity.CENTER);
        confirm_dialog.show();


    }

    private String validateDate(Date arrival, Date prev_departDate, Date nine_hrsDate,long runTimehrs) {

        String error = "none";

        if (arrival.after(prev_departDate)) {
            long difference_In_Time = arrival.getTime() - prev_departDate.getTime();
            long difference_In_Minutes = (difference_In_Time / 1000) / 60;

            long totalruntimeinminutes = runTimehrs / 1000 /60;
            if(totalruntimeinminutes < JourneyPlanner.MAX_DRIVING)
            {
                if (difference_In_Minutes > JourneyPlanner.CONT_DRIVING) {
                    return activity.getString(R.string.exceeds4hrs);
                }
            }
            /*
            difference_In_Time = arrival.getTime() - nine_hrsDate.getTime();
            difference_In_Minutes = (difference_In_Time / 1000) / 60;
            */

            difference_In_Minutes = ((runTimehrs + difference_In_Time) / 1000) / 60;

            if (difference_In_Minutes > JourneyPlanner.MAX_DRIVING) {
                return activity.getString(R.string.exceed10hrs);
            }

        } else {
            return activity.getString(R.string.shouldafter) + prev_departDate.toString();
        }


        return error;
    }


    private void getParkingSpots() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("RouteId", JourneyPlanner.ROUTE_ID);
            jsonObject.put("TripType", JourneyPlanner.TRIP_TYPE);
            jsonObject.put("tripid", JourneyPlanner.TRIP_ID);
        } catch (Exception e) {
        }


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Log.e("BODY", jsonObject.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                "https://fleetsmart3.ui.sensel.in/SenselRestService.svc/rest/v3/GetJMPParkingDetails",
                jsonObject,
                //"https://api.json-generator.com/templates/YwJPzUnKbGEm/data?access_token=epqpuw0ry1jhhfigqs35osbqm7a0hx5cvcl3tlqs",
                //null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("resp", response.toString());
                        progress.dismiss();
                        parkingSpotsArrayList.clear();
                        try {
                            JSONArray jsonArray = response.getJSONArray("JMPParkingDetails");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                ParkingSpots parkingSpots = new ParkingSpots(object.getString("Location"), object.getString("latitude") + "," + object.getString("longitude"));
                                parkingSpotsArrayList.add(parkingSpots);
                            }
                            ParkingSpots parkingSpots = new ParkingSpots(JourneyPlanner.END_LOC + " DESTINATION", JourneyPlanner.DEST_LATLANG);
                            parkingSpotsArrayList.add(parkingSpots);
//                            if (JourneyPlanner.TRIP_TYPE.contains("DOWN")) {
//                                ParkingSpots parkingSpots = new ParkingSpots(JourneyPlanner.START_LOC + " DESTINATION", JourneyPlanner.DEST_LATLANG);
//                                parkingSpotsArrayList.add(parkingSpots);
//                            } else {
//                                ParkingSpots parkingSpots = new ParkingSpots(JourneyPlanner.END_LOC + " DESTINATION", JourneyPlanner.DEST_LATLANG);
//                                parkingSpotsArrayList.add(parkingSpots);
//                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adaptor.notifyDataSetChanged();
                                }
                            });

                        } catch (Exception e) {
                            Log.e("ex", e.toString());

                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("err", error.toString());

                    }
                });

        requestQueue.add(jsonObjectRequest);

    }

    private void getAddedParkingSpot() {
        progress = ProgressDialog.show(ParkingSpotSelect.this, null, null, true);
        progress.setContentView(R.layout.progressbar);
        progress.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Cursor cursor = journeyPlanDB.getdata();
        if (cursor.getCount() > 0) {
            btn_view.setVisibility(View.VISIBLE);
//            btn_view_plan.setVisibility(View.VISIBLE);
            while (cursor.moveToNext()) {
                cursor.getString(0);
                cursor.getString(1);
                Date eta, ela;
                try {
                    eta = sdf.parse(cursor.getString(2));
                    ela = sdf.parse(cursor.getString(3));
                    SelectedParingSpotObject temp = new SelectedParingSpotObject(cursor.getString(1), eta, ela, "######", "no", cursor.getString(5), cursor.getLong(4), 00, "", "", cursor.getString(6));
                    spotObjectArrayList.add(temp);
                    parkingadaptor.notifyDataSetChanged();
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
        }



//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
//                "https://api.json-generator.com/templates/0S67SktYkWlO/data?access_token=epqpuw0ry1jhhfigqs35osbqm7a0hx5cvcl3tlqs",
//                null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        progress.dismiss();
//                        spotObjectArrayList.clear();
//                        Log.e("resp", response.toString());
//                        parkingSpotsArrayList.clear();
//                        try {
//                            JSONArray jsonArray = response.getJSONArray("addedparkingSpots");
//                            Date eta, ata;
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject object = jsonArray.getJSONObject(i);
//                                eta = sdf.parse(object.getString("eta"));
////                                SelectedParingSpotObject temp = new SelectedParingSpotObject(object.getString("name"), eta, object.getString("ata"), object.getString("isComplete"), object.getLong("restTime"), object.getString("latlon"));
////                                spotObjectArrayList.add(temp);
////                                parkingadaptor.notifyDataSetChanged();
//                                //todo rest api consideration
//                            }
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    adaptor.notifyDataSetChanged();
//                                    updateSelectedList();
//                                }
//                            });
//
//                        } catch (Exception e) {
//                            Log.e("ex", e.toString());
//
//                        }
//
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("err", error.toString());
//                        progress.dismiss();
//
//                    }
//                });
//
//        requestQueue.add(jsonObjectRequest);


    }

//    private void ETA_validation(Date arrival, Date leaving) {
//        Date selectedDateTime;
//        Date leavingDateTime;
//        Date lastSelectedDatetime, nineHrsRestDateTime;
//        long restDuration;
//
//        lastSelectedDatetime = JourneyPlanner.START_DATE;
//        selectedDateTime = arrival;
//        leavingDateTime = leaving;
//
//        restDuration = leavingDateTime.getTime() - selectedDateTime.getTime();
//        restDuration = (restDuration / 1000) / 60;
//
//
//        if ((spotObjectArrayList.size() <= 0)) {
//            if (selectedDateTime.after(JourneyPlanner.START_DATE)) {
//                long difference_In_Time = selectedDateTime.getTime() - JourneyPlanner.START_DATE.getTime();
//                long difference_In_Minutes = (difference_In_Time / 1000) / 60;
//                Log.e("di", String.valueOf(difference_In_Minutes));
//                if (difference_In_Minutes > 240) {
//                    //todo change to max driving
//                    E_arrivalDateTime.setError("Continuous driving is 4 hours");
//                    E_arrivalDateTime.getEditText().setText("");
//                } else {
//                    SelectedParingSpotObject temp = new SelectedParingSpotObject(ParkingSpotAdaptor.selectedParking.getSpotName(), selectedDateTime, leavingDateTime, "Not arrived", "false", restDuration, ParkingSpotAdaptor.selectedParking.getLan());
//                    spotObjectArrayList.add(temp);
//                    parkingadaptor.notifyDataSetChanged();
//                    updateSelectedList();
//                    confirm_dialog.dismiss();
//                }
//            } else {
//                E_arrivalDateTime.setError("Date should be after start time");
//                E_arrivalDateTime.getEditText().setText("");
//            }
//
//
//        } else {
//            lastSelectedDatetime = spotObjectArrayList.get(spotObjectArrayList.size() - 1).getEstimatedLeavingTime();
//            if (selectedDateTime.after(lastSelectedDatetime)) {
//                nineHrsRestDateTime = JourneyPlanner.START_DATE;
//                for (int i = 0; i <= spotObjectArrayList.size() - 1; i++) {
//
//                    if (spotObjectArrayList.get(i).getRestTime() >= 540) {
//                        nineHrsRestDateTime = spotObjectArrayList.get(i).getEstimatedLeavingTime();
//
//                    } else {
//                        nineHrsRestDateTime = JourneyPlanner.START_DATE;
//                    }
//
//                }
//
//                long difference_In_Time = selectedDateTime.getTime() - nineHrsRestDateTime.getTime();
//                long difference_In_Minutes = (difference_In_Time / 1000) / 60;
//                Log.e("di", String.valueOf(difference_In_Minutes));
//
//                if (difference_In_Minutes >= 600) {
//                    E_arrivalDateTime.setError("FULL REST REQUIRED");
//                    E_arrivalDateTime.getEditText().setText("");
//                } else {
//                    difference_In_Time = selectedDateTime.getTime() - lastSelectedDatetime.getTime();
//                    difference_In_Minutes = (difference_In_Time / 1000) / 60;
//                    if (difference_In_Minutes > 240) {
//                        //todo change to max driving
//                        E_arrivalDateTime.setError("4 hrs required");
//                        E_arrivalDateTime.getEditText().setText("");
//                    } else {
//                        SelectedParingSpotObject temp = new SelectedParingSpotObject(ParkingSpotAdaptor.selectedParking.getSpotName(), selectedDateTime, leavingDateTime, "Not arrived", "false", restDuration, ParkingSpotAdaptor.selectedParking.getLan());
//                        spotObjectArrayList.add(temp);
//                        parkingadaptor.notifyDataSetChanged();
//                        updateSelectedList();
//                        confirm_dialog.dismiss();
//                    }
//                }
//            } else {
//                E_arrivalDateTime.setError("Date should be after ");
//                E_arrivalDateTime.getEditText().setText("");
//            }
//
//
//        }
//
//    }


    private void Error_alert(String errormessage, String title) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final AlertDialog ad = new AlertDialog.Builder(ParkingSpotSelect.this).create();
                ad.setTitle(title);
                ad.setCancelable(false);
                ad.setMessage(errormessage);
                ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });


    }

    public void showDialog(String Error, String description) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);


        builder1.setTitle(Error);
        builder1.setMessage(description);
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();

    }
}
