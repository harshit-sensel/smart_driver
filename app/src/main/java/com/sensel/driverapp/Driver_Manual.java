package com.sensel.driverapp;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class Driver_Manual extends AppCompatActivity {


    WebView myWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver__manual);
         myWebView=(WebView) findViewById(R.id.webpage);
        final ProgressDialog progressDialog = new ProgressDialog(Driver_Manual.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading Please wait...");
        progressDialog.show();
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
        //WebStorage.getInstance().deleteAllData();
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.setWebChromeClient(new WebChromeClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            myWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        String page=Driver_Manual_List.page;

        page=Config.DOMAINURL1+"/Driver_manual/"+Splash.ACCOUNTID+"/"+page+"/"+page+".html?Lang="+Driver_Manual_List.languge;
        Log.e("Url",page);
        myWebView.loadUrl(page);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver__manual, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                myWebView.loadUrl("about:blank");
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed()
    {
       myWebView.loadUrl("about:blank");
       this.finish();
    }
    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }
}
