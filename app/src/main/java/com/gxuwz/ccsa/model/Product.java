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

    // --- 新增：补全缺失字段，解决 PhysicalProductEditActivity 中的报错 ---
    public String price;          // 单价（兼容旧逻辑）
    public String coverImage;     // 封面图（兼容旧逻辑）

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

    // 获取第一张图片作为封面
    public String getFirstImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            return imagePaths.split(",")[0];
        }
        return "";
    }
}