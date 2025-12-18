package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.ProductReview;
import java.util.List;

@Dao
public interface ProductReviewDao {
    @Insert
    void insert(ProductReview review);

    // 获取某商品的最新2条评价（用于详情页展示）
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY createTime DESC LIMIT 2")
    List<ProductReview> getTop2Reviews(int productId);

    // 获取某商品的所有评价
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY createTime DESC")
    List<ProductReview> getAllReviews(int productId);
}