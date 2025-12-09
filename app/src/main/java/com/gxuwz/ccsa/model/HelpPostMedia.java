package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "help_post_media")
public class HelpPostMedia implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int helpPostId; // 关联 HelpPost 的 id
    public String url;     // 图片或视频路径
    public int type;       // 1:图片, 2:视频
}