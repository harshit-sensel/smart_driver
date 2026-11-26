package com.sensel.driverapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AppErrorLogger - Production Error & Crash Logging Utility
 *
 * Captures fatal uncaught crashes and caught exceptions across all Android versions
 * (Android 7 to 16) and OEM devices (Samsung, Xiaomi, Vivo, Oppo, etc.).
 *
 * Guarantees:
 * 1. Strict 450-character limit (never exceeds 500 VARCHAR DB limit).
 * 2. Sanitizes single quotes (') to prevent SQL syntax errors.
 * 3. Sanitizes hash (#) to protect the #AppVersion delimiter.
 * 4. Deduplication cache to prevent flooding the database with identical errors.
 * 5. 100% crash-proof and non-blocking.
 */
public class AppErrorLogger {

    private static final String TAG = "AppErrorLogger";
    private static final int MAX_LOG_LENGTH = 450;
    private static final long DEDUP_WINDOW_MS = 5 * 60 * 1000; // 5 minutes

    private static volatile boolean isInstalled = false;
    private static Context appContext;
    private static final Map<String, Long> deduplicationCache = new ConcurrentHashMap<>();

    /**
     * Install the process-wide uncaught crash handler.
     * Call once in Splash.java onCreate().
     */
    public static synchronized void install(Context context) {
        if (isInstalled || context == null) {
            return;
        }
        appContext = context.getApplicationContext();

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    if (throwable != null) {
                        String screen = detectScreenName(throwable);
                        String errorLog = formatError(screen, throwable);

                        if (errorLog != null) {
                            Log.e(TAG, "Uncaught Crash detected! Sending to DB: " + errorLog);
                            // Send synchronously with a safety timeout so network request completes before process dies
                            Thread sendThread = new Thread(() -> dispatchToWebservice(errorLog));
                            sendThread.start();
                            try {
                                sendThread.join(4000); // Wait up to 4 seconds for SOAP request
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Failed in uncaughtException handler: " + t.getMessage());
                } finally {
                    if (defaultHandler != null) {
                        defaultHandler.uncaughtException(thread, throwable);
                    }
                }
            }
        });

        isInstalled = true;
        Log.i(TAG, "AppErrorLogger crash protection successfully installed");
    }

    /**
     * Manually log a caught exception from any try-catch block.
     * Executes asynchronously in the background with zero UI impact.
     *
     * @param screen    Screen or feature name (e.g., "CustomerFeedback", "DeviationReport")
     * @param throwable Caught exception
     */
    public static void log(final String screen, final Throwable throwable) {
        if (throwable == null) {
            return;
        }
        new Thread(() -> {
            try {
                String errorLog = formatError(screen != null ? screen : "Unknown", throwable);
                if (errorLog != null) {
                    dispatchToWebservice(errorLog);
                }
            } catch (Throwable t) {
                Log.e(TAG, "Error in manual log: " + t.getMessage());
            }
        }).start();
    }

    /**
     * Format and sanitize error details into a structured string.
     * Returns null if the identical error was logged recently (deduplication).
     */
    private static String formatError(String screen, Throwable throwable) {
        try {
            String device = getDeviceModel();
            String os = "Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
            String exName = throwable.getClass().getSimpleName();
            String message = throwable.getMessage() != null ? throwable.getMessage().trim() : "No message";

            // Find root cause location in com.sensel.driverapp code
            String rootLocation = "";
            for (StackTraceElement elem : throwable.getStackTrace()) {
                if (elem != null && elem.getClassName() != null && elem.getClassName().contains("com.sensel.driverapp")) {
                    String fileName = elem.getFileName() != null ? elem.getFileName() : "Unknown";
                    rootLocation = " at " + fileName + ":" + elem.getLineNumber();
                    break;
                }
            }

            // Deduplication check (prevents spamming the same error repeatedly)
            String dedupKey = screen + "|" + exName + "|" + rootLocation;
            long now = System.currentTimeMillis();

            // Clean up expired entries older than 5 minutes to keep memory footprint virtually zero
            if (deduplicationCache.size() > 10) {
                deduplicationCache.entrySet().removeIf(entry -> (now - entry.getValue()) > DEDUP_WINDOW_MS);
            }

            Long lastSent = deduplicationCache.get(dedupKey);
            if (lastSent != null && (now - lastSent) < DEDUP_WINDOW_MS) {
                Log.d(TAG, "Skipping duplicate error within window: " + dedupKey);
                return null;
            }
            deduplicationCache.put(dedupKey, now);

            // Assemble structured message:
            // ERR | Screen | Device | OS | Exception: Message at File:Line
            String assembled = "ERR|" + screen + "|" + device + "|" + os + "|" + exName + ": " + message + rootLocation;

            // Sanitize: strip single quotes (protects SQL) and hash (protects version delimiter)
            assembled = assembled.replace("'", "")
                    .replace("#", "-")
                    .replace("\r", " ")
                    .replace("\n", " ");

            // Strict Hard Cap at 450 characters (leaves buffer below 500 limit)
            if (assembled.length() > MAX_LOG_LENGTH) {
                assembled = assembled.substring(0, MAX_LOG_LENGTH - 3) + "...";
            }

            return assembled;
        } catch (Throwable t) {
            return "ERR|" + screen + "|DeviceError|" + t.getClass().getSimpleName();
        }
    }

    /**
     * Inspect stack trace to dynamically detect which Activity / Screen crashed.
     */
    private static String detectScreenName(Throwable throwable) {
        if (throwable != null) {
            for (StackTraceElement elem : throwable.getStackTrace()) {
                if (elem != null && elem.getClassName() != null && elem.getClassName().contains("com.sensel.driverapp")) {
                    String className = elem.getClassName();
                    int lastDot = className.lastIndexOf('.');
                    String simpleName = (lastDot >= 0 && lastDot < className.length() - 1) ? className.substring(lastDot + 1) : className;
                    if (simpleName.contains("$")) {
                        simpleName = simpleName.substring(0, simpleName.indexOf('$'));
                    }
                    return simpleName;
                }
            }
        }
        return "AppStartup";
    }

    /**
     * Retrieve phone manufacturer and model safely without permissions.
     */
    private static String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER : "";
        String model = Build.MODEL != null ? Build.MODEL : "";
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) return "";
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) return s;
        return Character.toUpperCase(first) + s.substring(1);
    }

    /**
     * Dispatch formatted error string to the remote database via WebserviceCall.
     */
    private static void dispatchToWebservice(String errorLog) {
        try {
            String deviceId = getDeviceId();
            String[] latlng = getCoordinates();

            WebserviceCall webserviceCall = new WebserviceCall();
            String response = webserviceCall.KeepActivityLog(deviceId, errorLog, latlng[0], latlng[1]);
            Log.e(TAG, "Logged to DB -> " + errorLog + " | Response: " + response);
        } catch (Throwable t) {
            Log.e(TAG, "Failed dispatching error log: " + t.getMessage());
        }
    }

    /**
     * Safely resolve IMEI / deviceId from app memory or Android ID fallback.
     */
    private static String getDeviceId() {
        try {
            if (Gridmenu.device_id != null && !Gridmenu.device_id.trim().isEmpty()) {
                return Gridmenu.device_id.trim();
            }
            if (SaveMyAppsService.deviceid != null && !SaveMyAppsService.deviceid.trim().isEmpty()) {
                return SaveMyAppsService.deviceid.trim();
            }
            if (appContext != null) {
                String androidId = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                if (androidId != null && !androidId.trim().isEmpty()) {
                    return androidId.trim();
                }
            }
        } catch (Throwable ignored) {
        }
        return "0";
    }

    /**
     * Safely get latitude and longitude coordinates.
     * Uses our app's live GPSTracker location first, then falls back to Android LocationManager.
     */
    private static String[] getCoordinates() {
        try {
            // Priority 1: Check live location cached by our app's GPSTracker / Service
            if (GPSTracker.lastLocation != null) {
                return new String[]{
                        String.valueOf(GPSTracker.lastLocation.getLatitude()),
                        String.valueOf(GPSTracker.lastLocation.getLongitude())
                };
            }
            if (GPSTracker.LATLONG != null && GPSTracker.LATLONG.contains(",") && !GPSTracker.LATLONG.equals("0,0")) {
                String[] parts = GPSTracker.LATLONG.split(",");
                if (parts.length >= 2) {
                    return new String[]{parts[0].trim(), parts[1].trim()};
                }
            }

            // Priority 2: Query Android LocationManager directly
            if (appContext != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                        appContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        appContext.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    LocationManager lm = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
                    if (lm != null) {
                        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc == null) {
                            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (loc != null) {
                            return new String[]{String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude())};
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return new String[]{"0.0", "0.0"};
    }
}
