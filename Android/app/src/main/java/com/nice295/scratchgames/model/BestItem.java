package com.nice295.scratchgames.model;

/**
 * Created by kyuholee on 2016. 9. 4..
 */

public class BestItem {

    public String id;
    public String name;
    public String desc;
    public String user;
    public int star;
    public String imageUrl;
    public String imageProfile;


    public BestItem(String id, String name, String desc, String user, int star, String imageUrl, String imageProfile) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.user = user;
        this.star = star;
        this.imageUrl = imageUrl;
        this.imageProfile = imageProfile;
    }


    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfle) {
        this.imageProfile = imageProfle;
    }

    public BestItem() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {

        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {

        return user;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}