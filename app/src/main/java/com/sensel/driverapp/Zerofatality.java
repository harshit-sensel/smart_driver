package com.sensel.driverapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.sensel.driverapp.adapter.ZerofatilityAdaptor;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.request.CheckListRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Zerofatality extends AppCompatActivity {
    public RecyclerView recyclerView;
    static boolean executed=false;
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    public ZerofatilityAdaptor adaptor;
    public ArrayList<String> ruleid;
    public ArrayList<String> rulename;
    public ArrayList<String> color;
    public ArrayList<String> listtoupload;
    public String check_type;
    public String result;
    public Context context;
    public static String imei;
    public  String languagepref;
    public LanguageSet languageSet;
    String URL=Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/GetSglChecklist";
    String URL_UPLOAD=Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/insertSglCheckList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zerofatality);
        ActionBar actionBar = getSupportActionBar();

        context=getApplicationContext();

        check_type=getIntent().getStringExtra("checklist_type");
        imei=getIMEI();
        //imei="861478047179607";
        Log.e("imei",imei);
        //json object to get the checklist
        JSONObject obj = new JSONObject();
        try {
            obj.put("appId", "2");
            obj.put("imei",imei.toString() );
            obj.put("chklistname", check_type.toString());
        }

        catch (Exception e){ }

        //action bar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(check_type.toUpperCase());

        if(isNetworkAvailable()){
            final ProgressDialog progressDialog = new ProgressDialog(Zerofatality.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Please wait...");
            progressDialog.show();
            RequestQueue requestQueue=Volley.newRequestQueue(this);
            JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(
                    Request.Method.POST,
                    URL,
                    obj,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.e("Json response", "onResponse: "+response.toString());
                            result=response.toString();
                            if(result.isEmpty()||result.length()==0){
                                Toast.makeText(context,"FAILED TO GET CHECKLIST ERROR CODE Z001",Toast.LENGTH_LONG).show();
                                finish();
                            }

                            else {
                                CreateReport();
                                recyclerView = (RecyclerView) findViewById(R.id.zerofatility_recycler);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                adaptor = new ZerofatilityAdaptor(getApplicationContext(), rulename, color);
                                recyclerView.setAdapter(adaptor);
                                progressDialog.dismiss();

                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error",error.toString());
                            Toast.makeText(getApplicationContext(),"Something went wrong or network issue",Toast.LENGTH_LONG).show();
                            finish();

                        }
                    }
            );
            requestQueue.add(jsonArrayRequest);

        }
        else {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    new ContextThemeWrapper(Zerofatality.this, android.R.style.Theme_Holo_Light_Dialog));
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
        }
    }


    public void CreateReport(){
        try {
            ruleid= new ArrayList<>();
            rulename=new ArrayList<>();
            color=new ArrayList<>();
            final JSONArray jArr = new JSONArray(result);
            if(jArr.length()>0){
                for (int j = 0; j <= jArr.length()-1; j++) {
                try {
                    JSONObject data = jArr.getJSONObject(j);
                    ruleid.add(data.getString("ruleid"));
                    rulename.add(data.getString("rulename"));
                    color.add(data.getString("color"));
                    languagepref=(data.getString("LanguagePrefered"));

                } catch (JSONException js) {
                    Log.e("Exception",js.toString());
                    Toast.makeText(context,"FAILED TO GET CHECKLIST ERROR CODE Z002",Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            }
            else{
                Toast.makeText(context,"FAILED TO GET CHECKLIST ERROR CODE Z006",Toast.LENGTH_LONG).show();
                finish();
            }
        }
        catch (JSONException e){
            Toast.makeText(context,"FAILED TO GET CHECKLIST ERROR CODE Z003",Toast.LENGTH_LONG).show();
            finish();
        }


       languageSet=new LanguageSet();
        try {
            if(!languageSet.whichlanguage(languagepref.substring(languagepref.indexOf("-")+1)).equals(Gridmenu.LANGUGE)){
                Log.e("language code",languageSet.whichlanguage(languagepref.substring(languagepref.indexOf("-")+1)).toString());
                languageSet.setLocate(languageSet.whichlanguage(languagepref.substring(languagepref.indexOf("-")+1)),getBaseContext(),getApplicationContext());
            }

        }
        catch (Exception e){ }

    }
    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                showAlertDialog();
                break;
            case R.id.menu_add:

                if (adaptor.result.contains("nill")){
                    showAlertDialog_not_filled();

                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        //deprecated in API 26
                        v.vibrate(100);
                    }
                    Toast.makeText(getApplicationContext(),"PLease check all fields",Toast.LENGTH_LONG).show();
                }
                else {
                    if(isNetworkAvailable()){
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Zerofatality.this);
                        alertDialogBuilder.setTitle("Checklist ");
                        alertDialogBuilder.setMessage(R.string.alert_continue)
                                .setCancelable(false)
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Submit",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                               upload();
                                                dialog.cancel();
                                            }
                                        });

                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();
                    }
                    else {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                new ContextThemeWrapper(Zerofatality.this, android.R.style.Theme_Holo_Light_Dialog));
                        alertDialogBuilder.setIcon(R.drawable.error);
                        alertDialogBuilder.setTitle("No Network connection");
                        alertDialogBuilder.setMessage("Check your internet connections")
                                .setCancelable(false)
                                .setPositiveButton("Ok", (dialog, id) -> {
                                    dialog.cancel();
                                });
                        AlertDialog alert = alertDialogBuilder.create();
                        alert.show();
                    }
                    break;
                }

        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onBackPressed() {
        showAlertDialog();
    }

    public void showAlertDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_alert);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button btnYes = dialog.findViewById(R.id.btnYes);
        Button btnNo = dialog.findViewById(R.id.btnNo);
        btnYes.setOnClickListener(v -> {
            finish();
            dialog.dismiss();
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public void showAlertDialog_not_filled() {
        final Dialog dialog = new Dialog(this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialogue_checklist_error);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        Button btnNo = dialog.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

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
            String android_id= Settings.Secure.getString(this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id =android_id;
            }
            return device_id;
        }
    }

    public void upload(){
        Integer i;
        JSONObject mainObj = new JSONObject();
        try {
            //Creating a json array
            JSONArray ja = new JSONArray();

            for (i=0;i<ruleid.size();i++){
                JSONObject jo = new JSONObject();
                jo.put("RuleId", ruleid.get(i).toString());
                jo.put("RuleValue",adaptor.result.get(i));
                ja.put(jo);

            }

            mainObj.put("imei",imei);
            mainObj.put("ChecklistName",check_type);
            mainObj.put("checklistValues", ja);



            Log.e("json upload", mainObj.toString());
            final ProgressDialog progressDialog = new ProgressDialog(Zerofatality.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Please wait...");
            progressDialog.show();

            RequestQueue requestQueue=Volley.newRequestQueue(this);
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(
                    Request.Method.POST,
                    URL_UPLOAD,
                    mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {


                            try{
                                String apiResponse=response.getString("result");
                                if(apiResponse.contains("Successfully")){
                                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                    String currentdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("savingtime",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(check_type, date);
                                    Log.e("date", date);
                                    Boolean commit=editor.commit();
                                    while (!commit){
                                        commit=editor.commit();
                                        Log.e("commt","called");
                                    }
                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Zerofatality.this);
                                    alertDialogBuilder.setIcon(R.drawable.success);
                                    alertDialogBuilder.setTitle("Success ");
                                    alertDialogBuilder.setMessage("Reported successfully on" + currentdate)
                                            .setCancelable(false)
                                            .setPositiveButton("Ok",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.cancel();
                                                            finish();
                                                        }
                                                    });
                                    AlertDialog alert = alertDialogBuilder.create();
                                    alert.show();

                                    progressDialog.dismiss();

                                }

                                else {
                                    progressDialog.dismiss();
                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Zerofatality.this);
                                    alertDialogBuilder.setIcon(R.drawable.success);
                                    alertDialogBuilder.setTitle("Failed");
                                    alertDialogBuilder.setMessage("Failed to upload please try again")
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
                            }
                            catch (Exception e){
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("erroe",error.toString());
                            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Zerofatality.this);
                            alertDialogBuilder.setIcon(R.drawable.ic_error);
                            alertDialogBuilder.setTitle("Error");
                            alertDialogBuilder.setMessage("Something went wrong please try again")
                                    .setCancelable(false)
                                    .setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });
                            AlertDialog alert = alertDialogBuilder.create();
                            alert.show();
                            progressDialog.dismiss();
                        }
                    }
            );
            requestQueue.add(jsonObjectRequest);
        }
        catch (Exception e){
            Log.e("json error",e.toString());
            Toast.makeText(getApplicationContext(),"something went wrong please try again",Toast.LENGTH_LONG).show();
        }
    }
}