package com.nice295.scratchgames.model;

/**
 * Created by kyuholee on 2016. 9. 4..
 */

public class ShowRoomExtItem {

    public Integer viewCount;
    public String deviceId;
    public String reserved1;
    public String reserved2;

    public ShowRoomExtItem() {
    }

    public ShowRoomExtItem(String id, Integer viewCount, String deviceId, String reserved1, String reserved2) {
        this.viewCount = viewCount;
        this.deviceId = deviceId;
        this.reserved1 = reserved1;
        this.reserved2 = reserved2;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getReserved1() {
        return reserved1;
    }

    public void setReserved1(String reserved1) {
        this.reserved1 = reserved1;
    }

    public String getReserved2() {
        return reserved2;
    }

    public void setReserved2(String reserved2) {
        this.reserved2 = reserved2;
    }
}