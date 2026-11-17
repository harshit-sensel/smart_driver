
package com.sensel.driverapp.response.report;

import com.google.gson.annotations.Expose;

public class ChecklistValue {

    @Expose
    private String checklistHeader;
    @Expose
    private int obtainedScore;
    @Expose
    private int percentage;
    @Expose
    private int totalScore;

    public String getChecklistHeader() {
        return checklistHeader;
    }

    public void setChecklistHeader(String checklistHeader) {
        this.checklistHeader = checklistHeader;
    }

    public int getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(int obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

}
