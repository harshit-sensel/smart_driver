package com.sensel.driverapp;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by User on 16-01-2017.
 */

public class PriorityNotification extends Activity {
    public static String message="Happy Driving, Have a great day";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.priority_notification);

        WebView webview=(WebView) findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setDefaultFixedFontSize(14);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        //webSettings.setAppCacheEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadDataWithBaseURL("", message, "text/html", "UTF-8", "");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webview.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }
        AudioManager am =
                (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);

        am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
    }

    @Override
    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
               // | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    public void close(View view)
    {
        finish();
    }
   }
