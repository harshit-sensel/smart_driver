package com.sensel.driverapp.objectclass;

public class Object_LatLong {
    private String latitude;
    private String longitude;
    private String color;
    private String colorCode;

    public Object_LatLong(String latitude, String longitude, String color, String colorCode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.color = color;
        this.colorCode = colorCode;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getColor() {
        return color;
    }

    public String getColorCode() {
        return colorCode;
    }
}


