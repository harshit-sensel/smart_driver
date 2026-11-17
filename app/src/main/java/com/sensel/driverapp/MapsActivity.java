package com.sensel.driverapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sensel.driverapp.objectclass.RootWeather;
import com.sensel.driverapp.objectclass.SelectedParingSpotObject;
import com.sensel.driverapp.objectclass.weather.City;
import com.sensel.driverapp.objectclass.weather.Clouds;
import com.sensel.driverapp.objectclass.weather.Coord;
import com.sensel.driverapp.objectclass.weather.Sys;
import com.sensel.driverapp.objectclass.weather.WList;
import com.sensel.driverapp.objectclass.weather.Weather;
import com.sensel.driverapp.objectclass.weather.WeatherObject;
import com.sensel.driverapp.objectclass.weather.Wind;
import com.sensel.driverapp.utility.JourneyPlanDB;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String REQUEST_TAG = "WEATHERAPIREQUEST";
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.map_info_window, null);
        }


        @Override
        public View getInfoWindow(Marker marker) {
            TextView tvInfo = ((TextView) myContentsView.findViewById(R.id.mapinfo));
            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.parkingname));
            tvInfo.setText(marker.getSnippet());
            tvTitle.setText(marker.getTitle());

            return myContentsView;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private GoogleMap mMap;
    private ArrayList<LatLng> latLngArrayList;
    private JourneyPlanDB journeyPlanDB;
    private Cursor cursor;
    private RequestQueue mQueue;

    private String polylineColor = "nill";

    int count, count1 = 0;
    Marker mPositionMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        latLngArrayList = new ArrayList<>();
        journeyPlanDB = new JourneyPlanDB(getApplicationContext());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        count = 1;


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cursor = journeyPlanDB.getLatLong();



                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {

                        if (count % 300 == 0) {
                            startParsingTask(cursor.getString(0), cursor.getString(1));
                        }
                        count++;
                        if (polylineColor.equals(cursor.getString(2))) {
                            polylineColor = cursor.getString(2);


                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));
                            latLngArrayList.add(temp);
                        } else {
                            LatLng temp = new LatLng(Double.valueOf(cursor.getString(0)), Double.valueOf(cursor.getString(1)));
                            latLngArrayList.add(temp);
                            drawPolyline(polylineColor, latLngArrayList);
                            polylineColor = cursor.getString(2);
                            latLngArrayList.clear();
                        }

                    }
                    drawPolyline(polylineColor, latLngArrayList);
                }

                parkingSpot();
            }
        });



//        cursor = journeyPlanDB.getdata();
//        ParkingSpotSelect.spotObjectArrayList.clear();
//        if (cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                cursor.getString(0);
//                cursor.getString(1);
//                Date eta, ela;
//                try {
//                    eta = sdf.parse(cursor.getString(2));
//                    ela = sdf.parse(cursor.getString(3));
//                    SelectedParingSpotObject temp = new SelectedParingSpotObject(cursor.getString(1), eta, ela, "######", "no", cursor.getString(5), cursor.getLong(4), 00, "", "", cursor.getString(6));
//                    ParkingSpotSelect.spotObjectArrayList.add(temp);
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//
//
//            }
//        }




    }

    private void drawPolyline(String polylineColor, ArrayList<LatLng> latLngArrayList) {
        PolylineOptions polyOptions;
        polyOptions = new PolylineOptions();
        int color;
        color = Color.BLUE;

        if (polylineColor.equals("Red")) {
            color = Color.RED;
        } else if (polylineColor.equals("Blue")) {
            color = Color.BLUE;
        } else if (polylineColor.equals("Yellow")) {
            color = Color.YELLOW;
        } else if (polylineColor.equals("Green")) {
            color = Color.GREEN;
        } else {
            color = Color.BLUE;
        }

        polyOptions.color(color);

        polyOptions.width(20);
        polyOptions.addAll(latLngArrayList);
        mMap.addPolyline(polyOptions);
    }

