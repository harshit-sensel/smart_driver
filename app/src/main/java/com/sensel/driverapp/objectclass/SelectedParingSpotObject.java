package com.sensel.driverapp.objectclass;

import java.util.Date;

public class SelectedParingSpotObject {
    private String parkingName;
    private Date estimatedArrival;
    private Date estimatedLeavingTime;
    private String actualArrival;
    private String isEditable;
    private String latLan;
    private float restTime;
    private float actualRestTime;
    private String EntryATA;
    private String EXITATA;
    private String IsCompleted;





    public SelectedParingSpotObject(String parkingName, Date estimatedArrival, Date estimatedLeavingTime, String actualArrival, String isEditable, String latLan, float restTime, float actualRestTime, String entryATA, String EXITATA, String isCompleted) {
        this.parkingName = parkingName;
        this.estimatedArrival = estimatedArrival;
        this.estimatedLeavingTime = estimatedLeavingTime;
        this.actualArrival = actualArrival;
        this.isEditable = isEditable;
        this.latLan = latLan;
        this.restTime = restTime;
        this.actualRestTime = actualRestTime;
        this.EntryATA = entryATA;
        this.EXITATA = EXITATA;
        this.IsCompleted = isCompleted;
    }


    public String getParkingName() {
        return parkingName;
    }

    public Date getEstimatedArrival() {
        return estimatedArrival;
    }

    public String getActualArrival() {
        return actualArrival;
    }

    public String getisEditable() {
        return isEditable;
    }

    public float getRestTime() {
        return restTime;
    }

    public String getLatLan() {
        return latLan;
    }

    public Date getEstimatedLeavingTime() {
        return estimatedLeavingTime;
    }

    public String getIsEditable() {
        return isEditable;
    }

    public float getActualRestTime() {
        return actualRestTime;
    }

    public String getEntryATA() {
        return EntryATA;
    }

    public String getEXITATA() {
        return EXITATA;
    }

    public String getIsCompleted() {
        return IsCompleted;
    }




    @Override
    public String toString() {
        return parkingName;
    }

}
