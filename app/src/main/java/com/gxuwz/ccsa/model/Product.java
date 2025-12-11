package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "product")
public class Product implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int merchantId;        // 商家ID
    public String name;           // 商品名称
    public String imagePaths;     // 图片路径，用逗号分隔
    public String description;    // 详细描述

    public String type;           // "GOODS" 或 "SERVICE"
    public String priceTableJson; // JSON字符串存储价格表
    public int deliveryMethod;    // 0: 商家配送, 1: 自提
    public String createTime;     // 发布时间

    // 无参构造函数
    public Product() {
    }

    @Ignore
    public Product(int merchantId, String name, String imagePaths, String description,
                   String type, String priceTableJson, int deliveryMethod, String createTime) {
        this.merchantId = merchantId;
        this.name = name;
        this.imagePaths = imagePaths;
        this.description = description;
        this.type = type;
        this.priceTableJson = priceTableJson;
        this.deliveryMethod = deliveryMethod;
        this.createTime = createTime;
    }

    // --- 新增：解决 Cannot resolve method 'getFirstImage' ---
    public String getFirstImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            // 分割字符串获取第一张图片路径
            return imagePaths.split(",")[0];
        }
        return ""; // 如果没有图片，返回空字符串
    }
}