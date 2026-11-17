package com.sensel.driverapp.objectclass;

public class LockScreenParkingSpots {
    int id;
    String parkingName;
    String isComplete;
    String latLong;

    public LockScreenParkingSpots(int id, String parkingName, String latLong, String isComplete) {
        this.id = id;
        this.parkingName = parkingName;
        this.latLong = latLong;
        this.isComplete = isComplete;

    }

    public int getId() {
        return id;
    }

    public String getParkingName() {
        return parkingName;
    }

    public String getIsComplete() {
        return isComplete;
    }

    public String getLatLong() {
        return latLong;
    }
}
