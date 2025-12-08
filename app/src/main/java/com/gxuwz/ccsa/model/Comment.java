package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comment")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId;
    public int userId;
    public String userName;
    public String userAvatar;
    public String content;
    public long createTime;
    public int parentId; // 如果是回复某条评论，这里存父评论ID，否则为0
    public String replyToUserName; // 回复给谁
}