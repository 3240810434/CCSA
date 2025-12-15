package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore; // 记得导入这个
import androidx.room.PrimaryKey;

@Entity(tableName = "admin")
public class Admin {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String account;
    private String password;
    private String community; // 可管理的小区

    // --- 新增：Room 必须需要的无参构造函数 ---
    public Admin() {
    }


    @Ignore
    public Admin(String account, String password, String community) {
        this.account = account;
        this.password = password;
        this.community = community;
    }

    // Getter和Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
}