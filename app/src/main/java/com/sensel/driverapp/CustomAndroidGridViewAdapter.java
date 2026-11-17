package com.sensel.driverapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sensel.driverapp.activity.DynamicFormActivity;
import com.sensel.driverapp.activity.ReportActivity;
import com.sensel.driverapp.activity.SyncActivity;
import com.sensel.driverapp.adapter.ZerofatilityAdaptor;
import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.utility.ToastHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Created by HP on 5/11/2016.
 */

public class CustomAndroidGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private Activity mActivity;
    private final String[] string;
    private final Integer[] Imageid;
    private ProgressDialog progressDialog;
    public static int worktime;
    public static String language;

    public CustomAndroidGridViewAdapter(Activity activity, Context c, String[] string, Integer[] Imageid) {
        mContext = c;
        mActivity = activity;
        this.Imageid = Imageid;
        this.string = string;
    }

    @Override
    public int getCount() {
        return string.length;
    }

    @Override
    public Object getItem(int p) {
        return null;
    }

    @Override
    public long getItemId(int p) {
        return 0;
    }

    @Override
    public View getView(int p, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        grid = new View(mContext);
        grid = inflater.inflate(R.layout.gridview_custom_layout, null);
        final TextView textView = (TextView) grid.findViewById(R.id.gridview_text);
        ImageView imageView = (ImageView) grid.findViewById(R.id.gridview_image);
        imageView.setId(p);
        textView.setText(string[p]);
        imageView.setImageResource(Imageid[p]);
        grid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                switch (textView.getText().toString()) {

                    case "eVet Checklist":// Daily Checklist
                        if (isNetworkAvailable()) {
                            //TODO go to activity
                            //String provider = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
                            //if (provider.contains("network") || provider.contains("gps")) {
                            if(isLocationAvailable()){
                                decideWhereToRedirect(false);
                            } else {
                                ToastHelper.showToast(mContext, "Please Turn On GPS");
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(intent);
                            }
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Sync eVet Data":// Daily Checklist
                        if (isNetworkAvailable()) {
                            Intent intent = new Intent(mContext, SyncActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Hospital"://Hospital
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "1";
                                // Map.Place = "hospital";
                                Map.title = "Nearest Hospitals";
                                ActivityLog("hospital");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;

                    case "ATM"://ATM
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "atm";
                                Map.title = "Nearest ATMs";
                                ActivityLog("atm");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Ambulance"://Ambulance
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "2";
                                Map.title = "Nearest Ambulances";
                                ActivityLog("ambulances");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Fire Brigade"://Fire Brigade
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "3";
                                Map.title = "Nearest Fire Station";
                                ActivityLog("fire station");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Puncture Shop"://Puncture Shop
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "4";
                                Map.title = "Nearest Puncture Shops";
                                ActivityLog("puncture shop");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Motor Garage"://Motor Garage
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "5";
                                Map.title = "Nearest Motor Garages";
                                ActivityLog("garages");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Police"://Police
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "9";
                                Map.title = "Nearest Police Stations";
                                ActivityLog("police station");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Cranes"://Cranes
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "10";
                                Map.title = "Nearest Cranes";
                                ActivityLog("cranes");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "JRM Route"://JRM Route
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                if (Gridmenu.points == null) {
                                    //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();

                                } else if (Gridmenu.points.length == 8) {
                                    Intent intent = new Intent(mContext, Map.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Map.Place = "route";
                                    Map.title = "JRM Route";
                                    ActivityLog("route");
                                    mContext.startActivity(intent);
                                } else {
                                    Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                                }


                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Emergency Vehicle"://Emergency Vehicle
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, Map.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "emergencyVehicles";
                                Map.title = "Emergency Vehicles";
                                ActivityLog("emergency vehicles");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Locate Vehicle"://Emergency Vehicle
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                Intent intent = new Intent(mContext, ERV_Driver.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Map.Place = "ERV";
                                Map.title = "Vehicle Position";
                                ActivityLog("ERV");
                                mContext.startActivity(intent);
                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Unload Quantity"://Unload Quantity

                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                Intent i = new Intent(mContext, Unload_QTY.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ActivityLog("Unload QTY");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();

                        break;
                    case "Incentive Points":// Incentive Points


                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                Intent i = new Intent(mContext, Driver_Incentives.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ActivityLog("Incentive Points");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Notifications":// Notifications
                        if (isNetworkAvailable()) {
                            Intent i = new Intent(mContext, Notification1.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            ActivityLog("Notification");
                            mContext.startActivity(i);
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Customer Feedback":// Customer Feedback
                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                Intent i = new Intent(mContext, Driver_Feedback.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                // ActivityLog("Customer Feedback");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Send Notification":// Send Notification

                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                Intent i = new Intent(mContext, NotifyTransporter.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ActivityLog("Send Notification");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();

                        break;
                    case "Vehicle Runtime":// Vehicle Runtime
                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                Intent i = new Intent(mContext, RunTime.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ActivityLog("Running Time");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Deviation Report":// Deviation Report
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                if (Gridmenu.points == null) {
                                    Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                                } else if (Gridmenu.points.length == 8) {
                                    Intent i = new Intent(mContext, DeviationReportMap.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ActivityLog("Deviation Report");
                                    mContext.startActivity(i);
                                } else {
                                    Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                                }

                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Daily Checklist":// Daily Checklist
                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                //Intent i = new Intent(mContext, Daily_Checklist.class);
                                Intent i = new Intent(mContext, Daily_Checklist1.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                ActivityLog("Daily Checklist");
                                mContext.startActivity(i);
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();


                        break;
                    case "Self Appraisal":// Self Appraisal
                        if (isNetworkAvailable()) {
                            if (Gridmenu.points == null) {
                                Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                            } else if (Gridmenu.points.length == 8) {
                                if (isLocationAvailable()) {
                                    Intent i = new Intent(mContext, DriverSelfAppraisal.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    ActivityLog("DriverSelfAppraisal");
                                    mContext.startActivity(i);
                                } else
                                    Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Driver Manual":// Driver Manual
                        if (isNetworkAvailable()) {
                            Intent i = new Intent(mContext, Driver_Manual_List.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(i);
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "See JRM Route":// See JRM Route
                        if (isNetworkAvailable()) {
                            Intent i = new Intent(mContext, See_Route.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(i);
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Taging":// Taging
                        if (isNetworkAvailable()) {
                            if (isLocationAvailable()) {
                                if (Gridmenu.points == null) {
                                    Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                                    //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                                } else if (Gridmenu.points.length == 8) {
                                    Intent i = new Intent(mContext, Taging.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(i);
                                } else {
                                    Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                                }

                            } else
                                Toast.makeText(mContext, "Enable GPS", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(mContext, "No internet", Toast.LENGTH_SHORT).show();
                        break;
                    case "Dashboard":// Dash Board
                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            LockScreenActivity.isdashboard = true;
                            Intent i = new Intent(mContext, LockScreenActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(i);
                        } else
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();

//                            LockScreenActivity.isdashboard = true;
//                            Intent i1 = new Intent(mContext, LockScreenActivity.class);
//                            i1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            mContext.startActivity(i1);
                        break;
                    case "QRScan":// QR Scan
                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            LockScreenActivity.isdashboard = true;
                            Intent i = new Intent(mContext, QRScanner.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(i);
                        } else
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        break;

                    case "ZERO Fatality":// ZERO Fatality
                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            if (isNetworkAvailable()) {
                                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                SharedPreferences pref = mContext.getSharedPreferences("savingtime", Activity.MODE_PRIVATE);
                                String greenlightday = pref.getString("Safety Green Light", "");
                                String lifesavinday = pref.getString("Life Saving", "");

                                Log.e("date", date);


                                final Dialog dialog = new Dialog(mActivity);
                                dialog.setContentView(R.layout.dialogue_fatality_option);
                                dialog.setCanceledOnTouchOutside(true);
                                dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT,
                                        WindowManager.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;

                                TextView txtgreencheck = dialog.findViewById(R.id.safetygreenlighttxt);
                                txtgreencheck.setVisibility(View.GONE);
                                Button btnsafety_green_check = dialog.findViewById(R.id.safety_green_check);
                                if (greenlightday.equals(date)) {
                                    btnsafety_green_check.setEnabled(false);
                                    btnsafety_green_check.setAlpha(.5f);
                                    txtgreencheck.setVisibility(View.VISIBLE);
                                }
                                btnsafety_green_check.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(mContext, Zerofatality.class);
                                        i.putExtra("checklist_type", "Safety Green Light");
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(i);
                                        dialog.dismiss();

                                    }
                                });
                                TextView lifesavingtxt = dialog.findViewById(R.id.lifesavingtxt);
                                lifesavingtxt.setVisibility(View.GONE);
                                Button btnlife_saving_check = dialog.findViewById(R.id.life_saving_check);
                                if (lifesavinday.equals(date)) {
                                    btnlife_saving_check.setEnabled(false);
                                    btnlife_saving_check.setAlpha(.5f);
                                    lifesavingtxt.setVisibility(View.VISIBLE);
                                }
                                btnlife_saving_check.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent i = new Intent(mContext, Zerofatality.class);
                                        i.putExtra("checklist_type", "Life Saving");
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(i);
                                        dialog.dismiss();

                                    }
                                });

                                dialog.show();

                            } else {

                                Toast.makeText(mContext, "Network not available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();
                        }


                        break;


                    case "Rest Timer":

                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            Intent iRest = new Intent(mContext, RestTime.class);
                            iRest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(iRest);
                        } else
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();


                        break;





                    case "Panic button":

                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            Intent panic = new Intent(mContext, PanicActivity.class);
                            panic.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(panic);
                        } else
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_SHORT).show();

                        break;

                    case "Journey Plan":
                        Intent journey = new Intent(mContext, JourneyPlanner.class);
                        journey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(journey);
                        break;


                    case "Start & End Work":


                        if (Gridmenu.points == null) {
                            Toast.makeText(mContext, "Please restart app", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(mContext, "Could not find vehicle id, please restart app", Toast.LENGTH_SHORT).show();
                        } else if (Gridmenu.points.length == 8) {
                            if (isNetworkAvailable()) {
                                JSONObject obj = new JSONObject();
                                try {
                                    obj.put("IMEI", Gridmenu.device_id);
                                    //obj.put("IMEI", "f56755ee86b3c829");
                                    Log.e("json", obj.toString());

                                } catch (Exception e) {
                                }


                                final ProgressDialog progressDialog = new ProgressDialog(mActivity);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage("Loading Please wait...");
                                progressDialog.show();

                                RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                                        Request.Method.POST,
                                        Config.DOMAINURL1 + "/SenselRestService.svc/rest/v3/GetWorkLimit",
                                        obj,
                                        new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {

                                                try {
                                                    Log.e("response", response.toString());

                                                    worktime = response.getInt("dutylimit");
                                                    language = response.getString("preferedlanguage");
                                                    SharedPreferences sharedPref = mContext.getSharedPreferences("WORKSTARTEND", mContext.MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putInt("workinghrs", worktime);
                                                    editor.apply();

                                                    // Save language separately in "lang_setting"
                                                    SharedPreferences langPref = mContext.getSharedPreferences("lang_setting", mContext.MODE_PRIVATE);
                                                    SharedPreferences.Editor langEditor = langPref.edit();
                                                    langEditor.putString("my_lang", language);
                                                    langEditor.apply();

                                                    Log.e("woktime", String.valueOf(worktime));
                                                    progressDialog.dismiss();


                                                    Intent i = new Intent(mContext, WrokStartandWorkEnd.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    mContext.startActivity(i);
                                                } catch (Exception e) {
                                                    String error;
                                                    error = e.toString();
                                                    progressDialog.dismiss();
                                                    Toast.makeText(mContext, "Something went wrong on getting your working hours" + error, Toast.LENGTH_LONG).show();  //j
                                                    Log.e("ex", e.toString());

                                                }
                                            }
                                        },
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Log.e("error", error.toString());
                                                String errormessage;
                                                try {
                                                    errormessage = error.toString();
                                                    Toast.makeText(mContext, "Something went wrong on getting your working hours" + errormessage, Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(mContext, "Something went wrong on getting your working hours", Toast.LENGTH_LONG).show();
                                                }


                                                progressDialog.dismiss();

                                            }
                                        }

                                );
                                requestQueue.add(jsonObjectRequest);

                            } else {
                                SharedPreferences pref = mContext.getSharedPreferences("WORKSTARTEND", mActivity.MODE_PRIVATE);
                                worktime = pref.getInt("workinghrs", 0);
                                //worktime=pref.getInt("workinghrs",720);
                                if (worktime != 0 && worktime > 0) {
                                    Intent i = new Intent(mContext, WrokStartandWorkEnd.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(i);
                                } else {
                                    Toast.makeText(mContext, "First time internet is required", Toast.LENGTH_SHORT).show();
                                }
                            }

                        } else {
                            Toast.makeText(mContext, "No vehicle assigned", Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        });
        return grid;
    }
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isLocationAvailable() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            progressDialog.setMessage("Slow loading, please wait...");
        }
    };

    private void ActivityLog(final String activity) {
        GPSTracker gpsTracker = new GPSTracker(mContext);
        final String[] latlng = gpsTracker.getLocation().split(",");
        Thread activityThread = new Thread() {
            @Override
            public void run() {
                try {
                    WebserviceCall webserviceCall = new WebserviceCall();
                    webserviceCall.KeepActivityLog(Gridmenu.device_id, activity, latlng[0], latlng[1]);
                } catch (Exception e) {
                }
            }
        };
        activityThread.start();
    }

    private void decideWhereToRedirect(boolean isRedirectingToReport) {
        //check if we have multiple configurations
        List<LoginConfigs> configs = loadOfflineLoginConfigs();
        if (isRedirectingToReport) {
            //for reports
            if (configs != null && configs.size() > 0) {
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (configs.size() == 1) {
                    intent.putExtra("APP_NAME", configs.get(0));
                    mContext.startActivity(intent);
                } else {
                    //show options to select from
                    showSelectionDialog(configs, isRedirectingToReport);
                }
            } else {
                showConfigurationNotAvailableDialog();
            }
        } else {
            //for new vetting
            if (configs != null && configs.size() > 0) {
                if (configs.size() == 1) {
                    Intent intent = new Intent(mContext, DynamicFormActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("APP_NAME", configs.get(0));
                    mContext.startActivity(intent);
                } else {
                    //show options to select from
                    showSelectionDialog(configs, isRedirectingToReport);
                }
            } else {
                showConfigurationNotAvailableDialog();
            }
        }
    }

    private void showSelectionDialog(List<LoginConfigs> configs, boolean isRedirectingToReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Choose Configuration");

        String[] options = new String[configs.size()];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            List<String> names = configs.stream().map(LoginConfigs::getName).collect(Collectors.toList());
            options = names.stream().toArray(String[]::new);
        } else {
            for (int i = 0; i < configs.size(); i++) {
                options[i] = configs.get(i).getName();
            }
        }

        builder.setItems(options, (dialog, which) -> {
            LoginConfigs configSelected = configs.get(which); //selected item
            if (isRedirectingToReport) {
                //send to report activity
                Intent intent = new Intent(mContext, ReportActivity.class);
                intent.putExtra("APP_NAME", configSelected);
                mContext.startActivity(intent);
            }
            /*else if (configSelected.getName().equals(CONST_CONFIG_REGULAR)) {
                //send to regular activity
                mContext.startActivity(new Intent(mContext, DetailsActivity.class));
            }*/
            else {
                //send to dynamic activity
                Intent intent = new Intent(mContext, DynamicFormActivity.class);
                intent.putExtra("APP_NAME", configSelected);
                mContext.startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showConfigurationNotAvailableDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Configuration Not Available");
        builder.setMessage("No application configured, please contact your ADMIN team.");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            //onBackPressed();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<LoginConfigs> loadOfflineLoginConfigs() {
        AppDatabase database = AppDatabase.getDatabase(mContext.getApplicationContext());
        return database.loginConfigModel().loadAllConfigs();
    }

}