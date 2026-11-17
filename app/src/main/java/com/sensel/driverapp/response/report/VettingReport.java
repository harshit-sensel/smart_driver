
package com.sensel.driverapp.response.report;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class VettingReport {

    @SerializedName("avg_score")
    private int avgScore;
    @SerializedName("checklist_Values")
    private List<ChecklistValue> checklistValues;
    @Expose
    private String dateTime;
    @Expose
    private String vehicleId;
    @SerializedName("vehicleType")
    @Expose
    private String vehicleType;
    @SerializedName("filePath")
    @Expose
    private String filePath;
    @Expose
    private String vettingId;

    public int getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(int avgScore) {
        this.avgScore = avgScore;
    }

    public List<ChecklistValue> getChecklistValues() {
        return checklistValues;
    }

    public void setChecklistValues(List<ChecklistValue> checklistValues) {
        this.checklistValues = checklistValues;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVettingId() {
        return vettingId;
    }

    public void setVettingId(String vettingId) {
        this.vettingId = vettingId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
