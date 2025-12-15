package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Admin;

@Dao
public interface AdminDao {
    @Query("SELECT * FROM admin WHERE account = :account")
    Admin findByAccount(String account);

    // 【新增】根据ID查找管理员，用于在消息列表中确认身份
    @Query("SELECT * FROM admin WHERE id = :id")
    Admin findById(int id);

    @Insert
    void insert(Admin admin);

    @Update
    void update(Admin admin);
}