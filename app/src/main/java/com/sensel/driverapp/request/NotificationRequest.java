
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;


public class NotificationRequest {

    @Expose
    private String sessionid;

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

}
