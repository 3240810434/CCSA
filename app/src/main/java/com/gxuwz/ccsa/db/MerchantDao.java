package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

@Dao
public interface MerchantDao {
    // 【修复 1】：将返回值从 void 改为 long，以便获取自动生成的 ID
    @Insert
    long insert(Merchant merchant);

    @Update
    void update(Merchant merchant);

    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    Merchant findById(int id);

    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1")
    List<Merchant> findPendingAudits();
}