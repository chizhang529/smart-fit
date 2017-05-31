package com.example.jangwonsuh.smartfittingmerchant;

/**
 * Created by kilas on 2017/5/17.
 */

public class CustomerRequest {
    private String room;
    private String size;
    private String color;
    private String catalog;
    private String imageURL;

    public CustomerRequest() {}

    public CustomerRequest(String room, String size, String color, String catalog, String imageURL) {
        this.room = room;
        this.size = size;
        this.color = color;
        this.catalog = catalog;
        this.imageURL = imageURL;
    }

    public String getRoom() {
        return room;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
