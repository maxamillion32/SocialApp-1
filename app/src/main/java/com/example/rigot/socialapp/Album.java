package com.example.rigot.socialapp;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by rigot on 11/18/2016.
 */

public class Album implements Serializable {
    private String title;
    private ArrayList<String> images;

    public Album() {
    }

    @Override
    public String toString() {
        return "Album{" +
                "title='" + title + '\'' +
                ", images=" + images +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }
}
