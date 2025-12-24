package com.gxuwz.ccsa.api;

import com.gxuwz.ccsa.common.Result;
import com.gxuwz.ccsa.model.Admin;
import com.gxuwz.ccsa.model.AdminNotice;
import com.gxuwz.ccsa.model.Merchant;
import com.gxuwz.ccsa.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/admin/login")
    Call<Result<Admin>> adminLogin(@Body Admin admin);

    @POST("api/user/login")
    Call<Result<User>> userLogin(@Body User user);

    @POST("api/user/register")
    Call<Result<User>> userRegister(@Body User user);

    @POST("api/merchant/login")
    Call<Result<Merchant>> merchantLogin(@Body Merchant merchant);

    @POST("api/merchant/register")
    Call<Result<Merchant>> merchantRegister(@Body Merchant merchant);

    // --- 通知公告相关接口 ---

    // 管理员：保存或发布通知
    @POST("api/notice/save")
    Call<Result<String>> saveNotice(@Body AdminNotice notice);

    // 管理员：删除通知
    @DELETE("api/notice/delete/{id}")
    Call<Result<String>> deleteNotice(@Path("id") long id);

    // 管理员：获取列表 (status: 0草稿, 1已发布)
    @GET("api/notice/admin/list")
    Call<Result<List<AdminNotice>>> getAdminNoticeList(@Query("status") Integer status);

    // 居民/商家：获取通知列表 (读取 Redis 缓存)
    @GET("api/notice/user/list")
    Call<Result<List<AdminNotice>>> getUserNoticeList(@Query("userType") String userType);
}