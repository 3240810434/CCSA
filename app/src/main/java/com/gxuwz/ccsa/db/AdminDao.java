package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gxuwz.ccsa.model.Admin;

@Dao
public interface AdminDao {
    @Query("SELECT * FROM admin WHERE account = :account")
    Admin findByAccount(String account);

    @Insert
    void insert(Admin admin);
}
