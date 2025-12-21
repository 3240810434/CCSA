package com.gxuwz.ccsa.api;

import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.common.Result;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    // 对应后端的 /api/admin/login
    @POST("api/admin/login")
    Call<Result<Admin>> adminLogin(@Body Admin admin);

    // 对应后端的 /api/user/login
    @POST("api/user/login")
    Call<Result<User>> userLogin(@Body User user);

    // 对应后端的 /api/user/register
    @POST("api/user/register")
    Call<Result<User>> userRegister(@Body User user);
}