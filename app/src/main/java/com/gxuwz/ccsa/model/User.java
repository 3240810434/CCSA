package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "user") // Room数据库表名
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id; // 自增主键
    private String name; // 姓名
    private String gender; // 性别
    private String phone; // 手机号（登录账号）
    private String password; // 密码
    private String community; // 所在小区
    private String building; // 楼栋
    private String room; // 房间号

    // 完整的7参数构造器（匹配注册时的参数）
    public User(String name, String gender, String phone, String password,
                String community, String building, String room) {
        this.name = name;
        this.gender = gender;
        this.phone = phone;
        this.password = password;
        this.community = community;
        this.building = building;
        this.room = room;
    }

    // 所有字段的Getter和Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}