package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "product")
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int merchantId; // 关联商家ID
    public String name; // 商品名称
    public String price; // 价格
    public String coverImage; // 封面图片URI
    public String description; // 描述
    public long createTime; // 发布时间

    public Product(int merchantId, String name, String price, String coverImage, String description) {
        this.merchantId = merchantId;
        this.name = name;
        this.price = price;
        this.coverImage = coverImage;
        this.description = description;
        this.createTime = System.currentTimeMillis();
    }
}