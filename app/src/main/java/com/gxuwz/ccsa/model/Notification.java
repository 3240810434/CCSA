// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/model/Notification.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String community; // 小区名称
    private String recipientPhone; // 接收者手机号
    private String title; // 通知标题
    private String content; // 通知内容
    private int type; // 通知类型：1-缴费提醒
    private Date createTime; // 创建时间
    private boolean isRead; // 是否已读

    // 构造函数
    public Notification(String community, String recipientPhone, String title, String content, int type, Date createTime, boolean isRead) {
        this.community = community;
        this.recipientPhone = recipientPhone;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
        this.isRead = isRead;
    }

    // Getter和Setter方法
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}