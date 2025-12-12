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

    public String type;           // "实物" 或 "服务"
    public String priceTableJson; // JSON字符串存储价格表
    public int deliveryMethod;    // 0: 商家配送, 1: 自提
    public String createTime;     // 发布时间

    public String price;          // 单价
    public String coverImage;     // 封面图
    public String tag;            // 商品标签
    public String unit;           // 单位

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

    // --- 现有的 Getter ---
    public int getId() { return id; }
    public int getMerchantId() { return merchantId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrls() { return imagePaths; }
    public String getType() { return type; }
    public String getPrice() { return price; }
    public String getUnit() { return unit; }

    // --- 【请添加下面这个方法来修复报错】 ---
    public String getFirstImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) {
            String[] paths = imagePaths.split(",");
            if (paths.length > 0) {
                return paths[0];
            }
        }
        return null;
    }
}