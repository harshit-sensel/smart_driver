package com.sensel.driverapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.view.ContextThemeWrapper;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Mallikarjun on 22-10-2015.
 */
public class JavaScriptInterface {
    Activity activity;
    String TAG = "JsHandler";
    WebView webView;
    public static Context con;
    Intent i;

    public JavaScriptInterface(Activity _contxt, WebView _webView, Context _cont) {
        activity = _contxt;
        webView = _webView;
        con = _cont;
    }

    //
    @JavascriptInterface
    public String Test() {
        String device_id = "";
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            TelephonyManager tm = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
            device_id = tm.getDeviceId();
        } else {
            String android_id = Settings.Secure.getString(con.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (android_id != null) {
                device_id = android_id;
            }
        }
        WebserviceCall service = new WebserviceCall();
        return device_id;
    }

    @JavascriptInterface
    public void play() {
        MediaPlayer mp = new MediaPlayer();
        try {

            MediaPlayer m = new MediaPlayer();
            AssetFileDescriptor descriptor = activity.getAssets().openFd("hb.wav");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();
            m.prepare();
            m.setVolume(2f, 2f);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    //***********Get current locationGetPlace
    @JavascriptInterface
    public String GetLocation() {
        if (Gridmenu.LATLONG != null && !Gridmenu.LATLONG.isEmpty()) {
            return Gridmenu.LATLONG;
        }
        if (GPSTracker.LATLONG != null && !GPSTracker.LATLONG.isEmpty()) {
            return GPSTracker.LATLONG;
        }
        return "0,0";
    }


    //sessiion id
    @JavascriptInterface
    public String Sessionid() {
        return Splash.SESSIONID;
    }

    //***********Get Domain URL
    @JavascriptInterface
    public String GetDomainUrl() {
        return Config.DOMAINURL1;
    }

    @JavascriptInterface
    public String isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
            return "true";
        else return "false";
    }

    @JavascriptInterface
    public String isLocationAvailable() {
        LocationManager locationManager = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return "true";
        } else {
            return "false";
        }
    }

    @JavascriptInterface
    public void displayMessege(String msg) {
        Toast.makeText(con, msg, Toast.LENGTH_LONG).show();
    }

    @JavascriptInterface
    public void success(String string) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog));
        alertDialogBuilder.setIcon(R.drawable.success);
        alertDialogBuilder.setTitle("Success ");
        alertDialogBuilder.setMessage(string)
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

    @JavascriptInterface
    public void error(String string) {

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                new ContextThemeWrapper(activity, android.R.style.Theme_Holo_Light_Dialog));
        alertDialogBuilder.setIcon(R.drawable.error);
        alertDialogBuilder.setTitle("Error ");
        alertDialogBuilder.setMessage(string)
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


    @JavascriptInterface
    public void setPlace(String type, String latlng, String address) {
        DeviationReport.latlng = latlng;
        DeviationReport.drawing_mode = type;
        DeviationReport.place = address;
    }

    @JavascriptInterface
    public void ResetPlace() {
        DeviationReport.latlng = null;
        DeviationReport.drawing_mode = null;
        DeviationReport.place = null;
    }

    @JavascriptInterface
    public String GetPlace() {
        return DeviationReport.latlng;
    }

    @JavascriptInterface
    public void openGoogleMaps(final String lat, final String lng) {
        Context targetContext = activity != null ? activity : con;
        if (targetContext == null) {
            return;
        }
        try {
            // 1. Launch Google Maps in direct Turn-by-Turn GPS Navigation Mode
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (targetContext.getPackageManager() != null && mapIntent.resolveActivity(targetContext.getPackageManager()) != null) {
                targetContext.startActivity(mapIntent);
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch Google Maps navigation: " + e.getMessage());
        }

        try {
            // 2. Generic geo intent (any installed map app)
            Uri geoUri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng);
            Intent geoIntent = new Intent(Intent.ACTION_VIEW, geoUri);
            geoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (targetContext.getPackageManager() != null && geoIntent.resolveActivity(targetContext.getPackageManager()) != null) {
                targetContext.startActivity(geoIntent);
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to launch geo intent: " + e.getMessage());
        }

        try {
            // 3. Browser fallback (works on 100% of devices)
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            webIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            targetContext.startActivity(webIntent);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open web maps fallback: " + e.getMessage());
        }
    }
}
