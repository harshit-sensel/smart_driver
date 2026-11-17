package com.sensel.driverapp.objectclass.weather;




import com.sensel.driverapp.objectclass.weather.Clouds;
import com.sensel.driverapp.objectclass.weather.Rain;
import com.sensel.driverapp.objectclass.weather.Sys;
import com.sensel.driverapp.objectclass.weather.Weather;
import com.sensel.driverapp.objectclass.weather.WeatherObject;
import com.sensel.driverapp.objectclass.weather.Wind;

import java.util.ArrayList;
import java.util.List;

public class  WList{
    public int dt;
    public WeatherObject weatherObject;
    public ArrayList<Weather> weather;
    public Clouds clouds;
    public Wind wind;
    public int visibility;
    public double pop;
    public Sys sys;
    public String dt_txt;


    public WList(int dt, WeatherObject weatherObject, ArrayList<Weather> weather, Clouds clouds, Wind wind, int visibility, double pop, Sys sys, String dt_txt) {
        this.dt = dt;
        this.weatherObject = weatherObject;
        this.weather = weather;
        this.clouds = clouds;
        this.wind = wind;
        this.visibility = visibility;
        this.pop = pop;
        this.sys = sys;
        this.dt_txt = dt_txt;

    }
}