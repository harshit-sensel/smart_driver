package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SaveRequest {

    @SerializedName("staticDetails")
    @Expose
    private StaticDetails staticDetails;

    @SerializedName("checklistValues")
    @Expose
    private List<ChecklistValues> checklistValues;

    @SerializedName("fieldValues")
    @Expose
    private List<FieldValueRequest> fieldValues;

    @SerializedName("sessionid")
    @Expose
    private String sessionid;

    //only used for updating record in database
    private Long dbRowId;

    public StaticDetails getStaticDetails() {
        return staticDetails;
    }

    public void setStaticDetails(StaticDetails staticDetails) {
        this.staticDetails = staticDetails;
    }

    public List<ChecklistValues> getChecklistValues() {
        return checklistValues;
    }

    public void setChecklistValues(List<ChecklistValues> checklistValues) {
        this.checklistValues = checklistValues;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public Long getDbRowId() {
        return dbRowId;
    }

    public void setDbRowId(Long dbRowId) {
        this.dbRowId = dbRowId;
    }

    public List<FieldValueRequest> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<FieldValueRequest> fieldValues) {
        this.fieldValues = fieldValues;
    }
}
