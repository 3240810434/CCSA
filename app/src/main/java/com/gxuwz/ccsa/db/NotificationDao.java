// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/db/NotificationDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.Notification;
import java.util.List;

@Dao
public interface NotificationDao {

    // 插入单条通知
    @Insert
    void insert(Notification notification);

    // 插入多条通知
    @Insert
    void insertAll(List<Notification> notifications);

    /**
     * 原有查询方法：根据接收者手机号获取通知
     * (保留此方法以防居民端或其他旧代码正在使用它)
     */
    @Query("SELECT * FROM notifications WHERE recipientPhone = :phone ORDER BY createTime DESC")
    List<Notification> getByRecipientPhone(String phone);

    /**
     * 【新增】根据接收者手机号查询通知
     * (专门用于商家端获取通知，修复商家无法获取通知的问题)
     */
    @Query("SELECT * FROM notifications WHERE recipientPhone = :phone ORDER BY createTime DESC")
    List<Notification> findByRecipientPhone(String phone);

    // 标记通知为已读
    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    void markAsRead(long id);

    // 获取未读通知数量
    @Query("SELECT COUNT(*) FROM notifications WHERE recipientPhone = :phone AND isRead = 0")
    int getUnreadCount(String phone);

    // 根据管理员通知ID删除所有分发的通知（实现撤回/删除功能）
    @Query("DELETE FROM notifications WHERE adminNoticeId = :adminNoticeId")
    void deleteByAdminNoticeId(long adminNoticeId);
}