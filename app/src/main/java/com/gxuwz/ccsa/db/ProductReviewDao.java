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

    // 修改参数类型为 long
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY createTime DESC LIMIT 2")
    List<ProductReview> getTop2Reviews(long productId);

    // 修改参数类型为 long
    @Query("SELECT * FROM product_reviews WHERE productId = :productId ORDER BY createTime DESC")
    List<ProductReview> getAllReviews(long productId);
}