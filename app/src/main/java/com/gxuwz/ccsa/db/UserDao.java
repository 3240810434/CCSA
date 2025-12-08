// CCSA/app/app/src/main/java/com/gxuwz/ccsa/db/UserDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.User;
import java.util.List;
import androidx.room.Update; 

@Dao
public interface UserDao {
    /**
     * 插入新用户（注册时使用）
     */
    @Insert
    void insert(User user);

    /**
     * 手机号+密码登录验证
     */
    @Query("SELECT * FROM user WHERE phone = :phone AND password = :password LIMIT 1")
    User login(String phone, String password);

    /**
     * 根据手机号查询用户（判断是否已注册）
     */
    @Query("SELECT * FROM user WHERE phone = :phone LIMIT 1")
    User findByPhone(String phone);

    /**
     * 根据小区查询居民并按楼栋和房号排序
     */
    @Query("SELECT * FROM user WHERE community = :community ORDER BY building ASC, room ASC")
    List<User> findResidentsByCommunity(String community);

    /**
     * 添加按小区查询居民的基础方法（可按姓名排序）
     */
    @Query("SELECT * FROM user WHERE community = :community ORDER BY name ASC")
    List<User> findByCommunity(String community); // 新增此方法

    /**
     * 按楼栋和房间号升序排序的查询方法
     */
    @Query("SELECT * FROM user WHERE community = :community ORDER BY building ASC, room ASC")
    List<User> findResidentsByCommunitySorted(String community);


    // 新增：统计指定社区的用户数量
    @Query("SELECT COUNT(*) FROM user WHERE community = :community")
    int countByCommunity(String community);

    // 修正后代码（删除role条件，假设通过其他方式区分居民）：
    @Query("SELECT * FROM user WHERE community = :community")
    List<User> getResidentsByCommunity(String community);

    // 在 CCSA/app/src/main/java/com/gxuwz/ccsa/db/UserDao.java 中添加
    @Query("SELECT * FROM user WHERE community = :community AND building = :building AND room = :roomNumber LIMIT 1")
    User getByRoom(String community, String building, String roomNumber);

    // 在 UserDao 接口中添加
    @Query("SELECT * FROM user WHERE phone = :phone")
    User getByPhone(String phone);

    // 新增：更新用户信息
    @Update
    void update(User user);
}