package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.Ignore; // 必须导入 Ignore
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.io.Serializable;

@Entity(tableName = "after_sales_records")
public class AfterSalesRecord implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "order_id")
    public Long orderId;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "reason")
    public String reason;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "image_paths")
    public String imagePaths;

    @ColumnInfo(name = "merchant_reply")
    public String merchantReply;

    @ColumnInfo(name = "create_time")
    public String createTime;

    // Room 默认使用这个无参构造函数
    public AfterSalesRecord() {}

    // 使用 @Ignore 消除警告，告诉 Room 不要尝试使用这个构造函数来映射数据
    @Ignore
    public AfterSalesRecord(Long orderId, String type, String reason, String description, String imagePaths, String createTime) {
        this.orderId = orderId;
        this.type = type;
        this.reason = reason;
        this.description = description;
        this.imagePaths = imagePaths;
        this.createTime = createTime;
    }
}