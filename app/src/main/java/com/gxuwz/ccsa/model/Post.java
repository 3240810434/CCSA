package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "post")
public class Post implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int userId; // 发布者ID
    public String userName; // 发布者名字（冗余存储方便查询）
    public String userAvatar; // 发布者头像
    public String content; // 文字内容
    public long createTime; // 发布时间
    public int type; // 0:纯文, 1:图片, 2:视频

    @Ignore
    public List<PostMedia> mediaList; // 关联的媒体文件
    @Ignore
    public int commentCount; // 评论数（用于UI显示）
}