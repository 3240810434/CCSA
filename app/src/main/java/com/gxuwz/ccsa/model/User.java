package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "user")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String password;
    private String mobile;
    private String community; // 小区
    private String building;  // 楼栋
    private String room;      // 房号

    // 新增：头像路径字段
    private String avatar;

    // 构造函数
    public User(String username, String password, String mobile, String community, String building, String room) {
        this.username = username;
        this.password = password;
        this.mobile = mobile;
        this.community = community;
        this.building = building;
        this.room = room;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    // 新增头像的 Getter/Setter
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}