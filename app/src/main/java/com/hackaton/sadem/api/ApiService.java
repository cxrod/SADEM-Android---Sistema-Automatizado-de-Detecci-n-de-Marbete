package com.hackaton.sadem.api;

import com.hackaton.sadem.api.model.DetectionResponse;
import com.hackaton.sadem.api.model.DgiiResponse;
import com.hackaton.sadem.api.model.LoginRequest;
import com.hackaton.sadem.api.model.LoginResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public interface ApiService {

    @POST("get-token/")
    Call<LoginResponse> doLoginApiCall(@Body LoginRequest request);

    @GET("dgii_query/{planteNumber}/")
    Call<DgiiResponse> doDegiiQuery(@Path("planteNumber") String planteNumber,
                                    @QueryMap Map<String, String> coordenates);

    @Multipart
    @PUT("detection/{uuid}")
    Call<DetectionResponse> doDetectionPut(@Path("uuid") String uuid,
                                           @Part  MultipartBody.Part image,
                                           @Part("fined") RequestBody fined);
}