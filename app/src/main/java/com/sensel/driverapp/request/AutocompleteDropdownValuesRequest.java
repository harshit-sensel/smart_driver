
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class AutocompleteDropdownValuesRequest {

    @SerializedName("sessionId")
    @Expose
    private String sessionId;

    @SerializedName("appId")
    @Expose
    private String appId;

    @SerializedName("fieldId")
    @Expose
    private String fieldId;

    @SerializedName("imei")
    @Expose
    private String imei;

    @SerializedName("value")
    @Expose
    private String value;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }
}
