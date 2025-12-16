// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/db/NotificationDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.Notification;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(Notification notification);

    @Insert
    void insertAll(List<Notification> notifications);

    @Query("SELECT * FROM notifications WHERE recipientPhone = :phone ORDER BY createTime DESC")
    List<Notification> getByRecipientPhone(String phone);

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(long id);

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientPhone = :phone AND isRead = 0")
    int getUnreadCount(String phone);

    // 根据管理员通知ID删除所有分发的通知（实现撤回/删除功能）
    @Query("DELETE FROM notifications WHERE adminNoticeId = :adminNoticeId")
    void deleteByAdminNoticeId(long adminNoticeId);
}