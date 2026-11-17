
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Field {

    @Expose
    private String headerName;

    @Expose
    private List<FieldValues> fieldValues;

    //for internal use
    private Boolean listStatus = true;

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public List<FieldValues> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(List<FieldValues> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public Boolean getListStatus() {
        return listStatus;
    }

    public void setListStatus(Boolean listStatus) {
        this.listStatus = listStatus;
    }
}
