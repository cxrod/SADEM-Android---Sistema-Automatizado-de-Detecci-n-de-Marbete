package com.hackaton.sadem.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class DgiiResponse extends BaseResponse{

    @SerializedName("data")
    private Marbete marbete;

    public Marbete getMarbete() {
        return marbete;
    }

}
