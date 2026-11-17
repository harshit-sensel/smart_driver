package com.sensel.driverapp.objectclass.weather;

public class Wind{
    public double speed;
    public int deg;
    public double gust;

    public Wind(double speed, int deg, double gust) {
        this.speed = speed;
        this.deg = deg;
        this.gust = gust;
    }
}