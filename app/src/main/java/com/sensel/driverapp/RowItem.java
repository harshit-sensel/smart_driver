package com.sensel.driverapp;

/**
 * Created by mallikarjun on 18-Apr-16.
 */
public class RowItem {
    private String imageId;
    private int image;
    private String title;
    private String desc;
    private String address;

    public RowItem(String imageId, String title, String desc, String address) {
        this.imageId = imageId;
        this.title = title;
        this.desc = desc;
        this.address=address;
    }
    public RowItem(int image, String rank, String name, String points) {
        this.image = image;
        this.title = rank;
        this.desc = name;
        this.address=points;
    }

    public RowItem(int image, String title, String count) {
        this.image = image;
        this.title = title;
        this.desc = count;
    }

    public String getImageId() {
        return imageId;
    }

    public int NgetImageId() {
        return image;
    }
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getAddress() {
        return address;
    }
    @Override
    public String toString() {
        return title + " " + desc +" "+address;
    }
}