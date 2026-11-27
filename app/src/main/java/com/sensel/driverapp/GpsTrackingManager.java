package com.sensel.driverapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateFormat;
import android.util.Log;

import org.ksoap2.serialization.PropertyInfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages 24/7 background GPS breadcrumb recording and batch server synchronization.
 */
public class GpsTrackingManager {
    private static final String TAG = "GpsTrackingManager";
    private static long lastBreadcrumbTime = 0;
    private static final AtomicBoolean isSyncing = new AtomicBoolean(false);

    /**
     * Records a location breadcrumb into SQLite and triggers sync if throttle interval has elapsed.
     */
    public static synchronized void recordAndSync(Context context, double lat, double lng, float speed, String deviceId) {
        if (context == null || lat == 0 || lng == 0 || deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }

        try {
            SharedPreferences pref = context.getSharedPreferences("totaljrm", Context.MODE_PRIVATE);
            int gpsUpdateRate = pref.getInt("gpsupdaterate", 30); // Default 30 seconds
            if (gpsUpdateRate <= 0) {
                gpsUpdateRate = 30;
            }

            long now = System.currentTimeMillis();
            if ((now - lastBreadcrumbTime) < (gpsUpdateRate * 1000L)) {
                return; // Throttle interval not yet elapsed
            }
            lastBreadcrumbTime = now;

            DecimalFormat df = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
            String formattedSpeed = df.format(speed);
            String timestamp = DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date(now)).toString();

            SQLiteDatabase db = null;
            try {
                db = context.openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                db.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(lat DECIMAL(9,6),lng DECIMAL(9,6),speed DECIMAL(5,2),timestamp DATETIME);");
                String query = "INSERT INTO gpsdata(lat, lng, speed, timestamp) VALUES ('"
                        + lat + "','" + lng + "','" + formattedSpeed + "','" + timestamp + "');";
                db.execSQL(query);
                Log.d(TAG, "Recorded GPS breadcrumb: lat=" + lat + ", lng=" + lng + ", speed=" + formattedSpeed + ", time=" + timestamp);
            } finally {
                if (db != null) {
                    try { db.close(); } catch (Exception ignored) {}
                }
            }

            // Immediately trigger background upload for queued points
            syncPendingGpsData(context, deviceId);

        } catch (Exception e) {
            Log.e(TAG, "Error recording GPS breadcrumb: " + e.getMessage());
        }
    }

    /**
     * Uploads pending breadcrumbs in batches of up to 100 via insertMobileGpsData SOAP webservice.
     */
    public static void syncPendingGpsData(final Context context, final String deviceId) {
        if (context == null || deviceId == null || deviceId.trim().isEmpty()) {
            return;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        if (netInfo == null || !netInfo.isConnected()) {
            return; // No internet connection; points remain safely in SQLite queue
        }

        if (!isSyncing.compareAndSet(false, true)) {
            return; // Sync already in progress
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase selectDb = null;
                Cursor cursor = null;
                try {
                    selectDb = context.openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                    selectDb.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(lat DECIMAL(9,6),lng DECIMAL(9,6),speed DECIMAL(5,2),timestamp DATETIME);");
                    String query = "SELECT lat, lng, speed, timestamp FROM gpsdata ORDER BY timestamp DESC LIMIT 100";
                    cursor = selectDb.rawQuery(query, null);

                    if (cursor != null && cursor.getCount() > 0) {
                        StringBuilder gpsDataBuilder = new StringBuilder();
                        String[] gpsDateArray = new String[100];
                        cursor.moveToFirst();
                        int i = 0;
                        do {
                            gpsDateArray[i] = cursor.getString(3);
                            gpsDataBuilder.append(cursor.getString(0)).append("#")
                                    .append(cursor.getString(1)).append("#")
                                    .append(cursor.getString(2)).append("#")
                                    .append(cursor.getString(3)).append("|");
                            i++;
                        } while (cursor.moveToNext() && i < 100);

                        cursor.close();
                        cursor = null;
                        selectDb.close();
                        selectDb = null;

                        String payload = gpsDataBuilder.toString();
                        if (payload.length() > 0) {
                            PropertyInfo[] piarray = new PropertyInfo[2];
                            PropertyInfo pi = new PropertyInfo();
                            pi.setName("IMEI");
                            pi.setValue(deviceId);
                            pi.setType(String.class);
                            piarray[0] = pi;

                            pi = new PropertyInfo();
                            pi.setName("data");
                            pi.setValue(payload);
                            pi.setType(String.class);
                            piarray[1] = pi;

                            WebserviceCall webserviceCall = new WebserviceCall();
                            String result = webserviceCall.callWebservice(
                                    Config.DOMAINURL1, Config.DOMAINURL2,
                                    "insertMobileGpsData", piarray,
                                    "http://tempuri.org/insertMobileGpsData"
                            );

                            if (result != null && result.contains("Data Inserted successfully")) {
                                StringBuilder deleteSql = new StringBuilder("DELETE FROM gpsdata WHERE timestamp IN (");
                                for (int j = 0; j < gpsDateArray.length; j++) {
                                    if (gpsDateArray[j] != null) {
                                        if (j != 0) deleteSql.append(",");
                                        deleteSql.append("'").append(gpsDateArray[j]).append("'");
                                    } else {
                                        break;
                                    }
                                }
                                deleteSql.append(");");

                                SQLiteDatabase deleteDb = null;
                                try {
                                    deleteDb = context.openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                                    deleteDb.execSQL(deleteSql.toString());
                                    Log.d(TAG, "Successfully synced " + i + " GPS breadcrumbs to server and cleaned SQLite.");
                                } finally {
                                    if (deleteDb != null) {
                                        try { deleteDb.close(); } catch (Exception ignored) {}
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error syncing GPS data: " + ex.getMessage());
                } finally {
                    if (cursor != null) {
                        try { cursor.close(); } catch (Exception ignored) {}
                    }
                    if (selectDb != null) {
                        try { selectDb.close(); } catch (Exception ignored) {}
                    }
                    isSyncing.set(false);
                }
            }
        }).start();
    }
}
