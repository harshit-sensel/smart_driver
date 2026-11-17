package com.sensel.driverapp.response;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;

public class MultipleImageDetailsWrapper implements Serializable {

    @Expose(serialize = true, deserialize = true)
    private String id;

    //only used for updating record in database
    private Long dbRowId;

    @Expose(serialize = true, deserialize = true)
    private ArrayList<ImageDetails> images;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDbRowId() {
        return dbRowId;
    }

    public void setDbRowId(Long dbRowId) {
        this.dbRowId = dbRowId;
    }

    public ArrayList<ImageDetails> getImages() {
        return images;
    }

    public void setImages(ArrayList<ImageDetails> images) {
        this.images = images;
    }
}
