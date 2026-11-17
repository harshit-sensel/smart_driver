package com.sensel.driverapp.objectclass.weather;


import com.sensel.driverapp.objectclass.weather.Coord;

public class City{
    public int id;
    public String name;
    public Coord coord;
    public String country;
    public int population;
    public int timezone;
    public int sunrise;
    public int sunset;

    public City(int id, String name, Coord coord, String country, int population, int timezone, int sunrise, int sunset) {
        this.id = id;
        this.name = name;
        this.coord = coord;
        this.country = country;
        this.population = population;
        this.timezone = timezone;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }
}