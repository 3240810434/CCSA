package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
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

    // 新增资质相关字段
    // 状态：0=未认证, 1=审核中, 2=已认证, 3=审核未通过
    private int qualificationStatus = 0;

    // 图片URI字符串
    private String idCardFrontUri; // 身份证人像面
    private String idCardBackUri;  // 身份证国徽面
    private String licenseUri;     // 营业执照/技能证书

    // 构造方法
    public Merchant(String community, String merchantName, String contactName,
                    String gender, String phone, String password) {
        this.community = community;
        this.merchantName = merchantName;
        this.contactName = contactName;
        this.gender = gender;
        this.phone = phone;
        this.password = password;
        this.qualificationStatus = 0; // 默认为未认证
    }

    // --- Getter 和 Setter 方法 ---

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

    // 新增字段的 Getter/Setter
    public int getQualificationStatus() { return qualificationStatus; }
    public void setQualificationStatus(int qualificationStatus) { this.qualificationStatus = qualificationStatus; }

    public String getIdCardFrontUri() { return idCardFrontUri; }
    public void setIdCardFrontUri(String idCardFrontUri) { this.idCardFrontUri = idCardFrontUri; }

    public String getIdCardBackUri() { return idCardBackUri; }
    public void setIdCardBackUri(String idCardBackUri) { this.idCardBackUri = idCardBackUri; }

    public String getLicenseUri() { return licenseUri; }
    public void setLicenseUri(String licenseUri) { this.licenseUri = licenseUri; }
}