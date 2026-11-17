package com.sensel.driverapp.objectclass;

public class ParkingSpots {
    public String SpotName;
    public String latlan;


    public ParkingSpots(String spotName, String latlan) {
        SpotName = spotName;
        this.latlan = latlan;

    }

    public String getSpotName() {
        return SpotName;
    }

    public String getLatlan() {
        return latlan;
    }

}


