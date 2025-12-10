package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "merchant")
public class Merchant implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String community; // 小区
    private String merchantName; // 商家名称
    private String contactName; // 联系人姓名
    private String gender; // 性别
    private String phone; // 手机号
    private String password; // 密码
    private String avatar; // 头像

    // 店铺状态：true=开启, false=关闭
    private boolean isOpen = true;

    // 资质相关字段
    private int qualificationStatus = 0;
    private String idCardFrontUri;
    private String idCardBackUri;
    private String licenseUri;

    public Merchant(String community, String merchantName, String contactName,
                    String gender, String phone, String password) {
        this.community = community;
        this.merchantName = merchantName;
        this.contactName = contactName;
        this.gender = gender;
        this.phone = phone;
        this.password = password;
        this.qualificationStatus = 0;
        this.isOpen = true; // 默认开启
    }

    // --- Getter 和 Setter ---
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
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public boolean isOpen() { return isOpen; }
    public void setOpen(boolean open) { isOpen = open; }
    public int getQualificationStatus() { return qualificationStatus; }
    public void setQualificationStatus(int qualificationStatus) { this.qualificationStatus = qualificationStatus; }
    public String getIdCardFrontUri() { return idCardFrontUri; }
    public void setIdCardFrontUri(String idCardFrontUri) { this.idCardFrontUri = idCardFrontUri; }
    public String getIdCardBackUri() { return idCardBackUri; }
    public void setIdCardBackUri(String idCardBackUri) { this.idCardBackUri = idCardBackUri; }
    public String getLicenseUri() { return licenseUri; }
    public void setLicenseUri(String licenseUri) { this.licenseUri = licenseUri; }
}