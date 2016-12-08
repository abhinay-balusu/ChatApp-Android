package com.abhinaybalusu.messagingapp;

/**
 * Created by abhinaybalusu on 11/16/16.
 */
public class User {

    String fName, lName, email, profileIcon, dateCreated, gender;
    int unreadMessagesCount;

    public User(String fName, String lName, String email, String profileIcon, String dateCreated, String gender) {
        this.fName = fName;
        this.lName = lName;
        this.email = email;
        this.profileIcon = profileIcon;
        this.dateCreated = dateCreated;
        this.gender = gender;
    }

    public User()
    {

    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getlName() {
        return lName;
    }

    public void setlName(String lName) {
        this.lName = lName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileIcon() {
        return profileIcon;
    }

    public void setProfileIcon(String profileIcon) {
        this.profileIcon = profileIcon;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }
}
