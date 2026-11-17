
package com.sensel.driverapp.response.firebase;

import com.google.gson.annotations.Expose;


public class FirebaseResponse {

    @Expose
    private String result;
    @Expose
    private String statuscode;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(String statuscode) {
        this.statuscode = statuscode;
    }

}
