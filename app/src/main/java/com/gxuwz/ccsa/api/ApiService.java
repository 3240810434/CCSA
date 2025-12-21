package com.gxuwz.ccsa.api;

import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.common.Result; // 下面会创建这个类
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/admin/login")
    Call<Result<Admin>> adminLogin(@Body Admin admin);

    @POST("api/user/login")
    Call<Result<User>> userLogin(@Body User user);

    @POST("api/user/register")
    Call<Result<User>> userRegister(@Body User user);
}
