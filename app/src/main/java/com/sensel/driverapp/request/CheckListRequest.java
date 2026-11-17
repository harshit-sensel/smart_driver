
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class CheckListRequest {

    @SerializedName("sessionid")
    @Expose
    private String sessionid;
    @SerializedName("truckType")
    @Expose
    private String truckType;

    @SerializedName("appId")
    @Expose
    private String appId;

    @SerializedName("imei")
    @Expose
    private String imei;

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getTruckType() {
        return truckType;
    }

    public void setTruckType(String truckType) {
        this.truckType = truckType;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}
