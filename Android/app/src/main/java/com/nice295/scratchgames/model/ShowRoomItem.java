package com.nice295.scratchgames.model;

/**
 * Created by kyuholee on 2016. 9. 4..
 */

public class ShowRoomItem {

    public String id;
    public String name;
    public String user;
    public String imageUrl;

    public ShowRoomItem() {
    }

    public ShowRoomItem(String id, String name, String user, String imageUrl) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.imageUrl = imageUrl;
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

}