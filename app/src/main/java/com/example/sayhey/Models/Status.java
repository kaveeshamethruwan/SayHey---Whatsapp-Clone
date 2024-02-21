package com.example.sayhey.Models;

public class Status {

    private String imageURL;
    private long timeStamp;

    public Status() {

    }

    public Status(String imageURL, long timeStamp) {

        this.imageURL = imageURL;
        this.timeStamp = timeStamp;

    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getImageURL() {
        return imageURL;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
