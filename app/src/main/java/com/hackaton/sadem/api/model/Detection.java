package com.hackaton.sadem.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class Detection {

    private String uuid;
    private String agent_id;
    private float latitude;
    private float longitude;
    private String photo;
    private String fined;

    @SerializedName("marbete_id")
    private Marbete marbete;

    public Marbete getMarbete() {
        return marbete;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAgent_id() {
        return agent_id;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getPhoto() {
        return photo;
    }

    public String getFined() {
        return fined;
    }
}
