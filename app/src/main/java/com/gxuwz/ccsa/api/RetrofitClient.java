package com.gxuwz.ccsa.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 确保这里的 IP 是你电脑当前的局域网 IP
    // 确保端口是 8080
    // 确保最后有一个斜杠 /
    private static final String BASE_URL = "http://192.168.115.251:8080/";

    private static RetrofitClient instance;
    private ApiService apiService;

    private RetrofitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ApiService getApi() {
        return apiService;
    }
}