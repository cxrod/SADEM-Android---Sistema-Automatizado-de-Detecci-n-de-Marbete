package com.hackaton.sadem.api;

import com.hackaton.sadem.api.model.LoginRequest;
import com.hackaton.sadem.api.model.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public interface ApiService {

    @POST("get-token/")
    Call<LoginResponse> doLoginApiCall(@Body LoginRequest request);

//    @POST("588d15d3100000ae072d2944")
//    Observable<LoginResponse> doFacebookLoginApiCall(@Body LoginRequest.FacebookLoginRequest request);
//
//    @POST("588d161c100000a9072d2946")
//    Observable<BaseResponse> doLogoutApiCall();
//
//    @GET("5926ce9d11000096006ccb30")
//    Observable<BlogResponse> getBlogApiCall();
//
//    @GET("5926c34212000035026871cd")
//    Observable<OpenSourceResponse> getOpenSourceApiCall();
}