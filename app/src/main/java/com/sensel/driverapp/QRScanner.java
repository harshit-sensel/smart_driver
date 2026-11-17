package com.sensel.driverapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.ksoap2.serialization.PropertyInfo;

/**
 * Created by User on 18-09-2017.
 */

public class QRScanner extends AppCompatActivity {
    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    public static String latlng=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gridmenu);
        IntentIntegrator qrScan=new IntentIntegrator(this);
        qrScan.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), "QR code Not Found", Toast.LENGTH_LONG).show();
            }
            else {
                final String qrcode=result.getContents();
                final ProgressDialog progressDialog =  new ProgressDialog(QRScanner.this);
                progressDialog.setMessage("Loading please wait ...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                Thread splashThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int waited = 0;
                            while (waited < 300) {
                                sleep(10);
                                waited += 100;
                                if (waited == 100) {
                                    final String res = InsertQRCodeData(Gridmenu.device_id,qrcode);
                                    if (res.contains("Exception") || res.equals("Server Error")) {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(QRScanner.this);
                                                alertDialogBuilder.setIcon(R.drawable.error);
                                                alertDialogBuilder.setTitle("Error ");
                                                alertDialogBuilder.setMessage("Server not responding, try later")
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
                                            }
                                        });
                                    } else  {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Successfully Reported, Thank You", Toast.LENGTH_LONG).show();
                                                finish();
                                            }
                                        });
                                    }

                                }
                            }
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Sorry, Failed to report, Try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } finally {
                            progressDialog.dismiss();
                        }
                    }

                };

                splashThread.start();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String InsertQRCodeData(String device_id,String qrcode) {

        String SOAP_ACTION = "http://tempuri.org/NFCData";
        String OPERATION_NAME = "NFCData";
        PropertyInfo[] piarray=new PropertyInfo[5];
        GPSTracker gpsTracker=new GPSTracker(getApplicationContext());
        latlng=gpsTracker.getLocation();
        String[] geocode=latlng.split(",");

        PropertyInfo pi = new PropertyInfo();
        pi.setName("IMEI");
        pi.setValue(qrcode);
        pi.setType(String.class);
        piarray[0]=pi;

        pi = new PropertyInfo();
        pi.setName("NFC");
        pi.setValue(device_id);
        pi.setType(String.class);
        piarray[1]=pi;

        pi = new PropertyInfo();
        pi.setName("lat");
        pi.setValue(geocode[0]);
        pi.setType(String.class);
        piarray[2]=pi;

        pi = new PropertyInfo();
        pi.setName("lng");
        pi.setValue(geocode[1]);
        pi.setType(String.class);
        piarray[3]=pi;

        pi = new PropertyInfo();
        pi.setName("tagid");
        pi.setValue("");
        pi.setType(String.class);
        piarray[4]=pi;

        WebserviceCall webserviceCall=new WebserviceCall();
        return  webserviceCall.callWebservice(Config.DOMAINURL1,Config.DOMAINURL2,OPERATION_NAME,piarray,SOAP_ACTION);
    }
}
