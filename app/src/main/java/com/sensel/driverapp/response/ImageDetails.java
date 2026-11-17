package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class ImageDetails implements Serializable {

    /**
     * only for offline use as URI can't be stored directly in DB we are using this variable
     * String s = mUri.toString(); // uri in a string format
     * Uri mUri = Uri.parse(s); // decode the string into a URI
     */

    @Expose(serialize = true, deserialize = true)
    String finalFile;

    @Expose(serialize = true, deserialize = true)
    String id;

    @Expose(serialize = true, deserialize = true)
    String fileName;

    @Expose(serialize = true, deserialize = true)
    String sessionid;

    //only used for updating record in database
    private Long dbRowId;

    public String getFinalFile() {
        return finalFile;
    }

    public void setFinalFile(String finalFile) {
        this.finalFile = finalFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

}
