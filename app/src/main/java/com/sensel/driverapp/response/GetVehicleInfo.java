
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;

public class GetVehicleInfo {

    @Expose
    private List<Vehicle> vehicles;

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

}
