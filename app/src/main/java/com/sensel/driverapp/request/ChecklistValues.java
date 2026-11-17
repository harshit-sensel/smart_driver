package com.sensel.driverapp.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ChecklistValues {

    @SerializedName("Score")
    @Expose
    private String Score;
    @SerializedName("Comments")
    @Expose
    private String Comments;
    @SerializedName("RuleId")
    @Expose
    private String RuleId;
    @SerializedName("Photo")
    @Expose
    private String Photo;

    private ArrayList<String> photos;

    public String getScore() {
        return Score;
    }

    public void setScore(String score) {
        Score = score;
    }

    public String getComments() {
        return Comments;
    }

    public void setComments(String comments) {
        Comments = comments;
    }

    public String getRuleId() {
        return RuleId;
    }

    public void setRuleId(String ruleId) {
        RuleId = ruleId;
    }

    public String getPhoto() {
        return Photo;
    }

    public void setPhoto(String photo) {
        Photo = photo;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void setPhotos(ArrayList<String> photos) {
        this.photos = photos;
        //set the comma separated values photo parameter
        if (photos != null && !photos.isEmpty()) {
            StringBuilder sbString = new StringBuilder("");
            //iterate through ArrayList
            for (String photo : photos) {
                //photo = photo.replace(".jpg", "");
                //append ArrayList element followed by comma
                sbString.append(photo).append(",");
            }
            //convert StringBuffer to String
            Photo = sbString.toString();
            //remove last comma from String if you want
            if (Photo.length() > 0) {
                Photo = Photo.substring(0, Photo.length() - 1);
            }
        }
    }
}
