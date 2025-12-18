package com.gxuwz.ccsa.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "product_reviews")
public class ProductReview {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int productId;      // 商品ID
    public int userId;         // 评价人ID
    public String userName;    // 评价人昵称
    public String userAvatar;  // 评价人头像URL或路径
    public int score;          // 分数 (2, 4, 6, 8, 10)
    public String content;     // 评价内容
    public String imagePaths;  // 图片路径，多张图片用逗号分隔
    public long createTime;    // 评价时间

    // 构造函数
    public ProductReview(int productId, int userId, String userName, String userAvatar, int score, String content, String imagePaths, long createTime) {
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.score = score;
        this.content = content;
        this.imagePaths = imagePaths;
        this.createTime = createTime;
    }
}