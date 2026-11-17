
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;


public class GetInsertResponse {

    @Expose
    private String result;
    @Expose
    private int statuscode;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(int statuscode) {
        this.statuscode = statuscode;
    }

}
