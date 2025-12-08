package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "post_media")
public class PostMedia implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int postId; // 关联的帖子ID
    public String url; // 本地路径或网络URL
    public int type; // 1:图片, 2:视频
}