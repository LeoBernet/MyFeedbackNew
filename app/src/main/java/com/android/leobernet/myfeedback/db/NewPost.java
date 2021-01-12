package com.android.leobernet.myfeedback.db;

import java.io.Serializable;

public class NewPost implements Serializable {
    private String imageId;
    private String im_id2;
    private String im_id3;
    private String title;
    private String name;
    private String address;
    private String disc;
    private String key;
    private String uid;
    private String time;
    private String cat;
    private String total_views;

    public String getIm_id2() {
        return im_id2;
    }

    public void setIm_id2(String im_id2) {
        this.im_id2 = im_id2;
    }

    public String getIm_id3() {
        return im_id3;
    }

    public void setIm_id3(String im_id3) {
        this.im_id3 = im_id3;
    }

    public String getTotal_views() {
        return total_views;
    }

    public void setTotal_views(String total_views) {
        this.total_views = total_views;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDisc() {
        return disc;
    }

    public void setDisc(String disc) {
        this.disc = disc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
