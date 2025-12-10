// 文件路径：app/src/main/java/com/gxuwz/ccsa/db/MerchantDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update; // 新增导入
import androidx.room.Query;
import com.gxuwz.ccsa.model.Merchant;


@Dao
public interface MerchantDao {
    // 注册商家
    @Insert
    void insert(Merchant merchant);

    // 新增：更新商家信息（用于修改头像和名称）
    @Update
    void update(Merchant merchant);

    // 商家登录（通过手机号和密码查询）
    @Query("SELECT * FROM merchant WHERE phone = :phone AND password = :password LIMIT 1")
    Merchant login(String phone, String password);

    // 检查手机号是否已注册
    @Query("SELECT * FROM merchant WHERE phone = :phone LIMIT 1")
    Merchant findByPhone(String phone);
}