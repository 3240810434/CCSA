package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "admin_notices")
public class AdminNotice {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private String targetType; // "RESIDENT", "MERCHANT", "BOTH"
    private String targetBuildings; // "ALL" 或 "1,3,5"
    private String attachmentPath;
    private int status; // 0-草稿, 1-已发布
    private Date createTime;
    private Date publishTime;

    public AdminNotice(String title, String content, String targetType, String targetBuildings, String attachmentPath, int status, Date createTime, Date publishTime) {
        this.title = title;
        this.content = content;
        this.targetType = targetType;
        this.targetBuildings = targetBuildings;
        this.attachmentPath = attachmentPath;
        this.status = status;
        this.createTime = createTime;
        this.publishTime = publishTime;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTargetBuildings() { return targetBuildings; }
    public void setTargetBuildings(String targetBuildings) { this.targetBuildings = targetBuildings; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getPublishTime() { return publishTime; }
    public void setPublishTime(Date publishTime) { this.publishTime = publishTime; }
}