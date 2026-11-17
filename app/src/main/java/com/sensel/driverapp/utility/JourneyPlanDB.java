package com.sensel.driverapp.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sensel.driverapp.objectclass.Object_LatLong;
import com.sensel.driverapp.objectclass.SelectedParingSpotObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class JourneyPlanDB extends SQLiteOpenHelper {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");

    public JourneyPlanDB(Context context) {
        super(context, "JourneyPlanDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {

        DB.execSQL("create Table ParkingSpots (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, location TEXT, arrival TEXT, leaving TEXT, resttime REAL, latlons TEXT, isComplete TEXT DEFAULT 'No')");
        DB.execSQL("create Table JRPlatlong (latitude TEXT, longitude TEXT, color TEXT, colorcode TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists ParkingSpots");
        DB.execSQL("drop Table if exists JRPlatlong");

    }

    public void insertLatLongs(ArrayList<Object_LatLong> latLongArrayList) {
        SQLiteDatabase DB = this.getWritableDatabase();
        deleteLatLongs();
        for (int i = 0; i < latLongArrayList.size(); i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("latitude", latLongArrayList.get(i).getLatitude());
            contentValues.put("longitude", latLongArrayList.get(i).getLongitude());
            contentValues.put("color",latLongArrayList.get(i).getColor());
            contentValues.put("colorcode",latLongArrayList.get(i).getColorCode());

            DB.insert("JRPlatlong", null, contentValues);
        }
    }

    public void deleteLatLongs() {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.execSQL("delete FROM JRPlatlong");
    }

    public Cursor getLatLong() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from JRPlatlong", null);
        return cursor;
    }


    public void insertdata(ArrayList<SelectedParingSpotObject> spotObject) {
        SQLiteDatabase DB = this.getWritableDatabase();
        for (int i = 0; i < spotObject.size(); i++) {
            String eta, ela;
            eta = sdf.format(spotObject.get(i).getEstimatedArrival());
            ela = sdf.format(spotObject.get(i).getEstimatedLeavingTime());
            ContentValues contentValues = new ContentValues();
            contentValues.put("location", spotObject.get(i).getParkingName());
            contentValues.put("arrival", eta);
            contentValues.put("leaving", ela);
            contentValues.put("resttime", spotObject.get(i).getRestTime());
            contentValues.put("latlons", spotObject.get(i).getLatLan());
            contentValues.put("isComplete", spotObject.get(i).getIsCompleted());

            DB.insert("ParkingSpots", null, contentValues);
        }


    }

    public void updateParkingDone(String location, int id) {
        SQLiteDatabase DB = this.getWritableDatabase();
        String[] args = new String[]{String.valueOf(id)};
        ContentValues contentValues = new ContentValues();
        contentValues.put("isComplete", "Yes");
        DB.update("ParkingSpots", contentValues, "id<= ?", args);
    }

    public Cursor getdata() {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from ParkingSpots", null);
        return cursor;
    }

    public void deletedata() {
        SQLiteDatabase DB = this.getWritableDatabase();
        DB.execSQL("delete FROM ParkingSpots");
    }


}

