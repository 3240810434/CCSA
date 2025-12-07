// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/db/NotificationDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.gxuwz.ccsa.model.Notification;

import java.util.List;

@Dao
public interface NotificationDao {
    // 插入新通知
    @Insert
    void insert(Notification notification);

    // 根据接收者手机号查询通知
    @Query("SELECT * FROM notifications WHERE recipientPhone = :phone ORDER BY createTime DESC")
    List<Notification> getByRecipientPhone(String phone);

    // 标记通知为已读
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(long id);

    // 查询未读通知数量
    @Query("SELECT COUNT(*) FROM notifications WHERE recipientPhone = :phone AND isRead = 0")
    int getUnreadCount(String phone);
}