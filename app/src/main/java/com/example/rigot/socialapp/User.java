package com.example.rigot.socialapp;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rigot on 11/16/2016.
 */

public class User implements Serializable{

    String uId, firstName, lastName, gender, imagePath;


    public User(String firstName, String lastName, String gender, String image) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.imagePath = image;
    }

    public User(){

    }


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String image) {
        this.imagePath = image;
    }
}
