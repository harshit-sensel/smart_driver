package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by User on 28-12-2016.
 */

public class Map extends AppCompatActivity {
    WebView myWebView;
    public static String Place;
    public static String title;
    public static String vehicle;
    public static String route;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        if (ContextCompat.checkSelfPermission(Map.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Map.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
       else {

            final ProgressDialog progressDialog = new ProgressDialog(Map.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Please wait...");
            progressDialog.show();

            myWebView = (WebView) findViewById(R.id.map);
            myWebView.setWebViewClient(
                    new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            view.loadUrl(url);
                            return true;
                        }
                    });
            myWebView.setWebChromeClient(new WebChromeClient() {
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    callback.invoke(origin, true, false);
                }
            });
            myWebView.setWebViewClient(new WebViewClient() {

                public void onPageFinished(WebView view, String url) {
                    if (Splash.isBirthday.equals("false")) {

                        ShowPlace();
                    }
                    progressDialog.dismiss();
                }
            });
            WebSettings settings = myWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);
            //settings.setAppCacheEnabled(true);
            settings.setAllowFileAccess(true);
            JavaScriptInterface jsInterface = new JavaScriptInterface(this, myWebView, getApplicationContext());
            myWebView.addJavascriptInterface(jsInterface, "JSInterface");
            if (Splash.isBirthday.equals("true")) {
                myWebView.getSettings().setUseWideViewPort(true);
                myWebView.loadUrl("file:///android_asset/simple.html");
                setTitle("Best Birthday Wishes");
                Handler handlerUI = new Handler();
                handlerUI.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(Map.this, Gridmenu.class);
                        startActivity(i);
                        finish();
                    }
                }, 28000);
            } else
                myWebView.loadUrl("file:///android_asset/map.html");
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final ProgressDialog progressDialog = new ProgressDialog(Map.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Loading Please wait...");
                    progressDialog.show();

                    myWebView = (WebView) findViewById(R.id.map);
                    myWebView.setWebViewClient(
                            new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    view.loadUrl(url);
                                    return true;
                                }
                            });
                    myWebView.setWebChromeClient(new WebChromeClient() {
                        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                            callback.invoke(origin, true, false);
                        }
                    });
                    myWebView.setWebViewClient(new WebViewClient() {

                        public void onPageFinished(WebView view, String url) {
                            if (Splash.isBirthday.equals("false")) {
                                ShowPlace();
                            }
                            progressDialog.dismiss();
                        }
                    });
                    WebSettings settings = myWebView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setAllowFileAccess(true);
                    //settings.setAppCacheEnabled(true);
                    settings.setAllowFileAccess(true);
                    JavaScriptInterface jsInterface = new JavaScriptInterface(this, myWebView, getApplicationContext());
                    myWebView.addJavascriptInterface(jsInterface, "JSInterface");
                    if (Splash.isBirthday.equals("true")) {
                        myWebView.getSettings().setUseWideViewPort(true);
                        myWebView.loadUrl("file:///android_asset/simple.html");
                        setTitle("Best Birthday Wishes");
                        Handler handlerUI = new Handler();
                        handlerUI.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i = new Intent(Map.this, Gridmenu.class);
                                startActivity(i);
                                finish();
                            }
                        }, 28000);
                    } else
                        myWebView.loadUrl("file:///android_asset/map.html");
                } else {
                    final AlertDialog ad = new AlertDialog.Builder(this).create();
                    ad.setIcon(R.drawable.error);
                    ad.setTitle("Need Location Permission");
                    ad.setMessage("Need Location permission to run app.");
                    ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    ad.show();
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_add).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(!Splash.isBirthday.equals("true"))
                {
                    finish();
                }
                break;
            case R.id.menu_add:
                Toast.makeText(getApplicationContext(),"hello",Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ShowPlace()
    {
        setTitle(title);
        Log.e("place",Place);
        if(Place.equals("ERV")){
            myWebView.loadUrl("javascript:ERVRoute('"+vehicle+"')");
        }

        else if(Place.equals("NonDCRoute")){
            myWebView.loadUrl("javascript:getPlace('"+route+"')");
            Log.e("route",route);
            //myWebView.loadUrl("javascript:jrmRoute1('"+route+"')");
        }

        else if(Place.equals("Violations")){
            myWebView.loadUrl("javascript:getAllViolations()");
        }

        else{
            myWebView.loadUrl("javascript:getPlace('"+Place+"')");
        }


    }
}

