// CCSA/app/src/main/java/com/gxuwz/ccsa/model/FeeAnnouncement.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "fee_announcement")
public class FeeAnnouncement implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String community; // 小区
    private String title; // 标题
    private String content; // 内容
    private String startTime; // 公示开始时间
    private String endTime; // 公示结束时间
    private long publishTime; // 发布时间
    private String publisher; // 发布人

    // 构造方法、getter和setter
    public FeeAnnouncement(String community, String title, String content,
                           String startTime, String endTime, long publishTime, String publisher) {
        this.community = community;
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.publishTime = publishTime;
        this.publisher = publisher;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public long getPublishTime() { return publishTime; }
    public void setPublishTime(long publishTime) { this.publishTime = publishTime; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
}

