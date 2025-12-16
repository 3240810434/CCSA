// 路径：CCSA/app/src/main/java/com/gxuwz/ccsa/model/Notification.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long adminNoticeId; // 关联管理员发布的通知ID，用于级联删除
    private String community;
    private String recipientPhone;
    private String title;
    private String content;
    private int type; // 1-缴费提醒, 2-管理员公告
    private String attachmentPath; // 附件路径
    private String publisher; // 发布人
    private Date createTime;
    private boolean isRead;

    // 默认构造函数供Room使用
    public Notification() {}

    @Ignore
    public Notification(String community, String recipientPhone, String title, String content, int type, Date createTime, boolean isRead) {
        this.community = community;
        this.recipientPhone = recipientPhone;
        this.title = title;
        this.content = content;
        this.type = type;
        this.createTime = createTime;
        this.isRead = isRead;
        this.publisher = "系统通知";
    }

    // 完整构造函数
    public Notification(long adminNoticeId, String community, String recipientPhone, String title, String content, int type, String attachmentPath, String publisher, Date createTime, boolean isRead) {
        this.adminNoticeId = adminNoticeId;
        this.community = community;
        this.recipientPhone = recipientPhone;
        this.title = title;
        this.content = content;
        this.type = type;
        this.attachmentPath = attachmentPath;
        this.publisher = publisher;
        this.createTime = createTime;
        this.isRead = isRead;
    }

    // Getter和Setter
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAdminNoticeId() { return adminNoticeId; }
    public void setAdminNoticeId(long adminNoticeId) { this.adminNoticeId = adminNoticeId; }
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
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}