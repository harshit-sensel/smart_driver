
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;

import java.io.Serializable;


public class FieldValueRequest implements Serializable {

    @Expose
    private String fieldId;

    @Expose
    private String value;

    public FieldValueRequest(String fieldId, String value) {
        this.fieldId = fieldId;
        this.value = value;
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


}
