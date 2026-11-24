package com.sensel.driverapp.utility;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.sensel.driverapp.Config;
import com.sensel.driverapp.WrokStartandWorkEnd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WorktimeUploader extends BroadcastReceiver {
    public static String Restresponse;
    public static String IMEINO;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("uploading", "----------");
        uploadNext(context);
    }

    private void uploadNext(final Context context) {
        final Dbhelper DB = new Dbhelper(context);
        Cursor res = DB.getdata();
        if (res == null || res.getCount() == 0) {
            if (res != null) {
                res.close();
            }
            Log.e("db", "nodata");
            return;
        }

        try {
            String status = "";
            String time = "";
            while (res.moveToNext()) {
                status = res.getString(0);
                time = res.getString(1);
            }
            res.close();

            String imei = IMEINO;
            if (imei == null || imei.trim().isEmpty()) {
                imei = getDeviceIMEI(context);
            }

            JSONObject mainObj = new JSONObject();
            mainObj.put("IMEI", imei);

            String uploadURL = "";
            if (status.contains("startwork")) {
                mainObj.put("startTime", time);
                uploadURL = Config.DOMAINURL1 + "/SenselRestService.svc/rest/v3/PostWorkStartData";
            } else {
                mainObj.put("endTime", time);
                uploadURL = Config.DOMAINURL1 + "/SenselRestService.svc/rest/v3/PostWorkEndData";
            }

            Log.e("object", mainObj.toString());

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    uploadURL,
                    mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("response", response.toString());
                            try {
                                String name = response.getString("result");
                                if (name.contains("Success") || name.contains("Vehicle not assigned")) {
                                    DB.updateyes();
                                    DB.deletedata();
                                    // Successfully uploaded and deleted, process the next pending row
                                    uploadNext(context);
                                }
                            } catch (JSONException e) {
                                Log.e("ex", e.toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", error.toString());
                        }
                    }
            );

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(jsonObjectRequest);

        } catch (Exception e) {
            DB.updateno();
            Log.e("ex", e.toString());
        }
    }

    private String getDeviceIMEI(Context context) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            try {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    String device_id = tm.getDeviceId();
                    if (device_id != null && !device_id.isEmpty()) {
                        return device_id;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return android_id != null ? android_id : "";
    }
}
