package com.hackaton.sadem.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by cesar_000 on 18/11/2017.
 */

public class ApiHelper {

    private static String BASE_URL = "http://10.193.0.97:8000/api/v1/";


    /**
     * Helper class that sets up a new services
     **/
    public static ApiService newApiService(final String accessToken) {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {

                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder();

                if (accessToken != null){
                    builder.header("Authorization", "Token "+accessToken);
                }

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        })
        .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        return retrofit.create(ApiService.class);
    }
}
