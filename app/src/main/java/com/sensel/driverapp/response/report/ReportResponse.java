package com.sensel.driverapp.response.report;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ReportResponse implements Serializable {

    @SerializedName("Link")
    @Expose
    private String Link;

    @SerializedName("Score")
    @Expose
    private String Score;

    @SerializedName("DateTime")
    @Expose
    private String DateTime;

    @SerializedName("Data")
    @Expose
    private String Data;

    @SerializedName("vettingId")
    @Expose
    private String vettingId;

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public String getVettingId() {
        return vettingId;
    }

    public void setVettingId(String vettingId) {
        this.vettingId = vettingId;
    }
}
