
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;


public class Vehicle {

    @Expose
    private String vehicleInfo;
    @Expose
    private String vehicleid;

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getVehicleid() {
        return vehicleid;
    }

    public void setVehicleid(String vehicleid) {
        this.vehicleid = vehicleid;
    }

}
