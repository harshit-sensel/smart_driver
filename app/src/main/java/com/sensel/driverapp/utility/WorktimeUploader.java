package com.sensel.driverapp.utility;

import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
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

        Dbhelper DB;
        Log.e("uploading", "----------");
        DB=new Dbhelper(context);
        Cursor res=DB.getdata();
        if (res.getCount()==0){

           // WrokStartandWorkEnd.stopUploadBroadcast();

            Log.e("db","nodata");
        }
        else {
            try {
                String status="" ;
                String time="";
                JSONObject mainObj = new JSONObject();
                mainObj.put("IMEI",IMEINO);
   //             mainObj.put("IMEI","867498045562996");
                while (res.moveToNext()){
                    status=res.getString(0);
                    time=res.getString(1);

                }



                String uploadURL="";
                if(status.contains("startwork")){
                    mainObj.put("startTime",time);
                    uploadURL= Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/PostWorkStartData";
                }
                else {
                    mainObj.put("endTime",time);
                    uploadURL=Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/PostWorkEndData";
                }

                Log.e("object",mainObj.toString());

                RequestQueue requestQueue= Volley.newRequestQueue(context);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        uploadURL,
                        mainObj,

                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("response", response.toString());
                                String s=response.toString();
                                try {
                                    JSONObject jsonObj = new JSONObject(s);
                                    String name = jsonObj.getString("result");
                                    if(name.contains("Success") || name.contains("Vehicle not assigned")){
                                        DB.updateyes();
                                        DB.deletedata();

                                        //todo consider {} to delete
                                    }
                                } catch (JSONException e) {
                                    Log.e("ex",e.toString());
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

                requestQueue.add(jsonObjectRequest);

            }
            catch (Exception e){
                DB.updateno();

                Log.e("ex",e.toString());
            }
        }
    }


}
