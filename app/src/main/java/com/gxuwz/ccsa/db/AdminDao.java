package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gxuwz.ccsa.model.Admin;
import java.util.List; // 记得导入 List

@Dao
public interface AdminDao {
    @Query("SELECT * FROM admin WHERE account = :account")
    Admin findByAccount(String account);

    @Query("SELECT * FROM admin WHERE id = :id")
    Admin findById(int id);

    @Query("SELECT * FROM admin WHERE community = :community LIMIT 1")
    Admin findByCommunity(String community);

    @Query("SELECT * FROM admin WHERE community = :community")
    List<Admin> findAllByCommunity(String community);

    // 【新增】获取所有管理员，用于构建屏蔽名单
    @Query("SELECT * FROM admin")
    List<Admin> getAll();

    @Insert
    void insert(Admin admin);

    @Update
    void update(Admin admin);
}