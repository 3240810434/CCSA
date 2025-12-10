// CCSA/app/src/main/java/com/gxuwz/ccsa/model/Merchant.java
package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "merchant")
public class Merchant implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String community; // 小区（多个小区用逗号分隔）
    private String merchantName; // 商家名称
    private String contactName; // 联系人姓名
    private String gender; // 性别
    private String phone; // 手机号（登录账号）
    private String password; // 密码
    private String avatar; // 新增字段
    // 构造方法
    public Merchant(String community, String merchantName, String contactName,
                    String gender, String phone, String password) {
        this.community = community;
        this.merchantName = merchantName;
        this.contactName = contactName;
        this.gender = gender;
        this.phone = phone;
        this.password = password;
    }

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Getter 和 Setter
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar;}
}