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
    public String imagePaths;     // 图片路径，用逗号分隔: "path1,path2"
    public String description;    // 详细描述

    // 新增字段
    public String type;           // "GOODS" (实物) 或 "SERVICE" (服务)
    public String priceTableJson; // JSON字符串存储价格表: [{"desc":"香蕉","price":"5"},{"desc":"苹果","price":"5"}]
    public int deliveryMethod;    // 0: 商家配送, 1: 自提
    public String createTime;     // 发布时间

    // 1. 无参构造函数 (Room 数据库读取数据时需要)
    public Product() {
    }

    // 2. 全参构造函数 (方便在 Activity 中创建新对象)
    // 注意：id 是自增的，所以在创建新对象时不需要传入 id
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
}