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

    // 根据ID查找管理员
    @Query("SELECT * FROM admin WHERE id = :id")
    Admin findById(int id);

    // 【新增】根据小区名称查找该小区的管理员 (假设每个小区有一个主管理员)
    @Query("SELECT * FROM admin WHERE community = :community LIMIT 1")
    Admin findByCommunity(String community);

    @Insert
    void insert(Admin admin);

    @Update
    void update(Admin admin);
}