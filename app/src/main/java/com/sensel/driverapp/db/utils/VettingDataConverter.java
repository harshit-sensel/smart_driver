package com.sensel.driverapp.db.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sensel.driverapp.request.ChecklistValues;
import com.sensel.driverapp.request.FieldValueRequest;
import com.sensel.driverapp.request.StaticDetails;
import com.sensel.driverapp.response.MultipleImageDetailsWrapper;


import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

public class VettingDataConverter implements Serializable {

    @TypeConverter
    public String fromChecklist(List<ChecklistValues> optionValues) {
        if (optionValues == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChecklistValues>>() {
        }.getType();
        String json = gson.toJson(optionValues, type);
        return json;
    }

    @TypeConverter
    public List<ChecklistValues> toChecklist(String optionValuesString) {
        if (optionValuesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<ChecklistValues>>() {
        }.getType();
        List<ChecklistValues> productCategoriesList = gson.fromJson(optionValuesString, type);
        return productCategoriesList;
    }

    @TypeConverter
    public String fromImageList(List<MultipleImageDetailsWrapper> imageList) {
        if (imageList == null) {
            return (null);
        }
        //to ignore the URI field from serialization and deserialization build GSON using following method
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        Type type = new TypeToken<List<MultipleImageDetailsWrapper>>() {
        }.getType();
        String json = gson.toJson(imageList, type);
        return json;
    }

    @TypeConverter
    public List<MultipleImageDetailsWrapper> toImageList(String imageListString) {
        if (imageListString == null) {
            return (null);
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.excludeFieldsWithoutExposeAnnotation().create();
        Type type = new TypeToken<List<MultipleImageDetailsWrapper>>() {
        }.getType();
        List<MultipleImageDetailsWrapper> imageList = gson.fromJson(imageListString, type);
        return imageList;
    }

    @TypeConverter
    public String fromStaticDetails(StaticDetails staticDetails) {
        if (staticDetails == null) {
            return (null);
        }
        return new Gson().toJson(staticDetails);
    }

    @TypeConverter
    public StaticDetails toStaticDetails(String staticDetailsString) {
        if (staticDetailsString == null) {
            return (null);
        }
        return new Gson().fromJson(staticDetailsString, StaticDetails.class);
    }

    @TypeConverter
    public String fromFieldValues(List<FieldValueRequest> optionValues) {
        if (optionValues == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<FieldValueRequest>>() {
        }.getType();
        return gson.toJson(optionValues, type);
    }

    @TypeConverter
    public List<FieldValueRequest> toFieldValues(String optionValuesString) {
        if (optionValuesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<FieldValueRequest>>() {
        }.getType();
        return gson.fromJson(optionValuesString, type);
    }

}