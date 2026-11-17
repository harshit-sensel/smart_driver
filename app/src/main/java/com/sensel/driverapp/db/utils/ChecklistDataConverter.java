package com.sensel.driverapp.db.utils;

import androidx.room.TypeConverter;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sensel.driverapp.response.Checklist;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

public class ChecklistDataConverter implements Serializable {

    @TypeConverter
    public String fromChecklist(List<Checklist> optionValues) {
        if (optionValues == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Checklist>>() {
        }.getType();
        String json = gson.toJson(optionValues, type);
        return json;
    }

    @TypeConverter
    public List<Checklist> toChecklist(String optionValuesString) {
        if (optionValuesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Checklist>>() {
        }.getType();
        List<Checklist> productCategoriesList = gson.fromJson(optionValuesString, type);
        return productCategoriesList;
    }

}