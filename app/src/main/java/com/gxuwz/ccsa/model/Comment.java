package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "comment")
public class Comment implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId;
    public int userId;
    public String userName;
    public String userAvatar; // 新增：保存评论时的头像
    public String content;
    public long createTime;
}