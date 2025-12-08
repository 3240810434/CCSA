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
    public int userId;
    public String userName;
    public String userAvatar;
    public String content;
    public long createTime;
    public int type; // 0:纯文, 1:图片, 2:视频

    @Ignore
    public List<PostMedia> mediaList;
    @Ignore
    public int commentCount;

    // 新增互动状态字段 (UI状态)
    @Ignore
    public boolean isLiked = false;
    @Ignore
    public boolean isDisliked = false;
}