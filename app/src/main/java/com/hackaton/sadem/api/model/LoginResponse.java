package com.hackaton.sadem.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class LoginResponse extends BaseResponse{

    @Expose
    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }
}