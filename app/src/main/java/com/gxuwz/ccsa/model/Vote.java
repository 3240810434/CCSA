// CCSA/app/src/main/java/com/gxuwz/ccsa/model/Vote.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "votes")
public class Vote implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private String community; // 所属小区
    private String publisher; // 发布者账号
    private long publishTime; // 发布时间戳
    private int agreeCount; // 赞成票数
    private int opposeCount; // 反对票数

    // 构造函数
    public Vote(String title, String content, String community, String publisher, long publishTime) {
        this.title = title;
        this.content = content;
        this.community = community;
        this.publisher = publisher;
        this.publishTime = publishTime;
        this.agreeCount = 0;
        this.opposeCount = 0;
    }

    // getter和setter方法
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public long getPublishTime() { return publishTime; }
    public void setPublishTime(long publishTime) { this.publishTime = publishTime; }

    public int getAgreeCount() { return agreeCount; }
    public void setAgreeCount(int agreeCount) { this.agreeCount = agreeCount; }

    public int getOpposeCount() { return opposeCount; }
    public void setOpposeCount(int opposeCount) { this.opposeCount = opposeCount; }
}