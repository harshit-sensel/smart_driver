package com.sensel.driverapp.db.model;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;


import com.sensel.driverapp.db.utils.VettingDataConverter;
import com.sensel.driverapp.request.ChecklistValues;
import com.sensel.driverapp.request.FieldValueRequest;
import com.sensel.driverapp.request.StaticDetails;
import com.sensel.driverapp.response.MultipleImageDetailsWrapper;

import java.io.Serializable;
import java.util.List;

@Entity
public class VehicleVetting implements Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long id;

    @TypeConverters(VettingDataConverter.class)
    public StaticDetails staticDetails;

    @TypeConverters(VettingDataConverter.class)
    public List<ChecklistValues> checklistValues;

    @TypeConverters(VettingDataConverter.class)
    public List<MultipleImageDetailsWrapper> imageList;

    @TypeConverters(VettingDataConverter.class)
    public List<FieldValueRequest> fieldValues;

    public String sessionid;

    public String appName;

    public Boolean isUploaded;

}
