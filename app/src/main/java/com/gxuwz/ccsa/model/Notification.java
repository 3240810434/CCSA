package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "notifications")
public class Notification {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long adminNoticeId; // 关联管理员发布的通知ID，用于级联删除
    private String community; // 小区名称
    private String recipientPhone; // 接收者手机号
    private String title; // 通知标题
    private String content; // 通知内容
    private int type; // 通知类型：1-缴费提醒, 2-管理员公告
    private String attachmentPath; // 附件路径
    private String publisher; // 发布人
    private Date createTime; // 创建时间
    private boolean isRead; // 是否已读

    // 默认构造函数供Room使用 (Room将使用此构造函数+Setters来还原数据)
    public Notification() {}

    // 用于旧代码兼容的构造函数，标记为忽略
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

    // 【修改点】新增 @Ignore 注解
    // 完整构造函数（用于新建通知对象，不包含自增ID），告诉 Room 忽略这个，使用上面的无参构造
    @Ignore
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

    // Getter和Setter方法
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