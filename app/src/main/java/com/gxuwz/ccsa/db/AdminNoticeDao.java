// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/db/AdminNoticeDao.java
package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.gxuwz.ccsa.model.AdminNotice;
import java.util.List;

@Dao
public interface AdminNoticeDao {
    @Insert
    long insert(AdminNotice notice);

    @Update
    void update(AdminNotice notice);

    @Delete
    void delete(AdminNotice notice);

    // 查询发布列表 (status = 1)
    @Query("SELECT * FROM admin_notices WHERE status = 1 ORDER BY publishTime DESC")
    List<AdminNotice> getPublishedNotices();

    // 查询草稿列表 (status = 0)
    @Query("SELECT * FROM admin_notices WHERE status = 0 ORDER BY createTime DESC")
    List<AdminNotice> getDraftNotices();

    @Query("SELECT * FROM admin_notices WHERE id = :id")
    AdminNotice getById(long id);
}