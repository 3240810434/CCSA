package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

@Dao
public interface MerchantDao {
    @Insert
    void insert(Merchant merchant);

    @Update
    void update(Merchant merchant);

    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    Merchant findById(int id);

    // 新增：查询所有提交了审核申请的商家 (状态为 1)
    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1")
    List<Merchant> findPendingAudits();
}