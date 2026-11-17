package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

public class AppVersionResponse {

    @Expose
    private String AppName;
    @Expose
    private String VersionCode;
    @Expose
    private String Priority;
    @Expose
    private String StableVersion;
    @Expose
    private String DomainUrl;
    @Expose
    private String GoogleApiClientId;

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName) {
        AppName = appName;
    }

    public String getVersionCode() {
        return VersionCode;
    }

    public void setVersionCode(String versionCode) {
        VersionCode = versionCode;
    }

    public String getPriority() {
        return Priority;
    }

    public void setPriority(String priority) {
        Priority = priority;
    }

    public String getStableVersion() {
        return StableVersion;
    }

    public void setStableVersion(String stableVersion) {
        StableVersion = stableVersion;
    }

    public String getDomainUrl() {
        return DomainUrl;
    }

    public void setDomainUrl(String domainUrl) {
        DomainUrl = domainUrl;
    }

    public String getGoogleApiClientId() {
        return GoogleApiClientId;
    }

    public void setGoogleApiClientId(String googleApiClientId) {
        GoogleApiClientId = googleApiClientId;
    }
}
