package com.sensel.driverapp.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.sensel.driverapp.db.model.LoginConfigs;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface LoginConfigDao {

    @Query("select * from LoginConfigs")
    List<LoginConfigs> loadAllConfigs();

    @Query("select * from LoginConfigs where appId = :appId")
    LoginConfigs loadConfigByAppId(String appId);

    @Insert(onConflict = REPLACE)
    void insertConfig(LoginConfigs loginConfig);

    @Delete
    void deleteConfig(LoginConfigs loginConfig);

    @Query("DELETE FROM LoginConfigs where appId = :appId")
    void deleteConfigByAppId(String appId);

    @Query("DELETE FROM LoginConfigs")
    void deleteAll();

    @Insert(onConflict = IGNORE)
    void insertOrReplaceConfig(LoginConfigs... configs);

}