//    public void drawPolyLineOnMap(ArrayList<LatLng> list) throws InterruptedException {
//
//
//        handler = new Handler();
//        task = new Runnable() {
//            @Override
//            public void run() {
//                if (count >= list.size() - 1) {
//                    count = 0;
//
//                    parkingSpot();
//
//                    handler.removeCallbacks(task);
//                } else {
//                    LatLng latLng = list.get(count);
//
//
//                    if (mPositionMarker == null) {
//
//                        mPositionMarker = mMap.addMarker(new MarkerOptions()
//                                .flat(true)
//                                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_baseline_navigation))
//                                .anchor(0.5f, 0.5f)
//                                .position(list.get(count)));
//                    }
//
//                    animateMarker(mPositionMarker, list.get(count),list.get(count+1));
//
//                    mMap.animateCamera(CameraUpdateFactory.newLatLng(list.get(count)));
//
//
////                polyOptions.add(list.get(count));
////                mMap.addPolyline(polyOptions);
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 50, null);
//
//                    count++;
//
//                    handler.postDelayed(this, 50);
//                }
//
//
//            }
//        };
//        handler.post(task);
//
//    }


    public void parkingSpot() {
        LatLng parking;
        String srcdestlatlong[];
        srcdestlatlong = JourneyPlanner.SRC_LATLANG.split(",");
        parking = new LatLng(Double.valueOf(srcdestlatlong[0]), Double.valueOf(srcdestlatlong[1]));
        String srcinfo = "";

        srcinfo = "Start time : " + JourneyPlanner.sdf.format(JourneyPlanner.START_DATE);

        Marker source = mMap.addMarker(
                new MarkerOptions()
                        .position(parking)
                        .title(JourneyPlanner.START_LOC)
                        .snippet(srcinfo)
                        .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_round_greenloc)));

        source.showInfoWindow();


        for (int i = 0; i < ParkingSpotSelect.spotObjectArrayList.size(); i++) {


            String latlong[] = ParkingSpotSelect.spotObjectArrayList.get(i).getLatLan().split(",");
            parking = new LatLng(Double.valueOf(latlong[0]), Double.valueOf(latlong[1]));
            String infos = "Estimated arrival :" + ParkingSpotSelect.sdf.format(ParkingSpotSelect.spotObjectArrayList.get(i).getEstimatedArrival())
                    + "\nActual arrival :" + ParkingSpotSelect.spotObjectArrayList.get(i).getActualArrival()
                    + "\nEstimated Rest Time : " + String.valueOf(ParkingSpotSelect.spotObjectArrayList.get(i).getRestTime())
                    + "\nActual Rest Time : " + String.valueOf(ParkingSpotSelect.spotObjectArrayList.get(i).getActualRestTime())
                    + "\nActual Leaving Time " + String.valueOf(ParkingSpotSelect.spotObjectArrayList.get(i).getEXITATA());


            Marker park = mMap.addMarker(
                    new MarkerOptions()
                            .position(parking)
                            .title(ParkingSpotSelect.spotObjectArrayList.get(i).getParkingName())
                            .snippet(infos)
                            .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_parking_sign)));

            park.showInfoWindow();

            mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());


        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(parking, 10));


    }

    private void animateRoute() {

        final Handler handler = new Handler();
        Runnable task = new Runnable() {
            @Override
            public void run() {
                LatLng latLng = latLngArrayList.get(count);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 100, null);
                Log.e("rrr", "Doing task");
                count++;
                handler.postDelayed(this, 100);


            }
        };
        handler.post(task);

    }


    public void animateMarker(final Marker marker, final LatLng location, final LatLng location2) {
        float bearing = (float) angleFromCoordinate(location.latitude, location.longitude, location2.latitude, location2.longitude);

        marker.setPosition(location);
        marker.setRotation(bearing);


//        final Handler handler = new Handler();
//        final long start = SystemClock.uptimeMillis();
//        final LatLng startLatLng = marker.getPosition();
//        final double startRotation = marker.getRotation();
//        final long duration = 500;
//
//        final Interpolator interpolator = new LinearInterpolator();
//
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                long elapsed = SystemClock.uptimeMillis() - start;
//                float t = interpolator.getInterpolation((float) elapsed
//                        / duration);
//
//                double lng = t * location.longitude + (1 - t)
//                        * startLatLng.longitude;
//                double lat = t * location.latitude + (1 - t)
//                        * startLatLng.latitude;
//
////                float rotation = (float) (t * location.getBearing() + (1 - t)
////                        * startRotation);
//
//                marker.setPosition(new LatLng(lat, lng));
//
//
//                if (t < 1.0) {
//                    // Post again 16ms later.
//                    handler.postDelayed(this, 16);
//                }
//            }
//        });
    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private double angleFromCoordinate(double lat1, double long1, double lat2,
                                       double long2) {

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
//        brng = (brng + 360) % 360;
//        brng =  brng-90;


        return brng;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mQueue != null) {
            mQueue.cancelAll(REQUEST_TAG);
        }
    }


    public void startParsingTask(String lat, String lon) {

        mQueue = Volley.newRequestQueue(this);
        String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&exclude=current,minutely,hourly&units=metric&appid=4b406562a5c50b1f44bcfbbeb35795af";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {


                            ArrayList<WList> list;
                            list = new ArrayList<>();

                            JSONArray j_lst = response.getJSONArray("list");
                            JSONObject j_city = response.getJSONObject("city");
                            JSONObject j_coord = j_city.getJSONObject("coord");

                            Log.e("RESP1", j_lst.toString());
                            Log.e("RESP2", j_city.toString());
                            Log.e("RESP3", j_city.toString());

                            for (int i = 0; i < j_lst.length(); i++) {
                                JSONObject jsonObject = j_lst.getJSONObject(i);
                                JSONObject j_main = jsonObject.getJSONObject("main");
                                Log.e("RESP4", j_main.toString());

                                WeatherObject weatherObject = new WeatherObject(j_main.getDouble("temp"), j_main.getDouble("feels_like"), j_main.getDouble("temp_min"), j_main.getDouble("temp_max"),
                                        j_main.getInt("pressure"), j_main.getInt("sea_level"), j_main.getInt("grnd_level"), j_main.getInt("humidity"),
                                        j_main.getDouble("temp_kf"));

                                JSONArray jarray_weather = jsonObject.getJSONArray("weather");
                                Log.e("RESP5", jarray_weather.toString());
                                ArrayList<Weather> weatherArrayList = new ArrayList<>();

                                for (int j = 0; j < jarray_weather.length(); j++) {
                                    JSONObject j_weather = jarray_weather.getJSONObject(j);
                                    Log.e("RESP6", j_weather.toString());
                                    Weather weather = new Weather(j_weather.getInt("id"), j_weather.getString("main"), j_weather.getString("description"), j_weather.getString("icon"));
                                    weatherArrayList.add(weather);
                                }

                                JSONObject j_clouds = jsonObject.getJSONObject("clouds");
                                Log.e("RESP7", j_clouds.toString());
                                Clouds clouds = new Clouds(j_clouds.getInt("all"));

                                JSONObject j_winds = jsonObject.getJSONObject("wind");

                                Wind wind = new Wind(j_winds.getDouble("speed"), j_winds.getInt("deg"), j_winds.getDouble("gust"));

                                JSONObject j_sys = jsonObject.getJSONObject("sys");

                                Sys sys = new Sys(j_sys.getString("pod"));


                                WList wList = new WList(jsonObject.getInt("dt"), weatherObject, weatherArrayList, clouds, wind, jsonObject.getInt("visibility"), jsonObject.getDouble("pop"), sys, jsonObject.getString("dt_txt"));
                                list.add(wList);
                            }


                            Coord coord = new Coord(j_coord.getDouble("lat"), j_coord.getDouble("lon"));

                            LatLng latLng = new LatLng(coord.lat, coord.lon);
                            String lat[] = Gridmenu.LATLONG.split(",");
                            LatLng userlatlong = new LatLng(Double.valueOf(lat[0]), Double.valueOf(lat[1]));


                            City city = new City(j_city.getInt("id"), j_city.getString("name"), coord, j_city.getString("country"),
                                    j_city.getInt("population"), j_city.getInt("timezone"), j_city.getInt("sunrise"), j_city.getInt("sunset"));


                            RootWeather rootWeather = new RootWeather(response.getString("cod"), response.getInt("message"), response.getInt("cnt"), list, city);

                            ArrayList<Date> dateArrayList = new ArrayList<>();

                            for (WList l : list) {
                                dateArrayList.add(sdf.parse(l.dt_txt));
                            }


                            Date nextdate = calculateDistanceInKilometer(userlatlong, latLng);


                            int index = getNearestDate(dateArrayList, nextdate);
                            Log.e("NEARDATE", String.valueOf(index));

                            int resid = R.drawable.ic_w_clearsky;
                            Log.e("WEATHER", rootWeather.getList().get(0).weather.get(0).description);

                            if (rootWeather.getList().get(index).weather.get(0).description.contains("clear sky")) {
                                resid = R.drawable.ic_w_clearsky;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("few clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(0).weather.get(0).description.contains("scattered clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("broken clouds")) {
                                resid = R.drawable.ic_cloud;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("shower rain")) {
                                resid = R.drawable.ic_w_rain1;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("rain")) {
                                resid = R.drawable.ic_w_rain1;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("thunderstorm")) {
                                resid = R.drawable.ic_w_thunderstorm;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("snow")) {
                                resid = R.drawable.ic_w_snowflake;
                            } else if (rootWeather.getList().get(index).weather.get(0).description.contains("mist")) {
                                resid = R.drawable.ic_w_snowflake;
                            }

                            String weather = rootWeather.getList().get(index).weather.get(0).description
                                    + "\nFeels like " + rootWeather.getList().get(index).weatherObject.feels_like
                                    + "\nTemperature " + rootWeather.getList().get(index).weatherObject.temp
                                    + "\ntemp min " + rootWeather.getList().get(index).weatherObject.temp_min
                                    + "\ntemp max" + rootWeather.getList().get(index).weatherObject.temp_max;


                            mMap.addMarker(
                                    new MarkerOptions()
                                            .position(latLng)
                                            .title(rootWeather.getList().get(index).weather.get(0).main)
                                            .snippet(weather)
                                            .icon(bitmapDescriptorFromVector(getApplicationContext(), resid)));

                        } catch (Exception e) {

                            Log.e("ERROR", e.toString());

                        }


                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonObjectRequest.setTag(REQUEST_TAG);
        mQueue.add(jsonObjectRequest);

    }


    public static int getNearestDate(ArrayList<Date> dates, Date currentDate) {
        int index = 0, i = 0;
        long minDiff = -1, currentTime = currentDate.getTime();
        Date minDate = null;
        for (Date date : dates) {

            long diff = Math.abs(currentTime - date.getTime());
            if ((minDiff == -1) || (diff < minDiff)) {
                index = i;

                minDiff = diff;
                minDate = date;
            }
            i++;
        }
        return index;
    }


    public Date calculateDistanceInKilometer(LatLng source, LatLng destination) {
        long currenttime = System.currentTimeMillis();


        double AVERAGE_RADIUS_OF_EARTH_KM = 6371;


        double parkLat = 0, parkLng = 0, latDistance, lngDistance, a, c;
        int distance = 0;
        int tempdistance = 9999;

        parkLat = destination.latitude;
        parkLng = destination.longitude;

        latDistance = Math.toRadians(source.latitude - parkLat);
        lngDistance = Math.toRadians(source.longitude - parkLng);

        a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(source.latitude)) * Math.cos(Math.toRadians(parkLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = (int) (Math.round(AVERAGE_RADIUS_OF_EARTH_KM * c));
        Log.e("DISTANCE:", String.valueOf(distance));

        int timeoftravel = (int) distance / 45;


        Date date = new Date();
        date.setTime(date.getTime() + timeoftravel * 3600000);


        return date;


    }


//
//    private class ThreadB extends AsyncTask<Void, Void, JSONObject> {
//        Context mContext;
//        String lat, lon;
//
//        public ThreadB(Context ctx, String lat, String lan) {
//            mContext = ctx;
//            this.lon = lan;
//            this.lat = lat;
//        }
//
//        @Override
//        protected JSONObject doInBackground(Void... params) {
//            final RequestFuture<JSONObject> futureRequest = RequestFuture.newFuture();
//            mQueue = CustomVolleyRequestQueue.getInstance(mContext.getApplicationContext())
//                    .getRequestQueue();
//            String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=4b406562a5c50b1f44bcfbbeb35795af";
//            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method
//                    .GET, url,
//                    new JSONObject(), futureRequest, futureRequest);
//            jsonRequest.setTag(REQUEST_TAG);
//            mQueue.add(jsonRequest);
//            try {
//                return futureRequest.get(10, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
}



