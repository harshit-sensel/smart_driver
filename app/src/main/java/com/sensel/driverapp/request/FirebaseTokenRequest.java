
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class FirebaseTokenRequest {

    @SerializedName("AppName")
    private String appName;
    @Expose
    private String sessionid;
    @SerializedName("Token")
    private String token;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
