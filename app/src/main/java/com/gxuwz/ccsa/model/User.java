package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "user")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String gender;
    private String phone;
    private String password;
    private String community;
    private String building;
    private String room;
    private String avatar;

    public User() {
    }

    @Ignore
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

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // 新增 getUsername 兼容方法
    public String getUsername() { return name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getCommunityName() { return community; }
    public String getRoomNumber() { return room; }
}