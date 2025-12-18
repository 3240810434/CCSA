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

    // 修复报错：Cannot resolve method 'getAllMerchants'
    @Query("SELECT * FROM merchant")
    List<Merchant> getAllMerchants();

    // 核心修改：将原本的 = :community 改为 LIKE 模糊查询
    // 使用 '%' || :community || '%' 来匹配包含该小区名称的字符串
    // 例如：当 community 为 "A小区" 时，它可以匹配 "A小区,B小区" 或 "B小区,A小区"
    @Query("SELECT * FROM merchant WHERE community LIKE '%' || :community || '%'")
    List<Merchant> findByCommunity(String community);

    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);

    @Query("SELECT * FROM merchant WHERE id = :id LIMIT 1")
    Merchant findById(int id);

    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1")
    List<Merchant> findPendingAudits();

    // 此处原本已经是正确的 LIKE 写法，findByCommunity 应与此处保持一致逻辑
    @Query("SELECT * FROM merchant WHERE qualificationStatus = 1 AND community LIKE '%' || :adminCommunity || '%'")
    List<Merchant> findPendingAuditsByCommunity(String adminCommunity);
}