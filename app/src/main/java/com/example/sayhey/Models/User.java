package com.example.sayhey.Models;

public class User {

    private String uid;
    private String name;
    private String phoneNumber;
    private String profileImage;

    public User() {

    }

    public User(String uid, String name, String phoneNumber, String profileImage) {

        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImage = profileImage;

    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfileImage() {
        return profileImage;
    }

}
