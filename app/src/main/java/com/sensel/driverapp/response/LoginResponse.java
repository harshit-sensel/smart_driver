
package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;


public class LoginResponse {

    @Expose
    private String domain1;
    @Expose
    private String domain2;
    @Expose
    private String name;
    @Expose
    private String result;
    @Expose
    private String sessionid;

    public String getDomain1() {
        return domain1;
    }

    public void setDomain1(String domain1) {
        this.domain1 = domain1;
    }

    public String getDomain2() {
        return domain2;
    }

    public void setDomain2(String domain2) {
        this.domain2 = domain2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

}
