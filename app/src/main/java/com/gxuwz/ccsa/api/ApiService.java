package com.gxuwz.ccsa.api;

import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.Merchant; // 确保导入 Merchant
import com.gxuwz.ccsa.model.User;
import com.gxuwz.ccsa.common.Result;
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

    // --- 新增商家接口 ---

    @POST("api/merchant/login")
    Call<Result<Merchant>> merchantLogin(@Body Merchant merchant);

    @POST("api/merchant/register")
    Call<Result<Merchant>> merchantRegister(@Body Merchant merchant);
}