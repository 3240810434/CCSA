package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "help_post")
public class HelpPost implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId;
    public String title; // 标题（新增）
    public String content;
    public long createTime;
    public int type; // 0:纯文, 1:图片, 2:视频

    // 辅助字段，不存数据库，用于UI显示
    @Ignore
    public String userName;
    @Ignore
    public String userAvatar;
    @Ignore
    public List<HelpPostMedia> mediaList;
}