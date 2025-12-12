package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "orders")
public class Order implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String orderNo; // 订单编号
    public String residentId; // 居民ID
    public String residentName;
    public String residentPhone;
    public String address; // 完整收货地址

    public String merchantId; // 商家ID
    public String merchantName;

    public String productId; // 商品ID
    public String productName;
    public String productType; // "实物" 或 "服务"
    public String productImageUrl;

    // 实物特有
    public String selectedSpec; // 选中的规格（例如：第一行价格表的内容）

    // 服务特有
    public int serviceCount; // 服务次数/数量

    public String payAmount; // 支付金额
    public String status; // "待接单", "配送中", "已完成"
    public String createTime;

    // 标签等辅助信息
    public String tags;
}