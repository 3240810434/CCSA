package com.gxuwz.ccsa.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "repairs")
public class Repair implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String repairNo; // 报修单号

    @NonNull
    private String userId; // 用户ID(手机号)

    private String userName; // 用户名
    private String userPhone; // 用户电话
    private String community; // 小区
    private String building; // 楼栋
    private String room; // 房号

    @NonNull
    private String title; // 报修标题

    @NonNull
    private String description; // 报修详情

    private String imageUrls; // 图片URL，用逗号分隔
    private String videoUrl; // 视频URL

    private int status; // 状态：0-受理中，1-已完成

    private long submitTime; // 提交时间
    private long completeTime; // 完成时间

    public Repair() {
        this.repairNo = generateRepairNo();
        this.status = 0;
        this.submitTime = new Date().getTime();
    }

    // 生成唯一报修单号
    private String generateRepairNo() {
        String prefix = "BX";
        String timeStr = String.valueOf(System.currentTimeMillis()).substring(4, 13);
        String randomStr = String.valueOf((int)(Math.random() * 1000)).replace("0", "1");
        return prefix + timeStr + randomStr;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getRepairNo() {
        return repairNo;
    }

    public void setRepairNo(@NonNull String repairNo) {
        this.repairNo = repairNo;
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
    }
}