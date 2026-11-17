package com.sensel.driverapp.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;


import com.sensel.driverapp.db.model.VehicleVetting;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface VehicleVettingDao {

    @Query("select * from VehicleVetting")
    List<VehicleVetting> loadAllVetting();

    @Query("select * from VehicleVetting where id = :id")
    VehicleVetting loadVettingById(Long id);

    @Insert(onConflict = REPLACE)
    void insertVetting(VehicleVetting vehicleVetting);

    @Delete
    void deleteVetting(VehicleVetting vehicleVetting);

    @Query("DELETE FROM VehicleVetting where id = :id")
    void deleteVettingById(Long id);

    @Query("DELETE FROM VehicleVetting")
    void deleteAll();

    @Insert(onConflict = IGNORE)
    void insertOrReplaceVetting(VehicleVetting... vehicleVetting);

}