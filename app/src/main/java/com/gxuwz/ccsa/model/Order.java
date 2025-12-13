package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "orders")
public class Order implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String orderNo;       // 订单编号
    public String createTime;    // 下单时间
    public String status;        // "待接单", "配送中", "已完成"

    // --- 居民信息 ---
    public String residentId;
    public String residentName;
    public String residentPhone;
    public String address;       // 收货地址

    // --- 商家信息 ---
    public String merchantId;
    public String merchantName;

    // --- 商品快照信息 (严格区分展示用) ---
    public String productId;
    public String productName;
    public String productType;   // "实物" 或 "服务"
    public String productImageUrl;
    public String tags;          // 商品标签

    // 实物特有字段
    public String selectedSpec;  // 选中的规格 (描述+价格)
    public String deliveryMethod;// 配送方式 (商家配送/到店自提)

    // 服务特有字段
    public int serviceCount;     // 购买数量
    public String productUnit;   // 计价单位 (如：次、小时)
    public String unitPrice;     // 基础单价 (下单时的单价)

    // --- 支付信息 ---
    public String payAmount;     // 总支付金额
    public String paymentMethod; // 支付方式 (微信/支付宝)
}