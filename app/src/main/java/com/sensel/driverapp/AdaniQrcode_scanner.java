package com.sensel.driverapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
//import com.budiyev.android.codescanner.CodeScanner;
//import com.budiyev.android.codescanner.CodeScannerView;
//import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class AdaniQrcode_scanner extends AppCompatActivity {
    /*private CodeScanner mCodeScanner;
    Context context;
    private static final int MY_PERMISSION_CAMERA = 104;
    private FusedLocationProviderClient fusedLocationClient;
    public static String latlong="00.0000,00.0000";
    Dialog message_dialog;
//    SharedPreferences pref;
//    SharedPreferences.Editor editor;
//    private final String sharedprename="ADANI_QR";
//    private final String sharedprefkey="vehQr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adani_qrcode_scanner);
        context = getApplicationContext();

//         pref = context.getSharedPreferences(sharedprename, MODE_PRIVATE);
//         editor = pref.edit();

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

        else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions

                return;
            }


            if (ContextCompat.checkSelfPermission(AdaniQrcode_scanner.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AdaniQrcode_scanner.this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }

            else {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    latlong=String.valueOf(location.getLatitude())+","+String.valueOf(location.getLongitude());
                                    Qrcodescanner();

                                }
                            }
                        });


            }

        }

    }

    private void Qrcodescanner() {

        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.startPreview();

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        if(result.getText().contains("http://www.sensel.in/?VEH_")){
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {

                                v.vibrate(100);
                            }

//                            String prevvehicle=pref.getString(sharedprefkey, "first");

//                            if((prevvehicle.equals(result.getText()))){
//
//                                AlertDialog.Builder builder1 = new AlertDialog.Builder(AdaniQrcode_scanner.this);
//                                builder1.setMessage("Info");
//                                builder1.setTitle("Already done");
//                                builder1.setIcon(R.drawable.ic_checked);
//
//                                builder1.setCancelable(false);
//
//                                builder1.setPositiveButton(
//                                        "OK",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int id) {
//                                                dialog.cancel();
//                                                finish();
//                                            }
//                                        });
//
//
//
//                                AlertDialog alert11 = builder1.create();
//                                alert11.show();
//
//
//
//                            }

//                            else {

                                final ProgressDialog progressDialog = new ProgressDialog(AdaniQrcode_scanner.this);
                                progressDialog.setCancelable(false);
                                progressDialog.setMessage("Loading Please wait...");
                                progressDialog.show();
                                JSONObject mainObj = new JSONObject();


                                try {
                                    mainObj.put("Vehilceid",result.getText());
                                    mainObj.put("IMEI",Gridmenu.device_id);
                                    mainObj.put("latlong",latlong);

                                    Log.e("ob",mainObj.toString());

                                    RequestQueue requestQueue= Volley.newRequestQueue(context);
                                    JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(
                                            Request.Method.POST,
                                            Config.DOMAINURL1+"/SenselRestService.svc/rest/v3/PostTagingData",
                                            mainObj,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {

                                                    Log.e("resp",response.toString());
                                                    try {
                                                        String result_success=response.getString("result");
                                                        String result_message=response.getString("statuscode");


                                                        if(result_success.contains("Success")||result_success.contains("Failed")){
                                                            progressDialog.dismiss();

                                                            DialogForUserInfo(result_success,result_message,result.getText());


//                                                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AdaniQrcode_scanner.this);
//                                                    alertDialogBuilder.setIcon(R.drawable.success);
//                                                    alertDialogBuilder.setTitle("Success ");
//                                                    alertDialogBuilder.setMessage("Updated successfully")
//                                                            .setCancelable(false)
//                                                            .setPositiveButton("Ok",
//                                                                    new DialogInterface.OnClickListener() {
//                                                                        public void onClick(DialogInterface dialog, int id) {
//                                                                            dialog.cancel();
//                                                                            finish();
//                                                                        }
//                                                                    });
//                                                    AlertDialog alert = alertDialogBuilder.create();
//                                                    alert.show();
                                                        }
                                                        else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(context,result_success,Toast.LENGTH_LONG);
                                                        }
                                                    }

                                                    catch (Exception e){
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();


                                                    }


                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    progressDialog.dismiss();
                                                    DialogForUserInfo("d","success",result.getText());
                                                    Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show();

                                                }
                                            }
                                    );
                                    requestQueue.add(jsonObjectRequest);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

//                            }



                        }

                        else {
                            Toast.makeText(context,"Not a valid qr code",Toast.LENGTH_SHORT).show();

                        }



                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    private void DialogForUserInfo(String result_success, String result_message,String  qrcodevalue) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(AdaniQrcode_scanner.this);

        if(result_message.toLowerCase().contains("vehicle not assigned")){
            builder1.setTitle(result_success);
            builder1.setIcon(R.drawable.ic_car_red);
            builder1.setMessage(result_message+"\nMay be you are using different vehicle");
        }

        else if(result_message.toLowerCase().contains("vehicleid not registered")){
            builder1.setTitle("Vehicle not found");
            builder1.setIcon(R.drawable.ic_error);
            builder1.setMessage("This vehicle is not registered please ask your admin to add the vehicle in sensel");

        }
        else if(result_message.toLowerCase().contains("assigned vehicle")){
            builder1.setMessage(result_message);
            builder1.setTitle(result_success);
            builder1.setIcon(R.drawable.ic_checked);


//            editor.putString(sharedprefkey,qrcodevalue);
//
//            while (!editor.commit()){
//                Log.e("while","called");
//                editor.putString(sharedprefkey,qrcodevalue);
//
//            }
        }

        else {
            builder1.setTitle("Error");
            builder1.setMessage(result_success+"\nSomething went wrong. Try again");
            builder1.setIcon(R.drawable.ic_round_warning);

        }


        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });



        AlertDialog alert11 = builder1.create();
        alert11.show();





    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mCodeScanner.startPreview();
        }
        catch (Exception e){

        }

    }

    @Override
    protected void onPause() {
        try {
            mCodeScanner.releaseResources();
        }
        catch (Exception e){

        }

        super.onPause();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSION_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog ad = new AlertDialog.Builder(AdaniQrcode_scanner.this).create();
                    ad.setTitle("Permission Need");
                    ad.setMessage("Camera permission is required");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();

                        }
                    });
                    ad.show();


                }
                else {
                    Qrcodescanner();
                }
        }
    }*/
}