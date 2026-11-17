package com.sensel.driverapp.db.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class LoginConfigs implements Serializable {

    @PrimaryKey//(autoGenerate = true)
    @NonNull
    //public Long id;
    public String appId;

    public String name;

    @NonNull
    public String getAppId() {
        return appId;
    }

    public void setAppId(@NonNull String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
