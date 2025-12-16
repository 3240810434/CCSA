package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.gxuwz.ccsa.model.HistoryRecord;
import java.util.List;

@Dao
public interface HistoryDao {
    // 插入记录，如果同一用户同一帖子已存在，则替换（更新时间）
    // 注意：Room默认的REPLACE策略是基于主键的。如果想实现“浏览同一个帖子更新时间”，
    // 可以在业务逻辑中先删除旧的再插入，或者只插入新的。
    // 这里简单处理：直接插入。为了避免重复过多，展示时可以去重，或者在插入前手动检查。
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryRecord record);

    @Query("SELECT * FROM history_record WHERE userId = :userId ORDER BY viewTime DESC")
    List<HistoryRecord> getUserHistory(int userId);

    @Query("DELETE FROM history_record WHERE userId = :userId")
    void clearHistory(int userId);

    // 辅助方法：删除特定的一条记录（为了插入新的以更新时间）
    @Query("DELETE FROM history_record WHERE userId = :userId AND relatedId = :relatedId AND type = :type")
    void deleteRecord(int userId, int relatedId, int type);
}