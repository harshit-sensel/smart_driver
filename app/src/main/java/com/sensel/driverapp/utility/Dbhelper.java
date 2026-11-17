package com.sensel.driverapp.utility;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class Dbhelper extends SQLiteOpenHelper {
    public Dbhelper(Context context) {
        super(context, "Worktimelog.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {

        DB.execSQL("create Table Worktimelog (status TEXT, time TEXT, flag TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int oldVersion, int newVersion) {
        DB.execSQL("drop Table if exists Worktimelog");

    }


    public Boolean insertdata(String status, String time,String flag){
        SQLiteDatabase DB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put("status",status);
        contentValues.put("time",time);
        contentValues.put("flag",flag);

        long result=DB.insert("Worktimelog",null,contentValues);
        if (result==-1){
            return false;
        }
        else {
            return true;
        }


    }
    public Cursor getdata(){
        SQLiteDatabase DB=this.getWritableDatabase();
        Cursor cursor=DB.rawQuery("select * from Worktimelog where flag='no' order by  time ASC limit 1",null);
        return cursor;
    }

    public void deletedata(){
        SQLiteDatabase DB=this.getWritableDatabase();


        DB.execSQL("delete FROM Worktimelog where flag='yes'");
    }

    public void updateyes(){
        SQLiteDatabase DB=this.getWritableDatabase();
        DB.execSQL("update Worktimelog  set flag ='yes' where flag='no' and time in (select time from Worktimelog order by time ASC limit 1) ");
    }
    public void updateno(){
        SQLiteDatabase DB=this.getWritableDatabase();
        DB.execSQL("update Worktimelog set flag ='no' ");
    }



}

