package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gxuwz.ccsa.model.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user WHERE phone = :phone AND password = :password LIMIT 1")
    User login(String phone, String password);

    @Query("SELECT * FROM user WHERE phone = :phone LIMIT 1")
    User findByPhone(String phone);

    @Query("SELECT * FROM user WHERE id = :id")
    User getUserById(int id);

    // --- 新增此方法，Activity 中使用的是 long 类型 ---
    @Query("SELECT * FROM user WHERE id = :id")
    User findById(long id);

    @Query("SELECT * FROM user WHERE community = :community ORDER BY building ASC, room ASC")
    List<User> findResidentsByCommunity(String community);

    @Query("SELECT * FROM user WHERE community = :community ORDER BY name ASC")
    List<User> findByCommunity(String community);

    @Query("SELECT * FROM user WHERE community = :community ORDER BY building ASC, room ASC")
    List<User> findResidentsByCommunitySorted(String community);

    @Query("SELECT COUNT(*) FROM user WHERE community = :community")
    int countByCommunity(String community);

    @Query("SELECT * FROM user WHERE community = :community")
    List<User> getResidentsByCommunity(String community);

    @Query("SELECT * FROM user WHERE community = :community AND building = :building AND room = :roomNumber LIMIT 1")
    User getByRoom(String community, String building, String roomNumber);

    @Query("SELECT * FROM user WHERE phone = :phone")
    User getByPhone(String phone);

    @Update
    void update(User user);
}