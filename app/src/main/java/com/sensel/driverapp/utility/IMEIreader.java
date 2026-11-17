package com.sensel.driverapp.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class IMEIreader extends AppCompatActivity {
    @SuppressLint("MissingPermission")
    public String getIMEI(Context context){
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            try {
                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String device_id = "";
                device_id = tm.getDeviceId();
                return device_id;
            } catch (Exception ex) {
                Toast.makeText(context, "Give READ_PHONE_STATE permission", Toast.LENGTH_LONG).show();
                return null;
            }
        } else {
            String android_id=Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String device_id = "";
            if (android_id != null) {
                device_id =android_id;
            }
            return device_id;
        }
    }
}
