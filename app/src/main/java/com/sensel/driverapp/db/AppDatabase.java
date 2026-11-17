package com.sensel.driverapp.db;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.sensel.driverapp.db.dao.LoginConfigDao;
import com.sensel.driverapp.db.dao.VehicleTypeChecklistDao;
import com.sensel.driverapp.db.dao.VehicleVettingDao;
import com.sensel.driverapp.db.model.LoginConfigs;
import com.sensel.driverapp.db.model.VehicleTypeChecklist;
import com.sensel.driverapp.db.model.VehicleVetting;


@Database(entities = {VehicleTypeChecklist.class, VehicleVetting.class, LoginConfigs.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract VehicleTypeChecklistDao vehicleTypeChecklistModel();

    public abstract VehicleVettingDao vehicleVettingModel();

    public abstract LoginConfigDao loginConfigModel();

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE LoginConfigs (appId TEXT PRIMARY KEY NOT NULL, name TEXT)");
            database.execSQL("ALTER TABLE VehicleVetting ADD COLUMN fieldValues TEXT");
            database.execSQL("ALTER TABLE VehicleVetting ADD COLUMN appName TEXT");
        }
    };

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "VehicleVetting.db")
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_2_3)
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

}