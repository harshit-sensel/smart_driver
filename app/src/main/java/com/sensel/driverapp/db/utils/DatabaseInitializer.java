package com.sensel.driverapp.db.utils;


import android.os.AsyncTask;

import com.sensel.driverapp.db.AppDatabase;
import com.sensel.driverapp.db.model.VehicleTypeChecklist;


public class DatabaseInitializer {

    public static void populateAsync(final AppDatabase db) {
        PopulateVehicleTypeChecklistAsync task = new PopulateVehicleTypeChecklistAsync(db);
        task.execute();
    }

   /* private static VehicleVetting addVehicleVetting(final AppDatabase db, final String id, final String name, final String lastName, final int age) {
        VehicleVetting vehicleVetting = new VehicleVetting();
        vehicleVetting.id = id;
        vehicleVetting.age = age;
        vehicleVetting.name = name;
        vehicleVetting.lastName = lastName;
        db.vehicleVettingModel().insertVetting(vehicleVetting);
        return vehicleVetting;
    }*/

   /* private static VehicleTypeChecklist addVehicleTypeChecklist(final AppDatabase db, final String id, final String name, final String lastName, final int age) {
        VehicleTypeChecklist vehicleTypeChecklist = new VehicleTypeChecklist();
        vehicleTypeChecklist.id = id;
        vehicleTypeChecklist.name = name;
        vehicleTypeChecklist.lastName = lastName;
        db.vehicleTypeChecklistModel().insertVehicleTypeChecklist(vehicleTypeChecklist);
        return vehicleTypeChecklist;
    }*/

    private static void clearDB(AppDatabase db) {
        db.vehicleVettingModel().deleteAll();
        db.vehicleTypeChecklistModel().deleteAll();
    }

    private static class PopulateVehicleTypeChecklistAsync extends AsyncTask<VehicleTypeChecklist, Void, Void> {

        private final AppDatabase mDb;

        PopulateVehicleTypeChecklistAsync(AppDatabase db) {
            mDb = db;
        }

        @Override
        protected Void doInBackground(VehicleTypeChecklist... vehicleTypeChecklists) {
            mDb.vehicleTypeChecklistModel().insertVehicleTypeChecklist(vehicleTypeChecklists[0]);
            return null;
        }
    }
}
