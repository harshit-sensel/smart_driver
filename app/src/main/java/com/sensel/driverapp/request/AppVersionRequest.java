
package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;


public class AppVersionRequest {

    @Expose
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
