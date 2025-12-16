package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity(tableName = "votes")
public class Vote implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private String community;
    private String publisher;
    private long publishTime;

    // 新增字段
    private String optionString; // 选项字符串，用 "|#|" 分隔
    private int selectionType;   // 0: 单选, 1: 多选
    private int status;          // 0: 草稿, 1: 已发布
    private String attachmentPath; // 附件路径

    public Vote(String title, String content, String community, String publisher, long publishTime, String optionString, int selectionType, int status, String attachmentPath) {
        this.title = title;
        this.content = content;
        this.community = community;
        this.publisher = publisher;
        this.publishTime = publishTime;
        this.optionString = optionString;
        this.selectionType = selectionType;
        this.status = status;
        this.attachmentPath = attachmentPath;
    }

    // 辅助方法：获取选项列表
    @Ignore
    public List<String> getOptionList() {
        if (optionString == null || optionString.isEmpty()) return new ArrayList<>();
        return Arrays.asList(optionString.split("\\|#\\|"));
    }

    // Getters and Setters
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
    public String getOptionString() { return optionString; }
    public void setOptionString(String optionString) { this.optionString = optionString; }
    public int getSelectionType() { return selectionType; }
    public void setSelectionType(int selectionType) { this.selectionType = selectionType; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getAttachmentPath() { return attachmentPath; }
    public void setAttachmentPath(String attachmentPath) { this.attachmentPath = attachmentPath; }
}