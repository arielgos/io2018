package com.agos.ioextended2018.model;

import java.io.Serializable;
import java.util.Date;

public class Item implements Serializable {

    private String date;
    private String user;
    private String userImage;
    private String url;
    private String labels;
    private String text;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Item{" +
                "date='" + date + '\'' +
                ", user='" + user + '\'' +
                ", userImage='" + userImage + '\'' +
                ", url='" + url + '\'' +
                ", labels='" + labels + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
