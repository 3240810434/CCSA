package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
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

    @Delete
    void delete(Merchant merchant);

    // 根据小区查找商家（用于管理员管理列表）
    @Query("SELECT * FROM merchant WHERE community = :community")
    List<Merchant> findByCommunity(String community);

    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    Merchant findById(int id);

    // 原有的查找所有待审核
    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1")
    List<Merchant> findPendingAudits();

    // 新增：根据小区查找待审核商家 (支持商家选择了多个小区的情况，如 "小区A,小区B")
    // 使用 LIKE 进行模糊匹配，只要商家的 community 字段包含管理员负责的小区名即可
    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1 AND community LIKE '%' || :adminCommunity || '%'")
    List<Merchant> findPendingAuditsByCommunity(String adminCommunity);
}