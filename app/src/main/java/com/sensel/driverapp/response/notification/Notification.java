
package com.sensel.driverapp.response.notification;

import com.google.gson.annotations.Expose;


public class Notification {

    @Expose
    private String datetime;
    @Expose
    private String info;
    @Expose
    private String isnotified;
    @Expose
    private String subject;

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getIsnotified() {
        return isnotified;
    }

    public void setIsnotified(String isnotified) {
        this.isnotified = isnotified;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

}
