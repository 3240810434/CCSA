package com.gxuwz.ccsa.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "chat_message")
public class ChatMessage implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int senderId;   // 发送者ID
    public int receiverId; // 接收者ID
    public String content; // 内容
    public long createTime;// 时间戳

    // 【新增】逻辑删除标记
    // 默认值为0 (false)，1 (true)
    @ColumnInfo(defaultValue = "0")
    public boolean isDeletedBySender;

    @ColumnInfo(defaultValue = "0")
    public boolean isDeletedByReceiver;

    // 辅助字段：用于消息列表展示对方信息
    @Ignore
    public String targetName;
    @Ignore
    public String targetAvatar;
}