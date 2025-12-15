package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete; // 新增导入
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gxuwz.ccsa.model.Merchant;
import java.util.List;

@Dao
public interface MerchantDao {
    @Insert
    long insert(Merchant merchant);

    @Update
    void update(Merchant merchant);

    @Delete // 新增：删除商家
    void delete(Merchant merchant);

    // 新增：根据小区查找商家（用于管理员管理列表）
    @Query("SELECT * FROM merchant WHERE community = :community")
    List<Merchant> findByCommunity(String community);

    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    Merchant findById(int id);

    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1")
    List<Merchant> findPendingAudits();
}