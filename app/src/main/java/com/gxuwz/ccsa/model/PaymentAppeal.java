// CCSA/app/src/main/java/com/gxuwz/ccsa/model/PaymentAppeal.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "payment_appeal")
public class PaymentAppeal implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String userId; // 用户ID
    private String userName; // 用户名
    private String community; // 小区
    private String building; // 楼栋
    private String room; // 房号
    private String appealType; // 申诉类型
    private String appealContent; // 申诉内容
    private String relatedPeriod; // 相关周期
    private double relatedAmount; // 相关金额
    private int status; // 0-待处理 1-处理中 2-已解决 3-已驳回
    private long submitTime; // 提交时间
    private String replyContent; // 回复内容
    private long replyTime; // 回复时间
    private String handler; // 处理人

    // 构造方法、getter和setter
    public PaymentAppeal(String userId, String userName, String community, String building,
                         String room, String appealType, String appealContent,
                         String relatedPeriod, double relatedAmount, int status,
                         long submitTime, String replyContent, long replyTime, String handler) {
        this.userId = userId;
        this.userName = userName;
        this.community = community;
        this.building = building;
        this.room = room;
        this.appealType = appealType;
        this.appealContent = appealContent;
        this.relatedPeriod = relatedPeriod;
        this.relatedAmount = relatedAmount;
        this.status = status;
        this.submitTime = submitTime;
        this.replyContent = replyContent;
        this.replyTime = replyTime;
        this.handler = handler;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getAppealType() { return appealType; }
    public void setAppealType(String appealType) { this.appealType = appealType; }
    public String getAppealContent() { return appealContent; }
    public void setAppealContent(String appealContent) { this.appealContent = appealContent; }
    public String getRelatedPeriod() { return relatedPeriod; }
    public void setRelatedPeriod(String relatedPeriod) { this.relatedPeriod = relatedPeriod; }
    public double getRelatedAmount() { return relatedAmount; }
    public void setRelatedAmount(double relatedAmount) { this.relatedAmount = relatedAmount; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public long getSubmitTime() { return submitTime; }
    public void setSubmitTime(long submitTime) { this.submitTime = submitTime; }
    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
    public long getReplyTime() { return replyTime; }
    public void setReplyTime(long replyTime) { this.replyTime = replyTime; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
}

