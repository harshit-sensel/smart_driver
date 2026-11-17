package com.sensel.driverapp.db.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.sensel.driverapp.db.model.VehicleTypeChecklist;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface VehicleTypeChecklistDao {

    @Query("select * from VehicleTypeChecklist")
    List<VehicleTypeChecklist> loadAllVehicleTypeChecklist();

    @Query("select * from VehicleTypeChecklist where vehicleType = :vehicleType")
    VehicleTypeChecklist loadChecklistById(String vehicleType);

    //@Query("select * from VehicleTypeChecklist where vehicleType = :vehicleType")
    //List<VehicleTypeChecklist> findChecklistByVehicleType(String vehicleType);

    @Insert(onConflict = REPLACE)
    void insertVehicleTypeChecklist(VehicleTypeChecklist vehicleTypeChecklist);

    @Delete
    void deleteVehicleTypeChecklist(VehicleTypeChecklist vehicleTypeChecklist);

    @Query("DELETE FROM VehicleTypeChecklist")
    void deleteAll();

    @Query("delete from VehicleTypeChecklist where vehicleType = :vehicleType")
    int deleteChecklistByVehicleType(String vehicleType);

    @Insert(onConflict = IGNORE)
    void insertOrReplaceUsers(VehicleTypeChecklist... vehicleTypeChecklists);

}