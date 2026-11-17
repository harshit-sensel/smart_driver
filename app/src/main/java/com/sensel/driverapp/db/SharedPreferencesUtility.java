package com.sensel.driverapp.db;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtility {


    private Context context;

    private static final String ANY_STORE_PREFERENCES = "MyTotalPrefs";

    public static final String LOGGED_IN = "logged_in";
    public static final String SESSION_ID = "session_id";
    public static final String USER_NAME = "username";
    public static final String BASE_URL = "https://fleetsmart3.ui.sensel.in/";
    public static final String FCM_TOKEN = "fcmToken";
    public static final String FCM_TOKEN_POSTED = "fcmTokenUpdate";


    public SharedPreferencesUtility(Context context) {
        this.context = context;
    }

    public void saveStringPreferences(String key, String value) {
        SharedPreferences.Editor editor = context.
                getSharedPreferences(ANY_STORE_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }


    public String getStringPreferences(String key) {
        return context.getSharedPreferences(ANY_STORE_PREFERENCES, Context.MODE_PRIVATE).getString(key, "NA");
    }

    public void saveBooleanPreferences(String key, Boolean value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(ANY_STORE_PREFERENCES, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBooleanPreferences(String key) {
        return context.getSharedPreferences(ANY_STORE_PREFERENCES, Context.MODE_PRIVATE).getBoolean(key, false);
    }


}
