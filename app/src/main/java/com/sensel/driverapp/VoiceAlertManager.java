package com.sensel.driverapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import org.ksoap2.serialization.PropertyInfo;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * Centralized background audio engine for driver safety voice alerts.
 * Operates reliably in all app states (foreground, minimized, or screen locked/black)
 * when called from SaveMyAppsService (Foreground Service).
 */
public class VoiceAlertManager {

    private static final String TAG = "VoiceAlertManager";
    private static MediaPlayer activeMediaPlayer = null;

    // Debounce tracking (milliseconds)
    private static long lastOverspeedTime = 0;
    private static long lastAccidentSpotTime = 0;
    private static long lastBlindSpotTime = 0;
    private static long lastParkingTime = 0;
    private static long lastContinuousDrivingTime = 0;
    private static long lastMaxDrivingTime = 0;
    private static long lastNightDrivingTime = 0;

    /**
     * Internal safe player method
     */
    private static synchronized void playAudio(final Context context, final Uri uri, final int rawResId, final String audioLogType, final double lat, final double lng) {
        try {
            if (activeMediaPlayer != null) {
                try {
                    if (activeMediaPlayer.isPlaying()) {
                        activeMediaPlayer.stop();
                    }
                    activeMediaPlayer.release();
                } catch (Exception ignored) {
                }
                activeMediaPlayer = null;
            }

            Context appContext = context.getApplicationContext();
            if (uri != null) {
                activeMediaPlayer = MediaPlayer.create(appContext, uri);
            } else if (rawResId != 0) {
                activeMediaPlayer = MediaPlayer.create(appContext, rawResId);
            }

            if (activeMediaPlayer != null) {
                activeMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            mp.release();
                        } catch (Exception ignored) {
                        }
                        if (activeMediaPlayer == mp) {
                            activeMediaPlayer = null;
                        }
                    }
                });
                activeMediaPlayer.start();
                Log.d(TAG, "Playing audio alert: " + audioLogType);

                if (audioLogType != null && !audioLogType.isEmpty()) {
                    insertAudioLog(appContext, lat, lng, audioLogType);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio: " + e.getMessage());
        }
    }

    /**
     * 1. Overspeed Alert (slowdown_voice.mp3)
     */
    public static synchronized void checkAndPlayOverspeed(Context context, float speed, int maxSpeed, double lat, double lng) {
        if (speed <= maxSpeed) {
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - lastOverspeedTime) < 20000) { // 20s debounce
            return;
        }
        lastOverspeedTime = now;

        File dir = new File(context.getExternalFilesDir(null), "driverapp");
        File file = new File(dir, "slowdown_voice.mp3");
        if (file.exists()) {
            playAudio(context, Uri.parse(file.getAbsolutePath()), 0, "APP-OVERSPEEDING", lat, lng);
        } else {
            playAudio(context, null, R.raw.slowdown_voice, "APP-OVERSPEEDING", lat, lng);
        }
    }

    /**
     * 2 & 3. Hazard Spot Alerts (speedlimit25.mp3 / speedlimit35.mp3)
     */
    public static synchronized void checkAndPlayHazardSpot(Context context, int limit, double lat, double lng) {
        long now = System.currentTimeMillis();
        if (limit == 25) {
            if ((now - lastAccidentSpotTime) < 30000) { // 30s debounce
                return;
            }
            lastAccidentSpotTime = now;
            playAudio(context, null, R.raw.speedlimit25, "APP-HAZARD-25", lat, lng);
        } else if (limit == 35) {
            if ((now - lastBlindSpotTime) < 30000) { // 30s debounce
                return;
            }
            lastBlindSpotTime = now;
            playAudio(context, null, R.raw.speedlimit35, "APP-HAZARD-35", lat, lng);
        }
    }

    /**
     * 4. Parking Approaching Alerts (park15km, park10km, park3km)
     */
    public static synchronized void checkAndPlayParking(Context context, int limit, double lat, double lng) {
        long now = System.currentTimeMillis();
        if ((now - lastParkingTime) < 30000) { // 30s debounce
            return;
        }
        lastParkingTime = now;

        if (limit == 15) {
            playAudio(context, null, R.raw.park15km, "APP-PARKING-15", lat, lng);
        } else if (limit == 10) {
            playAudio(context, null, R.raw.park10km, "APP-PARKING-10", lat, lng);
        } else if (limit == 3) {
            playAudio(context, null, R.raw.park3km, "APP-PARKING-3", lat, lng);
        }
    }

    /**
     * 5, 6 & 7. Fatigue & Night Driving Alerts
     */
    public static synchronized void checkAndPlayFatigueAlert(Context context, String alertType, double lat, double lng) {
        long now = System.currentTimeMillis();
        File dir = new File(context.getExternalFilesDir(null), "driverapp");

        if ("CONTINUOUS_DRIVING".equals(alertType)) {
            if ((now - lastContinuousDrivingTime) < 60000) { // 1 min debounce
                return;
            }
            lastContinuousDrivingTime = now;
            File file = new File(dir, "continuous_driving_voice.mp3");
            if (file.exists()) {
                playAudio(context, Uri.parse(file.getAbsolutePath()), 0, "APP-CONTINUOUS_DRIVING", lat, lng);
            } else {
                playAudio(context, null, R.raw.continues_driving, "APP-CONTINUOUS_DRIVING", lat, lng);
            }
        } else if ("MAX_DRIVING".equals(alertType)) {
            if ((now - lastMaxDrivingTime) < 60000) { // 1 min debounce
                return;
            }
            lastMaxDrivingTime = now;
            File file = new File(dir, "max_driving_voice.mp3");
            if (file.exists()) {
                playAudio(context, Uri.parse(file.getAbsolutePath()), 0, "APP-MAXDRIVING", lat, lng);
            } else {
                playAudio(context, null, R.raw.max_driving, "APP-MAXDRIVING", lat, lng);
            }
        } else if ("NIGHT_DRIVING".equals(alertType)) {
            if ((now - lastNightDrivingTime) < 60000) { // 1 min debounce
                return;
            }
            lastNightDrivingTime = now;
            File file = new File(dir, "night_driving_voice.mp3");
            if (file.exists()) {
                playAudio(context, Uri.parse(file.getAbsolutePath()), 0, "APP-NIGHTDRIVING", lat, lng);
            } else {
                playAudio(context, null, R.raw.night_driving, "APP-NIGHTDRIVING", lat, lng);
            }
        }
    }

    /**
     * Audit log inserter: records lat/lng/type in SQLite and syncs to server
     */
    public static void insertAudioLog(final Context context, final double lat, final double lng, final String type) {
        try {
            Calendar c = Calendar.getInstance();
            SQLiteDatabase db = context.openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS insertAudioLog(lat DECIMAL(9,6),lng DECIMAL(9,6),type VARCHAR,timestamp DATETIME);");
            String query = "INSERT INTO insertAudioLog(lat,lng ,type , timestamp) VALUES ('" + lat + "','" + lng + "','" + type + "','" + DateFormat.format("yyyy-MM-dd H:mm:ss", new Date(c.getTimeInMillis())).toString() + "');";
            db.execSQL(query);
            db.close();

            syncAudioLogs(context);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting audio log: " + e.getMessage());
        }
    }

    public static void syncAudioLogs(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        if (netInfo == null || !netInfo.isConnected()) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase db = context.openOrCreateDatabase("totaljrmdb", Context.MODE_PRIVATE, null);
                    db.execSQL("CREATE TABLE IF NOT EXISTS insertAudioLog(lat DECIMAL(9,6),lng DECIMAL(9,6),type VARCHAR,timestamp DATETIME);");
                    String query = "SELECT distinct timestamp,lat,lng,type FROM insertAudioLog order by timestamp desc limit 100";
                    Cursor cursor = db.rawQuery(query, null);
                    if (cursor != null && cursor.getCount() > 0) {
                        StringBuilder audiodata = new StringBuilder();
                        final String[] audiodateArray = new String[100];
                        cursor.moveToFirst();
                        int i = 0;
                        do {
                            audiodateArray[i] = cursor.getString(0);
                            i++;
                            audiodata.append(cursor.getString(0)).append("@&")
                                    .append(cursor.getString(1)).append("@&")
                                    .append(cursor.getString(2)).append("@&")
                                    .append(cursor.getString(3)).append("&@");
                        } while (cursor.moveToNext() && i < 100);
                        cursor.close();

                        String deviceId = SaveMyAppsService.deviceid;
                        if (deviceId == null) {
                            deviceId = LockScreenActivity.deviceid;
                        }

                        if (deviceId != null && audiodata.length() > 0) {
                            PropertyInfo[] piarray = new PropertyInfo[2];
                            PropertyInfo pi = new PropertyInfo();
                            pi.setName("imei");
                            pi.setValue(deviceId);
                            pi.setType(String.class);
                            piarray[0] = pi;

                            pi = new PropertyInfo();
                            pi.setName("logData");
                            pi.setValue(audiodata.toString());
                            pi.setType(String.class);
                            piarray[1] = pi;

                            WebserviceCall webserviceCall = new WebserviceCall();
                            String result = webserviceCall.callWebservice(Config.DOMAINURL1, Config.DOMAINURL2, "insertlogAudioData", piarray, "http://tempuri.org/insertlogAudioData");
                            if (result != null && result.contains("Data Inserted successfully")) {
                                StringBuilder data = new StringBuilder("delete from insertAudioLog where timestamp in (");
                                for (int j = 0; j < audiodateArray.length; j++) {
                                    if (audiodateArray[j] != null) {
                                        if (j != 0) data.append(",");
                                        data.append("'").append(audiodateArray[j]).append("'");
                                    } else {
                                        break;
                                    }
                                }
                                data.append(");");
                                db.execSQL(data.toString());
                            }
                        }
                    }
                    if (cursor != null) cursor.close();
                    db.close();
                } catch (Exception ex) {
                    Log.e(TAG, "Error syncing audio logs: " + ex.getMessage());
                }
            }
        }).start();
    }
}
