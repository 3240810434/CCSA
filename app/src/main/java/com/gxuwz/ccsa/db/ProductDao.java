package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.gxuwz.ccsa.model.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Delete
    void delete(Product product);

    @Update
    void update(Product product);

    @Query("SELECT * FROM product")
    List<Product> getAllProducts();

    @Query("SELECT * FROM product WHERE merchantId = :merchantId ORDER BY id DESC")
    List<Product> getProductsByMerchantId(int merchantId);

    @Query("SELECT * FROM product WHERE id = :productId")
    Product getProductById(int productId);

    /**
     * 根据住户所在小区筛选商品
     * 原理：内连接 Merchant 表，查找商家服务小区字段 (community) 中包含住户小区名称 (userCommunity) 的所有商品
     * 使用 || 进行字符串拼接，匹配如 "%用户小区名%"
     */
    @Query("SELECT product.* FROM product " +
            "INNER JOIN merchant ON product.merchantId = merchant.id " +
            "WHERE merchant.community LIKE '%' || :userCommunity || '%' " +
            "ORDER BY product.createTime DESC")
    List<Product> getProductsByCommunity(String userCommunity);
}