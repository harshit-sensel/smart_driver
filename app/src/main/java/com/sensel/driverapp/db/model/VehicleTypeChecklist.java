package com.sensel.driverapp.db.model;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.sensel.driverapp.db.utils.ChecklistDataConverter;
import com.sensel.driverapp.response.Checklist;


import java.util.List;

@Entity
public class VehicleTypeChecklist {
    @PrimaryKey
    @NonNull
    //public String id;
    public String vehicleType;

    @TypeConverters(ChecklistDataConverter.class)
    public List<Checklist> checklists;

    public int checklistcount;
}
