package com.example.sayhey.Models;

import java.util.ArrayList;

public class UserStatus {

    private String name;
    private String profileImage;
    private long lastUpdated;
    private ArrayList<Status>statusList;

    public UserStatus() {

    }

    public UserStatus(String name, String profileImage, long lastUpdated, ArrayList<Status> statusList) {
        this.name = name;
        this.profileImage = profileImage;
        this.lastUpdated = lastUpdated;
        this.statusList = statusList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatusList() {
        return statusList;
    }

    public void setStatusList(ArrayList<Status> statusList) {
        this.statusList = statusList;
    }
}
