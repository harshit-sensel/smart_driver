
package com.sensel.driverapp.response.report;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Getreportdetails {

    @SerializedName("vetting_Reports")
    private List<VettingReport> vettingReports;

    public List<VettingReport> getVettingReports() {
        return vettingReports;
    }

    public void setVettingReports(List<VettingReport> vettingReports) {
        this.vettingReports = vettingReports;
    }

}
