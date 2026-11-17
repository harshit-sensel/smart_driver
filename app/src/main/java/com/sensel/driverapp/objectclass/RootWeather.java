package com.sensel.driverapp.objectclass;



import java.util.ArrayList;
import java.util.List;

import com.sensel.driverapp.objectclass.weather.City;
import com.sensel.driverapp.objectclass.weather.WList;

public  class RootWeather{
    public String cod;
    public int message;
    public int cnt;
    public ArrayList<WList> list;
    public City city;

    public RootWeather(String cod, int message, int cnt, ArrayList<WList> list, City city) {
        this.cod = cod;
        this.message = message;
        this.cnt = cnt;
        this.list = list;
        this.city = city;
    }

    public String getCod() {
        return cod;
    }

    public int getMessage() {
        return message;
    }

    public int getCnt() {
        return cnt;
    }

    public List<WList> getList() {
        return list;
    }

    public City getCity() {
        return city;
    }
}
