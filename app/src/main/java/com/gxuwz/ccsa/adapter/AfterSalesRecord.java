package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.io.Serializable;

@Entity(tableName = "after_sales_records")
public class AfterSalesRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "order_id")
    public Long orderId; // 关联的订单ID

    @ColumnInfo(name = "type")
    public String type; // 售后类型：仅退款/退货退款/换货

    @ColumnInfo(name = "reason")
    public String reason; // 申请原因

    @ColumnInfo(name = "description")
    public String description; // 详细描述

    @ColumnInfo(name = "image_paths")
    public String imagePaths; // 图片路径，多个用分号分隔

    @ColumnInfo(name = "merchant_reply")
    public String merchantReply; // 商家拒绝理由或回复

    @ColumnInfo(name = "create_time")
    public String createTime; // 申请时间

    // 空构造函数供Room使用
    public AfterSalesRecord() {}

    public AfterSalesRecord(Long orderId, String type, String reason, String description, String imagePaths, String createTime) {
        this.orderId = orderId;
        this.type = type;
        this.reason = reason;
        this.description = description;
        this.imagePaths = imagePaths;
        this.createTime = createTime;
    }
}