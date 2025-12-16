package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_record")
public class HistoryRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;

    // 关联的内容ID (Post ID 或 HelpPost ID)
    public int relatedId;

    // 类型：1 = 生活动态(Post), 2 = 邻里互助(HelpPost)
    public int type;

    public String title;       // 标题或内容摘要
    public String coverImage;  // 封面图或头像
    public String authorName;  // 作者名
    public long viewTime;      // 浏览时间

    public HistoryRecord(int userId, int relatedId, int type, String title, String coverImage, String authorName, long viewTime) {
        this.userId = userId;
        this.relatedId = relatedId;
        this.type = type;
        this.title = title;
        this.coverImage = coverImage;
        this.authorName = authorName;
        this.viewTime = viewTime;
    }
}