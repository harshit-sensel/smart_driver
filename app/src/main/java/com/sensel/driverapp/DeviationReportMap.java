package com.sensel.driverapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Created by User on 03-12-2016.
 */

public class DeviationReportMap  extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviatioreport_map);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        if (ContextCompat.checkSelfPermission(DeviationReportMap.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DeviationReportMap.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(DeviationReportMap.this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Loading Please wait...");
            progressDialog.show();

            WebView myWebView = (WebView) findViewById(R.id.webview);
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
                    progressDialog.dismiss();
                }
            });


            WebSettings settings = myWebView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);
            //settings.setAppCacheEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            JavaScriptInterface jsInterface = new JavaScriptInterface(this, myWebView, getApplicationContext());
            myWebView.addJavascriptInterface(jsInterface, "JSInterface");
            myWebView.loadUrl("file:///android_asset/DeviationReport.html");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final ProgressDialog progressDialog = new ProgressDialog(DeviationReportMap.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Loading Please wait...");
                    progressDialog.show();

                    WebView myWebView = (WebView) findViewById(R.id.webview);
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
                            progressDialog.dismiss();
                        }
                    });


                    WebSettings settings = myWebView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    settings.setDomStorageEnabled(true);
                    settings.setAllowFileAccess(true);
                    //settings.setAppCacheEnabled(true);
                    settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    JavaScriptInterface jsInterface = new JavaScriptInterface(this, myWebView, getApplicationContext());
                    myWebView.addJavascriptInterface(jsInterface, "JSInterface");
                    myWebView.loadUrl("file:///android_asset/DeviationReport.html");
                }
                else {
                    final AlertDialog ad=new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Holo_Light_Dialog)).create();
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

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                DeviationReport.drawing_mode=null;
                DeviationReport.latlng=null;
                DeviationReport.place=null;
                finish();
                break;
            case R.id.menu_add:
               if(DeviationReport.drawing_mode==null || DeviationReport.latlng==null){
                    Toast.makeText(getApplicationContext(),"Choose place in map",Toast.LENGTH_LONG).show();
                }
                else {
                   Intent i = new  Intent(getApplicationContext(), DeviationReport.class);
                   i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   getApplicationContext().startActivity(i);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
